package io.github.bobcgn.miniapp.host.wechat.interop

import kotlin.js.jsTypeOf

/**
 * Raw callback-style options accepted by [wx.uploadFile].
 *
 * The base library shipped with the installed Developer Tools enumerates `url`,
 * `filePath`, `name`, `header`, `formData`, and `timeout` for this call.
 */
internal external interface WxUploadFileOptions {
    /** Absolute endpoint the host posts to. */
    var url: String

    /** Path to the file inside the host's file sandbox. */
    var filePath: String

    /** Name the host gives the file in the multipart body. */
    var name: String

    /** Request headers as a plain JavaScript object of string values. */
    var header: Any?

    /** Additional multipart form fields as a plain JavaScript object. */
    var formData: Any?

    /** Host timeout in milliseconds. */
    var timeout: Int?

    /** Called only when the host obtained an HTTP response. */
    var success: WxUploadFileSuccessCallback?

    /** Called when no response was obtained, including a timeout. */
    var fail: WxGeneralCallback?

    /** Called after either [success] or [fail]. */
    var complete: WxGeneralCallback?
}

/** Raw success result returned by [wx.uploadFile]. */
internal external interface WxUploadFileSuccessResult : WxGeneralCallbackResult {
    /** Documented type is `string`: the response body. */
    val data: Any?

    /** Documented type is `number`: the HTTP status the host received. */
    val statusCode: Any?
}

/** Success callback accepted by [WxUploadFileOptions]. */
internal typealias WxUploadFileSuccessCallback = (WxUploadFileSuccessResult) -> Unit

/**
 * Raw callback-style options accepted by [wx.downloadFile].
 *
 * The installed base library enumerates `url`, `header`, `timeout`, and `filePath`
 * for this call.
 */
internal external interface WxDownloadFileOptions {
    /** Endpoint the host fetches from. */
    var url: String

    /** Request headers as a plain JavaScript object of string values. */
    var header: Any?

    /** Host timeout in milliseconds. */
    var timeout: Int?

    /** Where the host should put the file, or unset for its own temporary location. */
    var filePath: String?

    /** Called only when the host obtained an HTTP response. */
    var success: WxDownloadFileSuccessCallback?

    /** Called when no response was obtained, including a timeout. */
    var fail: WxGeneralCallback?

    /** Called after either [success] or [fail]. */
    var complete: WxGeneralCallback?
}

/** Raw success result returned by [wx.downloadFile]. */
internal external interface WxDownloadFileSuccessResult : WxGeneralCallbackResult {
    /** Documented type is `string`: the host's temporary copy of the content. */
    val tempFilePath: Any?

    /** Documented type is `string`: the path the host reports for a requested target. */
    val filePath: Any?

    /** Documented type is `number`: the HTTP status the host received. */
    val statusCode: Any?
}

/** Success callback accepted by [WxDownloadFileOptions]. */
internal typealias WxDownloadFileSuccessCallback = (WxDownloadFileSuccessResult) -> Unit

/**
 * Raw progress payload the host hands to a transfer task's progress listener.
 *
 * The upload and download directions report different byte field names, so all four
 * are declared here and the reader takes whichever pair the host supplied. The
 * installed base library's own transfer bookkeeping emits `progress` as a whole
 * percentage beside these byte counts.
 */
internal external interface WxTransferProgressResult {
    /** Documented type is `number`: percentage complete, in `0..100`. */
    val progress: Any?

    /** Documented type is `number`: upload bytes sent so far. */
    val totalBytesSent: Any?

    /** Documented type is `number`: upload bytes the host expects to send. */
    val totalBytesExpectedToSend: Any?

    /** Documented type is `number`: download bytes written so far. */
    val totalBytesWritten: Any?

    /** Documented type is `number`: download bytes the host expects to write. */
    val totalBytesExpectedToWrite: Any?
}

/** Progress listener signature accepted by a transfer task. */
internal typealias WxTransferProgressListener = (WxTransferProgressResult) -> Unit

/**
 * The task object [wx.uploadFile] and [wx.downloadFile] return.
 *
 * The installed base library lists `abort`, `onProgressUpdate`, `offProgressUpdate`,
 * `onHeadersReceived`, and `offHeadersReceived` as the members of both task types.
 * Only the first three are modelled: the SDK reports progress and can stop a transfer,
 * and it does not surface response headers for uploads or downloads.
 */
internal external interface WxTransferTask {
    /** Stops the transfer. Calling it more than once has no further effect. */
    fun abort()

    /** Registers a progress listener on this task. */
    fun onProgressUpdate(listener: WxTransferProgressListener)

    /** Removes a progress listener previously registered on this task. */
    fun offProgressUpdate(listener: WxTransferProgressListener)
}

