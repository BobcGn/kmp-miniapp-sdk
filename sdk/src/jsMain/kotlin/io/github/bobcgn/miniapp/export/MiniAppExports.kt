@file:OptIn(ExperimentalJsExport::class)

package io.github.bobcgn.miniapp.export

import io.github.bobcgn.miniapp.api.MiniAppSdk
import io.github.bobcgn.miniapp.capability.CapabilityKey
import io.github.bobcgn.miniapp.capability.CapabilitySupport
import io.github.bobcgn.miniapp.capability.network.HttpMethod
import io.github.bobcgn.miniapp.capability.network.MiniAppHttpRequest
import io.github.bobcgn.miniapp.capability.network.MiniAppHttpTransport
import io.github.bobcgn.miniapp.capability.permission.MiniAppPermissions
import io.github.bobcgn.miniapp.capability.permission.PermissionKey
import io.github.bobcgn.miniapp.capability.permission.PermissionState
import io.github.bobcgn.miniapp.capability.privacy.MiniAppPrivacy
import io.github.bobcgn.miniapp.capability.privacy.PrivacyAuthorizationOutcome
import io.github.bobcgn.miniapp.capability.privacy.PrivacyAuthorizationRequirement
import io.github.bobcgn.miniapp.capability.privacy.PrivacyStatus
import io.github.bobcgn.miniapp.capability.storage.MiniAppStorage
import io.github.bobcgn.miniapp.host.requireSupported
import io.github.bobcgn.miniapp.host.wechat.WeChatLoginResult
import io.github.bobcgn.miniapp.host.wechat.WeChatSessionState
import io.github.bobcgn.miniapp.host.wechat.WechatHost
import io.github.bobcgn.miniapp.host.wechat.runtime.WechatRuntimeInfo
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Stable JavaScript and TypeScript entry point for host-neutral SDK exports.
 *
 * Keep this facade small and delegate behavior to shared Kotlin APIs. Host APIs
 * such as WeChat's `wx` object must not be accessed from this export boundary.
 */
@JsExport
public object MiniAppExports {
    private val host: WechatHost = WechatHost()
    private val storage: MiniAppStorage = host.storage
    private val network: MiniAppHttpTransport = host.network
    private val permissions: MiniAppPermissions = host.permissions
    private val privacy: MiniAppPrivacy = host.privacy

    /**
     * Returns the version of the Kotlin SDK bundled into the JavaScript artifact.
     *
     * @return the same value exposed by [MiniAppSdk.VERSION]
     */
    public fun sdkVersion(): String = MiniAppSdk.VERSION

    /** Returns a stored string, or `null` when [key] does not exist. */
    public suspend fun storageGet(key: String): String? = storage.get(key)

    /** Stores a string, replacing the value currently associated with [key]. */
    public suspend fun storageSet(key: String, value: String): Unit = storage.set(key, value)

    /** Removes [key]; removing an absent key is successful. */
    public suspend fun storageRemove(key: String): Unit = storage.remove(key)

    /**
     * Obtains a short-lived WeChat login code for server-side exchange.
     *
     * The returned value does not represent an authenticated user or session.
     */
    public suspend fun wechatLogin(): WeChatLoginResult = host.platform.auth.login()

    /**
     * Reports whether WeChat still holds a usable login session.
     *
     * Resolves with `Valid` or `Invalid`. A valid answer says only that WeChat's
     * own client login state is intact: it is not an authenticated user, not a
     * consumer backend session, and not proof that any credential the consumer
     * holds is still accepted. An `Invalid` answer is a result, so nothing is
     * re-logged-in automatically; call [wechatLogin] when a new code is needed.
     */
    public suspend fun wechatCheckSession(): String =
        when (host.platform.auth.checkSession()) {
            WeChatSessionState.VALID -> SESSION_VALID
            WeChatSessionState.INVALID -> SESSION_INVALID
        }

    /**
     * Performs an HTTP exchange and resolves with its result.
     *
     * A completed exchange resolves even when its status reports an HTTP error;
     * only a transport failure rejects, with `MiniAppException.Timeout` for an
     * expired timeout and `MiniAppException.HostFailure` otherwise.
     *
     * [headers] is a flat sequence of alternating name and value strings because
     * a JavaScript caller cannot construct the Kotlin map used inside the SDK.
     * The bundled CommonJS wrapper converts plain objects to and from this shape.
     *
     * @param url absolute request target
     * @param method HTTP method name, for example `GET` or `POST`
     * @param headers alternating header name and value entries
     * @param body already-encoded request body, or `null` for no body
     * @param timeoutMillis host timeout in milliseconds, or `null` for the host default
     * @throws IllegalArgumentException when [method] is unknown or [headers] is unpaired
     */
    public suspend fun networkRequest(
        url: String,
        method: String,
        headers: Array<String>,
        body: String?,
        timeoutMillis: Int?,
    ): MiniAppHttpResult {
        val response = network.request(
            MiniAppHttpRequest(
                url = url,
                method = httpMethod(method),
                headers = headerMap(headers),
                body = body,
                timeoutMillis = timeoutMillis,
            ),
        )
        return MiniAppHttpResult(
            statusCode = response.statusCode,
            headers = response.headers.flatMap { (name, value) -> listOf(name, value) }.toTypedArray(),
            body = response.body,
        )
    }

