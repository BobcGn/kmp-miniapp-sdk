@file:OptIn(ExperimentalJsExport::class)

package io.github.bobcgn.miniapp.export

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * JavaScript-facing view of one file the user selected.
 *
 * The path is a host temporary resource, not a durable one: it belongs to the
 * session that produced it and nothing in the host contract promises it survives.
 * A caller that needs the media later must copy it to storage it owns; the SDK
 * does not do that on the caller's behalf, and it keeps no copy itself.
 *
 * Nothing here is logged, cached, or uploaded by the SDK. What the user selected is
 * theirs, and a caller that keeps it takes on the responsibility that comes with it.
 *
 * @property tempFilePath the host's temporary path to the file
 * @property sizeBytes the file's size in bytes, as a JavaScript number
 * @property fileType the kind the host named, or `null` when it named one this SDK
 *   does not know; [hostFileType] preserves that unknown name
 * @property hostFileType the required kind name the host reported, verbatim
 * @property durationSeconds a video's duration in seconds, or `null` when the host
 *   reported none, which is what an image reports
 * @property width a video's width in pixels, or `null` when the host reported none
 * @property height a video's height in pixels, or `null` when the host reported none
 * @property thumbTempFilePath a temporary path to a video's thumbnail, or `null`
 *   when the host reported none
 */
@JsExport
public class JsMediaFile internal constructor(
    /** The host's temporary path to the selected file. */
    public val tempFilePath: String,
    /** The file's size in bytes. */
    public val sizeBytes: Double,
    /** The kind resolved against this SDK's known set, when it recognizes it. */
    public val fileType: String?,
    /** The host's own name for the kind, verbatim, when it reported one. */
    public val hostFileType: String,
    /** A video's duration in seconds, when the host reported one. */
    public val durationSeconds: Double?,
    /** A video's width in pixels, when the host reported one. */
    public val width: Double?,
    /** A video's height in pixels, when the host reported one. */
    public val height: Double?,
    /** A temporary path to a video's thumbnail, when the host reported one. */
    public val thumbTempFilePath: String?,
)
