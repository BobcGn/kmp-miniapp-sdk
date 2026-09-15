@file:OptIn(ExperimentalJsExport::class)

package io.github.bobcgn.miniapp.export

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * JavaScript-facing view of one scan WeChat reported.
 *
 * The decoded content is the user's data, handed over because the caller asked to
 * scan. Nothing in the SDK logs, stores, or uploads it, and a caller that does
 * takes on the responsibility that comes with the content the user scanned.
 *
 * @property text the decoded content
 * @property scanType the host's own name for the format, verbatim, or `null` when
 *   the host reported none
 * @property format the same name resolved against the formats this SDK knows, or
 *   `null` when the host named one it does not; a `null` [format] with a non-`null`
 *   [scanType] is a format the SDK did not recognize, not a missing answer
 * @property charSet the character set of [text], or `null` when the host reported none
 * @property rawData the decoded bytes as the host reports them, or `null`
 * @property path a path to the scanned image, or `null`; the host reports this only
 *   in some cases
 */
@JsExport
public class JsScanResult internal constructor(
    /** The decoded content. */
    public val text: String,
    /** The host's own name for the format, verbatim, when it reported one. */
    public val scanType: String?,
    /** The format resolved against this SDK's known set, when it recognizes it. */
    public val format: String?,
    /** The character set of the decoded content, when the host reported one. */
    public val charSet: String?,
    /** The decoded bytes as the host reports them, when it reported them. */
    public val rawData: String?,
    /** A path to the scanned image, when the host reported one. */
    public val path: String?,
)
