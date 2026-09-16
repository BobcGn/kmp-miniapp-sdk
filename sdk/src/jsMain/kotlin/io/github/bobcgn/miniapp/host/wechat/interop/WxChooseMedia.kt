package io.github.bobcgn.miniapp.host.wechat.interop

import kotlin.js.jsTypeOf

/**
 * Raw callback-style options accepted by [wx.chooseMedia].
 *
 * The array-valued fields are declared [Any] because the host expects plain
 * JavaScript arrays of strings; the factory is the only place one is built. Every
 * field is left unset unless the caller asked for it, so the host's own default
 * applies rather than one this SDK invented.
 */
internal external interface WxChooseMediaOptions {
    /** Maximum number of files the host may return. */
    var count: Int?

    /** Media types the host may return, as a plain JavaScript array of strings. */
    var mediaType: Any?

    /** Where the host may take media from, as a plain JavaScript array of strings. */
    var sourceType: Any?

    /** Longest video recording the host may take, in seconds. */
    var maxDuration: Int?

    /** Whether the host compresses the images it returns, as a plain array of strings. */
    var sizeType: Any?

    /** Which camera the host uses, when it uses one. */
    var camera: String?

    /** Called only when the host returned a selection. */
    var success: WxChooseMediaSuccessCallback?

    /** Called when no selection was returned, including when the user dismissed the interface. */
    var fail: WxGeneralCallback?

    /** Called after either [success] or [fail]. */
    var complete: WxGeneralCallback?
}

/**
 * Raw success result returned by [wx.chooseMedia].
 *
 * [tempFiles] is declared [Any] because it is a JavaScript array of host objects;
 * [wxMediaSelection] is the only reader.
 */
internal external interface WxChooseMediaSuccessResult : WxGeneralCallbackResult {
    /** The files the host returned, as a JavaScript array of objects. */
    val tempFiles: Any?
}

/**
 * One entry of the raw `tempFiles` array.
 *
 * `tempFilePath`, `size`, and `fileType` are the fields the host sets for every
 * entry; the remaining four describe a video and are absent for an image. Every
 * field is declared [Any] rather than its documented type, because the host may
 * omit one and a value of the wrong type would otherwise pass silently.
 */
internal external interface WxChooseMediaTempFile {
    /** Documented type is `string`: the temporary path of the selected file. */
    val tempFilePath: Any?

    /** Documented type is `number`: the size of the file in bytes. */
    val size: Any?

    /** Documented type is `string`: the host's own name for the file's kind. */
    val fileType: Any?

    /** Documented type is `number`: a video's duration in seconds. */
    val duration: Any?

    /** Documented type is `number`: a video's width in pixels. */
    val width: Any?

    /** Documented type is `number`: a video's height in pixels. */
    val height: Any?

    /** Documented type is `string`: a temporary path to a video's thumbnail. */
    val thumbTempFilePath: Any?
}

/** Success callback accepted by [WxChooseMediaOptions]. */
internal typealias WxChooseMediaSuccessCallback = (WxChooseMediaSuccessResult) -> Unit

/**
 * What a raw [wx.chooseMedia] answer carries.
 *
 * A raw JavaScript object cannot cross out of this package, so this is the typed
 * shape the adapter reads. The adapter rejects an empty [Present] selection because
 * a successful picker interaction must identify at least one selected file.
 */
internal sealed interface WxMediaSelection {
    /**
     * The host returned a selection, which may contain no files.
     *
     * The entries are [WxMediaFile.Present] because a selection with an unreadable
     * entry is not reported as a partial list at all: it becomes [Unreadable].
     */
    data class Present(val files: List<WxMediaFile.Present>) : WxMediaSelection

    /** The host's answer is not a value the contract can carry. */
    data object Unreadable : WxMediaSelection
}

/**
 * One file of a raw [WxMediaSelection].
 *
 * [tempFilePath], [sizeBytes], and [fileType] are required. The remaining fields
 * are what the host reported, and a field it did not report stays absent rather
 * than becoming a fabricated zero: an image has no duration and an unreported
 * dimension is not a dimension of zero.
 */
internal sealed interface WxMediaFile {
    /** The host reported an entry with the fields the contract requires. */
    data class Present(
        val tempFilePath: String,
        val sizeBytes: Long,
        val fileType: String,
        val durationSeconds: Double?,
        val width: Double?,
        val height: Double?,
        val thumbTempFilePath: String?,
    ) : WxMediaFile

    /** The entry is not a value the contract can carry. */
    data object Unreadable : WxMediaFile
}

/**
 * Reads a raw [wx.chooseMedia] answer.
 *
 * This is the only place the raw selection is inspected. A `tempFiles` that is not
 * an array, or any entry that breaks the contract, makes the whole answer
 * [WxMediaSelection.Unreadable]: a partial list would hide that the host answered
 * something it cannot mean.
 */
internal fun wxMediaSelection(result: WxChooseMediaSuccessResult): WxMediaSelection {
    val tempFiles = result.tempFiles
    if (!isJsArray(tempFiles)) return WxMediaSelection.Unreadable

    val length: Int = js("tempFiles.length")
    val files = mutableListOf<WxMediaFile.Present>()
    for (index in 0 until length) {
        val entry: Any? = js("tempFiles[index]")
        when (val file = wxMediaFile(entry)) {
            is WxMediaFile.Present -> files += file
            WxMediaFile.Unreadable -> return WxMediaSelection.Unreadable
        }
    }
    return WxMediaSelection.Present(files)
}

