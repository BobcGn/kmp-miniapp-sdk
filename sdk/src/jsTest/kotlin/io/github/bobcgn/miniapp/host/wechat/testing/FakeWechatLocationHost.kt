package io.github.bobcgn.miniapp.host.wechat.testing

import io.github.bobcgn.miniapp.host.wechat.WeChatCoordinateSystem
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatLocationHost
import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxGetLocationSuccessResult

/**
 * The FakeAdapter boundary for location: a stand-in for the `wx.getLocation`
 * callbacks that replies immediately with one scripted outcome.
 *
 * It implements the raw callback port, so a test can drive `WechatLocation`
 * without a WeChat runtime and can reproduce conditions a real host produces
 * rarely, such as a missing coordinate or a non-finite one.
 *
 * It records the coordinate system it was asked for, because the SDK must not
 * silently take the host default.
 */
internal class FakeWechatLocationHost(
    var supported: Boolean = true,
    private val latitude: Any? = 31.0,
    private val longitude: Any? = 121.0,
    private val accuracy: Any? = 12.0,
) : WechatLocationHost {
    /** Message `getLocation` fails with, or `null` to answer with a position. */
    var failureMessage: String? = null

    /** When true, `getLocation` reports its outcome twice, as a defective host might. */
    var completeTwice: Boolean = false

    var calls: Int = 0
        private set

    /** The coordinate system last requested. */
    var lastCoordinateSystem: WeChatCoordinateSystem? = null
        private set

    override fun isSupported(): Boolean = supported

    override fun currentPosition(
        coordinateSystem: WeChatCoordinateSystem,
        success: (WxGetLocationSuccessResult) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        calls += 1
        lastCoordinateSystem = coordinateSystem

        val complete = {
            val message = failureMessage
            if (message == null) {
                success(fakeGetLocationSuccess(latitude, longitude, accuracy))
            } else {
                failure(fakeWxFailure(message))
            }
        }

        complete()
        if (completeTwice) complete()
    }
}
