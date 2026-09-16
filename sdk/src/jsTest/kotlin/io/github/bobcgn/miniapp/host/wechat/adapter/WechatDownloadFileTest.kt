package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.WeChatDownloadRequest
import io.github.bobcgn.miniapp.host.wechat.testing.FakeTransferTask
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatDownloadFileHost
import io.github.bobcgn.miniapp.host.wechat.testing.fakeTransferProgress
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * How the WeChat adapter turns the host's download callbacks and task into SDK behaviour.
 */
internal class WechatDownloadFileTest {
    @Test
    fun aCompletedDownloadIsReported() = runTest {
        val host = FakeWechatDownloadFileHost().apply {
            statusCode = 200
            tempFilePath = "/tmp/downloaded.bin"
        }

        val result = WechatDownloadFile(host).start(request()).await()

        assertEquals(200, result.statusCode)
        assertEquals("/tmp/downloaded.bin", result.tempFilePath)
        assertNull(result.filePath)
        assertEquals(1, host.calls)
    }

    @Test
    fun theRequestReachesTheHostUnchanged() = runTest {
        val host = FakeWechatDownloadFileHost()

        WechatDownloadFile(host).start(
            WeChatDownloadRequest(
                url = "https://example.com/file.bin",
                headers = mapOf("X-Test" to "yes"),
                timeoutMillis = 5000,
                filePath = "/sandbox/target.bin",
            ),
        ).await()

        val forwarded = requireNotNull(host.lastRequest)
        assertEquals("https://example.com/file.bin", forwarded.url)
        assertEquals(mapOf("X-Test" to "yes"), forwarded.headers)
        assertEquals(5000, forwarded.timeoutMillis)
        assertEquals("/sandbox/target.bin", forwarded.filePath)
    }

    @Test
    fun aRequestWithoutATargetLeavesThePathUnasked() = runTest {
        val host = FakeWechatDownloadFileHost()

        WechatDownloadFile(host).start(request()).await()

        assertNull(requireNotNull(host.lastRequest).filePath)
    }

    @Test
    fun thePathTheHostReportsForARequestedTargetIsCarried() = runTest {
        val host = FakeWechatDownloadFileHost().apply {
            reportedFilePath = "/sandbox/target.bin"
        }

        val result = WechatDownloadFile(host).start(request()).await()

        assertEquals("/sandbox/target.bin", result.filePath)
    }

    @Test
    fun anHttpErrorStatusIsAResultRatherThanAFailure() = runTest {
        val host = FakeWechatDownloadFileHost().apply { statusCode = 404 }

        val result = WechatDownloadFile(host).start(request()).await()

        assertEquals(404, result.statusCode)
    }

    @Test
    fun aDownloadWithoutAUsableTemporaryPathIsAnInvalidResponse() = runTest {
        // A result with no file is not a download the caller can use, and reporting one
        // would hand over a reference that names nothing.
        listOf<Any?>(null, 7, "", "   ").forEach { value ->
            val host = FakeWechatDownloadFileHost().apply { tempFilePath = value }

            assertFailsWith<MiniAppException.InvalidResponse>("tempFilePath of $value") {
                WechatDownloadFile(host).start(request()).await()
            }
        }
    }

    @Test
    fun anUnusableStatusCodeIsAnInvalidResponse() = runTest {
        listOf<Any?>(null, "200", 99, 600).forEach { value ->
            val host = FakeWechatDownloadFileHost().apply { statusCode = value }

            assertFailsWith<MiniAppException.InvalidResponse>("status of $value") {
                WechatDownloadFile(host).start(request()).await()
            }
        }
    }

    @Test
    fun aFailedDownloadIsAHostFailure() = runTest {
        val failure = assertFailsWith<MiniAppException.HostFailure> {
            WechatDownloadFile(
                FakeWechatDownloadFileHost().apply {
                    failureMessage = "downloadFile:fail system error"
                },
            ).start(request()).await()
        }

        assertEquals("downloadFile", failure.metadata["operation"])
    }

    @Test
    fun anExactTimeoutMessageBecomesATimeout() = runTest {
        val failure = assertFailsWith<MiniAppException.Timeout> {
            WechatDownloadFile(
                FakeWechatDownloadFileHost().apply { failureMessage = "downloadFile:fail timeout" },
            ).start(request()).await()
        }

        assertEquals("downloadFile", failure.operation)
    }

    @Test
    fun aMessageThatMerelyMentionsTimeoutIsAHostFailure() = runTest {
        val nearMisses = listOf(
            "downloadFile:fail timeout exceeded",
            "downloadFile:fail Timeout",
            "downloadFile:fail read timeout",
            "downloadFile:fail",
            "timeout",
        )

        nearMisses.forEach { message ->
            val failure = assertFailsWith<MiniAppException.HostFailure>("'$message'") {
                WechatDownloadFile(
                    FakeWechatDownloadFileHost().apply { failureMessage = message },
                ).start(request()).await()
            }

            assertEquals(message, failure.hostMessage)
        }
    }

