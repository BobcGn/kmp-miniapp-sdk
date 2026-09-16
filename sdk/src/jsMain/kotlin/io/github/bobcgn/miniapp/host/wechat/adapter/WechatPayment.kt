package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.async.awaitHostCallback
import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.WeChatDeviceCapabilities
import io.github.bobcgn.miniapp.host.wechat.WeChatPaymentRequest
import io.github.bobcgn.miniapp.host.wechat.interop.WxPaymentAnswer
import io.github.bobcgn.miniapp.host.wechat.interop.wxPaymentAnswer

/**
 * WeChat standard payment, deliberately WeChat-specific.
 *
 * A merchant account, an order, and WeChat's payment interface are all WeChat concepts,
 * so nothing here is promoted to a host-neutral capability: there is no portable notion
 * of "pay" for another host to satisfy.
 *
 * What the SDK owns is narrow on purpose. It carries the parameters a trusted backend
 * produced to the host and reports what the host said about the interaction. It computes
 * no signature, holds no merchant key, knows no order, and — most importantly — it never
 * turns the host's answer into an order fact.
 *
 * Resolving means one thing: the host reported that the payment interaction completed.
 * It is not a payment, not a receipt, and not proof that money moved. The authoritative
 * answer belongs to the consumer backend, which learns it from WeChat Pay's server API,
 * from its asynchronous notification, or by querying the order. A consumer that treats
 * this success as final will hand over goods nobody paid for, which is why nothing here
 * is named paid, settled, or confirmed.
 *
 * WeChat reports no task handle for this call, so coroutine cancellation only stops the
 * caller waiting: the payment interface is the host's own, and the SDK does not abort it.
 */
internal class WechatPayment(
    private val host: WechatPaymentHost = WxRequestPaymentHost,
) {
    /**
     * Asks the host to run the payment interaction described by [request].
     *
     * The caller must call this from a user gesture: WeChat requires one for a payment,
     * and the SDK neither supplies one nor retries when the host refuses because none
     * preceded the call. Nothing in this SDK calls it on page load.
     *
     * @throws MiniAppException.UnsupportedCapability when the host has no payment API
     * @throws MiniAppException.HostInteractionInterrupted when the interaction ended
     *   without completing, which is also what a dismissal produces
     * @throws MiniAppException.InvalidResponse when the host's callback payload is not a
     *   value the contract can carry
     * @throws MiniAppException.HostFailure when the payment fails for any other reason
     */
    suspend fun request(request: WeChatPaymentRequest) {
        if (!host.isSupported()) {
            throw MiniAppException.UnsupportedCapability(WeChatDeviceCapabilities.RequestPayment)
        }

        val answer = awaitHostCallback { success, failure ->
            host.request(
                request = request,
                success = { result -> success(wxPaymentAnswer(result)) },
                failure = { result ->
                    failure(mapWechatRequestPaymentFailure(result))
                },
            )
            null
        }

        when (answer) {
            WxPaymentAnswer.Completed -> Unit

            WxPaymentAnswer.Unreadable -> throw MiniAppException.InvalidResponse(
                "The WeChat host answered requestPayment with a payload the SDK cannot read",
            )
        }
    }
}
