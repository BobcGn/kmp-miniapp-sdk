package io.github.bobcgn.miniapp.export

import io.github.bobcgn.miniapp.capability.network.MiniAppNetworkState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * A JavaScript-friendly observation session over the network status stream.
 *
 * The Kotlin capability exposes a `Flow`; a JavaScript caller can neither collect one
 * nor cancel a Promise, so this holds one collector open between an explicit start and
 * stop and keeps what it saw. It is boundary adaptation, not a second implementation:
 * every state it reports came from the capability's own stream, so the host listener
 * pairing, the late-event rule, and the absence of polling are the capability's.
 *
 * Only the most recent [MAX_OBSERVED] states are kept, because an observation session
 * has no natural end and an unbounded buffer would grow for as long as it runs.
 *
 * The session does not stop observing by itself. A stop that was never asked for is a
 * listener the consumer cannot account for.
 */
internal class NetworkStatusObservation(
    private val changes: Flow<MiniAppNetworkState>,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private var collector: Job? = null
    private val observed = mutableListOf<MiniAppNetworkState>()
    private var failureName: String? = null

    /** Whether a collector is running right now. */
    val isObserving: Boolean
        get() = collector?.isActive == true

    /**
     * Begins observing, discarding anything a previous session saw.
     *
     * Calling this while already observing does nothing: one session is one host
     * registration, so a second start cannot leave a second listener behind.
     */
    fun start() {
        if (isObserving) return
        observed.clear()
        failureName = null
        collector = scope.launch {
            try {
                changes.collect { state ->
                    observed += state
                    if (observed.size > MAX_OBSERVED) observed.removeAt(0)
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Throwable) {
                // The stream ends when the host reports something unreadable. The
                // session records why rather than pretending it saw nothing.
                failureName = error::class.simpleName ?: "Error"
            }
        }
    }

    /**
     * Stops observing and waits until the host listener is removed.
     *
     * Waiting here is what makes the removal observable to the caller: the returned
     * snapshot is taken after the collector ended, so it cannot grow afterwards.
     */
    suspend fun stop(): NetworkStatusObservationResult {
        collector?.cancelAndJoin()
        collector = null
        val result = NetworkStatusObservationResult(
            events = observed.toList(),
            failureName = failureName,
        )
        // A stop consumes this session's snapshot. Repeating stop without a new
        // start must not replay old host events or an old terminal failure.
        observed.clear()
        failureName = null
        return result
    }

    private companion object {
        /** How many states a session keeps. */
        const val MAX_OBSERVED: Int = 32
    }
}

/** What one observation session saw. */
internal class NetworkStatusObservationResult(
    val events: List<MiniAppNetworkState>,
    val failureName: String?,
)
