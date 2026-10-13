@file:OptIn(kotlin.js.ExperimentalJsExport::class)

package consumer

import io.github.bobcgn.miniapp.export.MiniAppExports
import kotlin.js.JsExport

/** Host information comes from the SDK; rendering remains in the root WeChat project. */
@JsExport
public fun platformGreeting(): String {
    val info = MiniAppExports.wechatRuntimeInfo()
    val platform = info.platform ?: "未知"
    val version = info.baseLibraryVersion ?: "未知"
    return greeting("WeChat · $platform · 基础库 $version")
}
