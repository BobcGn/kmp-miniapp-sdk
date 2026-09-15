package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.async.awaitHostCallback
import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.WeChatLoginResult
import io.github.bobcgn.miniapp.host.wechat.WeChatSessionState

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

    /**
     * Reports whether WeChat still holds a usable login session for this mini
     * program.
     *
     * This is a query and nothing else. It never obtains a new code, exchanges
     * one with a backend, refreshes a token, or retries: a consumer that needs a
     * new code after [WeChatSessionState.INVALID] decides that itself and calls
     * [login].
     *
     * A valid answer says only that WeChat's own login state is intact. It is not
     * a trusted user, not a consumer backend session, and not proof that any
     * credential the consumer holds is still accepted.
     *
     * @throws MiniAppException.UnsupportedCapability when the host has no session
     *   check API
     * @throws MiniAppException.InternalFailure when the host call cannot be registered
     */
    suspend fun checkSession(): WeChatSessionState {
        if (!host.isSessionCheckSupported()) {
            throw MiniAppException.UnsupportedCapability(WeChatSessionState.Key)
        }

        return awaitHostCallback { success, failure ->
            host.checkSession(
                success = { success(WeChatSessionState.VALID) },
                // The wx.checkSession contract uses its failure callback to
                // report an expired login state. The callback shape itself is
                // the discriminator; its human-readable errMsg is not an ABI.
                failure = { success(WeChatSessionState.INVALID) },
            )
            null
        }
    }
}
