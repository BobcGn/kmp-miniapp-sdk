package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.host.wechat.WeChatUploadRequest
import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxUploadFileSuccessResult
import io.github.bobcgn.miniapp.host.wechat.interop.hasWxUploadFile
import io.github.bobcgn.miniapp.host.wechat.interop.wx
import io.github.bobcgn.miniapp.host.wechat.interop.wxStringMapObject
import io.github.bobcgn.miniapp.host.wechat.interop.wxUploadFileOptions

/** Callback port that keeps [WechatUploadFile] independently testable. */
internal interface WechatUploadFileHost {
    /** Whether this host can upload a file at all. */
    fun isSupported(): Boolean

    /**
     * Posts [request]'s file to the host.
     *
     * @return the host's task, or `null` when the host returned none. A null task
     *   means the transfer cannot be aborted, which the adapter reports rather than
     *   pretending otherwise.
     */
    fun upload(
        request: WeChatUploadRequest,
        success: (WxUploadFileSuccessResult) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ): WechatTransferTask?
}

/** Production upload port backed directly by the global WeChat API. */
internal object WxUploadFileHost : WechatUploadFileHost {
    override fun isSupported(): Boolean = hasWxUploadFile()

    override fun upload(
        request: WeChatUploadRequest,
        success: (WxUploadFileSuccessResult) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ): WechatTransferTask? {
        val options = wxUploadFileOptions(
            url = request.url,
            filePath = request.filePath,
            name = request.name,
            header = if (request.headers.isEmpty()) null else wxStringMapObject(request.headers),
            formData = if (request.formData.isEmpty()) null else wxStringMapObject(request.formData),
            timeoutMillis = request.timeoutMillis,
        )
        options.success = success
        options.fail = failure
        return wx.uploadFile(options)?.let { WxTransferTaskAdapter(it) }
    }
}
