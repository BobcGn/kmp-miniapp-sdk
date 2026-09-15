package io.github.bobcgn.miniapp.host.wechat.interop

import io.github.bobcgn.miniapp.host.wechat.testing.fakeScanCodeSuccess
import kotlin.js.jsTypeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class WxScanCodeTest {
    @Test
    fun theOptionsAlwaysCarryTheCameraChoice() {
        // The caller's answer is always sent, so the host's own default never
        // silently replaces it in one direction only.
        val fromCameraOnly = wxScanCodeOptions(
            onlyFromCamera = true,
            scanCategories = emptyList(),
        )
        val alsoFromImages = wxScanCodeOptions(
            onlyFromCamera = false,
            scanCategories = emptyList(),
        )

        assertEquals(true, fromCameraOnly.onlyFromCamera)
        assertEquals(false, alsoFromImages.onlyFromCamera)
        assertEquals("undefined", jsTypeOf(fromCameraOnly.success))
        assertEquals("undefined", jsTypeOf(fromCameraOnly.fail))
        assertEquals("undefined", jsTypeOf(fromCameraOnly.complete))
    }

    @Test
    fun anEmptyCategoryListLeavesTheCategoryFieldUnset() {
        // Unset is how the host is asked for every category it supports. An empty
        // array would ask it for none, which is a different request.
        val options = wxScanCodeOptions(onlyFromCamera = false, scanCategories = emptyList())

        assertEquals("undefined", jsTypeOf(options.scanType))
    }

    @Test
    fun theCategoriesAreForwardedInOrderAndAsStrings() {
        val options = wxScanCodeOptions(
            onlyFromCamera = false,
            scanCategories = listOf("qrCode", "barCode", "pdf417"),
        )

        val forwarded = options.scanType as Array<*>
        assertEquals(listOf("qrCode", "barCode", "pdf417"), forwarded.toList())
        assertEquals("string", jsTypeOf(forwarded[0]))
    }

    @Test
    fun theOptionsAcceptCallbacks() {
        val options = wxScanCodeOptions(onlyFromCamera = false, scanCategories = emptyList())
        options.success = { result -> result.result; result.errMsg }
        options.fail = { result -> result.errMsg.length }

        assertNotNull(options.success)
        assertNotNull(options.fail)
    }

    @Test
    fun thePresenceGuardAnswersFalseWithoutAWxGlobal() {
        assertFalse(hasWxScanCode())
    }

    @Test
    fun aScannedAnswerIsRead() {
        val result = fakeScanCodeSuccess(
            result = "https://example.com/scanned",
            scanType = "QR_CODE",
            charSet = "utf-8",
            rawData = "raw",
            path = "/tmp/image.png",
        )

        val sample = wxScanSample(result) as WxScanSample.Present
        assertEquals("https://example.com/scanned", sample.text)
        assertEquals("QR_CODE", sample.scanType)
        assertEquals("utf-8", sample.charSet)
        assertEquals("raw", sample.rawData)
        assertEquals("/tmp/image.png", sample.path)
    }

    @Test
    fun anAnswerWithoutTheDescriptiveFieldsIsStillRead() {
        // Only the content is required. WeChat is documented as reporting the
        // others but not as always doing so, so their absence must not fail a scan
        // that produced content.
        val result = fakeScanCodeSuccess(
            result = "payload",
            scanType = null,
            charSet = null,
            rawData = null,
            path = null,
        )

        val sample = wxScanSample(result) as WxScanSample.Present
        assertEquals("payload", sample.text)
        assertNull(sample.scanType)
        assertNull(sample.charSet)
        assertNull(sample.rawData)
        assertNull(sample.path)
    }

    @Test
    fun emptyContentIsAPresentAnswer() {
        // An empty payload is what the host decoded, not a missing answer.
        val sample = wxScanSample(fakeScanCodeSuccess(result = "")) as WxScanSample.Present

        assertEquals("", sample.text)
    }

    @Test
    fun missingContentIsUnreadable() {
        // The content is the point of the call: without it the host did not deliver
        // a scan, and defaulting it would invent a payload the user never scanned.
        assertEquals(WxScanSample.Unreadable, wxScanSample(fakeScanCodeSuccess(result = null)))
    }

    @Test
    fun contentThatIsNotAStringIsUnreadable() {
        assertEquals(WxScanSample.Unreadable, wxScanSample(fakeScanCodeSuccess(result = 7)))
        assertEquals(
            WxScanSample.Unreadable,
            wxScanSample(fakeScanCodeSuccess(result = js("({ decoded: true })"))),
        )
    }

    @Test
    fun aDescriptiveFieldOfTheWrongTypeIsUnreadable() {
        // An absent field is allowed, but one the host reported with the wrong type
        // means it broke its own contract, and dropping it would hide that.
        val wrongTypes = listOf<Any?>(
            7,
            js("({})"),
            js("[]"),
            true,
        )

        wrongTypes.forEach { value ->
            assertEquals(
                WxScanSample.Unreadable,
                wxScanSample(fakeScanCodeSuccess(result = "payload", scanType = value)),
                "scanType of $value",
            )
            assertEquals(
                WxScanSample.Unreadable,
                wxScanSample(fakeScanCodeSuccess(result = "payload", charSet = value)),
                "charSet of $value",
            )
            assertEquals(
                WxScanSample.Unreadable,
                wxScanSample(fakeScanCodeSuccess(result = "payload", rawData = value)),
                "rawData of $value",
            )
            assertEquals(
                WxScanSample.Unreadable,
                wxScanSample(fakeScanCodeSuccess(result = "payload", path = value)),
                "path of $value",
            )
        }
    }
}
