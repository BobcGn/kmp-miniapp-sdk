package io.github.bobcgn.miniapp.host.wechat.interop

/** Raw callback-style options accepted by [wx.showToast]. */
internal external interface WxShowToastOptions {
    /** Text displayed by the toast. */
    var title: String

    /**
     * Native icon selection: `success`, `error`, `loading`, or `none`.
     *
     * This remains a string because Kotlin/JS external interfaces cannot model a
     * TypeScript string-literal union without adding a runtime representation.
     */
    var icon: String?

    /** Optional custom icon image path; it takes precedence over [icon]. */
    var image: String?

    /** Display duration in milliseconds. */
    var duration: Int?

    /** Whether a transparent interaction-blocking mask is displayed. */
    var mask: Boolean?

    /** Called only when the toast request succeeds. */
    var success: WxGeneralCallback?

    /** Called only when the toast request fails. */
    var fail: WxGeneralCallback?

    /** Called after either [success] or [fail]. */
    var complete: WxGeneralCallback?
}

/**
 * External interfaces have no Kotlin constructor. `js("({})")` is deliberately
 * confined to this low-level interop package and only creates the plain option
 * bag expected by the WeChat runtime; it performs no host or business logic.
 * Optional properties remain absent (`undefined`) until explicitly assigned.
 *
 * @param title text displayed by the toast
 */
internal fun wxShowToastOptions(title: String): WxShowToastOptions {
    val options: WxShowToastOptions = js("({})")
    options.title = title
    return options
}
