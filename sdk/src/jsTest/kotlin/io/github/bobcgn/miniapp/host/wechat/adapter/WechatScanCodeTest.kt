package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.WeChatScanCategory
import io.github.bobcgn.miniapp.host.wechat.WeChatScanFormat
import io.github.bobcgn.miniapp.host.wechat.WeChatScanRequest
import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxScanCodeSuccessResult
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatScanCodeHost
import io.github.bobcgn.miniapp.host.wechat.testing.fakeScanCodeSuccess
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * How the WeChat adapter turns the host's scan callbacks into SDK behaviour.
 *
 * These are the raw-callback behaviours; `WechatScanCodeContractTest` states what
 * must hold for the SDK as a whole.
 */
internal class WechatScanCodeTest {
    @Test
    fun aScannedAnswerIsReported() = runTest {
        val host = FakeWechatScanCodeHost(
            text = "https://example.com/scanned",
            scanType = "EAN_13",
            charSet = "utf-8",
            rawData = "raw",
            path = "/tmp/image.png",
        )

        val result = WechatScanCode(host).scan()

        assertEquals("https://example.com/scanned", result.text)
        assertEquals(WeChatScanFormat.EAN_13, result.format)
        assertEquals("EAN_13", result.scanType)
        assertEquals("utf-8", result.charSet)
        assertEquals("raw", result.rawData)
        assertEquals("/tmp/image.png", result.path)
        assertEquals(1, host.calls)
    }

    @Test
    fun theDefaultRequestAsksForEveryCategoryAndNotOnlyTheCamera() = runTest {
        val host = FakeWechatScanCodeHost()

        WechatScanCode(host).scan()

        // An empty category list is "no restriction", not "none", and the camera
        // choice is the SDK's own default rather than the host's.
        assertEquals(false, host.lastOnlyFromCamera)
        assertEquals(emptyList(), host.lastCategories)
    }

    @Test
    fun theRequestReachesTheHostInOrder() = runTest {
        val host = FakeWechatScanCodeHost()
        val request = WeChatScanRequest(
            onlyFromCamera = true,
            allowedCategories = listOf(
                WeChatScanCategory.QR_CODE,
                WeChatScanCategory.BAR_CODE,
                WeChatScanCategory.PDF_417,
            ),
        )

        WechatScanCode(host).scan(request)

        assertEquals(true, host.lastOnlyFromCamera)
        assertEquals(listOf("qrCode", "barCode", "pdf417"), host.lastCategories)
    }

    @Test
    fun aRepeatedCategoryIsSentOnce() = runTest {
        // Duplicates are the caller's, not the host's, problem: they are collapsed
        // here so the host is never handed a request the caller did not mean.
        val host = FakeWechatScanCodeHost()
        val request = WeChatScanRequest(
            allowedCategories = listOf(
                WeChatScanCategory.QR_CODE,
                WeChatScanCategory.BAR_CODE,
                WeChatScanCategory.QR_CODE,
            ),
        )

        WechatScanCode(host).scan(request)

        assertEquals(listOf("qrCode", "barCode"), host.lastCategories)
    }

    @Test
    fun anUnknownFormatIsReportedWithoutFailingTheScan() = runTest {
        // A host may decode a format this SDK has never seen. That is a fact about
        // the host, not a failed scan, and the host's own name is kept.
        val host = FakeWechatScanCodeHost(text = "payload", scanType = "SOMETHING_NEW")

        val result = WechatScanCode(host).scan()

        assertEquals("payload", result.text)
        assertNull(result.format)
        assertEquals("SOMETHING_NEW", result.scanType)
    }

    @Test
    fun anAnswerWithoutAFormatIsReportedAsSuch() = runTest {
        val host = FakeWechatScanCodeHost(text = "payload", scanType = null)

        val result = WechatScanCode(host).scan()

        // No name at all is different from a name the SDK does not know, and both
        // are distinguishable because one carries the host's string and one does not.
        assertNull(result.format)
        assertNull(result.scanType)
    }

    @Test
    fun theDescriptiveFieldsStayAbsentWhenTheHostOmitsThem() = runTest {
        val host = FakeWechatScanCodeHost(
            text = "payload",
            scanType = null,
            charSet = null,
            rawData = null,
            path = null,
        )

        val result = WechatScanCode(host).scan()

        assertNull(result.charSet)
        assertNull(result.rawData)
        assertNull(result.path)
    }

