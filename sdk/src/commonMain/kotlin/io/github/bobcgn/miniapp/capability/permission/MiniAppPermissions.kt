package io.github.bobcgn.miniapp.capability.permission

import io.github.bobcgn.miniapp.capability.CapabilityKey

/**
 * What the host currently knows about one permission.
 *
 * These are the host's answers, not SDK policy: the SDK never decides that a
 * permission is granted, it only reports what the host said. [NotRequested] and
 * [Denied] are separate because only one of them can still be resolved by asking
 * again — a host that has recorded a refusal will not prompt a second time.
 */
public sealed interface PermissionState {
    /** The host holds no decision for this permission yet. */
    public data object NotRequested : PermissionState

    /** The host reports the permission as granted. */
    public data object Granted : PermissionState

    /** The host reports the permission as refused. */
    public data object Denied : PermissionState
}

/**
 * Platform-neutral permission lifecycle.
 *
 * A permission is not SDK state. The user can change it in the host's own
 * settings at any time, so every call asks the host and no answer is kept as a
 * lasting fact. Both [request] and [openSettings] need a real user gesture in a
 * real host, and the SDK never prompts on its own — not on startup, and not as a
 * side effect of another call.
 *
 * A refusal is [io.github.bobcgn.miniapp.error.MiniAppException.PermissionDenied],
 * which is deliberately not a host failure: the host worked correctly and the
 * answer was no.
 */
public interface MiniAppPermissions {
    /**
     * Returns the host's current state for [permission].
     *
     * @throws io.github.bobcgn.miniapp.error.MiniAppException.HostFailure when the
     *   host cannot answer
     * @throws io.github.bobcgn.miniapp.error.MiniAppException.InvalidResponse when
     *   the host answers with something the contract cannot represent
     */
    public suspend fun stateOf(permission: PermissionKey): PermissionState

    /**
     * Asks the host for [permission] and returns the state afterwards.
     *
     * A user gesture is required: the host will refuse to prompt otherwise.
     *
     * @throws io.github.bobcgn.miniapp.error.MiniAppException.PermissionDenied when
     *   the host refuses the permission
     */
    public suspend fun request(permission: PermissionKey): PermissionState

    /**
     * Opens the host's permission settings and returns the state afterwards.
     *
     * Must be triggered by a user gesture, because it leaves the mini program.
     * A successful return means the settings page closed; it never means the
     * permission was granted, so the state is read from the host afterwards.
     */
    public suspend fun openSettings(permission: PermissionKey): PermissionState

    public companion object {
        /** Stable identity used for host capability-support queries. */
        public val Key: CapabilityKey = CapabilityKey("permission")
    }
}

/** Host facet that provides the common [MiniAppPermissions] capability. */
public interface PermissionCapabilityProvider {
    /** Permission implementation supplied by the active host. */
    public val permissions: MiniAppPermissions
}
