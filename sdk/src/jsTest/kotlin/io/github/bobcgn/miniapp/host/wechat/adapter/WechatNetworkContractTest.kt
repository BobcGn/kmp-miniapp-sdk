package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.capability.network.MiniAppHttpRequest
import io.github.bobcgn.miniapp.capability.network.MiniAppHttpResponse
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatNetworkHost
import io.github.bobcgn.miniapp.testing.HttpTransportContractChecks
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

/**
 * Runs the shared transport contract against the WeChat adapter.
 *
 * These are the same checks `MiniAppHttpTransportContractTest` runs against the
 * host-neutral reference implementation. Passing here means `WechatNetwork`
 * reports an exchange faithfully through its callback port; it is not real-host
 * evidence, because the port is a fake rather than `wx`.
 *
 * Host-specific behavior — timeout classification, errno mapping, cancellation,
 * and abort — cannot be expressed as a shared guarantee and stays in
 * `WechatNetworkTest`.
 */
internal class WechatNetworkContractTest {
    @Test
    fun exchangeResultIsReportedUnchanged() = runTest {
        HttpTransportContractChecks.exchangeResultIsReportedUnchanged(
            transport = WechatNetwork(FakeWechatNetworkHost()),
            request = REQUEST,
            expected = MiniAppHttpResponse(
                statusCode = 200,
                headers = mapOf("Content-Type" to "application/json"),
                body = "{\"ok\":true}",
            ),
        )
    }

    @Test
    fun httpErrorStatusIsAnOutcome() = runTest {
        HttpTransportContractChecks.httpErrorStatusIsAnOutcome(
            transport = WechatNetwork(FakeWechatNetworkHost(statusCode = 503)),
            request = REQUEST,
            errorStatus = 503,
        )
    }

    private companion object {
        private val REQUEST = MiniAppHttpRequest(url = "https://example.com/ping")
    }
}
