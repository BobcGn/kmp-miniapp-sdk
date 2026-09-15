package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.async.awaitHostCallback
import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.WeChatDeviceCapabilities

/**
 * WeChat haptics, deliberately WeChat-specific.
 *
 * The short and long vibrations are modelled and gated separately, because they
 * are two host APIs and a host may offer one without the other. Neither call
 * reports an intensity: the SDK asks for the vibration the host defines rather
 * than inventing a strength scale the host does not share.
 *
 * A successful call means the host accepted and performed the vibration. It is
 * not something the SDK can verify further, and it is not evidence that a user
 * felt anything: no software check can confirm that.
 *
 * The host offers no abort handle, so coroutine cancellation only stops the
 * caller waiting.
 */
internal class WechatHaptics(
    private val host: WechatHapticsHost = WxHapticsHost,
) {
    /**
     * Performs the host's short vibration.
     *
     * @throws MiniAppException.UnsupportedCapability when the host has no short
     *   vibration API
     * @throws MiniAppException.HostFailure when the host cannot perform it
     */
    suspend fun vibrateShort() {
        requireShortSupported()

        awaitHostCallback { success, failure ->
            host.vibrateShort(
                success = { success(Unit) },
                failure = { result ->
                    failure(mapWechatHostFailure(operation = "vibrateShort", result = result))
                },
            )
            null
        }
    }

    /**
     * Performs the host's long vibration.
     *
     * @throws MiniAppException.UnsupportedCapability when the host has no long
     *   vibration API
     * @throws MiniAppException.HostFailure when the host cannot perform it
     */
    suspend fun vibrateLong() {
        requireLongSupported()

        awaitHostCallback { success, failure ->
            host.vibrateLong(
                success = { success(Unit) },
                failure = { result ->
                    failure(mapWechatHostFailure(operation = "vibrateLong", result = result))
                },
            )
            null
        }
    }

    private fun requireShortSupported() {
        if (!host.isShortSupported()) {
            throw MiniAppException.UnsupportedCapability(WeChatDeviceCapabilities.VibrateShort)
        }
    }

    private fun requireLongSupported() {
        if (!host.isLongSupported()) {
            throw MiniAppException.UnsupportedCapability(WeChatDeviceCapabilities.VibrateLong)
        }
    }
}
