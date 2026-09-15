package io.github.bobcgn.miniapp.host.wechat.testing

import io.github.bobcgn.miniapp.host.wechat.adapter.WechatNetworkHost
import io.github.bobcgn.miniapp.host.wechat.interop.WxRequestFailureResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxRequestSuccessResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxRequestTask

/** One request the fake host was asked to perform, with every field the adapter passes. */
internal class RecordedWechatRequest(
    val url: String,
    val method: String,
    val headers: Map<String, String>,
    val body: String?,
    val timeoutMillis: Int?,
)

/**
 * The FakeAdapter boundary for the HTTP transport: a stand-in for the WeChat
 * `wx.request` callbacks that replies immediately with one scripted outcome.
 *
 * It implements the raw callback port, so a test can drive `WechatNetwork`
 * without a WeChat runtime and can reproduce host behavior the contract does not
 * allow, such as an exchange that completes with a parsed object instead of text.
 *
 * A scenario that needs the host to stay silent, for example cancellation, needs
 * a deferred port rather than this one.
 *
 * @param statusCode HTTP status a completed exchange reports
 * @param body response body; may be a non-string to exercise contract rejection
 * @param responseHeaders raw host header object, or `null` for a single string-valued header
 * @param failWith a `fail` message, which makes every call fail instead of completing
 * @param errno WeChat errno to report alongside [failWith]
 * @param abortable whether the returned task can abort, as the real `wx.request` can
 */
internal class FakeWechatNetworkHost(
    private val statusCode: Int = 200,
    private val body: Any? = "{\"ok\":true}",
    private val responseHeaders: Any? = null,
    private val failWith: String? = null,
    private val errno: Int? = null,
    private val abortable: Boolean = true,
) : WechatNetworkHost {
    /** Every request this host was asked to perform, in order. */
    val requests: MutableList<RecordedWechatRequest> = mutableListOf()

    /** How many times an in-flight request was aborted. */
    var abortCount: Int = 0
        private set

    override fun request(
        url: String,
        method: String,
        headers: Map<String, String>,
        body: String?,
        timeoutMillis: Int?,
        success: (WxRequestSuccessResult) -> Unit,
        failure: (WxRequestFailureResult) -> Unit,
    ): WxRequestTask? {
        requests += RecordedWechatRequest(url, method, headers, body, timeoutMillis)

        val message = failWith
        if (message == null) {
            success(
                fakeWxRequestSuccess(
                    statusCode = statusCode,
                    body = this.body,
                    headers = responseHeaders ?: fakeWxResponseHeaders(),
                ),
            )
        } else {
            failure(fakeWxRequestFailure(message = message, errno = errno))
        }

        return if (abortable) fakeAbortableTask { abortCount += 1 } else null
    }
}
