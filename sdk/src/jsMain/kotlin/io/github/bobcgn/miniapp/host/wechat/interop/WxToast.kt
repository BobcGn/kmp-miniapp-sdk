package io.github.bobcgn.miniapp.host.wechat.interop

internal external interface WxShowToastOptions {
    var title: String

    /** One of `success`, `error`, `loading`, or `none`. */
    var icon: String?

    var image: String?
    var duration: Int?
    var mask: Boolean?
    var success: WxGeneralCallback?
    var fail: WxGeneralCallback?
    var complete: WxGeneralCallback?
}

/**
 * External interfaces have no Kotlin constructor. `js("({})")` is deliberately
 * confined to this low-level interop package and only creates the plain option
 * bag expected by the WeChat runtime; it performs no host or business logic.
 */
internal fun wxShowToastOptions(title: String): WxShowToastOptions {
    val options: WxShowToastOptions = js("({})")
    options.title = title
    return options
}
