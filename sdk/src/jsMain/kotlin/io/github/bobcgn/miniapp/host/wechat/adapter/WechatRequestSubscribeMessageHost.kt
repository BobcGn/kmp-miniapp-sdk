package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.host.wechat.WeChatTemplateId
import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxRequestSubscribeMessageSuccessResult
import io.github.bobcgn.miniapp.host.wechat.interop.hasWxRequestSubscribeMessage
import io.github.bobcgn.miniapp.host.wechat.interop.wx
import io.github.bobcgn.miniapp.host.wechat.interop.wxRequestSubscribeMessageOptions

/** Callback port that keeps [WechatRequestSubscribeMessage] independently testable. */
internal interface WechatRequestSubscribeMessageHost {
    /** Whether this host can ask the user about templates at all. */
    fun isSupported(): Boolean

    /** Asks the host to put the templates in front of the user. */
    fun request(
        templateIds: List<WeChatTemplateId>,
        success: (WxRequestSubscribeMessageSuccessResult) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ): Unit
}

/** Production subscription port backed directly by the global WeChat API. */
internal object WxRequestSubscribeMessageHost : WechatRequestSubscribeMessageHost {
    override fun isSupported(): Boolean = hasWxRequestSubscribeMessage()

    override fun request(
        templateIds: List<WeChatTemplateId>,
        success: (WxRequestSubscribeMessageSuccessResult) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        val options = wxRequestSubscribeMessageOptions(templateIds)
        options.success = success
        options.fail = failure
        wx.requestSubscribeMessage(options)
    }
}
