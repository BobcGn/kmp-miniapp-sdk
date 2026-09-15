package io.github.bobcgn.miniapp.capability

import io.github.bobcgn.miniapp.host.HostVersion

/**
 * Stable identity of a semantic capability exposed by a mini-app host.
 *
 * A key identifies an SDK-level capability such as storage; it must not name a
 * raw method belonging to a particular host runtime.
 *
 * @property value stable, host-neutral identifier
 */
public data class CapabilityKey(public val value: String)

/**
 * Describes whether a host can currently provide a capability.
 *
 * A host answers this by inspecting its own runtime, so the answer can differ
 * between two hosts running the same SDK build. The states distinguish a
 * capability the host will never provide from one it cannot provide *here*,
 * because only the second is something a consumer can act on.
 */
public sealed interface CapabilitySupport {
    /** The host can provide the requested capability now. */
    public data object Supported : CapabilitySupport

    /** The host cannot provide the requested capability and no action would change that. */
    public data object Unsupported : CapabilitySupport

    /**
     * The host provides the capability only from a later runtime version.
     *
     * This is a statement about the host, not about the SDK: the same SDK build
     * reports [Supported] on a new enough runtime. A consumer that requires the
     * capability must treat this as unavailable.
     *
     * @property requiredVersion the oldest host version that provides the capability
     * @property currentVersion the version the host reported, or `null` when it
     *   could not be determined
     */
    public data class VersionDependent(
        public val requiredVersion: HostVersion,
        public val currentVersion: HostVersion?,
    ) : CapabilitySupport

    /**
     * The host provides the capability only once a permission is obtained.
     *
     * This states a dependency, not a granted state: the permission may already
     * have been granted by the time it is reported. Resolving that is the job of
     * the permission lifecycle capability, which reports this state once it can
     * tell the difference.
     *
     * @property permission the permission the capability depends on
     */
    public data class PermissionDependent(
        public val permission: String,
    ) : CapabilitySupport
}
