package io.github.bobcgn.miniapp.host.wechat.testing

import io.github.bobcgn.miniapp.host.wechat.adapter.WechatBluetoothHost
import io.github.bobcgn.miniapp.host.wechat.interop.WxBleConnectionStateChangeListener
import io.github.bobcgn.miniapp.host.wechat.interop.WxBluetoothAdapterStateListener
import io.github.bobcgn.miniapp.host.wechat.interop.WxBluetoothAdapterStateResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxBluetoothDeviceFoundListener
import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult

/**
 * The FakeAdapter boundary for Bluetooth: a stand-in for WeChat's adapter, discovery,
 * and connection callbacks and for its `on`/`off` listener pairs.
 *
 * It implements the raw callback port, so a test can drive the adapter without a
 * WeChat runtime and can reproduce conditions a real host produces rarely: a device
 * payload of the wrong type, a removal that fails, or a callback that arrives after
 * the caller stopped listening.
 *
 * It keeps every registered listener and every removed one, so a test can assert the
 * pairing by identity rather than only by count. Identity is the whole point: WeChat
 * removes a listener by the value it was given, so a removal that passed a different
 * value would silently leave the registration in place.
 *
 * @param adapterSupported whether the adapter can be opened and closed
 * @param discoverySupported whether discovery can be started, stopped, and observed
 * @param connectionSupported whether connection can be created, closed, and observed
 * @param adapterStateChangeSupported whether adapter state changes can be observed
 */
