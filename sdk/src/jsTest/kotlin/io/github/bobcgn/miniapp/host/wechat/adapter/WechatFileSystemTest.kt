package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxReadFileSuccessResult
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatFileSystemHost
import io.github.bobcgn.miniapp.host.wechat.testing.fakeReadFileSuccess
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * How the WeChat adapter turns the file manager's callbacks into the SDK
 * behaviour.
 *
 * These are the raw-callback behaviours; `WechatFileSystemContractTest` states
 * what must hold for the SDK as a whole.
 */
internal class WechatFileSystemTest {
    private val path = "/sandbox/kmp-miniapp-sdk-bob72.txt"

    @Test
    fun textTheHostReturnsIsReported() = runTest {
        val host = FakeWechatFileSystemHost().apply { putRawFile(path, "hello") }

        assertEquals("hello", WechatFileSystem(host).readText(path))
        assertEquals(path, host.lastPath)
    }

    @Test
    fun anEmptyFileIsReportedAsAnEmptyString() = runTest {
        val host = FakeWechatFileSystemHost().apply { putRawFile(path, "") }

        assertEquals("", WechatFileSystem(host).readText(path))
    }

    @Test
    fun aMissingAnswerIsAnInvalidResponse() = runTest {
        val host = FakeWechatFileSystemHost().apply { putRawFile(path, null) }

        assertFailsWith<MiniAppException.InvalidResponse> { WechatFileSystem(host).readText(path) }
    }

    @Test
    fun binaryContentIsAnInvalidResponse() = runTest {
        val host = FakeWechatFileSystemHost().apply {
            putRawFile(path, js("(new Uint8Array([104, 105]).buffer)"))
        }

        assertFailsWith<MiniAppException.InvalidResponse> { WechatFileSystem(host).readText(path) }
    }

    @Test
    fun aFailedReadIsAHostFailure() = runTest {
        val host = FakeWechatFileSystemHost().apply { readFailure = "readFile:fail permission denied" }

        val failure = assertFailsWith<MiniAppException.HostFailure> {
            WechatFileSystem(host).readText(path)
        }

        assertEquals("readFile", failure.metadata["operation"])
    }

    @Test
    fun aHostThatCannotReadIsAnUnsupportedCapability() = runTest {
        val host = FakeWechatFileSystemHost(readSupported = false)

        val failure = assertFailsWith<MiniAppException.UnsupportedCapability> {
            WechatFileSystem(host).readText(path)
        }

        assertEquals("wechat.filesystem-read", failure.capability.value)
        assertEquals(0, host.readCalls)
    }

    @Test
    fun theContentIsForwardedToTheHostUnchanged() = runTest {
        val host = FakeWechatFileSystemHost()

        WechatFileSystem(host).writeText(path, "kmp-miniapp-sdk bob72 test")

        assertEquals(path, host.lastPath)
        assertEquals("kmp-miniapp-sdk bob72 test", host.lastWritten)
        assertEquals(1, host.writeCalls)
    }

    @Test
    fun anEmptyStringCanBeWritten() = runTest {
        val host = FakeWechatFileSystemHost()

        WechatFileSystem(host).writeText(path, "")

        assertEquals("", host.lastWritten)
    }

    @Test
    fun aFailedWriteIsAHostFailure() = runTest {
        val host = FakeWechatFileSystemHost().apply { writeFailure = "writeFile:fail invalid path" }

        val failure = assertFailsWith<MiniAppException.HostFailure> {
            WechatFileSystem(host).writeText(path, "value")
        }

        assertEquals("writeFile", failure.metadata["operation"])
    }

    @Test
    fun aHostThatCannotWriteIsAnUnsupportedCapability() = runTest {
        val host = FakeWechatFileSystemHost(writeSupported = false)

        val failure = assertFailsWith<MiniAppException.UnsupportedCapability> {
            WechatFileSystem(host).writeText(path, "value")
        }

        assertEquals("wechat.filesystem-write", failure.capability.value)
        assertEquals(0, host.writeCalls)
    }

    @Test
    fun anExistingFileIsReportedAsPresent() = runTest {
        val host = FakeWechatFileSystemHost().apply { putRawFile(path, "hello") }

        assertTrue(WechatFileSystem(host).exists(path))
    }

    @Test
    fun aMissingFileIsReportedAsAbsent() = runTest {
        val host = FakeWechatFileSystemHost()

        assertFalse(WechatFileSystem(host).exists(path))
    }

    @Test
    fun aFailureThatIsNotAMissingFileIsNotReportedAsAbsent() = runTest {
        // A permission error and a missing file are different answers, and only
        // one of them means the caller may create the file.
        val host = FakeWechatFileSystemHost().apply { accessFailure = "access:fail permission denied" }

        val failure = assertFailsWith<MiniAppException.HostFailure> {
            WechatFileSystem(host).exists(path)
        }

        assertEquals("access", failure.metadata["operation"])
    }

