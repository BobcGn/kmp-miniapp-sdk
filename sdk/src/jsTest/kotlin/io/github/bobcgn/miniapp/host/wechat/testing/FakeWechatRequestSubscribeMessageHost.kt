package io.github.bobcgn.miniapp.host.wechat.testing

import io.github.bobcgn.miniapp.host.wechat.WeChatTemplateId
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatRequestSubscribeMessageHost
import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxRequestSubscribeMessageSuccessResult

/**
 * The FakeAdapter boundary for subscription requests: a stand-in for the WeChat
 * `requestSubscribeMessage` callbacks.
 *
 * It implements the raw callback port, so a test can drive
 * `WechatRequestSubscribeMessage` without a WeChat runtime and can reproduce answers
 * a real host produces rarely, such as a status of the wrong type, a template the
 * host says nothing about, or a template the caller never asked about.
 *
 * There is no permission or privacy state here, because the adapter asks for
 * neither: the offline sources for this API name no scope for it.
 *
 * @param supported whether this host exposes the subscription API
 * @param answer the result the host reports when it does not fail
 */
internal class FakeWechatRequestSubscribeMessageHost(
    var supported: Boolean = true,
    var answer: WxRequestSubscribeMessageSuccessResult =
        fakeSubscribeMessageSuccess("template-one" to "accept"),
) : WechatRequestSubscribeMessageHost {
    /** Message the request fails with, or `null` to succeed. */
    var failureMessage: String? = null

    /** When true, the request reports its outcome twice, as a defective host might. */
    var completeTwice: Boolean = false

    var calls: Int = 0
        private set

    /** The template ids last handed to the host, so a test can assert what was forwarded. */
    var lastTemplateIds: List<WeChatTemplateId>? = null
        private set

    override fun isSupported(): Boolean = supported

    override fun request(
        templateIds: List<WeChatTemplateId>,
        success: (WxRequestSubscribeMessageSuccessResult) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        calls += 1
        lastTemplateIds = templateIds

        val complete = {
            val message = failureMessage
            if (message == null) {
                success(answer)
            } else {
                failure(fakeWxFailure(message))
            }
        }

        complete()
        if (completeTwice) complete()
    }
}
