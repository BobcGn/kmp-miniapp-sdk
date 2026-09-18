package io.github.bobcgn.miniapp.host.wechat.interop

import io.github.bobcgn.miniapp.host.wechat.testing.fakeBleConnectionStateResult
import io.github.bobcgn.miniapp.host.wechat.testing.fakeBluetoothAdapterStateChangeResult
import io.github.bobcgn.miniapp.host.wechat.testing.fakeBluetoothAdapterStateResult
import io.github.bobcgn.miniapp.host.wechat.testing.fakeBluetoothDeviceFoundResult
import io.github.bobcgn.miniapp.host.wechat.testing.fakeBluetoothDeviceRecord
import kotlin.js.jsTypeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull

/**
 * The BLE interop boundary: what it refuses to read, and what it leaves absent.
 *
 * The guards are evaluated with no WeChat global at all, because the Node test
 * environment has none, and a guard that threw there would break the SDK's own
 * outside-WeChat loading rather than report a missing capability.
 */
internal class WxBluetoothTest {

    @Test
    fun presenceGuardsAnswerFalseWithoutAWxGlobal() {
        assertFalse(hasWxBluetoothAdapter())
        assertFalse(hasWxGetBluetoothAdapterState())
        assertFalse(hasWxBluetoothDiscovery())
        assertFalse(hasWxBleConnection())
        assertFalse(hasWxBluetoothAdapterStateChange())
    }

    @Test
    fun theDiscoveryOptionsStateTheDuplicateChoiceAndNothingElse() {
        val options = wxStartBluetoothDevicesDiscoveryOptions(allowDuplicates = false)

        assertEquals(false, options.allowDuplicatesKey)
        // Everything else is left to the host's own default: the SDK sets no service
        // filter, no batching interval, and no radio profile.
        assertEquals("undefined", jsTypeOf(options.success))
        assertEquals("undefined", jsTypeOf(options.fail))
        assertEquals("undefined", jsTypeOf(options.complete))
    }

    @Test
    fun theConnectionOptionsCarryOnlyTheDevice() {
        val options = wxCreateBleConnectionOptions("device-a")

        assertEquals("device-a", options.deviceId)
        // No timeout is chosen by the SDK, so the host's own default applies.
        assertEquals("undefined", jsTypeOf(options.success))
        assertEquals("undefined", jsTypeOf(options.fail))

        val closeOptions = wxCloseBleConnectionOptions("device-a")
        assertEquals("device-a", closeOptions.deviceId)
    }

    @Test
    fun theAdapterOptionsCarryNothingTheSdkChose() {
        val open = wxOpenBluetoothAdapterOptions()
        assertEquals("undefined", jsTypeOf(open.success))
        assertEquals("undefined", jsTypeOf(open.fail))

        val state = wxGetBluetoothAdapterStateOptions()
        assertEquals("undefined", jsTypeOf(state.success))
    }

    @Test
    fun aReadableAdapterStateKeepsAbsentFieldsAbsent() {
        val state = wxReadAdapterState(fakeBluetoothAdapterStateResult(true, false, null))
        assertEquals(true, state?.available)
        assertEquals(false, state?.discovering)
        // The host did not state whether it is powered, which is not the same as off.
        assertNull(state?.powered)
    }

    @Test
    fun anAdapterStateWithoutItsRequiredFieldIsUnreadable() {
        assertNull(wxReadAdapterState(fakeBluetoothAdapterStateResult(available = null, discovering = true, powered = true)))
        assertNull(wxReadAdapterState(fakeBluetoothAdapterStateResult(available = "yes", discovering = true, powered = true)))
    }

    @Test
    fun anAdapterStateChangeCarriesOnlyWhatTheEventDeclares() {
        val state = wxReadAdapterStateChange(fakeBluetoothAdapterStateChangeResult(available = true, discovering = false))

        assertEquals(true, state?.available)
        assertEquals(false, state?.discovering)
        // The installed base library's schema for this event does not include `powered`,
        // so the SDK does not invent it from the query's shape.
        assertNull(state?.powered)
    }

    @Test
    fun aDeviceBatchIsReadWithTheHostsOwnFieldNames() {
        val batch = wxReadDeviceBatch(
            fakeBluetoothDeviceFoundResult(
                arrayOf(
                    fakeBluetoothDeviceRecord(deviceId = "device-a", name = "Alpha", rssi = -55),
                ),
            ),
        )

        val present = assertIs<WxDeviceBatch.Present>(batch)
        assertEquals(1, present.devices.size)
        assertEquals("device-a", present.devices.single().deviceId)
        assertEquals("Alpha", present.devices.single().name)
        assertEquals(-55, present.devices.single().rssi)
        assertEquals(0, present.unreadableEntries)
    }

    @Test
    fun aDeviceWithoutANameOrSignalIsReadWithoutInventingOne() {
        val batch = wxReadDeviceBatch(
            fakeBluetoothDeviceFoundResult(arrayOf(fakeBluetoothDeviceRecord(deviceId = "device-a"))),
        )

        val device = assertIs<WxDeviceBatch.Present>(batch).devices.single()
        assertNull(device.name)
        assertNull(device.rssi)
    }

    @Test
    fun anEntryWithoutAnIdentifierIsCountedRatherThanGuessedAt() {
        val batch = wxReadDeviceBatch(
            fakeBluetoothDeviceFoundResult(
                arrayOf(
                    fakeBluetoothDeviceRecord(deviceId = null),
                    fakeBluetoothDeviceRecord(deviceId = ""),
                    fakeBluetoothDeviceRecord(deviceId = "device-a", name = "Alpha", rssi = -40),
                ),
            ),
        )

        val present = assertIs<WxDeviceBatch.Present>(batch)
        assertEquals(1, present.devices.size)
        assertEquals(2, present.unreadableEntries)
    }

    @Test
    fun aPayloadThatIsNotADeviceArrayIsUnreadable() {
        assertIs<WxDeviceBatch.Unreadable>(wxReadDeviceBatch(fakeBluetoothDeviceFoundResult(devices = null)))
        assertIs<WxDeviceBatch.Unreadable>(wxReadDeviceBatch(fakeBluetoothDeviceFoundResult(devices = "device-a")))
        assertIs<WxDeviceBatch.Unreadable>(wxReadDeviceBatch(fakeBluetoothDeviceFoundResult(devices = 42)))
    }

    @Test
    fun aConnectionStateNeedsBothTheDeviceAndTheChange() {
        val state = wxReadConnectionState(fakeBleConnectionStateResult(deviceId = "device-a", connected = true))
        assertEquals("device-a", state?.deviceId)
        assertEquals(true, state?.connected)

        assertNull(wxReadConnectionState(fakeBleConnectionStateResult(deviceId = null, connected = true)))
        assertNull(wxReadConnectionState(fakeBleConnectionStateResult(deviceId = "device-a", connected = null)))
        assertNull(wxReadConnectionState(fakeBleConnectionStateResult(deviceId = "device-a", connected = "yes")))
    }
}
