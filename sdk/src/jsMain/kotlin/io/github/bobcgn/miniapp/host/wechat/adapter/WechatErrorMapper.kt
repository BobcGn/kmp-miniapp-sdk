package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.capability.permission.PermissionKey
import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxRequestFailureResult

/**
 * Converts a raw WeChat callback failure into the platform-neutral SDK model.
 *
 * Only typed scalar values cross into [MiniAppException.HostFailure]; the raw
 * external result object remains confined to the WeChat adapter boundary.
 */
internal fun mapWechatHostFailure(
    operation: String,
    result: WxGeneralCallbackResult,
    code: String? = null,
): MiniAppException.HostFailure = MiniAppException.HostFailure(
    host = "wechat",
    code = code,
    hostMessage = result.errMsg,
    metadata = mapOf("operation" to operation),
)

/**
 * Converts a raw `wx.request` failure into the platform-neutral SDK model.
 *
 * WeChat reports an expired timeout as an ordinary exchange failure, so it is
 * recognized from the host message before falling back to [mapWechatHostFailure].
 * The remaining failure shapes are transport failures that carry no HTTP status.
 */
internal fun mapWechatRequestFailure(result: WxRequestFailureResult): MiniAppException =
    if (result.errMsg.contains(TIMEOUT_ERRMSG_MARKER, ignoreCase = true)) {
        MiniAppException.Timeout(operation = "request", hostMessage = result.errMsg)
    } else {
        mapWechatHostFailure(
            operation = "request",
            result = result,
            code = result.errno?.toString(),
        )
    }

/** Substring WeChat embeds in `errMsg` when a request exhausts its timeout. */
private const val TIMEOUT_ERRMSG_MARKER: String = "timeout"

/**
 * Converts a raw `wx.authorize` failure into the platform-neutral SDK model.
 *
 * WeChat reports a refused permission as an ordinary failure whose message says
 * the authorization was denied; it publishes no error code for this that the SDK
 * can rely on across base libraries. Only that message shape becomes
 * [MiniAppException.PermissionDenied]. Every other failure stays a
 * [MiniAppException.HostFailure], which is the safe direction: an unrecognized
 * message is never reclassified as a refusal the caller might act on.
 *
 * The permission is reported by its host-neutral key, so the WeChat scope name
 * never reaches the shared error model.
 */
internal fun mapWechatAuthorizeFailure(
    permission: PermissionKey,
    result: WxGeneralCallbackResult,
): MiniAppException =
    if (result.errMsg.contains(DENIED_ERRMSG_MARKER, ignoreCase = true)) {
        MiniAppException.PermissionDenied(
            permission = permission.value,
            message = result.errMsg,
        )
    } else {
        mapWechatHostFailure(operation = "authorize", result = result)
    }

/** Substring WeChat embeds in `errMsg` when the user refused a scope. */
private const val DENIED_ERRMSG_MARKER: String = "auth deny"

/**
 * Converts a raw `wx.scanCode` failure into the platform-neutral SDK model.
 *
 * A user dismissing the host's scanning interface reaches the failure callback by
 * the same path as a genuine failure, and WeChat publishes no structured field
 * that separates them, so the host message is the only evidence there is.
 *
 * The exact ambiguous messages become
 * [MiniAppException.HostInteractionInterrupted]. A substring or prefix match is deliberately
 * not used: a real failure that happens to mention cancelling would then be
 * reclassified as a user decision the caller would act on — and telling a
 * consumer "the user chose this" when the host actually broke is worse than
 * reporting a host failure it can diagnose.
 *
 * **Why these two forms.** Real-device verification showed that `scanCode:cancel`
 * can mean either a user dismissal or a camera restriction that prevented the
 * interface from opening. The host's other conventional form is
 * `scanCode:fail cancel`. Both are therefore classified as interruptions with an
 * indeterminate cause, not as user intent. Matching remains exact so unrelated
 * failures keep their diagnostic [MiniAppException.HostFailure] classification.
 *
 * This is the only place the scan failure text is interpreted, and the raw message
 * never reaches a public type. Every other failure stays a
 * [MiniAppException.HostFailure].
 */
internal fun mapWechatScanFailure(result: WxGeneralCallbackResult): MiniAppException =
    if (result.errMsg in SCAN_INTERRUPTED_ERRMSGS) {
        MiniAppException.HostInteractionInterrupted(
            host = "wechat",
            operation = "scanCode",
            hostMessage = result.errMsg,
        )
    } else {
        mapWechatHostFailure(operation = "scanCode", result = result)
    }

/**
 * The exact host messages that mean the user dismissed the scanning interface.
 *
 * Matching is exact and case-sensitive on purpose: `ScanCode:cancel` and
 * `scanCode:fail user cancel` are host failures, not user decisions.
 */
