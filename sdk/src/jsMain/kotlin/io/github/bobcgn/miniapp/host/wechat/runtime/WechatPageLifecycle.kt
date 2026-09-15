package io.github.bobcgn.miniapp.host.wechat.runtime

/**
 * WeChat page-level lifecycle, deliberately not a common capability.
 *
 * A WeChat page has a route and pages form a stack. That is the shape of a DSL
 * mini-program runtime. A WebView-based host has no page, no route, and no
 * stack, so modelling this as a platform-neutral contract would describe WeChat
 * semantics as universal ones. It is reachable only through the WeChat escape
 * hatch.
 *
 * This tracks the page the user is currently on. It is not a navigation stack:
 * the SDK records what the runtime reports and takes no action on it, and going
 * back is left to the runtime. Consumers that need to react to a page already
 * receive these hooks directly.
 */
internal class WechatPageLifecycle {
    private var route: String? = null
    private var visible: Boolean = false

    /** Route of the page the runtime most recently reported as shown. */
    val currentRoute: String?
        get() = route

    /** Whether a page currently reports itself as shown. */
    val isVisible: Boolean
        get() = visible

    /**
     * Records `Page.onShow` for [pageRoute].
     *
     * The route is taken here rather than from `onLoad` because becoming shown is
     * the moment a page becomes the one the user is on. That also makes a return
     * from a deeper page correct: the unloading page clears its route first, and
     * the page being revealed reports its own route immediately afterwards.
     */
    fun pageShown(pageRoute: String): Unit {
        route = pageRoute
        visible = true
    }

    /** Records `Page.onHide`. The page stays on the stack, so its route is kept. */
    fun pageHidden(): Unit {
        visible = false
    }

    /**
     * Records `Page.onUnload` for [pageRoute].
     *
     * Only the page this tracker currently points at clears it. An earlier page
     * that WeChat unloads while another page is current must not erase the route
     * of the page the user is on.
     */
    fun pageUnloaded(pageRoute: String): Unit {
        if (route != pageRoute) return
        route = null
        visible = false
    }
}
