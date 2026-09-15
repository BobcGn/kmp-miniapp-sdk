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

    /**
     * Reports whether an API, parameter, or component exists in this base library.
     *
     * The call is synchronous. A base library older than the one that introduced
     * `canIUse` has no such member at all, so callers must probe for it first.
     */
    fun canIUse(schema: String): Boolean

    /** Returns app and base-library information. Absent before base library 2.20.1. */
    fun getAppBaseInfo(): WxAppBaseInfo

    /**
     * Returns legacy system information, including the base-library version.
     *
     * Unmaintained since base library 2.20.1, but it is the only source of the
     * version on base libraries that predate [getAppBaseInfo].
     */
    fun getSystemInfoSync(): WxSystemInfo

    /** Returns device information. Absent before base library 2.20.1. */
    fun getDeviceInfo(): WxDeviceInfo

    /** Reads the authorization state the host holds for each scope. */
    fun getSetting(options: WxGetSettingOptions): Unit

    /**
     * Requests one scope from the user.
     *
     * The host will not prompt for a scope it has already recorded a decision
     * for, and it requires a user gesture.
     */
    fun authorize(options: WxAuthorizeOptions): Unit

    /**
     * Opens the host's permission settings page.
     *
     * The host requires a user gesture before it will open the page.
     */
    fun openSetting(options: WxOpenSettingOptions): Unit
}
