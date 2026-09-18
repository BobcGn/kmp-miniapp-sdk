package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.async.awaitHostCallback
import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.ExperimentalMiniAppBleApi
import io.github.bobcgn.miniapp.host.wechat.WeChatBleAdapterState
import io.github.bobcgn.miniapp.host.wechat.WeChatBleConnectionState
import io.github.bobcgn.miniapp.host.wechat.WeChatBleDevice
import io.github.bobcgn.miniapp.host.wechat.WeChatDeviceCapabilities
import io.github.bobcgn.miniapp.host.wechat.interop.WxBleConnectionStateChangeListener
import io.github.bobcgn.miniapp.host.wechat.interop.WxBluetoothAdapterStateListener
import io.github.bobcgn.miniapp.host.wechat.interop.WxBluetoothDeviceFoundListener
import io.github.bobcgn.miniapp.host.wechat.interop.WxDeviceBatch
import io.github.bobcgn.miniapp.host.wechat.interop.wxReadAdapterState
import io.github.bobcgn.miniapp.host.wechat.interop.wxReadAdapterStateChange
import io.github.bobcgn.miniapp.host.wechat.interop.wxReadConnectionState
import io.github.bobcgn.miniapp.host.wechat.interop.wxReadDeviceBatch
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow

/**
 * The WeChat BLE proof of concept.
 *
 * This exists to exercise the SDK's event-driven resource model against a real
 * event source, not to offer Bluetooth. Adapter open and close, discovery with its
 * event stream, and connection with its state stream are implemented; services,
 * characteristics, notifications, MTU, signal strength queries, pairing,
 * reconnection and background scanning are not.
 *
 * **Registration is paired with removal by construction.** Every stream below
 * registers exactly one host listener when it starts collecting and removes that
 * same value when collection ends, whatever ends it: normal completion, an
 * exception, or cancellation. That is the same rule the network status stream
 * follows, and it is the reason each collector owns its own registration instead
 * of sharing one: WeChat removes listeners by identity, so a shared registration
 * would need reference counting to decide when to remove it, and an error there
 * leaks a listener for the lifetime of the mini program.
 *
 * **Cancellation is not an error.** A cancelled collector ends the flow; it is
 * never turned into a [MiniAppException]. A host callback that arrives after the
 * collector ended reaches a closed channel and is discarded, which is what the
 * flow machinery does with a `trySend` that fails.
 *
 * **Cleanup failure never hides the real outcome.** If the host's removal call
 * itself fails, the failure is counted in [listenerRemovalFailures] rather than
 * thrown: throwing from cleanup would replace the caller's own result or
 * cancellation with an error about removal, and the listener count would still be
 * accurate because a listener that could not be removed is still registered.
 *
 * The adapter keeps its own open and scanning state so that closing an adapter
 * that is closed, or stopping a scan that is not running, is a no-op rather than a
 * host call that reports "already closed" as a failure.
 */
