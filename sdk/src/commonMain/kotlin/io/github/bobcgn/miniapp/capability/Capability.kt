package io.github.bobcgn.miniapp.capability

/**
 * Stable identity of a semantic capability exposed by a mini-app host.
 *
 * A key identifies an SDK-level capability such as storage; it must not name a
 * raw method belonging to a particular host runtime.
 *
 * @property value stable, host-neutral identifier
 */
public data class CapabilityKey(public val value: String)

/** Describes whether a host can currently provide a capability. */
public sealed interface CapabilitySupport {
    /** The host can provide the requested capability. */
    public data object Supported : CapabilitySupport

    /** The host cannot provide the requested capability. */
    public data object Unsupported : CapabilitySupport
}
