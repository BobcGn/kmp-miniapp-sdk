package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.host.wechat.WeChatMediaRequest
import io.github.bobcgn.miniapp.host.wechat.interop.WxChooseMediaSuccessResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.interop.hasWxChooseMedia
import io.github.bobcgn.miniapp.host.wechat.interop.wx
import io.github.bobcgn.miniapp.host.wechat.interop.wxChooseMediaOptions

/** Callback port that keeps [WechatChooseMedia] independently testable. */
internal interface WechatChooseMediaHost {
    /** Whether this host can select media at all. */
    fun isSupported(): Boolean

    /** Asks the host to let the user choose media through its own interface. */
    fun choose(
        request: WeChatMediaRequest,
        success: (WxChooseMediaSuccessResult) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ): Unit
}

/** Production media port backed directly by the global WeChat API. */
internal object WxChooseMediaHost : WechatChooseMediaHost {
    override fun isSupported(): Boolean = hasWxChooseMedia()

    override fun choose(
        request: WeChatMediaRequest,
        success: (WxChooseMediaSuccessResult) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        val options = wxChooseMediaOptions(
            count = request.count,
            mediaTypes = request.mediaType.map { it.hostValue },
            sourceTypes = request.sourceType.map { it.hostValue },
            maxDurationSeconds = request.maxDurationSeconds,
            sizeTypes = request.sizeType.map { it.hostValue },
            camera = request.camera?.hostValue,
        )
        options.success = success
        options.fail = failure
        wx.chooseMedia(options)
    }
}
