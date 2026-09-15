package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.capability.network.HttpMethod
import io.github.bobcgn.miniapp.capability.network.MiniAppHttpRequest
import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.interop.WxRequestFailureResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxRequestSuccessResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxRequestTask
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatNetworkHost
import io.github.bobcgn.miniapp.host.wechat.testing.fakeAbortableTask
import io.github.bobcgn.miniapp.host.wechat.testing.fakeWxRequestFailure
import io.github.bobcgn.miniapp.host.wechat.testing.fakeWxRequestSuccess
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Adapter-specific HTTP transport behavior: how the WeChat callback port is
 * mapped onto the SDK contract, onto the common error model, and onto host
 * cancellation.
 *
 * Faithful reporting of a completed exchange is covered once by the shared
 * contract checks, which `WechatNetworkContractTest` runs against this adapter.
 */
internal class WechatNetworkTest {
    @Test
    fun completedExchangeReturnsStatusHeadersAndBody() = runTest {
        val response = WechatNetwork(FakeWechatNetworkHost()).request(REQUEST)

        assertEquals(200, response.statusCode)
        assertEquals("{\"ok\":true}", response.body)
        assertEquals("application/json", response.headers["Content-Type"])
    }

    @Test
    fun headerShapesTheStringContractCannotCarryAreSkipped() = runTest {
        val host = FakeWechatNetworkHost(
            responseHeaders = js("({ 'Content-Type': 'application/json', 'Set-Cookie': ['a=1'] })"),
        )

        val response = WechatNetwork(host).request(REQUEST)

        // WeChat reports Set-Cookie as an array; the transport contract carries strings only.
        assertNull(response.headers["Set-Cookie"])
    }

    @Test
    fun nonTextBodyIsInvalidResponse() = runTest {
        val host = FakeWechatNetworkHost(body = mapOf("parsed" to "json"))

        assertFailsWith<MiniAppException.InvalidResponse> {
            WechatNetwork(host).request(REQUEST)
        }
    }

    @Test
    fun timeoutFailureMapsToTimeout() = runTest {
        val host = FakeWechatNetworkHost(failWith = "request:fail timeout")

        val failure = assertFailsWith<MiniAppException.Timeout> {
            WechatNetwork(host).request(REQUEST)
        }

        assertEquals("request", failure.operation)
        assertEquals("request:fail timeout", failure.hostMessage)
    }

    @Test
    fun transportFailureMapsToHostFailureWithErrno() = runTest {
        val host = FakeWechatNetworkHost(
            failWith = "request:fail unable to resolve host",
            errno = 600009,
        )

        val failure = assertFailsWith<MiniAppException.HostFailure> {
            WechatNetwork(host).request(REQUEST)
        }

        assertEquals("wechat", failure.host)
        assertEquals("600009", failure.code)
        assertEquals("request", failure.metadata["operation"])
    }

    @Test
    fun requestFieldsReachTheHostPort() = runTest {
        val host = FakeWechatNetworkHost()

        WechatNetwork(host).request(
            MiniAppHttpRequest(
                url = "https://example.com/submit",
                method = HttpMethod.POST,
                headers = mapOf("Content-Type" to "application/json"),
                body = "{\"hello\":\"world\"}",
                timeoutMillis = 5_000,
            ),
        )

        val recorded = host.requests.single()
        assertEquals("https://example.com/submit", recorded.url)
        assertEquals("POST", recorded.method)
        assertEquals(mapOf("Content-Type" to "application/json"), recorded.headers)
        assertEquals("{\"hello\":\"world\"}", recorded.body)
        assertEquals(5_000, recorded.timeoutMillis)
    }

    @Test
    fun cancellationAbortsTheInFlightHostRequestOnce() = runTest {
        val host = DeferredNetworkHost(abortable = true)

        val deferred = async(start = CoroutineStart.UNDISPATCHED) {
            WechatNetwork(host).request(REQUEST)
        }

        deferred.cancel()
        deferred.cancelAndJoin()

        assertEquals(1, host.abortCount)
    }

    @Test
    fun hostResultDeliveredAfterCancellationIsIgnored() = runTest {
        val host = DeferredNetworkHost(abortable = true)

        val deferred = async(start = CoroutineStart.UNDISPATCHED) {
            WechatNetwork(host).request(REQUEST)
        }

        deferred.cancelAndJoin()
        // An abort makes WeChat report the exchange as failed; it must not reach the caller.
        host.fail("request:fail abort")

        assertTrue(deferred.isCancelled)
    }

    @Test
    fun cancellationWithoutAnAbortHandleStillCancelsTheCaller() = runTest {
        val host = DeferredNetworkHost(abortable = false)

        val deferred = async(start = CoroutineStart.UNDISPATCHED) {
            WechatNetwork(host).request(REQUEST)
        }

        deferred.cancelAndJoin()
        host.succeed(fakeWxRequestSuccess())

        assertTrue(deferred.isCancelled)
    }

    /**
     * A port that stays silent until the test completes it, for cancellation
     * scenarios. Two of them are needed because the WeChat adapter distinguishes a
     * port that can abort from one that cannot.
     */
    private class DeferredNetworkHost(private val abortable: Boolean) : WechatNetworkHost {
        private var succeedCallback: ((WxRequestSuccessResult) -> Unit)? = null
        private var failCallback: ((WxRequestFailureResult) -> Unit)? = null

        var abortCount: Int = 0
            private set

        fun succeed(result: WxRequestSuccessResult) {
            val callback = requireNotNull(succeedCallback) {
                "The adapter has not started a request yet"
            }
            callback(result)
        }

        fun fail(message: String) {
            val callback = requireNotNull(failCallback) {
                "The adapter has not started a request yet"
            }
            callback(fakeWxRequestFailure(message = message, errno = null))
        }

        override fun request(
            url: String,
            method: String,
            headers: Map<String, String>,
            body: String?,
            timeoutMillis: Int?,
            success: (WxRequestSuccessResult) -> Unit,
            failure: (WxRequestFailureResult) -> Unit,
        ): WxRequestTask? {
            succeedCallback = success
            failCallback = failure
            return if (abortable) fakeAbortableTask { abortCount += 1 } else null
        }
    }

    private companion object {
        private val REQUEST = MiniAppHttpRequest(url = "https://example.com/ping")
    }
}
