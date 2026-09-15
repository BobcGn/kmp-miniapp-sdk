package io.github.bobcgn.miniapp.host.wechat.interop

import kotlin.js.jsTypeOf

/** Raw callback-style options accepted by [wx.getSetting]. */
internal external interface WxGetSettingOptions {
    /** Called only when the host reports its authorization state. */
    var success: WxGetSettingSuccessCallback?

    /** Called only when the state cannot be read. */
    var fail: WxGeneralCallback?

    /** Called after either [success] or [fail]. */
    var complete: WxGeneralCallback?
}

/** Raw success result returned by [wx.getSetting]. */
internal external interface WxGetSettingSuccessResult : WxGeneralCallbackResult {
    /**
     * Host map of scopes it holds a decision for.
     *
     * A scope the user never decided is absent from the map rather than present
     * with a false value, which is what separates "never asked" from "refused".
     * The map is a plain JavaScript object, so it stays [Any] here and is read
     * only through [wxScopeEntry].
     */
    val authSetting: Any?
}

/** Raw callback-style options accepted by [wx.authorize]. */
internal external interface WxAuthorizeOptions {
    /** Host scope being requested. Only the adapter produces this string. */
    var scope: String

    /** Called only when the host records the permission as granted. */
    var success: WxGeneralCallback?

    /** Called when the request is refused or cannot be made. */
    var fail: WxGeneralCallback?

    /** Called after either [success] or [fail]. */
    var complete: WxGeneralCallback?
}

/** Raw success result returned by [wx.openSetting]. */
internal external interface WxOpenSettingSuccessResult : WxGeneralCallbackResult {
    /** Host map of scopes after the settings page closed. */
    val authSetting: Any?
}

/** Raw callback-style options accepted by [wx.openSetting]. */
internal external interface WxOpenSettingOptions {
    /** Called only when the settings page closed normally. */
    var success: WxOpenSettingSuccessCallback?

    /** Called when the settings page cannot be opened. */
    var fail: WxGeneralCallback?

    /** Called after either [success] or [fail]. */
    var complete: WxGeneralCallback?
}

/** Success callback accepted by [WxGetSettingOptions]. */
internal typealias WxGetSettingSuccessCallback = (WxGetSettingSuccessResult) -> Unit

/** Success callback accepted by [WxOpenSettingOptions]. */
internal typealias WxOpenSettingSuccessCallback = (WxOpenSettingSuccessResult) -> Unit

/**
 * What a raw host authorization map says about one scope.
 *
 * A raw JavaScript object cannot cross out of this package, so this is the typed
 * shape the rest of the SDK reads. [Absent] and [Unreadable] are deliberately
 * different: one is a host that has not been asked yet, the other is a host that
 * answered with something the contract cannot represent.
 */
internal sealed interface WxScopeEntry {
    /** The host holds no decision for the scope. */
    data object Absent : WxScopeEntry

    /** The host decided the scope, and this is its answer. */
    data class Decided(val granted: Boolean) : WxScopeEntry

    /** The host's answer for the scope is not a value the contract can carry. */
    data object Unreadable : WxScopeEntry
}

/**
 * Reads one scope out of a raw host authorization map.
 *
 * This is the only place that touches the map as a JavaScript object. Everything
 * downstream receives a [WxScopeEntry], so no raw value and no host key reaches
 * the adapter, the error model, or the export surface.
 *
 * @param authSetting raw map as the host supplied it, or `null`
 * @param scope host scope name to read
 */
internal fun wxScopeEntry(authSetting: Any?, scope: String): WxScopeEntry {
    if (authSetting == null) return WxScopeEntry.Unreadable
    if (jsTypeOf(authSetting) != "object") return WxScopeEntry.Unreadable

    val present: Boolean = js("Object.prototype.hasOwnProperty.call(authSetting, scope)")
    if (!present) return WxScopeEntry.Absent

    val value: Any? = js("authSetting[scope]")
    return when (value) {
        is Boolean -> WxScopeEntry.Decided(value)
        // Any other value shape is a host answer the contract cannot carry.
        else -> WxScopeEntry.Unreadable
    }
}

/**
 * Creates a plain JavaScript option bag for [wx.getSetting].
 *
 * External interfaces cannot be instantiated with Kotlin constructors, so the
 * factory exists solely to produce the object the host expects.
 */
internal fun wxGetSettingOptions(): WxGetSettingOptions {
    val options: WxGetSettingOptions = js("({})")
    return options
}

/**
 * Creates a plain JavaScript option bag for [wx.authorize].
 *
 * @param scope host scope name, produced only by the WeChat permission adapter
 */
internal fun wxAuthorizeOptions(scope: String): WxAuthorizeOptions {
    val options: WxAuthorizeOptions = js("({})")
    options.scope = scope
    return options
}

/**
 * Creates a plain JavaScript option bag for [wx.openSetting].
 *
 * The host requires a user gesture before it will open; this factory cannot and
 * does not enforce that.
 */
internal fun wxOpenSettingOptions(): WxOpenSettingOptions {
    val options: WxOpenSettingOptions = js("({})")
    return options
}
