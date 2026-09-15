package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.WeChatSessionState
import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxLoginFailureResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxLoginSuccessResult
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatAuthHost
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * How the WeChat adapter turns the host's session check callbacks into the
 * platform-neutral result.
 *
 * These are the raw-callback behaviours; `WechatSessionCheckContractTest` states
 * what must hold for the SDK as a whole.
 */
internal class WechatAuthSessionCheckTest {
    @Test
    fun aUsableSessionIsReportedAsValid() = runTest {
        val host = FakeWechatAuthHost()

        assertEquals(WeChatSessionState.VALID, WechatAuth(host).checkSession())
        assertEquals(1, host.sessionCheckCalls)
    }

    @Test
    fun aFailureCallbackIsReportedAsInvalid() = runTest {
        val host = FakeWechatAuthHost().apply {
            sessionCheckFailure = "checkSession:fail session time out, need relogin"
        }

        assertEquals(WeChatSessionState.INVALID, WechatAuth(host).checkSession())
    }

    @Test
    fun failureMappingDoesNotDependOnTheHostMessage() = runTest {
        val host = FakeWechatAuthHost().apply {
            sessionCheckFailure = "checkSession:fail 需要重新登录"
        }

        assertEquals(WeChatSessionState.INVALID, WechatAuth(host).checkSession())
    }

    @Test
    fun aBareFailureMessageIsAlsoInvalid() = runTest {
        val host = FakeWechatAuthHost().apply {
            sessionCheckFailure = "checkSession:fail"
        }

        assertEquals(WeChatSessionState.INVALID, WechatAuth(host).checkSession())
    }

    @Test
    fun aHostWithoutTheSessionCheckApiIsAnUnsupportedCapability() = runTest {
        val host = FakeWechatAuthHost().apply { sessionCheckSupported = false }

        val failure = assertFailsWith<MiniAppException.UnsupportedCapability> {
            WechatAuth(host).checkSession()
        }

        assertEquals("wechat.check-session", failure.capability.value)
        // Nothing reached the host.
        assertEquals(0, host.sessionCheckCalls)
    }

    @Test
    fun anInvalidSessionDoesNotAcquireANewLoginCode() = runTest {
        val host = FakeWechatAuthHost().apply {
            sessionCheckFailure = "checkSession:fail session time out, need relogin"
        }

        assertEquals(WeChatSessionState.INVALID, WechatAuth(host).checkSession())

        // Recovering is the consumer's decision, not a side effect of the query.
        assertEquals(0, host.calls)
    }

    @Test
    fun aRepeatedTerminalCallbackCompletesOnlyOnce() = runTest {
        val host = FakeWechatAuthHost().apply { completeSessionCheckTwice = true }

        assertEquals(WeChatSessionState.VALID, WechatAuth(host).checkSession())
    }

    @Test
    fun aCallbackAfterCancellationIsIgnored() = runTest {
        val host = DeferredSessionCheckHost()
        val deferred = async(start = CoroutineStart.UNDISPATCHED) {
            WechatAuth(host).checkSession()
        }

        deferred.cancelAndJoin()
        host.succeed()

        assertTrue(deferred.isCancelled)
    }

    /**
     * The host offers no abort handle for this call, so the adapter returns none;
     * nothing is invented to make cancellation look like host cancellation.
     */
    @Test
    fun cancellationDoesNotClaimToCancelTheHostCheck() = runTest {
        val host = DeferredSessionCheckHost()

        val deferred = async(start = CoroutineStart.UNDISPATCHED) {
            WechatAuth(host).checkSession()
        }
        deferred.cancelAndJoin()

        assertEquals(1, host.calls)
        assertTrue(deferred.isCancelled)
    }

    /** A port that stays silent until the test completes it. */
    private class DeferredSessionCheckHost : WechatAuthHost {
        var calls: Int = 0
            private set

        private var succeedCallback: (() -> Unit)? = null

        fun succeed() {
            val callback = requireNotNull(succeedCallback) {
                "The adapter has not started a session check yet"
            }
            callback()
        }

        override fun login(
            success: (WxLoginSuccessResult) -> Unit,
            failure: (WxLoginFailureResult) -> Unit,
        ): Unit = error("Not used")

        override fun isSessionCheckSupported(): Boolean = true

        override fun checkSession(
            success: () -> Unit,
            failure: (WxGeneralCallbackResult) -> Unit,
        ) {
            calls += 1
            succeedCallback = success
        }
    }
}
