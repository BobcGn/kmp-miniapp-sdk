package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.host.wechat.interop.WxLoginFailureResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxLoginSuccessResult
import io.github.bobcgn.miniapp.host.wechat.interop.wx
import io.github.bobcgn.miniapp.host.wechat.interop.wxLoginOptions

/** Callback port that keeps [WechatAuth] independently testable. */
internal interface WechatAuthHost {
    fun login(
        success: (WxLoginSuccessResult) -> Unit,
        failure: (WxLoginFailureResult) -> Unit,
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
}
