package io.github.bobcgn.miniapp.host.wechat.interop

import kotlin.js.jsTypeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class WxVibrateTest {
    @Test
    fun vibrateOptionsLeaveCallbacksAbsentUntilAssigned() {
        val options = wxVibrateOptions()

        assertEquals("undefined", jsTypeOf(options.success))
        assertEquals("undefined", jsTypeOf(options.fail))
        assertEquals("undefined", jsTypeOf(options.complete))

        options.success = { result -> result.errMsg.length }
        options.fail = { result -> result.errMsg.length }
        assertNotNull(options.success)
        assertNotNull(options.fail)
    }

    @Test
    fun thePresenceGuardsAreIndependent() {
        // The two vibrations are separate host APIs, so neither implies the other,
        // and the Node environment has no wx at all.
        assertFalse(hasWxVibrateShort())
        assertFalse(hasWxVibrateLong())
    }
}
