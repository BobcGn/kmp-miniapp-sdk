package io.github.bobcgn.miniapp.host.wechat.interop

/**
 * Raw callback-style options accepted by [wx.request].
 *
 * [dataType] is fixed to text by the SDK factory because the platform-neutral
 * transport contract carries an already-encoded string body. Letting WeChat
 * parse JSON would hand the adapter an object the contract cannot represent.
 */
internal external interface WxRequestOptions {
    /** Absolute request target. */
    var url: String

    /** HTTP method; WeChat defaults to `GET` when absent. */
    var method: String?

    /** Request headers as a plain JavaScript object of string values. */
    var header: Any?

    /** Already-encoded request body. */
    var data: Any?

    /** Response decoding mode; the SDK always requests raw text. */
    var dataType: String?

    /** Host timeout in milliseconds. */
    var timeout: Int?

    /** Called only when the host completes an HTTP exchange. */
    var success: WxRequestSuccessCallback?

    /** Called only when the exchange fails below HTTP, for example timeout. */
    var fail: WxRequestFailureCallback?

    /** Called after either [success] or [fail]. */
    var complete: WxGeneralCallback?
}

/**
 * Raw result returned by [wx.request] when an HTTP exchange completes.
 *
 * Any HTTP status reaches this callback, including `4xx` and `5xx`. A response
 * status is an outcome of the exchange, not a JavaScript-level failure.
 */
internal external interface WxRequestSuccessResult : WxGeneralCallbackResult {
    /** HTTP status code reported by the host. */
    val statusCode: Int

    /** Response headers as a plain JavaScript object. */
    val header: Any?

    /** Response body, kept as raw text by the `dataType` requested by the SDK. */
    val data: Any?
}

/** Raw failure result returned by [wx.request] when the exchange does not complete. */
internal external interface WxRequestFailureResult : WxGeneralCallbackResult {
    /** WeChat errno when supplied by the active base-library version. */
    val errno: Int?
}

/** Handle returned by [wx.request] that can abort an in-flight exchange. */
internal external interface WxRequestTask {
    /**
     * Aborts the exchange.
     *
     * The aborted exchange reports no usable result; WeChat may still deliver a
     * failure callback, which the adapter discards after cancellation.
     */
    fun abort()
}

/** Success callback accepted by [WxRequestOptions]. */
internal typealias WxRequestSuccessCallback = (WxRequestSuccessResult) -> Unit

/** Failure callback accepted by [WxRequestOptions]. */
internal typealias WxRequestFailureCallback = (WxRequestFailureResult) -> Unit

/**
 * Creates a plain JavaScript option bag for [wx.request].
 *
 * Optional fields are assigned only when the caller supplies a value, so absent
 * fields keep their native WeChat defaults. External interfaces cannot be
 * instantiated with Kotlin constructors.
 *
 * @param url absolute request target
 * @param method HTTP method name
 * @param data already-encoded request body, or `null` for no body
 * @param timeoutMillis host timeout in milliseconds, or `null` for the host default
 */
internal fun wxRequestOptions(
    url: String,
    method: String,
    data: Any? = null,
    timeoutMillis: Int? = null,
): WxRequestOptions {
    val options: WxRequestOptions = js("({})")
    options.url = url
    options.method = method
    options.dataType = RAW_TEXT_DATA_TYPE
    if (data != null) {
        options.data = data
    }
    if (timeoutMillis != null) {
        options.timeout = timeoutMillis
    }
    return options
}

/**
 * The `dataType` value that keeps the response body unparsed.
 *
 * Any value other than `json` makes WeChat return the raw response text, which
 * is what the platform-neutral transport contract carries.
 */
private const val RAW_TEXT_DATA_TYPE: String = "text"
