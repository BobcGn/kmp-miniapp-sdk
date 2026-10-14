package io.github.bobcgn.miniapp.gradle

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property

/**
 * Build configuration for the WeChat host — the current and only Mini App host.
 *
 * It is a [MiniAppHostConfiguration] rather than an extension of its own, so that "Mini App" stays
 * the platform and "WeChat" stays a host. Adding a second host means adding another subtype and
 * another accessor on [MiniAppExtension]; it does not mean widening this one.
 *
 * Only one setting is WeChat's own word rather than a portable build concept: the directory inside
 * the source host that WeChat packages, relative to that host. `miniprogram` is WeChat's vocabulary,
 * so it is declared here and not on the platform.
 */
public abstract class WeChatHostConfiguration : MiniAppHostConfiguration() {

    /**
     * Selects this module as the sole producer of a host maintained at the consumer build root.
     *
     * Defaults to false for compatibility. When true, source/distribution defaults use the root
     * project's directories, and root-level host tasks delegate to this module. Only one module
     * may opt in. It does not change this module's Kotlin source sets or bundle output.
     */
    public abstract val rootHost: Property<Boolean>

    /**
     * Where the compiled Kotlin/JS modules are written inside [MiniAppHostConfiguration.sourceDirectory].
     *
     * Defaults to `miniprogram/libs`. This directory is the plugin's to own: [MiniAppPluginDiagnostics.SYNC_MINIAPP_TASK_NAME]
     * synchronizes it, so anything in it that is not part of the current bundle is deleted. It must
     * stay strictly inside the source host — nothing outside that host is ever touched.
     */
    public abstract val generatedLibraryDirectory: DirectoryProperty

    public companion object {
        /** The accessor name on [MiniAppExtension]. */
        public const val NAME: String = "wechat"
    }
}
