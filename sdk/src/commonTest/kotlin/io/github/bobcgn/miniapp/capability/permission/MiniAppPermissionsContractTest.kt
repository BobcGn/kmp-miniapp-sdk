package io.github.bobcgn.miniapp.capability.permission

import io.github.bobcgn.miniapp.capability.CapabilityKey
import io.github.bobcgn.miniapp.testing.FakeMiniAppPermissions
import io.github.bobcgn.miniapp.testing.PermissionContractChecks
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Runs the shared permission contract against the host-neutral reference
 * implementation, plus the model guards the contract itself must satisfy.
 *
 * The WeChat adapter runs the same checks in `WechatPermissionContractTest`.
 */
internal class MiniAppPermissionsContractTest {
    @Test
    fun aPermissionKeyIdentifiesMeaningRatherThanAHostName() {
        assertEquals("microphone", PermissionKey.Microphone.value)
        assertEquals(PermissionKey("microphone"), PermissionKey.Microphone)
        assertNotEquals(PermissionKey("microphone"), PermissionKey("camera"))
    }

    @Test
    fun capabilityKeyIsHostNeutral() {
        assertEquals(CapabilityKey("permission"), MiniAppPermissions.Key)
    }

    @Test
    fun anUndecidedPermissionIsNotRequested() = runTest {
        PermissionContractChecks.anUndecidedPermissionIsNotRequested(
            permissions = FakeMiniAppPermissions(),
            permission = PermissionKey.Microphone,
        )
    }

    @Test
    fun aGrantedPermissionReadsAsGranted() = runTest {
        PermissionContractChecks.aGrantedPermissionReadsAsGranted(
            permissions = granting(),
            permission = PermissionKey.Microphone,
        )
    }

    @Test
    fun aRefusedPermissionReadsAsDenied() = runTest {
        PermissionContractChecks.aRefusedPermissionReadsAsDenied(
            permissions = refusing(),
            permission = PermissionKey.Microphone,
        )
    }

    @Test
    fun requestingARefusedPermissionIsAPermissionDenial() = runTest {
        PermissionContractChecks.requestingARefusedPermissionIsAPermissionDenial(
            permissions = refusing(),
            permission = PermissionKey.Microphone,
        )
    }

    @Test
    fun aGrantedRequestReportsGranted() = runTest {
        PermissionContractChecks.aGrantedRequestReportsGranted(
            permissions = FakeMiniAppPermissions(),
            permission = PermissionKey.Microphone,
        )
    }

    @Test
    fun anAlreadyGrantedPermissionIsNotSentToTheHostAgain() = runTest {
        val permissions = granting()

        val state = permissions.request(PermissionKey.Microphone)

        assertEquals(PermissionState.Granted, state)
        assertEquals(0, permissions.hostRequests)
    }

    @Test
    fun openSettingsReportsAGrantedDecision() = runTest {
        PermissionContractChecks.openSettingsReportsTheHostsDecision(
            permissions = FakeMiniAppPermissions().apply { nextDecision = PermissionState.Granted },
            permission = PermissionKey.Microphone,
            expected = PermissionState.Granted,
        )
    }

    @Test
    fun openSettingsReportsARefusalRatherThanAssumingSuccess() = runTest {
        PermissionContractChecks.openSettingsReportsTheHostsDecision(
            permissions = FakeMiniAppPermissions().apply { nextDecision = PermissionState.Denied },
            permission = PermissionKey.Microphone,
            expected = PermissionState.Denied,
        )
    }

    private fun granting(): FakeMiniAppPermissions =
        FakeMiniAppPermissions().apply { decide(PermissionKey.Microphone, PermissionState.Granted) }

    private fun refusing(): FakeMiniAppPermissions =
        FakeMiniAppPermissions().apply { decide(PermissionKey.Microphone, PermissionState.Denied) }
}
