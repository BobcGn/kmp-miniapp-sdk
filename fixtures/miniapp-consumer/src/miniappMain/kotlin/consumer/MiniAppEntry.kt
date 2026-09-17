package consumer

import io.github.bobcgn.miniapp.api.MiniAppSdk
import kotlin.js.JsExport

// Host integration and binding only: these are what the WeChat host's thin JavaScript calls. A
// Kotlin/JS library exports nothing else, so an unexported declaration never reaches the bundle.
//
// Only @JsExport declarations are visible to the host. The last one calls the runtime SDK's public
// API, which compiles only because the plugin wired the runtime into this source set.

@JsExport
public fun hostGreeting(name: String): String = greeting(name)

@JsExport
public fun countUpTo(limit: Int): Int {
    val counter = Counter()
    repeat(limit) { counter.increment() }
    return counter.current()
}

@JsExport
public fun sdkVersion(): String = MiniAppSdk.VERSION

/** Kotlin/JS-only construct: this source set is compiled by the JavaScript compiler. */
internal fun runtimeMarker(): String = js("'consumer-miniapp'") as String
