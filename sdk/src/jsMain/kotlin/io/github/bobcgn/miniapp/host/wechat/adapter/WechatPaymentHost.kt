package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.host.wechat.WeChatPaymentRequest
import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxRequestPaymentSuccessResult
import io.github.bobcgn.miniapp.host.wechat.interop.hasWxRequestPayment
import io.github.bobcgn.miniapp.host.wechat.interop.wx
import io.github.bobcgn.miniapp.host.wechat.interop.wxRequestPaymentOptions

/** Callback port that keeps [WechatPayment] independently testable. */
internal interface WechatPaymentHost {
    /** Whether this host exposes the payment API at all. */
    fun isSupported(): Boolean

    /**
     * Hands [request] to the host's payment interface.
     *
     * The host offers no abort handle: a payment interaction is the host's own, and the
     * SDK neither cancels it nor invents a way to.
     */
    fun request(
        request: WeChatPaymentRequest,
        success: (WxRequestPaymentSuccessResult) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ): Unit
}

/** Production payment port backed directly by the global WeChat API. */
internal object WxRequestPaymentHost : WechatPaymentHost {
    override fun isSupported(): Boolean = hasWxRequestPayment()

    override fun request(
        request: WeChatPaymentRequest,
        success: (WxRequestPaymentSuccessResult) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        val options = wxRequestPaymentOptions(
            timeStamp = request.timeStamp,
            nonceStr = request.nonceStr,
            prepayPackage = request.prepayPackage,
            signType = request.signType.hostValue,
            paySign = request.paySign,
        )
        options.success = success
        options.fail = failure
        wx.requestPayment(options)
    }
}
