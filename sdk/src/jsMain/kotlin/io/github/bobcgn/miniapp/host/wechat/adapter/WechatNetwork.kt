package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.async.HostOperationAborter
import io.github.bobcgn.miniapp.async.awaitHostCallback
import io.github.bobcgn.miniapp.capability.network.MiniAppHttpRequest
import io.github.bobcgn.miniapp.capability.network.MiniAppHttpResponse
import io.github.bobcgn.miniapp.capability.network.MiniAppHttpTransport
import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.interop.wxResponseHeaders

/** WeChat implementation of the platform-neutral HTTP transport capability. */
internal class WechatNetwork(
    private val host: WechatNetworkHost = WxNetworkHost,
) : MiniAppHttpTransport {
    /**
     * Performs the exchange described by [request].
     *
     * `wx.request` returns an abort handle, so this is the first capability whose
     * coroutine cancellation also stops the host operation. The handle is invoked
     * at most once, and any failure callback WeChat delivers afterwards is
     * discarded because the continuation already completed.
     */
    override suspend fun request(request: MiniAppHttpRequest): MiniAppHttpResponse =
        awaitHostCallback { success, failure ->
            val task = host.request(
                url = request.url,
                method = request.method.name,
                headers = request.headers,
                body = request.body,
                timeoutMillis = request.timeoutMillis,
                success = { result ->
                    val body = result.data
                    if (body is String) {
                        success(
                            MiniAppHttpResponse(
                                statusCode = result.statusCode,
                                headers = wxResponseHeaders(result.header),
                                body = body,
                            ),
                        )
                    } else {
                        failure(
                            MiniAppException.InvalidResponse(
                                "WeChat request returned a non-text body for '${request.url}'",
                            ),
                        )
                    }
                },
                failure = { result -> failure(mapWechatRequestFailure(result)) },
            )
            if (task == null) null else HostOperationAborter { task.abort() }
        }
}
