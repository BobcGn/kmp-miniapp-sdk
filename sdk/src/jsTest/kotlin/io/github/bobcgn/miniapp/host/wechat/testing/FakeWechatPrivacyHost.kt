package io.github.bobcgn.miniapp.host.wechat.testing

import io.github.bobcgn.miniapp.host.wechat.adapter.WechatPrivacyHost
import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxGetPrivacySettingSuccessResult

/**
 * The FakeAdapter boundary for privacy: a stand-in for the WeChat
 * `getPrivacySetting` and `requirePrivacyAuthorize` callbacks.
 *
 * It implements the raw callback port, so a test can drive `WechatPrivacy`
 * without a WeChat runtime and can reproduce conditions a real host produces
 * rarely, such as a base library without the privacy APIs or an answer whose
 * fields have the wrong type.
 *
 * [holdAuthorize] exists so a test can keep one authorization in flight while
 * other callers arrive, which is the only way to observe request merging rather
 * than the sequential path.
 *
 * @param supported whether this host exposes the privacy APIs at all
 * @param needAuthorization raw `needAuthorization` value, typed loosely so a test
 *   can supply a malformed one
 * @param privacyContractName raw contract name, typed loosely for the same reason
 */
internal class FakeWechatPrivacyHost(
    var supported: Boolean = true,
    var needAuthorization: Any? = true,
    var privacyContractName: Any? = "《example privacy contract》",
) : WechatPrivacyHost {
    /** Message `getPrivacySetting` fails with, or `null` to succeed. */
    var getPrivacySettingFailure: String? = null

    /** Message `requirePrivacyAuthorize` fails with, or `null` to succeed. */
    var authorizeFailure: String? = null

    /** When true, `requirePrivacyAuthorize` holds its callbacks until released. */
    var holdAuthorize: Boolean = false

    /** When true, `requirePrivacyAuthorize` reports its outcome twice, as a defective host might. */
    var completeTwice: Boolean = false

    var getPrivacySettingCalls: Int = 0
        private set

    var authorizeCalls: Int = 0
        private set

    private var pendingAuthorize: (() -> Unit)? = null

    /** Completes a call that [holdAuthorize] kept in flight. */
    fun releaseAuthorize() {
        val pending = requireNotNull(pendingAuthorize) {
            "No privacy authorization call is being held"
        }
        pendingAuthorize = null
        pending()
    }

    override fun isSupported(): Boolean = supported

    override fun getPrivacySetting(
        success: (WxGetPrivacySettingSuccessResult) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        getPrivacySettingCalls += 1

        val message = getPrivacySettingFailure
        if (message != null) {
            failure(fakeWxFailure(message))
            return
        }

        success(fakeGetPrivacySettingSuccess(needAuthorization, privacyContractName))
    }

    override fun requirePrivacyAuthorize(
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        authorizeCalls += 1

        val complete = {
            val message = authorizeFailure
            if (message == null) {
                needAuthorization = false
                success()
            } else {
                failure(fakeWxFailure(message))
            }
        }

        if (holdAuthorize) {
            pendingAuthorize = complete
        } else {
            complete()
            if (completeTwice) complete()
        }
    }
}
