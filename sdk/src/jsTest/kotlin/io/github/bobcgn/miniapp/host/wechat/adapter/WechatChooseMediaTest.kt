package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.WeChatCameraPosition
import io.github.bobcgn.miniapp.host.wechat.WeChatMediaFileType
import io.github.bobcgn.miniapp.host.wechat.WeChatMediaRequest
import io.github.bobcgn.miniapp.host.wechat.WeChatMediaSizeType
import io.github.bobcgn.miniapp.host.wechat.WeChatMediaSource
import io.github.bobcgn.miniapp.host.wechat.WeChatMediaType
import io.github.bobcgn.miniapp.host.wechat.interop.WxChooseMediaSuccessResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatChooseMediaHost
import io.github.bobcgn.miniapp.host.wechat.testing.fakeChooseMediaEntry
import io.github.bobcgn.miniapp.host.wechat.testing.fakeChooseMediaFiles
import io.github.bobcgn.miniapp.host.wechat.testing.fakeChooseMediaSuccess
import io.github.bobcgn.miniapp.host.wechat.testing.fakeChooseMediaVideoEntry
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * How the WeChat adapter turns the host's media callbacks into SDK behaviour.
 *
 * These are the raw-callback behaviours; `WechatChooseMediaContractTest` states
 * what must hold for the SDK as a whole.
 */
internal class WechatChooseMediaTest {
    @Test
    fun aDefaultRequestIsForwardedAsTheCallerDescribed() = runTest {
        val host = FakeWechatChooseMediaHost()

        WechatChooseMedia(host).choose(request(mediaType = listOf(WeChatMediaType.IMAGE)))

        val forwarded = requireNotNull(host.lastRequest)
        assertEquals(listOf(WeChatMediaType.IMAGE), forwarded.mediaType)
        assertEquals(1, forwarded.count)
        // Everything the caller did not ask for stays unasked, so the host's own
        // default applies rather than one this SDK invented.
        assertEquals(emptyList(), forwarded.sourceType)
        assertEquals(emptyList(), forwarded.sizeType)
        assertNull(forwarded.maxDurationSeconds)
        assertNull(forwarded.camera)
    }

    @Test
    fun everySupportedOptionReachesTheHost() = runTest {
        val host = FakeWechatChooseMediaHost()

        WechatChooseMedia(host).choose(
            WeChatMediaRequest(
                mediaType = listOf(WeChatMediaType.MIX),
                count = 3,
                sourceType = listOf(WeChatMediaSource.ALBUM, WeChatMediaSource.CAMERA),
                maxDurationSeconds = 30,
                sizeType = listOf(WeChatMediaSizeType.COMPRESSED),
                camera = WeChatCameraPosition.FRONT,
            ),
        )

        val forwarded = requireNotNull(host.lastRequest)
        assertEquals(listOf(WeChatMediaType.MIX), forwarded.mediaType)
        assertEquals(3, forwarded.count)
        assertEquals(
            listOf(WeChatMediaSource.ALBUM, WeChatMediaSource.CAMERA),
            forwarded.sourceType,
        )
        assertEquals(30, forwarded.maxDurationSeconds)
        assertEquals(listOf(WeChatMediaSizeType.COMPRESSED), forwarded.sizeType)
        assertEquals(WeChatCameraPosition.FRONT, forwarded.camera)
    }

    @Test
    fun duplicateValuesAreCollapsedWithTheCallersOrderKept() {
        // Duplicates are the caller's problem, not the host's, and they are collapsed
        // here rather than being sent on.
        val request = WeChatMediaRequest(
            mediaType = listOf(
                WeChatMediaType.VIDEO,
                WeChatMediaType.IMAGE,
                WeChatMediaType.VIDEO,
            ),
            sourceType = listOf(WeChatMediaSource.CAMERA, WeChatMediaSource.CAMERA),
            sizeType = listOf(
                WeChatMediaSizeType.ORIGINAL,
                WeChatMediaSizeType.ORIGINAL,
            ),
        )

        assertEquals(listOf(WeChatMediaType.VIDEO, WeChatMediaType.IMAGE), request.mediaType)
        assertEquals(listOf(WeChatMediaSource.CAMERA), request.sourceType)
        assertEquals(listOf(WeChatMediaSizeType.ORIGINAL), request.sizeType)
    }

