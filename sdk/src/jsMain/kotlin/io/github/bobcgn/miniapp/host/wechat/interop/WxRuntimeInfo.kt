package io.github.bobcgn.miniapp.host.wechat.interop

/** Result of [wx.getAppBaseInfo], read only for the fields the SDK gates on. */
internal external interface WxAppBaseInfo {
    /** Base-library version, for example `3.17.3`. */
    val SDKVersion: String?

    /** WeChat client version. */
    val version: String?
}

/**
 * The subset of the legacy [wx.getSystemInfoSync] result the SDK reads.
 *
 * Only the fallback path uses this, so the many other fields are not modelled.
 */
internal external interface WxSystemInfo {
    /** Base-library version, for example `2.19.4`. */
    val SDKVersion: String?

    /** Runtime platform, for example `devtools`. */
    val platform: String?
}

/** Result of [wx.getDeviceInfo], read only for the runtime platform. */
internal external interface WxDeviceInfo {
    /** `ios`, `android`, `devtools`, `windows`, or `mac`. */
    val platform: String?
}

/**
 * Presence probes for the runtime-inspection members of [wx].
 *
 * The `wx` global is not guaranteed to exist and the inspection members are not
 * guaranteed to exist on an old base library. Reading a member that is absent
 * throws, so every read is preceded by one of these probes, and an absent member
 * yields "cannot be determined" instead of a crash. This is why `js()` is used
 * rather than calling the members directly: the probe has to inspect the global
 * without touching it.
 */

/** Whether [wx.canIUse] exists, which requires base library 1.1.1 or later. */
internal fun hasWxCanIUse(): Boolean =
    js("typeof wx !== 'undefined' && typeof wx.canIUse === 'function'")

/** Whether [wx.getAppBaseInfo] exists, which requires base library 2.20.1 or later. */
internal fun hasWxGetAppBaseInfo(): Boolean =
    js("typeof wx !== 'undefined' && typeof wx.getAppBaseInfo === 'function'")

/** Whether the unmaintained [wx.getSystemInfoSync] exists. */
internal fun hasWxGetSystemInfoSync(): Boolean =
    js("typeof wx !== 'undefined' && typeof wx.getSystemInfoSync === 'function'")

/** Whether [wx.getDeviceInfo] exists, which requires base library 2.20.1 or later. */
internal fun hasWxGetDeviceInfo(): Boolean =
    js("typeof wx !== 'undefined' && typeof wx.getDeviceInfo === 'function'")
