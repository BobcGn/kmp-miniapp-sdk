package io.github.bobcgn.miniapp.capability.lifecycle

import io.github.bobcgn.miniapp.capability.CapabilityKey
import io.github.bobcgn.miniapp.testing.FakeMiniAppLifecycle
import io.github.bobcgn.miniapp.testing.LifecycleContractChecks
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Runs the shared lifecycle contract against the host-neutral reference
 * implementation, plus the contract's own model guards.
 *
 * The WeChat adapter runs the same checks in `WechatLifecycleContractTest`.
 */
internal class MiniAppLifecycleContractTest {
    @Test
    fun initialStateIsBackground() {
        LifecycleContractChecks.initialStateIsBackground(FakeMiniAppLifecycle())
    }

    @Test
    fun foregroundIsObservableAsState() = runTest {
        val lifecycle = FakeMiniAppLifecycle()

        LifecycleContractChecks.foregroundIsObservableAsState(lifecycle) {
            lifecycle.movedToForeground()
        }
    }

    @Test
    fun backgroundIsObservableAsState() = runTest {
        val lifecycle = FakeMiniAppLifecycle()

        LifecycleContractChecks.backgroundIsObservableAsState(
            lifecycle = lifecycle,
            reportForeground = { lifecycle.movedToForeground() },
            reportBackground = { lifecycle.movedToBackground() },
        )
    }

    @Test
    fun aNewCollectorReceivesTheCurrentState() = runTest {
        val lifecycle = FakeMiniAppLifecycle()

        LifecycleContractChecks.aNewCollectorReceivesTheCurrentState(lifecycle) {
            lifecycle.movedToForeground()
        }
    }

    @Test
    fun eachTransitionIsEmittedOnce() = runTest {
        val lifecycle = FakeMiniAppLifecycle()

        LifecycleContractChecks.eachTransitionIsEmittedOnce(
            lifecycle = lifecycle,
            reportForeground = { lifecycle.movedToForeground() },
            reportBackground = { lifecycle.movedToBackground() },
        )
    }

    @Test
    fun capabilityKeyIsHostNeutral() {
        assertEquals(CapabilityKey("lifecycle"), MiniAppLifecycle.Key)
    }
}
