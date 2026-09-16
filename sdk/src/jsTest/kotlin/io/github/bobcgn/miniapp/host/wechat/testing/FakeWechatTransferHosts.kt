package io.github.bobcgn.miniapp.host.wechat.testing

import io.github.bobcgn.miniapp.host.wechat.WeChatDownloadRequest
import io.github.bobcgn.miniapp.host.wechat.WeChatUploadRequest
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatDownloadFileHost
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatTransferTask
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatUploadFileHost
import io.github.bobcgn.miniapp.host.wechat.interop.WxDownloadFileSuccessResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxTransferProgressListener
import io.github.bobcgn.miniapp.host.wechat.interop.WxTransferProgressResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxUploadFileSuccessResult

/**
 * The FakeAdapter boundary for one transfer task.
 *
 * It implements the task the SDK wraps, so a test can prove how the adapter pairs
 * progress registration with removal and how often it aborts. It holds no JavaScript,
 * so the same fake serves the upload and download directions.
 *
 * @param supportsProgress whether this task can report progress at all, which is
 *   false when the host is missing one of the two progress methods
 */
internal class FakeTransferTask(
    override val supportsProgress: Boolean = true,
) : WechatTransferTask {
    var abortCalls: Int = 0
        private set

    var progressRegistrations: Int = 0
        private set

    var progressRemovals: Int = 0
        private set

    private var listener: WxTransferProgressListener? = null

    /** Whether a progress listener is registered right now, so a test can prove none leaked. */
    val progressRegistered: Boolean
        get() = listener != null

    override fun abort() {
        abortCalls += 1
    }

    override fun onProgress(listener: WxTransferProgressListener) {
        progressRegistrations += 1
        this.listener = listener
    }

    override fun offProgress(listener: WxTransferProgressListener) {
        progressRemovals += 1
        if (this.listener === listener) this.listener = null
    }

    /** Reports a progress payload to the registered listener, if one is registered. */
    fun emitProgress(payload: WxTransferProgressResult) {
        listener?.invoke(payload)
    }

    /** Reports a whole-percentage progress payload with no byte counts. */
    fun emitPercent(percent: Int) {
        emitProgress(fakeTransferProgress(percent))
    }
}

/**
 * The FakeAdapter boundary for uploads.
 *
 * By default it answers immediately, which is how a host that rejects the request
 * before returning a task behaves. A test that needs to observe an in-flight transfer
 * sets [answerImmediately] to false and completes it by hand.
 *
 * @param supported whether this host exposes the upload API
 * @param task the task the host returns, or `null` for a host that returns none
 */
internal class FakeWechatUploadFileHost(
    var supported: Boolean = true,
    var task: WechatTransferTask? = FakeTransferTask(),
) : WechatUploadFileHost {
    /** Message the transfer fails with, or `null` to succeed. */
    var failureMessage: String? = null

    /** HTTP status the host reports on success, typed loosely for malformed shapes. */
    var statusCode: Any? = 200

    /** Response body the host reports on success, typed loosely for malformed shapes. */
    var body: Any? = "ok"

    /** Whether the host answers during the call, rather than waiting to be driven. */
    var answerImmediately: Boolean = true

    var calls: Int = 0
        private set

    /** The request last handed to the host, so a test can assert what was forwarded. */
    var lastRequest: WeChatUploadRequest? = null
        private set

    private var succeed: (() -> Unit)? = null
    private var fail: (() -> Unit)? = null

    override fun isSupported(): Boolean = supported

    override fun upload(
        request: WeChatUploadRequest,
        success: (WxUploadFileSuccessResult) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ): WechatTransferTask? {
        calls += 1
        lastRequest = request

        succeed = { success(fakeUploadSuccess(statusCode = statusCode, data = body)) }
        fail = { failure(fakeWxFailure(failureMessage ?: "uploadFile:fail")) }

        if (answerImmediately) {
            if (failureMessage != null) fail?.invoke() else succeed?.invoke()
        }
        return task
    }

    /** Completes the transfer successfully. */
    fun complete(): Unit = requireNotNull(succeed) { "No upload is in flight" }.invoke()

    /** Fails the transfer with [message]. */
    fun failWith(message: String) {
        failureMessage = message
        requireNotNull(fail) { "No upload is in flight" }.invoke()
    }
}

/**
 * The FakeAdapter boundary for downloads.
 *
 * @param supported whether this host exposes the download API
 * @param task the task the host returns, or `null` for a host that returns none
 */
internal class FakeWechatDownloadFileHost(
    var supported: Boolean = true,
    var task: WechatTransferTask? = FakeTransferTask(),
) : WechatDownloadFileHost {
    /** Message the transfer fails with, or `null` to succeed. */
    var failureMessage: String? = null

    /** Temporary path the host reports, typed loosely for malformed shapes. */
    var tempFilePath: Any? = "/tmp/wechat-download.bin"

    /** HTTP status the host reports, typed loosely for malformed shapes. */
    var statusCode: Any? = 200

    /** Target path the host echoes back, or `null` when it reports none. */
    var reportedFilePath: Any? = null

    /** Whether the host answers during the call, rather than waiting to be driven. */
    var answerImmediately: Boolean = true

    var calls: Int = 0
        private set

    /** The request last handed to the host, so a test can assert what was forwarded. */
    var lastRequest: WeChatDownloadRequest? = null
        private set

    private var succeed: (() -> Unit)? = null
    private var fail: (() -> Unit)? = null

    override fun isSupported(): Boolean = supported

    override fun download(
        request: WeChatDownloadRequest,
        success: (WxDownloadFileSuccessResult) -> Unit,
        failure: (WxGeneralCallbackResult) -> Unit,
    ): WechatTransferTask? {
        calls += 1
        lastRequest = request

        succeed = {
            success(
                fakeDownloadSuccess(
                    tempFilePath = tempFilePath,
                    statusCode = statusCode,
                    path = reportedFilePath,
                ),
            )
        }
        fail = { failure(fakeWxFailure(failureMessage ?: "downloadFile:fail")) }

        if (answerImmediately) {
            if (failureMessage != null) fail?.invoke() else succeed?.invoke()
        }
        return task
    }

    /** Completes the transfer successfully. */
    fun complete(): Unit = requireNotNull(succeed) { "No download is in flight" }.invoke()

    /** Fails the transfer with [message]. */
    fun failWith(message: String) {
        failureMessage = message
        requireNotNull(fail) { "No download is in flight" }.invoke()
    }
}
