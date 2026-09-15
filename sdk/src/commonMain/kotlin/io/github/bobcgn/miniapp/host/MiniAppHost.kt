package io.github.bobcgn.miniapp.host

import io.github.bobcgn.miniapp.capability.CapabilityKey
import io.github.bobcgn.miniapp.capability.CapabilitySupport
import io.github.bobcgn.miniapp.error.MiniAppException

/**
 * Marker for a host-specific API escape hatch.
 *
 * A host may expose APIs that have no honest cross-platform semantic equivalent.
 * Those APIs belong behind an implementation of this marker instead of being
 * forced into a lowest-common-denominator capability.
 */
public interface HostPlatformApi

/**
 * Boundary between platform-neutral SDK code and one mini-app host runtime.
 *
 * Implementations report support for host-neutral capabilities and expose a
 * typed [platform] escape hatch for APIs specific to that host. Adding a host is
 * expected to add an implementation rather than extend a platform enum or a
 * central `when` statement.
 *
 * @param P host-specific platform API type
 */
public interface MiniAppHost<out P : HostPlatformApi> {
    /** Host-specific APIs that intentionally remain outside common capabilities. */
    public val platform: P

    /** Returns whether this host currently provides [capability]. */
    public fun capabilitySupport(capability: CapabilityKey): CapabilitySupport
}

/**
 * Fails unless this host currently provides [capability].
 *
 * A caller that cannot proceed without a capability uses this instead of reading
 * [MiniAppHost.capabilitySupport] and inventing its own failure, so an unavailable
 * host surfaces as [MiniAppException.UnsupportedCapability] rather than as
 * whatever host failure happens to occur later.
 *
 * Every state other than [CapabilitySupport.Supported] fails, including
 * [CapabilitySupport.VersionDependent] and [CapabilitySupport.PermissionDependent]:
 * those describe what this host would need in order to help, which is not a
 * capability the caller can use. Call [MiniAppHost.capabilitySupport] directly
 * when the reason matters, because the thrown exception carries only the key.
 *
 * @throws MiniAppException.UnsupportedCapability when [capability] is not supported
 */
public fun MiniAppHost<*>.requireSupported(capability: CapabilityKey): Unit {
    if (capabilitySupport(capability) != CapabilitySupport.Supported) {
        throw MiniAppException.UnsupportedCapability(capability)
    }
}
