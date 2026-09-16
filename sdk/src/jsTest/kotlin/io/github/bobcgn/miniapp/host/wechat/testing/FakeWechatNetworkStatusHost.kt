package io.github.bobcgn.miniapp.host.wechat.testing

import io.github.bobcgn.miniapp.host.wechat.adapter.WechatNetworkStatusHost
import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxGetNetworkTypeSuccessResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxNetworkStatusChangeListener

/**
 * The FakeAdapter boundary for network status: a stand-in for the WeChat
 * `getNetworkType` callbacks and the `on`/`offNetworkStatusChange` pair.
 *
 * It implements the raw callback port, so a test can drive the network status adapter
 * without a WeChat runtime and can reproduce conditions a real host produces rarely,
 * such as a connection kind the SDK does not recognize or a state payload of the wrong
 * type.
 *
 * The query and the listener are switchable independently, because a host may answer
 * the question without offering change events.
 *
 * @param querySupported whether this host exposes the network type query
 * @param listenerSupported whether this host exposes both listener methods
 * @param networkType the connection kind the query reports, typed loosely so a test can
 *   supply a malformed answer
 */
internal class FakeWechatNetworkStatusHost(
    var querySupported: Boolean = true,
    var listenerSupported: Boolean = true,
    var networkType: Any? = "wifi",
) : WechatNetworkStatusHost {
    /** Message the query fails with, or `null` to succeed. */
    var queryFailure: String? = null

    /** When true, the query reports its outcome twice, as a defective host might. */
    var completeQueryTwice: Boolean = false

    var queryCalls: Int = 0
        private set

    var addCalls: Int = 0
        private set

    var removeCalls: Int = 0
        private set

    private val listeners = mutableListOf<WxNetworkStatusChangeListener>()

    /** How many listeners this host currently holds, so a test can prove none leaked. */
    val activeListeners: Int
        get() = listeners.size

    override fun isQuerySupported(): Boolean = querySupported

    override fun isListenerSupported(): Boolean = listenerSupported

    override fun query(
        success: (WxGetNetworkTypeSuccessResult) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        queryCalls += 1

        val complete = {
            val message = queryFailure
            if (message == null) {
                success(fakeGetNetworkTypeSuccess(networkType))
            } else {
                failure(fakeWxFailure(message))
            }
        }

        complete()
        if (completeQueryTwice) complete()
    }

    override fun addListener(listener: WxNetworkStatusChangeListener) {
        addCalls += 1
        listeners += listener
    }

    override fun removeListener(listener: WxNetworkStatusChangeListener) {
        removeCalls += 1
        listeners.remove(listener)
    }

    /**
     * Reports a state change to every listener this host holds.
     *
     * The payload fields are typed loosely so a test can supply a malformed event.
     */
    fun emit(isConnected: Any?, networkType: Any?) {
        val event = fakeNetworkStatusEvent(isConnected, networkType)
        listeners.toList().forEach { it(event) }
    }
}
