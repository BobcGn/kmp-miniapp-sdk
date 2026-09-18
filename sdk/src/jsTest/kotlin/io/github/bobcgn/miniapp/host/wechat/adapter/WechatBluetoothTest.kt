package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.ExperimentalMiniAppBleApi
import io.github.bobcgn.miniapp.host.wechat.WeChatBleConnectionState
import io.github.bobcgn.miniapp.host.wechat.WeChatBleDevice
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatBluetoothHost
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * The BLE proof of concept's own subject: the event-driven resource model.
 *
 * These tests are about registration, cancellation, cleanup, duplicate policy, and
 * error classification rather than about Bluetooth. The adapter is driven through
 * its raw callback port, so a host condition a real device produces rarely — a
 * listener that refuses removal, a payload of the wrong type, a callback that
 * arrives after the collector left — is produced exactly.
 */
@OptIn(ExperimentalMiniAppBleApi::class)
internal class WechatBluetoothTest {

    @Test
    fun discoveryReachesTheHostAndStopsAgain() = runTest {
        val host = FakeWechatBluetoothHost()
        val bluetooth = WechatBluetooth(host)

        bluetooth.startDiscovery()

        assertEquals(1, host.startDiscoveryCalls)
        // Deduplication is the SDK's own, so the host is asked not to repeat devices.
        assertEquals(false, host.lastAllowDuplicates)
        assertTrue(bluetooth.isDiscoveryRunning)

        bluetooth.stopDiscovery()

        assertEquals(1, host.stopDiscoveryCalls)
        assertFalse(bluetooth.isDiscoveryRunning)
    }

    @Test
    fun stoppingAScanThatIsNotRunningDoesNotCallTheHost() = runTest {
        val host = FakeWechatBluetoothHost()
        val bluetooth = WechatBluetooth(host)

        bluetooth.stopDiscovery()
        bluetooth.stopDiscovery()

        assertEquals(0, host.stopDiscoveryCalls)
    }

    @Test
    fun startingAScanTwiceCallsTheHostOnce() = runTest {
        val host = FakeWechatBluetoothHost()
        val bluetooth = WechatBluetooth(host)

        bluetooth.startDiscovery()
        bluetooth.startDiscovery()

        assertEquals(1, host.startDiscoveryCalls)
    }

    @Test
    fun closingAnAdapterThatIsClosedDoesNotCallTheHost() = runTest {
        val host = FakeWechatBluetoothHost()
        val bluetooth = WechatBluetooth(host)

        bluetooth.closeAdapter()
        bluetooth.closeAdapter()

        assertEquals(0, host.closeCalls)

        bluetooth.openAdapter()
        bluetooth.closeAdapter()
        bluetooth.closeAdapter()

        assertEquals(1, host.openCalls)
        assertEquals(1, host.closeCalls)
        assertFalse(bluetooth.isOpen)
    }

    @Test
    fun discoveredDevicesReachTheCollector() = runTest {
        val host = FakeWechatBluetoothHost()
        val bluetooth = WechatBluetooth(host)

        val seen = mutableListOf<WeChatBleDevice>()
        val collector = launch { bluetooth.discoveredDevices.collect { seen += it } }
        runCurrent()

        host.emitDevices("device-a", "device-b")
        advanceUntilIdle()

        assertContentEquals(
            listOf(
                WeChatBleDevice(deviceId = "device-a", name = "device-device-a", rssi = -40),
                WeChatBleDevice(deviceId = "device-b", name = "device-device-b", rssi = -40),
            ),
            seen,
        )
        collector.cancel()
    }

    @Test
    fun aDeviceReportedTwiceIsEmittedOnce() = runTest {
        val host = FakeWechatBluetoothHost()
        val bluetooth = WechatBluetooth(host)

        val seen = mutableListOf<WeChatBleDevice>()
        val collector = launch { bluetooth.discoveredDevices.collect { seen += it } }
        runCurrent()

        // The host repeats the device, both in one batch and in a later event, as a
        // host asked to report duplicates would.
        host.emitDevices("device-a", "device-a")
        advanceUntilIdle()
        host.emitDevices("device-a")
        advanceUntilIdle()

        assertContentEquals(
            listOf(WeChatBleDevice(deviceId = "device-a", name = "device-device-a", rssi = -40)),
            seen,
        )
        collector.cancel()
    }

