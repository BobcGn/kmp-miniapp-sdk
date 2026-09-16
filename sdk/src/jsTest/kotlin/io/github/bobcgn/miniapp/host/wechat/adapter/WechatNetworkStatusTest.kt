package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.capability.network.MiniAppNetworkState
import io.github.bobcgn.miniapp.capability.network.MiniAppNetworkStatus
import io.github.bobcgn.miniapp.capability.network.NetworkType
import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatNetworkStatusHost
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * How the WeChat adapter turns the host's network callbacks into SDK behaviour.
 *
 * These are the raw-callback and listener behaviours; `WechatNetworkContractTest`
 * states what must hold for the SDK as a whole.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class WechatNetworkStatusTest {
    @Test
    fun theQueryReportsTheConnectionKindTheHostNamed() = runTest {
        val host = FakeWechatNetworkStatusHost(networkType = "wifi")

        val state = WechatNetworkStatus(host).current()

        assertEquals(true, state.isConnected)
        assertEquals(NetworkType.WIFI, state.networkType)
        assertEquals("wifi", state.hostNetworkType)
        assertEquals(1, host.queryCalls)
    }

    @Test
    fun everyKnownKindIsMapped() = runTest {
        val expected = mapOf(
            "wifi" to NetworkType.WIFI,
            "2g" to NetworkType.CELLULAR_2G,
            "3g" to NetworkType.CELLULAR_3G,
            "4g" to NetworkType.CELLULAR_4G,
            "5g" to NetworkType.CELLULAR_5G,
            "unknown" to NetworkType.UNKNOWN,
            "none" to NetworkType.NONE,
        )

        expected.forEach { (hostType, expectedType) ->
            val state = WechatNetworkStatus(
                FakeWechatNetworkStatusHost(networkType = hostType),
            ).current()

            assertEquals(expectedType, state.networkType, hostType)
        }
    }

    @Test
    fun aKindTheSdkDoesNotKnowIsPreservedRatherThanGuessedAt() = runTest {
        val host = FakeWechatNetworkStatusHost(networkType = "6g")

        val state = WechatNetworkStatus(host).current()

        assertNull(state.networkType)
        assertEquals("6g", state.hostNetworkType)
        assertEquals(true, state.isConnected)
    }

    @Test
    fun noConnectionIsReportedAsNotConnected() = runTest {
        val host = FakeWechatNetworkStatusHost(networkType = "none")

        val state = WechatNetworkStatus(host).current()

        assertEquals(false, state.isConnected)
        assertEquals(NetworkType.NONE, state.networkType)
    }

    @Test
    fun anUnusableQueryAnswerIsAnInvalidResponse() = runTest {
        listOf<Any?>(null, 7, "   ").forEach { value ->
            val host = FakeWechatNetworkStatusHost(networkType = value)

            assertFailsWith<MiniAppException.InvalidResponse>("networkType of $value") {
                WechatNetworkStatus(host).current()
            }
        }
    }

    @Test
    fun aFailedQueryIsAHostFailure() = runTest {
        val host = FakeWechatNetworkStatusHost().apply {
            queryFailure = "getNetworkType:fail system error"
        }

        val failure = assertFailsWith<MiniAppException.HostFailure> {
            WechatNetworkStatus(host).current()
        }

        assertEquals("getNetworkType", failure.metadata["operation"])
    }

    @Test
    fun aHostWithoutTheQueryIsAnUnsupportedCapability() = runTest {
        val host = FakeWechatNetworkStatusHost(querySupported = false)

        val failure = assertFailsWith<MiniAppException.UnsupportedCapability> {
            WechatNetworkStatus(host).current()
        }

        assertEquals(MiniAppNetworkStatus.QueryKey, failure.capability)
        assertEquals(0, host.queryCalls)
    }

    @Test
    fun aQueryThatCompletesTwiceIsAnsweredOnce() = runTest {
        val host = FakeWechatNetworkStatusHost(networkType = "wifi").apply {
            completeQueryTwice = true
        }

        assertEquals(NetworkType.WIFI, WechatNetworkStatus(host).current().networkType)
    }

    @Test
    fun changesForwardsWhatTheHostReports() = runTest {
        val host = FakeWechatNetworkStatusHost()
        val states = mutableListOf<MiniAppNetworkState>()
        val job = launch { WechatNetworkStatus(host).changes.collect { states += it } }
        runCurrent()

        host.emit(isConnected = true, networkType = "wifi")
        advanceUntilIdle()
        host.emit(isConnected = true, networkType = "4g")
        advanceUntilIdle()

        assertEquals(
            listOf(NetworkType.WIFI, NetworkType.CELLULAR_4G),
            states.map { it.networkType },
        )
        job.cancel()
    }

    @Test
    fun noStateIsSynthesizedWhenCollecting() = runTest {
        // A host that has reported nothing has nothing to say, and the SDK does not
        // invent a reading to fill the gap.
        val host = FakeWechatNetworkStatusHost()
        val states = mutableListOf<MiniAppNetworkState>()
        val job = launch { WechatNetworkStatus(host).changes.collect { states += it } }
        runCurrent()

        assertEquals(emptyList(), states)
        assertEquals(0, host.queryCalls, "collecting must not query the host")
        job.cancel()
    }

    @Test
    fun theListenerIsRegisteredOnceAndRemovedOnce() = runTest {
        val host = FakeWechatNetworkStatusHost()
        val job = launch { WechatNetworkStatus(host).changes.collect { } }
        runCurrent()

        assertEquals(1, host.addCalls)
        assertEquals(0, host.removeCalls)

        job.cancel()
        advanceUntilIdle()

        assertEquals(1, host.removeCalls)
        assertEquals(0, host.activeListeners)
    }

    @Test
    fun anEventAfterTheCollectorEndsIsNotDelivered() = runTest {
        val host = FakeWechatNetworkStatusHost()
        val states = mutableListOf<MiniAppNetworkState>()
        val job = launch { WechatNetworkStatus(host).changes.collect { states += it } }
        runCurrent()

        job.cancel()
        advanceUntilIdle()
        host.emit(isConnected = true, networkType = "wifi")
        advanceUntilIdle()

        assertEquals(emptyList(), states)
        assertEquals(0, host.activeListeners)
    }

    @Test
    fun eachCollectorOwnsItsOwnRegistration() = runTest {
        // Per-collector registration is the documented choice: it makes every
        // registration pair with exactly one removal, at the cost of one host listener
        // per active collector.
        val host = FakeWechatNetworkStatusHost()
        val status = WechatNetworkStatus(host)
        val first = launch { status.changes.collect { } }
        runCurrent()
        val second = launch { status.changes.collect { } }
        runCurrent()

        assertEquals(2, host.addCalls)

        first.cancel()
        advanceUntilIdle()

        assertEquals(1, host.removeCalls)
        assertEquals(1, host.activeListeners)

        second.cancel()
        advanceUntilIdle()

        assertEquals(2, host.removeCalls)
        assertEquals(0, host.activeListeners)
    }

    @Test
    fun aHostWithoutTheListenerIsAnUnsupportedCapability() = runTest {
        val host = FakeWechatNetworkStatusHost(listenerSupported = false)

        val failure = assertFailsWith<MiniAppException.UnsupportedCapability> {
            WechatNetworkStatus(host).changes.collect { }
        }

        assertEquals(MiniAppNetworkStatus.ListenerKey, failure.capability)
        assertEquals(0, host.addCalls, "nothing may be registered that cannot be removed")
    }

    @Test
    fun anEventTheSdkCannotReadEndsTheStream() = runTest {
        val host = FakeWechatNetworkStatusHost()
        val failure = mutableListOf<Throwable>()
        val job = launch {
            try {
                WechatNetworkStatus(host).changes.collect { }
            } catch (error: Throwable) {
                failure += error
            }
        }
        runCurrent()

        host.emit(isConnected = "yes", networkType = "wifi")
        advanceUntilIdle()

        assertEquals(1, failure.size)
        assertTrue(failure[0] is MiniAppException.InvalidResponse, "was ${failure[0]}")
        assertEquals(1, host.removeCalls)
        assertEquals(0, host.activeListeners)
        job.cancel()
    }

    @Test
    fun aMalformedEventEndsThatCollectorAndALaterOneStillWorks() = runTest {
        val host = FakeWechatNetworkStatusHost()
        val first = async {
            try {
                WechatNetworkStatus(host).changes.collect { }
                null
            } catch (error: Throwable) {
                error
            }
        }
        runCurrent()

        host.emit(isConnected = true, networkType = null)
        advanceUntilIdle()

        assertTrue(first.await() is MiniAppException.InvalidResponse)
        assertEquals(0, host.activeListeners)

        val states = mutableListOf<MiniAppNetworkState>()
        val second = launch { WechatNetworkStatus(host).changes.collect { states += it } }
        runCurrent()
        host.emit(isConnected = true, networkType = "wifi")
        advanceUntilIdle()

        assertEquals(1, states.size)
        second.cancel()
    }
}
