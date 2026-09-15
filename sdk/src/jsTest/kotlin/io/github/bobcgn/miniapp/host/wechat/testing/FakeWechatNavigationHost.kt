package io.github.bobcgn.miniapp.host.wechat.testing

import io.github.bobcgn.miniapp.host.wechat.adapter.WechatNavigationHost
import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult

/** One page-stack operation the fake host was asked to perform. */
internal data class RecordedWechatNavigation(
    val operation: String,
    val url: String?,
    val delta: Int?,
)

/**
 * The FakeAdapter boundary for navigation: a stand-in for the WeChat page-stack
 * callbacks that replies immediately with one scripted outcome.
 *
 * It implements the raw callback port, so a test can drive `WechatNavigation`
 * without a WeChat runtime and can reproduce host behavior the SDK reports as a
 * failure, such as going back from the first page.
 *
 * @param failWith a `fail` message, which makes every call fail instead of succeeding
 */
internal class FakeWechatNavigationHost(
    private val failWith: String? = null,
) : WechatNavigationHost {
    /** Every operation this host was asked to perform, in order. */
    val calls: MutableList<RecordedWechatNavigation> = mutableListOf()

    override fun navigateTo(
        url: String,
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ): Unit = dispatch("navigateTo", url = url, delta = null, success = success, failure = failure)

    override fun redirectTo(
        url: String,
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ): Unit = dispatch("redirectTo", url = url, delta = null, success = success, failure = failure)

    override fun navigateBack(
        delta: Int?,
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ): Unit = dispatch("navigateBack", url = null, delta = delta, success = success, failure = failure)

    private fun dispatch(
        operation: String,
        url: String?,
        delta: Int?,
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        calls += RecordedWechatNavigation(operation = operation, url = url, delta = delta)

        val message = failWith
        if (message == null) {
            success()
        } else {
            failure(fakeWxFailure(message))
        }
    }
}
