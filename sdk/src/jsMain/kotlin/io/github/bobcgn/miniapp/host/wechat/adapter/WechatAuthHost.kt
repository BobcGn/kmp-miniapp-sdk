package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxLoginFailureResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxLoginSuccessResult
import io.github.bobcgn.miniapp.host.wechat.interop.hasWxCheckSession
import io.github.bobcgn.miniapp.host.wechat.interop.wx
import io.github.bobcgn.miniapp.host.wechat.interop.wxCheckSessionOptions
import io.github.bobcgn.miniapp.host.wechat.interop.wxLoginOptions

/** Callback port that keeps [WechatAuth] independently testable. */
internal interface WechatAuthHost {
    fun login(
        success: (WxLoginSuccessResult) -> Unit,
        failure: (WxLoginFailureResult) -> Unit,
    ): Unit

    /**
     * Whether this host exposes the session check API at all.
     *
     * A base library without it must answer "unsupported" rather than reach a
     * missing function.
     */
    fun isSessionCheckSupported(): Boolean

    /** Reports the host's session validity through callbacks. */
    fun checkSession(
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ): Unit
}

/** Production authentication port backed directly by the global WeChat API. */
internal object WxAuthHost : WechatAuthHost {
    override fun login(
        success: (WxLoginSuccessResult) -> Unit,
        failure: (WxLoginFailureResult) -> Unit,
    ) {
        val options = wxLoginOptions()
        options.success = success
        options.fail = failure
        wx.login(options)
    }

    override fun isSessionCheckSupported(): Boolean = hasWxCheckSession()

    override fun checkSession(
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        val options = wxCheckSessionOptions()
        options.success = { success() }
        options.fail = failure
        wx.checkSession(options)
    }
}
