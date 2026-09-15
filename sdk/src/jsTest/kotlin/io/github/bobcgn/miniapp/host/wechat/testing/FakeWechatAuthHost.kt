package io.github.bobcgn.miniapp.host.wechat.testing

import io.github.bobcgn.miniapp.host.wechat.adapter.WechatAuthHost
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
    /** How many times the adapter has asked this host to log in. */
    var calls: Int = 0
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
}
