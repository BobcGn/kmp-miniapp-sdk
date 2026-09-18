@file:OptIn(ExperimentalJsExport::class)

package io.github.bobcgn.miniapp.export

import io.github.bobcgn.miniapp.host.wechat.ExperimentalMiniAppBleApi
import io.github.bobcgn.miniapp.host.wechat.WeChatBleAdapterState
import io.github.bobcgn.miniapp.host.wechat.WeChatBleConnectionState
import io.github.bobcgn.miniapp.host.wechat.WeChatBleDevice
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * One discovered device, as a JavaScript caller sees it.
 *
 * The identifier is the host's own value. On some platforms it is a MAC address,
 * so a consumer must treat it as identifying: log a shortened or session-local
 * form, never the value itself.
 */
@ExperimentalMiniAppBleApi
@JsExport
public class JsBleDevice internal constructor(
    /** The host's identifier for this device. */
    public val deviceId: String,
    /** The device's advertised name, or `null` when it advertised none. */
    public val name: String?,
    /** The reported signal strength, or `null` when the host reported none. */
    public val rssi: Int?,
)

/**
 * What the host reports about its Bluetooth adapter.
 *
 * `discovering` and `powered` are `null` when the source of the state does not
 * report them, which is not the same as `false`.
 */
@ExperimentalMiniAppBleApi
@JsExport
public class JsBleAdapterState internal constructor(
    /** Whether this host has a Bluetooth adapter at all. */
    public val available: Boolean,
    /** Whether the adapter is scanning, or `null` when unstated. */
    public val discovering: Boolean?,
    /** Whether the adapter is switched on, or `null` when unstated. */
    public val powered: Boolean?,
)

/** One connection state change, as a JavaScript caller sees it. */
@ExperimentalMiniAppBleApi
@JsExport
public class JsBleConnectionState internal constructor(
    /** The host's identifier for the device this change is about. */
    public val deviceId: String,
    /** Whether that device is now connected. */
    public val connected: Boolean,
)

/** Maps a device onto the JavaScript-facing shape. */
@ExperimentalMiniAppBleApi
internal fun WeChatBleDevice.toJs(): JsBleDevice =
    JsBleDevice(deviceId = deviceId, name = name, rssi = rssi)

/** Maps an adapter state onto the JavaScript-facing shape. */
@ExperimentalMiniAppBleApi
internal fun WeChatBleAdapterState.toJs(): JsBleAdapterState =
    JsBleAdapterState(available = available, discovering = discovering, powered = powered)

/** Maps a connection state onto the JavaScript-facing shape. */
@ExperimentalMiniAppBleApi
internal fun WeChatBleConnectionState.toJs(): JsBleConnectionState =
    JsBleConnectionState(deviceId = deviceId, connected = connected)
