package io.github.bobcgn.miniapp.host.wechat.interop

/** Raw callback-style options accepted by [wx.checkSession]. */
internal external interface WxCheckSessionOptions {
    /** Called only while the host still holds a valid login session. */
    var success: WxGeneralCallback?

    /** Called when the host reports the login session as no longer usable. */
    var fail: WxGeneralCallback?

    /** Called after either [success] or [fail]. */
    var complete: WxGeneralCallback?
}

/**
 * Whether [wx.checkSession] exists.
 *
 * The host offers no abort handle for this call, so there is nothing else to
 * probe.
 */
internal fun hasWxCheckSession(): Boolean =
    js("typeof wx !== 'undefined' && typeof wx.checkSession === 'function'")

/**
 * Creates a plain JavaScript option bag for [wx.checkSession].
 *
 * External interfaces cannot be instantiated with Kotlin constructors, so the
 * factory exists solely to produce the object the host expects.
 */
internal fun wxCheckSessionOptions(): WxCheckSessionOptions {
    val options: WxCheckSessionOptions = js("({})")
    return options
}
