package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxGetSettingSuccessResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxOpenSettingSuccessResult
import io.github.bobcgn.miniapp.host.wechat.interop.wx
import io.github.bobcgn.miniapp.host.wechat.interop.wxAuthorizeOptions
import io.github.bobcgn.miniapp.host.wechat.interop.wxGetSettingOptions
import io.github.bobcgn.miniapp.host.wechat.interop.wxOpenSettingOptions

/** Callback port that keeps [WechatPermissions] independently testable. */
internal interface WechatPermissionHost {
    /** Reads the host's authorization map through callbacks. */
    fun getSetting(
        success: (WxGetSettingSuccessResult) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ): Unit

    /** Requests one host scope through callbacks. */
    fun authorize(
        scope: String,
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ): Unit

    /** Opens the host permission settings page through callbacks. */
    fun openSetting(
        success: (WxOpenSettingSuccessResult) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ): Unit
}

/** Production permission port backed directly by the global WeChat API. */
internal object WxPermissionHost : WechatPermissionHost {
    override fun getSetting(
        success: (WxGetSettingSuccessResult) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        val options = wxGetSettingOptions()
        options.success = success
        options.fail = failure
        wx.getSetting(options)
    }

    override fun authorize(
        scope: String,
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        val options = wxAuthorizeOptions(scope)
        options.success = { success() }
        options.fail = failure
        wx.authorize(options)
    }

    override fun openSetting(
        success: (WxOpenSettingSuccessResult) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        val options = wxOpenSettingOptions()
        options.success = success
        options.fail = failure
        wx.openSetting(options)
    }
}
