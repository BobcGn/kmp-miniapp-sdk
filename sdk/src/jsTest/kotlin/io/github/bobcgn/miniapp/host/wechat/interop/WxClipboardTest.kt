package io.github.bobcgn.miniapp.host.wechat.interop

import io.github.bobcgn.miniapp.host.wechat.testing.fakeGetClipboardDataSuccess
import kotlin.js.jsTypeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class WxClipboardTest {
    @Test
    fun getClipboardOptionsLeaveCallbacksAbsentUntilAssigned() {
        val options = wxGetClipboardDataOptions()

        assertEquals("undefined", jsTypeOf(options.success))
        assertEquals("undefined", jsTypeOf(options.fail))
        assertEquals("undefined", jsTypeOf(options.complete))

        options.success = { result -> result.data; result.errMsg }
        assertNotNull(options.success)
    }

    @Test
    fun setClipboardOptionsCarryTheText() {
        val options = wxSetClipboardDataOptions("kmp-miniapp-sdk clipboard test")

        assertEquals("kmp-miniapp-sdk clipboard test", options.data)
        assertEquals("undefined", jsTypeOf(options.success))
        assertEquals("undefined", jsTypeOf(options.fail))
    }

    @Test
    fun thePresenceGuardsAreIndependent() {
        // Each direction is probed on its own, so neither can imply the other, and
        // the Node environment has no wx at all.
        assertFalse(hasWxGetClipboardData())
        assertFalse(hasWxSetClipboardData())
    }

    @Test
    fun aTextAnswerIsRead() {
        val result = fakeGetClipboardDataSuccess("copied text")

        assertEquals(WxClipboardText.Present("copied text"), wxClipboardText(result))
    }

    @Test
    fun anEmptyClipboardIsAValueRatherThanAnUnreadableAnswer() {
        val result = fakeGetClipboardDataSuccess("")

        // An empty clipboard and an answer the contract cannot carry are different
        // things, so this must not be collapsed into the unreadable case.
        assertEquals(WxClipboardText.Present(""), wxClipboardText(result))
    }

    @Test
    fun aMissingTextIsUnreadable() {
        val result = fakeGetClipboardDataSuccess(null)

        assertEquals(WxClipboardText.Unreadable, wxClipboardText(result))
    }

    @Test
    fun aTextThatIsNotAStringIsUnreadable() {
        val result = fakeGetClipboardDataSuccess(42)

        assertEquals(WxClipboardText.Unreadable, wxClipboardText(result))
    }

    @Test
    fun anObjectAnswerIsUnreadable() {
        val result = fakeGetClipboardDataSuccess(js("({ text: 'value' })"))

        assertEquals(WxClipboardText.Unreadable, wxClipboardText(result))
    }
}
