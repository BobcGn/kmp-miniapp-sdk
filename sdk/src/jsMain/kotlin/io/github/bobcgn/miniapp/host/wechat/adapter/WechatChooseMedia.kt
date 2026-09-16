package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.async.awaitHostCallback
import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.WeChatDeviceCapabilities
import io.github.bobcgn.miniapp.host.wechat.WeChatMediaFile
import io.github.bobcgn.miniapp.host.wechat.WeChatMediaFileType
import io.github.bobcgn.miniapp.host.wechat.WeChatMediaRequest
import io.github.bobcgn.miniapp.host.wechat.interop.WxMediaFile
import io.github.bobcgn.miniapp.host.wechat.interop.WxMediaSelection
import io.github.bobcgn.miniapp.host.wechat.interop.wxMediaSelection

/**
 * WeChat media selection, deliberately WeChat-specific.
 *
 * The selection happens in WeChat's own interface, so what the SDK owns is the
 * request the caller describes, the files the host returns, and the errors in
 * between. It does not own the interface, model a camera or a player, parse what
 * the user picked, or store it.
 *
 * Nothing here asks the host for a permission. WeChat's own picker needs none, and
 * the host's scope list contains no scope for reading the library, so there is
 * nothing the SDK could map even if it tried.
 *
 * The files the host returns are temporary host resources. This adapter hands over
 * the paths the host reported and keeps no copy, so it makes no claim to their
 * lifetime beyond the call that produced them.
 *
 * WeChat reports no task handle for this call, so coroutine cancellation only
 * stops the caller waiting; the host operation is not aborted and no abort hook is
 * invented for it.
 */
internal class WechatChooseMedia(
    private val host: WechatChooseMediaHost = WxChooseMediaHost,
) {
    /**
     * Asks the host to let the user choose media through its own interface.
     *
     * @throws MiniAppException.UnsupportedCapability when the host has no media API
     * @throws MiniAppException.HostInteractionInterrupted when the host interaction
     *   ended without enough information to identify why
     * @throws MiniAppException.InvalidResponse when the host answers with something
     *   the contract cannot carry
     * @throws MiniAppException.HostFailure when the selection fails for any other reason
     */
    suspend fun choose(request: WeChatMediaRequest): List<WeChatMediaFile> {
        if (!host.isSupported()) {
            throw MiniAppException.UnsupportedCapability(WeChatDeviceCapabilities.ChooseMedia)
        }

        val selection = awaitHostCallback { success, failure ->
            host.choose(
                request = request,
                success = { result -> success(wxMediaSelection(result)) },
                failure = { result -> failure(mapWechatChooseMediaFailure(result)) },
            )
            null
        }

        return when (selection) {
            is WxMediaSelection.Present -> {
                if (selection.files.isEmpty()) {
                    throw MiniAppException.InvalidResponse(
                        "The WeChat host answered chooseMedia successfully without a selected file",
                    )
                }
                selection.files.map { it.toMediaFile() }
            }

            WxMediaSelection.Unreadable -> throw MiniAppException.InvalidResponse(
                "The WeChat host answered chooseMedia with a selection the SDK cannot read",
            )
        }
    }
}

/**
 * Turns an entry the interop layer accepted into the SDK's own model.
 *
 * The kind is resolved to the SDK's enumeration where it can be, and the host's own
 * name is kept either way, so an unknown kind is reported rather than dropped.
 */
private fun WxMediaFile.Present.toMediaFile(): WeChatMediaFile = WeChatMediaFile(
    tempFilePath = tempFilePath,
    sizeBytes = sizeBytes,
    fileType = WeChatMediaFileType.fromHostValue(fileType),
    hostFileType = fileType,
    durationSeconds = durationSeconds,
    width = width,
    height = height,
    thumbTempFilePath = thumbTempFilePath,
)
