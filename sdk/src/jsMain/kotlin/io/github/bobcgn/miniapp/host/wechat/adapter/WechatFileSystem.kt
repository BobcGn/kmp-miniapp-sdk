package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.async.awaitHostCallback
import io.github.bobcgn.miniapp.capability.CapabilityKey
import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.WeChatDeviceCapabilities
import io.github.bobcgn.miniapp.host.wechat.interop.WxFileText
import io.github.bobcgn.miniapp.host.wechat.interop.wxFileText

/**
 * WeChat file access, deliberately WeChat-specific.
 *
 * It works only on the mini program file sandbox and only on UTF-8 text. WeChat
 * keeps no portable file API and exposes no descriptor, stream, or directory
 * traversal here, and the SDK adds none: a consumer that needs those is not
 * served by pretending they exist.
 *
 * The four operations are gated separately, because each is a separate host
 * method and a host may expose any subset.
 *
 * The manager's async calls carry no abort handle, so coroutine cancellation only
 * stops the caller waiting.
 *
 * Paths are the caller's responsibility. The SDK does not normalise them, so it
 * makes no claim to prevent traversal: a consumer must pass a path inside the
 * sandbox, which is where [WeChatDeviceCapabilities.FileSystemSandboxPath] comes
 * from.
 */
internal class WechatFileSystem(
    private val host: WechatFileSystemHost = WxFileSystemHost,
) {
    /**
     * Returns the sandbox root user files live under.
     *
     * @throws MiniAppException.UnsupportedCapability when the host reports none
     */
    fun userDataPath(): String = host.userDataPath()
        ?: throw MiniAppException.UnsupportedCapability(
            WeChatDeviceCapabilities.FileSystemSandboxPath,
        )

    /**
     * Reads a file as UTF-8 text.
     *
     * An empty file is an empty string, which is a value the contract carries. Any
     * answer that is not text is reported as such rather than stringified, because
     * rendering bytes as text would invent content the file does not hold.
     *
     * @throws MiniAppException.UnsupportedCapability when the host cannot read files
     * @throws MiniAppException.InvalidResponse when the host answers with something
     *   other than text
     * @throws MiniAppException.HostFailure when the read fails
     */
    suspend fun readText(filePath: String): String {
        requireSupported(WeChatDeviceCapabilities.FileSystemRead, host::isReadSupported)

        val text = awaitHostCallback { success, failure ->
            host.read(
                filePath = filePath,
                success = { result -> success(wxFileText(result)) },
                failure = { result ->
                    failure(mapWechatHostFailure(operation = "readFile", result = result))
                },
            )
            null
        }

        return when (text) {
            is WxFileText.Present -> text.text
            WxFileText.Unreadable -> throw MiniAppException.InvalidResponse(
                "The WeChat host answered readFile with a value the SDK cannot read as text",
            )
        }
    }

    /**
     * Writes [content] to a file as UTF-8 text, replacing anything already there.
     *
     * The parent directory must already exist; this does not create one.
     *
     * @throws MiniAppException.UnsupportedCapability when the host cannot write files
     * @throws MiniAppException.HostFailure when the write fails
     */
    suspend fun writeText(filePath: String, content: String) {
        requireSupported(WeChatDeviceCapabilities.FileSystemWrite, host::isWriteSupported)

        awaitHostCallback { success, failure ->
            host.write(
                filePath = filePath,
                data = content,
                success = { success(Unit) },
                failure = { result ->
                    failure(mapWechatHostFailure(operation = "writeFile", result = result))
                },
            )
            null
        }
    }

    /**
     * Reports whether [path] exists and is accessible.
     *
     * A host that reports the path missing answers `false`. Any other failure
     * raises, because a permission error and a missing file are different answers
     * and only one of them means "you may create this".
     *
     * @throws MiniAppException.UnsupportedCapability when the host cannot test paths
     * @throws MiniAppException.HostFailure when the check fails for any other reason
     */
    suspend fun exists(path: String): Boolean {
        requireSupported(WeChatDeviceCapabilities.FileSystemAccess, host::isAccessSupported)

        val outcome = awaitHostCallback { success, failure ->
            host.access(
                path = path,
                success = { success(true) },
                failure = { result ->
                    when (val classified = mapWechatFileSystemFailure("access", result)) {
                        WxFileSystemFailure.NotFound -> success(false)
                        is WxFileSystemFailure.Failed -> failure(classified.error)
                    }
                },
            )
            null
        }

        return outcome
    }

    /**
     * Removes a file.
     *
     * Removing a file that is not there fails, because that is what WeChat's
     * `unlink` does; this reports the host faithfully rather than inventing an
     * idempotence the host does not offer. A caller that wants that must check
     * [exists] first.
     *
     * @throws MiniAppException.UnsupportedCapability when the host cannot remove files
     * @throws MiniAppException.HostFailure when the file cannot be removed, including
     *   when it is not there
     */
    suspend fun remove(filePath: String) {
        requireSupported(WeChatDeviceCapabilities.FileSystemRemove, host::isRemoveSupported)

        awaitHostCallback { success, failure ->
            host.unlink(
                filePath = filePath,
                success = { success(Unit) },
                failure = { result ->
                    failure(mapWechatHostFailure(operation = "unlink", result = result))
                },
            )
            null
        }
    }

    private fun requireSupported(capability: CapabilityKey, isSupported: () -> Boolean) {
        if (!isSupported()) {
            throw MiniAppException.UnsupportedCapability(capability)
        }
    }
}
