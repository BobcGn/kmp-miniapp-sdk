package io.github.bobcgn.miniapp.host.wechat.interop

/**
 * The WeChat Mini Program global API object.
 *
 * This declaration intentionally models only the callback-style subset needed by
 * the first typed interop consumers. Promise return values are not exposed here.
 */
internal external object wx {
    fun showToast(options: WxShowToastOptions): Unit

    fun getStorage(options: WxGetStorageOptions): Unit

    fun setStorage(options: WxSetStorageOptions): Unit

    fun removeStorage(options: WxRemoveStorageOptions): Unit
}
