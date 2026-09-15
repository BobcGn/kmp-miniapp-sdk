package io.github.bobcgn.miniapp.host.wechat.runtime

import io.github.bobcgn.miniapp.capability.lifecycle.MiniAppLifecycleState
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * WeChat-specific lifecycle behavior that the shared contract does not describe:
 * which hook produces which state, and how WeChat's launch sequence interacts
 * with the neutral state model.
 */
internal class WechatLifecycleTest {
    @Test
    fun everyWechatForegroundHookReportsTheForegroundState() {
        val lifecycle = WechatAppLifecycle()

        lifecycle.appLaunched()
        assertEquals(MiniAppLifecycleState.FOREGROUND, lifecycle.state)

        lifecycle.appHidden()
        assertEquals(MiniAppLifecycleState.BACKGROUND, lifecycle.state)

        lifecycle.appShown()
        assertEquals(MiniAppLifecycleState.FOREGROUND, lifecycle.state)
    }

    @Test
    fun launchFollowedByShowIsASingleTransition() = runTest {
        val lifecycle = WechatAppLifecycle()
        val observed = mutableListOf<MiniAppLifecycleState>()

        coroutineScope {
            val collector = launch { lifecycle.stateChanges.collect { observed += it } }
            yield()

            // WeChat always calls onShow immediately after onLaunch, so the two hooks
            // report the same state and only one transition is observable.
            lifecycle.appLaunched()
            yield()
            lifecycle.appShown()
            yield()

            collector.cancel()
        }

        assertEquals(
            listOf(MiniAppLifecycleState.BACKGROUND, MiniAppLifecycleState.FOREGROUND),
            observed,
        )
    }
}