    @Test
    fun aRequestWithNoMediaTypeIsRejected() {
        // WeChat's own schema marks the option required, so the SDK refuses rather
        // than sending something the host would read as unset.
        val failure = assertFailsWith<IllegalArgumentException> {
            WeChatMediaRequest(mediaType = emptyList())
        }

        assertTrue(failure.message!!.contains("at least one media type"))
    }

    @Test
    fun aCountBelowOneIsRejected() {
        listOf(0, -1).forEach { count ->
            assertFailsWith<IllegalArgumentException>("count of $count") {
                WeChatMediaRequest(mediaType = listOf(WeChatMediaType.IMAGE), count = count)
            }
        }
    }

    @Test
    fun aCountAboveTheHostsOwnLimitIsLeftToTheHost() {
        // The host's own maximum depends on its base library, so the SDK does not
        // invent one: it sends what the caller asked for and reports what comes back.
        val request = WeChatMediaRequest(mediaType = listOf(WeChatMediaType.IMAGE), count = 50)

        assertEquals(50, request.count)
    }

    @Test
    fun aDurationOutsideTheDocumentedRangeIsRejected() {
        // WeChat documents 3 to 60 seconds. The SDK refuses what the host could not
        // accept instead of quietly correcting it.
        listOf(2, 61, 0, -5).forEach { duration ->
            assertFailsWith<IllegalArgumentException>("duration of $duration") {
                WeChatMediaRequest(
                    mediaType = listOf(WeChatMediaType.VIDEO),
                    maxDurationSeconds = duration,
                )
            }
        }
    }

    @Test
    fun theDocumentedDurationBoundsAreAccepted() {
        listOf(3, 60).forEach { duration ->
            val request = WeChatMediaRequest(
                mediaType = listOf(WeChatMediaType.VIDEO),
                maxDurationSeconds = duration,
            )

            assertEquals(duration, request.maxDurationSeconds)
        }
    }

    @Test
    fun anImageSelectionIsReported() = runTest {
        val host = FakeWechatChooseMediaHost(
            tempFiles = fakeChooseMediaFiles(
                fakeChooseMediaEntry(tempFilePath = "/tmp/a.png", size = 2048),
            ),
        )

        val files = WechatChooseMedia(host).choose(defaultRequest())

        assertEquals(1, files.size)
        assertEquals("/tmp/a.png", files[0].tempFilePath)
        assertEquals(2048L, files[0].sizeBytes)
        assertEquals(WeChatMediaFileType.IMAGE, files[0].fileType)
        assertEquals(1, host.calls)
    }

    @Test
    fun aVideoSelectionReportsItsMetadata() = runTest {
        val host = FakeWechatChooseMediaHost(
            tempFiles = fakeChooseMediaFiles(
                fakeChooseMediaVideoEntry(
                    tempFilePath = "/tmp/v.mp4",
                    duration = 12.5,
                    width = 1920,
                    height = 1080,
                    thumbTempFilePath = "/tmp/v-thumb.jpg",
                ),
            ),
        )

        val file = WechatChooseMedia(host).choose(defaultRequest())[0]

        assertEquals(WeChatMediaFileType.VIDEO, file.fileType)
        assertEquals(12.5, file.durationSeconds)
        assertEquals(1920.0, file.width)
        assertEquals(1080.0, file.height)
        assertEquals("/tmp/v-thumb.jpg", file.thumbTempFilePath)
    }

