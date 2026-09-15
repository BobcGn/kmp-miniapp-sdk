package io.github.bobcgn.miniapp.host.wechat.testing

import io.github.bobcgn.miniapp.host.wechat.adapter.WechatHapticsHost
import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult

/**
 * The FakeAdapter boundary for haptics: a stand-in for the WeChat `vibrateShort`
 * and `vibrateLong` callbacks.
 *
 * It implements the raw callback port, so a test can drive `WechatHaptics`
 * without a WeChat runtime and can reproduce a host that offers one vibration
 * without the other. It records which call was made, because the two vibrations
 * must not be able to stand in for each other.
 *
 * A successful call here says only that the fake accepted it. It is not evidence
 * that any device vibrated, which no Node test can produce.
 *
 * @param shortSupported whether this host exposes the short vibration API
 * @param longSupported whether this host exposes the long vibration API
 */
internal class FakeWechatHapticsHost(
    var shortSupported: Boolean = true,
    var longSupported: Boolean = true,
) : WechatHapticsHost {
    /** Message `vibrateShort` fails with, or `null` to succeed. */
    var shortFailure: String? = null

    /** Message `vibrateLong` fails with, or `null` to succeed. */
    var longFailure: String? = null

    var shortCalls: Int = 0
        private set

    var longCalls: Int = 0
        private set

    override fun isShortSupported(): Boolean = shortSupported

    override fun isLongSupported(): Boolean = longSupported

    override fun vibrateShort(
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        shortCalls += 1
        report(shortFailure, success, failure)
    }

    override fun vibrateLong(
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        longCalls += 1
        report(longFailure, success, failure)
    }

    private fun report(
        message: String?,
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        if (message == null) {
            success()
        } else {
            failure(fakeWxFailure(message))
        }
    }
}
