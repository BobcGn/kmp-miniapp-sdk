package org.example.kmp_demo

import io.github.bobcgn.miniapp.export.MiniAppExports

/** WeChat-specific environment; missing host information is displayed rather than invented. */
internal class MiniAppPlatform(platform: String?, baseLibraryVersion: String?) : Platform {
    override val name: String =
        "WeChat · ${platform?.takeIf { it.isNotBlank() } ?: "未知"} · 基础库 ${baseLibraryVersion?.takeIf { it.isNotBlank() } ?: "未知"}"
}

/** Reads the SDK's existing host adapter; UI and raw wx interop stay out of this source set. */
public actual fun getPlatform(): Platform {
    val runtime = MiniAppExports.wechatRuntimeInfo()
    return MiniAppPlatform(runtime.platform, runtime.baseLibraryVersion)
}
