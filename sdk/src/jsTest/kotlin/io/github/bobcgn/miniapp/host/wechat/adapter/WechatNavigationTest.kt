package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatNavigationHost
import io.github.bobcgn.miniapp.host.wechat.testing.RecordedWechatNavigation
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Adapter-specific navigation behavior: how the WeChat page-stack callbacks are
 * mapped onto the SDK and onto the common error model.
 *
 * Navigation is not a common capability, so these checks live with the adapter
 * rather than in a shared contract object.
 */
internal class WechatNavigationTest {
    @Test
    fun eachOperationReachesTheHostPortWithItsArguments() = runTest {
        val host = FakeWechatNavigationHost()
        val navigation = WechatNavigation(host)

        navigation.navigateTo("pages/second/index")
        navigation.redirectTo("pages/index/index")
        navigation.navigateBack(delta = 2)
        // Omitting delta must stay omitted so WeChat's own default of one applies.
        navigation.navigateBack()

        assertEquals(
            listOf(
                RecordedWechatNavigation("navigateTo", url = "pages/second/index", delta = null),
                RecordedWechatNavigation("redirectTo", url = "pages/index/index", delta = null),
                RecordedWechatNavigation("navigateBack", url = null, delta = 2),
                RecordedWechatNavigation("navigateBack", url = null, delta = null),
            ),
            host.calls,
        )
    }

    @Test
    fun hostFailureMapsToTheCommonErrorModel() = runTest {
        val host = FakeWechatNavigationHost(failWith = "navigateTo:fail webview count limit exceed")

        val failure = assertFailsWith<MiniAppException.HostFailure> {
            WechatNavigation(host).navigateTo("pages/second/index")
        }

        assertEquals("wechat", failure.host)
        assertEquals("navigateTo:fail webview count limit exceed", failure.hostMessage)
        assertEquals("navigateTo", failure.metadata["operation"])
    }

    @Test
    fun goingBackFromTheFirstPageIsNotSilentlySuccessful() = runTest {
        val host = FakeWechatNavigationHost(failWith = "navigateBack:fail cannot navigate back at first page")

        val failure = assertFailsWith<MiniAppException.HostFailure> {
            WechatNavigation(host).navigateBack()
        }

        assertEquals("navigateBack", failure.metadata["operation"])
    }

    @Test
    fun callbackAfterCancellationIsIgnored() = runTest {
        val host = DeferredNavigationHost()
        val deferred = async(start = CoroutineStart.UNDISPATCHED) {
            WechatNavigation(host).navigateTo("pages/second/index")
        }

        deferred.cancelAndJoin()
        host.succeed()

        assertTrue(deferred.isCancelled)
    }

    /** A port that stays silent until the test completes it, for cancellation scenarios. */
    private class DeferredNavigationHost : WechatNavigationHost {
        private var succeedCallback: (() -> Unit)? = null

        fun succeed() {
            val callback = requireNotNull(succeedCallback) {
                "The adapter has not started a navigation yet"
            }
            callback()
        }

        override fun navigateTo(
            url: String,
            success: () -> Unit,
            failure: (WxGeneralCallbackResult) -> Unit,
        ) {
            succeedCallback = success
        }

        override fun redirectTo(
            url: String,
            success: () -> Unit,
            failure: (WxGeneralCallbackResult) -> Unit,
        ): Unit = error("Not used")

        override fun navigateBack(
            delta: Int?,
            success: () -> Unit,
            failure: (WxGeneralCallbackResult) -> Unit,
        ): Unit = error("Not used")
    }
}
