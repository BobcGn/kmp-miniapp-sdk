package io.github.bobcgn.miniapp.host.wechat.interop

import kotlin.js.jsTypeOf
import kotlin.js.unsafeCast

/**
 * Raw option bag accepted by [wx.openBluetoothAdapter].
 *
 * WeChat declares one optional field. The SDK sets nothing: the mode it offers
 * (`central` and `peripheral`) is a host capability this proof of concept does not
 * choose between, so the host's own default applies.
 */
internal external interface WxOpenBluetoothAdapterOptions {
    /** Called only when the adapter opened. */
    var success: WxGeneralCallback?

    /** Called when the adapter cannot be opened, including when Bluetooth is off. */
    var fail: WxGeneralCallback?

    /** Called after either [success] or [fail]. */
    var complete: WxGeneralCallback?
}

/** Raw option bag accepted by [wx.closeBluetoothAdapter]. */
internal external interface WxCloseBluetoothAdapterOptions {
    /** Called only when the adapter closed. */
    var success: WxGeneralCallback?

    /** Called when the adapter cannot be closed. */
    var fail: WxGeneralCallback?

    /** Called after either [success] or [fail]. */
    var complete: WxGeneralCallback?
}

/** Raw option bag accepted by [wx.getBluetoothAdapterState]. */
internal external interface WxGetBluetoothAdapterStateOptions {
    /** Called only when the host reports its adapter state. */
    var success: WxGetBluetoothAdapterStateSuccessCallback?

    /** Called when the host cannot report its adapter state. */
    var fail: WxGeneralCallback?

    /** Called after either [success] or [fail]. */
    var complete: WxGeneralCallback?
}

/**
 * Raw adapter state returned by [wx.getBluetoothAdapterState].
 *
 * `powered` is declared here and not on the change event: the installed base
 * library's own schema lists three required fields for the query and two for the
 * event, so the SDK does not pretend the event carries the third.
 */
internal external interface WxBluetoothAdapterStateResult : WxGeneralCallbackResult {
    /** Documented type is `boolean`: whether this host has an adapter. */
    val available: Any?

    /** Documented type is `boolean`: whether the adapter is scanning. */
    val discovering: Any?

    /** Documented type is `boolean`: whether the adapter is switched on. */
    val powered: Any?
}

/** Success callback accepted by [WxGetBluetoothAdapterStateOptions]. */
internal typealias WxGetBluetoothAdapterStateSuccessCallback = (WxBluetoothAdapterStateResult) -> Unit

/** Raw payload WeChat hands to an adapter state listener. */
internal external interface WxBluetoothAdapterStateChangeResult {
    /** Documented type is `boolean`: whether this host has an adapter. */
    val available: Any?

    /** Documented type is `boolean`: whether the adapter is scanning. */
    val discovering: Any?
}

/** Listener signature accepted by [wx.onBluetoothAdapterStateChange]. */
internal typealias WxBluetoothAdapterStateListener = (WxBluetoothAdapterStateChangeResult) -> Unit

/**
 * Raw option bag accepted by [wx.startBluetoothDevicesDiscovery].
 *
 * WeChat declares `services`, `allowDuplicatesKey`, `interval` and `powerLevel`.
 * The SDK sets `allowDuplicatesKey`, because its own deduplication policy is
 * documented and does not depend on the host honouring the flag, and sets nothing
 * else: `interval` batches events for a caller that asked for batching, and
 * `powerLevel` chooses a radio profile this proof of concept does not.
 */
internal external interface WxStartBluetoothDevicesDiscoveryOptions {
    /** Documented type is `boolean`: whether the host reports a device again. */
    var allowDuplicatesKey: Boolean?

    /** Called only when discovery started. */
    var success: WxGeneralCallback?

    /** Called when discovery cannot start. */
    var fail: WxGeneralCallback?

    /** Called after either [success] or [fail]. */
    var complete: WxGeneralCallback?
}

