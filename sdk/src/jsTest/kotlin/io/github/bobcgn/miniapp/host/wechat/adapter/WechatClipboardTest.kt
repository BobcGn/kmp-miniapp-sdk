package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxGetClipboardDataSuccessResult
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatClipboardHost
import io.github.bobcgn.miniapp.host.wechat.testing.fakeGetClipboardDataSuccess
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * How the WeChat adapter turns the host's clipboard callbacks into the SDK
 * behaviour.
 *
 * These are the raw-callback behaviours; `WechatDeviceCapabilityContractTest`
 * states what must hold for the SDK as a whole.
 */
internal class WechatClipboardTest {
    @Test
    fun textTheHostReturnsIsReported() = runTest {
        val host = FakeWechatClipboardHost(clipboardText = "copied text")

        assertEquals("copied text", WechatClipboard(host).readText())
        assertEquals(1, host.readCalls)
    }

    @Test
    fun anEmptyClipboardIsReportedAsAnEmptyString() = runTest {
        val host = FakeWechatClipboardHost(clipboardText = "")

        // An empty clipboard is a value, not a failure, so it must not be turned
        // into an error or a placeholder.
        assertEquals("", WechatClipboard(host).readText())
    }

    @Test
    fun aMissingTextIsAnInvalidResponse() = runTest {
        val host = FakeWechatClipboardHost(clipboardText = null)

        assertFailsWith<MiniAppException.InvalidResponse> { WechatClipboard(host).readText() }
    }

    @Test
    fun aTextThatIsNotAStringIsAnInvalidResponse() = runTest {
        val host = FakeWechatClipboardHost(clipboardText = 7)

        // Silently coercing this to a string would report something the host never
        // said.
        assertFailsWith<MiniAppException.InvalidResponse> { WechatClipboard(host).readText() }
    }

    @Test
    fun aFailedReadIsAHostFailure() = runTest {
        val host = FakeWechatClipboardHost().apply {
            readFailure = "getClipboardData:fail system error"
        }

        val failure = assertFailsWith<MiniAppException.HostFailure> {
            WechatClipboard(host).readText()
        }

        assertEquals("getClipboardData", failure.metadata["operation"])
    }

    @Test
    fun aHostWithoutTheReadApiIsAnUnsupportedCapability() = runTest {
        val host = FakeWechatClipboardHost(readSupported = false)

        val failure = assertFailsWith<MiniAppException.UnsupportedCapability> {
            WechatClipboard(host).readText()
        }

        assertEquals("wechat.clipboard-read", failure.capability.value)
        // Nothing reached the host.
        assertEquals(0, host.readCalls)
    }

    @Test
    fun theTextIsForwardedToTheHostUnchanged() = runTest {
        val host = FakeWechatClipboardHost()

        WechatClipboard(host).writeText("kmp-miniapp-sdk clipboard test")

        assertEquals("kmp-miniapp-sdk clipboard test", host.lastWritten)
        assertEquals(1, host.writeCalls)
    }

    @Test
    fun anEmptyStringCanBeWritten() = runTest {
        val host = FakeWechatClipboardHost()

        WechatClipboard(host).writeText("")

        assertEquals("", host.lastWritten)
    }

    @Test
    fun aFailedWriteIsAHostFailure() = runTest {
        val host = FakeWechatClipboardHost().apply {
            writeFailure = "setClipboardData:fail system error"
        }

        val failure = assertFailsWith<MiniAppException.HostFailure> {
            WechatClipboard(host).writeText("value")
        }

        assertEquals("setClipboardData", failure.metadata["operation"])
    }

    @Test
    fun aHostWithoutTheWriteApiIsAnUnsupportedCapability() = runTest {
        val host = FakeWechatClipboardHost(writeSupported = false)

        val failure = assertFailsWith<MiniAppException.UnsupportedCapability> {
            WechatClipboard(host).writeText("value")
        }

        assertEquals("wechat.clipboard-write", failure.capability.value)
        assertEquals(0, host.writeCalls)
    }

    @Test
    fun aHostThatCanOnlyWriteStillReportsReadingUnsupported() = runTest {
        // The two directions are independent, so one being present must not make
        // the other look available.
        val host = FakeWechatClipboardHost(readSupported = false, writeSupported = true)
        val clipboard = WechatClipboard(host)

        clipboard.writeText("value")
        assertFailsWith<MiniAppException.UnsupportedCapability> { clipboard.readText() }

        assertEquals(1, host.writeCalls)
        assertEquals(0, host.readCalls)
    }

    @Test
    fun aRepeatedTerminalCallbackCompletesOnlyOnce() = runTest {
        val host = FakeWechatClipboardHost(clipboardText = "once").apply {
            completeReadTwice = true
        }

        assertEquals("once", WechatClipboard(host).readText())
    }

    @Test
    fun aCallbackAfterCancellationIsIgnored() = runTest {
        val host = DeferredClipboardHost()
        val deferred = async(start = CoroutineStart.UNDISPATCHED) {
            WechatClipboard(host).readText()
        }

        deferred.cancelAndJoin()
        host.succeed("late")

        assertTrue(deferred.isCancelled)
    }

    /** A port that stays silent until the test completes it. */
    private class DeferredClipboardHost : WechatClipboardHost {
        private var succeedCallback: ((WxGetClipboardDataSuccessResult) -> Unit)? = null

        fun succeed(text: String) {
            val callback = requireNotNull(succeedCallback) {
                "The adapter has not started a read yet"
            }
            callback(fakeGetClipboardDataSuccess(text))
        }

        override fun isReadSupported(): Boolean = true

        override fun isWriteSupported(): Boolean = true

        override fun read(
            success: (WxGetClipboardDataSuccessResult) -> Unit,
            failure: (WxGeneralCallbackResult) -> Unit,
        ) {
            succeedCallback = success
        }

        override fun write(
            data: String,
            success: () -> Unit,
            failure: (WxGeneralCallbackResult) -> Unit,
        ): Unit = error("Not used")
    }
}
