@file:OptIn(ExperimentalJsExport::class)

package io.github.bobcgn.miniapp.export

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * JavaScript-facing view of what the host requires for its privacy contract.
 *
 * The requirement is a string because a JavaScript caller cannot match a Kotlin
 * enum. `contractName` is whatever the host calls its privacy contract; it is
 * absent when the host reports none, and the SDK never invents one.
 */
@JsExport
public class JsPrivacyStatus internal constructor(
    /** `REQUIRED` or `NOT_REQUIRED`. */
    public val requirement: String,
    /** The host's own name for its privacy contract, or `null` when it reports none. */
    public val contractName: String?,
)
