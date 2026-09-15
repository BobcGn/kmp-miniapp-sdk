package io.github.bobcgn.miniapp.host.wechat.interop

import io.github.bobcgn.miniapp.host.wechat.testing.fakeWxAuthSetting
import kotlin.js.jsTypeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class WxPermissionTest {
    @Test
    fun getSettingOptionsLeaveCallbacksAbsentUntilAssigned() {
        val options = wxGetSettingOptions()

        assertEquals("undefined", jsTypeOf(options.success))
        assertEquals("undefined", jsTypeOf(options.fail))
        assertEquals("undefined", jsTypeOf(options.complete))

        options.success = { result -> result.authSetting; result.errMsg }
        assertNotNull(options.success)
    }

    @Test
    fun authorizeOptionsCarryOnlyTheScope() {
        val options = wxAuthorizeOptions("scope.record")

        assertEquals("scope.record", options.scope)
        assertEquals("undefined", jsTypeOf(options.success))
        assertEquals("undefined", jsTypeOf(options.fail))

        options.fail = { result -> result.errMsg.length }
        assertNotNull(options.fail)
    }

    @Test
    fun openSettingOptionsLeaveCallbacksAbsentUntilAssigned() {
        val options = wxOpenSettingOptions()

        assertEquals("undefined", jsTypeOf(options.success))
        assertEquals("undefined", jsTypeOf(options.fail))

        options.success = { result -> result.authSetting }
        assertNotNull(options.success)
    }

    @Test
    fun aGrantedScopeReadsAsADecision() {
        val map = fakeWxAuthSetting("scope.record" to true)

        assertEquals(WxScopeEntry.Decided(granted = true), wxScopeEntry(map, "scope.record"))
    }

    @Test
    fun aRefusedScopeReadsAsADecision() {
        val map = fakeWxAuthSetting("scope.record" to false)

        assertEquals(WxScopeEntry.Decided(granted = false), wxScopeEntry(map, "scope.record"))
    }

    @Test
    fun aScopeTheHostNeverDecidedIsAbsentRatherThanRefused() {
        val map = fakeWxAuthSetting("scope.record" to true)

        assertEquals(WxScopeEntry.Absent, wxScopeEntry(map, "scope.something-else"))
    }

    @Test
    fun aValueThatIsNotABooleanIsUnreadable() {
        val map = fakeWxAuthSetting("scope.record" to 1)

        assertEquals(WxScopeEntry.Unreadable, wxScopeEntry(map, "scope.record"))
    }

    @Test
    fun aMissingOrNonObjectMapIsUnreadable() {
        assertEquals(WxScopeEntry.Unreadable, wxScopeEntry(null, "scope.record"))
        assertEquals(WxScopeEntry.Unreadable, wxScopeEntry("not a map", "scope.record"))
    }

    @Test
    fun anInheritedKeyIsNotMistakenForADecision() {
        // The map is read with an own-property check, so a prototype member such
        // as `toString` is not reported as a scope the host decided.
        val map = fakeWxAuthSetting()

        assertEquals(WxScopeEntry.Absent, wxScopeEntry(map, "toString"))
    }
}
