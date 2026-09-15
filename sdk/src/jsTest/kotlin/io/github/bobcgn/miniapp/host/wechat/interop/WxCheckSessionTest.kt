package io.github.bobcgn.miniapp.host.wechat.interop

import kotlin.js.jsTypeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class WxCheckSessionTest {
    @Test
    fun checkSessionOptionsLeaveCallbacksAbsentUntilAssigned() {
        val options = wxCheckSessionOptions()

        assertEquals("undefined", jsTypeOf(options.success))
        assertEquals("undefined", jsTypeOf(options.fail))
        assertEquals("undefined", jsTypeOf(options.complete))

        options.success = { result -> result.errMsg.length }
        options.fail = { result -> result.errMsg.length }
        assertNotNull(options.success)
        assertNotNull(options.fail)
    }

    @Test
    fun thePresenceGuardAnswersFalseWithoutAWxGlobal() {
        // The Node test environment has no wx, so the guard must answer rather
        // than throw on a missing global.
        assertFalse(hasWxCheckSession())
    }
}
