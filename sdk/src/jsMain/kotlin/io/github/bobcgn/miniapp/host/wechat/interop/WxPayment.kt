package io.github.bobcgn.miniapp.host.wechat.interop

import kotlin.js.jsTypeOf

/**
 * Raw callback-style options accepted by [wx.requestPayment].
 *
 * The base library shipped with the installed Developer Tools enumerates exactly five
 * fields — `timeStamp`, `nonceStr`, `package`, `signType`, and `paySign` — and
 * constrains `signType` to `MD5` and `HMAC-SHA256`. No other field or sign type is
 * listed there, so none is modelled.
 *
 * Every value is a payment parameter a trusted backend produced. This layer only
 * carries what it is given: it computes no signature and holds no key.
 */
internal external interface WxRequestPaymentOptions {
    /** Documented type is `string`: the timestamp the backend put in its signature. */
    var timeStamp: String

    /** Documented type is `string`: the random string the backend put in its signature. */
    var nonceStr: String

    /** Documented type is `string`: the prepay package the backend signed. */
    var `package`: String

    /** Documented type is `string`: `MD5` or `HMAC-SHA256`, as the backend signed. */
    var signType: String

    /** Documented type is `string`: the backend's signature. */
    var paySign: String

    /** Called only when the host reports the payment interaction completed. */
    var success: WxRequestPaymentSuccessCallback?

    /** Called when the interaction did not complete, including when it was dismissed. */
    var fail: WxGeneralCallback?

    /** Called after either [success] or [fail]. */
    var complete: WxGeneralCallback?
}

/**
 * Raw success result returned by [wx.requestPayment].
 *
 * The installed base library declares no result fields for this call — its success
 * shape is empty — so nothing is read from this payload: the callback itself is the
 * whole signal. It is declared here so the adapter can still reject a payload that is
 * not an object at all, which is the only thing about it that can be wrong.
 */
internal external interface WxRequestPaymentSuccessResult : WxGeneralCallbackResult

/** Success callback accepted by [WxRequestPaymentOptions]. */
internal typealias WxRequestPaymentSuccessCallback = (WxRequestPaymentSuccessResult) -> Unit

/**
 * What a raw [wx.requestPayment] success callback carries.
 *
 * A raw JavaScript object cannot cross out of this package, so this is the typed shape
 * the adapter reads.
 */
internal sealed interface WxPaymentAnswer {
    /** The host reported that the payment interaction completed. */
    data object Completed : WxPaymentAnswer

    /** The host's callback payload is not a value the contract can carry. */
    data object Unreadable : WxPaymentAnswer
}

/**
 * Reads a raw payment success payload.
 *
 * This contract declares no result fields, so there is no field to validate and nothing
 * to invent: the only way the payload can be wrong is by not being an object. Everything
 * else the caller needs to know about an order comes from its own backend.
 *
 * The payload is read as nullable because a JavaScript host can pass anything, including
 * `null` — whose `typeof` is `"object"`, which is why nullness is checked separately.
 */
internal fun wxPaymentAnswer(result: WxRequestPaymentSuccessResult?): WxPaymentAnswer {
    if (result == null || jsTypeOf(result) != "object") return WxPaymentAnswer.Unreadable
    return WxPaymentAnswer.Completed
}

/**
 * Whether [wx.requestPayment] exists.
 *
 * This is the only thing the probe answers. Whether a merchant is configured, whether an
 * order exists, and whether the parameters are valid are all questions the host answers
 * when it is called, and none of them is an API-presence question.
 */
internal fun hasWxRequestPayment(): Boolean =
    js("typeof wx !== 'undefined' && typeof wx.requestPayment === 'function'")

/**
 * Creates a plain JavaScript option bag for [wx.requestPayment].
 *
 * External interfaces cannot be instantiated with Kotlin constructors, so the factory
 * exists solely to produce the object the host expects.
 *
 * @param timeStamp backend-provided timestamp
 * @param nonceStr backend-provided random string
 * @param prepayPackage backend-provided prepay package, which the host receives as `package`
 * @param signType the sign type the backend used, as the host names it
 * @param paySign backend-provided signature
 */
internal fun wxRequestPaymentOptions(
    timeStamp: String,
    nonceStr: String,
    prepayPackage: String,
    signType: String,
    paySign: String,
): WxRequestPaymentOptions {
    val options: WxRequestPaymentOptions = js("({})")
    options.timeStamp = timeStamp
    options.nonceStr = nonceStr
    options.`package` = prepayPackage
    options.signType = signType
    options.paySign = paySign
    return options
}
