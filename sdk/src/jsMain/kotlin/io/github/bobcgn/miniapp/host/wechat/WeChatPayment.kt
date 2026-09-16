package io.github.bobcgn.miniapp.host.wechat

/**
 * The signature algorithms the host accepts for a payment request.
 *
 * The installed base library enumerates exactly these two for `requestPayment`, so the
 * set is closed: a caller cannot ask for something the host does not accept, and the SDK
 * never widens the union on its own.
 *
 * Which one to use is the trusted backend's decision, because the backend is what signs.
 * The SDK only forwards the name of the algorithm the backend used.
 */
internal enum class WeChatPaymentSignType(
    /** The value [io.github.bobcgn.miniapp.host.wechat.interop.wx] expects. */
    internal val hostValue: String,
) {
    /** WeChat's older MD5 signature. */
    MD5("MD5"),

    /** WeChat's HMAC-SHA256 signature, which WeChat recommends for new integrations. */
    HMAC_SHA256("HMAC-SHA256"),
}

/**
 * One payment request: exactly the parameters the host accepts, all produced by a
 * trusted backend.
 *
 * **The SDK creates none of this.** It does not compute a signature, does not hold a
 * merchant key, does not obtain a prepay identifier, and does not know what an order is.
 * Every value here is what a consumer's trusted backend returned from WeChat Pay's
 * unified-order API, forwarded to the host unchanged. The host is what validates them,
 * and a consumer that fabricates one has produced something the host will reject — no
 * part of this SDK helps with that, and none of it can.
 *
 * Every field must be non-blank. A blank field names no value, so it is a caller mistake
 * rather than something to hand to the host.
 *
 * Nothing here is logged, cached, or persisted. `nonceStr`, [prepayPackage], and
 * `paySign` are credentials for one payment interaction; the SDK neither prints them nor
 * writes them to storage, and neither should a consumer.
 *
 * @property timeStamp the timestamp the backend signed
 * @property nonceStr the random string the backend signed
 * @property prepayPackage the prepay package the backend signed, which the host receives
 *   under its own `package` field name
 * @property signType the algorithm the backend used
 * @property paySign the backend's signature
 */
internal class WeChatPaymentRequest(
    internal val timeStamp: String,
    internal val nonceStr: String,
    internal val prepayPackage: String,
    internal val signType: WeChatPaymentSignType,
    internal val paySign: String,
) {
    init {
        require(timeStamp.isNotBlank()) {
            "requestPayment requires a timeStamp, because the host cannot verify a blank one"
        }
        require(nonceStr.isNotBlank()) {
            "requestPayment requires a nonceStr, because the host cannot verify a blank one"
        }
        require(prepayPackage.isNotBlank()) {
            "requestPayment requires a package, because a blank one names no prepayment"
        }
        require(paySign.isNotBlank()) {
            "requestPayment requires a paySign, because a blank signature cannot be valid"
        }
    }
}
