package io.github.bobcgn.miniapp.host.wechat.interop

import io.github.bobcgn.miniapp.host.wechat.testing.fakeSubscribeMessageSuccess
import kotlin.js.jsTypeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class WxRequestSubscribeMessageTest {
    @Test
    fun theOptionsCarryTheTemplateIdsInOrder() {
        val options = wxRequestSubscribeMessageOptions(
            listOf("template-one", "template-two", "template-three"),
        )

        val forwarded = options.tmplIds as Array<*>
        assertEquals(
            listOf("template-one", "template-two", "template-three"),
            forwarded.toList(),
        )
        assertEquals("string", jsTypeOf(forwarded[0]))
        assertEquals("undefined", jsTypeOf(options.success))
        assertEquals("undefined", jsTypeOf(options.fail))
        assertEquals("undefined", jsTypeOf(options.complete))
    }

    @Test
    fun anEmptyTemplateListStillProducesTheField() {
        // The request model refuses an empty list before this point, but the factory
        // must still send a real array rather than leaving the field undefined, so a
        // mistake surfaces as the host's answer instead of as an unset option.
        val options = wxRequestSubscribeMessageOptions(emptyList())

        val forwarded = options.tmplIds as Array<*>
        assertEquals(emptyList(), forwarded.toList())
    }

    @Test
    fun theOptionsAcceptCallbacks() {
        val options = wxRequestSubscribeMessageOptions(listOf("template-one"))
        options.success = { result -> result.errMsg }
        options.fail = { result -> result.errMsg.length }

        assertNotNull(options.success)
        assertNotNull(options.fail)
    }

    @Test
    fun thePresenceGuardAnswersFalseWithoutAWxGlobal() {
        assertFalse(hasWxRequestSubscribeMessage())
    }

    @Test
    fun theAnswerIsReadByTemplateId() {
        val answer = wxSubscriptionAnswer(
            fakeSubscribeMessageSuccess("template-one" to "accept"),
        ) as WxSubscriptionAnswer.Present

        assertEquals(
            mapOf("template-one" to WxSubscriptionEntry.Text("accept")),
            answer.entries,
        )
    }

    @Test
    fun severalTemplatesAreReadWithTheirOwnStatuses() {
        val answer = wxSubscriptionAnswer(
            fakeSubscribeMessageSuccess(
                "template-one" to "accept",
                "template-two" to "reject",
                "template-three" to "ban",
            ),
        ) as WxSubscriptionAnswer.Present

        assertEquals(
            mapOf(
                "template-one" to WxSubscriptionEntry.Text("accept"),
                "template-two" to WxSubscriptionEntry.Text("reject"),
                "template-three" to WxSubscriptionEntry.Text("ban"),
            ),
            answer.entries,
        )
    }

    @Test
    fun theHostsStatusLineIsNotReadAsATemplate() {
        // WeChat puts its own status line in the same object as the per-template
        // answers, so reading it as one would invent a template the caller never
        // asked about.
        val answer = wxSubscriptionAnswer(
            fakeSubscribeMessageSuccess("template-one" to "accept"),
        ) as WxSubscriptionAnswer.Present

        assertFalse(answer.entries.containsKey("errMsg"))
        assertEquals(setOf("template-one"), answer.entries.keys)
    }

    @Test
    fun anAnswerThatNamesNoTemplateIsStillAnAnswer() {
        val answer = wxSubscriptionAnswer(fakeSubscribeMessageSuccess())

        assertEquals(WxSubscriptionAnswer.Present(emptyMap()), answer)
    }

    @Test
    fun aStatusThatIsNotTextIsReportedAsSuch() {
        // The adapter decides what this means for a template the caller asked about;
        // the interop layer's job is to say that the value is not text.
        val answer = wxSubscriptionAnswer(
            fakeSubscribeMessageSuccess("template-one" to 7),
        ) as WxSubscriptionAnswer.Present

        assertEquals(WxSubscriptionEntry.NotText, answer.entries["template-one"])
    }

    @Test
    fun everyNonTextShapeIsReportedAsNotText() {
        listOf<Any?>(7, true, js("({})"), js("[]"), null).forEach { value ->
            val answer = wxSubscriptionAnswer(
                fakeSubscribeMessageSuccess("template-one" to value),
            ) as WxSubscriptionAnswer.Present

            assertEquals(
                WxSubscriptionEntry.NotText,
                answer.entries["template-one"],
                "value $value",
            )
        }
    }

    @Test
    fun anAnswerThatIsNotAnObjectIsUnreadable() {
        val notAnObject: WxRequestSubscribeMessageSuccessResult = js("'requestSubscribeMessage:ok'")
        val aNumber: WxRequestSubscribeMessageSuccessResult = js("7")
        val nothing: WxRequestSubscribeMessageSuccessResult = js("null")

        assertEquals(WxSubscriptionAnswer.Unreadable, wxSubscriptionAnswer(notAnObject))
        assertEquals(WxSubscriptionAnswer.Unreadable, wxSubscriptionAnswer(aNumber))
        assertEquals(WxSubscriptionAnswer.Unreadable, wxSubscriptionAnswer(nothing))
    }

    @Test
    fun anEmptyStatusIsPreservedRatherThanTreatedAsAbsent() {
        // The interop layer reports what the host said; whether an empty status is
        // meaningful is the adapter's question, and it decides with the value in hand.
        val answer = wxSubscriptionAnswer(
            fakeSubscribeMessageSuccess("template-one" to ""),
        ) as WxSubscriptionAnswer.Present

        assertEquals(WxSubscriptionEntry.Text(""), answer.entries["template-one"])
    }

    @Test
    fun aTemplateIdKeyIsPreservedVerbatim() {
        // Template identifiers are opaque host data, so the key is carried through
        // exactly as the host wrote it.
        val id = "aB3-_xYz0123456789abcdefghijklmnopqrstuvwxyz"
        val answer = wxSubscriptionAnswer(
            fakeSubscribeMessageSuccess(id to "accept"),
        ) as WxSubscriptionAnswer.Present

        assertEquals(setOf(id), answer.entries.keys)
    }
}
