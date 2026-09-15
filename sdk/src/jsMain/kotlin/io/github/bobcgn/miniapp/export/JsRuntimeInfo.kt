@file:OptIn(ExperimentalJsExport::class)

package io.github.bobcgn.miniapp.export

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * JavaScript-facing view of what the host runtime reports about itself.
 *
 * This is host-specific information rather than a capability: a base-library
 * version is a WeChat concept with no honest equivalent in a WebView-based host.
 */
@JsExport
public class JsRuntimeInfo internal constructor(
    /** Base-library version, or `null` when the runtime could not report one. */
    public val baseLibraryVersion: String?,
    /** Runtime platform, for example `devtools`, or `null` when unreadable. */
    public val platform: String?,
    /** Whether this runtime is WeChat Developer Tools rather than a device. */
    public val isDeveloperTools: Boolean,
)
