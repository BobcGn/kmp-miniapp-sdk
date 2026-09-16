package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxGetNetworkTypeSuccessResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxNetworkStatusChangeListener
import io.github.bobcgn.miniapp.host.wechat.interop.hasWxGetNetworkType
import io.github.bobcgn.miniapp.host.wechat.interop.hasWxOffNetworkStatusChange
import io.github.bobcgn.miniapp.host.wechat.interop.hasWxOnNetworkStatusChange
import io.github.bobcgn.miniapp.host.wechat.interop.wx
import io.github.bobcgn.miniapp.host.wechat.interop.wxGetNetworkTypeOptions

/**
 * Callback port that keeps the network status adapter independently testable.
 *
 * The query and the listener are reported separately, because a host may answer the
 * question without offering change events.
 */
internal interface WechatNetworkStatusHost {
    /** Whether this host can answer a network type query. */
    fun isQuerySupported(): Boolean

    /**
     * Whether this host can register and remove a change listener.
     *
     * Both methods are required: a host that could register but not remove one would
     * leak the listener for the lifetime of the mini program, so this reports false
     * rather than offering a registration the SDK cannot pair with a removal.
     */
    fun isListenerSupported(): Boolean

    /** Asks the host for its current network state. */
    fun query(
        success: (WxGetNetworkTypeSuccessResult) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ): Unit

    /** Registers [listener] to receive network state changes. */
    fun addListener(listener: WxNetworkStatusChangeListener): Unit

    /** Removes [listener] so it receives no further changes. */
    fun removeListener(listener: WxNetworkStatusChangeListener): Unit
}

/** Production network status port backed directly by the global WeChat API. */
internal object WxNetworkStatusHost : WechatNetworkStatusHost {
    override fun isQuerySupported(): Boolean = hasWxGetNetworkType()

    override fun isListenerSupported(): Boolean =
        hasWxOnNetworkStatusChange() && hasWxOffNetworkStatusChange()

    override fun query(
        success: (WxGetNetworkTypeSuccessResult) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        val options = wxGetNetworkTypeOptions()
        options.success = success
        options.fail = failure
        wx.getNetworkType(options)
    }

    override fun addListener(listener: WxNetworkStatusChangeListener): Unit =
        wx.onNetworkStatusChange(listener)

    override fun removeListener(listener: WxNetworkStatusChangeListener): Unit =
        wx.offNetworkStatusChange(listener)
}
