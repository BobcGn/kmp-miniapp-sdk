package io.github.bobcgn.miniapp.host.wechat.runtime

import io.github.bobcgn.miniapp.capability.CapabilityKey
import io.github.bobcgn.miniapp.capability.permission.MiniAppPermissions
import io.github.bobcgn.miniapp.capability.privacy.MiniAppPrivacy
import io.github.bobcgn.miniapp.host.wechat.WeChatSessionState
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatAuthHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatPermissionHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatPrivacyHost
import io.github.bobcgn.miniapp.host.wechat.testing.fakeWechatHost
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * What the session check must hold for the SDK as a whole, driven through the
 * real `WechatHost`.
 *
 * These are not real-host facts: a genuine session check needs WeChat. They state
 * the boundary around it — that the two results are distinguishable, and that the
 * result is not allowed to become anything a consumer could mistake for identity,
 * a backend session, or a credential.
 */
internal class WechatSessionCheckContractTest {
    @Test
    fun validAndInvalidAreDistinguishable() = runTest {
        val valid = fakeWechatHost(authHost = FakeWechatAuthHost()).platform.auth.checkSession()
        val invalid = fakeWechatHost(
            authHost = FakeWechatAuthHost().apply {
                sessionCheckFailure = "checkSession:fail session time out, need relogin"
            },
        ).platform.auth.checkSession()

        assertEquals(WeChatSessionState.VALID, valid)
        assertEquals(WeChatSessionState.INVALID, invalid)
        assertNotEquals(valid, invalid)
    }

    @Test
    fun theResultIsWeChatSpecificRatherThanAGenericAuthenticationConcept() {
        // A namespaced key, so no host-neutral authentication capability is
        // implied by the model.
        assertEquals(CapabilityKey("wechat.check-session"), WeChatSessionState.Key)
        assertEquals("wechat.check-session", WeChatSessionState.Key.value)
    }

    @Test
    fun checkingTheSessionDoesNotAcquireOrKeepACode() = runTest {
        val authHost = FakeWechatAuthHost()

        fakeWechatHost(authHost = authHost).platform.auth.checkSession()

        // No login call, so no code was obtained, and nothing is retained.
        assertEquals(0, authHost.calls)
    }

    @Test
    fun checkingTheSessionDoesNotReachAnyOtherCapability() = runTest {
        val authHost = FakeWechatAuthHost()
        val permissionHost = FakeWechatPermissionHost()
        val privacyHost = FakeWechatPrivacyHost()
        val host = fakeWechatHost(
            authHost = authHost,
            permissionHost = permissionHost,
            privacyHost = privacyHost,
        )

        host.platform.auth.checkSession()

        // Session checking is part of the auth bootstrap chain only; it must not
        // start a permission request or a privacy flow.
        assertEquals(1, authHost.sessionCheckCalls)
        assertEquals(0, permissionHost.getSettingCalls)
        assertEquals(0, permissionHost.authorizeCalls)
        assertEquals(0, privacyHost.getPrivacySettingCalls)
        assertEquals(0, privacyHost.authorizeCalls)
    }

    @Test
    fun theFailureCallbackMeansAnInvalidSessionRegardlessOfMessageText() = runTest {
        val host = fakeWechatHost(
            authHost = FakeWechatAuthHost().apply {
                sessionCheckFailure = "checkSession:fail cgi response is empty"
            },
        )

        assertEquals(WeChatSessionState.INVALID, host.platform.auth.checkSession())
    }

    @Test
    fun theSessionCheckIsNotAPrivacyOrPermissionCapability() {
        // Three separate keys: none of the three can be mistaken for another.
        assertNotEquals(WeChatSessionState.Key, MiniAppPrivacy.Key)
        assertNotEquals(WeChatSessionState.Key, MiniAppPermissions.Key)
        assertNotEquals(MiniAppPrivacy.Key, MiniAppPermissions.Key)
    }
}
