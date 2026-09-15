package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatHapticsHost
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * How the WeChat adapter drives the two vibration callbacks.
 *
 * A resolved call here means only that the fake accepted it. No Node test can
 * show that a device vibrated, so nothing in this file claims one did.
 */
internal class WechatHapticsTest {
    @Test
    fun theShortVibrationReachesTheHost() = runTest {
        val host = FakeWechatHapticsHost()

        WechatHaptics(host).vibrateShort()

        assertEquals(1, host.shortCalls)
        assertEquals(0, host.longCalls)
    }

    @Test
    fun theLongVibrationReachesTheHost() = runTest {
        val host = FakeWechatHapticsHost()

        WechatHaptics(host).vibrateLong()

        assertEquals(1, host.longCalls)
        assertEquals(0, host.shortCalls)
    }

    @Test
    fun theTwoVibrationsDoNotStandInForEachOther() = runTest {
        val host = FakeWechatHapticsHost()
        val haptics = WechatHaptics(host)

        haptics.vibrateShort()
        haptics.vibrateLong()

        // Each call reached its own host API exactly once.
        assertEquals(1, host.shortCalls)
        assertEquals(1, host.longCalls)
    }

    @Test
    fun aHostThatOffersOnlyTheLongVibrationStillReportsShortUnsupported() = runTest {
        val host = FakeWechatHapticsHost(shortSupported = false, longSupported = true)
        val haptics = WechatHaptics(host)

        haptics.vibrateLong()
        val failure = assertFailsWith<MiniAppException.UnsupportedCapability> {
            haptics.vibrateShort()
        }

        assertEquals("wechat.vibrate-short", failure.capability.value)
        assertEquals(1, host.longCalls)
        assertEquals(0, host.shortCalls)
    }

    @Test
    fun aHostWithoutTheShortApiIsAnUnsupportedCapability() = runTest {
        val host = FakeWechatHapticsHost(shortSupported = false)

        val failure = assertFailsWith<MiniAppException.UnsupportedCapability> {
            WechatHaptics(host).vibrateShort()
        }

        assertEquals("wechat.vibrate-short", failure.capability.value)
        assertEquals(0, host.shortCalls)
    }

    @Test
    fun aHostWithoutTheLongApiIsAnUnsupportedCapability() = runTest {
        val host = FakeWechatHapticsHost(longSupported = false)

        val failure = assertFailsWith<MiniAppException.UnsupportedCapability> {
            WechatHaptics(host).vibrateLong()
        }

        assertEquals("wechat.vibrate-long", failure.capability.value)
        assertEquals(0, host.longCalls)
    }

    @Test
    fun aFailedShortVibrationIsAHostFailure() = runTest {
        val host = FakeWechatHapticsHost().apply {
            shortFailure = "vibrateShort:fail system error"
        }

        val failure = assertFailsWith<MiniAppException.HostFailure> {
            WechatHaptics(host).vibrateShort()
        }

        assertEquals("vibrateShort", failure.metadata["operation"])
    }

    @Test
    fun aFailedLongVibrationIsAHostFailure() = runTest {
        val host = FakeWechatHapticsHost().apply {
            longFailure = "vibrateLong:fail system error"
        }

        val failure = assertFailsWith<MiniAppException.HostFailure> {
            WechatHaptics(host).vibrateLong()
        }

        assertEquals("vibrateLong", failure.metadata["operation"])
    }

    @Test
    fun aCallbackAfterCancellationIsIgnored() = runTest {
        val host = DeferredHapticsHost()
        val deferred = async(start = CoroutineStart.UNDISPATCHED) {
            WechatHaptics(host).vibrateShort()
        }

        deferred.cancelAndJoin()
        host.succeed()

        assertTrue(deferred.isCancelled)
    }

    /** A port that stays silent until the test completes it. */
    private class DeferredHapticsHost : WechatHapticsHost {
        private var succeedCallback: (() -> Unit)? = null

        fun succeed() {
            val callback = requireNotNull(succeedCallback) {
                "The adapter has not started a vibration yet"
            }
            callback()
        }

        override fun isShortSupported(): Boolean = true

        override fun isLongSupported(): Boolean = true

        override fun vibrateShort(
            success: () -> Unit,
            failure: (WxGeneralCallbackResult) -> Unit,
        ) {
            succeedCallback = success
        }

        override fun vibrateLong(
            success: () -> Unit,
            failure: (WxGeneralCallbackResult) -> Unit,
        ): Unit = error("Not used")
    }
}