/** Raw option bag accepted by [wx.stopBluetoothDevicesDiscovery]. */
internal external interface WxStopBluetoothDevicesDiscoveryOptions {
    /** Called only when discovery stopped. */
    var success: WxGeneralCallback?

    /** Called when discovery cannot stop. */
    var fail: WxGeneralCallback?

    /** Called after either [success] or [fail]. */
    var complete: WxGeneralCallback?
}

/**
 * One device inside a discovery event.
 *
 * The field names are the host's, including `RSSI` in capitals, because these are
 * the names the payload carries.
 */
internal external interface WxBluetoothDeviceRecord {
    /** Documented type is `string`: the device's advertised name. */
    val name: Any?

    /** Documented type is `string`: the host's identifier for the device. */
    val deviceId: Any?

    /** Documented type is `number`: the reported signal strength. */
    val RSSI: Any?
}

/** Raw payload WeChat hands to a device-found listener. */
internal external interface WxBluetoothDeviceFoundResult {
    /** Documented type is `array`: the devices this event reports. */
    val devices: Any?
}

/** Listener signature accepted by [wx.onBluetoothDeviceFound]. */
internal typealias WxBluetoothDeviceFoundListener = (WxBluetoothDeviceFoundResult) -> Unit

/**
 * Raw option bag accepted by [wx.createBLEConnection].
 *
 * `timeout` is declared by WeChat and deliberately not set: this proof of concept
 * does not choose a connection timeout, so the host's own default applies.
 */
internal external interface WxCreateBleConnectionOptions {
    /** Route of the device to connect to. */
    var deviceId: String?

    /** Called only when the device connected. */
    var success: WxGeneralCallback?

    /** Called when the device cannot be connected. */
    var fail: WxGeneralCallback?

    /** Called after either [success] or [fail]. */
    var complete: WxGeneralCallback?
}

/** Raw option bag accepted by [wx.closeBLEConnection]. */
internal external interface WxCloseBleConnectionOptions {
    /** Route of the device to disconnect. */
    var deviceId: String?

    /** Called only when the device disconnected. */
    var success: WxGeneralCallback?

    /** Called when the device cannot be disconnected. */
    var fail: WxGeneralCallback?

    /** Called after either [success] or [fail]. */
    var complete: WxGeneralCallback?
}

/** Raw payload WeChat hands to a connection-state listener. */
internal external interface WxBleConnectionStateChangeResult {
    /** Documented type is `string`: the device this change is about. */
    val deviceId: Any?

    /** Documented type is `boolean`: whether the device is now connected. */
    val connected: Any?
}

/** Listener signature accepted by [wx.onBLEConnectionStateChange]. */
internal typealias WxBleConnectionStateChangeListener = (WxBleConnectionStateChangeResult) -> Unit

/** A device as read from the host's payload. */
internal data class WxBluetoothDevice(
    val deviceId: String,
    val name: String?,
    val rssi: Int?,
)

/** What one discovery event carried. */
internal sealed interface WxDeviceBatch {
    /**
     * The event named devices.
     *
     * @property devices the entries the SDK could read
     * @property unreadableEntries how many entries the payload carried that it could not
     */
    data class Present(val devices: List<WxBluetoothDevice>, val unreadableEntries: Int) : WxDeviceBatch

    /** The payload is not a shape the contract can carry. */
    data object Unreadable : WxDeviceBatch
}

/** The adapter state as read from a query result. */
internal data class WxBluetoothAdapterState(
    val available: Boolean,
    val discovering: Boolean?,
    val powered: Boolean?,
)

/** A connection-state change as read from the host's payload. */
internal data class WxBluetoothConnectionState(
    val deviceId: String,
    val connected: Boolean,
)

/**
 * Reads a `wx.getBluetoothAdapterState` success result.
 *
 * `available` is required by the host's own schema, so a missing or non-boolean
 * value is unreadable rather than assumed. The other two are reported as absent
 * when the host does not state them.
 */