    @Test
    fun anUnreadableAnswerIsAnInvalidResponse() = runTest {
        val host = FakeWechatScanCodeHost(text = null)

        assertFailsWith<MiniAppException.InvalidResponse> { WechatScanCode(host).scan() }
    }

    @Test
    fun theObservedCancelSignalIsAnIndeterminateInterruption() = runTest {
        // The form the base library shipped with WeChat Developer Tools reports.
        val host = FakeWechatScanCodeHost().apply { failureMessage = "scanCode:cancel" }

        val failure = assertFailsWith<MiniAppException.HostInteractionInterrupted> {
            WechatScanCode(host).scan()
        }
        assertEquals("wechat", failure.host)
        assertEquals("scanCode", failure.operation)
        assertEquals("scanCode:cancel", failure.hostMessage)
    }

    @Test
    fun theHostsOtherCancelFormIsAnIndeterminateInterruption() = runTest {
        // The host's general convention for a dismissed interface.
        val host = FakeWechatScanCodeHost().apply { failureMessage = "scanCode:fail cancel" }

        assertFailsWith<MiniAppException.HostInteractionInterrupted> {
            WechatScanCode(host).scan()
        }
    }

    @Test
    fun aFailureThatMerelyMentionsCancellingIsAHostFailure() = runTest {
        // Only the exact dismissal messages are a user decision. A looser match
        // would let a real failure be reported as something the user chose, which
        // tells a consumer to stop retrying a call that never opened an interface.
        val nearMisses = listOf(
            "scanCode:fail user cancel",
            "scanCode:fail cancel by system",
            "scanCode:cancelX",
            "ScanCode:cancel",
            "scancode:cancel",
            "scanCode:fail",
            "scanCode:fail canceled",
            "scanCode:fail system error",
            "cancel",
            "",
        )

        nearMisses.forEach { message ->
            val host = FakeWechatScanCodeHost().apply { failureMessage = message }

            val failure = assertFailsWith<MiniAppException.HostFailure>("'$message'") {
                WechatScanCode(host).scan()
            }

            assertEquals("scanCode", failure.metadata["operation"])
            assertEquals(message, failure.hostMessage)
        }
    }

    @Test
    fun aHostWithoutTheScanApiIsAnUnsupportedCapability() = runTest {
        val host = FakeWechatScanCodeHost(supported = false)

        val failure = assertFailsWith<MiniAppException.UnsupportedCapability> {
            WechatScanCode(host).scan()
        }

        assertEquals("wechat.scan-code", failure.capability.value)
        // The host was never asked, so no interface was opened.
        assertEquals(0, host.calls)
    }

    @Test
    fun aRepeatedTerminalCallbackCompletesOnlyOnce() = runTest {
        val host = FakeWechatScanCodeHost(text = "once").apply { completeTwice = true }

        assertEquals("once", WechatScanCode(host).scan().text)
    }

    @Test
    fun aCallbackAfterCancellationIsIgnored() = runTest {
        val host = DeferredScanHost()
        val deferred = async(start = CoroutineStart.UNDISPATCHED) {
            WechatScanCode(host).scan()
        }

        deferred.cancelAndJoin()
        host.succeed("late")

        assertTrue(deferred.isCancelled)
    }

    @Test
    fun aSecondScanAsksTheHostAgainRatherThanReplayingTheFirst() = runTest {
        // The scanned content is not kept anywhere between calls.
        val host = FakeWechatScanCodeHost(text = "first")
        val adapter = WechatScanCode(host)

        assertEquals("first", adapter.scan().text)

        host.text = "second"
        assertEquals("second", adapter.scan().text)
        assertEquals(2, host.calls)
    }

    /** A port that stays silent until the test completes it. */
    private class DeferredScanHost : WechatScanCodeHost {
        private var succeedCallback: ((WxScanCodeSuccessResult) -> Unit)? = null

        fun succeed(text: String) {
            val callback = requireNotNull(succeedCallback) {
                "The adapter has not started a scan yet"
            }
            callback(fakeScanCodeSuccess(result = text))
        }

        override fun isSupported(): Boolean = true

        override fun scan(
            onlyFromCamera: Boolean,
            scanCategories: List<String>,
            success: (WxScanCodeSuccessResult) -> Unit,
            failure: (WxGeneralCallbackResult) -> Unit,
        ) {
            succeedCallback = success
        }
    }
}
