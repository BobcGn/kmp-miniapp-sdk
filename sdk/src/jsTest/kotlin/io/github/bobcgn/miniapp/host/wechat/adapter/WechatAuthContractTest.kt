package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatAuthHost
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * The WeChat client login bootstrap contract.
 *
 * Client login is deliberately not a common capability, so there is no shared
 * check object in `commonTest` for it; the contract is stated here, in terms of
 * the SDK-facing [WechatAuth] surface, and driven through the FakeAdapter
 * boundary. Host-specific mapping stays in `WechatAuthTest`.
 */
internal class WechatAuthContractTest {
    @Test
    fun successfulLoginReturnsTheHostsCode() = runTest {
        val result = WechatAuth(FakeWechatAuthHost(code = "short-lived-code")).login()

        assertEquals("short-lived-code", result.code)
    }

    @Test
    fun aBlankCodeIsRejected() = runTest {
        assertFailsWith<MiniAppException.InvalidResponse> {
            WechatAuth(FakeWechatAuthHost(code = "  ")).login()
        }
    }

    @Test
    fun eachLoginCallAsksTheHostOnce() = runTest {
        val host = FakeWechatAuthHost()
        val auth = WechatAuth(host)

        auth.login()
        auth.login()

        assertEquals(2, host.calls)
    }
}
