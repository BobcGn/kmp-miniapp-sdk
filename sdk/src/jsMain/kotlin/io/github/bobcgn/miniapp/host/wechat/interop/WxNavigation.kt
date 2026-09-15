package io.github.bobcgn.miniapp.host.wechat.interop

/** Raw callback-style options accepted by [wx.navigateTo]. */
internal external interface WxNavigateToOptions {
    /** Route of the page to open, relative to the mini program root. */
    var url: String

    /** Called only when the page is opened. */
    var success: WxGeneralCallback?

    /** Called only when the page cannot be opened, for example when the stack is full. */
    var fail: WxGeneralCallback?

    /** Called after either [success] or [fail]. */
    var complete: WxGeneralCallback?
}

/** Raw callback-style options accepted by [wx.redirectTo]. */
internal external interface WxRedirectToOptions {
    /** Route of the page that replaces the current one. */
    var url: String

    /** Called only when the current page is replaced. */
    var success: WxGeneralCallback?

    /** Called only when the page cannot be replaced. */
    var fail: WxGeneralCallback?

    /** Called after either [success] or [fail]. */
    var complete: WxGeneralCallback?
}

/**
 * Raw callback-style options accepted by [wx.navigateBack].
 *
 * WeChat pages form a stack, so going back is expressed as a number of pages
 * rather than as a target route.
 */
internal external interface WxNavigateBackOptions {
    /** Number of pages to go back; the host default is one. */
    var delta: Int?

    /** Called only when the stack was popped. */
    var success: WxGeneralCallback?

    /** Called only when no page can be popped, for example on the first page. */
    var fail: WxGeneralCallback?

    /** Called after either [success] or [fail]. */
    var complete: WxGeneralCallback?
}

/**
 * Creates a plain JavaScript option bag for [wx.navigateTo].
 *
 * Optional fields are assigned only when a caller supplies a value, so absent
 * fields keep their native WeChat defaults.
 *
 * @param url route of the page to open
 */
internal fun wxNavigateToOptions(url: String): WxNavigateToOptions {
    val options: WxNavigateToOptions = js("({})")
    options.url = url
    return options
}

/**
 * Creates a plain JavaScript option bag for [wx.redirectTo].
 *
 * @param url route of the page that replaces the current one
 */
internal fun wxRedirectToOptions(url: String): WxRedirectToOptions {
    val options: WxRedirectToOptions = js("({})")
    options.url = url
    return options
}

/**
 * Creates a plain JavaScript option bag for [wx.navigateBack].
 *
 * @param delta number of pages to go back, or `null` to apply WeChat's default of one
 */
internal fun wxNavigateBackOptions(delta: Int? = null): WxNavigateBackOptions {
    val options: WxNavigateBackOptions = js("({})")
    if (delta != null) {
        options.delta = delta
    }
    return options
}
