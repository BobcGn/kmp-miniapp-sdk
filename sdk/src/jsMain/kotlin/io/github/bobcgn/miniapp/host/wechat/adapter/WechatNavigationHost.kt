package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.interop.wx
import io.github.bobcgn.miniapp.host.wechat.interop.wxNavigateBackOptions
import io.github.bobcgn.miniapp.host.wechat.interop.wxNavigateToOptions
import io.github.bobcgn.miniapp.host.wechat.interop.wxRedirectToOptions

/** Callback port that keeps [WechatNavigation] independently testable. */
internal interface WechatNavigationHost {
    fun navigateTo(
        url: String,
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ): Unit

    fun redirectTo(
        url: String,
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ): Unit

    fun navigateBack(
        delta: Int?,
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ): Unit
}

/** Production navigation port backed directly by the global WeChat API. */
internal object WxNavigationHost : WechatNavigationHost {
    override fun navigateTo(
        url: String,
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        val options = wxNavigateToOptions(url)
        options.success = { success() }
        options.fail = failure
        wx.navigateTo(options)
    }

    override fun redirectTo(
        url: String,
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        val options = wxRedirectToOptions(url)
        options.success = { success() }
        options.fail = failure
        wx.redirectTo(options)
    }

    override fun navigateBack(
        delta: Int?,
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        val options = wxNavigateBackOptions(delta)
        options.success = { success() }
        options.fail = failure
        wx.navigateBack(options)
    }
}
