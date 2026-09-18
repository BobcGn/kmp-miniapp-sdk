package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatNavigationHost
import io.github.bobcgn.miniapp.host.wechat.testing.RecordedWechatNavigation
import io.github.bobcgn.miniapp.host.wechat.testing.fakeWxFailure
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
        navigation.switchTab("pages/second/index")

        assertEquals(
            listOf(
                RecordedWechatNavigation("navigateTo", url = "pages/second/index", delta = null),
                RecordedWechatNavigation("redirectTo", url = "pages/index/index", delta = null),
                RecordedWechatNavigation("navigateBack", url = null, delta = 2),
                RecordedWechatNavigation("navigateBack", url = null, delta = null),
                RecordedWechatNavigation("switchTab", url = "pages/second/index", delta = null),
            ),
            host.calls,
        )
    }

    @Test
    fun aBlankTabRouteIsRejectedBeforeTheHostIsCalled() = runTest {
        val host = FakeWechatNavigationHost()
        val navigation = WechatNavigation(host)

        listOf("", " ", "\t\n").forEach { blank ->
            val failure = assertFailsWith<IllegalArgumentException> {
                navigation.switchTab(blank)
            }
            assertTrue(
                failure.message!!.contains("tabBar page"),
                "the rejection must say what a switchTab route has to be: ${failure.message}",
            )
        }

        // Nothing reached the host, so no WeChat call was made for an input that
        // names no page at all.
        assertEquals(emptyList(), host.calls)
    }

    @Test
    fun aRouteThatIsNotATabBarPageStaysAHostFailure() = runTest {
        // WeChat reports a route it has no tabBar entry for through the failure
        // callback. The SDK has no list of its own to check against, so this is a
        // host failure and must not be reclassified as anything else.
        val host = FakeWechatNavigationHost(
            failWith = "switchTab:fail can not switch to a non-tabBar page",
        )

        val failure = assertFailsWith<MiniAppException.HostFailure> {
            WechatNavigation(host).switchTab("pages/third/index")
        }

        assertEquals("wechat", failure.host)
        assertEquals("switchTab", failure.metadata["operation"])
        assertEquals("switchTab:fail can not switch to a non-tabBar page", failure.hostMessage)
        assertEquals(listOf(RecordedWechatNavigation("switchTab", url = "pages/third/index", delta = null)), host.calls)
    }

    @Test
    fun theSdkDoesNotSecondGuessTheHostsTabBarList() = runTest {
        // Whether a route has a tab is the mini program's own configuration. The
        // SDK keeps no list, so a route it cannot recognize still reaches the host
        // and succeeds when the host accepts it.
        val host = FakeWechatNavigationHost()

        WechatNavigation(host).switchTab("pages/second/index")

        assertEquals(listOf(RecordedWechatNavigation("switchTab", url = "pages/second/index", delta = null)), host.calls)
    }

    @Test
    fun aFailureThatReadsLikeARefusalIsStillAHostFailure() = runTest {
        // The word "deny" is what the authorize classifier keys on. A tab switch
        // failure is not a permission decision, and reusing that classifier here
        // would report one as the other.
        val host = FakeWechatNavigationHost(
            failWith = "switchTab:fail switch not allowed: user deny for this page",
        )

        val failure = assertFailsWith<MiniAppException.HostFailure> {
            WechatNavigation(host).switchTab("pages/tabtarget/index")
        }

        assertEquals("switchTab", failure.metadata["operation"])
    }

    @Test
    fun repeatedHostCallbacksResumeOnce() = runTest {
        // WeChat documents one terminal callback per call, but the adapter must not
        // depend on that: a second success would otherwise resume a continuation
        // that is already complete.
        val host = RepeatingNavigationHost()

        WechatNavigation(host).switchTab("pages/second/index")

        assertEquals(2, host.successInvocations)
        assertEquals(0, host.failureInvocations)
    }

    @Test
    fun aFailureArrivingAfterSuccessDoesNotReplaceTheResult() = runTest {
        val host = RepeatingNavigationHost(failAfterSuccess = true)

        // Success arrives first, so the call succeeded; the later failure is ignored
        // instead of raising on an already-completed continuation.
        WechatNavigation(host).switchTab("pages/second/index")

        assertEquals(1, host.successInvocations)
        assertEquals(1, host.failureInvocations)
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

        override fun switchTab(
            url: String,
            success: () -> Unit,
            failure: (WxGeneralCallbackResult) -> Unit,
        ): Unit = error("Not used")
    }

    /**
     * A port that answers a `switchTab` call more than once, which WeChat does not
     * do but which the adapter must survive: it counts how often it invoked each
     * callback so a test can see that the second answer was delivered and ignored.
     */
    private class RepeatingNavigationHost(
        private val failAfterSuccess: Boolean = false,
    ) : WechatNavigationHost {
        var successInvocations: Int = 0
            private set

        var failureInvocations: Int = 0
            private set

        override fun switchTab(
            url: String,
            success: () -> Unit,
            failure: (WxGeneralCallbackResult) -> Unit,
        ) {
            success()
            successInvocations += 1
            if (failAfterSuccess) {
                failureInvocations += 1
                failure(fakeWxFailure("switchTab:fail arrived after success"))
            } else {
                success()
                successInvocations += 1
            }
        }

        override fun navigateTo(
            url: String,
            success: () -> Unit,
            failure: (WxGeneralCallbackResult) -> Unit,
        ): Unit = error("Not used")

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
