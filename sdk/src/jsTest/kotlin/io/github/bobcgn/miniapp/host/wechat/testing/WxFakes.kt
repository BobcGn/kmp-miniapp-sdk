package io.github.bobcgn.miniapp.host.wechat.testing

import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxGetSettingSuccessResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxLoginFailureResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxLoginSuccessResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxGetClipboardDataSuccessResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxGetLocationSuccessResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxGetPrivacySettingSuccessResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxOpenSettingSuccessResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxReadFileSuccessResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxRequestFailureResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxRequestSuccessResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxRequestTask
import io.github.bobcgn.miniapp.host.wechat.interop.WxScanCodeSuccessResult

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

/**
 * Builds a raw host authorization map.
 *
 * Entries are supplied explicitly so a test can produce the shapes the contract
 * must reject, such as a value that is neither `true` nor `false`. A scope that is
 * not listed is absent from the map, which is how the host reports "never asked".
 */
internal fun fakeWxAuthSetting(vararg entries: Pair<String, Any?>): Any {
    val authSetting: Any = js("({})")
    for ((scope, value) in entries) {
        js("authSetting[scope] = value")
    }
    return authSetting
}

/**
 * Builds a `wx.getSetting` success result.
 *
 * @param authSetting raw authorization map, or `null` to model a host that
 *   answered without one
 */
internal fun fakeGetSettingSuccess(authSetting: Any?): WxGetSettingSuccessResult {
    val result: WxGetSettingSuccessResult = js("({})")
    js("result.errMsg = 'getSetting:ok'")
    js("result.authSetting = authSetting")
    return result
}

/**
 * Builds a `wx.getClipboardData` success result.
 *
 * `data` is supplied as `Any?` so a test can produce the malformed shapes the
 * contract must reject, such as a missing value or a non-string one.
 */
internal fun fakeGetClipboardDataSuccess(data: Any?): WxGetClipboardDataSuccessResult {
    val result: WxGetClipboardDataSuccessResult = js("({})")
    js("result.errMsg = 'getClipboardData:ok'")
    js("result.data = data")
    return result
}

/**
 * Builds a `FileSystemManager.readFile` success result.
 *
 * `data` is supplied as `Any?` so a test can produce the shapes the contract must
 * reject, such as a missing value or binary content.
 */
internal fun fakeReadFileSuccess(data: Any?): WxReadFileSuccessResult {
    val result: WxReadFileSuccessResult = js("({})")
    js("result.errMsg = 'readFile:ok'")
    js("result.data = data")
    return result
}

/**
 * Builds a `wx.getLocation` success result.
 *
 * Every field is supplied as `Any?` so a test can produce the shapes the contract
 * must reject, such as a missing coordinate or a non-finite one.
 */
internal fun fakeGetLocationSuccess(
    latitude: Any?,
    longitude: Any?,
    accuracy: Any?,
): WxGetLocationSuccessResult {
    val result: WxGetLocationSuccessResult = js("({})")
    js("result.errMsg = 'getLocation:ok'")
    js("result.latitude = latitude")
    js("result.longitude = longitude")
    js("result.accuracy = accuracy")
    return result
}

/**
 * Builds a `wx.scanCode` success result.
 *
 * Every field is supplied as `Any?` so a test can produce the shapes the contract
 * must reject, such as missing content or a descriptive field of the wrong type.
 * The default [path] is absent, which is how WeChat reports a scan it did not take
 * from a file.
 */
internal fun fakeScanCodeSuccess(
    result: Any?,
    scanType: Any? = "QR_CODE",
    charSet: Any? = "utf-8",
    rawData: Any? = "raw-bytes",
    path: Any? = null,
): WxScanCodeSuccessResult {
    val value: WxScanCodeSuccessResult = js("({})")
    js("value.errMsg = 'scanCode:ok'")
    js("value.result = result")
    js("value.scanType = scanType")
    js("value.charSet = charSet")
    js("value.rawData = rawData")
    js("value.path = path")
    return value
}

/**
 * Builds a `wx.getPrivacySetting` success result.
 *
 * Both fields are supplied as `Any?` so a test can produce the malformed shapes
 * the contract must reject, such as a missing flag or a non-boolean one.
 */
internal fun fakeGetPrivacySettingSuccess(
    needAuthorization: Any?,
    privacyContractName: Any?,
): WxGetPrivacySettingSuccessResult {
    val result: WxGetPrivacySettingSuccessResult = js("({})")
    js("result.errMsg = 'getPrivacySetting:ok'")
    js("result.needAuthorization = needAuthorization")
    js("result.privacyContractName = privacyContractName")
    return result
}

/**
 * Builds a `wx.openSetting` success result, which reports the map after the
 * settings page closed.
 */
internal fun fakeOpenSettingSuccess(authSetting: Any?): WxOpenSettingSuccessResult {
    val result: WxOpenSettingSuccessResult = js("({})")
    js("result.errMsg = 'openSetting:ok'")
    js("result.authSetting = authSetting")
    return result
}
