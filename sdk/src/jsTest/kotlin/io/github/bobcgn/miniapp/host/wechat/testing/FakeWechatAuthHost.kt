package io.github.bobcgn.miniapp.host.wechat.testing

import io.github.bobcgn.miniapp.host.wechat.adapter.WechatAuthHost
import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxLoginFailureResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxLoginSuccessResult

/**
 * The FakeAdapter boundary for WeChat client login: a stand-in for the `wx.login`
 * callbacks that replies immediately with one scripted outcome.
 *
 * It implements the raw callback port, so a test can drive `WechatAuth` without a
 * WeChat runtime and can reproduce host behavior the contract rejects, such as a
 * successful login that carries a blank code.
 *
 * @param code login code a successful call reports
 * @param failWith a `fail` message, which makes every call fail instead of succeeding
 * @param errno WeChat errno to report alongside [failWith]
 */
internal class FakeWechatAuthHost(
    private val code: String = "short-lived-code",
    private val failWith: String? = null,
    private val errno: Int? = null,
) : WechatAuthHost {
    /** Whether this host exposes the session check API at all. */
    var sessionCheckSupported: Boolean = true

    /** Message `checkSession` fails with, or `null` to succeed. */
    var sessionCheckFailure: String? = null

    /** When true, `checkSession` reports its outcome twice, as a defective host might. */
    var completeSessionCheckTwice: Boolean = false

    /** How many times the adapter has asked this host to log in. */
    var calls: Int = 0
        private set

    /** How many times the adapter has asked this host to check the session. */
    var sessionCheckCalls: Int = 0
        private set

    override fun login(
        success: (WxLoginSuccessResult) -> Unit,
        failure: (WxLoginFailureResult) -> Unit,
    ) {
        calls += 1

        val message = failWith
        if (message == null) {
            success(fakeWxLoginSuccess(code))
        } else {
            failure(fakeWxLoginFailure(message = message, errno = errno))
        }
    }

    override fun isSessionCheckSupported(): Boolean = sessionCheckSupported

    override fun checkSession(
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        sessionCheckCalls += 1

        val complete = {
            val message = sessionCheckFailure
            if (message == null) {
                success()
            } else {
                failure(fakeWxFailure(message))
            }
        }

        complete()
        if (completeSessionCheckTwice) complete()
    }
}