    /**
     * Records WeChat's `App.onLaunch`, forwarded by the consumer's app entry point.
     *
     * WeChat calls `onLaunch` and then `onShow`, so both report the app as being in
     * the foreground. The state is read back through [wechatAppLifecycleState].
     */
    public fun wechatAppOnLaunch(): Unit = host.platform.appLifecycle.appLaunched()

    /** Records WeChat's `App.onShow`, forwarded by the consumer's app entry point. */
    public fun wechatAppOnShow(): Unit = host.platform.appLifecycle.appShown()

    /** Records WeChat's `App.onHide`, forwarded by the consumer's app entry point. */
    public fun wechatAppOnHide(): Unit = host.platform.appLifecycle.appHidden()

    /** Returns the app-level lifecycle state WeChat reported most recently. */
    public fun wechatAppLifecycleState(): String = host.lifecycle.state.name

    /**
     * Records WeChat's `Page.onShow` for [route], forwarded by the page.
     *
     * The route is taken from the page rather than passed by WeChat, so the page
     * supplies its own `this.route`. `Page.onLoad` is not forwarded: a page becomes
     * the one the user is on when it is shown, and `onShow` reports that same route.
     */
    public fun wechatPageOnShow(route: String): Unit =
        host.platform.pageLifecycle.pageShown(route)

    /** Records WeChat's `Page.onHide`, forwarded by the page. */
    public fun wechatPageOnHide(): Unit = host.platform.pageLifecycle.pageHidden()

    /** Records WeChat's `Page.onUnload` for [route], forwarded by the page. */
    public fun wechatPageOnUnload(route: String): Unit =
        host.platform.pageLifecycle.pageUnloaded(route)

    /** Returns the route of the page that loaded most recently, or `null` when none is loaded. */
    public fun wechatPageRoute(): String? = host.platform.pageLifecycle.currentRoute

    /**
     * Opens [url] on top of the current WeChat page stack.
     *
     * WeChat keeps a bounded stack, so this rejects even for a valid [url] when the
     * stack is full. Navigation is WeChat-specific: it is not a common capability.
     */
    public suspend fun wechatNavigateTo(url: String): Unit = host.platform.navigation.navigateTo(url)

    /** Replaces the current page with [url], removing it from the stack. */
    public suspend fun wechatRedirectTo(url: String): Unit =
        host.platform.navigation.redirectTo(url)

    /**
     * Pops [delta] pages off the current page stack, or one page when [delta] is `null`.
     *
     * Going back from the first page rejects rather than succeeding silently.
     */
    public suspend fun wechatNavigateBack(delta: Int?): Unit =
        host.platform.navigation.navigateBack(delta)

    /**
     * Returns how the host currently supports the capability named by [capability].
     *
     * A host answers by inspecting its own runtime, so the same build can answer
     * differently on two hosts. `storage`, `network`, and `lifecycle` are the
     * capabilities this SDK currently gates.
     */
    public fun capabilitySupport(capability: String): JsCapabilitySupport =
        host.capabilitySupport(CapabilityKey(capability)).toJs()

    /**
     * Fails unless the host currently supports the capability named by [capability].
     *
     * Use this when the caller cannot proceed without the capability. Every state
     * other than `Supported` fails, so a version-dependent or permission-dependent
     * capability is reported as unavailable rather than failing later. Call
     * [capabilitySupport] instead when the reason matters.
     */
    public fun requireCapability(capability: String): Unit =
        host.requireSupported(CapabilityKey(capability))

    /**
     * Returns what the WeChat runtime reports about itself.
     *
     * The base-library version is `null` on a runtime that cannot report one.
     */
    public fun wechatRuntimeInfo(): JsRuntimeInfo = host.platform.runtimeInfo.toJs()

    /**
     * Reports whether a WeChat API, parameter, or component exists in this base library.
     *
     * This is the live answer for the running host, which is why it is exposed
     * alongside the capability states: a capability this SDK does not gate can
     * still be probed here.
     */
    public fun wechatCanIUse(schema: String): Boolean = host.platform.runtimeInfo.canIUse(schema)

    /**
     * Returns the host's current state for [permission].
     *
     * The answer is `NotRequested`, `Granted`, or `Denied`. Every call asks the
     * host: the state is never a cached fact, because the user can change a
     * permission in the host's own settings at any time.
     *
     * @param permission host-neutral permission name, for example `microphone`
     * @throws IllegalArgumentException when this SDK has no mapping for the name
     */
    public suspend fun permissionState(permission: String): String =
        permissions.stateOf(PermissionKey(permission)).toJsName()

    /**
     * Asks the host for [permission] and returns the state afterwards.
     *
     * Must be called from a user gesture: the host refuses to prompt otherwise.
     * Rejects with the SDK's permission-denied error when the host refuses, which
     * is distinct from a host failure — the host worked and the answer was no.
     */
    public suspend fun requestPermission(permission: String): String =
        permissions.request(PermissionKey(permission)).toJsName()

