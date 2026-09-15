package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.capability.permission.PermissionKey
import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxRequestFailureResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WechatErrorMapperTest {
    @Test
    fun rawWechatFailureMapsWithoutLeakingExternalObject() {
        val raw: WxGeneralCallbackResult = js("({ errMsg: 'getStorage:fail denied' })")

        val mapped = mapWechatHostFailure(operation = "getStorage", result = raw)

        assertEquals("wechat", mapped.host)
        assertNull(mapped.code)
        assertEquals("getStorage:fail denied", mapped.hostMessage)
        assertEquals("getStorage", mapped.metadata["operation"])
    }

    @Test
    fun requestTimeoutMapsToTimeoutRatherThanHostFailure() {
        val raw: WxRequestFailureResult = js("({ errMsg: 'request:fail timeout' })")

        val mapped = mapWechatRequestFailure(raw)

        val timeout = mapped as MiniAppException.Timeout
        assertEquals("request", timeout.operation)
        assertEquals("request:fail timeout", timeout.hostMessage)
    }

    @Test
    fun requestTransportFailureKeepsErrnoAsScalarDiagnostic() {
        val raw: WxRequestFailureResult =
            js("({ errMsg: 'request:fail unable to resolve host', errno: 600009 })")

        val mapped = mapWechatRequestFailure(raw)

        val failure = mapped as MiniAppException.HostFailure
        assertEquals("wechat", failure.host)
        assertEquals("600009", failure.code)
        assertEquals("request", failure.metadata["operation"])
    }

    @Test
    fun aDeclinedPrivacyContractIsClassifiedAsARefusal() {
        val raw: WxGeneralCallbackResult =
            js("({ errMsg: 'requirePrivacyAuthorize:fail privacy permission is not authorized' })")

        // Only the recognizable refusal message becomes a user outcome.
        assertEquals(WxPrivacyAuthorizeFailure.Refused, mapWechatPrivacyAuthorizeFailure(raw))
    }

    @Test
    fun anUnknownPrivacyAuthorizationFailureRemainsAHostFailure() {
        val raw: WxGeneralCallbackResult =
            js("({ errMsg: 'requirePrivacyAuthorize:fail system error' })")

        val classified = mapWechatPrivacyAuthorizeFailure(raw)

        val failed = classified as WxPrivacyAuthorizeFailure.Failed
        val error = failed.error as MiniAppException.HostFailure
        assertEquals("wechat", error.host)
        assertEquals("requirePrivacyAuthorize", error.metadata["operation"])
    }

    @Test
    fun anUndeclaredPrivacyCollectionIsAHostFailureRatherThanARefusal() {
        val raw: WxGeneralCallbackResult =
            js("({ errMsg: 'requirePrivacyAuthorize:fail api scope is not declared in the privacy agreement' })")

        val classified = mapWechatPrivacyAuthorizeFailure(raw)

        val failed = classified as WxPrivacyAuthorizeFailure.Failed
        val error = failed.error as MiniAppException.HostFailure
        assertEquals("wechat", error.host)
        assertEquals("requirePrivacyAuthorize", error.metadata["operation"])
    }

    @Test
    fun anAuthorizeRefusalMapsToAPermissionDenial() {
        val raw: WxGeneralCallbackResult = js("({ errMsg: 'authorize:fail auth deny' })")

        val denial = mapWechatAuthorizeFailure(PermissionKey.Microphone, raw) as MiniAppException.PermissionDenied

        // The host-neutral key is reported, never the WeChat scope name.
        assertEquals("microphone", denial.permission)
        assertEquals("authorize:fail auth deny", denial.message)
    }

    @Test
    fun anAuthorizeFailureThatIsNotARefusalStaysAHostFailure() {
        val raw: WxGeneralCallbackResult = js("({ errMsg: 'authorize:fail invalid scope' })")

        val failure = mapWechatAuthorizeFailure(PermissionKey.Microphone, raw) as MiniAppException.HostFailure

        assertEquals("wechat", failure.host)
        assertEquals("authorize", failure.metadata["operation"])
    }
}
