package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.WeChatDeviceCapabilities
import io.github.bobcgn.miniapp.host.wechat.WeChatUploadRequest
import io.github.bobcgn.miniapp.host.wechat.WeChatUploadResult
import io.github.bobcgn.miniapp.host.wechat.interop.wxTransferStatusCode

/**
 * WeChat file upload, deliberately WeChat-specific.
 *
 * The portable part of an HTTP exchange — method, URL, headers, status, text body —
 * is already the common HTTP transport capability. What an upload adds is a *file*: a
 * host path in a host file sandbox, which this SDK deliberately has no portable form
 * of. Modelling it as a common capability would mean inventing a portable file
 * reference no other host has been shown to need, so it stays behind the WeChat
 * escape hatch.
 *
 * The SDK reads no file, logs no path, and defines no upload protocol: it carries what
 * the caller described to the host and reports what came back.
 *
 * WeChat returns a task, so this is one of the capabilities where a consumer can stop
 * the host operation, and where cancelling the wait stops it too.
 */
internal class WechatUploadFile(
    private val host: WechatUploadFileHost = WxUploadFileHost,
) {
    /**
     * Begins an upload and returns the transfer that describes it.
     *
     * @throws MiniAppException.UnsupportedCapability when the host has no upload API
     */
    fun start(request: WeChatUploadRequest): WechatTransfer<WeChatUploadResult> {
        if (!host.isSupported()) {
            throw MiniAppException.UnsupportedCapability(WeChatDeviceCapabilities.UploadFile)
        }

        val transfer = WechatTransfer<WeChatUploadResult>()
        val task = host.upload(
            request = request,
            success = { result ->
                val status = wxTransferStatusCode(result.statusCode)
                val body = result.data
                when {
                    status == null -> transfer.fail(
                        MiniAppException.InvalidResponse(
                            "WeChat uploadFile reported an HTTP status the SDK cannot read",
                        ),
                    )

                    body !is String -> transfer.fail(
                        MiniAppException.InvalidResponse(
                            "WeChat uploadFile returned a non-text response body",
                        ),
                    )

                    else -> transfer.complete(
                        WeChatUploadResult(statusCode = status, responseText = body),
                    )
                }
            },
            failure = { result -> transfer.fail(mapWechatUploadFailure(result)) },
        )
        transfer.attach(task)
        return transfer
    }
}
