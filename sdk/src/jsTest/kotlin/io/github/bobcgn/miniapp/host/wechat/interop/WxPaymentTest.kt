package io.github.bobcgn.miniapp.host.wechat.interop

import io.github.bobcgn.miniapp.host.wechat.testing.fakePaymentSuccess
import kotlin.js.jsTypeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class WxPaymentTest {
    @Test
    fun theOptionsCarryExactlyTheHostsPaymentFields() {
        val options = wxRequestPaymentOptions(
            timeStamp = "1700000000",
            nonceStr = "nonce",
            prepayPackage = "prepay_id=abc",
            signType = "MD5",
            paySign = "signature",
        )

        assertEquals("1700000000", options.timeStamp)
        assertEquals("nonce", options.nonceStr)
        // The host's field is named `package`, and that is the name it must receive.
        assertEquals("prepay_id=abc", options.`package`)
        assertEquals("MD5", options.signType)
        assertEquals("signature", options.paySign)
        assertEquals("undefined", jsTypeOf(options.success))
        assertEquals("undefined", jsTypeOf(options.fail))
        assertEquals("undefined", jsTypeOf(options.complete))
    }

    @Test
    fun theOptionBagExposesNoFieldTheHostDoesNotAccept() {
        // The installed base library enumerates five fields and nothing else, so the bag
        // carries five and nothing else: an extra field would be something the SDK
        // invented about payments.
        val options = wxRequestPaymentOptions(
            timeStamp = "1700000000",
            nonceStr = "nonce",
            prepayPackage = "prepay_id=abc",
            signType = "HMAC-SHA256",
            paySign = "signature",
        )

        val keys: Array<String> = js("Object.keys(options)")
        assertEquals(
            listOf("timeStamp", "nonceStr", "package", "signType", "paySign"),
            keys.toList(),
        )
    }

    @Test
    fun theOptionsAcceptCallbacks() {
        val options = wxRequestPaymentOptions(
            timeStamp = "1700000000",
            nonceStr = "nonce",
            prepayPackage = "prepay_id=abc",
            signType = "MD5",
            paySign = "signature",
        )
        options.success = { result -> result.errMsg }
        options.fail = { result -> result.errMsg.length }

        assertNotNull(options.success)
        assertNotNull(options.fail)
    }

    @Test
    fun thePresenceGuardAnswersFalseWithoutAWxGlobal() {
        assertFalse(hasWxRequestPayment())
    }

    @Test
    fun aSuccessPayloadIsRecognizedAsTheCompletionSignal() {
        // The contract declares no result fields, so the callback itself is the signal and
        // there is nothing in it to validate beyond being an object.
        assertEquals(WxPaymentAnswer.Completed, wxPaymentAnswer(fakePaymentSuccess()))
    }

    @Test
    fun aPayloadThatIsNotAnObjectIsUnreadable() {
        val text: WxRequestPaymentSuccessResult = js("'requestPayment:ok'")
        val number: WxRequestPaymentSuccessResult = js("7")
        val nothing: WxRequestPaymentSuccessResult = js("null")

        assertEquals(WxPaymentAnswer.Unreadable, wxPaymentAnswer(text))
        assertEquals(WxPaymentAnswer.Unreadable, wxPaymentAnswer(number))
        assertEquals(WxPaymentAnswer.Unreadable, wxPaymentAnswer(nothing))
    }

    @Test
    fun theReaderInventsNothingFromAPayloadThatCarriesNoFields() {
        // A payload with no fields at all is still the completion signal: the SDK reads no
        // field from it, so it has nothing to require and nothing to fill in. Everything a
        // caller needs about an order comes from its own backend.
        val empty: WxRequestPaymentSuccessResult = js("({})")

        assertEquals(WxPaymentAnswer.Completed, wxPaymentAnswer(empty))
    }
}
