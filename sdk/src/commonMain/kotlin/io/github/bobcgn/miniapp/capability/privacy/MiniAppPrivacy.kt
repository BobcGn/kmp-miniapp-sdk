package io.github.bobcgn.miniapp.capability.privacy

import io.github.bobcgn.miniapp.capability.CapabilityKey

/**
 * Whether the host currently requires the user to accept its privacy contract.
 *
 * This states the host's requirement, not a consent the SDK can vouch for.
 * [NOT_REQUIRED] deliberately does not mean "the user agreed": WeChat also reports
 * it when the mini program declares no privacy collection at all, so treating it
 * as proof of consent would be a claim the SDK cannot support.
 */
public enum class PrivacyAuthorizationRequirement {
    /** The host currently requires the user to accept its privacy contract. */
    REQUIRED,

    /** The host reports that no privacy authorization is needed right now. */
    NOT_REQUIRED,
}

/**
 * What the host reports about its privacy contract.
 *
 * @property requirement whether the host currently requires authorization
 * @property contractName the host's own name for its privacy contract, or `null`
 *   when the host reports none. The SDK neither invents nor reformats this value;
 *   a host without such a concept simply reports nothing.
 */
public class PrivacyStatus(
    public val requirement: PrivacyAuthorizationRequirement,
    public val contractName: String?,
)

/**
 * The result of one authorization attempt, as the host reported it.
 *
 * A refusal is an outcome rather than an error: the host did exactly what it is
 * supposed to do when the user declines. [Refused] covers a recognizable
 * decline or dismissal reported by the host; an unrecognized failure remains an
 * error, because the SDK must not invent a user decision.
 */
public sealed interface PrivacyAuthorizationOutcome {
    /** The user accepted the host's privacy contract. */
    public data object Authorized : PrivacyAuthorizationOutcome

    /** The user did not accept the host's privacy contract. */
    public data object Refused : PrivacyAuthorizationOutcome
}

/**
 * Platform-neutral privacy authorization, deliberately separate from permissions.
 *
 * A system permission is granted to the application by the user through the
 * operating system. A privacy contract is the host's own condition for the
 * personal data a mini program collects. The two are prompted, stored, and
 * reported separately by the host, so they are modelled separately here; sharing
 * one model would misreport one as the other.
 *
 * Nothing is cached: the host's requirement changes when the user answers its
 * prompt and when the mini program's declared collection changes, so every call
 * asks the host.
 *
 * Nothing here shows any interface. Only the consumer can present a prompt, and
 * only from a user gesture; the SDK never prompts on its own, neither at startup
 * nor as a side effect of another call.
 */
public interface MiniAppPrivacy {
    /**
     * Returns what the host currently requires for its privacy contract.
     *
     * @throws io.github.bobcgn.miniapp.error.MiniAppException.UnsupportedCapability
     *   when the host has no privacy authorization API
     * @throws io.github.bobcgn.miniapp.error.MiniAppException.HostFailure when the
     *   host cannot answer
     * @throws io.github.bobcgn.miniapp.error.MiniAppException.InvalidResponse when
     *   the host answers with something the contract cannot represent
     */
    public suspend fun status(): PrivacyStatus

    /**
     * Asks the host to obtain the user's acceptance of its privacy contract.
     *
     * The host presents its own prompt, so a user gesture is required. The returned
     * outcome is what the host reported; a refusal is an outcome, so only a
     * host-level failure raises.
     *
     * @throws io.github.bobcgn.miniapp.error.MiniAppException.UnsupportedCapability
     *   when the host has no privacy authorization API
     * @throws io.github.bobcgn.miniapp.error.MiniAppException.HostFailure when the
     *   request cannot be made for a reason that is not the user's answer
     */
    public suspend fun requestAuthorization(): PrivacyAuthorizationOutcome

    /**
     * The single privacy precondition point for capabilities the host gates behind
     * its privacy contract.
     *
     * It queries the host and returns once the host requires nothing further. It
     * shows no interface and starts no prompt, so a capability that needs the user
     * to accept the contract lets the refusal surface and leaves the consumer to
     * ask again from a gesture.
     *
     * @throws io.github.bobcgn.miniapp.error.MiniAppException.PrivacyAuthorizationRequired
     *   when the host currently requires the user to accept its privacy contract
     * @throws io.github.bobcgn.miniapp.error.MiniAppException.UnsupportedCapability
     *   when the host has no privacy authorization API
     */
    public suspend fun requireSatisfied(): Unit

    public companion object {
        /** Stable identity used for host capability-support queries. */
        public val Key: CapabilityKey = CapabilityKey("privacy")
    }
}

/** Host facet that provides the common [MiniAppPrivacy] capability. */
public interface PrivacyCapabilityProvider {
    /** Privacy implementation supplied by the active host. */
    public val privacy: MiniAppPrivacy
}
