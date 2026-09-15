package io.github.bobcgn.miniapp.host.wechat.interop

/**
 * The WeChat Mini Program global API object.
 *
 * This declaration intentionally models only the callback-style subset needed by
 * the first typed interop consumers. Promise return values are not exposed here.
 */
internal external object wx {
    /** Obtains a short-lived client login code through callbacks. */
    fun login(options: WxLoginOptions): Unit

    /** Requests a native toast using the supplied raw WeChat options. */
    fun showToast(options: WxShowToastOptions): Unit

    /** Reads a value from WeChat local storage through callbacks. */
    fun getStorage(options: WxGetStorageOptions): Unit

    /** Writes a value to WeChat local storage through callbacks. */
    fun setStorage(options: WxSetStorageOptions): Unit

    /** Removes a value from WeChat local storage through callbacks. */
    fun removeStorage(options: WxRemoveStorageOptions): Unit

    /**
     * Performs an HTTP exchange through callbacks.
     *
     * Unlike the other declarations here, this one returns a task handle so the
     * caller can abort an exchange that is still in flight.
     */
    fun request(options: WxRequestOptions): WxRequestTask

    /** Opens a page on top of the current page stack. */
    fun navigateTo(options: WxNavigateToOptions): Unit

    /** Replaces the current page, which is removed from the stack. */
    fun redirectTo(options: WxRedirectToOptions): Unit

    /** Pops the given number of pages off the current page stack. */
    fun navigateBack(options: WxNavigateBackOptions): Unit
}
