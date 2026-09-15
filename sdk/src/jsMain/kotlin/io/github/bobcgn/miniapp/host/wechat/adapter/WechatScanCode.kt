package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.async.awaitHostCallback
import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.WeChatDeviceCapabilities
import io.github.bobcgn.miniapp.host.wechat.WeChatScanFormat
import io.github.bobcgn.miniapp.host.wechat.WeChatScanRequest
import io.github.bobcgn.miniapp.host.wechat.WeChatScanResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxScanSample
import io.github.bobcgn.miniapp.host.wechat.interop.wxScanSample

/**
 * WeChat scanning, deliberately WeChat-specific.
 *
 * The scan happens in WeChat's own interface, so what the SDK owns is the request
 * the caller describes, the answer the host gives, and the errors in between. It
 * does not own the interface, model a camera, parse anything it is handed, or
 * store what was scanned.
 *
 * Nothing here asks the host for a permission. `wx.scanCode` drives WeChat's own
 * scanning interface and no permission precondition for it could be established
 * from the host contract, so the SDK neither prompts for one nor invents a
 * mapping to `scope.camera`, which exists on this host but is not tied to this
 * API by anything the SDK can cite.
 *
 * WeChat reports no task handle for this call, so coroutine cancellation only
 * stops the caller waiting; the host operation is not aborted and no abort hook
 * is invented for it.
 */
internal class WechatScanCode(
    private val host: WechatScanCodeHost = WxScanCodeHost,
) {
    /**
     * Asks the host to scan, through its own interface.
     *
     * The host uses the same interruption signal for a user dismissal and for a
     * camera restriction that prevents the interface from opening. It is therefore
     * reported as [MiniAppException.HostInteractionInterrupted] without assigning
     * a cause the host did not provide.
     *
     * @throws MiniAppException.UnsupportedCapability when the host has no scan API
     * @throws MiniAppException.HostInteractionInterrupted when the host interaction
     *   ended without enough information to identify why
     * @throws MiniAppException.InvalidResponse when the host answers with something
     *   the contract cannot carry
     * @throws MiniAppException.HostFailure when the scan fails for any other reason
     */
    suspend fun scan(request: WeChatScanRequest = WeChatScanRequest()): WeChatScanResult {
        if (!host.isSupported()) {
            throw MiniAppException.UnsupportedCapability(WeChatDeviceCapabilities.ScanCode)
        }

        val sample = awaitHostCallback { success, failure ->
            host.scan(
                onlyFromCamera = request.onlyFromCamera,
                scanCategories = request.allowedCategories.map { it.hostValue },
                success = { result -> success(wxScanSample(result)) },
                failure = { result -> failure(mapWechatScanFailure(result)) },
            )
            null
        }

        return when (sample) {
            is WxScanSample.Present -> WeChatScanResult(
                text = sample.text,
                // An unrecognized name is not a failure: the host reported a format
                // this SDK does not know, and the name itself is kept below.
                format = sample.scanType?.let { WeChatScanFormat.fromHostValue(it) },
                scanType = sample.scanType,
                charSet = sample.charSet,
                rawData = sample.rawData,
                path = sample.path,
            )

            WxScanSample.Unreadable -> throw MiniAppException.InvalidResponse(
                "The WeChat host answered scanCode with a result the SDK cannot read",
            )
        }
    }
}
