package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.WeChatUploadRequest
import io.github.bobcgn.miniapp.host.wechat.testing.FakeTransferTask
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatUploadFileHost
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
 * How the WeChat adapter turns the host's upload callbacks and task into SDK behaviour.
 */
internal class WechatUploadFileTest {
    @Test
    fun aCompletedUploadIsReported() = runTest {
        val host = FakeWechatUploadFileHost().apply {
            statusCode = 201
            body = "{\"id\":\"abc\"}"
        }

        val result = WechatUploadFile(host).start(request()).await()

        assertEquals(201, result.statusCode)
        assertEquals("{\"id\":\"abc\"}", result.responseText)
        assertEquals(1, host.calls)
    }

    @Test
    fun theRequestReachesTheHostUnchanged() = runTest {
        val host = FakeWechatUploadFileHost()

        WechatUploadFile(host).start(
            WeChatUploadRequest(
                url = "https://example.com/upload",
                filePath = "/sandbox/file.txt",
                name = "file",
                headers = mapOf("X-Test" to "yes"),
                formData = mapOf("field" to "value"),
                timeoutMillis = 5000,
            ),
        ).await()

        val forwarded = requireNotNull(host.lastRequest)
        assertEquals("https://example.com/upload", forwarded.url)
        assertEquals("/sandbox/file.txt", forwarded.filePath)
        assertEquals("file", forwarded.name)
        assertEquals(mapOf("X-Test" to "yes"), forwarded.headers)
        assertEquals(mapOf("field" to "value"), forwarded.formData)
        assertEquals(5000, forwarded.timeoutMillis)
    }

    @Test
    fun anHttpErrorStatusIsAResultRatherThanAFailure() = runTest {
        // A completed exchange is a response for every status, exactly as the HTTP
        // transport capability reports it.
        val host = FakeWechatUploadFileHost().apply {
            statusCode = 500
            body = "server error"
        }

        val result = WechatUploadFile(host).start(request()).await()

        assertEquals(500, result.statusCode)
        assertEquals("server error", result.responseText)
    }

    @Test
    fun aNonTextBodyIsAnInvalidResponse() = runTest {
        val host = FakeWechatUploadFileHost().apply { body = 7 }

        assertFailsWith<MiniAppException.InvalidResponse> {
            WechatUploadFile(host).start(request()).await()
        }
    }

    @Test
    fun anUnusableStatusCodeIsAnInvalidResponse() = runTest {
        listOf<Any?>(null, "200", 99, 600).forEach { value ->
            val host = FakeWechatUploadFileHost().apply { statusCode = value }

            assertFailsWith<MiniAppException.InvalidResponse>("status of $value") {
                WechatUploadFile(host).start(request()).await()
            }
        }
    }

    @Test
    fun aFailedUploadIsAHostFailure() = runTest {
        val failure = assertFailsWith<MiniAppException.HostFailure> {
            WechatUploadFile(
                FakeWechatUploadFileHost().apply { failureMessage = "uploadFile:fail system error" },
            ).start(request()).await()
        }

        assertEquals("uploadFile", failure.metadata["operation"])
    }

    @Test
    fun anExactTimeoutMessageBecomesATimeout() = runTest {
        val failure = assertFailsWith<MiniAppException.Timeout> {
            WechatUploadFile(
                FakeWechatUploadFileHost().apply { failureMessage = "uploadFile:fail timeout" },
            ).start(request()).await()
        }

        assertEquals("uploadFile", failure.operation)
    }

    @Test
    fun aMessageThatMerelyMentionsTimeoutIsAHostFailure() = runTest {
        // Only the exact host message is a timeout. A wider match would report a
        // transfer that failed for another reason as one the host stopped waiting for.
        val nearMisses = listOf(
            "uploadFile:fail timeout exceeded",
            "uploadFile:fail Timeout",
            "uploadFile:fail connection timeout",
            "uploadFile:fail",
            "timeout",
        )

        nearMisses.forEach { message ->
            val failure = assertFailsWith<MiniAppException.HostFailure>("'$message'") {
                WechatUploadFile(
                    FakeWechatUploadFileHost().apply { failureMessage = message },
                ).start(request()).await()
            }

            assertEquals(message, failure.hostMessage)
        }
    }

    @Test
    fun aHostWithoutTheUploadApiIsAnUnsupportedCapability() = runTest {
        val host = FakeWechatUploadFileHost(supported = false)

        val failure = assertFailsWith<MiniAppException.UnsupportedCapability> {
            WechatUploadFile(host).start(request())
        }

        assertEquals("wechat.upload-file", failure.capability.value)
        assertEquals(0, host.calls)
    }

    @Test
    fun abortingStopsTheHostTaskOnce() = runTest {
        val task = FakeTransferTask()
        val host = FakeWechatUploadFileHost(task = task).apply { answerImmediately = false }
        val transfer = WechatUploadFile(host).start(request())

        assertEquals(true, transfer.abort())
        // A second abort has nothing left to stop, which is what the false reports.
        assertEquals(false, transfer.abort())
        assertEquals(1, task.abortCalls)
    }

    @Test
    fun aLateAnswerAfterAbortIsIgnored() = runTest {
        val host = FakeWechatUploadFileHost().apply { answerImmediately = false }
        val transfer = WechatUploadFile(host).start(request())

        transfer.abort()
        host.complete()

        val thrown = cancellationOf { transfer.await() }
        assertTrue(thrown is CancellationException, "was $thrown")
    }

