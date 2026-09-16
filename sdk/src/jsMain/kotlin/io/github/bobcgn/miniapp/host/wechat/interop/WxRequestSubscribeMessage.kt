package io.github.bobcgn.miniapp.host.wechat.interop

import kotlin.js.jsTypeOf

/**
 * Raw callback-style options accepted by [wx.requestSubscribeMessage].
 *
 * [tmplIds] is declared [Any] because the host expects a plain JavaScript array of
 * strings; the factory is the only place one is built.
 */
internal external interface WxRequestSubscribeMessageOptions {
    /** Template identifiers the host should ask the user about, as a JavaScript array. */
    var tmplIds: Any?

    /** Called only when the host asked the user about the templates. */
    var success: WxRequestSubscribeMessageSuccessCallback?

    /** Called when no answer was obtained, including when the prompt was dismissed. */
    var fail: WxGeneralCallback?

    /** Called after either [success] or [fail]. */
    var complete: WxGeneralCallback?
}

/**
 * Raw success result returned by [wx.requestSubscribeMessage].
 *
 * WeChat does not nest the per-template answers: it puts them on this object
 * directly, keyed by the template ID it answered about, with `errMsg` beside them.
 * Because the keys are host data rather than a fixed shape, no member is declared
 * here and [wxSubscriptionAnswer] reads the object as a whole.
 */
internal external interface WxRequestSubscribeMessageSuccessResult : WxGeneralCallbackResult

/** Success callback accepted by [WxRequestSubscribeMessageOptions]. */
internal typealias WxRequestSubscribeMessageSuccessCallback =
    (WxRequestSubscribeMessageSuccessResult) -> Unit

/**
 * What a raw [wx.requestSubscribeMessage] answer carries.
 *
 * A raw JavaScript object cannot cross out of this package, so this is the typed
 * shape the adapter reads. [Present] can be empty at this boundary, but the adapter
 * validates it against the requested template set before exposing a result.
 */
internal sealed interface WxSubscriptionAnswer {
    /**
     * The host answered, keyed by the template ID it answered about.
     *
     * `errMsg` is deliberately absent: it is the host's own status line for the call,
     * not an answer about a template, and reading it as one would invent a template
     * the caller never asked about.
     */
    data class Present(val entries: Map<String, WxSubscriptionEntry>) : WxSubscriptionAnswer

    /** The host's answer is not a value the contract can carry. */
    data object Unreadable : WxSubscriptionAnswer
}

/**
 * One template's entry of a raw [WxSubscriptionAnswer].
 *
 * The distinction matters to the adapter, which rejects non-text values and validates
 * that the host returned exactly the requested template keys.
 */
internal sealed interface WxSubscriptionEntry {
    /** The host reported a status string for this template. */
    data class Text(val value: String) : WxSubscriptionEntry

    /** The host reported something for this template that is not a status string. */
    data object NotText : WxSubscriptionEntry
}

/**
 * Reads a raw [wx.requestSubscribeMessage] answer.
 *
 * This is the only place the raw answer is inspected. The host keys the answer by
 * template ID, so the object is read by name rather than by position, and the
 * status line the host adds alongside those keys is skipped rather than mistaken
 * for one of them.
 */
internal fun wxSubscriptionAnswer(
    result: WxRequestSubscribeMessageSuccessResult,
): WxSubscriptionAnswer {
    if (result.asDynamic() == null || jsTypeOf(result) != "object") {
        return WxSubscriptionAnswer.Unreadable
    }

    val keys: Array<String> = js("Object.keys(result)")
    val entries = mutableMapOf<String, WxSubscriptionEntry>()
    for (key in keys) {
        if (key == STATUS_LINE_KEY) continue
        val value: Any? = js("result[key]")
        entries[key] = if (value is String) {
            WxSubscriptionEntry.Text(value)
        } else {
            WxSubscriptionEntry.NotText
        }
    }
    return WxSubscriptionAnswer.Present(entries)
}

/**
 * The key WeChat uses for its own status line on the success result.
 *
 * It sits in the same object as the per-template answers, so it is excluded by name
 * before anything else is treated as a template ID.
 */
private const val STATUS_LINE_KEY: String = "errMsg"

/**
 * Whether [wx.requestSubscribeMessage] exists.
 *
 * Subscription requests run through the host's own prompt, so there is no separate
 * object or method behind it to probe as well.
 */
internal fun hasWxRequestSubscribeMessage(): Boolean =
    js("typeof wx !== 'undefined' && typeof wx.requestSubscribeMessage === 'function'")

/**
 * Creates a plain JavaScript option bag for [wx.requestSubscribeMessage].
 *
 * External interfaces cannot be instantiated with Kotlin constructors, so the
 * factory exists solely to produce the object the host expects.
 *
 * @param templateIds template identifiers to ask the host about, in the order given
 */
internal fun wxRequestSubscribeMessageOptions(
    templateIds: List<String>,
): WxRequestSubscribeMessageOptions {
    val options: WxRequestSubscribeMessageOptions = js("({})")
    val ids: Array<String> = templateIds.toTypedArray()
    js("options.tmplIds = ids")
    return options
}
