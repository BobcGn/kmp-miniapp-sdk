package io.github.bobcgn.miniapp.testing

import io.github.bobcgn.miniapp.capability.lifecycle.MiniAppLifecycle
import io.github.bobcgn.miniapp.capability.lifecycle.MiniAppLifecycleState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Host-neutral [MiniAppLifecycle] used to express the lifecycle contract.
 *
 * Its driver methods are named for what they mean rather than for the hooks any
 * one host calls, so the contract can be stated independently of WeChat.
 */
internal class FakeMiniAppLifecycle : MiniAppLifecycle {
    private val lifecycleState = MutableStateFlow(MiniAppLifecycleState.BACKGROUND)

    override val state: MiniAppLifecycleState
        get() = lifecycleState.value

    override val stateChanges: Flow<MiniAppLifecycleState> = lifecycleState

    /** Reports that the host moved the application in front of the user. */
    fun movedToForeground(): Unit {
        lifecycleState.value = MiniAppLifecycleState.FOREGROUND
    }

    /** Reports that the host moved the application out of the user's view. */
    fun movedToBackground(): Unit {
        lifecycleState.value = MiniAppLifecycleState.BACKGROUND
    }
}
