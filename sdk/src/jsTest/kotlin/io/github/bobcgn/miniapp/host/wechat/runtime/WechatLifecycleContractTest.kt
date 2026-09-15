package io.github.bobcgn.miniapp.host.wechat.runtime

import io.github.bobcgn.miniapp.testing.LifecycleContractChecks
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

/**
 * Runs the shared lifecycle contract against the WeChat runtime adapter, driven
 * through the hooks WeChat actually delivers.
 *
 * These are the same checks `MiniAppLifecycleContractTest` runs against the
 * host-neutral reference implementation. Passing here means the WeChat hooks are
 * mapped onto the contract correctly; it is not real-host evidence, because no
 * WeChat runtime is involved.
 */
internal class WechatLifecycleContractTest {
    @Test
    fun initialStateIsBackground() {
        LifecycleContractChecks.initialStateIsBackground(WechatAppLifecycle())
    }

    @Test
    fun foregroundIsObservableAsState() = runTest {
        val lifecycle = WechatAppLifecycle()

        LifecycleContractChecks.foregroundIsObservableAsState(lifecycle) {
            lifecycle.appShown()
        }
    }

    @Test
    fun backgroundIsObservableAsState() = runTest {
        val lifecycle = WechatAppLifecycle()

        LifecycleContractChecks.backgroundIsObservableAsState(
            lifecycle = lifecycle,
            reportForeground = { lifecycle.appShown() },
            reportBackground = { lifecycle.appHidden() },
        )
    }

    @Test
    fun aNewCollectorReceivesTheCurrentState() = runTest {
        val lifecycle = WechatAppLifecycle()

        LifecycleContractChecks.aNewCollectorReceivesTheCurrentState(lifecycle) {
            lifecycle.appShown()
        }
    }

    @Test
    fun eachTransitionIsEmittedOnce() = runTest {
        val lifecycle = WechatAppLifecycle()

        LifecycleContractChecks.eachTransitionIsEmittedOnce(
            lifecycle = lifecycle,
            reportForeground = { lifecycle.appShown() },
            reportBackground = { lifecycle.appHidden() },
        )
    }
}