    @Test
    fun eachCollectorOwnsItsOwnRegistration() = runTest {
        val host = FakeWechatBluetoothHost()
        val bluetooth = WechatBluetooth(host)

        val first = mutableListOf<WeChatBleDevice>()
        val second = mutableListOf<WeChatBleDevice>()
        val firstCollector = launch { bluetooth.discoveredDevices.collect { first += it } }
        runCurrent()
        val secondCollector = launch { bluetooth.discoveredDevices.collect { second += it } }
        runCurrent()

        assertEquals(2, host.registeredDeviceListeners.size)
        assertEquals(2, bluetooth.activeListenerCount)

        host.emitDevices("device-a")
        advanceUntilIdle()

        assertContentEquals(listOf(WeChatBleDevice("device-a", "device-device-a", -40)), first)
        assertContentEquals(listOf(WeChatBleDevice("device-a", "device-device-a", -40)), second)

        // Cancelling one collector removes exactly one registration, and the other keeps
        // receiving: neither collector's state is shared with the other.
        firstCollector.cancel()
        advanceUntilIdle()
        assertEquals(1, host.removedDeviceListeners.size)
        assertEquals(1, bluetooth.activeListenerCount)

        host.emitDevices("device-b")
        advanceUntilIdle()
        // The cancelled collector's listener is still on the fake host's list, so this
        // also shows a late callback reaching it changes nothing: the live collector
        // has both devices and the cancelled one was not resumed.
        assertEquals(2, second.size)
        secondCollector.cancel()
        advanceUntilIdle()
        assertEquals(0, bluetooth.activeListenerCount)
    }

    @Test
    fun cancellingACollectorRemovesItsListener() = runTest {
        val host = FakeWechatBluetoothHost()
        val bluetooth = WechatBluetooth(host)

        val collector = launch { bluetooth.discoveredDevices.collect { } }
        runCurrent()
        assertEquals(1, bluetooth.activeListenerCount)

        collector.cancel()
        advanceUntilIdle()

        assertEquals(1, host.removedDeviceListeners.size)
        assertEquals(0, bluetooth.activeListenerCount)
        // The removal used the very value that was registered.
        assertSame(host.registeredDeviceListeners.single(), host.removedDeviceListeners.single())
    }

    @Test
    fun aCollectorThatFinishesNormallyRemovesItsListener() = runTest {
        val host = FakeWechatBluetoothHost()
        val bluetooth = WechatBluetooth(host)

        val collector = launch { bluetooth.discoveredDevices.take(1).collect { } }
        runCurrent()
        host.emitDevices("device-a")
        advanceUntilIdle()
        assertTrue(collector.isCompleted)

        assertEquals(1, host.removedDeviceListeners.size)
        assertSame(host.registeredDeviceListeners.single(), host.removedDeviceListeners.single())
        assertEquals(0, bluetooth.activeListenerCount)
    }

    @Test
    fun aStreamThatFailsRemovesItsListener() = runTest {
        val host = FakeWechatBluetoothHost()
        val bluetooth = WechatBluetooth(host)

        val outcome = mutableListOf<Throwable>()
        val collector = launch {
            try {
                bluetooth.discoveredDevices.collect { }
            } catch (error: Throwable) {
                outcome += error
            }
        }
        runCurrent()
        assertEquals(1, bluetooth.activeListenerCount)

        // A payload the host's own schema forbids: `devices` is not an array.
        host.emitUnreadableDevices()
        advanceUntilIdle()
        collector.join()

        assertEquals(1, outcome.size, "a payload the SDK cannot read must end the stream")
        assertIs<MiniAppException.InvalidResponse>(outcome.single())
        assertEquals(1, host.removedDeviceListeners.size)
        assertEquals(0, bluetooth.activeListenerCount)
    }

    @Test
    fun cancellingACollectorIsNotReportedAsAHostFailure() = runTest {
        val host = FakeWechatBluetoothHost()
        val bluetooth = WechatBluetooth(host)

        val collector = launch { bluetooth.discoveredDevices.collect { } }
        runCurrent()
        collector.cancel()
        advanceUntilIdle()

        assertTrue(collector.isCancelled)
        assertEquals(0, bluetooth.listenerRemovalFailures)
    }

