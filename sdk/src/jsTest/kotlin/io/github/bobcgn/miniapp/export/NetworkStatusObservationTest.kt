package io.github.bobcgn.miniapp.export

import io.github.bobcgn.miniapp.capability.network.MiniAppNetworkState
import io.github.bobcgn.miniapp.capability.network.NetworkType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
internal class NetworkStatusObservationTest {
    @Test
    fun stoppingConsumesTheSessionSnapshot() = runTest {
        val changes = MutableSharedFlow<MiniAppNetworkState>(extraBufferCapacity = 1)
        val observation = NetworkStatusObservation(changes, this)
        observation.start()
        runCurrent()

        changes.emit(
            MiniAppNetworkState(
                isConnected = true,
                networkType = NetworkType.WIFI,
                hostNetworkType = "wifi",
            ),
        )
        advanceUntilIdle()

        assertEquals(1, observation.stop().events.size)
        assertEquals(0, observation.stop().events.size)
    }
}
