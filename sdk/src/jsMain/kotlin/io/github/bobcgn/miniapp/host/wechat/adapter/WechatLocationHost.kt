package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.host.wechat.WeChatCoordinateSystem
import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxGetLocationSuccessResult
import io.github.bobcgn.miniapp.host.wechat.interop.hasWxGetLocation
import io.github.bobcgn.miniapp.host.wechat.interop.wx
import io.github.bobcgn.miniapp.host.wechat.interop.wxGetLocationOptions

/** Callback port that keeps [WechatLocation] independently testable. */
internal interface WechatLocationHost {
    /** Whether this host exposes the position API at all. */
    fun isSupported(): Boolean

    /** Obtains a position in [coordinateSystem] through callbacks. */
    fun currentPosition(
        coordinateSystem: WeChatCoordinateSystem,
        success: (WxGetLocationSuccessResult) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ): Unit
}

/**
 * Production location port backed directly by the global WeChat API.
 *
 * WeChat reports no task handle for `getLocation`, so there is nothing to abort
 * and this returns none.
 */
internal object WxLocationHost : WechatLocationHost {
    override fun isSupported(): Boolean = hasWxGetLocation()

    override fun currentPosition(
        coordinateSystem: WeChatCoordinateSystem,
        success: (WxGetLocationSuccessResult) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        val options = wxGetLocationOptions(coordinateSystem.hostValue)
        options.success = success
        options.fail = failure
        wx.getLocation(options)
    }
}