private val SCAN_INTERRUPTED_ERRMSGS: Set<String> = setOf(
    "scanCode:cancel",
    "scanCode:fail cancel",
)

/**
 * Converts a raw `wx.chooseMedia` failure into the platform-neutral SDK model.
 *
 * Media selection runs in the host's own interface. The installed Developer Tools
 * base library reports a simulated dismissal as the exact `chooseMedia:cancel`
 * message and provides no structured cause. The SDK therefore reports only that
 * the interaction ended, without attributing user intent or a permission result.
 *
 * Only the exact messages are matched, so a real failure that merely mentions
 * cancelling is not reclassified. Developer Tools evidence established
 * `chooseMedia:cancel`; Android real-device evidence established
 * `chooseMedia:fail cancel` for a manual dismissal.
 *
 * This is the only place the media failure text is interpreted, and the raw message
 * never reaches a public type. Every other failure stays a
 * [MiniAppException.HostFailure].
 */
internal fun mapWechatChooseMediaFailure(result: WxGeneralCallbackResult): MiniAppException =
    if (result.errMsg in CHOOSE_MEDIA_INTERRUPTED_ERRMSGS) {
        MiniAppException.HostInteractionInterrupted(
            host = "wechat",
            operation = "chooseMedia",
            hostMessage = result.errMsg,
        )
    } else {
        mapWechatHostFailure(operation = "chooseMedia", result = result)
    }

/**
 * The exact host messages that mean the media interaction ended without a selection.
 *
 * Matching is exact and case-sensitive on purpose: a differently worded message is
 * a host failure, not evidence that the user decided anything.
 */
private val CHOOSE_MEDIA_INTERRUPTED_ERRMSGS: Set<String> = setOf(
    "chooseMedia:cancel",
    "chooseMedia:fail cancel",
)

/**
 * Converts a raw `wx.requestSubscribeMessage` failure into the platform-neutral SDK model.
 *
 * No dismissal signal has been observed for this API yet. Similar-looking messages
 * from scan or media are not evidence for subscription requests, so every failure
 * remains a [MiniAppException.HostFailure] until a real-host run establishes an exact
 * signal. The raw message remains internal to the exception and is never exposed as
 * a subscription answer.
 */
internal fun mapWechatRequestSubscribeMessageFailure(
    result: WxGeneralCallbackResult,
): MiniAppException = mapWechatHostFailure(operation = "requestSubscribeMessage", result = result)

/**
 * Converts a raw `wx.uploadFile` failure into the platform-neutral SDK model.
 *
 * The base library shipped with the installed Developer Tools reports an expired
 * transfer as `<api>:fail timeout`, which is the same convention its `wx.request`
 * path uses. Only that exact message becomes [MiniAppException.Timeout]; a message
 * that merely contains the word is a host failure, because a transfer that failed for
 * another reason should not be reported as one the host stopped waiting for.
 */
internal fun mapWechatUploadFailure(result: WxGeneralCallbackResult): MiniAppException =
    if (result.errMsg == UPLOAD_TIMEOUT_ERRMSG) {
        MiniAppException.Timeout(operation = "uploadFile", hostMessage = result.errMsg)
    } else {
        mapWechatHostFailure(operation = "uploadFile", result = result)
    }

/** The exact host message for an upload that exhausted its timeout. */
private const val UPLOAD_TIMEOUT_ERRMSG: String = "uploadFile:fail timeout"

/**
 * Converts a raw `wx.downloadFile` failure into the platform-neutral SDK model.
 *
 * As with the upload direction, only the exact documented timeout message becomes
 * [MiniAppException.Timeout].
 */
internal fun mapWechatDownloadFailure(result: WxGeneralCallbackResult): MiniAppException =
    if (result.errMsg == DOWNLOAD_TIMEOUT_ERRMSG) {
        MiniAppException.Timeout(operation = "downloadFile", hostMessage = result.errMsg)
    } else {
        mapWechatHostFailure(operation = "downloadFile", result = result)
    }

/** The exact host message for a download that exhausted its timeout. */
private const val DOWNLOAD_TIMEOUT_ERRMSG: String = "downloadFile:fail timeout"

