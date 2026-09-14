package io.github.bobcgn.miniapp.host.wechat.interop

internal external interface WxGeneralCallbackResult {
    val errMsg: String
}

internal external interface WxGetStorageSuccessResult {
    val data: Any?
    val errMsg: String
}

internal typealias WxGeneralCallback = (WxGeneralCallbackResult) -> Unit

internal typealias WxGetStorageSuccessCallback = (WxGetStorageSuccessResult) -> Unit
