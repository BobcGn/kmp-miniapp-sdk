package io.github.bobcgn.miniapp.export

import io.github.bobcgn.miniapp.host.wechat.ExperimentalMiniAppBleApi
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatBluetooth
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatBluetoothHost
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The JavaScript-facing session: when collection starts and stops, and what it
 * reports afterwards.
 *
 * These tests exist because the session is what a JavaScript caller actually drives.
 * The listener counts here are the proof of concept's own question — whether the SDK
 * can hold a listener for exactly as long as it says it does.
 */
@OptIn(ExperimentalMiniAppBleApi::class)
internal class BleObservationSessionTest {

    @Test
    fun openingTheAdapterStartsTheConnectionWatcherAndClosingItStopsIt() = runTest {
        val host = FakeWechatBluetoothHost()
        val session = BleObservationSession(WechatBluetooth(host), scope = this)

        session.openAdapter()
        advanceUntilIdle()

        assertEquals(1, host.openCalls)
        assertTrue(session.isObservingConnections)
        assertEquals(1, session.listenerCount())

        session.closeAdapter()
        advanceUntilIdle()

        assertEquals(1, host.closeCalls)
        assertFalse(session.isObservingConnections)
        assertEquals(0, session.listenerCount())
        assertEquals(1, host.removedConnectionListeners.size)
    }

    @Test
    fun startingDiscoveryCollectsAndStoppingItReleasesTheListener() = runTest {
        val host = FakeWechatBluetoothHost()
        val session = BleObservationSession(WechatBluetooth(host), scope = this)

        session.startDiscovery()
        advanceUntilIdle()

        assertTrue(session.isObservingDevices)
        assertEquals(1, session.listenerCount())

        host.emitDevices("device-a")
        advanceUntilIdle()
        assertEquals(listOf("device-a"), session.devices().map { it.deviceId })

        session.stopDiscovery()
        advanceUntilIdle()

        assertEquals(0, session.listenerCount())
        assertEquals(1, host.removedDeviceListeners.size)
        // The session keeps what it saw: stopping collection is not forgetting.
        assertEquals(listOf("device-a"), session.devices().map { it.deviceId })
        // Nor is stopping a failure: the collector was cancelled, and a cancellation is
        // not an outcome the session reports as a stream that ended badly.
        assertNull(session.discoveryFailure())
    }

    @Test
    fun closingTheAdapterAlsoEndsACollectingScan() = runTest {
        // WeChat's own close ends a scan, so the SDK must not leave a device collector
        // holding a listener for a scan that has stopped.
        val host = FakeWechatBluetoothHost()
        val session = BleObservationSession(WechatBluetooth(host), scope = this)

        session.openAdapter()
        session.startDiscovery()
        advanceUntilIdle()
        assertEquals(2, session.listenerCount())

        session.closeAdapter()
        advanceUntilIdle()

        assertEquals(0, session.listenerCount())
        assertFalse(session.isObservingDevices)
        assertFalse(session.isObservingConnections)
    }

    @Test
    fun startingASecondDiscoveryDoesNotAddASecondListener() = runTest {
        val host = FakeWechatBluetoothHost()
        val session = BleObservationSession(WechatBluetooth(host), scope = this)

        session.startDiscovery()
        advanceUntilIdle()
        session.startDiscovery()
        advanceUntilIdle()

        assertEquals(1, host.registeredDeviceListeners.size)
        assertEquals(1, session.listenerCount())
        // The adapter also keeps its own scan state, so the host is asked to start once.
        assertEquals(1, host.startDiscoveryCalls)

        session.stopDiscovery()
        advanceUntilIdle()
        assertEquals(0, session.listenerCount())
    }

    @Test
    fun aStartThatTheHostRefusesLeavesNoCollectorBehind() = runTest {
        val host = FakeWechatBluetoothHost().apply {
            startDiscoveryFailure = "startBluetoothDevicesDiscovery:fail already discovering devices"
        }
        val session = BleObservationSession(WechatBluetooth(host), scope = this)

        val failure = runCatching { session.startDiscovery() }
        advanceUntilIdle()

        assertTrue(failure.isFailure)
        assertFalse(session.isObservingDevices)
        assertEquals(0, session.listenerCount())
    }

    @Test
    fun aStreamThatEndsIsReportedByItsClosedClassification() = runTest {
        val host = FakeWechatBluetoothHost()
        val session = BleObservationSession(WechatBluetooth(host), scope = this)

        session.startDiscovery()
        advanceUntilIdle()
        host.emitUnreadableDevices()
        advanceUntilIdle()

        assertEquals("InvalidResponse", session.discoveryFailure())
        // The collector has ended, so its listener is gone even though nobody asked to
        // stop observing.
        assertEquals(0, session.listenerCount())
    }

    @Test
    fun theSessionKeepsABoundedWindowRatherThanEverythingItSaw() = runTest {
        val host = FakeWechatBluetoothHost()
        val session = BleObservationSession(WechatBluetooth(host), scope = this)

        session.startDiscovery()
        advanceUntilIdle()
        repeat(40) { index ->
            host.emitDevices("device-$index")
            advanceUntilIdle()
        }

        val devices = session.devices()
        assertEquals(32, devices.size)
        // The oldest devices were dropped, and the newest was kept.
        assertEquals("device-39", devices.last().deviceId)
        assertEquals("device-8", devices.first().deviceId)

        session.stopDiscovery()
        advanceUntilIdle()
    }
}