/**
 * Converts a raw `wx.requestPayment` failure into the platform-neutral SDK model.
 *
 * **Evidence.** The base library shipped with the installed Developer Tools runs this
 * API's own payment flow, and its state machine reports a dismissed payment — and its
 * test-mode stand-in — with the exact message `requestPayment:cancel`. That is what is
 * matched here, and it becomes [MiniAppException.HostInteractionInterrupted] rather than
 * a user-cancellation type: the host uses one signal, and nothing in this SDK's evidence
 * separates a user dismissing the payment interface from any other condition that ends
 * the interaction without completing it.
 *
 * **No other form is matched.** The host's scan, media, and subscription interfaces also
 * use `<api>:fail cancel` for a dismissed interface, but no evidence covers that form for
 * this API, so a message like `requestPayment:fail cancel` remains a
 * [MiniAppException.HostFailure]. A real merchant-environment run has to show whether
 * this API ever produces it; classifying on a convention would put a guess into the path
 * that decides whether a payment attempt was cancelled or broken.
 *
 * This is the only place the payment failure text is interpreted. As with the SDK's
 * existing host-error model, the text can remain available as diagnostic
 * `hostMessage`; consumer UI must not print it, and the example reports only a closed
 * failure label.
 */
internal fun mapWechatRequestPaymentFailure(result: WxGeneralCallbackResult): MiniAppException =
    if (result.errMsg == PAYMENT_INTERRUPTED_ERRMSG) {
        MiniAppException.HostInteractionInterrupted(
            host = "wechat",
            operation = "requestPayment",
            hostMessage = result.errMsg,
        )
    } else {
        mapWechatHostFailure(operation = "requestPayment", result = result)
    }

/** The exact host message an ended payment interaction produces. */
private const val PAYMENT_INTERRUPTED_ERRMSG: String = "requestPayment:cancel"

/**
 * What a raw `wx.requirePrivacyAuthorize` failure means.
 *
 * A declined privacy contract is the user's answer and is modelled as an outcome;
 * only a failure that is not the user's answer becomes an error.
 */
internal sealed interface WxPrivacyAuthorizeFailure {
    /** The user did not accept the privacy contract. */
    data object Refused : WxPrivacyAuthorizeFailure

    /** The request failed for a reason that is not the user's answer. */
    data class Failed(val error: MiniAppException) : WxPrivacyAuthorizeFailure
}

/**
 * Classifies a raw `wx.requirePrivacyAuthorize` failure.
 *
 * WeChat reports a declined privacy contract through the failure path rather
 * than through a dedicated result type. Only the recognizable refusal message
 * is classified as [WxPrivacyAuthorizeFailure.Refused]. Every unrecognized
 * failure remains a host failure: treating an arbitrary system or invocation
 * error as a user decision would hide a condition the consumer must diagnose.
 *
 * This is the only place the failure text is interpreted. Nothing outside this
 * file reads it, and the raw message never reaches a public type.
 */
internal fun mapWechatPrivacyAuthorizeFailure(
    result: WxGeneralCallbackResult,
): WxPrivacyAuthorizeFailure =
    if (result.errMsg.contains(PRIVACY_REFUSED_ERRMSG_MARKER, ignoreCase = true)) {
        WxPrivacyAuthorizeFailure.Refused
    } else {
        WxPrivacyAuthorizeFailure.Failed(
            mapWechatHostFailure(operation = "requirePrivacyAuthorize", result = result),
        )
    }

/** Substring WeChat embeds in `errMsg` when the user did not authorize the contract. */
private const val PRIVACY_REFUSED_ERRMSG_MARKER: String = "privacy permission is not authorized"

/**
 * What a raw file-system failure means.
 *
 * WeChat reports a missing path and every other file failure through the same
 * failure callback, so a caller that needs "does this exist?" has to tell one
 * from the other.
 */
internal sealed interface WxFileSystemFailure {
    /** The host reports that the path does not exist. */
    data object NotFound : WxFileSystemFailure

    /** The call failed for a reason that is not a missing path. */
    data class Failed(val error: MiniAppException) : WxFileSystemFailure
}

/**
 * Classifies a raw file-system failure.
 *
 * WeChat publishes no error code that separates a missing path from a permission
 * or path error, so unlike the session check there is no way to answer "does this
 * exist?" without reading the failure text. WeChat's own pages for `access` and
 * `unlink` document `no such file or directory` as the text for a missing path,
 * so only that documented shape becomes [WxFileSystemFailure.NotFound].
 *
 * Every other failure stays a [MiniAppException.HostFailure]. That direction is
 * deliberate: reporting a permission or invalid-path error as "the file is not
 * there" would tell a caller to create a file it may not be allowed to create.
 *
 * This is the only place the failure text is interpreted, and the raw message
 * never reaches a public type.
 */
internal fun mapWechatFileSystemFailure(
    operation: String,
    result: WxGeneralCallbackResult,
): WxFileSystemFailure =
    if (result.errMsg.contains(NOT_FOUND_ERRMSG_MARKER, ignoreCase = true)) {
        WxFileSystemFailure.NotFound
    } else {
        WxFileSystemFailure.Failed(mapWechatHostFailure(operation = operation, result = result))
    }

/** Substring WeChat documents as the failure text for a missing path. */
private const val NOT_FOUND_ERRMSG_MARKER: String = "no such file or directory"
