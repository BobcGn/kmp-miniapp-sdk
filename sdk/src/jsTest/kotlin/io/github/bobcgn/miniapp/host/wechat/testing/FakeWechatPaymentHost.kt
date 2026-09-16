package io.github.bobcgn.miniapp.host.wechat.testing

import io.github.bobcgn.miniapp.host.wechat.WeChatPaymentRequest
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatPaymentHost
import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxRequestPaymentSuccessResult

/**
 * The FakeAdapter boundary for payment: a stand-in for the WeChat `requestPayment`
 * callbacks.
 *
 * It implements the raw callback port, so a test can drive `WechatPayment` without a
 * WeChat runtime and without a merchant account. It records the request it was handed so
 * a test can assert exactly what reached the host — which matters here, because the
 * values being forwarded are payment parameters.
 *
 * By default it answers immediately. A test that needs to complete the interaction by
 * hand sets [answerImmediately] to false, which is also how repeated and late callbacks
 * are produced.
 *
 * @param supported whether this host exposes the payment API
 */
internal class FakeWechatPaymentHost(
    var supported: Boolean = true,
) : WechatPaymentHost {
    /** Message the payment fails with, or `null` to succeed. */
    var failureMessage: String? = null

    /** Whether the host answers during the call, rather than waiting to be driven. */
    var answerImmediately: Boolean = true

    var calls: Int = 0
        private set

    /** The request last handed to the host, so a test can assert what was forwarded. */
    var lastRequest: WeChatPaymentRequest? = null
        private set

    private var succeed: (() -> Unit)? = null
    private var fail: (() -> Unit)? = null

    override fun isSupported(): Boolean = supported

    override fun request(
        request: WeChatPaymentRequest,
        success: (WxRequestPaymentSuccessResult) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        calls += 1
        lastRequest = request

        succeed = { success(fakePaymentSuccess()) }
        fail = { failure(fakeWxFailure(failureMessage ?: "requestPayment:fail")) }

        if (answerImmediately) {
            if (failureMessage != null) fail?.invoke() else succeed?.invoke()
        }
    }

    /** Completes the payment interaction, as a host that succeeded would. */
    fun complete(): Unit = requireNotNull(succeed) { "No payment is in flight" }.invoke()

    /** Fails the payment interaction with [message]. */
    fun failWith(message: String) {
        failureMessage = message
        requireNotNull(fail) { "No payment is in flight" }.invoke()
    }
}
