@file:OptIn(ExperimentalJsExport::class)

package io.github.bobcgn.miniapp.export

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * JavaScript-facing view of how the host currently supports a capability.
 *
 * The state is a string because a JavaScript caller cannot match a Kotlin sealed
 * hierarchy. The remaining properties carry the detail of the matching state and
 * are `null` for the others.
 */
@JsExport
public class JsCapabilitySupport internal constructor(
    /** `Supported`, `Unsupported`, `VersionDependent`, or `PermissionDependent`. */
    public val state: String,
    /** Oldest host version that provides the capability; set for `VersionDependent`. */
    public val requiredVersion: String?,
    /** Host version that was observed; set for `VersionDependent` when readable. */
    public val currentVersion: String?,
    /** Permission the capability depends on; set for `PermissionDependent`. */
    public val permission: String?,
)
