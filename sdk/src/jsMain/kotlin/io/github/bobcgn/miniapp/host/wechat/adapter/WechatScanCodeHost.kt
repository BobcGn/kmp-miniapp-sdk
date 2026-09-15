package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxScanCodeSuccessResult
import io.github.bobcgn.miniapp.host.wechat.interop.hasWxScanCode
import io.github.bobcgn.miniapp.host.wechat.interop.wx
import io.github.bobcgn.miniapp.host.wechat.interop.wxScanCodeOptions

/** Callback port that keeps [WechatScanCode] independently testable. */
internal interface WechatScanCodeHost {
    /** Whether this host can scan at all. */
    fun isSupported(): Boolean

    /**
     * Asks the host to scan through its own interface.
     *
     * @param onlyFromCamera whether the host may scan only through its camera
     * @param scanCategories category values to ask for; empty means every one the
     *   host supports
     */
    fun scan(
        onlyFromCamera: Boolean,
        scanCategories: List<String>,
        success: (WxScanCodeSuccessResult) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ): Unit
}

/** Production scan port backed directly by the global WeChat API. */
internal object WxScanCodeHost : WechatScanCodeHost {
    override fun isSupported(): Boolean = hasWxScanCode()

    override fun scan(
        onlyFromCamera: Boolean,
        scanCategories: List<String>,
        success: (WxScanCodeSuccessResult) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        val options = wxScanCodeOptions(
            onlyFromCamera = onlyFromCamera,
            scanCategories = scanCategories,
        )
        options.success = success
        options.fail = failure
        wx.scanCode(options)
    }
}
