package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxGetPrivacySettingSuccessResult
import io.github.bobcgn.miniapp.host.wechat.interop.hasWxGetPrivacySetting
import io.github.bobcgn.miniapp.host.wechat.interop.hasWxRequirePrivacyAuthorize
import io.github.bobcgn.miniapp.host.wechat.interop.wx
import io.github.bobcgn.miniapp.host.wechat.interop.wxGetPrivacySettingOptions
import io.github.bobcgn.miniapp.host.wechat.interop.wxRequirePrivacyAuthorizeOptions

/** Callback port that keeps [WechatPrivacy] independently testable. */
internal interface WechatPrivacyHost {
    /**
     * Whether this host exposes the privacy APIs at all.
     *
     * A base library without them does not intercept privacy-gated calls, so the
     * adapter must answer "unsupported" instead of reaching a missing function.
     */
    fun isSupported(): Boolean

    /** Reads the host's privacy requirement through callbacks. */
    fun getPrivacySetting(
        success: (WxGetPrivacySettingSuccessResult) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ): Unit

    /** Asks the host for the user's acceptance through callbacks. */
    fun requirePrivacyAuthorize(
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ): Unit
}

/** Production privacy port backed directly by the global WeChat API. */
internal object WxPrivacyHost : WechatPrivacyHost {
    override fun isSupported(): Boolean =
        hasWxGetPrivacySetting() && hasWxRequirePrivacyAuthorize()

    override fun getPrivacySetting(
        success: (WxGetPrivacySettingSuccessResult) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        val options = wxGetPrivacySettingOptions()
        options.success = success
        options.fail = failure
        wx.getPrivacySetting(options)
    }

    override fun requirePrivacyAuthorize(
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        val options = wxRequirePrivacyAuthorizeOptions()
        options.success = { success() }
        options.fail = failure
        wx.requirePrivacyAuthorize(options)
    }
}
