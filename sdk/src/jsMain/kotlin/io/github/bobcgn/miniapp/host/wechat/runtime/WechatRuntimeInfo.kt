package io.github.bobcgn.miniapp.host.wechat.runtime

import io.github.bobcgn.miniapp.host.HostVersion
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatRuntimeInfoHost

/**
 * What the WeChat runtime reports about itself.
 *
 * This is host-specific information, not a capability: a base-library version is
 * a WeChat concept, and a WebView-based host has no honest equivalent. It is
 * reachable through the platform escape hatch.
 *
 * Each reading is taken on first access rather than on construction, because the
 * export facade builds the host while the module is loading and a Node process
 * has no `wx` to read.
 */
internal class WechatRuntimeInfo(private val host: WechatRuntimeInfoHost) {
    /** Base-library version, or `null` when the host could not report one. */
    val baseLibraryVersion: HostVersion?
        get() = parsedBaseLibraryVersion

    /** Runtime platform as reported, for example `devtools`, or `null`. */
    val platform: String?
        get() = reportedPlatform

    /** Whether this runtime is WeChat Developer Tools rather than a device. */
    val isDeveloperTools: Boolean
        get() = reportedPlatform == DEVELOPER_TOOLS_PLATFORM

    /**
     * Reports whether the API, parameter, or component named by [schema] exists in
     * this base library.
     *
     * Answers `false` when the runtime cannot be asked at all.
     */
    fun canIUse(schema: String): Boolean = host.canIUse(schema)

    private val parsedBaseLibraryVersion: HostVersion? by lazy {
        host.baseLibraryVersion()?.let(HostVersion::parse)
    }

    private val reportedPlatform: String? by lazy { host.platform() }

    private companion object {
        private const val DEVELOPER_TOOLS_PLATFORM: String = "devtools"
    }
}
