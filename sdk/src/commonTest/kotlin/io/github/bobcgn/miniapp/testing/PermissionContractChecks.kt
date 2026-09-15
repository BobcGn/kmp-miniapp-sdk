package io.github.bobcgn.miniapp.testing

import io.github.bobcgn.miniapp.capability.permission.MiniAppPermissions
import io.github.bobcgn.miniapp.capability.permission.PermissionKey
import io.github.bobcgn.miniapp.capability.permission.PermissionState
import io.github.bobcgn.miniapp.error.MiniAppException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * The permission lifecycle contract, expressed once for every host.
 *
 * What the host answers depends on the user, so each check assumes the caller has
 * already arranged the host's decision and states only what must hold afterwards.
 * The WeChat adapter runs the same checks in `WechatPermissionContractTest`.
 */
internal object PermissionContractChecks {
    /**
     * A permission the host holds no decision for is not requested yet. Reporting
     * it as refused would be a lie the caller could not recover from.
     */
    suspend fun anUndecidedPermissionIsNotRequested(
        permissions: MiniAppPermissions,
        permission: PermissionKey,
    ) {
        assertEquals(PermissionState.NotRequested, permissions.stateOf(permission))
    }

    /** A decision the host holds is reported unchanged. */
    suspend fun aGrantedPermissionReadsAsGranted(
        permissions: MiniAppPermissions,
        permission: PermissionKey,
    ) {
        assertEquals(PermissionState.Granted, permissions.stateOf(permission))
    }

    suspend fun aRefusedPermissionReadsAsDenied(
        permissions: MiniAppPermissions,
        permission: PermissionKey,
    ) {
        assertEquals(PermissionState.Denied, permissions.stateOf(permission))
    }

    /**
     * Requesting a permission the host has already refused fails as a permission
     * denial, which is a different outcome from the host failing to run.
     */
    suspend fun requestingARefusedPermissionIsAPermissionDenial(
        permissions: MiniAppPermissions,
        permission: PermissionKey,
    ) {
        val denial = assertFailsWith<MiniAppException.PermissionDenied> {
            permissions.request(permission)
        }

        assertEquals(permission.value, denial.permission)
    }

    /** A request that the host grants reports the granted state. */
    suspend fun aGrantedRequestReportsGranted(
        permissions: MiniAppPermissions,
        permission: PermissionKey,
    ) {
        assertEquals(PermissionState.Granted, permissions.request(permission))
    }

    /**
     * The state after the settings page closes is the host's decision, never an
     * assumption that visiting settings granted anything.
     */
    suspend fun openSettingsReportsTheHostsDecision(
        permissions: MiniAppPermissions,
        permission: PermissionKey,
        expected: PermissionState,
    ) {
        assertEquals(expected, permissions.openSettings(permission))
        assertEquals(expected, permissions.stateOf(permission))
    }
}