    @Test
    fun aHostThatGaveNoTaskCannotBeAbortedAndDoesNotPretendOtherwise() = runTest {
        val host = FakeWechatUploadFileHost(task = null).apply { answerImmediately = false }
        val transfer = WechatUploadFile(host).start(request())

        assertFalse(transfer.abortable)
        // Nothing was aborted on the host, and the false is how the caller learns that:
        // the transfer ends for the caller, but the host operation was not stopped.
        assertEquals(false, transfer.abort())
        assertTrue(cancellationOf { transfer.await() } is CancellationException)

        // A transfer nobody aborted still completes normally.
        val untouched = WechatUploadFile(host).start(request())
        host.complete()
        assertEquals(200, untouched.await().statusCode)
    }

    @Test
    fun aCancelledWaitStopsTheHostTaskOnce() = runTest {
        val task = FakeTransferTask()
        val host = FakeWechatUploadFileHost(task = task).apply { answerImmediately = false }
        val transfer = WechatUploadFile(host).start(request())

        val waiting = async(start = CoroutineStart.UNDISPATCHED) { transfer.await() }
        waiting.cancelAndJoin()

        assertEquals(1, task.abortCalls)
    }

    @Test
    fun aHostThatAnswersImmediatelyLeavesNoProgressListenerBehind() = runTest {
        // The host may answer during the call, before its task is attached. Nothing may
        // be registered on a transfer that is already finished, because nothing would
        // ever remove it.
        val task = FakeTransferTask()
        val host = FakeWechatUploadFileHost(task = task)

        val transfer = WechatUploadFile(host).start(request())

        assertEquals(200, transfer.await().statusCode)
        assertEquals(0, task.progressRegistrations)
        assertEquals(0, task.progressRemovals)
        assertFalse(task.progressRegistered)
    }

    @Test
    fun progressIsRegisteredOnceAndRemovedOnCompletion() = runTest {
        val task = FakeTransferTask()
        val host = FakeWechatUploadFileHost(task = task).apply { answerImmediately = false }
        val transfer = WechatUploadFile(host).start(request())

        assertEquals(1, task.progressRegistrations)
        assertTrue(task.progressRegistered)

        host.complete()
        transfer.await()

        assertEquals(1, task.progressRemovals)
        assertFalse(task.progressRegistered)
    }

    @Test
    fun progressIsRemovedOnFailure() = runTest {
        val task = FakeTransferTask()
        val host = FakeWechatUploadFileHost(task = task).apply { answerImmediately = false }
        val transfer = WechatUploadFile(host).start(request())

        host.failWith("uploadFile:fail network is down")
        assertFailsWith<MiniAppException.HostFailure> { transfer.await() }

        assertEquals(1, task.progressRemovals)
        assertFalse(task.progressRegistered)
    }

    @Test
    fun progressIsRemovedOnAbort() = runTest {
        val task = FakeTransferTask()
        val host = FakeWechatUploadFileHost(task = task).apply { answerImmediately = false }
        val transfer = WechatUploadFile(host).start(request())

        transfer.abort()

        assertEquals(1, task.progressRemovals)
        assertFalse(task.progressRegistered)
    }

    @Test
    fun aTaskThatCannotPairProgressIsNotRegisteredAtAll() = runTest {
        val task = FakeTransferTask(supportsProgress = false)
        val host = FakeWechatUploadFileHost(task = task).apply { answerImmediately = false }
        val transfer = WechatUploadFile(host).start(request())

        assertEquals(0, task.progressRegistrations)
        assertNull(transfer.lastProgress())
        host.complete()
        transfer.await()
        assertEquals(0, task.progressRemovals)
    }

    @Test
    fun theLatestProgressIsReportedAndNeverInvented() = runTest {
        val task = FakeTransferTask()
        val host = FakeWechatUploadFileHost(task = task).apply { answerImmediately = false }
        val transfer = WechatUploadFile(host).start(request())

        assertNull(transfer.lastProgress())

        task.emitProgress(
            fakeTransferProgress(progress = 25, totalBytesSent = 250, totalBytesExpectedToSend = 1000),
        )
        task.emitProgress(
            fakeTransferProgress(progress = 60, totalBytesSent = 600, totalBytesExpectedToSend = 1000),
        )

        val progress = requireNotNull(transfer.lastProgress())
        assertEquals(60, progress.percent)
        assertEquals(600L, progress.bytesTransferred)
        assertEquals(1000L, progress.bytesExpected)
    }

    @Test
    fun anUnreadableProgressPayloadLeavesTheLastReadableFigure() = runTest {
        // Progress is advisory, so a figure the SDK cannot read is ignored rather than
        // failing a transfer that is working.
        val task = FakeTransferTask()
        val host = FakeWechatUploadFileHost(task = task).apply { answerImmediately = false }
        val transfer = WechatUploadFile(host).start(request())

        task.emitPercent(30)
        task.emitProgress(fakeTransferProgress(progress = "half way"))

        val progress = requireNotNull(transfer.lastProgress())
        assertEquals(30, progress.percent)

        host.complete()
        assertEquals(200, transfer.await().statusCode)
    }

    @Test
    fun aRepeatedTerminalCallbackIsAnsweredOnce() = runTest {
        val host = FakeWechatUploadFileHost().apply { answerImmediately = false }
        val transfer = WechatUploadFile(host).start(request())

        host.complete()
        host.complete()
        host.failWith("uploadFile:fail late")

        assertEquals(200, transfer.await().statusCode)
    }

    private fun request(): WeChatUploadRequest = WeChatUploadRequest(
        url = "https://example.com/upload",
        filePath = "/sandbox/file.txt",
        name = "file",
    )

    /** Runs [block] and returns whatever it threw, so a cancellation can be inspected. */
    private suspend fun cancellationOf(block: suspend () -> Unit): Throwable? = try {
        block()
        null
    } catch (error: Throwable) {
        error
    }
}
