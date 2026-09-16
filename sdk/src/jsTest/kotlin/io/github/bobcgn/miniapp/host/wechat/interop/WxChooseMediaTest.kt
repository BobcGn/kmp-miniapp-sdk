package io.github.bobcgn.miniapp.host.wechat.interop

import io.github.bobcgn.miniapp.host.wechat.testing.fakeChooseMediaEntry
import io.github.bobcgn.miniapp.host.wechat.testing.fakeChooseMediaFiles
import io.github.bobcgn.miniapp.host.wechat.testing.fakeChooseMediaSuccess
import io.github.bobcgn.miniapp.host.wechat.testing.fakeChooseMediaVideoEntry
import kotlin.js.jsTypeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class WxChooseMediaTest {
    @Test
    fun theOptionsCarryWhatTheCallerAskedFor() {
        val options = wxChooseMediaOptions(
            count = 4,
            mediaTypes = listOf("mix"),
            sourceTypes = listOf("album", "camera"),
            maxDurationSeconds = 30,
            sizeTypes = listOf("compressed"),
            camera = "front",
        )

        assertEquals(4, options.count)
        assertEquals(listOf("mix"), (options.mediaType as Array<*>).toList())
        assertEquals(listOf("album", "camera"), (options.sourceType as Array<*>).toList())
        assertEquals(30, options.maxDuration)
        assertEquals(listOf("compressed"), (options.sizeType as Array<*>).toList())
        assertEquals("front", options.camera)
        assertEquals("undefined", jsTypeOf(options.success))
        assertEquals("undefined", jsTypeOf(options.fail))
        assertEquals("undefined", jsTypeOf(options.complete))
    }

    @Test
    fun anUnrequestedOptionIsLeftUnset() {
        // Leaving a field unset is how the host's own default applies. Sending an
        // empty array instead would ask for something the caller never meant.
        val options = wxChooseMediaOptions(
            count = 1,
            mediaTypes = listOf("image"),
            sourceTypes = emptyList(),
            maxDurationSeconds = null,
            sizeTypes = emptyList(),
            camera = null,
        )

        assertEquals(1, options.count)
        assertEquals(listOf("image"), (options.mediaType as Array<*>).toList())
        assertEquals("undefined", jsTypeOf(options.sourceType))
        assertEquals("undefined", jsTypeOf(options.maxDuration))
        assertEquals("undefined", jsTypeOf(options.sizeType))
        assertEquals("undefined", jsTypeOf(options.camera))
    }

    @Test
    fun theMediaTypesAreForwardedInOrderAndAsStrings() {
        val options = wxChooseMediaOptions(
            count = 2,
            mediaTypes = listOf("image", "video"),
            sourceTypes = emptyList(),
            maxDurationSeconds = null,
            sizeTypes = emptyList(),
            camera = null,
        )

        val forwarded = options.mediaType as Array<*>
        assertEquals(listOf("image", "video"), forwarded.toList())
        assertEquals("string", jsTypeOf(forwarded[0]))
    }

    @Test
    fun theOptionsAcceptCallbacks() {
        val options = wxChooseMediaOptions(
            count = 1,
            mediaTypes = listOf("image"),
            sourceTypes = emptyList(),
            maxDurationSeconds = null,
            sizeTypes = emptyList(),
            camera = null,
        )
        options.success = { result -> result.tempFiles; result.errMsg }
        options.fail = { result -> result.errMsg.length }

        assertNotNull(options.success)
        assertNotNull(options.fail)
    }

    @Test
    fun thePresenceGuardAnswersFalseWithoutAWxGlobal() {
        assertFalse(hasWxChooseMedia())
    }

    @Test
    fun anImageSelectionIsRead() {
        val result = fakeChooseMediaSuccess(
            fakeChooseMediaFiles(fakeChooseMediaEntry(tempFilePath = "/tmp/a.png", size = 2048)),
        )

        val selection = wxMediaSelection(result) as WxMediaSelection.Present
        assertEquals(1, selection.files.size)
        val file = selection.files[0]
        assertEquals("/tmp/a.png", file.tempFilePath)
        assertEquals(2048L, file.sizeBytes)
        assertEquals("image", file.fileType)
        // WeChat reports no duration, dimensions, or thumbnail for an image, so
        // those stay absent rather than becoming zero.
        assertNull(file.durationSeconds)
        assertNull(file.width)
        assertNull(file.height)
        assertNull(file.thumbTempFilePath)
    }

    @Test
    fun aVideoSelectionIsRead() {
        val result = fakeChooseMediaSuccess(
            fakeChooseMediaFiles(
                fakeChooseMediaVideoEntry(
                    tempFilePath = "/tmp/v.mp4",
                    size = 1048576,
                    duration = 12.5,
                    width = 1920,
                    height = 1080,
                    thumbTempFilePath = "/tmp/v-thumb.jpg",
                ),
            ),
        )

        val file = (wxMediaSelection(result) as WxMediaSelection.Present).files[0]
        assertEquals("/tmp/v.mp4", file.tempFilePath)
        assertEquals(1048576L, file.sizeBytes)
        assertEquals("video", file.fileType)
        assertEquals(12.5, file.durationSeconds)
        assertEquals(1920.0, file.width)
        assertEquals(1080.0, file.height)
        assertEquals("/tmp/v-thumb.jpg", file.thumbTempFilePath)
    }

    @Test
    fun aSelectionOfSeveralFilesIsReadInOrder() {
        val result = fakeChooseMediaSuccess(
            fakeChooseMediaFiles(
                fakeChooseMediaEntry(tempFilePath = "/tmp/one.png"),
                fakeChooseMediaVideoEntry(tempFilePath = "/tmp/two.mp4"),
                fakeChooseMediaEntry(tempFilePath = "/tmp/three.png"),
            ),
        )

        val files = (wxMediaSelection(result) as WxMediaSelection.Present).files
        assertEquals(
            listOf("/tmp/one.png", "/tmp/two.mp4", "/tmp/three.png"),
            files.map { it.tempFilePath },
        )
    }

    @Test
    fun anEmptySelectionReachesTheAdapterForSemanticValidation() {
        val selection = wxMediaSelection(fakeChooseMediaSuccess(fakeChooseMediaFiles()))

        assertEquals(WxMediaSelection.Present(emptyList()), selection)
    }

    @Test
    fun aSelectionThatIsNotAnArrayIsUnreadable() {
        assertEquals(
            WxMediaSelection.Unreadable,
            wxMediaSelection(fakeChooseMediaSuccess(null)),
        )
        assertEquals(
            WxMediaSelection.Unreadable,
            wxMediaSelection(fakeChooseMediaSuccess("/tmp/a.png")),
        )
        assertEquals(
            WxMediaSelection.Unreadable,
            wxMediaSelection(fakeChooseMediaSuccess(js("({ 0: '/tmp/a.png' })"))),
        )
    }

    @Test
    fun aMissingTempFilePathIsUnreadable() {
        // A path is not a payload: an empty one names no file the caller can use.
        listOf<Any?>(null, 7, "", js("({})")).forEach { path ->
            assertEquals(
                WxMediaSelection.Unreadable,
                wxMediaSelection(fakeChooseMediaSuccess(fakeChooseMediaFiles(fakeChooseMediaEntry(tempFilePath = path)))),
                "tempFilePath of $path",
            )
        }
    }

    @Test
    fun aFileSizeThatIsNotAWholeNonNegativeNumberIsUnreadable() {
        listOf<Any?>(
            -1,
            1.5,
            "4096",
            null,
            js("({})"),
            Double.NaN,
            Double.POSITIVE_INFINITY,
            9_007_199_254_740_992.0,
        )
            .forEach { size ->
                assertEquals(
                    WxMediaSelection.Unreadable,
                    wxMediaSelection(fakeChooseMediaSuccess(fakeChooseMediaFiles(fakeChooseMediaEntry(size = size)))),
                    "size of $size",
                )
            }
    }

    @Test
    fun aZeroSizeIsAccepted() {
        // Zero is a byte count the host can legitimately report, so it is passed
        // through rather than treated as a missing value.
        val file = (
            wxMediaSelection(
                fakeChooseMediaSuccess(fakeChooseMediaFiles(fakeChooseMediaEntry(size = 0))),
            ) as WxMediaSelection.Present
            ).files[0]

        assertEquals(0L, file.sizeBytes)
    }

    @Test
    fun aTextFieldOfTheWrongTypeIsUnreadable() {
        // An absent field is allowed, but one the host reported with the wrong type
        // means it broke its own contract, and dropping the value would hide that. A
        // number is not text, so it is wrong for these two.
        val wrongTypes = listOf<Any?>(null, "", 7, js("({})"), js("[]"), true)

        wrongTypes.forEach { value ->
            assertEquals(
                WxMediaSelection.Unreadable,
                wxMediaSelection(fakeChooseMediaSuccess(fakeChooseMediaFiles(fakeChooseMediaEntry(fileType = value)))),
                "fileType of $value",
            )
            if (value != null) {
                assertEquals(
                    WxMediaSelection.Unreadable,
                    wxMediaSelection(
                        fakeChooseMediaSuccess(fakeChooseMediaFiles(fakeChooseMediaEntry(thumbTempFilePath = value))),
                    ),
                    "thumbTempFilePath of $value",
                )
            }
        }
    }

    @Test
    fun aNumberFieldOfTheWrongTypeIsUnreadable() {
        // The mirror of the check above: text is not a number.
        listOf<Any?>("12", js("({})"), js("[]"), true).forEach { value ->
            assertEquals(
                WxMediaSelection.Unreadable,
                wxMediaSelection(fakeChooseMediaSuccess(fakeChooseMediaFiles(fakeChooseMediaEntry(duration = value)))),
                "duration of $value",
            )
            assertEquals(
                WxMediaSelection.Unreadable,
                wxMediaSelection(fakeChooseMediaSuccess(fakeChooseMediaFiles(fakeChooseMediaEntry(width = value)))),
                "width of $value",
            )
            assertEquals(
                WxMediaSelection.Unreadable,
                wxMediaSelection(fakeChooseMediaSuccess(fakeChooseMediaFiles(fakeChooseMediaEntry(height = value)))),
                "height of $value",
            )
        }
    }

    @Test
    fun aDescriptiveNumberThatCannotDescribeMediaIsUnreadable() {
        listOf<Any?>(Double.NaN, Double.POSITIVE_INFINITY, -1, -0.5).forEach { value ->
            assertEquals(
                WxMediaSelection.Unreadable,
                wxMediaSelection(fakeChooseMediaSuccess(fakeChooseMediaFiles(fakeChooseMediaEntry(duration = value)))),
                "duration of $value",
            )
            assertEquals(
                WxMediaSelection.Unreadable,
                wxMediaSelection(fakeChooseMediaSuccess(fakeChooseMediaFiles(fakeChooseMediaEntry(width = value)))),
                "width of $value",
            )
        }
    }

    @Test
    fun anEntryThatIsNotAnObjectIsUnreadable() {
        listOf<Any?>("/tmp/a.png", 7, true, null).forEach { entry ->
            assertEquals(
                WxMediaSelection.Unreadable,
                wxMediaSelection(fakeChooseMediaSuccess(fakeChooseMediaFiles(entry))),
                "entry $entry",
            )
        }
    }

    @Test
    fun oneUnreadableEntryMakesTheWholeSelectionUnreadable() {
        // A partial list would hide that the host answered something it cannot
        // mean, so nothing is reported for the entries that were fine.
        val result = fakeChooseMediaSuccess(
            fakeChooseMediaFiles(
                fakeChooseMediaEntry(tempFilePath = "/tmp/ok.png"),
                fakeChooseMediaEntry(tempFilePath = null),
            ),
        )

        assertEquals(WxMediaSelection.Unreadable, wxMediaSelection(result))
    }
}
