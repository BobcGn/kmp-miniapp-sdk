package io.github.bobcgn.miniapp.export

import io.github.bobcgn.miniapp.host.wechat.ExperimentalMiniAppBleApi
import io.github.bobcgn.miniapp.host.wechat.WeChatBleConnectionState
import io.github.bobcgn.miniapp.host.wechat.WeChatBleDevice
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatBluetooth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch

/**
 * The JavaScript-facing BLE session.
 *
 * A JavaScript caller can neither collect a Kotlin `Flow` nor cancel a coroutine, so
 * this holds one collector per stream between the calls that start and stop it, and
 * keeps what it saw. It is boundary adaptation, not a second implementation: the
 * listener pairing, the duplicate policy, and the bounded-buffer policy are the
 * adapter's, and this class only decides when collection starts and stops.
 *
 * **Which call owns which listener.** Opening the adapter starts the connection-state
 * collector, and closing it stops that collector; starting discovery starts the
 * device collector, and stopping discovery stops it. Closing the adapter also stops
 * the device collector, because WeChat's own close ends a scan and a collector whose
 * scan has ended would hold a listener that reports nothing.
 *
 * Only the most recent [MAX_OBSERVED] items of each stream are kept. A session has no
 * natural end, and an unbounded buffer would grow for as long as it runs.
 *
 * The session never stops observing by itself: a stop that was never asked for is a
 * listener the consumer cannot account for.
 */
@OptIn(ExperimentalMiniAppBleApi::class)
internal class BleObservationSession(
    private val bluetooth: WechatBluetooth,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private var deviceCollector: Job? = null
    private var connectionCollector: Job? = null
    private val observedDevices = mutableListOf<WeChatBleDevice>()
    private val observedConnections = mutableListOf<WeChatBleConnectionState>()
    private var discoveryFailureName: String? = null
    private var connectionFailureName: String? = null

    /** Whether a device collector is running right now. */
    val isObservingDevices: Boolean get() = deviceCollector?.isActive == true

    /** Whether a connection-state collector is running right now. */
    val isObservingConnections: Boolean get() = connectionCollector?.isActive == true

    /** Opens the adapter and starts watching for connection state changes. */
    suspend fun openAdapter() {
        bluetooth.openAdapter()
        startConnectionObservation()
    }

    /**
     * Stops watching, then closes the adapter.
     *
     * The device collector is stopped as well, because closing the adapter ends a
     * scan on WeChat's side and the SDK keeps no scan it cannot observe.
     */
    suspend fun closeAdapter() {
        stopDeviceObservation()
        stopConnectionObservation()
        bluetooth.closeAdapter()
    }

    /** Starts a scan and begins collecting the devices it reports. */
    suspend fun startDiscovery() {
        // The collector starts first, so a device reported between the host starting
        // to scan and this session collecting is not lost.
        startDeviceObservation()
        try {
            bluetooth.startDiscovery()
        } catch (error: Throwable) {
            stopDeviceObservation()
            throw error
        }
    }

    /** Stops the scan, then stops collecting. */
    suspend fun stopDiscovery() {
        bluetooth.stopDiscovery()
        stopDeviceObservation()
    }

    /** The devices this session has seen, oldest first, bounded. */
    fun devices(): List<WeChatBleDevice> = observedDevices.toList()

    /** How a discovery collector ended, or `null` when it has not failed. */
    fun discoveryFailure(): String? = discoveryFailureName

    /** The connection state changes this session has seen, oldest first, bounded. */
    fun connectionStates(): List<WeChatBleConnectionState> = observedConnections.toList()

    /** How a connection-state collector ended, or `null` when it has not failed. */
    fun connectionFailure(): String? = connectionFailureName

    /** How many host listeners the adapter currently holds. */
    fun listenerCount(): Int = bluetooth.activeListenerCount

    private suspend fun startDeviceObservation() {
        if (isObservingDevices) return
        observedDevices.clear()
        discoveryFailureName = null
        val registered = CompletableDeferred<Unit>()
        val started = scope.launch {
            try {
                bluetooth.discoveredDevices { registered.complete(Unit) }.collect { device ->
                    observedDevices += device
                    if (observedDevices.size > MAX_OBSERVED) observedDevices.removeAt(0)
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Throwable) {
                discoveryFailureName = error::class.simpleName ?: "Error"
            }
        }
        // A collector that ends without ever registering — the host does not support
        // the stream — must release the wait too, or a start would wait for a signal
        // that can no longer arrive.
        started.invokeOnCompletion { registered.complete(Unit) }
        deviceCollector = started
        awaitRegistration(registered)
    }

    private suspend fun stopDeviceObservation() {
        deviceCollector?.cancelAndJoin()
        deviceCollector = null
    }

    private suspend fun startConnectionObservation() {
        if (isObservingConnections) return
        observedConnections.clear()
        connectionFailureName = null
        val registered = CompletableDeferred<Unit>()
        val started = scope.launch {
            try {
                bluetooth.connectionStates { registered.complete(Unit) }.collect { state ->
                    observedConnections += state
                    if (observedConnections.size > MAX_OBSERVED) observedConnections.removeAt(0)
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Throwable) {
                connectionFailureName = error::class.simpleName ?: "Error"
            }
        }
        started.invokeOnCompletion { registered.complete(Unit) }
        connectionCollector = started
        awaitRegistration(registered)
    }

    /**
     * Waits until a start has either registered its host listener or finished without
     * one.
     *
     * A collector starts on its own dispatch, so the registration it performs is a
     * moment away. Waiting for the signal the stream itself sends is what lets a
     * JavaScript caller treat the promise it awaited as meaning the listener is really
     * there. The wait also ends when the collector completes, which is what happens
     * when the host does not support the stream at all. Registration itself is a
     * synchronous host call inside the collector; silently timing out here would
     * break the promise that a successful start means the listener is installed.
     */
    private suspend fun awaitRegistration(registered: CompletableDeferred<Unit>) {
        registered.await()
    }

    private suspend fun stopConnectionObservation() {
        connectionCollector?.cancelAndJoin()
        connectionCollector = null
    }

    private companion object {
        /** How many items of one stream a session keeps. */
        const val MAX_OBSERVED: Int = 32

    }
}