    @Test
    fun aCallbackAfterTheCollectorLeftIsDiscarded() = runTest {
        val host = FakeWechatBluetoothHost()
        val bluetooth = WechatBluetooth(host)
        val seen = mutableListOf<WeChatBleDevice>()

        val collector = launch { bluetooth.discoveredDevices.collect { seen += it } }
        runCurrent()
        host.emitDevices("device-a")
        advanceUntilIdle()
        collector.cancel()
        advanceUntilIdle()

        // The host reports one more device after the collector ended. The listener the
        // fake keeps stands in for a host that has not processed the removal yet.
        host.emitDevices("device-late")

        assertEquals(1, seen.size)
        assertEquals("device-a", seen.single().deviceId)
    }

    @Test
    fun aRemovalTheHostRefusesIsCountedAndDoesNotReplaceTheOutcome() = runTest {
        val host = FakeWechatBluetoothHost().apply { removalFails = true }
        val bluetooth = WechatBluetooth(host)

        val seen = mutableListOf<WeChatBleDevice>()
        val collector = launch { bluetooth.discoveredDevices.take(1).collect { seen += it } }
        runCurrent()
        host.emitDevices("device-a")
        advanceUntilIdle()
        collector.join()

        // The collection still completes with what it saw: a cleanup failure must not
        // replace the result the caller was waiting for.
        assertEquals(1, seen.size)
        assertEquals(1, bluetooth.listenerRemovalFailures)
        // The listener is still registered as far as anyone can tell, and the count
        // says so rather than reporting a cleanup that did not happen.
        assertEquals(1, bluetooth.activeListenerCount)
    }

    @Test
    fun anEntryTheSdkCannotReadIsSkippedAndCounted() = runTest {
        val host = FakeWechatBluetoothHost()
        val bluetooth = WechatBluetooth(host)

        val seen = mutableListOf<WeChatBleDevice>()
        val collector = launch { bluetooth.discoveredDevices.collect { seen += it } }
        runCurrent()

        // One entry without an identifier beside one that is readable.
        host.emitDeviceWithoutIdentifier()
        advanceUntilIdle()
        host.emitDevices("device-a")
        advanceUntilIdle()

        assertContentEquals(listOf(WeChatBleDevice("device-a", "device-device-a", -40)), seen)
        collector.cancel()
        assertEquals(1, bluetooth.unreadableDeviceEntries)
    }

    @Test
    fun anUnsupportedCapabilityIsReportedAsSuch() = runTest {
        val host = FakeWechatBluetoothHost(discoverySupported = false, connectionSupported = false)
        val bluetooth = WechatBluetooth(host)

        val startFailure = assertFailsWith<MiniAppException.UnsupportedCapability> {
            bluetooth.startDiscovery()
        }
        assertEquals("wechat.bluetooth-discovery", startFailure.capability.value)

        val streamFailure = runCatching { bluetooth.discoveredDevices.toList() }
        assertIs<MiniAppException.UnsupportedCapability>(streamFailure.exceptionOrNull())

        assertIs<MiniAppException.UnsupportedCapability>(
            runCatching { bluetooth.connect("device-a") }.exceptionOrNull(),
        )
    }

    @Test
    fun anAdapterThatCannotBeOpenedReportsTheHostFailure() = runTest {
        // The installed base library reports a switched-off adapter as a plain
        // openBluetoothAdapter failure, so this is a host failure and the SDK does
        // not reclassify it as a permission or a refusal.
        val host = FakeWechatBluetoothHost().apply {
            openFailure = "openBluetoothAdapter:fail bluetooth adapter unavailable"
        }

        val failure = assertFailsWith<MiniAppException.HostFailure> {
            WechatBluetooth(host).openAdapter()
        }

        assertEquals("wechat", failure.host)
        assertEquals("openBluetoothAdapter", failure.metadata["operation"])
        assertEquals("openBluetoothAdapter:fail bluetooth adapter unavailable", failure.hostMessage)
    }

    @Test
    fun anAdapterStateTheSdkCannotReadIsAnInvalidResponse() = runTest {
        val host = FakeWechatBluetoothHost()
        // `available` is required by the host's own schema, so a host that omits it
        // has broken its contract rather than reported a state.
        host.adapterStateAvailable = null

        assertIs<MiniAppException.InvalidResponse>(
            runCatching { WechatBluetooth(host).adapterState() }.exceptionOrNull(),
        )
    }

