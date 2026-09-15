package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxReadFileSuccessResult
import io.github.bobcgn.miniapp.host.wechat.interop.hasWxFileSystemMethod
import io.github.bobcgn.miniapp.host.wechat.interop.hasWxUserDataPath
import io.github.bobcgn.miniapp.host.wechat.interop.wx
import io.github.bobcgn.miniapp.host.wechat.interop.wxAccessOptions
import io.github.bobcgn.miniapp.host.wechat.interop.wxReadFileOptions
import io.github.bobcgn.miniapp.host.wechat.interop.wxUnlinkOptions
import io.github.bobcgn.miniapp.host.wechat.interop.wxWriteFileOptions

/** Callback port that keeps [WechatFileSystem] independently testable. */
internal interface WechatFileSystemHost {
    /** Whether this host can read a file. */
    fun isReadSupported(): Boolean

    /** Whether this host can write a file. */
    fun isWriteSupported(): Boolean

    /** Whether this host can test whether a path exists. */
    fun isAccessSupported(): Boolean

    /** Whether this host can remove a file. */
    fun isRemoveSupported(): Boolean

    /**
     * The sandbox root user files live under, or `null` when the host reports none.
     *
     * The SDK never guesses this: a host without it cannot have paths built
     * against it.
     */
    fun userDataPath(): String?

    /** Reads a file through callbacks. */
    fun read(
        filePath: String,
        success: (WxReadFileSuccessResult) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ): Unit

    /** Writes [data] to a file through callbacks. */
    fun write(
        filePath: String,
        data: String,
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ): Unit

    /** Tests whether a path exists through callbacks. */
    fun access(
        path: String,
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ): Unit

    /** Removes a file through callbacks. */
    fun unlink(
        filePath: String,
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ): Unit
}

/** Production file-system port backed directly by the global WeChat API. */
internal object WxFileSystemHost : WechatFileSystemHost {
    override fun isReadSupported(): Boolean = hasWxFileSystemMethod("readFile")

    override fun isWriteSupported(): Boolean = hasWxFileSystemMethod("writeFile")

    override fun isAccessSupported(): Boolean = hasWxFileSystemMethod("access")

    override fun isRemoveSupported(): Boolean = hasWxFileSystemMethod("unlink")

    override fun userDataPath(): String? {
        if (!hasWxUserDataPath()) return null
        // The guard above established that this is a string; the declared type
        // stays `Any?` because an external property can still be absent.
        val path: Any? = wx.env?.USER_DATA_PATH
        return path as? String
    }

    override fun read(
        filePath: String,
        success: (WxReadFileSuccessResult) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        val options = wxReadFileOptions(filePath)
        options.success = success
        options.fail = failure
        wx.getFileSystemManager().readFile?.invoke(options)
    }

    override fun write(
        filePath: String,
        data: String,
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        val options = wxWriteFileOptions(filePath, data)
        options.success = { success() }
        options.fail = failure
        wx.getFileSystemManager().writeFile?.invoke(options)
    }

    override fun access(
        path: String,
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        val options = wxAccessOptions(path)
        options.success = { success() }
        options.fail = failure
        wx.getFileSystemManager().access?.invoke(options)
    }

    override fun unlink(
        filePath: String,
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        val options = wxUnlinkOptions(filePath)
        options.success = { success() }
        options.fail = failure
        wx.getFileSystemManager().unlink?.invoke(options)
    }
}
