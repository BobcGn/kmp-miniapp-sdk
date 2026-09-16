package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.host.wechat.WeChatTransferProgress
import io.github.bobcgn.miniapp.host.wechat.interop.WxTransferProgress
import io.github.bobcgn.miniapp.host.wechat.interop.WxTransferProgressListener
import io.github.bobcgn.miniapp.host.wechat.interop.WxTransferTask
import io.github.bobcgn.miniapp.host.wechat.interop.wxTransferProgress
import io.github.bobcgn.miniapp.host.wechat.interop.wxTransferTaskSupportsProgress
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CancellationException

/**
 * The task an upload or download hands back, as the SDK needs it.
 *
 * This is deliberately not the raw host object: a fake must be able to provide one
 * without JavaScript, and the adapter must be able to ask whether progress can be
 * paired with a removal before it registers anything.
 */
internal interface WechatTransferTask {
    /**
     * Whether this task can report progress.
     *
     * False means one of the two progress methods is missing, so registering a
     * listener could never be paired with removing it.
     */
    val supportsProgress: Boolean

    /** Stops the transfer. */
    fun abort()

    /** Registers [listener] on the underlying task. */
    fun onProgress(listener: WxTransferProgressListener)

    /** Removes [listener] from the underlying task. */
    fun offProgress(listener: WxTransferProgressListener)
}

/** Wraps the raw host task behind [WechatTransferTask]. */
internal class WxTransferTaskAdapter(
    private val task: WxTransferTask,
) : WechatTransferTask {
    override val supportsProgress: Boolean = wxTransferTaskSupportsProgress(task)

    override fun abort(): Unit = task.abort()

    override fun onProgress(listener: WxTransferProgressListener): Unit =
        task.onProgressUpdate(listener)

    override fun offProgress(listener: WxTransferProgressListener): Unit =
        task.offProgressUpdate(listener)
}

/**
 * One in-flight upload or download.
 *
 * It owns the single terminal answer, the progress registration, and the host abort,
 * so that every termination path — completion, failure, or a consumer abort — removes
 * the progress listener exactly once and invokes the host abort at most once.
 *
 * The host may answer synchronously, so a transfer can already be finished by the time
 * its task arrives. Registration is skipped in that case rather than performed late,
 * so a finished transfer never holds a listener nobody will remove.
 */
internal class WechatTransfer<T> {
    private val answer: CompletableDeferred<T> = CompletableDeferred()

    private var hostTask: WechatTransferTask? = null
    private var progressListener: WxTransferProgressListener? = null
    private var latestProgress: WeChatTransferProgress? = null
    private var finished: Boolean = false
    private var abortInvoked: Boolean = false

    /** Whether the host gave a task this transfer can still abort. */
    val abortable: Boolean
        get() = hostTask != null && !finished

    /** Takes the task the host returned and wires up progress reporting. */
    fun attach(task: WechatTransferTask?) {
        hostTask = task
        if (task == null) return

        // The host may have answered during the call, in which case there is nothing
        // left to report progress for and nothing to clean up later.
        if (finished) return

        if (task.supportsProgress) {
            val listener: WxTransferProgressListener = { raw ->
                val parsed = wxTransferProgress(raw)
                if (parsed is WxTransferProgress.Present) {
                    latestProgress = WeChatTransferProgress(
                        percent = parsed.percent,
                        bytesTransferred = parsed.bytesTransferred,
                        bytesExpected = parsed.bytesExpected,
                    )
                }
            }
            progressListener = listener
            task.onProgress(listener)
        }
    }

    /**
     * Records the host's terminal answer, unless the transfer was already aborted.
     *
     * WeChat may deliver a failure after an abort; that failure is discarded because
     * the consumer already ended the transfer.
     */
    fun complete(value: T) {
        if (finished) return
        finished = true
        removeProgress()
        answer.complete(value)
    }

    /** Records a terminal failure, unless the transfer was already aborted. */
    fun fail(error: Throwable) {
        if (finished) return
        finished = true
        removeProgress()
        answer.completeExceptionally(error)
    }

    /**
     * Stops the transfer on the consumer's behalf.
     *
     * @return whether a host abort was actually invoked. False means the host gave no
     *   task, so the underlying operation is still running and this does not pretend
     *   otherwise.
     */
    fun abort(): Boolean {
        if (finished) return false
        finished = true
        removeProgress()
        invokeAbort()
        // Later host callbacks are ignored because the answer is already settled.
        answer.cancel(CancellationException("The consumer aborted the transfer"))
        return hostTask != null
    }

    /** The most recent progress the host reported, or `null` when it reported none. */
    fun lastProgress(): WeChatTransferProgress? = latestProgress

    /**
     * Awaits the host's terminal answer.
     *
     * If the awaiting coroutine is cancelled, the host operation is stopped too — at
     * most once — because a caller that stopped waiting for a transfer did not ask for
     * it to keep running. A consumer abort reaches the same single path, so the host
     * abort is never invoked twice.
     */
    suspend fun await(): T = try {
        answer.await()
    } catch (cancellation: CancellationException) {
        abort()
        throw cancellation
    }

    private fun invokeAbort() {
        val task = hostTask ?: return
        if (abortInvoked) return
        abortInvoked = true
        task.abort()
    }

    private fun removeProgress() {
        val listener = progressListener ?: return
        progressListener = null
        hostTask?.offProgress(listener)
    }
}
