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

    /**
     * Reports whether the WeChat login session is still usable.
     *
     * The host offers no abort handle, so a cancelled caller only stops waiting.
     */
    fun checkSession(options: WxCheckSessionOptions): Unit

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

    /**
     * Reports what the host currently requires for its privacy contract.
     *
     * This is the host's own privacy condition and is unrelated to the system
     * permissions [getSetting] reports.
     */
    fun getPrivacySetting(options: WxGetPrivacySettingOptions): Unit

    /**
     * Asks the host to obtain the user's acceptance of its privacy contract.
     *
     * The host presents its own prompt, so a user gesture is required. An accepted
     * contract reaches `success`; a declined or dismissed one reaches `fail`.
     */
    fun requirePrivacyAuthorize(options: WxRequirePrivacyAuthorizeOptions): Unit

    /** Reads the system clipboard through callbacks. */
    fun getClipboardData(options: WxGetClipboardDataOptions): Unit

    /** Writes to the system clipboard through callbacks. */
    fun setClipboardData(options: WxSetClipboardDataOptions): Unit

    /** Performs the host's short vibration through callbacks. */
    fun vibrateShort(options: WxVibrateOptions): Unit

    /** Performs the host's long vibration through callbacks. */
    fun vibrateLong(options: WxVibrateOptions): Unit

    /**
     * Obtains the device's current position through callbacks.
     *
     * The host offers no abort handle, so a cancelled caller only stops waiting.
     */
    fun getLocation(options: WxGetLocationOptions): Unit

    /**
     * Asks the host to scan through its own scanning interface.
     *
     * The user dismissing that interface arrives at the failure callback, so a
     * caller cannot treat every failure as a host error. The host offers no abort
     * handle, so a cancelled caller only stops waiting.
     */
    fun scanCode(options: WxScanCodeOptions): Unit

    /**
     * Asks the host to let the user choose images or videos.
     *
     * Like [scanCode] this runs in the host's own interface, and a dismissal
     * arrives at the failure callback. The host offers no abort handle, so a
     * cancelled caller only stops waiting.
     */
    fun chooseMedia(options: WxChooseMediaOptions): Unit

    /**
     * Asks the host to put message templates in front of the user.
     *
     * WeChat requires a user gesture for this, so the caller owns that. The host
     * offers no abort handle, so a cancelled caller only stops waiting.
     */
    fun requestSubscribeMessage(options: WxRequestSubscribeMessageOptions): Unit

    /**
     * Reports the host's current network connection through callbacks.
     *
     * The host offers no abort handle, so a cancelled caller only stops waiting.
     */
    fun getNetworkType(options: WxGetNetworkTypeOptions): Unit

    /**
     * Registers a listener for network state changes.
     *
     * Listeners are removed by identity, so the same function value must be handed to
     * [offNetworkStatusChange].
     */
    fun onNetworkStatusChange(listener: WxNetworkStatusChangeListener): Unit

    /** Removes a network state listener, or every listener when none is given. */
    fun offNetworkStatusChange(listener: WxNetworkStatusChangeListener?): Unit

    /**
     * Uploads a file through the host.
     *
     * Returns the host's task so the caller can report progress and stop the
     * transfer; the host returns no task on some base libraries, which the adapter
     * reports rather than hiding.
     */
    fun uploadFile(options: WxUploadFileOptions): WxTransferTask?

    /** Downloads a file through the host, returning the same kind of task. */
    fun downloadFile(options: WxDownloadFileOptions): WxTransferTask?

    /**
     * Returns the host's global file manager.
     *
     * The manager is a host object whose methods are optional, so callers probe
     * each method rather than assuming the manager's presence implies them.
     */
    fun getFileSystemManager(): WxFileSystemManager

    /** Host environment values, including the file sandbox root. */
    val env: WxEnv?
}

/**
 * The subset of `wx.env` this SDK reads.
 *
 * Only the sandbox root is modelled; other environment values are not needed by
 * any current capability.
 */
internal external interface WxEnv {
    /** Documented type is `string`: the sandbox root for user files. */
    val USER_DATA_PATH: Any?
}