    @Test
    fun anImageWithoutVideoMetadataKeepsThoseFieldsAbsent() = runTest {
        // Fabricating zeros here would tell a caller that an image is zero seconds
        // long and has no pixels, which is not what the host said.
        val host = FakeWechatChooseMediaHost(
            tempFiles = fakeChooseMediaFiles(fakeChooseMediaEntry()),
        )

        val file = WechatChooseMedia(host).choose(defaultRequest())[0]

        assertNull(file.durationSeconds)
        assertNull(file.width)
        assertNull(file.height)
        assertNull(file.thumbTempFilePath)
    }

    @Test
    fun anEmptySuccessfulSelectionIsAnInvalidResponse() = runTest {
        // A successful picker interaction must identify at least one selected file.
        val host = FakeWechatChooseMediaHost(tempFiles = fakeChooseMediaFiles())

        assertFailsWith<MiniAppException.InvalidResponse> {
            WechatChooseMedia(host).choose(defaultRequest())
        }
    }

    @Test
    fun anUnknownFileKindIsReportedWithoutFailing() = runTest {
        // A host may return a kind this SDK has never seen. That is a fact about the
        // host, not a failed selection, and the host's own name is kept.
        val host = FakeWechatChooseMediaHost(
            tempFiles = fakeChooseMediaFiles(
                fakeChooseMediaEntry(fileType = "livePhoto"),
            ),
        )

        val file = WechatChooseMedia(host).choose(defaultRequest())[0]

        assertNull(file.fileType)
        assertEquals("livePhoto", file.hostFileType)
    }

    @Test
    fun anAnswerWithoutAFileKindIsAnInvalidResponse() = runTest {
        val host = FakeWechatChooseMediaHost(
            tempFiles = fakeChooseMediaFiles(fakeChooseMediaEntry(fileType = null)),
        )

        assertFailsWith<MiniAppException.InvalidResponse> {
            WechatChooseMedia(host).choose(defaultRequest())
        }
    }

    @Test
    fun anUnreadableSelectionIsAnInvalidResponse() = runTest {
        val shapes = listOf<Any?>(
            "/tmp/a.png",
            null,
            fakeChooseMediaFiles(fakeChooseMediaEntry(tempFilePath = null)),
            fakeChooseMediaFiles(fakeChooseMediaEntry(size = -1)),
            fakeChooseMediaFiles("not an entry"),
        )

        shapes.forEach { tempFiles ->
            val host = FakeWechatChooseMediaHost(tempFiles = tempFiles)

            assertFailsWith<MiniAppException.InvalidResponse>("tempFiles of $tempFiles") {
                WechatChooseMedia(host).choose(defaultRequest())
            }
        }
    }

    @Test
    fun anInterruptedSelectionIsNotAttributedToAUser() = runTest {
        // The installed Developer Tools base library's simulated picker reports
        // this exact signal. It has no structured cause, so the SDK does not infer
        // user intent or a permission result from it.
        val host = FakeWechatChooseMediaHost().apply { failureMessage = "chooseMedia:cancel" }

        val failure: MiniAppException = assertFailsWith<MiniAppException.HostInteractionInterrupted> {
            WechatChooseMedia(host).choose(defaultRequest())
        }

        val interrupted = assertIs<MiniAppException.HostInteractionInterrupted>(failure)
        assertEquals("wechat", interrupted.host)
        assertEquals("chooseMedia", interrupted.operation)
        assertEquals("chooseMedia:cancel", interrupted.hostMessage)
    }

    @Test
    fun theRealDeviceDismissalSignalIsAnIndeterminateInterruption() = runTest {
        val host = FakeWechatChooseMediaHost().apply { failureMessage = "chooseMedia:fail cancel" }

        val interrupted = assertFailsWith<MiniAppException.HostInteractionInterrupted> {
            WechatChooseMedia(host).choose(defaultRequest())
        }

        assertEquals("chooseMedia:fail cancel", interrupted.hostMessage)
    }

