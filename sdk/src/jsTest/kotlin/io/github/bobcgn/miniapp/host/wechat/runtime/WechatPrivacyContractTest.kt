package io.github.bobcgn.miniapp.host.wechat.runtime

import io.github.bobcgn.miniapp.capability.permission.PermissionKey
import io.github.bobcgn.miniapp.capability.privacy.PrivacyAuthorizationOutcome
import io.github.bobcgn.miniapp.capability.privacy.PrivacyAuthorizationRequirement
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatPermissionHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatPrivacyHost
import io.github.bobcgn.miniapp.host.wechat.testing.fakeWechatHost
import io.github.bobcgn.miniapp.host.wechat.testing.fakeWxAuthSetting
import io.github.bobcgn.miniapp.testing.PrivacyContractChecks
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Runs the shared privacy contract against the WeChat host, and checks that
 * WeChat's privacy contract and its system permissions stay apart.
 *
 * These are the same checks `MiniAppPrivacyContractTest` runs against the
 * host-neutral reference implementation. Passing here means the real `WechatHost`
 * honours the contract through its callback port; it is not real-host evidence,
 * because privacy state belongs to the host's backend configuration and the user.
 */
internal class WechatPrivacyContractTest {
    @Test
    fun aRequirementTheHostReportsIsReturnedUnchanged() = runTest {
        PrivacyContractChecks.theHostsRequirementIsReportedUnchanged(
            privacy = hostRequiring().privacy,
            expected = PrivacyAuthorizationRequirement.REQUIRED,
            expectedContractName = CONTRACT,
        )
    }

    @Test
    fun thePreconditionPassesWhenNothingIsRequired() = runTest {
        PrivacyContractChecks.thePreconditionPassesWhenNothingIsRequired(
            fakeWechatHost(privacyHost = FakeWechatPrivacyHost(needAuthorization = false)).privacy,
        )
    }

    @Test
    fun thePreconditionFailsWhenAuthorizationIsRequired() = runTest {
        PrivacyContractChecks.thePreconditionFailsWhenAuthorizationIsRequired(hostRequiring().privacy)
    }

    @Test
    fun aRefusalIsAnOutcomeRatherThanAnError() = runTest {
        PrivacyContractChecks.aRefusalIsAnOutcomeRatherThanAnError(
            privacy = fakeWechatHost(
                privacyHost = FakeWechatPrivacyHost().apply {
                    authorizeFailure = "requirePrivacyAuthorize:fail privacy permission is not authorized"
                },
            ).privacy,
            expected = PrivacyAuthorizationOutcome.Refused,
        )
    }

    @Test
    fun acceptingTheContractClearsTheRequirement() = runTest {
        PrivacyContractChecks.acceptingTheContractClearsTheRequirement(hostRequiring().privacy)
    }

    @Test
    fun aRefusalLeavesTheRequirementInPlace() = runTest {
        PrivacyContractChecks.aRefusalLeavesTheRequirementInPlace(
            fakeWechatHost(
                privacyHost = FakeWechatPrivacyHost(needAuthorization = true).apply {
                    authorizeFailure = "requirePrivacyAuthorize:fail privacy permission is not authorized"
                },
            ).privacy,
        )
    }

    @Test
    fun aPrivacyOperationDoesNotTouchThePermissionHost() = runTest {
        val permissionHost = FakeWechatPermissionHost()
        val host = fakeWechatHost(
            permissionHost = permissionHost,
            privacyHost = FakeWechatPrivacyHost(needAuthorization = true),
        )

        host.privacy.requestAuthorization()

        // Two separate host APIs, and one must not stand in for the other.
        assertEquals(0, permissionHost.authorizeCalls)
        assertEquals(0, permissionHost.getSettingCalls)
        assertEquals(0, permissionHost.openSettingCalls)
    }

    @Test
    fun aPermissionOperationIsNotAPrivacyAuthorization() = runTest {
        val privacyHost = FakeWechatPrivacyHost(needAuthorization = true)
        val host = fakeWechatHost(
            permissionHost = FakeWechatPermissionHost(authSetting = fakeWxAuthSetting("scope.record" to true)),
            privacyHost = privacyHost,
        )

        host.permissions.request(PermissionKey.Microphone)

        assertEquals(0, privacyHost.authorizeCalls)
        assertEquals(0, privacyHost.getPrivacySettingCalls)
    }

    private fun hostRequiring() = fakeWechatHost(
        privacyHost = FakeWechatPrivacyHost(needAuthorization = true, privacyContractName = CONTRACT),
    )

    private companion object {
        private const val CONTRACT = "《example privacy contract》"
    }
}
