package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxGetClipboardDataSuccessResult
import io.github.bobcgn.miniapp.host.wechat.interop.hasWxGetClipboardData
import io.github.bobcgn.miniapp.host.wechat.interop.hasWxSetClipboardData
import io.github.bobcgn.miniapp.host.wechat.interop.wx
import io.github.bobcgn.miniapp.host.wechat.interop.wxGetClipboardDataOptions
import io.github.bobcgn.miniapp.host.wechat.interop.wxSetClipboardDataOptions

/** Callback port that keeps [WechatClipboard] independently testable. */
internal interface WechatClipboardHost {
    /** Whether this host can read the clipboard at all. */
    fun isReadSupported(): Boolean

    /** Whether this host can write the clipboard at all. */
    fun isWriteSupported(): Boolean

    /** Reads the clipboard through callbacks. */
    fun read(
        success: (WxGetClipboardDataSuccessResult) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ): Unit

    /** Writes [data] to the clipboard through callbacks. */
    fun write(
        data: String,
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ): Unit
}

/** Production clipboard port backed directly by the global WeChat API. */
internal object WxClipboardHost : WechatClipboardHost {
    override fun isReadSupported(): Boolean = hasWxGetClipboardData()

    override fun isWriteSupported(): Boolean = hasWxSetClipboardData()

    override fun read(
        success: (WxGetClipboardDataSuccessResult) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        val options = wxGetClipboardDataOptions()
        options.success = success
        options.fail = failure
        wx.getClipboardData(options)
    }

    override fun write(
        data: String,
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        val options = wxSetClipboardDataOptions(data)
        options.success = { success() }
        options.fail = failure
        wx.setClipboardData(options)
    }
}
