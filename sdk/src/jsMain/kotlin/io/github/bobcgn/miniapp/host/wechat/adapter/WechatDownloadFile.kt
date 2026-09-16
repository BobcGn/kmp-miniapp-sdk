package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.WeChatDeviceCapabilities
import io.github.bobcgn.miniapp.host.wechat.WeChatDownloadRequest
import io.github.bobcgn.miniapp.host.wechat.WeChatDownloadResult
import io.github.bobcgn.miniapp.host.wechat.interop.wxTransferStatusCode

/**
 * WeChat file download, deliberately WeChat-specific.
 *
 * Like the upload direction, what is portable about this is the HTTP exchange, which
 * the common transport capability already covers; what is not portable is the result
 * — a path in the host's own file system. The SDK therefore hands over the host's
 * reference and stops there: it reads no content, moves nothing, and makes no claim
 * that the path survives the session. Further file work is the file-system
 * capability's job, driven explicitly by the caller.
 *
 * WeChat returns a task, so a consumer can stop the transfer and cancelling the wait
 * stops it too.
 */
internal class WechatDownloadFile(
    private val host: WechatDownloadFileHost = WxDownloadFileHost,
) {
    /**
     * Begins a download and returns the transfer that describes it.
     *
     * @throws MiniAppException.UnsupportedCapability when the host has no download API
     */
    fun start(request: WeChatDownloadRequest): WechatTransfer<WeChatDownloadResult> {
        if (!host.isSupported()) {
            throw MiniAppException.UnsupportedCapability(WeChatDeviceCapabilities.DownloadFile)
        }

        val transfer = WechatTransfer<WeChatDownloadResult>()
        val task = host.download(
            request = request,
            success = { result ->
                val status = wxTransferStatusCode(result.statusCode)
                val tempPath = result.tempFilePath
                when {
                    status == null -> transfer.fail(
                        MiniAppException.InvalidResponse(
                            "WeChat downloadFile reported an HTTP status the SDK cannot read",
                        ),
                    )

                    tempPath !is String || tempPath.isBlank() -> transfer.fail(
                        MiniAppException.InvalidResponse(
                            "WeChat downloadFile reported no usable temporary file path",
                        ),
                    )

                    else -> transfer.complete(
                        WeChatDownloadResult(
                            statusCode = status,
                            tempFilePath = tempPath,
                            filePath = result.filePath as? String,
                        ),
                    )
                }
            },
            failure = { result -> transfer.fail(mapWechatDownloadFailure(result)) },
        )
        transfer.attach(task)
        return transfer
    }
}
