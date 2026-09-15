package io.github.bobcgn.miniapp.capability.network

import io.github.bobcgn.miniapp.capability.CapabilityKey

/**
 * HTTP methods the SDK transport contract can express.
 *
 * The set is deliberately closed. Extending it is an SDK contract change, and a
 * host adapter maps each entry to the method string its runtime expects.
 */
public enum class HttpMethod {
    GET,
    POST,
    PUT,
    PATCH,
    DELETE,
    HEAD,
}

/**
 * Platform-neutral description of one HTTP exchange to perform.
 *
 * The contract is intentionally minimal: no serialization, no caching, no retry
 * policy, and no host-specific options. [body] is already-encoded text because
 * the SDK transports bytes of a consumer's choosing rather than interpreting a
 * payload format.
 *
 * @property url absolute request target
 * @property method HTTP method to use
 * @property headers request headers to send
 * @property body already-encoded request body, or `null` for no body
 * @property timeoutMillis host timeout in milliseconds, or `null` to apply the
 *   host default
 */
public class MiniAppHttpRequest(
    public val url: String,
    public val method: HttpMethod = HttpMethod.GET,
    public val headers: Map<String, String> = emptyMap(),
    public val body: String? = null,
    public val timeoutMillis: Int? = null,
)

/**
 * Platform-neutral result of a completed HTTP exchange.
 *
 * A completed exchange is a response even when [statusCode] reports an HTTP
 * error such as `404` or `500`. Status codes describe the exchange; only a
 * transport-level failure, such as an unreachable host or an expired timeout,
 * becomes a `MiniAppException`.
 *
 * @property statusCode HTTP status code reported by the host
 * @property headers response headers the host exposed as single string values
 * @property body decoded response body
 */
public class MiniAppHttpResponse(
    public val statusCode: Int,
    public val headers: Map<String, String>,
    public val body: String,
)

/**
 * Platform-neutral HTTP transport capability.
 *
 * The contract carries text only and defines no payload encoding, cookies,
 * redirect policy, or streaming. Consumers that need a structured body encode
 * and decode it themselves.
 */
public interface MiniAppHttpTransport {
    /**
     * Performs [request] and suspends until the exchange completes.
     *
     * @throws io.github.bobcgn.miniapp.error.MiniAppException.Timeout when the
     *   host aborts the exchange after the timeout window elapses
     * @throws io.github.bobcgn.miniapp.error.MiniAppException.HostFailure when
     *   the host cannot complete the exchange for any other reason
     * @throws io.github.bobcgn.miniapp.error.MiniAppException.InvalidResponse
     *   when the host completes the exchange with a body the contract cannot carry
     */
    public suspend fun request(request: MiniAppHttpRequest): MiniAppHttpResponse

    public companion object {
        /** Stable identity used for host capability-support queries. */
        public val Key: CapabilityKey = CapabilityKey("network")
    }
}

/** Host facet that provides the common [MiniAppHttpTransport] capability. */
public interface NetworkCapabilityProvider {
    /** HTTP transport implementation supplied by the active host. */
    public val network: MiniAppHttpTransport
}
