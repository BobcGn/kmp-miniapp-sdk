package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.host.wechat.interop.WxRequestFailureResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxRequestSuccessResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxRequestTask
import io.github.bobcgn.miniapp.host.wechat.interop.wx
import io.github.bobcgn.miniapp.host.wechat.interop.wxRequestHeader
import io.github.bobcgn.miniapp.host.wechat.interop.wxRequestOptions

/** Callback port that keeps [WechatNetwork] independently testable. */
internal interface WechatNetworkHost {
    /**
     * Starts an HTTP exchange.
     *
     * @return a handle that aborts the exchange, or `null` when the enclosing
     *   runtime cannot abort it
     */
    fun request(
        url: String,
        method: String,
        headers: Map<String, String>,
        body: String?,
        timeoutMillis: Int?,
        success: (WxRequestSuccessResult) -> Unit,
        failure: (WxRequestFailureResult) -> Unit,
    ): WxRequestTask?
}

/** Production transport port backed directly by the global WeChat API. */
internal object WxNetworkHost : WechatNetworkHost {
    override fun request(
        url: String,
        method: String,
        headers: Map<String, String>,
        body: String?,
        timeoutMillis: Int?,
        success: (WxRequestSuccessResult) -> Unit,
        failure: (WxRequestFailureResult) -> Unit,
    ): WxRequestTask? {
        val options = wxRequestOptions(
            url = url,
            method = method,
            data = body,
            timeoutMillis = timeoutMillis,
        )
        if (headers.isNotEmpty()) {
            options.header = wxRequestHeader(headers)
        }
        options.success = success
        options.fail = failure
        return wx.request(options)
    }
}
