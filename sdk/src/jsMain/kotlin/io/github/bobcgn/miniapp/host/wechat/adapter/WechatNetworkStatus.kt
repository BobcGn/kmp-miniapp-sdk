package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.async.awaitHostCallback
import io.github.bobcgn.miniapp.capability.network.MiniAppNetworkState
import io.github.bobcgn.miniapp.capability.network.MiniAppNetworkStatus
import io.github.bobcgn.miniapp.capability.network.NetworkType
import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.interop.WxNetworkState
import io.github.bobcgn.miniapp.host.wechat.interop.WxNetworkStatusChangeListener
import io.github.bobcgn.miniapp.host.wechat.interop.wxEventNetworkState
import io.github.bobcgn.miniapp.host.wechat.interop.wxQueryNetworkState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

/**
 * WeChat implementation of the platform-neutral network status capability.
 *
 * The concept is common — every host can say whether it is online and over what kind
 * of link — but the vocabulary is not: WeChat's own words are mapped to
 * [NetworkType] where they are recognized and preserved verbatim where they are not,
 * so a connection kind this SDK has never seen is reported rather than dropped.
 *
 * Each collector of [changes] owns its own host registration. That is a deliberate
 * choice over sharing one registration between collectors: WeChat removes listeners
 * by identity, so one shared registration would need reference counting to decide
 * when to remove it, and a mistake there leaks a listener for the lifetime of the
 * mini program. Per-collector registration makes the pairing exact — every
 * registration has exactly one removal, on every termination path — at the cost of
 * one host listener per active collector.
 *
 * Nothing here polls. The host pushes changes; this adapter forwards them.
 */
internal class WechatNetworkStatus(
    private val host: WechatNetworkStatusHost = WxNetworkStatusHost,
) : MiniAppNetworkStatus {
    override suspend fun current(): MiniAppNetworkState {
        if (!host.isQuerySupported()) {
            throw MiniAppException.UnsupportedCapability(MiniAppNetworkStatus.QueryKey)
        }

        // WeChat reports no task handle for this call, so cancellation only stops the
        // caller waiting.
        val state = awaitHostCallback { success, failure ->
            host.query(
                success = { result ->
                    success(wxQueryNetworkState(result.networkType))
                },
                failure = { result ->
                    failure(mapWechatHostFailure(operation = "getNetworkType", result = result))
                },
            )
            null
        }

        return when (state) {
            is WxNetworkState.Present -> state.toNetworkState()

            WxNetworkState.Unreadable -> throw MiniAppException.InvalidResponse(
                "The WeChat host answered getNetworkType with a state the SDK cannot read",
            )
        }
    }

    override val changes: Flow<MiniAppNetworkState> = callbackFlow {
        if (!host.isListenerSupported()) {
            close(MiniAppException.UnsupportedCapability(MiniAppNetworkStatus.ListenerKey))
            return@callbackFlow
        }

        val listener: WxNetworkStatusChangeListener = { result ->
            when (val state = wxEventNetworkState(result.isConnected, result.networkType)) {
                is WxNetworkState.Present -> trySend(state.toNetworkState())

                // A host that reports a state it cannot have meant is a broken host, and
                // reporting no state at all would make this stream lie about it.
                WxNetworkState.Unreadable -> close(
                    MiniAppException.InvalidResponse(
                        "The WeChat host reported a network status the SDK cannot read",
                    ),
                )
            }
        }

        host.addListener(listener)
        awaitClose { host.removeListener(listener) }
    }.conflate()
}

/** Maps a host state onto the neutral model, keeping the host's own word. */
private fun WxNetworkState.Present.toNetworkState(): MiniAppNetworkState = MiniAppNetworkState(
    isConnected = isConnected,
    networkType = wechatNetworkType(networkType),
    hostNetworkType = networkType,
)

/**
 * Resolves the host's own word for a connection kind.
 *
 * The words are WeChat's; this is the only place they are interpreted. `5g` is listed
 * for the query and not for the change event by the installed base library, and this
 * SDK recognizes the union of both so a host that reports either is understood.
 */
private fun wechatNetworkType(hostValue: String): NetworkType? = when (hostValue) {
    "wifi" -> NetworkType.WIFI
    "2g" -> NetworkType.CELLULAR_2G
    "3g" -> NetworkType.CELLULAR_3G
    "4g" -> NetworkType.CELLULAR_4G
    "5g" -> NetworkType.CELLULAR_5G
    "unknown" -> NetworkType.UNKNOWN
    "none" -> NetworkType.NONE
    else -> null
}
