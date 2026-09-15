package io.github.bobcgn.miniapp.host.wechat.testing

import io.github.bobcgn.miniapp.host.wechat.adapter.WechatFileSystemHost
import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxReadFileSuccessResult

/**
 * The FakeAdapter boundary for the file system: a stand-in for the WeChat
 * `FileSystemManager` callbacks backed by an in-memory map.
 *
 * It implements the raw callback port, so a test can drive `WechatFileSystem`
 * without a WeChat runtime and can reproduce conditions a real host produces
 * rarely, such as a `readFile` that came back as binary rather than text, or a
 * manager that exposes only some of its methods.
 *
 * Every operation is switchable independently, because the manager is a host
 * object and may offer any subset of its methods.
 */
internal class FakeWechatFileSystemHost(
    var readSupported: Boolean = true,
    var writeSupported: Boolean = true,
    var accessSupported: Boolean = true,
    var removeSupported: Boolean = true,
    var sandboxPath: String? = "/sandbox",
) : WechatFileSystemHost {
    private val files = mutableMapOf<String, Any?>()

    /** Message `readFile` fails with, or `null` to answer from the map. */
    var readFailure: String? = null

    /** Message `writeFile` fails with, or `null` to succeed. */
    var writeFailure: String? = null

    /** Message `access` fails with, or `null` to answer from the map. */
    var accessFailure: String? = null

    /** Message `unlink` fails with, or `null` to answer from the map. */
    var removeFailure: String? = null

    /** When true, `readFile` reports its outcome twice, as a defective host might. */
    var completeReadTwice: Boolean = false

    var readCalls: Int = 0
        private set

    var writeCalls: Int = 0
        private set

    var accessCalls: Int = 0
        private set

    var removeCalls: Int = 0
        private set

    /** The path last handed to `readFile`/`writeFile`/`unlink`, so a test can assert it. */
    var lastPath: String? = null
        private set

    /** The content last handed to `writeFile`. */
    var lastWritten: String? = null
        private set

    /** Places a raw file entry, which may not be text, at [path]. */
    fun putRawFile(path: String, content: Any?) {
        files[path] = content
    }

    /** Whether the fake still holds a file at [path]. */
    fun contains(path: String): Boolean = files.containsKey(path)

    override fun isReadSupported(): Boolean = readSupported

    override fun isWriteSupported(): Boolean = writeSupported

    override fun isAccessSupported(): Boolean = accessSupported

    override fun isRemoveSupported(): Boolean = removeSupported

    override fun userDataPath(): String? = sandboxPath

    override fun read(
        filePath: String,
        success: (WxReadFileSuccessResult) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        readCalls += 1
        lastPath = filePath

        val complete = {
            val message = readFailure
            when {
                message != null -> failure(fakeWxFailure(message))
                files.containsKey(filePath) -> success(fakeReadFileSuccess(files[filePath]))
                else -> failure(fakeWxFailure(NOT_FOUND))
            }
        }

        complete()
        if (completeReadTwice) complete()
    }

    override fun write(
        filePath: String,
        data: String,
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        writeCalls += 1
        lastPath = filePath
        lastWritten = data

        val message = writeFailure
        if (message == null) {
            files[filePath] = data
            success()
        } else {
            failure(fakeWxFailure(message))
        }
    }

    override fun access(
        path: String,
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        accessCalls += 1
        lastPath = path

        val message = accessFailure
        when {
            message != null -> failure(fakeWxFailure(message))
            files.containsKey(path) -> success()
            else -> failure(fakeWxFailure(NOT_FOUND))
        }
    }

    override fun unlink(
        filePath: String,
        success: () -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ) {
        removeCalls += 1
        lastPath = filePath

        val message = removeFailure
        when {
            message != null -> failure(fakeWxFailure(message))
            files.containsKey(filePath) -> {
                files.remove(filePath)
                success()
            }

            else -> failure(fakeWxFailure(NOT_FOUND))
        }
    }

    private companion object {
        /** The failure text WeChat documents for a missing path. */
        private const val NOT_FOUND: String = "fail no such file or directory"
    }
}
