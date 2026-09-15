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
