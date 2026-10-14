@file:OptIn(kotlin.js.ExperimentalJsExport::class)

package org.example.kmp_demo_with_server

import io.github.bobcgn.miniapp.export.MiniAppExports
import kotlin.js.JsExport

/** Shared greeting with the current WeChat platform and base-library version; no backend needed. */
@JsExport
public fun platformGreeting(): String {
    val runtime = MiniAppExports.wechatRuntimeInfo()
    return wechatGreeting(runtime.platform, runtime.baseLibraryVersion)
}

internal fun wechatGreeting(platform: String?, baseLibraryVersion: String?): String {
    val name = platform?.takeIf { it.isNotBlank() } ?: "未知"
    val version = baseLibraryVersion?.takeIf { it.isNotBlank() } ?: "未知"
    return sayHello("WeChat · $name · 基础库 $version")
}
