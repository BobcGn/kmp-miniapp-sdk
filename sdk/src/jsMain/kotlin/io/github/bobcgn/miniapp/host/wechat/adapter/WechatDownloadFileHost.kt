package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.host.wechat.WeChatDownloadRequest
import io.github.bobcgn.miniapp.host.wechat.interop.WxDownloadFileSuccessResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.interop.hasWxDownloadFile
import io.github.bobcgn.miniapp.host.wechat.interop.wx
import io.github.bobcgn.miniapp.host.wechat.interop.wxDownloadFileOptions
import io.github.bobcgn.miniapp.host.wechat.interop.wxStringMapObject

/** Callback port that keeps [WechatDownloadFile] independently testable. */
internal interface WechatDownloadFileHost {
    /** Whether this host can download a file at all. */
    fun isSupported(): Boolean

    /**
     * Fetches [request]'s URL to the host's file system.
     *
     * @return the host's task, or `null` when the host returned none.
     */
    fun download(
        request: WeChatDownloadRequest,
        success: (WxDownloadFileSuccessResult) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ): WechatTransferTask?
}

/** Production download port backed directly by the global WeChat API. */
internal object WxDownloadFileHost : WechatDownloadFileHost {
    override fun isSupported(): Boolean = hasWxDownloadFile()

    override fun download(
        request: WeChatDownloadRequest,
        success: (WxDownloadFileSuccessResult) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ): WechatTransferTask? {
        val options = wxDownloadFileOptions(
            url = request.url,
            header = if (request.headers.isEmpty()) null else wxStringMapObject(request.headers),
            timeoutMillis = request.timeoutMillis,
            targetPath = request.filePath,
        )
        options.success = success
        options.fail = failure
        return wx.downloadFile(options)?.let { WxTransferTaskAdapter(it) }
    }
}
