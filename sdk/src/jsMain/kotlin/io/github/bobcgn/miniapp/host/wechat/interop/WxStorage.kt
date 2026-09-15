package io.github.bobcgn.miniapp.host.wechat.interop

/** Raw callback-style options accepted by [wx.getStorage]. */
internal external interface WxGetStorageOptions {
    /** Storage key to read. */
    var key: String

    /** Called only when the value is read successfully. */
    var success: WxGetStorageSuccessCallback?

    /** Called only when the read fails. */
    var fail: WxGeneralCallback?

    /** Called after either [success] or [fail]. */
    var complete: WxGeneralCallback?
}

/** Raw callback-style options accepted by [wx.setStorage]. */
internal external interface WxSetStorageOptions {
    /** Storage key to write. */
    var key: String

    /** Raw JavaScript-compatible value to store. */
    var data: Any?

    /** Called only when the value is written successfully. */
    var success: WxGeneralCallback?

    /** Called only when the write fails. */
    var fail: WxGeneralCallback?

    /** Called after either [success] or [fail]. */
    var complete: WxGeneralCallback?
}

/** Raw callback-style options accepted by [wx.removeStorage]. */
internal external interface WxRemoveStorageOptions {
    /** Storage key to remove. */
    var key: String

    /** Called only when the value is removed successfully. */
    var success: WxGeneralCallback?

    /** Called only when the removal fails. */
    var fail: WxGeneralCallback?

    /** Called after either [success] or [fail]. */
    var complete: WxGeneralCallback?
}

/**
 * Creates a plain JavaScript option object for [wx.getStorage].
 *
 * Optional properties are deliberately left absent (`undefined`) until a caller
 * assigns them. External interfaces cannot be instantiated with Kotlin constructors.
 */
internal fun wxGetStorageOptions(key: String): WxGetStorageOptions {
    val options: WxGetStorageOptions = js("({})")
    options.key = key
    return options
}

/**
 * Creates a plain JavaScript option object for [wx.setStorage].
 *
 * The factory initializes only the two fields required by the raw host contract.
 */
internal fun wxSetStorageOptions(key: String, data: Any?): WxSetStorageOptions {
    val options: WxSetStorageOptions = js("({})")
    options.key = key
    options.data = data
    return options
}

/**
 * Creates a plain JavaScript option object for [wx.removeStorage].
 *
 * Optional callback properties remain absent until explicitly assigned.
 */
internal fun wxRemoveStorageOptions(key: String): WxRemoveStorageOptions {
    val options: WxRemoveStorageOptions = js("({})")
    options.key = key
    return options
}
