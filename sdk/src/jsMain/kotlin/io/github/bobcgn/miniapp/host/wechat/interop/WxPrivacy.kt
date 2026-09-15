package io.github.bobcgn.miniapp.host.wechat.interop

/** Raw callback-style options accepted by [wx.getPrivacySetting]. */
internal external interface WxGetPrivacySettingOptions {
    /** Called only when the host reports its privacy requirement. */
    var success: WxGetPrivacySettingSuccessCallback?

    /** Called only when the requirement cannot be read. */
    var fail: WxGeneralCallback?

    /** Called after either [success] or [fail]. */
    var complete: WxGeneralCallback?
}

/**
 * Raw success result returned by [wx.getPrivacySetting].
 *
 * Both fields are declared [Any] on purpose, for the same reason WeChat storage
 * declares its value that way: an external property the host did not set reads as
 * `undefined`, and a value of the wrong type would otherwise be accepted silently.
 * [wxPrivacyRequirement] is the only reader, so the adapter can tell "the host said
 * no" from "the host answered with something that is not an answer".
 */
internal external interface WxGetPrivacySettingSuccessResult : WxGeneralCallbackResult {
    /** Official type is `boolean`: whether the host requires the privacy contract. */
    val needAuthorization: Any?

    /** Official type is `string`: the host's own name for its privacy contract. */
    val privacyContractName: Any?
}

/** What a raw [wx.getPrivacySetting] answer says, in a form the adapter can trust. */
internal sealed interface WxPrivacyRequirement {
    /** The host requires the user to accept its privacy contract. */
    data class Required(val contractName: String?) : WxPrivacyRequirement

    /** The host requires nothing further. */
    data class NotRequired(val contractName: String?) : WxPrivacyRequirement

    /** The host answered with something the contract cannot carry. */
    data object Unreadable : WxPrivacyRequirement
}

/**
 * Reads a raw [wx.getPrivacySetting] answer.
 *
 * This is the only place the raw result is inspected, so a missing flag, a
 * non-boolean flag, or a contract name that is neither absent nor a string all
 * become [WxPrivacyRequirement.Unreadable] instead of reaching the adapter as a
 * plausible-looking answer.
 */
internal fun wxPrivacyRequirement(result: WxGetPrivacySettingSuccessResult): WxPrivacyRequirement {
    val needAuthorization = result.needAuthorization
    if (needAuthorization !is Boolean) return WxPrivacyRequirement.Unreadable

    val rawName = result.privacyContractName
    val contractName = when {
        rawName == null -> null
        rawName is String -> rawName
        else -> return WxPrivacyRequirement.Unreadable
    }

    return if (needAuthorization) {
        WxPrivacyRequirement.Required(contractName)
    } else {
        WxPrivacyRequirement.NotRequired(contractName)
    }
}

/**
 * Raw callback-style options accepted by [wx.requirePrivacyAuthorize].
 *
 * The host answers an accepted contract through [success] and a declined or
 * dismissed one through [fail], so the failure path also carries the user's
 * answer and is not by itself an error.
 */
internal external interface WxRequirePrivacyAuthorizeOptions {
    /** Called only when the user accepted the privacy contract. */
    var success: WxGeneralCallback?

    /** Called when the user did not accept the contract, or the request failed. */
    var fail: WxGeneralCallback?

    /** Called after either [success] or [fail]. */
    var complete: WxGeneralCallback?
}

/** Success callback accepted by [WxGetPrivacySettingOptions]. */
internal typealias WxGetPrivacySettingSuccessCallback = (WxGetPrivacySettingSuccessResult) -> Unit

/**
 * Whether [wx.getPrivacySetting] exists.
 *
 * The privacy APIs arrived together in one base library, so this is also the
 * reliable presence probe for privacy support as a whole; a base library without
 * them does not intercept privacy-gated calls at all.
 */
internal fun hasWxGetPrivacySetting(): Boolean =
    js("typeof wx !== 'undefined' && typeof wx.getPrivacySetting === 'function'")

/** Whether [wx.requirePrivacyAuthorize] exists. */
internal fun hasWxRequirePrivacyAuthorize(): Boolean =
    js("typeof wx !== 'undefined' && typeof wx.requirePrivacyAuthorize === 'function'")

/**
 * Creates a plain JavaScript option bag for [wx.getPrivacySetting].
 *
 * External interfaces cannot be instantiated with Kotlin constructors, so the
 * factory exists solely to produce the object the host expects.
 */
internal fun wxGetPrivacySettingOptions(): WxGetPrivacySettingOptions {
    val options: WxGetPrivacySettingOptions = js("({})")
    return options
}

/**
 * Creates a plain JavaScript option bag for [wx.requirePrivacyAuthorize].
 *
 * The host presents its own prompt, so the caller must already be inside a user
 * gesture; this factory cannot enforce that.
 */
internal fun wxRequirePrivacyAuthorizeOptions(): WxRequirePrivacyAuthorizeOptions {
    val options: WxRequirePrivacyAuthorizeOptions = js("({})")
    return options
}