internal fun wxReadAdapterState(result: WxBluetoothAdapterStateResult): WxBluetoothAdapterState? {
    val available = result.available.asBoolean() ?: return null
    return WxBluetoothAdapterState(
        available = available,
        discovering = result.discovering.asBoolean(),
        powered = result.powered.asBoolean(),
    )
}

/** Reads a `wx.onBluetoothAdapterStateChange` payload, with the same rules as the query. */
internal fun wxReadAdapterStateChange(result: WxBluetoothAdapterStateChangeResult): WxBluetoothAdapterState? {
    val available = result.available.asBoolean() ?: return null
    return WxBluetoothAdapterState(
        available = available,
        discovering = result.discovering.asBoolean(),
        powered = null,
    )
}

/**
 * Reads a `wx.onBluetoothDeviceFound` payload.
 *
 * An entry the SDK cannot read is skipped and counted rather than allowed to end
 * the stream: one malformed device in a batch is not a reason to stop reporting
 * the others. A payload that is not an object with a `devices` array is
 * [WxDeviceBatch.Unreadable], because then nothing in it can be trusted.
 */
internal fun wxReadDeviceBatch(result: WxBluetoothDeviceFoundResult): WxDeviceBatch {
    val raw = result.devices
    if (raw == null || jsTypeOf(raw) == "undefined" || jsTypeOf(raw) != "object") {
        return WxDeviceBatch.Unreadable
    }

    if (!isJavaScriptArray(raw)) return WxDeviceBatch.Unreadable

    val devices = mutableListOf<WxBluetoothDevice>()
    var unreadable = 0

    for (entry in raw.unsafeCast<Array<WxBluetoothDeviceRecord?>>()) {
        val device = entry?.let { wxReadDevice(it) }
        if (device == null) unreadable += 1 else devices += device
    }

    return WxDeviceBatch.Present(devices = devices, unreadableEntries = unreadable)
}

/** Reads one device entry, or `null` when it carries no usable identifier. */
private fun wxReadDevice(record: WxBluetoothDeviceRecord): WxBluetoothDevice? {
    val deviceId = record.deviceId.asNonBlankString() ?: return null
    val rssi = record.RSSI.asInt()
    return WxBluetoothDevice(
        deviceId = deviceId,
        name = record.name.asNonBlankString(),
        rssi = rssi,
    )
}

/**
 * Reads a `wx.onBLEConnectionStateChange` payload.
 *
 * The host names the device and states whether it is connected, so both are
 * required: without a device the change cannot be attributed, and without the
 * boolean there is no change to report.
 */
internal fun wxReadConnectionState(result: WxBleConnectionStateChangeResult): WxBluetoothConnectionState? {
    val deviceId = result.deviceId.asNonBlankString() ?: return null
    val connected = result.connected.asBoolean() ?: return null
    return WxBluetoothConnectionState(deviceId = deviceId, connected = connected)
}

/** `Array.isArray`, typed so the check needs no string interpolation. */
private val isJavaScriptArray: (Any?) -> Boolean = js("Array.isArray")

/** Reads a documented `boolean` field, or `null` when the host did not state one. */
private fun Any?.asBoolean(): Boolean? = if (this is Boolean) this else null

/** Reads a documented `number` field, or `null` when the host did not state one. */
private fun Any?.asInt(): Int? = if (this is Int) this else null

/** Reads a documented `string` field, or `null` when it is absent or blank. */
private fun Any?.asNonBlankString(): String? {
    val text = this as? String ?: return null
    return text.ifBlank { null }
}

/**
 * Whether [wx.openBluetoothAdapter] and [wx.closeBluetoothAdapter] exist.
 *
 * Both are required: an adapter that could be opened but not closed would hold a
 * host resource for the lifetime of the mini program.
 */
internal fun hasWxBluetoothAdapter(): Boolean =
    js(
        "typeof wx !== 'undefined' && typeof wx.openBluetoothAdapter === 'function' " +
            "&& typeof wx.closeBluetoothAdapter === 'function'",
    )

