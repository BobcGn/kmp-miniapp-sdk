package io.github.bobcgn.miniapp.host.wechat.testing

import io.github.bobcgn.miniapp.host.wechat.WeChatMediaRequest
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatChooseMediaHost
import io.github.bobcgn.miniapp.host.wechat.interop.WxChooseMediaSuccessResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult

/**
 * The FakeAdapter boundary for media selection: a stand-in for the WeChat
 * `chooseMedia` callbacks.
 *
 * It implements the raw callback port, so a test can drive `WechatChooseMedia`
 * without a WeChat runtime and can reproduce answers a real host produces rarely,
 * such as a selection whose entries are malformed or whose list is not an array.
 *
 * There is no permission or privacy state here, because the adapter asks for
 * neither: WeChat's own picker needs no permission the host contract ties to it.
 *
 * @param supported whether this host exposes the media API
 * @param tempFiles what the host reports as `tempFiles`, typed loosely so a test can
 *   supply any shape, including one that is not an array
 */
internal class FakeWechatChooseMediaHost(
    var supported: Boolean = true,
    var tempFiles: Any? = fakeChooseMediaFiles(fakeChooseMediaEntry()),
) : WechatChooseMediaHost {
    /** Message the selection fails with, or `null` to succeed. */
    var failureMessage: String? = null

    /** When true, the selection reports its outcome twice, as a defective host might. */
    var completeTwice: Boolean = false

    var calls: Int = 0
        private set

    /** The request last handed to the host, so a test can assert what was forwarded. */
    var lastRequest: WeChatMediaRequest? = null
        private set

    override fun isSupported(): Boolean = supported

    override fun choose(
        request: WeChatMediaRequest,
        success: (WxChooseMediaSuccessResult) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        calls += 1
        lastRequest = request

        val complete = {
            val message = failureMessage
            if (message == null) {
                success(fakeChooseMediaSuccess(tempFiles))
            } else {
                failure(fakeWxFailure(message))
            }
        }

        complete()
        if (completeTwice) complete()
    }
}
