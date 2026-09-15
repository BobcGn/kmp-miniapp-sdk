package io.github.bobcgn.miniapp.capability.network

import io.github.bobcgn.miniapp.capability.CapabilityKey
import io.github.bobcgn.miniapp.testing.HttpTransportContractChecks
import io.github.bobcgn.miniapp.testing.RecordingHttpTransport
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Runs the shared transport contract against the host-neutral reference
 * implementation, plus the contract's own model guards.
 *
 * The WeChat adapter runs the same checks in `WechatNetworkContractTest`.
 */
internal class MiniAppHttpTransportContractTest {
    @Test
    fun requestDefaultsDescribeAPlainGet() {
        val request = MiniAppHttpRequest(url = "https://example.com/resource")

        assertEquals("https://example.com/resource", request.url)
        assertEquals(HttpMethod.GET, request.method)
        assertEquals(emptyMap(), request.headers)
        assertNull(request.body)
        assertNull(request.timeoutMillis)
    }

    @Test
    fun capabilityKeyIsHostNeutral() {
        assertEquals(CapabilityKey("network"), MiniAppHttpTransport.Key)
    }

    @Test
    fun exchangeResultIsReportedUnchanged() = runTest {
        HttpTransportContractChecks.exchangeResultIsReportedUnchanged(
            transport = RecordingHttpTransport(SCRIPTED_RESPONSE),
            request = REQUEST,
            expected = SCRIPTED_RESPONSE,
        )
    }

    @Test
    fun requestIsHandedOverUnchanged() = runTest {
        HttpTransportContractChecks.requestIsHandedOverUnchanged(
            transport = RecordingHttpTransport(),
            request = MiniAppHttpRequest(
                url = "https://example.com/submit",
                method = HttpMethod.POST,
                headers = mapOf("Content-Type" to "application/json"),
                body = "{}",
                timeoutMillis = 2_000,
            ),
        )
    }

    @Test
    fun httpErrorStatusIsAnOutcome() = runTest {
        HttpTransportContractChecks.httpErrorStatusIsAnOutcome(
            transport = RecordingHttpTransport(ERROR_RESPONSE),
            request = REQUEST,
            errorStatus = ERROR_RESPONSE.statusCode,
        )
    }

    private companion object {
        private val REQUEST = MiniAppHttpRequest(url = "https://example.com/ping")

        private val SCRIPTED_RESPONSE = MiniAppHttpResponse(
            statusCode = 200,
            headers = mapOf("Content-Type" to "text/plain"),
            body = "ok",
        )

        private val ERROR_RESPONSE = MiniAppHttpResponse(
            statusCode = 503,
            headers = emptyMap(),
            body = "unavailable",
        )
    }
}
