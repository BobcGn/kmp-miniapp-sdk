package io.github.bobcgn.miniapp.host.wechat.testing

import io.github.bobcgn.miniapp.host.wechat.adapter.WechatPermissionHost
import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxGetSettingSuccessResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxOpenSettingSuccessResult

/**
 * The FakeAdapter boundary for permissions: a stand-in for the WeChat
 * `getSetting`, `authorize`, and `openSetting` callbacks.
 *
 * It implements the raw callback port, so a test can drive `WechatPermissions`
 * without a WeChat runtime and can reproduce conditions a real host produces
 * rarely, such as an authorization map that carries a value the contract cannot
 * read or a host that answers without a map at all.
 *
 * [holdAuthorize] exists so a test can keep one host call in flight while other
 * callers arrive, which is the only way to observe request merging rather than
 * the short-circuit an already-decided permission produces.
 *
 * @param authSetting raw authorization map the host currently reports
 */
internal class FakeWechatPermissionHost(
    var authSetting: Any? = fakeWxAuthSetting(),
) : WechatPermissionHost {
    /** Message `authorize` fails with, or `null` to succeed. */
    var authorizeFailure: String? = null

    /** Message `getSetting` fails with, or `null` to succeed. */
    var getSettingFailure: String? = null

    /** Message `openSetting` fails with, or `null` to succeed. */
    var openSettingFailure: String? = null

    /** When true, `authorize` holds its callbacks until [releaseAuthorize] is called. */
    var holdAuthorize: Boolean = false

    /**
     * The map `openSetting` reports, defaulting to [authSetting].
     *
     * Setting it separately lets a test model a settings page that answered with
     * something the contract cannot read while `getSetting` still answers normally.
     */
    var openSettingAuthSetting: Any? = null

    var getSettingCalls: Int = 0
        private set

    var authorizeCalls: Int = 0
        private set

    var openSettingCalls: Int = 0
        private set

    /** The scope last handed to the host, so a test can assert the mapping. */
    var lastAuthorizeScope: String? = null
        private set

    private var pendingAuthorize: (() -> Unit)? = null

    /** Completes a call that [holdAuthorize] kept in flight. */
    fun releaseAuthorize() {
        val pending = requireNotNull(pendingAuthorize) {
            "No authorize call is being held"
        }
        pendingAuthorize = null
        pending()
    }

    override fun getSetting(
        success: (WxGetSettingSuccessResult) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        getSettingCalls += 1

        val message = getSettingFailure
        if (message != null) {
            failure(fakeWxFailure(message))
            return
        }

        success(fakeGetSettingSuccess(authSetting))
    }

    override fun authorize(
        scope: String,
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        authorizeCalls += 1
        lastAuthorizeScope = scope

        val complete = {
            val message = authorizeFailure
            if (message == null) {
                success()
            } else {
                failure(fakeWxFailure(message))
            }
        }

        if (holdAuthorize) {
            pendingAuthorize = complete
        } else {
            complete()
        }
    }

    override fun openSetting(
        success: (WxOpenSettingSuccessResult) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        openSettingCalls += 1

        val message = openSettingFailure
        if (message != null) {
            failure(fakeWxFailure(message))
            return
        }

        success(fakeOpenSettingSuccess(openSettingAuthSetting ?: authSetting))
    }
}
