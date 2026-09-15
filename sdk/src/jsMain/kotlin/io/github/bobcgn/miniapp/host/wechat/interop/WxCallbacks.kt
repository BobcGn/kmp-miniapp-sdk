package io.github.bobcgn.miniapp.host.wechat.interop

/** Common result supplied by WeChat success, failure, and completion callbacks. */
internal external interface WxGeneralCallbackResult {
    /** Host-provided status text, for example `showToast:ok`. */
    val errMsg: String
}

/** Result supplied when [wx.getStorage] succeeds. */
internal external interface WxGetStorageSuccessResult {
    /**
     * The value stored by the mini program.
     *
     * WeChat storage accepts multiple JavaScript value shapes, so the raw host
     * contract remains [Any]. A later adapter is responsible for validation and
     * conversion into SDK-owned types.
     */
    val data: Any?

    /** Host-provided status text, normally `getStorage:ok`. */
    val errMsg: String
}

/** Callback used for general WeChat operation results. */
internal typealias WxGeneralCallback = (WxGeneralCallbackResult) -> Unit

/** Success callback used by [wx.getStorage]. */
internal typealias WxGetStorageSuccessCallback = (WxGetStorageSuccessResult) -> Unit
