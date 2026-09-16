package io.github.bobcgn.miniapp.capability.network

import io.github.bobcgn.miniapp.capability.CapabilityKey
import kotlinx.coroutines.flow.Flow

/**
 * The kind of network connection a host reports.
 *
 * Only the kinds the SDK can name are members. Every host names its connection
 * kinds in its own vocabulary, and a host may report one this SDK has never seen,
 * so [MiniAppNetworkState.hostNetworkType] always carries the host's own word and
 * [MiniAppNetworkState.networkType] is `null` for anything unrecognized. A caller
 * that needs to distinguish 2G from 3G can read either; one that only needs to know
 * whether the device is online reads [MiniAppNetworkState.isConnected].
 */
public enum class NetworkType {
    /** A Wi-Fi link. */
    WIFI,

    /** A second-generation cellular link. */
    CELLULAR_2G,

    /** A third-generation cellular link. */
    CELLULAR_3G,

    /** A fourth-generation cellular link. */
    CELLULAR_4G,

    /** A fifth-generation cellular link. */
    CELLULAR_5G,

    /** The host has a connection but cannot say what kind it is. */
    UNKNOWN,

    /** The host reports no connection at all. */
    NONE,
}

/**
 * What a host currently reports about its network.
 *
 * @property isConnected whether the host currently has a usable connection
 * @property networkType the kind of link, or `null` when the host named one this
 *   SDK does not recognize
 * @property hostNetworkType the host's own word for the link, verbatim
 */
public class MiniAppNetworkState internal constructor(
    public val isConnected: Boolean,
    public val networkType: NetworkType?,
    public val hostNetworkType: String,
)

/**
 * Platform-neutral network status capability.
 *
 * Every host can answer whether it is online and over what kind of link, so this is
 * a common capability rather than a WeChat escape hatch. What is *not* common is the
 * vocabulary, which is why the host's own word is always carried alongside the
 * recognized value instead of being mapped or dropped.
 *
 * The query and the listener are gated separately, because a host may be able to
 * answer the question without offering change events. A consumer that only needs the
 * current answer should not be told the capability is missing because the event API
 * is absent.
 */
public interface MiniAppNetworkStatus {
    /**
     * Asks the host for its current network state.
     *
     * @throws io.github.bobcgn.miniapp.error.MiniAppException.UnsupportedCapability
     *   when the host has no such query
     */
    public suspend fun current(): MiniAppNetworkState

    /**
     * Emits every network state the host reports while this collector is active.
     *
     * No state is synthesized on collection: a host that reports nothing has nothing
     * to say, and the SDK does not invent a reading to fill the gap. A consumer that
     * needs a starting point asks [current] first.
     *
     * Cancelling the collector is how observation stops, and each collector owns its
     * own host registration, so the host listener is removed exactly once when that
     * collector ends. Events that arrive after the collector ends are not emitted.
     * Collecting never falls back to polling the host.
     */
    public val changes: Flow<MiniAppNetworkState>

    public companion object {
        /** Gate identity for the one-shot network-type query. */
        public val QueryKey: CapabilityKey = CapabilityKey("network-status-query")

        /** Gate identity for the network change listener. */
        public val ListenerKey: CapabilityKey = CapabilityKey("network-status-listener")
    }
}

/** Host facet that provides the common [MiniAppNetworkStatus] capability. */
public interface NetworkStatusCapabilityProvider {
    /** Network status implementation supplied by the active host. */
    public val networkStatus: MiniAppNetworkStatus
}
