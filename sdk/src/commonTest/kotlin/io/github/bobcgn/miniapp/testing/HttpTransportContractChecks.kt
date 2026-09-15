package io.github.bobcgn.miniapp.testing

import io.github.bobcgn.miniapp.capability.network.MiniAppHttpRequest
import io.github.bobcgn.miniapp.capability.network.MiniAppHttpResponse
import io.github.bobcgn.miniapp.capability.network.MiniAppHttpTransport
import kotlin.test.assertEquals

/**
 * The [MiniAppHttpTransport] contract, expressed once for every implementation.
 *
 * The shared part of the contract is thin by design. Which callback a host
 * reports, how its failure vocabulary maps onto SDK errors, and whether an
 * exchange can be aborted are host-specific, so each adapter states and tests
 * those through its own callback port. Only guarantees that hold for every
 * implementation belong here; add them here rather than in one adapter's suite.
 */
internal object HttpTransportContractChecks {
    /** Asserts that the exchange result crosses the contract without being rewritten. */
    suspend fun exchangeResultIsReportedUnchanged(
        transport: MiniAppHttpTransport,
        request: MiniAppHttpRequest,
        expected: MiniAppHttpResponse,
    ) {
        val actual = transport.request(request)

        assertEquals(expected.statusCode, actual.statusCode)
        assertEquals(expected.headers, actual.headers)
        assertEquals(expected.body, actual.body)
    }

    /** Asserts that the request reaches the implementation without being rewritten. */
    suspend fun requestIsHandedOverUnchanged(
        transport: RecordingHttpTransport,
        request: MiniAppHttpRequest,
    ) {
        transport.request(request)

        assertEquals(listOf(request), transport.requests)
    }

    /**
     * Asserts that an HTTP error status is an outcome of the exchange rather than
     * a failure to perform it.
     */
    suspend fun httpErrorStatusIsAnOutcome(
        transport: MiniAppHttpTransport,
        request: MiniAppHttpRequest,
        errorStatus: Int,
    ) {
        assertEquals(errorStatus, transport.request(request).statusCode)
    }
}
