package io.github.bobcgn.miniapp.host.wechat.interop

import io.github.bobcgn.miniapp.host.wechat.testing.fakeGetPrivacySettingSuccess
import kotlin.js.jsTypeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class WxPrivacyTest {
    @Test
    fun getPrivacySettingOptionsLeaveCallbacksAbsentUntilAssigned() {
        val options = wxGetPrivacySettingOptions()

        assertEquals("undefined", jsTypeOf(options.success))
        assertEquals("undefined", jsTypeOf(options.fail))
        assertEquals("undefined", jsTypeOf(options.complete))

        options.success = { result -> result.needAuthorization; result.privacyContractName }
        assertNotNull(options.success)
    }

    @Test
    fun requirePrivacyAuthorizeOptionsLeaveCallbacksAbsentUntilAssigned() {
        val options = wxRequirePrivacyAuthorizeOptions()

        assertEquals("undefined", jsTypeOf(options.success))
        assertEquals("undefined", jsTypeOf(options.fail))

        options.fail = { result -> result.errMsg.length }
        assertNotNull(options.fail)
    }

    @Test
    fun presenceGuardsAnswerFalseWithoutAWxGlobal() {
        assertFalse(hasWxGetPrivacySetting())
        assertFalse(hasWxRequirePrivacyAuthorize())
    }

    @Test
    fun aRequiredAnswerIsRead() {
        val result = fakeGetPrivacySettingSuccess(needAuthorization = true, privacyContractName = CONTRACT)

        assertEquals(WxPrivacyRequirement.Required(CONTRACT), wxPrivacyRequirement(result))
    }

    @Test
    fun anAnswerThatRequiresNothingIsRead() {
        val result = fakeGetPrivacySettingSuccess(needAuthorization = false, privacyContractName = CONTRACT)

        assertEquals(WxPrivacyRequirement.NotRequired(CONTRACT), wxPrivacyRequirement(result))
    }

    @Test
    fun aMissingContractNameStaysAbsentRatherThanInvented() {
        val result = fakeGetPrivacySettingSuccess(needAuthorization = true, privacyContractName = null)

        assertEquals(WxPrivacyRequirement.Required(null), wxPrivacyRequirement(result))
    }

    @Test
    fun aMissingFlagIsUnreadableRatherThanAssumed() {
        val result = fakeGetPrivacySettingSuccess(needAuthorization = null, privacyContractName = CONTRACT)

        assertEquals(WxPrivacyRequirement.Unreadable, wxPrivacyRequirement(result))
    }

    @Test
    fun aFlagThatIsNotABooleanIsUnreadable() {
        val result = fakeGetPrivacySettingSuccess(needAuthorization = "yes", privacyContractName = CONTRACT)

        assertEquals(WxPrivacyRequirement.Unreadable, wxPrivacyRequirement(result))
    }

    @Test
    fun aContractNameThatIsNotAStringIsUnreadable() {
        val result = fakeGetPrivacySettingSuccess(needAuthorization = true, privacyContractName = 42)

        assertEquals(WxPrivacyRequirement.Unreadable, wxPrivacyRequirement(result))
    }

    private companion object {
        private const val CONTRACT = "《example privacy contract》"
    }
}