    @Test
    fun aHostThatCannotTestPathsIsAnUnsupportedCapability() = runTest {
        val host = FakeWechatFileSystemHost(accessSupported = false)

        val failure = assertFailsWith<MiniAppException.UnsupportedCapability> {
            WechatFileSystem(host).exists(path)
        }

        assertEquals("wechat.filesystem-access", failure.capability.value)
        assertEquals(0, host.accessCalls)
    }

    @Test
    fun removingAnExistingFileSucceeds() = runTest {
        val host = FakeWechatFileSystemHost().apply { putRawFile(path, "hello") }

        WechatFileSystem(host).remove(path)

        assertFalse(host.contains(path))
        assertEquals(1, host.removeCalls)
    }

    @Test
    fun removingAMissingFileFollowsTheHostContractAndFails() = runTest {
        // WeChat's unlink reports a missing path as a failure, so the SDK reports
        // it rather than inventing an idempotence the host does not offer.
        val host = FakeWechatFileSystemHost()

        val failure = assertFailsWith<MiniAppException.HostFailure> {
            WechatFileSystem(host).remove(path)
        }

        assertEquals("unlink", failure.metadata["operation"])
    }

    @Test
    fun aFailedRemovalIsAHostFailure() = runTest {
        val host = FakeWechatFileSystemHost().apply {
            removeFailure = "unlink:fail operation not permitted"
        }

        val failure = assertFailsWith<MiniAppException.HostFailure> {
            WechatFileSystem(host).remove(path)
        }

        assertEquals("unlink", failure.metadata["operation"])
    }

    @Test
    fun aHostThatCannotRemoveIsAnUnsupportedCapability() = runTest {
        val host = FakeWechatFileSystemHost(removeSupported = false)

        val failure = assertFailsWith<MiniAppException.UnsupportedCapability> {
            WechatFileSystem(host).remove(path)
        }

        assertEquals("wechat.filesystem-remove", failure.capability.value)
        assertEquals(0, host.removeCalls)
    }

    @Test
    fun theSandboxRootIsReadFromTheHost() = runTest {
        assertEquals("/sandbox", WechatFileSystem(FakeWechatFileSystemHost()).userDataPath())
    }

    @Test
    fun aHostWithoutASandboxRootReportsItUnsupported() = runTest {
        val host = FakeWechatFileSystemHost(sandboxPath = null)

        val failure = assertFailsWith<MiniAppException.UnsupportedCapability> {
            WechatFileSystem(host).userDataPath()
        }

        assertEquals("wechat.filesystem-sandbox-path", failure.capability.value)
    }

    @Test
    fun aHostThatCanOnlyWriteStillReportsReadingUnsupported() = runTest {
        // The four operations are independent, so one being present must not make
        // another look available.
        val host = FakeWechatFileSystemHost(readSupported = false, writeSupported = true)
        val fileSystem = WechatFileSystem(host)

        fileSystem.writeText(path, "value")
        assertFailsWith<MiniAppException.UnsupportedCapability> { fileSystem.readText(path) }

        assertEquals(1, host.writeCalls)
        assertEquals(0, host.readCalls)
    }

    @Test
    fun aRepeatedTerminalCallbackCompletesOnlyOnce() = runTest {
        val host = FakeWechatFileSystemHost().apply {
            putRawFile(path, "once")
            completeReadTwice = true
        }

        assertEquals("once", WechatFileSystem(host).readText(path))
    }

    @Test
    fun aCallbackAfterCancellationIsIgnored() = runTest {
        val host = DeferredReadHost()
        val deferred = async(start = CoroutineStart.UNDISPATCHED) {
            WechatFileSystem(host).readText(path)
        }

        deferred.cancelAndJoin()
        host.succeed("late")

        assertTrue(deferred.isCancelled)
    }

    /** A port that stays silent until the test completes it. */
    private class DeferredReadHost : WechatFileSystemHost {
        private var succeedCallback: ((WxReadFileSuccessResult) -> Unit)? = null

        fun succeed(text: String) {
            val callback = requireNotNull(succeedCallback) {
                "The adapter has not started a read yet"
            }
            callback(fakeReadFileSuccess(text))
        }

        override fun isReadSupported(): Boolean = true

        override fun isWriteSupported(): Boolean = true

        override fun isAccessSupported(): Boolean = true

        override fun isRemoveSupported(): Boolean = true

        override fun userDataPath(): String = "/sandbox"

        override fun read(
            filePath: String,
            success: (WxReadFileSuccessResult) -> Unit,
            failure: (WxGeneralCallbackResult) -> Unit,
        ) {
            succeedCallback = success
        }

        override fun write(
            filePath: String,
            data: String,
            success: () -> Unit,
            failure: (WxGeneralCallbackResult) -> Unit,
        ): Unit = error("Not used")

        override fun access(
            path: String,
            success: () -> Unit,
            failure: (WxGeneralCallbackResult) -> Unit,
        ): Unit = error("Not used")

        override fun unlink(
            filePath: String,
            success: () -> Unit,
            failure: (WxGeneralCallbackResult) -> Unit,
        ): Unit = error("Not used")
    }
}
