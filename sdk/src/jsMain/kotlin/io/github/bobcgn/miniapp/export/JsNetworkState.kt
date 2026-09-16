@file:OptIn(ExperimentalJsExport::class)

package io.github.bobcgn.miniapp.export

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * JavaScript-facing view of what the host reports about its network.
 *
 * @property isConnected whether the host currently has a usable connection
 * @property networkType the kind of link as this SDK names it, or `null` when the
 *   host named one it does not recognize
 * @property hostNetworkType the host's own word for the link, verbatim
 */
@JsExport
public class JsNetworkState internal constructor(
    /** Whether the host currently has a usable connection. */
    public val isConnected: Boolean,
    /** The kind of link, resolved against this SDK's known set when it recognizes it. */
    public val networkType: String?,
    /** The host's own word for the link, verbatim. */
    public val hostNetworkType: String,
)