    @Test
    fun aFailureThatMerelyMentionsCancellingIsAHostFailure() = runTest {
        // Only the exact messages are an interruption. A looser match would report a
        // real failure as something the user chose, which tells a consumer to stop
        // retrying a call that never opened an interface.
        val nearMisses = listOf(
            "chooseMedia:fail user cancel",
            "chooseMedia:fail cancel by system",
            "chooseMedia:cancelX",
            "ChooseMedia:cancel",
            "choosemedia:cancel",
            "chooseMedia:fail",
            "chooseMedia:fail canceled",
            "chooseMedia:fail auth deny",
            "chooseMedia:fail system error",
            "cancel",
            "",
        )

        nearMisses.forEach { message ->
            val host = FakeWechatChooseMediaHost().apply { failureMessage = message }

            val failure = assertFailsWith<MiniAppException.HostFailure>("'$message'") {
                WechatChooseMedia(host).choose(defaultRequest())
            }

            assertEquals("chooseMedia", failure.metadata["operation"])
            assertEquals(message, failure.hostMessage)
        }
    }

    @Test
    fun aHostWithoutTheMediaApiIsAnUnsupportedCapability() = runTest {
        val host = FakeWechatChooseMediaHost(supported = false)

        val failure = assertFailsWith<MiniAppException.UnsupportedCapability> {
            WechatChooseMedia(host).choose(defaultRequest())
        }

        assertEquals("wechat.choose-media", failure.capability.value)
        // The host was never asked, so no interface was opened.
        assertEquals(0, host.calls)
    }

    @Test
    fun aRepeatedTerminalCallbackCompletesOnlyOnce() = runTest {
        val host = FakeWechatChooseMediaHost().apply { completeTwice = true }

        assertEquals(1, WechatChooseMedia(host).choose(defaultRequest()).size)
    }

    @Test
    fun aCallbackAfterCancellationIsIgnored() = runTest {
        val host = DeferredChooseMediaHost()
        val deferred = async(start = CoroutineStart.UNDISPATCHED) {
            WechatChooseMedia(host).choose(defaultRequest())
        }

        deferred.cancelAndJoin()
        host.succeed()

        assertTrue(deferred.isCancelled)
    }

    @Test
    fun aSecondSelectionAsksTheHostAgain() = runTest {
        // The returned files are not kept anywhere between calls.
        val host = FakeWechatChooseMediaHost(
            tempFiles = fakeChooseMediaFiles(fakeChooseMediaEntry(tempFilePath = "/tmp/first.png")),
        )
        val adapter = WechatChooseMedia(host)

        assertEquals("/tmp/first.png", adapter.choose(defaultRequest())[0].tempFilePath)

        host.tempFiles = fakeChooseMediaFiles(
            fakeChooseMediaEntry(tempFilePath = "/tmp/second.png"),
        )
        assertEquals("/tmp/second.png", adapter.choose(defaultRequest())[0].tempFilePath)
        assertEquals(2, host.calls)
    }

    private fun defaultRequest(): WeChatMediaRequest =
        request(mediaType = listOf(WeChatMediaType.IMAGE))

    private fun request(mediaType: List<WeChatMediaType>): WeChatMediaRequest =
        WeChatMediaRequest(mediaType = mediaType)

    /** A port that stays silent until the test completes it. */
    private class DeferredChooseMediaHost : WechatChooseMediaHost {
        private var succeedCallback: ((WxChooseMediaSuccessResult) -> Unit)? = null

        fun succeed() {
            val callback = requireNotNull(succeedCallback) {
                "The adapter has not started a selection yet"
            }
            callback(fakeChooseMediaSuccess(fakeChooseMediaFiles(fakeChooseMediaEntry())))
        }

        override fun isSupported(): Boolean = true

        override fun choose(
            request: WeChatMediaRequest,
            success: (WxChooseMediaSuccessResult) -> Unit,
            failure: (WxGeneralCallbackResult) -> Unit,
        ) {
            succeedCallback = success
        }
    }
}
