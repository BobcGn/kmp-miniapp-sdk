package io.github.bobcgn.miniapp.host.wechat.interop

import kotlin.js.jsTypeOf

/**
 * Raw callback-style options accepted by [wx.getNetworkType].
 *
 * WeChat declares no options for this call; it answers with the current state.
 */
internal external interface WxGetNetworkTypeOptions {
    /** Called only when the host reports its network state. */
    var success: WxGetNetworkTypeSuccessCallback?

    /** Called when the host cannot answer. */
    var fail: WxGeneralCallback?

    /** Called after either [success] or [fail]. */
    var complete: WxGeneralCallback?
}

/** Raw success result returned by [wx.getNetworkType]. */
internal external interface WxGetNetworkTypeSuccessResult : WxGeneralCallbackResult {
    /**
     * Documented type is `string`.
     *
     * The base library shipped with the installed Developer Tools enumerates
     * `wifi`, `2g`, `3g`, `4g`, `5g`, `unknown`, and `none` for this call.
     */
    val networkType: Any?
}

/** Success callback accepted by [WxGetNetworkTypeOptions]. */
internal typealias WxGetNetworkTypeSuccessCallback = (WxGetNetworkTypeSuccessResult) -> Unit

/**
 * Raw payload WeChat hands to a network status listener.
 *
 * The installed base library enumerates `wifi`, `2g`, `3g`, `4g`, `unknown`, and
 * `none` here — the same vocabulary as [wx.getNetworkType] except that `5g` is not
 * listed for the event. That difference is the host's, so this SDK recognizes the
 * union and always carries the host's own word alongside it.
 */
internal external interface WxNetworkStatusChangeResult {
    /** Documented type is `boolean`: whether the device has a connection. */
    val isConnected: Any?

    /** Documented type is `string`: the host's own name for the connection kind. */
    val networkType: Any?
}

/** Listener signature accepted by [wx.onNetworkStatusChange]. */
internal typealias WxNetworkStatusChangeListener = (WxNetworkStatusChangeResult) -> Unit

/**
 * What a raw network state from WeChat carries.
 *
 * A raw JavaScript object cannot cross out of this package, so this is the typed
 * shape the adapter reads.
 */
internal sealed interface WxNetworkState {
    /** The host reported a usable state. */
    data class Present(val isConnected: Boolean, val networkType: String) : WxNetworkState

    /** The host's answer is not a value the contract can carry. */
    data object Unreadable : WxNetworkState
}

/**
 * Reads a raw `wx.getNetworkType` state.
 *
 * This call reports only the connection kind, so connectivity is read from the host's
 * own word rather than invented: `none` is the word the host uses for having no
 * connection, and every other word it reports — including one this SDK does not
 * recognize — describes a connection it does have.
 */
internal fun wxQueryNetworkState(networkType: Any?): WxNetworkState {
    val hostType = usableNetworkType(networkType) ?: return WxNetworkState.Unreadable
    return WxNetworkState.Present(isConnected = hostType != NO_CONNECTION, networkType = hostType)
}

/**
 * Reads a raw network status event.
 *
 * This shape reports connectivity explicitly, so nothing is derived: a missing or
 * non-boolean `isConnected` is [WxNetworkState.Unreadable].
 */
internal fun wxEventNetworkState(isConnected: Any?, networkType: Any?): WxNetworkState {
    if (isConnected == null || jsTypeOf(isConnected) == "undefined") {
        return WxNetworkState.Unreadable
    }
    if (isConnected !is Boolean) return WxNetworkState.Unreadable
    val hostType = usableNetworkType(networkType) ?: return WxNetworkState.Unreadable
    return WxNetworkState.Present(isConnected = isConnected, networkType = hostType)
}

/** Reads the host's own connection word, or `null` when it is not one the SDK can carry. */
private fun usableNetworkType(raw: Any?): String? =
    if (raw is String && raw.isNotBlank()) raw else null

/** The word WeChat uses for having no connection at all. */
private const val NO_CONNECTION: String = "none"

/**
 * Whether [wx.getNetworkType] exists.
 *
 * The query is probed on its own, because a host may answer it without offering
 * change events.
 */
internal fun hasWxGetNetworkType(): Boolean =
    js("typeof wx !== 'undefined' && typeof wx.getNetworkType === 'function'")

/** Whether [wx.onNetworkStatusChange] exists. */
internal fun hasWxOnNetworkStatusChange(): Boolean =
    js("typeof wx !== 'undefined' && typeof wx.onNetworkStatusChange === 'function'")

/** Whether [wx.offNetworkStatusChange] exists. */
internal fun hasWxOffNetworkStatusChange(): Boolean =
    js("typeof wx !== 'undefined' && typeof wx.offNetworkStatusChange === 'function'")

/**
 * Creates a plain JavaScript option bag for [wx.getNetworkType].
 *
 * External interfaces cannot be instantiated with Kotlin constructors, so the
 * factory exists solely to produce the object the host expects.
 */
internal fun wxGetNetworkTypeOptions(): WxGetNetworkTypeOptions {
    val options: WxGetNetworkTypeOptions = js("({})")
    return options
}
