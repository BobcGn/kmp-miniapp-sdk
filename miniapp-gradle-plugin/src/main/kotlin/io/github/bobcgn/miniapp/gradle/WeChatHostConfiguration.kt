package io.github.bobcgn.miniapp.gradle

/**
 * Build configuration for the WeChat host — the current and only Mini App host.
 *
 * It is a [MiniAppHostConfiguration] rather than an extension of its own, so that "Mini App" stays
 * the platform and "WeChat" stays a host. Nothing in this plugin reads a WeChat-specific build
 * setting today: the host's CommonJS plus TypeScript-declaration output shape and its renderer
 * boundary are owned by the platform build, and only the bundle location is a consumer choice.
 *
 * Adding a second host means adding another subtype and another accessor on [MiniAppExtension]; it
 * does not mean widening this one.
 */
public abstract class WeChatHostConfiguration : MiniAppHostConfiguration() {

    public companion object {
        /** The accessor name on [MiniAppExtension]. */
        public const val NAME: String = "wechat"
    }
}
