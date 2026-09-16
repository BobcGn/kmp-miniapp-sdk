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
import io.github.bobcgn.miniapp.host.wechat.WeChatCameraPosition
import io.github.bobcgn.miniapp.host.wechat.WeChatCoordinateSystem
import io.github.bobcgn.miniapp.host.wechat.WeChatLoginResult
import io.github.bobcgn.miniapp.host.wechat.WeChatMediaRequest
import io.github.bobcgn.miniapp.host.wechat.WeChatMediaSizeType
import io.github.bobcgn.miniapp.host.wechat.WeChatMediaSource
import io.github.bobcgn.miniapp.host.wechat.WeChatMediaType
import io.github.bobcgn.miniapp.host.wechat.WeChatScanCategory
import io.github.bobcgn.miniapp.host.wechat.WeChatScanRequest
import io.github.bobcgn.miniapp.host.wechat.WeChatSessionState
import io.github.bobcgn.miniapp.host.wechat.WeChatSubscriptionRequest
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
     * Reads the system clipboard as text.
     *
     * An empty clipboard is returned as an empty string: we only ever look at our
     * own clipboard text when explicitly asked. The clipboard is the user's, and
     * the SDK neither stores nor logs what it reads.
     *
     * @throws MiniAppException.UnsupportedCapability when the host has no clipboard read API
     */
    public suspend fun wechatGetClipboardText(): String = host.platform.clipboard.readText()

    /**
     * Replaces the system clipboard with [value].
     *
     * @param value text to place on the clipboard
     */
    public suspend fun wechatSetClipboardText(value: String): Unit =
        host.platform.clipboard.writeText(value)

    /**
     * Performs WeChat's short vibration.
     *
     * A resolved call means the host accepted and performed the vibration. It is
     * not evidence that a user felt anything, which no software check can confirm.
     */
    public suspend fun wechatVibrateShort(): Unit = host.platform.haptics.vibrateShort()

    /**
     * Performs WeChat's long vibration.
     *
     * The long vibration is a separate host API from the short one and may be
     * absent while the short one is present.
     */
    public suspend fun wechatVibrateLong(): Unit = host.platform.haptics.vibrateLong()

    /**
     * Returns the sandbox root that the file operations below work against.
     *
     * The caller builds file paths from this. The SDK does not normalise paths and
     * makes no claim to prevent traversal, so a path outside the sandbox is the
     * caller's mistake rather than something this refuses.
     */
    public fun wechatUserDataPath(): String = host.platform.fileSystem.userDataPath()

    /**
     * Reads a UTF-8 text file from the mini program file sandbox.
     *
     * An empty file resolves with an empty string.
     *
     * @param path file path inside the sandbox
     */
    public suspend fun wechatReadTextFile(path: String): String =
        host.platform.fileSystem.readText(path)

    /**
     * Writes UTF-8 text to a file in the mini program file sandbox, replacing what
     * is there.
     *
     * The parent directory must already exist; this creates none.
     */
    public suspend fun wechatWriteTextFile(path: String, content: String): Unit =
        host.platform.fileSystem.writeText(path, content)

    /**
     * Reports whether a sandbox path exists and is accessible.
     *
     * A path the host reports as missing resolves `false`. Any other failure
     * rejects, so a permission error is never reported as a missing file.
     */
    public suspend fun wechatFileExists(path: String): Boolean =
        host.platform.fileSystem.exists(path)

    /**
     * Removes a file from the mini program file sandbox.
     *
     * Removing a file that is not there rejects, because that is what WeChat's
     * `unlink` does. Check {@link wechatFileExists} first when that matters.
     */
    public suspend fun wechatRemoveFile(path: String): Unit =
        host.platform.fileSystem.remove(path)

    /**
     * Obtains the device's current position.
     *
     * The coordinates are returned in [coordinateSystem], which defaults to `gcj02`
     * because that is the system WeChat's own map views accept; `wgs84` is the raw
     * satellite fix. They are not interchangeable.
     *
     * Three separate conditions decide whether this resolves: whether WeChat
     * exposes the API at all, whether `scope.userLocation` is granted, and whether
     * the host's privacy contract has been accepted. Use the capability query and
     * the permission and privacy functions above to observe each on its own. This
     * call never prompts for a permission and never accepts a privacy contract on
     * the user's behalf.
     *
     * @param coordinateSystem `wgs84` or `gcj02`
     * @throws IllegalArgumentException when [coordinateSystem] is neither
     */
    public suspend fun wechatGetCurrentLocation(
        coordinateSystem: String,
    ): JsGeoPosition {
        val system = when (coordinateSystem) {
            WGS84_NAME -> WeChatCoordinateSystem.WGS84
            GCJ02_NAME -> WeChatCoordinateSystem.GCJ02
            else -> throw IllegalArgumentException(
                "Unsupported coordinate system: '$coordinateSystem'",
            )
        }

        val position = host.platform.location.currentPosition(system)
        return JsGeoPosition(
            latitude = position.latitude,
            longitude = position.longitude,
            accuracyMeters = position.accuracyMeters,
            coordinateSystem = position.coordinateSystem.hostValue,
        )
    }

    /**
     * Asks WeChat to scan through its own scanning interface.
     *
     * When the host reports its ambiguous cancel signal, this rejects with
     * `HostInteractionInterrupted`. Real-device verification showed that the same
     * signal can mean either user dismissal or camera access preventing the
     * interface from opening, so the SDK does not assign user intent.
     *
     * This call asks the host for no permission. `wx.scanCode` drives WeChat's own
     * interface, and no permission precondition for it could be established from
     * the host contract, so the SDK neither prompts for one nor maps it to a scope
     * the host does not tie to this API.
     *
     * The decoded content is never logged or stored by the SDK. It is the user's
     * data, so a caller that keeps it does so on its own authority.
     *
     * @param onlyFromCamera whether the host must scan through its camera rather
     *   than also accepting an image the user already has
     * @param scanTypes categories to ask for, for example `qrCode`; an empty array
     *   asks for every category the host supports, which is not the same as asking
     *   for none
     * @throws IllegalArgumentException when [scanTypes] names a category this SDK
     *   does not know
     */
    public suspend fun wechatScanCode(
        onlyFromCamera: Boolean,
        scanTypes: Array<String>,
    ): JsScanResult {
        val request = WeChatScanRequest(
            onlyFromCamera = onlyFromCamera,
            allowedCategories = scanTypes.map { scanCategory(it) },
        )

        val result = host.platform.scanCode.scan(request)
        return JsScanResult(
            text = result.text,
            scanType = result.scanType,
            format = result.format?.hostValue,
            charSet = result.charSet,
            rawData = result.rawData,
            path = result.path,
        )
    }

    /**
     * Asks WeChat to let the user choose images or videos through its own picker.
     *
     * Resolves with the non-empty file array the host returned. A success callback
     * without a selected file is rejected as an invalid host response.
     *
     * The interaction ending without a selection rejects with the SDK's
     * host-interrupted error, which is not a host failure and does not claim to know
     * whether the user dismissed the picker or something prevented it from
     * completing. Every other failure rejects as a host failure or an invalid
     * response.
     *
     * This call asks the host for no permission: WeChat's own picker needs none,
     * and the host's scope list holds no scope for reading the media library.
     *
     * The returned paths are host temporary resources. The SDK never logs, stores,
     * or uploads what the user selected, and a caller that keeps it does so on its
     * own authority.
     *
     * @param mediaTypes media types to ask for, for example `image`; WeChat
     *   documents this option as required, so it must not be empty
     * @param count maximum number of files to ask for; the host applies its own
     *   limit and may return fewer
     * @param sourceTypes where the host may take media from, for example `album`;
     *   an empty array asks for no restriction, which is not the same as asking for
     *   none
     * @param maxDurationSeconds longest video recording to ask for, or `null` for
     *   the host's own default; WeChat documents the range as 3 to 60
     * @param sizeTypes how much the host may compress the images it returns; an
     *   empty array asks for no restriction
     * @param camera which camera to use, or `null` to leave it to the host
     * @throws IllegalArgumentException when a name is not one this SDK knows, when
     *   no media type is given, when [count] is below 1, or when
     *   [maxDurationSeconds] is outside the range WeChat documents
     */
    public suspend fun wechatChooseMedia(
        mediaTypes: Array<String>,
        count: Int,
        sourceTypes: Array<String>,
        maxDurationSeconds: Int?,
        sizeTypes: Array<String>,
        camera: String?,
    ): Array<JsMediaFile> {
        val request = WeChatMediaRequest(
            mediaType = mediaTypes.map { mediaType(it) },
            count = count,
            sourceType = sourceTypes.map { mediaSource(it) },
            maxDurationSeconds = maxDurationSeconds,
            sizeType = sizeTypes.map { mediaSizeType(it) },
            camera = camera?.let { cameraPosition(it) },
        )

        return host.platform.chooseMedia.choose(request).map { file ->
            JsMediaFile(
                tempFilePath = file.tempFilePath,
                // Kotlin's Long is not a JavaScript value, so the byte count crosses
                // as a number, which is exact well beyond any file size.
                sizeBytes = file.sizeBytes.toDouble(),
                fileType = file.fileType?.hostValue,
                hostFileType = file.hostFileType,
                durationSeconds = file.durationSeconds,
                width = file.width,
                height = file.height,
                thumbTempFilePath = file.thumbTempFilePath,
            )
        }.toTypedArray()
    }

    /**
     * Asks WeChat to put message templates in front of the user.
     *
     * **The caller must call this from a user gesture.** WeChat requires one, and the
     * SDK neither supplies one nor retries when the host refuses because none
     * preceded the call. Nothing in this SDK calls it on page load.
     *
     * Resolves with exactly one entry per requested template, in the order given.
     * Missing, unexpected, blank, or non-text answers reject as invalid responses.
     * A non-blank status this SDK cannot name is still reported verbatim in
     * `hostStatus`.
     *
     * An answer says what the user decided about a template. It never says a message
     * was sent or delivered: acceptance is a subscription state, and sending is the
     * consumer backend's business.
     *
     * Failures remain host failures until a real-host run establishes an exact
     * dismissal signal for this API. Malformed success values reject as invalid
     * responses.
     *
     * This call asks the host for no permission: the offline sources for this API
     * name no scope for it.
     *
     * @param templateIds templates to ask about; blank identifiers are refused
     * @throws IllegalArgumentException when [templateIds] is empty or holds a blank id
     */
    public suspend fun wechatRequestSubscribeMessage(
        templateIds: Array<String>,
    ): Array<JsSubscriptionResult> {
        val request = WeChatSubscriptionRequest(templateIds.toList())

        return host.platform.requestSubscribeMessage.request(request).map { result ->
            JsSubscriptionResult(
                templateId = result.templateId,
                status = result.status?.hostValue,
                hostStatus = result.hostStatus,
            )
        }.toTypedArray()
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

private const val WGS84_NAME: String = "wgs84"
private const val GCJ02_NAME: String = "gcj02"
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

/** Resolves a JavaScript-supplied scan category to the closed SDK category set. */
private fun scanCategory(name: String): WeChatScanCategory =
    WeChatScanCategory.entries.firstOrNull { it.hostValue == name }
        ?: throw IllegalArgumentException("Unsupported scan category: '$name'")

/** Resolves a JavaScript-supplied media type to the closed SDK set. */
private fun mediaType(name: String): WeChatMediaType =
    WeChatMediaType.entries.firstOrNull { it.hostValue == name }
        ?: throw IllegalArgumentException("Unsupported media type: '$name'")

/** Resolves a JavaScript-supplied media source to the closed SDK set. */
private fun mediaSource(name: String): WeChatMediaSource =
    WeChatMediaSource.entries.firstOrNull { it.hostValue == name }
        ?: throw IllegalArgumentException("Unsupported media source: '$name'")

/** Resolves a JavaScript-supplied size type to the closed SDK set. */
private fun mediaSizeType(name: String): WeChatMediaSizeType =
    WeChatMediaSizeType.entries.firstOrNull { it.hostValue == name }
        ?: throw IllegalArgumentException("Unsupported size type: '$name'")

/** Resolves a JavaScript-supplied camera position to the closed SDK set. */
private fun cameraPosition(name: String): WeChatCameraPosition =
    WeChatCameraPosition.entries.firstOrNull { it.hostValue == name }
        ?: throw IllegalArgumentException("Unsupported camera position: '$name'")
