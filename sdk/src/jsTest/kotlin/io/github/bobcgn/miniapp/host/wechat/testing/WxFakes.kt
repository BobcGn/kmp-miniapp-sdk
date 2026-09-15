package io.github.bobcgn.miniapp.host.wechat.testing

import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxLoginFailureResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxLoginSuccessResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxRequestFailureResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxRequestSuccessResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxRequestTask

/**
 * Builders for the raw JavaScript result objects WeChat hands to callbacks.
 *
 * The external interfaces have no Kotlin constructors, so these factories exist
 * only to produce the shapes a host would. They assert nothing and contain no
 * adapter behavior.
 */

internal fun fakeWxFailure(message: String): WxGeneralCallbackResult {
    val result: WxGeneralCallbackResult = js("({})")
    js("result.errMsg = message")
    return result
}

internal fun fakeWxLoginSuccess(code: String): WxLoginSuccessResult {
    val result: WxLoginSuccessResult = js("({})")
    js("result.code = code")
    js("result.errMsg = 'login:ok'")
    return result
}

internal fun fakeWxLoginFailure(message: String, errno: Int?): WxLoginFailureResult {
    val result: WxLoginFailureResult = js("({})")
    js("result.errMsg = message")
    if (errno != null) {
        js("result.errno = errno")
    }
    return result
}

/**
 * Builds a `wx.request` success result.
 *
 * @param headers raw host header object, so a test can supply shapes such as the
 *   arrays WeChat reports for `Set-Cookie`
 */
internal fun fakeWxRequestSuccess(
    statusCode: Int = 200,
    body: Any? = "{\"ok\":true}",
    headers: Any? = null,
): WxRequestSuccessResult {
    val result: WxRequestSuccessResult = js("({})")
    js("result.statusCode = statusCode")
    js("result.data = body")
    js("result.errMsg = 'request:ok'")
    js("result.header = headers")
    return result
}

internal fun fakeWxRequestFailure(message: String, errno: Int?): WxRequestFailureResult {
    val result: WxRequestFailureResult = js("({})")
    js("result.errMsg = message")
    if (errno != null) {
        js("result.errno = errno")
    }
    return result
}

/** Builds the task handle `wx.request` returns, counting invocations of `abort`. */
internal fun fakeAbortableTask(onAbort: () -> Unit): WxRequestTask {
    val task: WxRequestTask = js("({})")
    js("task.abort = onAbort")
    return task
}

/** A host header object containing one single-valued header. */
internal fun fakeWxResponseHeaders(): Any = js("({ 'Content-Type': 'application/json' })")
