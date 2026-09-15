package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.interop.WxLoginFailureResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxLoginSuccessResult
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatAuthHost
import io.github.bobcgn.miniapp.host.wechat.testing.fakeWxLoginSuccess
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Adapter-specific login behavior: how the `wx.login` callback port is mapped
 * onto the common error model and onto coroutine cancellation.
 *
 * The login contract itself is covered by `WechatAuthContractTest`.
 */
internal class WechatAuthTest {
    @Test
    fun hostFailureMapsErrnoToCommonError() = runTest {
        val host = FakeWechatAuthHost(
            failWith = "login:fail system error",
            errno = 1000,
        )

        val failure = assertFailsWith<MiniAppException.HostFailure> {
            WechatAuth(host).login()
        }

        assertEquals("wechat", failure.host)
        assertEquals("1000", failure.code)
        assertEquals("login:fail system error", failure.hostMessage)
        assertEquals("login", failure.metadata["operation"])
    }

    @Test
    fun callbackAfterCancellationIsIgnored() = runTest {
        val host = DeferredAuthHost()
        val deferred = async(start = CoroutineStart.UNDISPATCHED) {
            WechatAuth(host).login()
        }

        deferred.cancelAndJoin()
        host.succeed("late-code")

        assertTrue(deferred.isCancelled)
    }

    /** A port that stays silent until the test completes it, for cancellation scenarios. */
    private class DeferredAuthHost : WechatAuthHost {
        private var succeedCallback: ((WxLoginSuccessResult) -> Unit)? = null

        fun succeed(code: String) {
            val callback = requireNotNull(succeedCallback) {
                "The adapter has not started a login yet"
            }
            callback(fakeWxLoginSuccess(code))
        }

        override fun login(
            success: (WxLoginSuccessResult) -> Unit,
            failure: (WxLoginFailureResult) -> Unit,
        ) {
            succeedCallback = success
        }

        override fun isSessionCheckSupported(): Boolean = error("Not used")

        override fun checkSession(
            success: () -> Unit,
            failure: (io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult) -> Unit,
        ): Unit = error("Not used")
    }
}