/**
 * Reads one raw `tempFiles` entry.
 *
 * A path is not a payload: an empty one is not a file the caller can use, so a
 * missing, non-string, or empty `tempFilePath` fails the entry. A file size must
 * be a whole, non-negative number of bytes, and a descriptive number the host
 * reported must be finite and non-negative. A descriptive field the host left out
 * stays absent, while one it reported with the wrong type fails the entry: the
 * host broke its own contract, and dropping the value would hide that.
 */
private fun wxMediaFile(entry: Any?): WxMediaFile {
    if (entry == null || jsTypeOf(entry) != "object") return WxMediaFile.Unreadable

    val path: Any? = js("entry.tempFilePath")
    if (path !is String || path.isEmpty()) return WxMediaFile.Unreadable

    val size: Any? = js("entry.size")
    val sizeBytes = wholeNumberOrNull(size) ?: return WxMediaFile.Unreadable

    val fileType: Any? = js("entry.fileType")
    if (fileType !is String || fileType.isEmpty()) return WxMediaFile.Unreadable

    val fields = MediaFields()
    val duration = fields.number(js("entry.duration"))
    val width = fields.number(js("entry.width"))
    val height = fields.number(js("entry.height"))
    val thumbPath = fields.text(js("entry.thumbTempFilePath"))
    if (fields.brokenContract) return WxMediaFile.Unreadable

    return WxMediaFile.Present(
        tempFilePath = path,
        sizeBytes = sizeBytes,
        fileType = fileType,
        durationSeconds = duration,
        width = width,
        height = height,
        thumbTempFilePath = thumbPath,
    )
}

/**
 * The optional fields of one entry, and whether the host broke the contract.
 *
 * An absent field is not a violation: the host is documented as reporting a
 * video's descriptive fields but not as always doing so. A field present with the
 * wrong type, or with a number that cannot describe media, is.
 */
private class MediaFields {
    /** Whether the host reported a field it could not have meant. */
    var brokenContract: Boolean = false
        private set

    /** Reads an optional text field. */
    fun text(raw: Any?): String? = when {
        isAbsent(raw) -> null
        raw is String && raw.isNotEmpty() -> raw
        else -> fail()
    }

    /** Reads an optional number that must be finite and non-negative. */
    fun number(raw: Any?): Double? = when {
        isAbsent(raw) -> null
        raw is Number -> {
            val value = raw.toDouble()
            if (value.isFinite() && value >= 0.0) value else fail()
        }

        else -> fail()
    }

    private fun fail(): Nothing? {
        brokenContract = true
        return null
    }

    private fun isAbsent(raw: Any?): Boolean = raw == null || jsTypeOf(raw) == "undefined"
}

/** Whether [value] is a JavaScript array. */
private fun isJsArray(value: Any?): Boolean {
    if (value == null || jsTypeOf(value) != "object") return false
    val result: Boolean = js("Array.isArray(value)")
    return result
}

/** Reads a number that must be a whole, non-negative safe integer, or `null`. */
private fun wholeNumberOrNull(raw: Any?): Long? {
    if (raw == null || jsTypeOf(raw) == "undefined" || raw !is Number) return null
    val value = raw.toDouble()
    if (
        !value.isFinite() ||
        value < 0.0 ||
        value > MAX_SAFE_INTEGER ||
        value != kotlin.math.floor(value)
    ) return null
    return value.toLong()
}

/** Largest integer a JavaScript number can carry without losing byte precision. */
private const val MAX_SAFE_INTEGER: Double = 9_007_199_254_740_991.0

/**
 * Whether [wx.chooseMedia] exists.
 *
 * Selection runs through the host's own interface, so there is no separate object
 * or method behind it to probe as well.
 */
internal fun hasWxChooseMedia(): Boolean =
    js("typeof wx !== 'undefined' && typeof wx.chooseMedia === 'function'")

/**
 * Creates a plain JavaScript option bag for [wx.chooseMedia].
 *
 * Every optional field is assigned only when the caller supplied it, so an
 * unrequested option keeps the host's own default rather than a value this SDK
 * invented. External interfaces cannot be instantiated with Kotlin constructors,
 * so the factory exists solely to produce the object the host expects.
 *
 * @param count maximum number of files to ask for
 * @param mediaTypes media type values to ask for, in the order given
 * @param sourceTypes source values to ask for; empty means no restriction
 * @param maxDurationSeconds longest video recording to ask for, in seconds
 * @param sizeTypes size-type values to ask for; empty means no restriction
 * @param camera camera position to ask for, or `null` for no preference
 */
internal fun wxChooseMediaOptions(
    count: Int,
    mediaTypes: List<String>,
    sourceTypes: List<String>,
    maxDurationSeconds: Int?,
    sizeTypes: List<String>,
    camera: String?,
): WxChooseMediaOptions {
    val options: WxChooseMediaOptions = js("({})")
    options.count = count
    val mediaTypeValues: Array<String> = mediaTypes.toTypedArray()
    js("options.mediaType = mediaTypeValues")
    if (sourceTypes.isNotEmpty()) {
        val sourceTypeValues: Array<String> = sourceTypes.toTypedArray()
        js("options.sourceType = sourceTypeValues")
    }
    if (maxDurationSeconds != null) {
        options.maxDuration = maxDurationSeconds
    }
    if (sizeTypes.isNotEmpty()) {
        val sizeTypeValues: Array<String> = sizeTypes.toTypedArray()
        js("options.sizeType = sizeTypeValues")
    }
    if (camera != null) {
        options.camera = camera
    }
    return options
}
