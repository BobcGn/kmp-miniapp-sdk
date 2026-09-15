package io.github.bobcgn.miniapp.testing

import io.github.bobcgn.miniapp.capability.permission.MiniAppPermissions
import io.github.bobcgn.miniapp.capability.permission.PermissionKey
import io.github.bobcgn.miniapp.capability.permission.PermissionState
import io.github.bobcgn.miniapp.error.MiniAppException

/**
 * Host-neutral [MiniAppPermissions] used to express the permission contract.
 *
 * It holds the decision a host would report and applies the same rules a host
 * does: an already granted permission is not prompted for again, and an already
 * refused one fails a request instead of pretending a second prompt happened.
 *
 * @param states decisions this host already holds, keyed by permission
 */
internal class FakeMiniAppPermissions(
    private val states: MutableMap<PermissionKey, PermissionState> = mutableMapOf(),
) : MiniAppPermissions {
    /** The decision the host reaches the next time it is asked or its settings are visited. */
    var nextDecision: PermissionState = PermissionState.Granted

    /** How many requests reached the host rather than being answered from a decision. */
    var hostRequests: Int = 0
        private set

    /** How many times the host settings page was opened. */
    var settingsVisits: Int = 0
        private set

    /** Places a decision in this host, as if the user had made it earlier. */
    fun decide(permission: PermissionKey, state: PermissionState) {
        states[permission] = state
    }

    override suspend fun stateOf(permission: PermissionKey): PermissionState =
        states[permission] ?: PermissionState.NotRequested

    override suspend fun request(permission: PermissionKey): PermissionState {
        val current = states[permission] ?: PermissionState.NotRequested
        if (current == PermissionState.Granted) return current
        if (current == PermissionState.Denied) {
            throw MiniAppException.PermissionDenied(permission = permission.value)
        }

        hostRequests += 1
        return nextDecision.also { states[permission] = it }
    }

    override suspend fun openSettings(permission: PermissionKey): PermissionState {
        settingsVisits += 1
        // Visiting settings can change the decision, but it never guarantees one.
        return nextDecision.also { states[permission] = it }
    }
}
