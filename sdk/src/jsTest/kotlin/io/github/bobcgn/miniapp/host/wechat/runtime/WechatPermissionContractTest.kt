package io.github.bobcgn.miniapp.host.wechat.runtime

import io.github.bobcgn.miniapp.capability.permission.PermissionKey
import io.github.bobcgn.miniapp.capability.permission.PermissionState
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatPermissionHost
import io.github.bobcgn.miniapp.host.wechat.testing.fakeWechatHost
import io.github.bobcgn.miniapp.host.wechat.testing.fakeWxAuthSetting
import io.github.bobcgn.miniapp.testing.PermissionContractChecks
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

/**
 * Runs the shared permission contract against the WeChat host.
 *
 * These are the same checks `MiniAppPermissionsContractTest` runs against the
 * host-neutral reference implementation. Passing here means the real `WechatHost`
 * honours the lifecycle through its callback port; it is not real-host evidence,
 * because permission state belongs to the user and only a real run can settle it.
 */
internal class WechatPermissionContractTest {
    @Test
    fun anUndecidedPermissionIsNotRequested() = runTest {
        PermissionContractChecks.anUndecidedPermissionIsNotRequested(
            permissions = fakeWechatHost(permissionHost = FakeWechatPermissionHost()).permissions,
            permission = MICROPHONE,
        )
    }

    @Test
    fun aGrantedPermissionReadsAsGranted() = runTest {
        PermissionContractChecks.aGrantedPermissionReadsAsGranted(
            permissions = hostReporting(true).permissions,
            permission = MICROPHONE,
        )
    }

    @Test
    fun aRefusedPermissionReadsAsDenied() = runTest {
        PermissionContractChecks.aRefusedPermissionReadsAsDenied(
            permissions = hostReporting(false).permissions,
            permission = MICROPHONE,
        )
    }

    @Test
    fun requestingARefusedPermissionIsAPermissionDenial() = runTest {
        PermissionContractChecks.requestingARefusedPermissionIsAPermissionDenial(
            permissions = hostReporting(false).permissions,
            permission = MICROPHONE,
        )
    }

    @Test
    fun aGrantedRequestReportsGranted() = runTest {
        PermissionContractChecks.aGrantedRequestReportsGranted(
            permissions = fakeWechatHost(permissionHost = FakeWechatPermissionHost()).permissions,
            permission = MICROPHONE,
        )
    }

    @Test
    fun openSettingsReportsTheHostsGrantRatherThanAssumingOne() = runTest {
        PermissionContractChecks.openSettingsReportsTheHostsDecision(
            permissions = hostReporting(true).permissions,
            permission = MICROPHONE,
            expected = PermissionState.Granted,
        )
    }

    @Test
    fun openSettingsReportsTheHostsRefusalRatherThanAssumingSuccess() = runTest {
        PermissionContractChecks.openSettingsReportsTheHostsDecision(
            permissions = hostReporting(false).permissions,
            permission = MICROPHONE,
            expected = PermissionState.Denied,
        )
    }

    private fun hostReporting(granted: Boolean) = fakeWechatHost(
        permissionHost = FakeWechatPermissionHost(
            authSetting = fakeWxAuthSetting("scope.record" to granted),
        ),
    )

    private companion object {
        private val MICROPHONE = PermissionKey.Microphone
    }
}
