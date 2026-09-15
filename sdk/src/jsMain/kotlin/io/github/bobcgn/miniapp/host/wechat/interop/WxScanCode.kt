package io.github.bobcgn.miniapp.host.wechat.interop

import kotlin.js.jsTypeOf

/**
 * Raw callback-style options accepted by [wx.scanCode].
 *
 * [scanType] is declared [Any] because the host expects a plain JavaScript array
 * of category names; the factory is the only place one is built.
 */
internal external interface WxScanCodeOptions {
    /**
     * Whether the host may scan only through its camera.
     *
     * When false the host may also offer an image the user already has.
     */
    var onlyFromCamera: Boolean?

    /** Categories the host may report, as a plain JavaScript array of strings. */
    var scanType: Any?

    /** Called only when the host decoded something. */
    var success: WxScanCodeSuccessCallback?

    /** Called when nothing was decoded, including when the user dismissed the interface. */
    var fail: WxGeneralCallback?

    /** Called after either [success] or [fail]. */
    var complete: WxGeneralCallback?
}

/**
 * Raw success result returned by [wx.scanCode].
 *
 * Every field is declared [Any] rather than the documented `string`, because the
 * host may omit one, and a value of the wrong type would otherwise be accepted
 * silently. [wxScanSample] is the only reader.
 */
internal external interface WxScanCodeSuccessResult : WxGeneralCallbackResult {
    /** Documented type is `string`: the decoded content. */
    val result: Any?

    /** Documented type is `string`: the host's own name for the format it decoded. */
    val scanType: Any?

    /** Documented type is `string`: the character set of the decoded content. */
    val charSet: Any?

    /** Documented type is `string`: the raw decoded bytes. */
    val rawData: Any?

    /** Documented type is `string`: a path to the scanned image, when the host reports one. */
    val path: Any?
}

/** Success callback accepted by [WxScanCodeOptions]. */
internal typealias WxScanCodeSuccessCallback = (WxScanCodeSuccessResult) -> Unit

/**
 * What a raw [wx.scanCode] answer carries.
 *
 * A raw JavaScript object cannot cross out of this package, so this is the typed
 * shape the adapter reads.
 */
internal sealed interface WxScanSample {
    /**
     * The host decoded something.
     *
     * Only [text] is required: it is the answer. The remaining fields describe
     * that answer, and the host is documented as reporting them but not as always
     * doing so, so an absent one stays absent rather than becoming an empty string
     * that is indistinguishable from a real empty value.
     */
    data class Present(
        val text: String,
        val scanType: String?,
        val charSet: String?,
        val rawData: String?,
        val path: String?,
    ) : WxScanSample

    /** The host's answer is not a value the contract can carry. */
    data object Unreadable : WxScanSample
}

/**
 * Reads a raw [wx.scanCode] answer.
 *
 * This is the only place the raw result is inspected. The decoded content is the
 * point of the call, so a missing or non-string `result` is [WxScanSample.Unreadable]
 * rather than an empty scan. A descriptive field that is absent is passed through
 * as absent, while one the host reported with the wrong type is not: the host
 * broke its own contract, and dropping the value would hide that.
 */
internal fun wxScanSample(result: WxScanCodeSuccessResult): WxScanSample {
    val text = result.result
    if (text !is String) return WxScanSample.Unreadable

    val scanType = optionalText(result.scanType)
    val charSet = optionalText(result.charSet)
    val rawData = optionalText(result.rawData)
    val path = optionalText(result.path)
    if (scanType.isWrongType || charSet.isWrongType || rawData.isWrongType || path.isWrongType) {
        return WxScanSample.Unreadable
    }

    return WxScanSample.Present(
        text = text,
        scanType = scanType.textOrNull,
        charSet = charSet.textOrNull,
        rawData = rawData.textOrNull,
        path = path.textOrNull,
    )
}

/** One optional field of a host answer. */
private sealed interface WxOptionalText {
    /** The host reported text. */
    data class Present(val text: String) : WxOptionalText

    /** The host did not report the field at all. */
    data object Absent : WxOptionalText

    /** The host reported the field as something other than text. */
    data object WrongType : WxOptionalText
}

private fun optionalText(value: Any?): WxOptionalText = when {
    value == null || jsTypeOf(value) == "undefined" -> WxOptionalText.Absent
    value is String -> WxOptionalText.Present(value)
    else -> WxOptionalText.WrongType
}

private val WxOptionalText.isWrongType: Boolean
    get() = this is WxOptionalText.WrongType

private val WxOptionalText.textOrNull: String?
    get() = (this as? WxOptionalText.Present)?.text

/**
 * Whether [wx.scanCode] exists.
 *
 * Scanning uses the host's own interface, so there is no separate object or
 * method behind it to probe as well.
 */
internal fun hasWxScanCode(): Boolean =
    js("typeof wx !== 'undefined' && typeof wx.scanCode === 'function'")

/**
 * Creates a plain JavaScript option bag for [wx.scanCode].
 *
 * An empty [scanCategories] leaves `scanType` unset, which is how the host is
 * asked for every category it supports. Sending an empty array instead would ask
 * it for none. `onlyFromCamera` is always set, so the caller's answer is never
 * silently replaced by the host's default.
 *
 * External interfaces cannot be instantiated with Kotlin constructors, so the
 * factory exists solely to produce the object the host expects.
 *
 * @param onlyFromCamera whether the host may scan only through its camera
 * @param scanCategories category values to ask for, in the order given
 */
internal fun wxScanCodeOptions(
    onlyFromCamera: Boolean,
    scanCategories: List<String>,
): WxScanCodeOptions {
    val options: WxScanCodeOptions = js("({})")
    options.onlyFromCamera = onlyFromCamera
    if (scanCategories.isNotEmpty()) {
        val categories: Array<String> = scanCategories.toTypedArray()
        js("options.scanType = categories")
    }
    return options
}