/**
 * What a raw progress payload carries.
 *
 * Progress is advisory: it describes a transfer that is still running, not its
 * result. A payload the SDK cannot read is therefore reported as absent rather than
 * as a broken transfer, because failing a working transfer over an advisory figure
 * would be worse than reporting no figure at all.
 */
internal sealed interface WxTransferProgress {
    /** The host reported a readable progress figure. */
    data class Present(
        val percent: Int,
        val bytesTransferred: Long?,
        val bytesExpected: Long?,
    ) : WxTransferProgress

    /** The host reported something the SDK cannot read. */
    data object Unreadable : WxTransferProgress
}

/**
 * Reads a raw progress payload.
 *
 * `progress` must be a whole number in `0..100`; the byte counts are optional, and a
 * byte count the host reported as something other than a whole non-negative number is
 * treated as unreported rather than failing the figure. The upload and download
 * directions name their byte fields differently, so whichever pair is present is the
 * pair that is read.
 */
internal fun wxTransferProgress(result: WxTransferProgressResult): WxTransferProgress {
    val percent = wholePercentOrNull(result.progress) ?: return WxTransferProgress.Unreadable

    val sent = wholeCountOrNull(result.totalBytesSent)
    val expectedToSend = wholeCountOrNull(result.totalBytesExpectedToSend)
    val written = wholeCountOrNull(result.totalBytesWritten)
    val expectedToWrite = wholeCountOrNull(result.totalBytesExpectedToWrite)

    val transferred = sent ?: written
    val expected = expectedToSend ?: expectedToWrite

    return WxTransferProgress.Present(
        percent = percent,
        bytesTransferred = transferred,
        bytesExpected = expected,
    )
}

/** Reads a percentage that must be a whole number in `0..100`. */
private fun wholePercentOrNull(raw: Any?): Int? {
    val value = wholeNumberOrNull(raw) ?: return null
    if (value < 0L || value > 100L) return null
    return value.toInt()
}

/** Reads a byte count, or `null` when the host did not report one this SDK can carry. */
private fun wholeCountOrNull(raw: Any?): Long? = wholeNumberOrNull(raw)

/** Reads a whole non-negative number, or `null` when the value is not one. */
private fun wholeNumberOrNull(raw: Any?): Long? {
    if (raw == null || jsTypeOf(raw) == "undefined" || raw !is Number) return null
    val value = raw.toDouble()
    if (!value.isFinite() || value < 0.0 || value != kotlin.math.floor(value)) return null
    return value.toLong()
}

/** Whether [wx.uploadFile] exists. */
internal fun hasWxUploadFile(): Boolean =
    js("typeof wx !== 'undefined' && typeof wx.uploadFile === 'function'")

/** Whether [wx.downloadFile] exists. */
internal fun hasWxDownloadFile(): Boolean =
    js("typeof wx !== 'undefined' && typeof wx.downloadFile === 'function'")

/** Whether [task] carries both progress methods, so registering one can be paired. */
internal fun wxTransferTaskSupportsProgress(task: WxTransferTask): Boolean {
    if (jsTypeOf(task) != "object") return false
    val supported: Boolean =
        js("typeof task.onProgressUpdate === 'function' && typeof task.offProgressUpdate === 'function'")
    return supported
}

/**
 * Creates a plain JavaScript option bag for [wx.uploadFile].
 *
 * External interfaces cannot be instantiated with Kotlin constructors, so the
 * factory exists solely to produce the object the host expects. Optional fields are
 * assigned only when the caller supplied them.
 */
internal fun wxUploadFileOptions(
    url: String,
    filePath: String,
    name: String,
    header: Any?,
    formData: Any?,
    timeoutMillis: Int?,
): WxUploadFileOptions {
    val options: WxUploadFileOptions = js("({})")
    options.url = url
    options.filePath = filePath
    options.name = name
    if (header != null) options.header = header
    if (formData != null) options.formData = formData
    if (timeoutMillis != null) options.timeout = timeoutMillis
    return options
}

/**
 * Creates a plain JavaScript option bag for [wx.downloadFile].
 *
 * @param targetPath where the host should put the file, or `null` for its own
 *   temporary location
 */
internal fun wxDownloadFileOptions(
    url: String,
    header: Any?,
    timeoutMillis: Int?,
    targetPath: String?,
): WxDownloadFileOptions {
    val options: WxDownloadFileOptions = js("({})")
    options.url = url
    if (header != null) options.header = header
    if (timeoutMillis != null) options.timeout = timeoutMillis
    if (targetPath != null) options.filePath = targetPath
    return options
}

/** Reads the HTTP status a transfer reported, or `null` when it is not a usable one. */
internal fun wxTransferStatusCode(raw: Any?): Int? {
    val value = wholeNumberOrNull(raw) ?: return null
    if (value < 100L || value > 599L) return null
    return value.toInt()
}
