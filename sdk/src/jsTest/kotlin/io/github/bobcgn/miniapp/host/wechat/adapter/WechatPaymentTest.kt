package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.WeChatPaymentRequest
import io.github.bobcgn.miniapp.host.wechat.WeChatPaymentSignType
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatPaymentHost
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * How the WeChat adapter turns the host's payment callbacks into SDK behaviour.
 *
 * These are the raw-callback behaviours; `WechatPaymentContractTest` states what must
 * hold for the SDK as a whole.
 */
internal class WechatPaymentTest {
    @Test
    fun theWholeRequestReachesTheHost() = runTest {
        val host = FakeWechatPaymentHost()

        WechatPayment(host).request(
            WeChatPaymentRequest(
                timeStamp = "1700000000",
                nonceStr = "nonce",
                prepayPackage = "prepay_id=abc",
                signType = WeChatPaymentSignType.HMAC_SHA256,
                paySign = "signature",
            ),
        )

        val forwarded = requireNotNull(host.lastRequest)
        assertEquals("1700000000", forwarded.timeStamp)
        assertEquals("nonce", forwarded.nonceStr)
        assertEquals("prepay_id=abc", forwarded.prepayPackage)
        assertEquals(WeChatPaymentSignType.HMAC_SHA256, forwarded.signType)
        assertEquals("signature", forwarded.paySign)
        assertEquals(1, host.calls)
    }

    @Test
    fun everySignTypeTheHostAcceptsIsForwarded() {
        // The installed base library enumerates exactly these two, so exactly these two are
        // modelled and neither is translated into something else on the way out.
        assertEquals("MD5", WeChatPaymentSignType.MD5.hostValue)
        assertEquals("HMAC-SHA256", WeChatPaymentSignType.HMAC_SHA256.hostValue)
        assertEquals(2, WeChatPaymentSignType.entries.size)
    }

    @Test
    fun eachBlankFieldIsRejectedBeforeTheHostIsCalled() = runTest {
        // A blank field names no value, and forwarding one would ask the host to verify a
        // payment parameter that does not exist.
        val blanks = listOf(
            "timeStamp",
            "nonceStr",
            "prepayPackage",
            "paySign",
        )

        blanks.forEach { field ->
            val host = FakeWechatPaymentHost()

            assertFailsWith<IllegalArgumentException>(field) {
                // The request model refuses it, which is where a caller's mistake belongs:
                // the host is never reached with a parameter that names nothing.
                WechatPayment(host).request(
                    payment(
                        timeStamp = if (field == "timeStamp") "   " else "1700000000",
                        nonceStr = if (field == "nonceStr") "" else "nonce",
                        prepayPackage = if (field == "prepayPackage") "  " else "prepay_id=abc",
                        paySign = if (field == "paySign") "" else "signature",
                    ),
                )
            }
            assertEquals(0, host.calls, field)
        }
    }

    @Test
    fun aCompletedInteractionSettlesWithoutInventingAnOrderFact() = runTest {
        // The host reports that the interaction completed. That is all this call can mean
        // and all it returns: there is no value here to mistake for an order.
        val host = FakeWechatPaymentHost()

        WechatPayment(host).request(payment())
    }

    @Test
    fun aFailureAfterSuccessDoesNotChangeTheFirstTerminalState() = runTest {
        val host = FakeWechatPaymentHost().apply { answerImmediately = false }
        val call = async(start = CoroutineStart.UNDISPATCHED) { WechatPayment(host).request(payment()) }

        host.complete()
        host.failWith("requestPayment:fail too late")

        call.await()
    }

    @Test
    fun aRepeatedSuccessIsSettledOnce() = runTest {
        val host = FakeWechatPaymentHost().apply { answerImmediately = false }
        val call = async(start = CoroutineStart.UNDISPATCHED) { WechatPayment(host).request(payment()) }

        host.complete()
        host.complete()

        call.await()
    }

    @Test
    fun aSuccessAfterFailureDoesNotChangeTheFirstTerminalState() = runTest {
        val host = FakeWechatPaymentHost().apply { answerImmediately = false }

        // A supervisor scope, because a failing child would otherwise fail this test through
        // structured concurrency before the assertion below could inspect it.
        supervisorScope {
            val call = async(start = CoroutineStart.UNDISPATCHED) {
                WechatPayment(host).request(payment())
            }

            host.failWith("requestPayment:fail -1")
            host.complete()

            assertFailsWith<MiniAppException.HostFailure> { call.await() }
        }
    }

