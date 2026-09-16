package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.async.awaitHostCallback
import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.WeChatDeviceCapabilities
import io.github.bobcgn.miniapp.host.wechat.WeChatSubscriptionRequest
import io.github.bobcgn.miniapp.host.wechat.WeChatSubscriptionResult
import io.github.bobcgn.miniapp.host.wechat.WeChatSubscriptionStatus
import io.github.bobcgn.miniapp.host.wechat.interop.WxSubscriptionAnswer
import io.github.bobcgn.miniapp.host.wechat.interop.WxSubscriptionEntry
import io.github.bobcgn.miniapp.host.wechat.interop.wxSubscriptionAnswer

/**
 * WeChat subscription-message requests, deliberately WeChat-specific.
 *
 * The request happens in WeChat's own prompt, so what the SDK owns is the template
 * list the caller describes, the per-template answer the host gives, and the errors
 * in between. It does not own the prompt, does not send messages, does not manage
 * templates, and does not turn any of this into a general push-notification
 * abstraction: subscribing to a WeChat template is a WeChat concept, and no other
 * host has been shown to share it.
 *
 * The caller owns the user gesture. WeChat requires this request to follow one, so
 * nothing here calls the host on its own, and nothing retries when the host refuses
 * because no gesture preceded it.
 *
 * Nothing here asks for a permission. The offline sources for this API name no
 * scope for it, so there is nothing to map, and this adapter deliberately does not
 * assume that BOB-60's unfinished privacy work does or does not apply to it.
 *
 * WeChat reports no task handle for this call, so coroutine cancellation only stops
 * the caller waiting; the host operation is not aborted and no abort hook is
 * invented for it.
 */
internal class WechatRequestSubscribeMessage(
    private val host: WechatRequestSubscribeMessageHost = WxRequestSubscribeMessageHost,
) {
    /**
     * Asks the host to put [request]'s templates in front of the user.
     *
     * The caller must call this from a user gesture; the SDK does not and cannot
     * supply one.
     *
     * The result holds exactly one entry per template the caller asked about, in the
     * caller's order. Missing or unexpected template keys are a malformed response:
     * silently accepting either would break correlation between the request and its
     * security-sensitive answer.
     *
     * A status the SDK cannot name is preserved verbatim rather than rejected;
     * a blank or non-text status is a broken answer and fails the call.
     *
     * @throws MiniAppException.UnsupportedCapability when the host has no such API
     * @throws MiniAppException.InvalidResponse when the host answers with something
     *   the contract cannot carry
     * @throws MiniAppException.HostFailure when the request fails for any other reason
     */
    suspend fun request(request: WeChatSubscriptionRequest): List<WeChatSubscriptionResult> {
        if (!host.isSupported()) {
            throw MiniAppException.UnsupportedCapability(
                WeChatDeviceCapabilities.RequestSubscribeMessage,
            )
        }

        val answer = awaitHostCallback { success, failure ->
            host.request(
                templateIds = request.templateIds,
                success = { result -> success(wxSubscriptionAnswer(result)) },
                failure = { result ->
                    failure(mapWechatRequestSubscribeMessageFailure(result))
                },
            )
            null
        }

        return when (answer) {
            is WxSubscriptionAnswer.Present -> {
                val requestedIds = request.templateIds.toSet()
                if (answer.entries.keys != requestedIds) {
                    throw MiniAppException.InvalidResponse(
                        "The WeChat host answered requestSubscribeMessage with template keys that do not match the request",
                    )
                }

                request.templateIds.map { templateId ->
                    when (val entry = answer.entries[templateId]) {
                        null -> error("Template-key equality guarantees every requested entry exists")

                        is WxSubscriptionEntry.Text -> {
                            if (entry.value.isBlank()) {
                                throw MiniAppException.InvalidResponse(
                                    "The WeChat host answered requestSubscribeMessage with a blank status",
                                )
                            }
                            WeChatSubscriptionResult(
                                templateId = templateId,
                                status = WeChatSubscriptionStatus.fromHostValue(entry.value),
                                hostStatus = entry.value,
                            )
                        }

                        WxSubscriptionEntry.NotText -> throw MiniAppException.InvalidResponse(
                            "The WeChat host answered requestSubscribeMessage with a status the SDK cannot read",
                        )
                    }
                }
            }

            WxSubscriptionAnswer.Unreadable -> throw MiniAppException.InvalidResponse(
                "The WeChat host answered requestSubscribeMessage with a result the SDK cannot read",
            )
        }
    }
}
