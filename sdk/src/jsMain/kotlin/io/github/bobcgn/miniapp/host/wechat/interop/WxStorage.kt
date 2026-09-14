package io.github.bobcgn.miniapp.host.wechat.interop

internal external interface WxGetStorageOptions {
    var key: String
    var success: WxGetStorageSuccessCallback?
    var fail: WxGeneralCallback?
    var complete: WxGeneralCallback?
}

internal external interface WxSetStorageOptions {
    var key: String
    var data: Any?
    var success: WxGeneralCallback?
    var fail: WxGeneralCallback?
    var complete: WxGeneralCallback?
}

internal external interface WxRemoveStorageOptions {
    var key: String
    var success: WxGeneralCallback?
    var fail: WxGeneralCallback?
    var complete: WxGeneralCallback?
}

/** Plain-object factories for the raw WeChat option contracts. */
internal fun wxGetStorageOptions(key: String): WxGetStorageOptions {
    val options: WxGetStorageOptions = js("({})")
    options.key = key
    return options
}

internal fun wxSetStorageOptions(key: String, data: Any?): WxSetStorageOptions {
    val options: WxSetStorageOptions = js("({})")
    options.key = key
    options.data = data
    return options
}

internal fun wxRemoveStorageOptions(key: String): WxRemoveStorageOptions {
    val options: WxRemoveStorageOptions = js("({})")
    options.key = key
    return options
}