    @Test
    fun aReadableAdapterStateCarriesWhatTheHostStated() = runTest {
        val host = FakeWechatBluetoothHost()
        host.adapterStateAvailable = true
        host.adapterStateDiscovering = true
        host.adapterStatePowered = false

        val state = WechatBluetooth(host).adapterState()

        assertEquals(true, state.available)
        assertEquals(true, state.discovering)
        // "Powered off" is reported as what it is, not flattened into `false`.
        assertEquals(false, state.powered)
    }

    @Test
    fun connectionAndDisconnectionReachTheHost() = runTest {
        val host = FakeWechatBluetoothHost()
        val bluetooth = WechatBluetooth(host)

        bluetooth.connect("device-a")
        bluetooth.disconnect("device-a")

        assertContentEquals(listOf("device-a"), host.connectCalls)
        assertContentEquals(listOf("device-a"), host.disconnectCalls)
    }

    @Test
    fun aBlankDeviceIdentifierIsRefusedBeforeTheHostIsCalled() = runTest {
        val host = FakeWechatBluetoothHost()
        val bluetooth = WechatBluetooth(host)

        listOf("", "  ").forEach { blank ->
            assertFailsWith<IllegalArgumentException> { bluetooth.connect(blank) }
            assertFailsWith<IllegalArgumentException> { bluetooth.disconnect(blank) }
        }

        assertEquals(emptyList(), host.connectCalls)
        assertEquals(emptyList(), host.disconnectCalls)
    }

    @Test
    fun connectionStateChangesReachTheCollector() = runTest {
        val host = FakeWechatBluetoothHost()
        val bluetooth = WechatBluetooth(host)

        val states = mutableListOf<WeChatBleConnectionState>()
        val collector = launch { bluetooth.connectionStates.collect { states += it } }
        runCurrent()

        host.emitConnectionState(deviceId = "device-a", connected = true)
        advanceUntilIdle()
        host.emitConnectionState(deviceId = "device-a", connected = false)
        advanceUntilIdle()

        assertEquals(true, states[0].connected)
        assertEquals(false, states[1].connected)
        assertEquals("device-a", states[0].deviceId)
        collector.cancel()
        advanceUntilIdle()
        assertEquals(0, bluetooth.activeListenerCount)
    }

    @Test
    fun aConnectionEventWithoutADeviceEndsTheStream() = runTest {
        val host = FakeWechatBluetoothHost()
        val bluetooth = WechatBluetooth(host)

        val outcome = mutableListOf<Throwable>()
        val collector = launch {
            try {
                bluetooth.connectionStates.collect { }
            } catch (error: Throwable) {
                outcome += error
            }
        }
        runCurrent()

        host.emitConnectionStateWithoutDevice()
        advanceUntilIdle()
        collector.join()

        assertIs<MiniAppException.InvalidResponse>(outcome.single())
        assertEquals(1, host.removedConnectionListeners.size)
    }

    @Test
    fun aConnectionFailureIsReportedAsAHostFailure() = runTest {
        val host = FakeWechatBluetoothHost().apply {
            connectFailure = "createBLEConnection:fail API_NOT_SUPPORT"
        }

        val failure = assertFailsWith<MiniAppException.HostFailure> {
            WechatBluetooth(host).connect("device-a")
        }

        assertEquals("createBLEConnection", failure.metadata["operation"])
    }

    @Test
    fun theAdapterQueriesNoPermissionOrPrivacyState() = runTest {
        // Bluetooth's own preconditions are not modelled: the SDK has no evidence
        // that this capability is gated by a WeChat permission or by the privacy
        // contract, so it asks for neither. The adapter's only port is the Bluetooth
        // host, and these counts show it stays that way when the whole flow runs.
        val host = FakeWechatBluetoothHost()
        val bluetooth = WechatBluetooth(host)

        bluetooth.openAdapter()
        val collector = launch { bluetooth.discoveredDevices.take(1).collect { } }
        runCurrent()
        bluetooth.startDiscovery()
        host.emitDevices("device-a")
        advanceUntilIdle()
        collector.join()
        bluetooth.connect("device-a")
        bluetooth.disconnect("device-a")
        bluetooth.stopDiscovery()
        bluetooth.closeAdapter()

        assertEquals(1, host.openCalls)
        // Opening the adapter does not query its state: the SDK reports what the host
        // says when asked, and asking is the caller's decision.
        assertEquals(0, host.adapterStateQueryCalls)
        assertFalse(bluetooth.isOpen)
        assertEquals(0, bluetooth.activeListenerCount)
    }
}