internal class FakeWechatBluetoothHost(
    var adapterSupported: Boolean = true,
    var discoverySupported: Boolean = true,
    var connectionSupported: Boolean = true,
    var adapterStateChangeSupported: Boolean = true,
    var adapterStateQuerySupported: Boolean = true,
) : WechatBluetoothHost {
    /** Message every open fails with, or `null` to succeed. */
    var openFailure: String? = null

    /** Message every close fails with, or `null` to succeed. */
    var closeFailure: String? = null

    /** Message every scan start fails with, or `null` to succeed. */
    var startDiscoveryFailure: String? = null

    /** Message every scan stop fails with, or `null` to succeed. */
    var stopDiscoveryFailure: String? = null

    /** Message every connect fails with, or `null` to succeed. */
    var connectFailure: String? = null

    /** Message every disconnect fails with, or `null` to succeed. */
    var disconnectFailure: String? = null

    /** When true, removing any listener throws, standing in for a host that refuses. */
    var removalFails: Boolean = false

    /** The adapter state the query answers with, typed loosely so a test can malform it. */
    var adapterStateAvailable: Any? = true
    var adapterStateDiscovering: Any? = false
    var adapterStatePowered: Any? = true

    /** Message the adapter state query fails with, or `null` to succeed. */
    var adapterStateQueryFailure: String? = null

    var openCalls: Int = 0
        private set
    var closeCalls: Int = 0
        private set
    var startDiscoveryCalls: Int = 0
        private set
    var stopDiscoveryCalls: Int = 0
        private set
    var adapterStateQueryCalls: Int = 0
        private set
    var connectCalls: MutableList<String> = mutableListOf()
        private set
    var disconnectCalls: MutableList<String> = mutableListOf()
        private set

    /** Whether the last scan start asked the host to report duplicates. */
    var lastAllowDuplicates: Boolean? = null
        private set

    /** Every device-found listener registered, in order. */
    val registeredDeviceListeners: MutableList<WxBluetoothDeviceFoundListener> = mutableListOf()

    /** Every device-found listener the adapter asked to remove, in order. */
    val removedDeviceListeners: MutableList<WxBluetoothDeviceFoundListener> = mutableListOf()

    val registeredConnectionListeners: MutableList<WxBleConnectionStateChangeListener> = mutableListOf()
    val removedConnectionListeners: MutableList<WxBleConnectionStateChangeListener> = mutableListOf()
    val registeredAdapterStateListeners: MutableList<WxBluetoothAdapterStateListener> = mutableListOf()
    val removedAdapterStateListeners: MutableList<WxBluetoothAdapterStateListener> = mutableListOf()

    override fun isAdapterSupported(): Boolean = adapterSupported

    override fun isAdapterStateQuerySupported(): Boolean = adapterStateQuerySupported

    override fun isDiscoverySupported(): Boolean = discoverySupported

    override fun isConnectionSupported(): Boolean = connectionSupported

    override fun isAdapterStateChangeSupported(): Boolean = adapterStateChangeSupported

    override fun openAdapter(success: () -> Unit, failure: (WxGeneralCallbackResult) -> Unit) {
        openCalls += 1
        respond(openFailure, success, failure)
    }

    override fun closeAdapter(success: () -> Unit, failure: (WxGeneralCallbackResult) -> Unit) {
        closeCalls += 1
        respond(closeFailure, success, failure)
    }

    override fun adapterState(
        success: (WxBluetoothAdapterStateResult) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        adapterStateQueryCalls += 1
        val message = adapterStateQueryFailure
        if (message != null) {
            failure(fakeWxFailure(message))
            return
        }
        success(fakeBluetoothAdapterStateResult(adapterStateAvailable, adapterStateDiscovering, adapterStatePowered))
    }

    override fun startDiscovery(
        allowDuplicates: Boolean,
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        startDiscoveryCalls += 1
        lastAllowDuplicates = allowDuplicates
        respond(startDiscoveryFailure, success, failure)
    }

    override fun stopDiscovery(success: () -> Unit, failure: (WxGeneralCallbackResult) -> Unit) {
        stopDiscoveryCalls += 1
        respond(stopDiscoveryFailure, success, failure)
    }

    override fun addDeviceFoundListener(listener: WxBluetoothDeviceFoundListener) {
        registeredDeviceListeners += listener
    }

    override fun removeDeviceFoundListener(listener: WxBluetoothDeviceFoundListener) {
        if (removalFails) throw IllegalStateException("The fake host refuses to remove a listener")
        removedDeviceListeners += listener
    }

    override fun addAdapterStateListener(listener: WxBluetoothAdapterStateListener) {
        registeredAdapterStateListeners += listener
    }

    override fun removeAdapterStateListener(listener: WxBluetoothAdapterStateListener) {
        if (removalFails) throw IllegalStateException("The fake host refuses to remove a listener")
        removedAdapterStateListeners += listener
    }

    override fun connect(deviceId: String, success: () -> Unit, failure: (WxGeneralCallbackResult) -> Unit) {
        connectCalls += deviceId
        respond(connectFailure, success, failure)
    }

    override fun disconnect(deviceId: String, success: () -> Unit, failure: (WxGeneralCallbackResult) -> Unit) {
        disconnectCalls += deviceId
        respond(disconnectFailure, success, failure)
    }

    override fun addConnectionStateListener(listener: WxBleConnectionStateChangeListener) {
        registeredConnectionListeners += listener
    }

    override fun removeConnectionStateListener(listener: WxBleConnectionStateChangeListener) {
        if (removalFails) throw IllegalStateException("The fake host refuses to remove a listener")
        removedConnectionListeners += listener
    }

    /** Sends a discovery event carrying [deviceIds] to every registered listener. */
    fun emitDevices(vararg deviceIds: String, rssi: Int? = -40) {
        val devices = deviceIds.map { fakeBluetoothDeviceRecord(deviceId = it, name = "device-$it", rssi = rssi) }
        val payload = fakeBluetoothDeviceFoundResult(devices.toTypedArray())
        registeredDeviceListeners.toList().forEach { it(payload) }
    }

    /** Sends a discovery event whose `devices` field is not an array, as a broken host might. */
    fun emitUnreadableDevices() {
        val payload = fakeBluetoothDeviceFoundResult(devices = null)
        registeredDeviceListeners.toList().forEach { it(payload) }
    }

    /** Sends a discovery event carrying one entry without a usable identifier. */
    fun emitDeviceWithoutIdentifier() {
        val payload = fakeBluetoothDeviceFoundResult(
            arrayOf(fakeBluetoothDeviceRecord(deviceId = null, name = "nameless", rssi = -50)),
        )
        registeredDeviceListeners.toList().forEach { it(payload) }
    }

    /** Sends a connection state change to every registered listener. */
    fun emitConnectionState(deviceId: String, connected: Boolean) {
        val payload = fakeBleConnectionStateResult(deviceId = deviceId, connected = connected)
        registeredConnectionListeners.toList().forEach { it(payload) }
    }

    /** Sends a connection state change with no device identifier. */
    fun emitConnectionStateWithoutDevice() {
        val payload = fakeBleConnectionStateResult(deviceId = null, connected = true)
        registeredConnectionListeners.toList().forEach { it(payload) }
    }

    /** Sends an adapter state change to every registered listener. */
    fun emitAdapterState(available: Boolean = true, discovering: Boolean = true) {
        val payload = fakeBluetoothAdapterStateChangeResult(available = available, discovering = discovering)
        registeredAdapterStateListeners.toList().forEach { it(payload) }
    }

    private fun respond(message: String?, success: () -> Unit, failure: (WxGeneralCallbackResult) -> Unit) {
        if (message == null) success() else failure(fakeWxFailure(message))
    }
}
