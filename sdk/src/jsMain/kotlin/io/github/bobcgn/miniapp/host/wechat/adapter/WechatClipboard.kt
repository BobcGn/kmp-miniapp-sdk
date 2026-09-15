package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.async.awaitHostCallback
import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.WeChatDeviceCapabilities
import io.github.bobcgn.miniapp.host.wechat.interop.WxClipboardText
import io.github.bobcgn.miniapp.host.wechat.interop.wxClipboardText

/**
 * WeChat system-clipboard access, deliberately WeChat-specific.
 *
 * Reading and writing are separate capabilities and are gated separately, because
 * a host may expose one without the other. Neither call keeps a copy of the text:
 * what the clipboard holds is the user's, not the SDK's.
 *
 * The host offers no abort handle for either call, so coroutine cancellation only
 * stops the caller waiting.
 */
internal class WechatClipboard(
    private val host: WechatClipboardHost = WxClipboardHost,
) {
    /**
     * Reads the system clipboard as text.
     *
     * An empty string is a value the clipboard can legitimately hold, so it is
     * returned as-is. A missing or non-string answer is not a value the contract
     * carries, and is reported as such rather than being coerced to empty.
     *
     * @throws MiniAppException.UnsupportedCapability when the host has no clipboard
     *   read API
     * @throws MiniAppException.InvalidResponse when the host answers with something
     *   other than text
     * @throws MiniAppException.HostFailure when the read fails
     */
    suspend fun readText(): String {
        requireReadSupported()

        val text = awaitHostCallback { success, failure ->
            host.read(
                success = { result -> success(wxClipboardText(result)) },
                failure = { result ->
                    failure(mapWechatHostFailure(operation = "getClipboardData", result = result))
                },
            )
            null
        }

        return when (text) {
            is WxClipboardText.Present -> text.text
            WxClipboardText.Unreadable -> throw MiniAppException.InvalidResponse(
                "The WeChat host answered getClipboardData with a value the SDK cannot read",
            )
        }
    }

    /**
     * Replaces the system clipboard with [data].
     *
     * @throws MiniAppException.UnsupportedCapability when the host has no clipboard
     *   write API
     * @throws MiniAppException.HostFailure when the write fails
     */
    suspend fun writeText(data: String) {
        requireWriteSupported()

        awaitHostCallback { success, failure ->
            host.write(
                data = data,
                success = { success(Unit) },
                failure = { result ->
                    failure(mapWechatHostFailure(operation = "setClipboardData", result = result))
                },
            )
            null
        }
    }

    private fun requireReadSupported() {
        if (!host.isReadSupported()) {
            throw MiniAppException.UnsupportedCapability(WeChatDeviceCapabilities.ClipboardRead)
        }
    }

    private fun requireWriteSupported() {
        if (!host.isWriteSupported()) {
            throw MiniAppException.UnsupportedCapability(WeChatDeviceCapabilities.ClipboardWrite)
        }
    }
}
