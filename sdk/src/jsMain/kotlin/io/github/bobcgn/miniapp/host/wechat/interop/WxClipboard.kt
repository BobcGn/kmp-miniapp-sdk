package io.github.bobcgn.miniapp.host.wechat.interop

import kotlin.js.jsTypeOf

/** Raw callback-style options accepted by [wx.getClipboardData]. */
internal external interface WxGetClipboardDataOptions {
    /** Called only when the host returns the clipboard contents. */
    var success: WxGetClipboardDataSuccessCallback?

    /** Called when the clipboard cannot be read. */
    var fail: WxGeneralCallback?

    /** Called after either [success] or [fail]. */
    var complete: WxGeneralCallback?
}

/**
 * Raw success result returned by [wx.getClipboardData].
 *
 * The documented type is `string`, but it is declared [Any] here for the same
 * reason WeChat storage declares its value that way: an external property the
 * host did not set reads as `undefined`, and a value of the wrong type would
 * otherwise be accepted silently. [wxClipboardText] is the only reader.
 */
internal external interface WxGetClipboardDataSuccessResult : WxGeneralCallbackResult {
    /** Documented type is `string`: the clipboard contents. */
    val data: Any?
}

/** Raw callback-style options accepted by [wx.setClipboardData]. */
internal external interface WxSetClipboardDataOptions {
    /** Text to place on the clipboard. */
    var data: String

    /** Called only when the host stored the text. */
    var success: WxGeneralCallback?

    /** Called when the text cannot be stored. */
    var fail: WxGeneralCallback?

    /** Called after either [success] or [fail]. */
    var complete: WxGeneralCallback?
}

/** Success callback accepted by [WxGetClipboardDataOptions]. */
internal typealias WxGetClipboardDataSuccessCallback = (WxGetClipboardDataSuccessResult) -> Unit

/**
 * What a raw [wx.getClipboardData] answer carries.
 *
 * A raw JavaScript object cannot cross out of this package, so this is the typed
 * shape the adapter reads. [Unreadable] is deliberately separate from a present
 * but empty string: an empty clipboard is a value, while a missing or
 * non-string one is an answer the contract cannot carry.
 */
internal sealed interface WxClipboardText {
    /** The host returned text, which may be empty. */
    data class Present(val text: String) : WxClipboardText

    /** The host's answer is not a value the contract can carry. */
    data object Unreadable : WxClipboardText
}

/**
 * Reads a raw [wx.getClipboardData] answer.
 *
 * This is the only place the raw result is inspected, so a missing `data` or a
 * non-string one becomes [WxClipboardText.Unreadable] instead of being coerced
 * to an empty string, which would be indistinguishable from an empty clipboard.
 */
internal fun wxClipboardText(result: WxGetClipboardDataSuccessResult): WxClipboardText {
    val data = result.data
    return when {
        data == null || jsTypeOf(data) == "undefined" -> WxClipboardText.Unreadable
        data is String -> WxClipboardText.Present(data)
        else -> WxClipboardText.Unreadable
    }
}

/**
 * Whether [wx.getClipboardData] exists.
 *
 * Reading the clipboard is a separate host capability from writing it, so it is
 * probed on its own rather than assumed to come with the write API.
 */
internal fun hasWxGetClipboardData(): Boolean =
    js("typeof wx !== 'undefined' && typeof wx.getClipboardData === 'function'")

/** Whether [wx.setClipboardData] exists. */
internal fun hasWxSetClipboardData(): Boolean =
    js("typeof wx !== 'undefined' && typeof wx.setClipboardData === 'function'")

/**
 * Creates a plain JavaScript option bag for [wx.getClipboardData].
 *
 * External interfaces cannot be instantiated with Kotlin constructors, so the
 * factory exists solely to produce the object the host expects.
 */
internal fun wxGetClipboardDataOptions(): WxGetClipboardDataOptions {
    val options: WxGetClipboardDataOptions = js("({})")
    return options
}

/**
 * Creates a plain JavaScript option bag for [wx.setClipboardData].
 *
 * @param data text to place on the clipboard
 */
internal fun wxSetClipboardDataOptions(data: String): WxSetClipboardDataOptions {
    val options: WxSetClipboardDataOptions = js("({})")
    options.data = data
    return options
}