/** Whether [wx.getBluetoothAdapterState] exists. */
internal fun hasWxGetBluetoothAdapterState(): Boolean =
    js("typeof wx !== 'undefined' && typeof wx.getBluetoothAdapterState === 'function'")

/**
 * Whether discovery can be started, stopped, and listened to.
 *
 * The listener pair is required together: WeChat removes listeners by identity, so
 * a host that could register one without removing it would leak it.
 */
internal fun hasWxBluetoothDiscovery(): Boolean =
    js(
        "typeof wx !== 'undefined' && typeof wx.startBluetoothDevicesDiscovery === 'function' " +
            "&& typeof wx.stopBluetoothDevicesDiscovery === 'function' " +
            "&& typeof wx.onBluetoothDeviceFound === 'function' " +
            "&& typeof wx.offBluetoothDeviceFound === 'function'",
    )

/** Whether connection can be created, closed, and observed. */
internal fun hasWxBleConnection(): Boolean =
    js(
        "typeof wx !== 'undefined' && typeof wx.createBLEConnection === 'function' " +
            "&& typeof wx.closeBLEConnection === 'function' " +
            "&& typeof wx.onBLEConnectionStateChange === 'function' " +
            "&& typeof wx.offBLEConnectionStateChange === 'function'",
    )

/** Whether [wx.onBluetoothAdapterStateChange] and its removal exist. */
internal fun hasWxBluetoothAdapterStateChange(): Boolean =
    js(
        "typeof wx !== 'undefined' && typeof wx.onBluetoothAdapterStateChange === 'function' " +
            "&& typeof wx.offBluetoothAdapterStateChange === 'function'",
    )

/** Creates a plain JavaScript option bag for [wx.openBluetoothAdapter]. */
internal fun wxOpenBluetoothAdapterOptions(): WxOpenBluetoothAdapterOptions {
    val options: WxOpenBluetoothAdapterOptions = js("({})")
    return options
}

/** Creates a plain JavaScript option bag for [wx.closeBluetoothAdapter]. */
internal fun wxCloseBluetoothAdapterOptions(): WxCloseBluetoothAdapterOptions {
    val options: WxCloseBluetoothAdapterOptions = js("({})")
    return options
}

/** Creates a plain JavaScript option bag for [wx.getBluetoothAdapterState]. */
internal fun wxGetBluetoothAdapterStateOptions(): WxGetBluetoothAdapterStateOptions {
    val options: WxGetBluetoothAdapterStateOptions = js("({})")
    return options
}

/**
 * Creates a plain JavaScript option bag for [wx.startBluetoothDevicesDiscovery].
 *
 * @param allowDuplicates whether the host should report a device more than once.
 *   The SDK's own stream deduplicates, so this only decides how much work the host
 *   does; it is set explicitly so the host's default is not left to chance.
 */
internal fun wxStartBluetoothDevicesDiscoveryOptions(
    allowDuplicates: Boolean,
): WxStartBluetoothDevicesDiscoveryOptions {
    val options: WxStartBluetoothDevicesDiscoveryOptions = js("({})")
    options.allowDuplicatesKey = allowDuplicates
    return options
}

/** Creates a plain JavaScript option bag for [wx.stopBluetoothDevicesDiscovery]. */
internal fun wxStopBluetoothDevicesDiscoveryOptions(): WxStopBluetoothDevicesDiscoveryOptions {
    val options: WxStopBluetoothDevicesDiscoveryOptions = js("({})")
    return options
}

/** Creates a plain JavaScript option bag for [wx.createBLEConnection]. */
internal fun wxCreateBleConnectionOptions(deviceId: String): WxCreateBleConnectionOptions {
    val options: WxCreateBleConnectionOptions = js("({})")
    options.deviceId = deviceId
    return options
}

/** Creates a plain JavaScript option bag for [wx.closeBLEConnection]. */
internal fun wxCloseBleConnectionOptions(deviceId: String): WxCloseBleConnectionOptions {
    val options: WxCloseBleConnectionOptions = js("({})")
    options.deviceId = deviceId
    return options
}
