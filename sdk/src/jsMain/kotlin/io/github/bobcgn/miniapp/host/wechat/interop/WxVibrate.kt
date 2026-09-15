package io.github.bobcgn.miniapp.host.wechat.interop

/**
 * Raw callback-style options accepted by [wx.vibrateShort] and [wx.vibrateLong].
 *
 * Both APIs take the same callback shape and neither is given the optional
 * `type` field `wx.vibrateShort` documents: the SDK models "short" and "long"
 * as two separate calls, and choosing an intensity is a decision the host
 * already makes sensibly on its own. See the capability matrix for that fact.
 */
internal external interface WxVibrateOptions {
    /** Called only when the host accepted and performed the vibration. */
    var success: WxGeneralCallback?

    /** Called when the vibration cannot be performed. */
    var fail: WxGeneralCallback?

    /** Called after either [success] or [fail]. */
    var complete: WxGeneralCallback?
}

/**
 * Whether [wx.vibrateShort] exists.
 *
 * The two vibrations are independent host capabilities, so each is probed on its
 * own rather than assumed to accompany the other.
 */
internal fun hasWxVibrateShort(): Boolean =
    js("typeof wx !== 'undefined' && typeof wx.vibrateShort === 'function'")

/** Whether [wx.vibrateLong] exists. */
internal fun hasWxVibrateLong(): Boolean =
    js("typeof wx !== 'undefined' && typeof wx.vibrateLong === 'function'")

/**
 * Creates a plain JavaScript option bag for [wx.vibrateShort] or [wx.vibrateLong].
 *
 * External interfaces cannot be instantiated with Kotlin constructors, so the
 * factory exists solely to produce the object the host expects.
 */
internal fun wxVibrateOptions(): WxVibrateOptions {
    val options: WxVibrateOptions = js("({})")
    return options
}
