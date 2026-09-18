package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.host.wechat.interop.hasWxBleConnection
import io.github.bobcgn.miniapp.host.wechat.interop.hasWxBluetoothAdapter
import io.github.bobcgn.miniapp.host.wechat.interop.hasWxBluetoothDiscovery
import io.github.bobcgn.miniapp.host.wechat.interop.hasWxCanIUse
import io.github.bobcgn.miniapp.host.wechat.interop.hasWxGetBluetoothAdapterState
import io.github.bobcgn.miniapp.host.wechat.interop.hasWxFileSystemMethod
import io.github.bobcgn.miniapp.host.wechat.interop.hasWxGetAppBaseInfo
import io.github.bobcgn.miniapp.host.wechat.interop.hasWxGetDeviceInfo
import io.github.bobcgn.miniapp.host.wechat.interop.hasWxGetSystemInfoSync
import io.github.bobcgn.miniapp.host.wechat.interop.hasWxUserDataPath
import io.github.bobcgn.miniapp.host.wechat.interop.wx

/**
 * Port that keeps runtime inspection independently testable.
 *
 * Each member answers "cannot be determined" rather than throwing, because a
 * WeChat base library is not obliged to expose any of this.
 */
internal interface WechatRuntimeInfoHost {
    /** Base-library version as the host reports it, or `null` when it cannot be read. */
    fun baseLibraryVersion(): String?

    /** Runtime platform as the host reports it, or `null` when it cannot be read. */
    fun platform(): String?

    /**
     * Reports whether [schema] is available in this base library.
     *
     * Answers `false` when the host cannot be asked at all, so an unprobeable host
     * fails closed instead of letting a call fail later.
     */
    fun canIUse(schema: String): Boolean

    /**
     * Whether a method of the host's file manager is present.
     *
     * The file manager is a host object rather than a `wx` API, so `wx.canIUse`
     * cannot answer for its members. Asking here keeps the capability gate and the
     * file-system adapter reading the same guard: a capability cannot be reported
     * supported while the call the adapter would make is missing.
     */
    fun hasFileSystemMethod(method: String): Boolean

    /** Whether the host reports a sandbox root for user files. */
    fun hasUserDataPath(): Boolean

    /**
     * Whether the host can open and close a Bluetooth adapter and report its state.
     *
     * Probed here rather than through `wx.canIUse` because the capability asks for
     * a group of members, and a host that offered only some of them would leave the
     * adapter either unopenable or uncloseable.
     */
    fun hasBluetoothAdapter(): Boolean

    /** Whether the host can start, stop, and report a device scan. */
    fun hasBluetoothDiscovery(): Boolean

    /** Whether the host can create, close, and observe a device connection. */
    fun hasBluetoothConnection(): Boolean
}

/** Production runtime port backed directly by the global WeChat API. */
internal object WxRuntimeInfoHost : WechatRuntimeInfoHost {
    override fun baseLibraryVersion(): String? {
        if (hasWxGetAppBaseInfo()) {
            val version = wx.getAppBaseInfo().SDKVersion
            if (!version.isNullOrBlank()) return version
        }
        // A base library that predates getAppBaseInfo has no other way to report
        // its version, so the unmaintained call remains the fallback there.
        if (hasWxGetSystemInfoSync()) {
            val version = wx.getSystemInfoSync().SDKVersion
            if (!version.isNullOrBlank()) return version
        }
        return null
    }

    override fun platform(): String? {
        if (hasWxGetDeviceInfo()) {
            val value = wx.getDeviceInfo().platform
            if (!value.isNullOrBlank()) return value
        }
        if (hasWxGetSystemInfoSync()) {
            val value = wx.getSystemInfoSync().platform
            if (!value.isNullOrBlank()) return value
        }
        return null
    }

    override fun canIUse(schema: String): Boolean = hasWxCanIUse() && wx.canIUse(schema)

    override fun hasFileSystemMethod(method: String): Boolean = hasWxFileSystemMethod(method)

    override fun hasUserDataPath(): Boolean = hasWxUserDataPath()

    override fun hasBluetoothAdapter(): Boolean =
        hasWxBluetoothAdapter() && hasWxGetBluetoothAdapterState()

    override fun hasBluetoothDiscovery(): Boolean = hasWxBluetoothDiscovery()

    override fun hasBluetoothConnection(): Boolean = hasWxBleConnection()
}
