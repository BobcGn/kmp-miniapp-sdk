package io.github.bobcgn.miniapp.host.wechat.runtime

import io.github.bobcgn.miniapp.capability.lifecycle.MiniAppLifecycle
import io.github.bobcgn.miniapp.capability.lifecycle.MiniAppLifecycleState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * WeChat runtime adapter for the platform-neutral app-level lifecycle.
 *
 * WeChat reports app lifecycle only to the `App(...)` registration owned by the
 * consumer, so the SDK cannot observe it on its own. The consumer forwards the
 * hooks it receives and this class republishes them as [MiniAppLifecycle] state.
 *
 * WeChat calls `onLaunch` and then `onShow`; both report the application as being
 * in front of the user, so both are represented by [appLaunched] and [appShown]
 * reporting the same state. A repeated report of the current state is not
 * re-emitted to collectors.
 */
internal class WechatAppLifecycle : MiniAppLifecycle {
    private val lifecycleState = MutableStateFlow(MiniAppLifecycleState.BACKGROUND)

    override val state: MiniAppLifecycleState
        get() = lifecycleState.value

    override val stateChanges: Flow<MiniAppLifecycleState> = lifecycleState

    /** Records WeChat's `App.onLaunch`. */
    fun appLaunched(): Unit {
        lifecycleState.value = MiniAppLifecycleState.FOREGROUND
    }

    /** Records WeChat's `App.onShow`. */
    fun appShown(): Unit {
        lifecycleState.value = MiniAppLifecycleState.FOREGROUND
    }

    /** Records WeChat's `App.onHide`. */
    fun appHidden(): Unit {
        lifecycleState.value = MiniAppLifecycleState.BACKGROUND
    }
}
