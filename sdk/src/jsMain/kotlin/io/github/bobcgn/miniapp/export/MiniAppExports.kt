@file:OptIn(ExperimentalJsExport::class)

package io.github.bobcgn.miniapp.export

import io.github.bobcgn.miniapp.api.MiniAppSdk
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@JsExport
public object MiniAppExports {
    public fun sdkVersion(): String = MiniAppSdk.VERSION
}
