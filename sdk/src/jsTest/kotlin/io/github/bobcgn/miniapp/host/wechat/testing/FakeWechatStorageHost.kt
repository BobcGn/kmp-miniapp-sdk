package io.github.bobcgn.miniapp.host.wechat.testing

import io.github.bobcgn.miniapp.host.wechat.adapter.WechatStorageHost
import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult

/**
 * The FakeAdapter boundary for storage: an in-memory stand-in for the WeChat
 * storage callbacks.
 *
 * It implements the raw callback port, not the SDK contract, so a test can drive
 * `WechatStorage` without a WeChat runtime and can reproduce host behavior the
 * contract does not allow, such as a read that returns a non-string value.
 *
 * @param failNextWith a `fail` message this host reports once, then forgets
 */
internal class FakeWechatStorageHost(
    failNextWith: String? = null,
) : WechatStorageHost {
    private val values = mutableMapOf<String, Any?>()
    private var nextFailure: String? = failNextWith

    /** Makes the next storage call fail with [message]. */
    fun failNextCallWith(message: String) {
        nextFailure = message
    }

    /** Places a raw host value, which may not be a string, at [key]. */
    fun putRawValue(key: String, value: Any?) {
        values[key] = value
    }

    override fun get(
        key: String,
        success: (Any?) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        if (reportFailure(failure)) return
        if (values.containsKey(key)) {
            success(values[key])
        } else {
            failure(fakeWxFailure("getStorage:fail data not found"))
        }
    }

    override fun set(
        key: String,
        value: String,
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        if (reportFailure(failure)) return
        values[key] = value
        success()
    }

    override fun remove(
        key: String,
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        if (reportFailure(failure)) return
        values.remove(key)
        success()
    }

    private fun reportFailure(failure: (WxGeneralCallbackResult) -> Unit): Boolean {
        val message = nextFailure ?: return false
        nextFailure = null
        failure(fakeWxFailure(message))
        return true
    }
}
