package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.async.awaitHostCallback

/**
 * WeChat navigation, deliberately not a common capability.
 *
 * A WeChat page stack is a DSL mini-program runtime concept. A WebView-based host
 * has no page stack, so this bridge is WeChat-specific and is reached through the
 * WeChat platform escape hatch rather than through a platform-neutral contract.
 *
 * All three operations share WeChat's callback shape: the host reports success
 * once the stack has changed, and reports failure for a condition the caller must
 * handle, such as exceeding the page limit or going back from the first page.
 */
internal class WechatNavigation(
    private val host: WechatNavigationHost = WxNavigationHost,
) {
    /**
     * Opens [url] on top of the current page stack.
     *
     * WeChat keeps a bounded stack, so this can fail even when [url] is valid.
     * There is no host abort handle; coroutine cancellation only stops this call
     * from consuming the later callback.
     */
    suspend fun navigateTo(url: String): Unit = awaitHostCallback { success, failure ->
        host.navigateTo(
            url = url,
            success = { success(Unit) },
            failure = { result ->
                failure(mapWechatHostFailure(operation = "navigateTo", result = result))
            },
        )
        null
    }

    /**
     * Replaces the current page with [url], removing the current page from the stack.
     *
     * Because the current page is removed rather than covered, the caller cannot
     * navigate back to it.
     */
    suspend fun redirectTo(url: String): Unit = awaitHostCallback { success, failure ->
        host.redirectTo(
            url = url,
            success = { success(Unit) },
            failure = { result ->
                failure(mapWechatHostFailure(operation = "redirectTo", result = result))
            },
        )
        null
    }

    /**
     * Pops [delta] pages off the current page stack, or one page when [delta] is
     * `null` and the host default applies.
     *
     * Going back from the first page is reported by WeChat as a failure, so this
     * does not silently succeed when there is nowhere to go.
     */
    suspend fun navigateBack(delta: Int? = null): Unit = awaitHostCallback { success, failure ->
        host.navigateBack(
            delta = delta,
            success = { success(Unit) },
            failure = { result ->
                failure(mapWechatHostFailure(operation = "navigateBack", result = result))
            },
        )
        null
    }
}
