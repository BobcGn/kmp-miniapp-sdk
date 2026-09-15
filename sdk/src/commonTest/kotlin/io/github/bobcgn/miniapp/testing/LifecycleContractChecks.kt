package io.github.bobcgn.miniapp.testing

import io.github.bobcgn.miniapp.capability.lifecycle.MiniAppLifecycle
import io.github.bobcgn.miniapp.capability.lifecycle.MiniAppLifecycleState
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlin.test.assertEquals

/**
 * The [MiniAppLifecycle] contract, expressed once for every implementation.
 *
 * A host decides *when* state changes based on the hooks its runtime delivers;
 * every implementation must agree on what a change means and on what a collector
 * observes. Checks therefore take the transitions they need as lambdas, named
 * for their meaning rather than for any host's hook names, so the same suite runs
 * against the host-neutral reference implementation and against a host adapter.
 */
internal object LifecycleContractChecks {
    /** Nothing has been reported yet, so the application is not in front of the user. */
    fun initialStateIsBackground(lifecycle: MiniAppLifecycle) {
        assertEquals(MiniAppLifecycleState.BACKGROUND, lifecycle.state)
    }

    suspend fun foregroundIsObservableAsState(
        lifecycle: MiniAppLifecycle,
        reportForeground: () -> Unit,
    ) {
        reportForeground()

        assertEquals(MiniAppLifecycleState.FOREGROUND, lifecycle.state)
    }

    suspend fun backgroundIsObservableAsState(
        lifecycle: MiniAppLifecycle,
        reportForeground: () -> Unit,
        reportBackground: () -> Unit,
    ) {
        reportForeground()
        reportBackground()

        assertEquals(MiniAppLifecycleState.BACKGROUND, lifecycle.state)
    }

    /** A new collector sees the current state without waiting for the next change. */
    suspend fun aNewCollectorReceivesTheCurrentState(
        lifecycle: MiniAppLifecycle,
        reportForeground: () -> Unit,
    ) {
        reportForeground()

        assertEquals(MiniAppLifecycleState.FOREGROUND, lifecycle.stateChanges.first())
    }

    /**
     * Each real transition is emitted once, and repeating the current state is not
     * a transition.
     */
    suspend fun eachTransitionIsEmittedOnce(
        lifecycle: MiniAppLifecycle,
        reportForeground: () -> Unit,
        reportBackground: () -> Unit,
    ): Unit = coroutineScope {
        val observed = mutableListOf<MiniAppLifecycleState>()
        val collector = launch { lifecycle.stateChanges.collect { observed += it } }

        yield()
        reportForeground()
        yield()
        // Reporting the state the lifecycle is already in is not a transition.
        reportForeground()
        yield()
        reportBackground()
        yield()
        collector.cancel()

        assertEquals(
            listOf(
                MiniAppLifecycleState.BACKGROUND,
                MiniAppLifecycleState.FOREGROUND,
                MiniAppLifecycleState.BACKGROUND,
            ),
            observed,
        )
    }
}
