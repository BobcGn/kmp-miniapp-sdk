package io.github.bobcgn.miniapp.host

import io.github.bobcgn.miniapp.capability.CapabilityKey
import io.github.bobcgn.miniapp.capability.CapabilitySupport

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
