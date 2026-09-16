package io.github.bobcgn.miniapp.host.wechat.interop

import io.github.bobcgn.miniapp.host.wechat.testing.fakeGetNetworkTypeSuccess
import kotlin.js.jsTypeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class WxNetworkStatusTest {
    @Test
    fun theQueryOptionsCarryNoFields() {
        // WeChat declares no options for this call, so the bag only ever receives the
        // callbacks the adapter assigns.
        val options = wxGetNetworkTypeOptions()

        assertEquals("undefined", jsTypeOf(options.success))
        assertEquals("undefined", jsTypeOf(options.fail))
        assertEquals("undefined", jsTypeOf(options.complete))
    }

    @Test
    fun theQueryOptionsAcceptCallbacks() {
        val options = wxGetNetworkTypeOptions()
        options.success = { result -> result.networkType; result.errMsg }
        options.fail = { result -> result.errMsg.length }

        assertNotNull(options.success)
        assertNotNull(options.fail)
    }

    @Test
    fun thePresenceGuardsAnswerFalseWithoutAWxGlobal() {
        assertFalse(hasWxGetNetworkType())
        assertFalse(hasWxOnNetworkStatusChange())
        assertFalse(hasWxOffNetworkStatusChange())
    }

    @Test
    fun everyKnownConnectionKindIsRead() {
        val expected = mapOf(
            "wifi" to true,
            "2g" to true,
            "3g" to true,
            "4g" to true,
            "5g" to true,
            "unknown" to true,
            "none" to false,
        )

        expected.forEach { (hostType, connected) ->
            val state = wxQueryNetworkState(fakeGetNetworkTypeSuccess(hostType).networkType)
                as WxNetworkState.Present

            assertEquals(connected, state.isConnected, hostType)
            assertEquals(hostType, state.networkType, hostType)
        }
    }

    @Test
    fun aConnectionKindTheSdkDoesNotKnowIsStillAConnection() {
        // The host naming a kind this SDK has never seen is not the host reporting no
        // connection, and treating it as offline would be the wrong way round.
        val state = wxQueryNetworkState(fakeGetNetworkTypeSuccess("6g").networkType)
            as WxNetworkState.Present

        assertEquals(true, state.isConnected)
        assertEquals("6g", state.networkType)
    }

    @Test
    fun anUnusableQueryAnswerIsUnreadable() {
        listOf<Any?>(null, 7, "", "   ", js("({})")).forEach { value ->
            assertEquals(
                WxNetworkState.Unreadable,
                wxQueryNetworkState(fakeGetNetworkTypeSuccess(value).networkType),
                "networkType of $value",
            )
        }
    }

    @Test
    fun anEventCarriesItsOwnConnectivity() {
        val connected = wxEventNetworkState(
            isConnected = true,
            networkType = "wifi",
        ) as WxNetworkState.Present
        assertEquals(true, connected.isConnected)
        assertEquals("wifi", connected.networkType)

        // `none` with an explicit false is the host saying it has no connection, and the
        // event's own flag is what is reported.
        val disconnected = wxEventNetworkState(
            isConnected = false,
            networkType = "none",
        ) as WxNetworkState.Present
        assertEquals(false, disconnected.isConnected)
        assertEquals("none", disconnected.networkType)
    }

    @Test
    fun anEventWithoutAUsableConnectivityFlagIsUnreadable() {
        listOf<Any?>(null, "true", 1, js("({})")).forEach { flag ->
            assertEquals(
                WxNetworkState.Unreadable,
                wxEventNetworkState(isConnected = flag, networkType = "wifi"),
                "isConnected of $flag",
            )
        }
    }

    @Test
    fun anEventWithoutAUsableConnectionKindIsUnreadable() {
        listOf<Any?>(null, 7, "", "  ").forEach { value ->
            assertEquals(
                WxNetworkState.Unreadable,
                wxEventNetworkState(isConnected = true, networkType = value),
                "networkType of $value",
            )
        }
    }

    @Test
    fun anEventKindTheSdkDoesNotKnowIsPreserved() {
        val state = wxEventNetworkState(isConnected = true, networkType = "satellite")
            as WxNetworkState.Present

        assertEquals("satellite", state.networkType)
    }
}
