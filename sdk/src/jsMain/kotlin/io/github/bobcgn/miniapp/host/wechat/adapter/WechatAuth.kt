package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.async.awaitHostCallback
import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.WeChatLoginResult

/** Coroutine adapter for the WeChat-specific client login bootstrap. */
internal class WechatAuth(
    private val host: WechatAuthHost = WxAuthHost,
) {
    /**
     * Obtains a short-lived WeChat login code.
     *
     * The returned code is not a trusted identity. This operation has no host
     * abort hook, so coroutine cancellation only stops consumption of callbacks.
     */
    suspend fun login(): WeChatLoginResult = awaitHostCallback { success, failure ->
        host.login(
            success = { result ->
                if (result.code.isBlank()) {
                    failure(
                        MiniAppException.InvalidResponse(
                            "WeChat login succeeded without a usable code",
                        ),
                    )
                } else {
                    success(WeChatLoginResult(result.code))
                }
            },
            failure = { result ->
                failure(
                    mapWechatHostFailure(
                        operation = "login",
                        result = result,
                        code = result.errno?.toString(),
                    ),
                )
            },
        )
        null
    }
}
