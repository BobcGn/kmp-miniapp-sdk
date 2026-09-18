package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.host.wechat.interop.WxBleConnectionStateChangeListener
import io.github.bobcgn.miniapp.host.wechat.interop.WxBluetoothAdapterStateListener
import io.github.bobcgn.miniapp.host.wechat.interop.WxBluetoothAdapterStateResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxBluetoothDeviceFoundListener
import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.interop.hasWxBleConnection
import io.github.bobcgn.miniapp.host.wechat.interop.hasWxBluetoothAdapter
import io.github.bobcgn.miniapp.host.wechat.interop.hasWxBluetoothAdapterStateChange
import io.github.bobcgn.miniapp.host.wechat.interop.hasWxBluetoothDiscovery
import io.github.bobcgn.miniapp.host.wechat.interop.hasWxGetBluetoothAdapterState
import io.github.bobcgn.miniapp.host.wechat.interop.wx
import io.github.bobcgn.miniapp.host.wechat.interop.wxCloseBleConnectionOptions
import io.github.bobcgn.miniapp.host.wechat.interop.wxCloseBluetoothAdapterOptions
import io.github.bobcgn.miniapp.host.wechat.interop.wxCreateBleConnectionOptions
import io.github.bobcgn.miniapp.host.wechat.interop.wxGetBluetoothAdapterStateOptions
import io.github.bobcgn.miniapp.host.wechat.interop.wxOpenBluetoothAdapterOptions
import io.github.bobcgn.miniapp.host.wechat.interop.wxStartBluetoothDevicesDiscoveryOptions
import io.github.bobcgn.miniapp.host.wechat.interop.wxStopBluetoothDevicesDiscoveryOptions

/**
 * Callback port that keeps the Bluetooth adapter independently testable.
 *
 * The listener methods take the listener as a value rather than returning a handle,
 * because WeChat removes a listener by identity: whatever is registered must be the
 * value handed back for removal, and a port that hid that could not be tested for
 * the pairing this proof of concept exists to verify.
 */
internal interface WechatBluetoothHost {
    /** Whether the adapter can be opened and closed. */
    fun isAdapterSupported(): Boolean

    /** Whether the adapter's state can be queried. */
    fun isAdapterStateQuerySupported(): Boolean

    /** Whether discovery can be started, stopped, and observed. */
    fun isDiscoverySupported(): Boolean

    /** Whether a connection can be created, closed, and observed. */
    fun isConnectionSupported(): Boolean

    /** Whether adapter state changes can be observed. */
    fun isAdapterStateChangeSupported(): Boolean

    fun openAdapter(success: () -> Unit, failure: (WxGeneralCallbackResult) -> Unit): Unit

    fun closeAdapter(success: () -> Unit, failure: (WxGeneralCallbackResult) -> Unit): Unit

    fun adapterState(
        success: (WxBluetoothAdapterStateResult) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ): Unit

    fun startDiscovery(
        allowDuplicates: Boolean,
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ): Unit

    fun stopDiscovery(success: () -> Unit, failure: (WxGeneralCallbackResult) -> Unit): Unit

    fun addDeviceFoundListener(listener: WxBluetoothDeviceFoundListener): Unit

    fun removeDeviceFoundListener(listener: WxBluetoothDeviceFoundListener): Unit

    fun addAdapterStateListener(listener: WxBluetoothAdapterStateListener): Unit

    fun removeAdapterStateListener(listener: WxBluetoothAdapterStateListener): Unit

    fun connect(
        deviceId: String,
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ): Unit

    fun disconnect(
        deviceId: String,
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ): Unit

    fun addConnectionStateListener(listener: WxBleConnectionStateChangeListener): Unit

    fun removeConnectionStateListener(listener: WxBleConnectionStateChangeListener): Unit
}

/** Production Bluetooth port backed directly by the global WeChat API. */
internal object WxBluetoothHost : WechatBluetoothHost {
    override fun isAdapterSupported(): Boolean = hasWxBluetoothAdapter()

    override fun isAdapterStateQuerySupported(): Boolean = hasWxGetBluetoothAdapterState()

    override fun isDiscoverySupported(): Boolean = hasWxBluetoothDiscovery()

    override fun isConnectionSupported(): Boolean = hasWxBleConnection()

    override fun isAdapterStateChangeSupported(): Boolean = hasWxBluetoothAdapterStateChange()

    override fun openAdapter(success: () -> Unit, failure: (WxGeneralCallbackResult) -> Unit) {
        val options = wxOpenBluetoothAdapterOptions()
        options.success = { success() }
        options.fail = failure
        wx.openBluetoothAdapter(options)
    }

    override fun closeAdapter(success: () -> Unit, failure: (WxGeneralCallbackResult) -> Unit) {
        val options = wxCloseBluetoothAdapterOptions()
        options.success = { success() }
        options.fail = failure
        wx.closeBluetoothAdapter(options)
    }

    override fun adapterState(
        success: (WxBluetoothAdapterStateResult) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        val options = wxGetBluetoothAdapterStateOptions()
        options.success = success
        options.fail = failure
        wx.getBluetoothAdapterState(options)
    }

    override fun startDiscovery(
        allowDuplicates: Boolean,
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        val options = wxStartBluetoothDevicesDiscoveryOptions(allowDuplicates)
        options.success = { success() }
        options.fail = failure
        wx.startBluetoothDevicesDiscovery(options)
    }

    override fun stopDiscovery(success: () -> Unit, failure: (WxGeneralCallbackResult) -> Unit) {
        val options = wxStopBluetoothDevicesDiscoveryOptions()
        options.success = { success() }
        options.fail = failure
        wx.stopBluetoothDevicesDiscovery(options)
    }

    override fun addDeviceFoundListener(listener: WxBluetoothDeviceFoundListener): Unit =
        wx.onBluetoothDeviceFound(listener)

    override fun removeDeviceFoundListener(listener: WxBluetoothDeviceFoundListener): Unit =
        wx.offBluetoothDeviceFound(listener)

    override fun addAdapterStateListener(listener: WxBluetoothAdapterStateListener): Unit =
        wx.onBluetoothAdapterStateChange(listener)

    override fun removeAdapterStateListener(listener: WxBluetoothAdapterStateListener): Unit =
        wx.offBluetoothAdapterStateChange(listener)

    override fun connect(
        deviceId: String,
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        val options = wxCreateBleConnectionOptions(deviceId)
        options.success = { success() }
        options.fail = failure
        wx.createBLEConnection(options)
    }

    override fun disconnect(
        deviceId: String,
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        val options = wxCloseBleConnectionOptions(deviceId)
        options.success = { success() }
        options.fail = failure
        wx.closeBLEConnection(options)
    }

    override fun addConnectionStateListener(listener: WxBleConnectionStateChangeListener): Unit =
        wx.onBLEConnectionStateChange(listener)

    override fun removeConnectionStateListener(listener: WxBleConnectionStateChangeListener): Unit =
        wx.offBLEConnectionStateChange(listener)
}
