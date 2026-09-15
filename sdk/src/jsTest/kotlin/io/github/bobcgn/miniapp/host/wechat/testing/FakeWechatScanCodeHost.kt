package io.github.bobcgn.miniapp.host.wechat.testing

import io.github.bobcgn.miniapp.host.wechat.adapter.WechatScanCodeHost
import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxScanCodeSuccessResult

/**
 * The FakeAdapter boundary for scanning: a stand-in for the WeChat `scanCode`
 * callbacks.
 *
 * It implements the raw callback port, so a test can drive `WechatScanCode`
 * without a WeChat runtime and can reproduce answers a real host produces rarely,
 * such as content that is missing or a descriptive field of the wrong type.
 *
 * There is no permission or privacy state here, because the adapter asks for
 * neither: `wx.scanCode` drives WeChat's own interface, and nothing in the host
 * contract ties a permission to it.
 *
 * @param supported whether this host exposes the scan API
 * @param text content the host reports, typed loosely so a test can supply a
 *   malformed answer
 * @param scanType format name the host reports, typed loosely for the same reason
 * @param charSet character set the host reports, typed loosely
 * @param rawData decoded bytes the host reports, typed loosely
 * @param path image path the host reports, typed loosely; the default models a
 *   host that reported none, which is the case WeChat is documented to produce
 */
internal class FakeWechatScanCodeHost(
    var supported: Boolean = true,
    var text: Any? = "https://example.com/scanned",
    var scanType: Any? = "QR_CODE",
    var charSet: Any? = "utf-8",
    var rawData: Any? = "raw-bytes",
    var path: Any? = null,
) : WechatScanCodeHost {
    /** Message the scan fails with, or `null` to succeed. */
    var failureMessage: String? = null

    /** When true, the scan reports its outcome twice, as a defective host might. */
    var completeTwice: Boolean = false

    var calls: Int = 0
        private set

    /** The camera choice last handed to the host, so a test can assert it was forwarded. */
    var lastOnlyFromCamera: Boolean? = null
        private set

    /** The categories last handed to the host, so a test can assert them. */
    var lastCategories: List<String>? = null
        private set

    override fun isSupported(): Boolean = supported

    override fun scan(
        onlyFromCamera: Boolean,
        scanCategories: List<String>,
        success: (WxScanCodeSuccessResult) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        calls += 1
        lastOnlyFromCamera = onlyFromCamera
        lastCategories = scanCategories

        val complete = {
            val message = failureMessage
            if (message == null) {
                success(
                    fakeScanCodeSuccess(
                        result = text,
                        scanType = scanType,
                        charSet = charSet,
                        rawData = rawData,
                        path = path,
                    ),
                )
            } else {
                failure(fakeWxFailure(message))
            }
        }

        complete()
        if (completeTwice) complete()
    }
}
