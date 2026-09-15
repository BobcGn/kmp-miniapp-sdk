package io.github.bobcgn.miniapp.capability.lifecycle

import io.github.bobcgn.miniapp.capability.CapabilityKey
import kotlinx.coroutines.flow.Flow

/**
 * App-level lifecycle state a mini-app host can report.
 *
 * Only the concept every mini-app host can honestly express is modelled here:
 * whether the application is in front of the user. WeChat and Alipay report it
 * through `onShow` and `onHide`; a WebView-based host reports it through
 * visibility. Page-level lifecycle, page stacks, and host-specific hooks are not
 * app-level states and stay with the host that defines them.
 */
public enum class MiniAppLifecycleState {
    /** The application is presented to the user. */
    FOREGROUND,

    /** The application has launched but is not presented to the user. */
    BACKGROUND,
}

/**
 * Platform-neutral app-level lifecycle capability.
 *
 * A host reports lifecycle only to the entry point the consumer registers, so an
 * SDK cannot observe it independently. Each host adapter therefore accepts the
 * hooks its runtime hands the consumer and republishes them through this contract.
 */
public interface MiniAppLifecycle {
    /**
     * The state reported most recently.
     *
     * Before the host reports anything, this is [MiniAppLifecycleState.BACKGROUND],
     * because the application has not been presented to the user yet.
     */
    public val state: MiniAppLifecycleState

    /**
     * Emits the current state to each new collector, then every later change.
     *
     * Collecting does not require a subscription handle: cancelling the collector
     * is the way to stop observing. A repeated report of the current state is not
     * emitted again.
     */
    public val stateChanges: Flow<MiniAppLifecycleState>

    public companion object {
        /** Stable identity used for host capability-support queries. */
        public val Key: CapabilityKey = CapabilityKey("lifecycle")
    }
}

/** Host facet that provides the common [MiniAppLifecycle] capability. */
public interface LifecycleCapabilityProvider {
    /** Lifecycle implementation supplied by the active host. */
    public val lifecycle: MiniAppLifecycle
}
