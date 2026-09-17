package io.github.bobcgn.miniapp.api

/**
 * Platform-neutral metadata for the KMP MiniApp SDK.
 *
 * This object belongs to `commonMain` and therefore must not expose or depend on
 * any mini-program host API.
 */
public object MiniAppSdk {
    /**
     * The SDK version embedded in the generated artifacts.
     *
     * The value is generated from `gradle/libs.versions.toml`; nothing in this module states it, so
     * it cannot differ from the version the SDK is built and published as.
     */
    public const val VERSION: String = GeneratedMiniAppSdkVersion.VALUE
}
