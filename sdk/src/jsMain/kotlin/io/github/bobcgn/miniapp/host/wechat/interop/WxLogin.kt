package io.github.bobcgn.miniapp.host.wechat.interop

/** Raw callback options accepted by [wx.login]. */
internal external interface WxLoginOptions {
    /** Optional host timeout in milliseconds. */
    var timeout: Int?

    /** Called with a short-lived login code when the host operation succeeds. */
    var success: WxLoginSuccessCallback?

    /** Called when the host cannot obtain a login code. */
    var fail: WxLoginFailureCallback?
}

/** Raw success result returned by [wx.login]. */
internal external interface WxLoginSuccessResult : WxGeneralCallbackResult {
    /** Short-lived credential intended for a server-side WeChat exchange. */
    val code: String
}

/** Raw failure result returned by [wx.login]. */
internal external interface WxLoginFailureResult : WxGeneralCallbackResult {
    /** WeChat errno when supplied by the active base-library version. */
    val errno: Int?
}

/** Success callback accepted by [WxLoginOptions]. */
internal typealias WxLoginSuccessCallback = (WxLoginSuccessResult) -> Unit

/** Failure callback accepted by [WxLoginOptions]. */
internal typealias WxLoginFailureCallback = (WxLoginFailureResult) -> Unit

/**
 * Creates a plain JavaScript option bag without assigning absent optional fields.
 *
 * Leaving optional fields undefined preserves the native `wx.login` defaults.
 */
internal fun wxLoginOptions(timeoutMillis: Int? = null): WxLoginOptions {
    val options: WxLoginOptions = js("({})")
    if (timeoutMillis != null) {
        options.timeout = timeoutMillis
    }
    return options
}
