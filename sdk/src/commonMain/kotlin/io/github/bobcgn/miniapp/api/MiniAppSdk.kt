package io.github.bobcgn.miniapp.api

/**
 * Platform-neutral metadata for the KMP MiniApp SDK.
 *
 * This object belongs to `commonMain` and therefore must not expose or depend on
 * any mini-program host API.
 */
public object MiniAppSdk {
    /** The SDK version embedded in the generated artifacts. */
    public const val VERSION: String = "0.1.0-SNAPSHOT"
}
