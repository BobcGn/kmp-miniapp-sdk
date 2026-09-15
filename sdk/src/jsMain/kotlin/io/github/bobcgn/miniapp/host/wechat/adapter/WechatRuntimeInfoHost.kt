package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.host.wechat.interop.hasWxCanIUse
import io.github.bobcgn.miniapp.host.wechat.interop.hasWxGetAppBaseInfo
import io.github.bobcgn.miniapp.host.wechat.interop.hasWxGetDeviceInfo
import io.github.bobcgn.miniapp.host.wechat.interop.hasWxGetSystemInfoSync
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
}