@OptIn(ExperimentalMiniAppBleApi::class)
internal class WechatBluetooth(
    private val host: WechatBluetoothHost = WxBluetoothHost,
) {
    private var isAdapterOpen: Boolean = false
    private var isScanning: Boolean = false
    private var registeredListeners: Int = 0
    private var removalFailures: Int = 0
    private var unreadableDeviceEntriesSeen: Int = 0

    /** How many host listeners this adapter currently holds. */
    val activeListenerCount: Int get() = registeredListeners

    /** How many listener removals the host rejected. */
    val listenerRemovalFailures: Int get() = removalFailures

    /** How many device entries in discovery events the adapter could not read. */
    val unreadableDeviceEntries: Int get() = unreadableDeviceEntriesSeen

    /** Whether this adapter has been opened and not closed. */
    val isOpen: Boolean get() = isAdapterOpen

    /** Whether a scan is running. */
    val isDiscoveryRunning: Boolean get() = isScanning

    /** Opens the adapter. Calling this while it is open does nothing. */
    suspend fun openAdapter(): Unit {
        if (isAdapterOpen) return
        if (!host.isAdapterSupported()) {
            throw MiniAppException.UnsupportedCapability(WeChatDeviceCapabilities.BluetoothAdapter)
        }
        awaitHostCallback<Unit> { success, failure ->
            host.openAdapter(
                success = { success(Unit) },
                failure = { result ->
                    failure(mapWechatHostFailure(operation = "openBluetoothAdapter", result = result))
                },
            )
            null
        }
        isAdapterOpen = true
    }

    /**
     * Closes the adapter, ending any scan. Calling this while it is closed does
     * nothing.
     */
    suspend fun closeAdapter(): Unit {
        if (!isAdapterOpen) return
        if (!host.isAdapterSupported()) {
            throw MiniAppException.UnsupportedCapability(WeChatDeviceCapabilities.BluetoothAdapter)
        }
        awaitHostCallback<Unit> { success, failure ->
            host.closeAdapter(
                success = { success(Unit) },
                failure = { result ->
                    failure(mapWechatHostFailure(operation = "closeBluetoothAdapter", result = result))
                },
            )
            null
        }
        isAdapterOpen = false
        isScanning = false
    }

    /** Reports what the host says about its adapter. */
    suspend fun adapterState(): WeChatBleAdapterState {
        if (!host.isAdapterStateQuerySupported()) {
            throw MiniAppException.UnsupportedCapability(WeChatDeviceCapabilities.BluetoothAdapter)
        }
        val raw = awaitHostCallback { success, failure ->
            host.adapterState(
                success = { result -> success(result) },
                failure = { result ->
                    failure(mapWechatHostFailure(operation = "getBluetoothAdapterState", result = result))
                },
            )
            null
        }
        val state = wxReadAdapterState(raw)
            ?: throw MiniAppException.InvalidResponse(
                "The WeChat host answered getBluetoothAdapterState with a state the SDK cannot read",
            )
        // The query is the only place the host states whether it is scanning, so the
        // adapter's belief follows it rather than drifting from it.
        isScanning = state.discovering ?: isScanning
        return WeChatBleAdapterState(
            available = state.available,
            discovering = state.discovering,
            powered = state.powered,
        )
    }

    /**
     * Starts scanning. Calling this while a scan is running does nothing.
     *
     * WeChat reports an already-running scan as a failure, so the adapter does not
     * ask a second time; the state it keeps is what makes a repeated call a no-op.
     *
     * @param allowDuplicates whether the host should report a device more than
     *   once. The stream below deduplicates regardless, so this only decides how
     *   much work the host does.
     */
    suspend fun startDiscovery(allowDuplicates: Boolean = false): Unit {
        if (isScanning) return
        if (!host.isDiscoverySupported()) {
            throw MiniAppException.UnsupportedCapability(WeChatDeviceCapabilities.BluetoothDiscovery)
        }
        awaitHostCallback<Unit> { success, failure ->
            host.startDiscovery(
                allowDuplicates = allowDuplicates,
                success = { success(Unit) },
                failure = { result ->
                    failure(mapWechatHostFailure(operation = "startBluetoothDevicesDiscovery", result = result))
                },
            )
            null
        }
        isScanning = true
    }

    /** Stops scanning. Calling this while no scan is running does nothing. */
    suspend fun stopDiscovery(): Unit {
        if (!isScanning) return
        if (!host.isDiscoverySupported()) {
            throw MiniAppException.UnsupportedCapability(WeChatDeviceCapabilities.BluetoothDiscovery)
        }
        awaitHostCallback<Unit> { success, failure ->
            host.stopDiscovery(
                success = { success(Unit) },
                failure = { result ->
                    failure(mapWechatHostFailure(operation = "stopBluetoothDevicesDiscovery", result = result))
                },
            )
            null
        }
        isScanning = false
    }

    /**
     * Connects to [deviceId].
     *
     * WeChat reports connection failures through the failure callback and offers no
     * abort handle, so a cancelled caller only stops waiting: the host may still
     * complete the connection.
     *
     * The installed Developer Tools base library refuses this call outright on
     * macOS (`createBLEConnection:fail API_NOT_SUPPORT`), so the simulator can
     * exercise discovery but not connection. That is a host limitation, reported as
     * a host failure, not as an SDK one.
     */
    suspend fun connect(deviceId: String): Unit {
        require(deviceId.isNotBlank()) { "connect requires a device identifier, and '$deviceId' is blank." }
        if (!host.isConnectionSupported()) {
            throw MiniAppException.UnsupportedCapability(WeChatDeviceCapabilities.BluetoothConnection)
        }
        awaitHostCallback<Unit> { success, failure ->
            host.connect(
                deviceId = deviceId,
                success = { success(Unit) },
                failure = { result ->
                    failure(mapWechatHostFailure(operation = "createBLEConnection", result = result))
                },
            )
            null
        }
    }

    /** Disconnects from [deviceId]. */
    suspend fun disconnect(deviceId: String): Unit {
        require(deviceId.isNotBlank()) { "disconnect requires a device identifier, and '$deviceId' is blank." }
        if (!host.isConnectionSupported()) {
            throw MiniAppException.UnsupportedCapability(WeChatDeviceCapabilities.BluetoothConnection)
        }
        awaitHostCallback<Unit> { success, failure ->
            host.disconnect(
                deviceId = deviceId,
                success = { success(Unit) },
                failure = { result ->
                    failure(mapWechatHostFailure(operation = "closeBLEConnection", result = result))
                },
            )
            null
        }
    }

    /**
     * The devices this collector is told about while a scan runs.
     *
     * **Duplicate policy: one event per `deviceId` per collector.** The host may
     * report the same device repeatedly — WeChat re-reports a device as its signal
     * changes — and this stream emits it once, on first sighting, with the fields
     * the host reported then. The policy is the SDK's own rather than the host's:
     * discovery is also started with `allowDuplicatesKey = false`, so the host
     * filters duplicates too, but a stream whose shape depended on a host honouring
     * that flag would be a stream that changes shape on another host.
     *
     * **Backpressure: a bounded buffer that drops the oldest events.** A collector
     * that stops keeping up must not block the host callback, so the buffer is
     * bounded at [DISCOVERY_BUFFER_CAPACITY] and the oldest pending event is
     * dropped when it is full. Nothing is buffered without limit, and no event is
     * kept for a collector that has stopped reading. A caller that needs every
     * device should keep up with the stream rather than rely on the buffer. Because
     * discovery asks the host not to repeat devices, a dropped event may be lost;
     * this proof of concept is therefore explicitly a best-effort observation, not
     * an exhaustive scan result.
     *
     * Starting this stream before a scan reports nothing, because the host reports
     * devices only while scanning.
     */
    val discoveredDevices: Flow<WeChatBleDevice> get() = discoveredDevices(onListenerRegistered = {})

    /**
     * The device stream, reporting when its host listener is registered.
     *
     * The signal exists because a collector starts on its own dispatch: a caller that
     * has to know the registration really happened — the JavaScript session, and the
     * tests that assert the listener count — would otherwise be guessing at a moment.
     * It is called from inside the collector, so it fires exactly once per collection
     * and only after the host accepted the registration.
     */
    internal fun discoveredDevices(onListenerRegistered: () -> Unit): Flow<WeChatBleDevice> = callbackFlow {
        if (!host.isDiscoverySupported()) {
            close(MiniAppException.UnsupportedCapability(WeChatDeviceCapabilities.BluetoothDiscovery))
            return@callbackFlow
        }

        val seen: MutableSet<String> = mutableSetOf()
        val listener: WxBluetoothDeviceFoundListener = { result ->
            when (val batch = wxReadDeviceBatch(result)) {
                is WxDeviceBatch.Present -> {
                    unreadableDeviceEntriesSeen += batch.unreadableEntries
                    batch.devices.forEach { device ->
                        // A duplicate is discarded here rather than counted: the
                        // policy is about what the stream carries, and counting
                        // duplicates per collector would be state no caller reads.
                        if (seen.add(device.deviceId)) {
                            trySend(WeChatBleDevice(device.deviceId, device.name, device.rssi))
                        }
                    }
                }

                // A payload the SDK cannot read at all is a host that broke its own
                // contract; ending the stream says so instead of reporting silence.
                WxDeviceBatch.Unreadable -> close(
                    MiniAppException.InvalidResponse(
                        "The WeChat host reported a discovery event the SDK cannot read",
                    ),
                )
            }
        }

        host.addDeviceFoundListener(listener)
        registeredListeners += 1
        onListenerRegistered()
        awaitClose { removeListener { host.removeDeviceFoundListener(listener) } }
    }.buffer(capacity = DISCOVERY_BUFFER_CAPACITY, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    /**
     * What the host reports about its adapter while this collector is active.
     *
     * The same registration rule and the same bounded-buffer policy as
     * [discoveredDevices] apply; the buffer is smaller because adapter state
     * changes are rare and only the newest matters.
     */
    val adapterStates: Flow<WeChatBleAdapterState> = callbackFlow {
        if (!host.isAdapterStateChangeSupported()) {
            close(MiniAppException.UnsupportedCapability(WeChatDeviceCapabilities.BluetoothAdapter))
            return@callbackFlow
        }

        val listener: WxBluetoothAdapterStateListener = { result ->
            val state = wxReadAdapterStateChange(result)
            if (state == null) {
                close(
                    MiniAppException.InvalidResponse(
                        "The WeChat host reported an adapter state the SDK cannot read",
                    ),
                )
            } else {
                if (state.discovering != null) isScanning = state.discovering
                trySend(
                    WeChatBleAdapterState(
                        available = state.available,
                        discovering = state.discovering,
                        powered = state.powered,
                    ),
                )
            }
        }

        host.addAdapterStateListener(listener)
        registeredListeners += 1
        awaitClose { removeListener { host.removeAdapterStateListener(listener) } }
    }.buffer(capacity = STATE_BUFFER_CAPACITY, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    /**
     * Connection state changes for any device.
     *
     * WeChat's event is not per device; it names the device each change belongs to.
     * The same registration rule and buffer policy as [discoveredDevices] apply.
     */
    val connectionStates: Flow<WeChatBleConnectionState> get() = connectionStates(onListenerRegistered = {})

    /** The connection-state stream, reporting when its host listener is registered. */
    internal fun connectionStates(onListenerRegistered: () -> Unit): Flow<WeChatBleConnectionState> = callbackFlow {
        if (!host.isConnectionSupported()) {
            close(MiniAppException.UnsupportedCapability(WeChatDeviceCapabilities.BluetoothConnection))
            return@callbackFlow
        }

        val listener: WxBleConnectionStateChangeListener = { result ->
            val state = wxReadConnectionState(result)
            if (state == null) {
                close(
                    MiniAppException.InvalidResponse(
                        "The WeChat host reported a connection state the SDK cannot read",
                    ),
                )
            } else {
                trySend(WeChatBleConnectionState(deviceId = state.deviceId, connected = state.connected))
            }
        }

        host.addConnectionStateListener(listener)
        registeredListeners += 1
        onListenerRegistered()
        awaitClose { removeListener { host.removeConnectionStateListener(listener) } }
    }.buffer(capacity = DISCOVERY_BUFFER_CAPACITY, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    /**
     * Removes one listener and keeps the account of them honest.
     *
     * The count decreases only when the host accepted the removal. A removal that
     * failed leaves the listener registered as far as anyone can tell, so the count
     * says so and the failure is counted rather than thrown: this runs while a flow
     * is ending, and throwing here would replace the caller's result or
     * cancellation with an error about cleanup.
     */
    private fun removeListener(remove: () -> Unit) {
        val removed = runCatching { remove() }.isSuccess
        if (removed) {
            registeredListeners -= 1
        } else {
            removalFailures += 1
        }
    }

    private companion object {
        /**
         * How many discovery events a collector that stops reading may leave pending.
         *
         * Bounded so a collector that stops consuming cannot make the SDK hold
         * events without limit.
         */
        const val DISCOVERY_BUFFER_CAPACITY: Int = 64

        /** How many adapter state changes may be pending; only the newest matters. */
        const val STATE_BUFFER_CAPACITY: Int = 8
    }
}
