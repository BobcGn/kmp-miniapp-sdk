package io.github.bobcgn.miniapp.error

import io.github.bobcgn.miniapp.capability.CapabilityKey

/**
 * Platform-neutral failure reported by the MiniApp SDK.
 *
 * Host adapters translate raw runtime failures into this semantic model. Raw
 * JavaScript values and host-specific result objects must not cross this boundary.
 */
public sealed class MiniAppException protected constructor(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {
    /** The requested semantic capability is unavailable on the active host. */
    public class UnsupportedCapability public constructor(
        public val capability: CapabilityKey,
    ) : MiniAppException("Unsupported capability: ${capability.value}")

    /** The host or user denied a permission required by the operation. */
    public class PermissionDenied public constructor(
        public val permission: String?,
        message: String = "Permission denied",
        cause: Throwable? = null,
    ) : MiniAppException(message, cause)

    /** The user intentionally cancelled or dismissed the operation. */
    public class UserCancelled public constructor(
        message: String = "Operation cancelled by user",
        cause: Throwable? = null,
    ) : MiniAppException(message, cause)

    /**
     * A host operation did not complete within the time the host allows.
     *
     * This is distinct from [HostFailure] because the operation itself is not
     * necessarily defective: the host stopped waiting for it. An adapter reports
     * this only when the host actually signals a timeout.
     */
    public class Timeout public constructor(
        public val operation: String,
        public val hostMessage: String,
        cause: Throwable? = null,
    ) : MiniAppException("$operation timed out: $hostMessage", cause)

    /**
     * A host operation failed without a more specific SDK-level interpretation.
     *
     * [metadata] contains only adapter-sanitized, platform-neutral values. It must
     * never contain raw JavaScript objects.
     */
    public class HostFailure public constructor(
        public val host: String,
        public val code: String?,
        public val hostMessage: String,
        public val metadata: Map<String, String> = emptyMap(),
        cause: Throwable? = null,
    ) : MiniAppException("$host host failure: $hostMessage", cause)

    /** The host returned data that cannot satisfy the SDK contract. */
    public class InvalidResponse public constructor(
        message: String,
        cause: Throwable? = null,
    ) : MiniAppException(message, cause)

    /**
     * The host requires the user to accept its privacy contract first.
     *
     * This is not a permission denial. A privacy contract is the host's own
     * condition for the personal data a mini program collects and is tracked
     * separately from system permissions, so collapsing the two would tell a
     * consumer to ask for the wrong thing.
     */
    public class PrivacyAuthorizationRequired public constructor(
        public val contractName: String? = null,
    ) : MiniAppException(
        contractName?.let { "Privacy authorization required: $it" }
            ?: "Privacy authorization required",
    )

    /** An unexpected SDK or adapter failure occurred. */
    public class InternalFailure public constructor(
        message: String,
        cause: Throwable? = null,
    ) : MiniAppException(message, cause)
}
