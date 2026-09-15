package io.github.bobcgn.miniapp.async

import io.github.bobcgn.miniapp.error.MiniAppException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Optional cancellation hook returned by a host operation that supports abort. */
internal fun interface HostOperationAborter {
    /** Requests cancellation of the underlying host operation. */
    fun abort(): Unit
}

/**
 * Adapts one callback-style host operation to a cancellable Kotlin suspension.
 *
 * [register] receives terminal success and failure callbacks and may return an
 * abort hook. If no hook is returned, coroutine cancellation only prevents later
 * callbacks from resuming the continuation; it does not stop the host operation.
 * Every terminal path is guarded so the continuation and abort hook run at most
 * once, including when a host invokes contradictory callbacks.
 */
internal suspend fun <T> awaitHostCallback(
    register: (
        success: (T) -> Unit,
        failure: (MiniAppException) -> Unit,
    ) -> HostOperationAborter?,
): T = suspendCancellableCoroutine { continuation ->
    var terminal = false
    var cancellationRequested = false
    var aborter: HostOperationAborter? = null
    var abortInvoked = false

    fun abortOnce() {
        val currentAborter = aborter
        if (!abortInvoked && currentAborter != null) {
            abortInvoked = true
            currentAborter.abort()
        }
    }

    continuation.invokeOnCancellation {
        if (!terminal) {
            terminal = true
            cancellationRequested = true
            abortOnce()
        }
    }

    try {
        aborter = register(
            success@{ value ->
                if (terminal || !continuation.isActive) return@success
                terminal = true
                continuation.resume(value)
            },
            failure@{ error ->
                if (terminal || !continuation.isActive) return@failure
                terminal = true
                continuation.resumeWithException(error)
            },
        )

        // Cancellation may occur synchronously while register is still returning.
        if (cancellationRequested) abortOnce()
    } catch (cause: Throwable) {
        if (!terminal && continuation.isActive) {
            terminal = true
            continuation.resumeWithException(
                MiniAppException.InternalFailure(
                    message = "Host callback registration failed",
                    cause = cause,
                ),
            )
        }
    }
}