    /**
     * Opens the host's permission settings and returns the state afterwards.
     *
     * Must be called from a user gesture. A resolved promise means the settings
     * page closed; it never means the permission was granted, so the state is read
     * from the host after the page closes.
     */
    public suspend fun openPermissionSettings(permission: String): String =
        permissions.openSettings(PermissionKey(permission)).toJsName()

    /**
     * Returns what the host currently requires for its privacy contract.
     *
     * Privacy is not a permission: the host tracks its privacy contract separately
     * from the system permissions the permission functions above report.
     */
    public suspend fun privacyStatus(): JsPrivacyStatus = privacy.status().toJs()

    /**
     * Asks the host to obtain the user's acceptance of its privacy contract.
     *
     * Must be called from a user gesture, because the host presents its own
     * prompt. Resolves with `Authorized` or `Refused`; a refusal is an answer, not
     * a failure, so only a host-level problem rejects.
     */
    public suspend fun requestPrivacyAuthorization(): String =
        when (privacy.requestAuthorization()) {
            is PrivacyAuthorizationOutcome.Authorized -> PRIVACY_AUTHORIZED
            is PrivacyAuthorizationOutcome.Refused -> PRIVACY_REFUSED
        }

    /**
     * Fails unless the host currently requires no privacy authorization.
     *
     * This is the precondition point for capabilities the host gates behind its
     * privacy contract. It shows nothing and starts no prompt.
     */
    public suspend fun requirePrivacySatisfied(): Unit = privacy.requireSatisfied()
}

/** Presents a capability's support state in a form a JavaScript caller can read. */
private fun CapabilitySupport.toJs(): JsCapabilitySupport = when (this) {
    is CapabilitySupport.Supported -> JsCapabilitySupport(SUPPORTED_STATE, null, null, null)

    is CapabilitySupport.Unsupported -> JsCapabilitySupport(UNSUPPORTED_STATE, null, null, null)

    is CapabilitySupport.VersionDependent -> JsCapabilitySupport(
        state = VERSION_DEPENDENT_STATE,
        requiredVersion = requiredVersion.toString(),
        currentVersion = currentVersion?.toString(),
        permission = null,
    )

    is CapabilitySupport.PermissionDependent -> JsCapabilitySupport(
        state = PERMISSION_DEPENDENT_STATE,
        requiredVersion = null,
        currentVersion = null,
        permission = permission,
    )
}

/** Presents what the runtime reports about itself in a form a JavaScript caller can read. */
private fun WechatRuntimeInfo.toJs(): JsRuntimeInfo = JsRuntimeInfo(
    baseLibraryVersion = baseLibraryVersion?.toString(),
    platform = platform,
    isDeveloperTools = isDeveloperTools,
)

/** Presents the host's privacy requirement in a form a JavaScript caller can read. */
private fun PrivacyStatus.toJs(): JsPrivacyStatus = JsPrivacyStatus(
    requirement = when (requirement) {
        PrivacyAuthorizationRequirement.REQUIRED -> PRIVACY_REQUIRED
        PrivacyAuthorizationRequirement.NOT_REQUIRED -> PRIVACY_NOT_REQUIRED
    },
    contractName = contractName,
)

private const val SESSION_VALID: String = "Valid"
private const val SESSION_INVALID: String = "Invalid"
private const val PRIVACY_REQUIRED: String = "REQUIRED"
private const val PRIVACY_NOT_REQUIRED: String = "NOT_REQUIRED"
private const val PRIVACY_AUTHORIZED: String = "Authorized"
private const val PRIVACY_REFUSED: String = "Refused"

/** Presents a permission state as the stable string the TypeScript contract declares. */
private fun PermissionState.toJsName(): String = when (this) {
    is PermissionState.NotRequested -> NOT_REQUESTED_STATE
    is PermissionState.Granted -> GRANTED_STATE
    is PermissionState.Denied -> DENIED_STATE
}

private const val NOT_REQUESTED_STATE: String = "NotRequested"
private const val GRANTED_STATE: String = "Granted"
private const val DENIED_STATE: String = "Denied"

private const val SUPPORTED_STATE: String = "Supported"
private const val UNSUPPORTED_STATE: String = "Unsupported"
private const val VERSION_DEPENDENT_STATE: String = "VersionDependent"
private const val PERMISSION_DEPENDENT_STATE: String = "PermissionDependent"

/** Restores the Kotlin map shape from the flat entry list used at this boundary. */
private fun headerMap(headers: Array<String>): Map<String, String> {
    require(headers.size % 2 == 0) {
        "headers must contain alternating name and value entries"
    }
    val result = mutableMapOf<String, String>()
    var index = 0
    while (index < headers.size) {
        result[headers[index]] = headers[index + 1]
        index += 2
    }
    return result
}

/** Resolves a JavaScript-supplied method name to the closed SDK method set. */
private fun httpMethod(method: String): HttpMethod {
    val normalized = method.uppercase()
    return HttpMethod.entries.firstOrNull { it.name == normalized }
        ?: throw IllegalArgumentException("Unsupported HTTP method: '$method'")
}
