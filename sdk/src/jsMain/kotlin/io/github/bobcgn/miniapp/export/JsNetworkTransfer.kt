@file:OptIn(ExperimentalJsExport::class)

package io.github.bobcgn.miniapp.export

import io.github.bobcgn.miniapp.host.wechat.WeChatDownloadResult
import io.github.bobcgn.miniapp.host.wechat.WeChatTransferProgress
import io.github.bobcgn.miniapp.host.wechat.WeChatUploadResult
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatTransfer
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Progress the host reported for an in-flight upload or download.
 *
 * This is the host's own last figure, not an estimate: a transfer that never reported
 * anything has no progress value at all, which is why the reader returns `null`
 * rather than zero per cent.
 *
 * @property percent the host's own percentage, in `0..100`
 * @property bytesTransferred bytes the host reported transferring, when it reports
 *   that figure for this direction
 * @property bytesExpected bytes the host reported expecting, when it reports that
 *   figure for this direction
 */
@JsExport
public class JsTransferProgress internal constructor(
    /** Percentage complete, as the host reported it. */
    public val percent: Int,
    /** Bytes transferred so far, when the host reports that figure. */
    public val bytesTransferred: Double?,
    /** Bytes expected in total, when the host reports that figure. */
    public val bytesExpected: Double?,
)

/**
 * One completed upload.
 *
 * A completed upload is a completed exchange: the host got an HTTP response, and its
 * status describes that response rather than whether the upload was useful. A non-2xx
 * status therefore resolves, exactly as it does for {@link networkRequest}.
 */
@JsExport
public class JsUploadResult internal constructor(
    /** HTTP status the host received. */
    public val statusCode: Int,
    /** The response body as text. */
    public val responseText: String,
)

/**
 * One completed download.
 *
 * The paths are host file references. The SDK reads no content, moves nothing, and
 * makes no claim that a path outlives the session; further file work is the caller's,
 * through the file-system functions.
 */
@JsExport
public class JsDownloadResult internal constructor(
    /** HTTP status the host received. */
    public val statusCode: Int,
    /** The host's temporary copy of the downloaded content. */
    public val tempFilePath: String,
    /** The path the host reports when a target location was requested, else `null`. */
    public val filePath: String?,
)

/**
 * JavaScript-facing handle for an in-flight upload.
 *
 * `result()` resolves with the host's answer, or rejects when the transfer fails.
 * After {@link abort} it rejects, because a transfer the caller stopped has no
 * answer; a caller that aborted knows it did, so it does not need the rejection to
 * tell it so.
 */
@JsExport
public class JsUploadTransfer internal constructor(
    private val transfer: WechatTransfer<WeChatUploadResult>,
) {
    /** Whether this transfer can still be aborted. */
    public val abortable: Boolean
        get() = transfer.abortable

    /** Resolves with the host's answer, or rejects when the transfer fails or is aborted. */
    public suspend fun result(): JsUploadResult {
        val completed = transfer.await()
        return JsUploadResult(
            statusCode = completed.statusCode,
            responseText = completed.responseText,
        )
    }

    /** Stops the transfer, at most once. Returns whether a host abort was invoked. */
    public fun abort(): Boolean = transfer.abort()

    /** The most recent progress the host reported, or `null` when it reported none. */
    public fun progress(): JsTransferProgress? = transfer.lastProgress()?.toJsProgress()
}

/**
 * JavaScript-facing handle for an in-flight download.
 *
 * The behaviour matches {@link JsUploadTransfer}: `result()` resolves with the host's
 * answer, rejects on failure, and rejects after {@link abort}.
 */
@JsExport
public class JsDownloadTransfer internal constructor(
    private val transfer: WechatTransfer<WeChatDownloadResult>,
) {
    /** Whether this transfer can still be aborted. */
    public val abortable: Boolean
        get() = transfer.abortable

    /** Resolves with the host's answer, or rejects when the transfer fails or is aborted. */
    public suspend fun result(): JsDownloadResult {
        val completed = transfer.await()
        return JsDownloadResult(
            statusCode = completed.statusCode,
            tempFilePath = completed.tempFilePath,
            filePath = completed.filePath,
        )
    }

    /** Stops the transfer, at most once. Returns whether a host abort was invoked. */
    public fun abort(): Boolean = transfer.abort()

    /** The most recent progress the host reported, or `null` when it reported none. */
    public fun progress(): JsTransferProgress? = transfer.lastProgress()?.toJsProgress()
}

/** Presents the host's progress in a form a JavaScript caller can read. */
private fun WeChatTransferProgress.toJsProgress(): JsTransferProgress = JsTransferProgress(
    percent = percent,
    // Kotlin's Long is not a JavaScript value, so byte counts cross as numbers.
    bytesTransferred = bytesTransferred?.toDouble(),
    bytesExpected = bytesExpected?.toDouble(),
)
