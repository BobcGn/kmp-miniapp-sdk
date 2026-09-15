package io.github.bobcgn.miniapp.host.wechat.testing

import io.github.bobcgn.miniapp.host.wechat.adapter.WechatClipboardHost
import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxGetClipboardDataSuccessResult

/**
 * The FakeAdapter boundary for the clipboard: a stand-in for the WeChat
 * `getClipboardData` and `setClipboardData` callbacks.
 *
 * It implements the raw callback port, so a test can drive `WechatClipboard`
 * without a WeChat runtime and can reproduce conditions a real host produces
 * rarely, such as an answer whose `data` is missing or is not a string.
 *
 * The two directions are switchable independently, because a host may expose one
 * without the other.
 *
 * @param readSupported whether this host exposes the clipboard read API
 * @param writeSupported whether this host exposes the clipboard write API
 * @param clipboardText text the host reports, typed loosely so a test can supply
 *   a malformed answer
 */
internal class FakeWechatClipboardHost(
    var readSupported: Boolean = true,
    var writeSupported: Boolean = true,
    var clipboardText: Any? = "",
) : WechatClipboardHost {
    /** Message `read` fails with, or `null` to succeed. */
    var readFailure: String? = null

    /** Message `write` fails with, or `null` to succeed. */
    var writeFailure: String? = null

    /** When true, `read` reports its outcome twice, as a defective host might. */
    var completeReadTwice: Boolean = false

    var readCalls: Int = 0
        private set

    var writeCalls: Int = 0
        private set

    /** The value last handed to the host, so a test can assert what was forwarded. */
    var lastWritten: String? = null
        private set

    override fun isReadSupported(): Boolean = readSupported

    override fun isWriteSupported(): Boolean = writeSupported

    override fun read(
        success: (WxGetClipboardDataSuccessResult) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        readCalls += 1

        val complete = {
            val message = readFailure
            if (message == null) {
                success(fakeGetClipboardDataSuccess(clipboardText))
            } else {
                failure(fakeWxFailure(message))
            }
        }

        complete()
        if (completeReadTwice) complete()
    }

    override fun write(
        data: String,
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        writeCalls += 1
        lastWritten = data

        val message = writeFailure
        if (message == null) {
            success()
        } else {
            failure(fakeWxFailure(message))
        }
    }
}
