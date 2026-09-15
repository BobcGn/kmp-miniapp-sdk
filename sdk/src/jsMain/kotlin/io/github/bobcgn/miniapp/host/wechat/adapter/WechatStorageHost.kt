package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.interop.wx
import io.github.bobcgn.miniapp.host.wechat.interop.wxGetStorageOptions
import io.github.bobcgn.miniapp.host.wechat.interop.wxRemoveStorageOptions
import io.github.bobcgn.miniapp.host.wechat.interop.wxSetStorageOptions

/** Callback port used to keep [WechatStorage] independently testable. */
internal interface WechatStorageHost {
    fun get(
        key: String,
        success: (Any?) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ): Unit

    fun set(
        key: String,
        value: String,
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ): Unit

    fun remove(
        key: String,
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ): Unit
}

/** Production callback port backed directly by the WeChat global `wx` object. */
internal object WxStorageHost : WechatStorageHost {
    override fun get(
        key: String,
        success: (Any?) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ): Unit {
        val options = wxGetStorageOptions(key)
        options.success = { result -> success(result.data) }
        options.fail = failure
        wx.getStorage(options)
    }

    override fun set(
        key: String,
        value: String,
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ): Unit {
        val options = wxSetStorageOptions(key, value)
        options.success = { success() }
        options.fail = failure
        wx.setStorage(options)
    }

    override fun remove(
        key: String,
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ): Unit {
        val options = wxRemoveStorageOptions(key)
        options.success = { success() }
        options.fail = failure
        wx.removeStorage(options)
    }
}