    @Test
    fun aHostWithoutTheDownloadApiIsAnUnsupportedCapability() = runTest {
        val host = FakeWechatDownloadFileHost(supported = false)

        val failure = assertFailsWith<MiniAppException.UnsupportedCapability> {
            WechatDownloadFile(host).start(request())
        }

        assertEquals("wechat.download-file", failure.capability.value)
        assertEquals(0, host.calls)
    }

    @Test
    fun abortingStopsTheHostTaskOnce() = runTest {
        val task = FakeTransferTask()
        val host = FakeWechatDownloadFileHost(task = task).apply { answerImmediately = false }
        val transfer = WechatDownloadFile(host).start(request())

        assertEquals(true, transfer.abort())
        // A second abort has nothing left to stop, which is what the false reports.
        assertEquals(false, transfer.abort())
        assertEquals(1, task.abortCalls)
    }

    @Test
    fun aLateAnswerAfterAbortIsIgnored() = runTest {
        val host = FakeWechatDownloadFileHost().apply { answerImmediately = false }
        val transfer = WechatDownloadFile(host).start(request())

        transfer.abort()
        host.complete()

        val thrown = try {
            transfer.await()
            null
        } catch (error: Throwable) {
            error
        }
        assertTrue(thrown is CancellationException, "was $thrown")
    }

    @Test
    fun aHostThatGaveNoTaskCannotBeAbortedAndDoesNotPretendOtherwise() = runTest {
        val host = FakeWechatDownloadFileHost(task = null).apply { answerImmediately = false }
        val transfer = WechatDownloadFile(host).start(request())

        assertFalse(transfer.abortable)
        // Nothing was aborted on the host, and the false is how the caller learns that:
        // the transfer ends for the caller, but the host operation was not stopped.
        assertEquals(false, transfer.abort())
        assertTrue(cancellationOf { transfer.await() } is CancellationException)

        // A transfer nobody aborted still completes normally.
        val untouched = WechatDownloadFile(host).start(request())
        host.complete()
        assertEquals(200, untouched.await().statusCode)
    }

    @Test
    fun aCancelledWaitStopsTheHostTaskOnce() = runTest {
        val task = FakeTransferTask()
        val host = FakeWechatDownloadFileHost(task = task).apply { answerImmediately = false }
        val transfer = WechatDownloadFile(host).start(request())

        val waiting = async(start = CoroutineStart.UNDISPATCHED) { transfer.await() }
        waiting.cancelAndJoin()

        assertEquals(1, task.abortCalls)
    }

    @Test
    fun progressIsRegisteredOnceAndRemovedOnEveryTerminalPath() = runTest {
        // Download reports its own byte fields, so the progress path is exercised here
        // as well as in the upload suite.
        val task = FakeTransferTask()
        val host = FakeWechatDownloadFileHost(task = task).apply { answerImmediately = false }
        val transfer = WechatDownloadFile(host).start(request())

        assertEquals(1, task.progressRegistrations)
        task.emitProgress(
            fakeTransferProgress(
                progress = 50,
                totalBytesWritten = 512,
                totalBytesExpectedToWrite = 1024,
            ),
        )
        val progress = requireNotNull(transfer.lastProgress())
        assertEquals(50, progress.percent)
        assertEquals(512L, progress.bytesTransferred)
        assertEquals(1024L, progress.bytesExpected)

        host.complete()
        transfer.await()

        assertEquals(1, task.progressRemovals)
        assertFalse(task.progressRegistered)
    }

    @Test
    fun progressIsRemovedWhenTheTransferIsAborted() = runTest {
        val task = FakeTransferTask()
        val host = FakeWechatDownloadFileHost(task = task).apply { answerImmediately = false }
        val transfer = WechatDownloadFile(host).start(request())

        transfer.abort()

        assertEquals(1, task.progressRemovals)
        assertFalse(task.progressRegistered)
    }

    @Test
    fun aHostThatAnswersImmediatelyLeavesNoProgressListenerBehind() = runTest {
        val task = FakeTransferTask()
        val host = FakeWechatDownloadFileHost(task = task)

        val transfer = WechatDownloadFile(host).start(request())

        assertEquals(200, transfer.await().statusCode)
        assertEquals(0, task.progressRegistrations)
        assertFalse(task.progressRegistered)
    }

    @Test
    fun anUnreadableProgressPayloadLeavesTheLastReadableFigure() = runTest {
        val task = FakeTransferTask()
        val host = FakeWechatDownloadFileHost(task = task).apply { answerImmediately = false }
        val transfer = WechatDownloadFile(host).start(request())

        task.emitProgress(
            fakeTransferProgress(progress = 10, totalBytesWritten = 100),
        )
        task.emitProgress(fakeTransferProgress(progress = 250))

        assertEquals(10, requireNotNull(transfer.lastProgress()).percent)
    }

    private fun request(): WeChatDownloadRequest = WeChatDownloadRequest(
        url = "https://example.com/file.bin",
    )

    /** Runs [block] and returns whatever it threw, so a cancellation can be inspected. */
    private suspend fun cancellationOf(block: suspend () -> Unit): Throwable? = try {
        block()
        null
    } catch (error: Throwable) {
        error
    }
}
