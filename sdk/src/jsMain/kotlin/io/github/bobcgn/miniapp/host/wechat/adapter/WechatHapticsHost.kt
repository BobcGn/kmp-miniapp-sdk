package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.interop.hasWxVibrateLong
import io.github.bobcgn.miniapp.host.wechat.interop.hasWxVibrateShort
import io.github.bobcgn.miniapp.host.wechat.interop.wx
import io.github.bobcgn.miniapp.host.wechat.interop.wxVibrateOptions

/** Callback port that keeps [WechatHaptics] independently testable. */
internal interface WechatHapticsHost {
    /** Whether this host can perform the short vibration at all. */
    fun isShortSupported(): Boolean

    /** Whether this host can perform the long vibration at all. */
    fun isLongSupported(): Boolean

    /** Performs the short vibration through callbacks. */
    fun vibrateShort(
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ): Unit

    /** Performs the long vibration through callbacks. */
    fun vibrateLong(
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ): Unit
}

/** Production haptics port backed directly by the global WeChat API. */
internal object WxHapticsHost : WechatHapticsHost {
    override fun isShortSupported(): Boolean = hasWxVibrateShort()

    override fun isLongSupported(): Boolean = hasWxVibrateLong()

    override fun vibrateShort(
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        val options = wxVibrateOptions()
        options.success = { success() }
        options.fail = failure
        wx.vibrateShort(options)
    }

    override fun vibrateLong(
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        val options = wxVibrateOptions()
        options.success = { success() }
        options.fail = failure
        wx.vibrateLong(options)
    }
}
