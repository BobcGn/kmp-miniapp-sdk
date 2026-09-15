@file:OptIn(ExperimentalJsExport::class)

package io.github.bobcgn.miniapp.export

import io.github.bobcgn.miniapp.api.MiniAppSdk
import io.github.bobcgn.miniapp.capability.network.HttpMethod
import io.github.bobcgn.miniapp.capability.network.MiniAppHttpRequest
import io.github.bobcgn.miniapp.capability.network.MiniAppHttpTransport
import io.github.bobcgn.miniapp.capability.storage.MiniAppStorage
import io.github.bobcgn.miniapp.host.wechat.WeChatLoginResult
import io.github.bobcgn.miniapp.host.wechat.WechatHost
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Stable JavaScript and TypeScript entry point for host-neutral SDK exports.
 *
 * Keep this facade small and delegate behavior to shared Kotlin APIs. Host APIs
 * such as WeChat's `wx` object must not be accessed from this export boundary.
 */
@JsExport
public object MiniAppExports {
    private val host: WechatHost = WechatHost()
    private val storage: MiniAppStorage = host.storage
    private val network: MiniAppHttpTransport = host.network

    /**
     * Returns the version of the Kotlin SDK bundled into the JavaScript artifact.
     *
     * @return the same value exposed by [MiniAppSdk.VERSION]
     */
    public fun sdkVersion(): String = MiniAppSdk.VERSION

    /** Returns a stored string, or `null` when [key] does not exist. */
    public suspend fun storageGet(key: String): String? = storage.get(key)

    /** Stores a string, replacing the value currently associated with [key]. */
    public suspend fun storageSet(key: String, value: String): Unit = storage.set(key, value)

    /** Removes [key]; removing an absent key is successful. */
    public suspend fun storageRemove(key: String): Unit = storage.remove(key)

    /**
     * Obtains a short-lived WeChat login code for server-side exchange.
     *
     * The returned value does not represent an authenticated user or session.
     */
    public suspend fun wechatLogin(): WeChatLoginResult = host.platform.auth.login()

    /**
     * Performs an HTTP exchange and resolves with its result.
     *
     * A completed exchange resolves even when its status reports an HTTP error;
     * only a transport failure rejects, with `MiniAppException.Timeout` for an
     * expired timeout and `MiniAppException.HostFailure` otherwise.
     *
     * [headers] is a flat sequence of alternating name and value strings because
     * a JavaScript caller cannot construct the Kotlin map used inside the SDK.
     * The bundled CommonJS wrapper converts plain objects to and from this shape.
     *
     * @param url absolute request target
     * @param method HTTP method name, for example `GET` or `POST`
     * @param headers alternating header name and value entries
     * @param body already-encoded request body, or `null` for no body
     * @param timeoutMillis host timeout in milliseconds, or `null` for the host default
     * @throws IllegalArgumentException when [method] is unknown or [headers] is unpaired
     */
    public suspend fun networkRequest(
        url: String,
        method: String,
        headers: Array<String>,
        body: String?,
        timeoutMillis: Int?,
    ): MiniAppHttpResult {
        val response = network.request(
            MiniAppHttpRequest(
                url = url,
                method = httpMethod(method),
                headers = headerMap(headers),
                body = body,
                timeoutMillis = timeoutMillis,
            ),
        )
        return MiniAppHttpResult(
            statusCode = response.statusCode,
            headers = response.headers.flatMap { (name, value) -> listOf(name, value) }.toTypedArray(),
            body = response.body,
        )
    }

    /**
     * Records WeChat's `App.onLaunch`, forwarded by the consumer's app entry point.
     *
     * WeChat calls `onLaunch` and then `onShow`, so both report the app as being in
     * the foreground. The state is read back through [wechatAppLifecycleState].
     */
    public fun wechatAppOnLaunch(): Unit = host.platform.appLifecycle.appLaunched()

    /** Records WeChat's `App.onShow`, forwarded by the consumer's app entry point. */
    public fun wechatAppOnShow(): Unit = host.platform.appLifecycle.appShown()

    /** Records WeChat's `App.onHide`, forwarded by the consumer's app entry point. */
    public fun wechatAppOnHide(): Unit = host.platform.appLifecycle.appHidden()

    /** Returns the app-level lifecycle state WeChat reported most recently. */
    public fun wechatAppLifecycleState(): String = host.lifecycle.state.name

    /**
     * Records WeChat's `Page.onShow` for [route], forwarded by the page.
     *
     * The route is taken from the page rather than passed by WeChat, so the page
     * supplies its own `this.route`. `Page.onLoad` is not forwarded: a page becomes
     * the one the user is on when it is shown, and `onShow` reports that same route.
     */
    public fun wechatPageOnShow(route: String): Unit =
        host.platform.pageLifecycle.pageShown(route)

    /** Records WeChat's `Page.onHide`, forwarded by the page. */
    public fun wechatPageOnHide(): Unit = host.platform.pageLifecycle.pageHidden()

    /** Records WeChat's `Page.onUnload` for [route], forwarded by the page. */
    public fun wechatPageOnUnload(route: String): Unit =
        host.platform.pageLifecycle.pageUnloaded(route)

    /** Returns the route of the page that loaded most recently, or `null` when none is loaded. */
    public fun wechatPageRoute(): String? = host.platform.pageLifecycle.currentRoute

    /**
     * Opens [url] on top of the current WeChat page stack.
     *
     * WeChat keeps a bounded stack, so this rejects even for a valid [url] when the
     * stack is full. Navigation is WeChat-specific: it is not a common capability.
     */
    public suspend fun wechatNavigateTo(url: String): Unit = host.platform.navigation.navigateTo(url)

    /** Replaces the current page with [url], removing it from the stack. */
    public suspend fun wechatRedirectTo(url: String): Unit =
        host.platform.navigation.redirectTo(url)

    /**
     * Pops [delta] pages off the current page stack, or one page when [delta] is `null`.
     *
     * Going back from the first page rejects rather than succeeding silently.
     */
    public suspend fun wechatNavigateBack(delta: Int?): Unit =
        host.platform.navigation.navigateBack(delta)
}

/** Restores the Kotlin map shape from the flat entry list used at this boundary. */
private fun headerMap(headers: Array<String>): Map<String, String> {
    require(headers.size % 2 == 0) {
        "headers must contain alternating name and value entries"
    }
    val result = mutableMapOf<String, String>()
    var index = 0
    while (index < headers.size) {
        result[headers[index]] = headers[index + 1]
        index += 2
    }
    return result
}

/** Resolves a JavaScript-supplied method name to the closed SDK method set. */
private fun httpMethod(method: String): HttpMethod {
    val normalized = method.uppercase()
    return HttpMethod.entries.firstOrNull { it.name == normalized }
        ?: throw IllegalArgumentException("Unsupported HTTP method: '$method'")
}