    @Test
    fun theExactCancellationSignalBecomesAnInterruptionRatherThanAFailure() = runTest {
        // The installed base library's own payment flow reports an ended interaction with
        // this exact message, and the SDK reports it as an interruption whose cause it does
        // not claim to know. It is deliberately not a permission answer and not a
        // user-cancellation claim either.
        val host = FakeWechatPaymentHost().apply { failureMessage = "requestPayment:cancel" }

        val failure: MiniAppException = assertFailsWith<MiniAppException.HostInteractionInterrupted> {
            WechatPayment(host).request(payment())
        }

        val interrupted = assertIs<MiniAppException.HostInteractionInterrupted>(failure)
        assertEquals("wechat", interrupted.host)
        assertEquals("requestPayment", interrupted.operation)
        assertEquals("requestPayment:cancel", interrupted.hostMessage)
        // Read through the base type, because these are claims about the runtime type: an
        // ended payment is not a `HostFailure`, not a permission answer, and not a
        // user-cancellation claim, which are three separate types in this error model.
        val measured: Any = failure
        assertFalse(measured is MiniAppException.HostFailure)
        assertFalse(measured is MiniAppException.PermissionDenied)
        assertFalse(measured is MiniAppException.UserCancelled)
    }

    @Test
    fun aMessageThatMerelyMentionsCancellingIsAHostFailure() = runTest {
        // Only the message the host's own payment flow was observed to produce is an
        // interruption. In particular `requestPayment:fail cancel` is *not* matched: the
        // host's other interfaces use that form, but no evidence covers it for this API,
        // and a guess in this path would decide whether a payment attempt was cancelled or
        // broken. See the capability matrix for this open item.
        val nearMisses = listOf(
            "requestPayment:fail cancel",
            "requestPayment:fail cancelled",
            "requestPayment:fail cancellation",
            "requestPayment:fail user cancel",
            "requestPayment:cancelX",
            "RequestPayment:cancel",
            "requestpayment:cancel",
            "requestPayment:fail",
            "requestPayment:fail -1",
            "cancel",
            "",
        )

        nearMisses.forEach { message ->
            val host = FakeWechatPaymentHost().apply { failureMessage = message }

            val failure = assertFailsWith<MiniAppException.HostFailure>("'$message'") {
                WechatPayment(host).request(payment())
            }

            assertEquals("requestPayment", failure.metadata["operation"])
            assertEquals(message, failure.hostMessage)
        }
    }

    @Test
    fun anUnreadableAnswerIsAnInvalidResponse() = runTest {
        assertFailsWith<MiniAppException.InvalidResponse> {
            WechatPayment(MalformedAnswerHost()).request(payment())
        }
    }

    @Test
    fun aHostWithoutThePaymentApiIsAnUnsupportedCapability() = runTest {
        val host = FakeWechatPaymentHost(supported = false)

        val failure = assertFailsWith<MiniAppException.UnsupportedCapability> {
            WechatPayment(host).request(payment())
        }

        assertEquals("wechat.request-payment", failure.capability.value)
        // The host was never asked, so no payment interface was opened.
        assertEquals(0, host.calls)
    }

    @Test
    fun eachCallCarriesItsOwnParameters() = runTest {
        // Nothing about a previous payment is kept: a second call hands the host the second
        // set of parameters rather than replaying the first.
        val host = FakeWechatPaymentHost()
        val payment = WechatPayment(host)

        payment.request(payment(paySign = "first-signature"))
        assertEquals("first-signature", requireNotNull(host.lastRequest).paySign)

        payment.request(payment(paySign = "second-signature"))
        assertEquals("second-signature", requireNotNull(host.lastRequest).paySign)
        assertEquals(2, host.calls)
    }

    @Test
    fun cancellingTheCallerLeavesTheHostInteractionAlone() = runTest {
        // The host offers no abort handle, so cancellation stops the caller waiting and
        // nothing more: the SDK does not claim it stopped the payment interface, and a
        // later callback finds nobody listening.
        val host = FakeWechatPaymentHost().apply { answerImmediately = false }
        val call = async(start = CoroutineStart.UNDISPATCHED) { WechatPayment(host).request(payment()) }

        call.cancelAndJoin()
        host.complete()

        assertTrue(call.isCancelled)
    }

    private fun payment(
        timeStamp: String = "1700000000",
        nonceStr: String = "nonce",
        prepayPackage: String = "prepay_id=abc",
        paySign: String = "signature",
    ): WeChatPaymentRequest = WeChatPaymentRequest(
        timeStamp = timeStamp,
        nonceStr = nonceStr,
        prepayPackage = prepayPackage,
        signType = WeChatPaymentSignType.MD5,
        paySign = paySign,
    )

    /** A port that answers with a payload that is not an object at all. */
    private class MalformedAnswerHost : WechatPaymentHost {
        override fun isSupported(): Boolean = true

        override fun request(
            request: WeChatPaymentRequest,
            success: (io.github.bobcgn.miniapp.host.wechat.interop.WxRequestPaymentSuccessResult) -> Unit,
            failure: (io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult) -> Unit,
        ) {
            val notAnObject: io.github.bobcgn.miniapp.host.wechat.interop.WxRequestPaymentSuccessResult =
                js("'requestPayment:ok'")
            success(notAnObject)
        }
    }
}
