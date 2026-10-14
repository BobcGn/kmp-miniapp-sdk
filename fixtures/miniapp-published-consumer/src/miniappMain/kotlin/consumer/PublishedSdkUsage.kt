package consumer

import io.github.bobcgn.miniapp.api.MiniAppSdk
import io.github.bobcgn.miniapp.export.JsRuntimeInfo
import io.github.bobcgn.miniapp.export.MiniAppExports

// Navigate from these consumer call sites, not by opening SDK source files manually.
public fun sdkVersion(): String = MiniAppExports.sdkVersion()

// Hover here must show the SDK KDoc, including the nullable base-library version explanation.
public fun runtimeInfo(): JsRuntimeInfo = MiniAppExports.wechatRuntimeInfo()

// Also covers a commonMain property exposed to the Mini App consumer.
public fun sharedVersion(): String = MiniAppSdk.VERSION
