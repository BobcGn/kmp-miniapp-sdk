package io.github.bobcgn.miniapp.host.wechat.interop

import io.github.bobcgn.miniapp.host.wechat.testing.FakeTransferTask
import io.github.bobcgn.miniapp.host.wechat.testing.fakeTransferProgress
import kotlin.js.jsTypeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WxTransferTest {
    @Test
    fun theUploadOptionsCarryWhatTheCallerAskedFor() {
        val options = wxUploadFileOptions(
            url = "https://example.com/upload",
            filePath = "/sandbox/file.txt",
            name = "file",
            header = js("({ 'X-Test': 'yes' })"),
            formData = js("({ field: 'value' })"),
            timeoutMillis = 5000,
        )

        assertEquals("https://example.com/upload", options.url)
        assertEquals("/sandbox/file.txt", options.filePath)
        assertEquals("file", options.name)
        assertNotNull(options.header)
        assertNotNull(options.formData)
        assertEquals(5000, options.timeout)
        assertEquals("undefined", jsTypeOf(options.success))
        assertEquals("undefined", jsTypeOf(options.fail))
    }

    @Test
    fun anUnrequestedUploadFieldIsLeftUnset() {
        // Leaving a field unset is how the host's own default applies.
        val options = wxUploadFileOptions(
            url = "https://example.com/upload",
            filePath = "/sandbox/file.txt",
            name = "file",
            header = null,
            formData = null,
            timeoutMillis = null,
        )

        assertEquals("undefined", jsTypeOf(options.header))
        assertEquals("undefined", jsTypeOf(options.formData))
        assertEquals("undefined", jsTypeOf(options.timeout))
    }

    @Test
    fun theDownloadOptionsCarryWhatTheCallerAskedFor() {
        val options = wxDownloadFileOptions(
            url = "https://example.com/file.bin",
            header = js("({ 'X-Test': 'yes' })"),
            timeoutMillis = 5000,
            targetPath = "/sandbox/target.bin",
        )

        assertEquals("https://example.com/file.bin", options.url)
        assertNotNull(options.header)
        assertEquals(5000, options.timeout)
        assertEquals("/sandbox/target.bin", options.filePath)
    }

    @Test
    fun aDownloadWithoutATargetLeavesThePathUnset() {
        val options = wxDownloadFileOptions(
            url = "https://example.com/file.bin",
            header = null,
            timeoutMillis = null,
            targetPath = null,
        )

        assertEquals("undefined", jsTypeOf(options.filePath))
        assertEquals("undefined", jsTypeOf(options.header))
        assertEquals("undefined", jsTypeOf(options.timeout))
    }

    @Test
    fun thePresenceGuardsAnswerFalseWithoutAWxGlobal() {
        assertFalse(hasWxUploadFile())
        assertFalse(hasWxDownloadFile())
    }

    @Test
    fun aUsableStatusCodeIsRead() {
        listOf<Any?>(200, 404, 500, 100, 599).forEach { value ->
            assertEquals(value, wxTransferStatusCode(value), "status $value")
        }
    }

    @Test
    fun anUnusableStatusCodeIsRejected() {
        listOf<Any?>(null, "200", 99, 600, -1, 200.5, js("({})")).forEach { value ->
            assertNull(wxTransferStatusCode(value), "status $value")
        }
    }

    @Test
    fun uploadProgressReportsItsOwnByteFields() {
        val progress = wxTransferProgress(
            fakeTransferProgress(
                progress = 40,
                totalBytesSent = 400,
                totalBytesExpectedToSend = 1000,
            ),
        ) as WxTransferProgress.Present

        assertEquals(40, progress.percent)
        assertEquals(400L, progress.bytesTransferred)
        assertEquals(1000L, progress.bytesExpected)
    }

    @Test
    fun downloadProgressReportsItsOwnByteFields() {
        val progress = wxTransferProgress(
            fakeTransferProgress(
                progress = 75,
                totalBytesWritten = 750,
                totalBytesExpectedToWrite = 1000,
            ),
        ) as WxTransferProgress.Present

        assertEquals(75, progress.percent)
        assertEquals(750L, progress.bytesTransferred)
        assertEquals(1000L, progress.bytesExpected)
    }

    @Test
    fun progressWithoutByteCountsIsStillUsable() {
        // Not every host reports byte counts for every direction, and a percentage
        // alone is a figure the contract can carry.
        val progress = wxTransferProgress(fakeTransferProgress(50)) as WxTransferProgress.Present

        assertEquals(50, progress.percent)
        assertNull(progress.bytesTransferred)
        assertNull(progress.bytesExpected)
    }

    @Test
    fun progressBoundsAreAccepted() {
        listOf(0, 100).forEach { percent ->
            val progress = wxTransferProgress(fakeTransferProgress(percent))
                as WxTransferProgress.Present

            assertEquals(percent, progress.percent, "percent $percent")
        }
    }

    @Test
    fun anUnusablePercentageMakesTheFigureUnreadable() {
        // Progress is advisory, so this is reported as no figure rather than as a
        // broken transfer; the adapter decides that, and here the reader says so.
        listOf<Any?>(null, "50", -1, 101, 50.5, js("({})")).forEach { value ->
            assertEquals(
                WxTransferProgress.Unreadable,
                wxTransferProgress(fakeTransferProgress(value)),
                "progress of $value",
            )
        }
    }

    @Test
    fun aByteCountTheSdkCannotCarryIsTreatedAsUnreported() {
        // A malformed byte count does not poison a usable percentage.
        listOf<Any?>("400", -1, 1.5, js("({})")).forEach { value ->
            val progress = wxTransferProgress(
                fakeTransferProgress(progress = 40, totalBytesSent = value),
            ) as WxTransferProgress.Present

            assertEquals(40, progress.percent)
            assertNull(progress.bytesTransferred, "bytesTransferred of $value")
        }
    }

    @Test
    fun aTaskWithBothProgressMethodsSupportsProgress() {
        assertTrue(wxTransferTaskSupportsProgress(fakeWxTask()))
    }

    @Test
    fun aTaskMissingEitherProgressMethodDoesNotSupportProgress() {
        // Registering without being able to remove would leak the listener, so a task
        // missing the removal half is treated as unable to report progress at all.
        assertFalse(wxTransferTaskSupportsProgress(fakeWxTask(hasOn = false)))
        assertFalse(wxTransferTaskSupportsProgress(fakeWxTask(hasOff = false)))
        assertFalse(wxTransferTaskSupportsProgress(fakeWxTask(hasAbort = false, hasOn = false)))
    }

    @Test
    fun theProgressFieldsAreTheSameShapeTheAdapterReads() {
        // The reader and the fake builder must agree on the wire fields, so a mistake
        // in either is caught here rather than in a device run.
        val task = FakeTransferTask()
        val seen = mutableListOf<WxTransferProgress>()
        task.onProgress { seen += wxTransferProgress(it) }
        task.emitPercent(30)

        assertEquals(1, seen.size)
        assertEquals(30, (seen[0] as WxTransferProgress.Present).percent)
    }

    /** A raw task shape with the members a test wants it to have. */
    private fun fakeWxTask(
        hasAbort: Boolean = true,
        hasOn: Boolean = true,
        hasOff: Boolean = true,
    ): WxTransferTask {
        val task: WxTransferTask = js("({})")
        if (hasAbort) js("task.abort = function () {}")
        if (hasOn) js("task.onProgressUpdate = function () {}")
        if (hasOff) js("task.offProgressUpdate = function () {}")
        return task
    }
}
