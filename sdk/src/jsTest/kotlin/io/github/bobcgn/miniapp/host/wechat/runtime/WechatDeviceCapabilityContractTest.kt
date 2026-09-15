package io.github.bobcgn.miniapp.host.wechat.runtime

import io.github.bobcgn.miniapp.capability.CapabilityKey
import io.github.bobcgn.miniapp.capability.CapabilitySupport
import io.github.bobcgn.miniapp.host.wechat.WeChatDeviceCapabilities
import io.github.bobcgn.miniapp.host.wechat.testing.fakeWechatHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatClipboardHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatHapticsHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatRuntimeInfoHost
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * What the four device capabilities must hold for the SDK as a whole, driven
 * through the real `WechatHost`.
 *
 * These are not real-host facts. A clipboard read needs the user's device, and a
 * vibration can only be confirmed by someone holding it; these checks state the
 * boundary around them instead.
 */
internal class WechatDeviceCapabilityContractTest {
    @Test
    fun theFourCapabilitiesHaveDistinctWeChatSpecificKeys() {
        val keys = listOf(
            WeChatDeviceCapabilities.ClipboardRead,
            WeChatDeviceCapabilities.ClipboardWrite,
            WeChatDeviceCapabilities.VibrateShort,
            WeChatDeviceCapabilities.VibrateLong,
        )

        assertEquals(4, keys.toSet().size)
        keys.forEach { key ->
            // Namespaced, so none of them is presented as a host-neutral capability.
            assertEquals(true, key.value.startsWith("wechat."), "unexpected key '${key.value}'")
        }
        assertNotEquals(CapabilityKey("clipboard"), WeChatDeviceCapabilities.ClipboardRead)
    }

    @Test
    fun aHostThatProvidesAllFourReportsThemSupported() {
        val host = fakeWechatHost()

        assertEquals(
            CapabilitySupport.Supported,
            host.capabilitySupport(WeChatDeviceCapabilities.ClipboardRead),
        )
        assertEquals(
            CapabilitySupport.Supported,
            host.capabilitySupport(WeChatDeviceCapabilities.ClipboardWrite),
        )
        assertEquals(
            CapabilitySupport.Supported,
            host.capabilitySupport(WeChatDeviceCapabilities.VibrateShort),
        )
        assertEquals(
            CapabilitySupport.Supported,
            host.capabilitySupport(WeChatDeviceCapabilities.VibrateLong),
        )
    }

    @Test
    fun aHostThatProvidesOnlySomeOfThemReportsEachSeparately() {
        // A host may expose one clipboard direction and one vibration length; the
        // gate must not answer all-or-nothing.
        val host = fakeWechatHost(
            runtimeInfoHost = FakeWechatRuntimeInfoHost(
                availableSchemas = setOf("getClipboardData", "vibrateLong"),
            ),
        )

        assertEquals(
            CapabilitySupport.Supported,
            host.capabilitySupport(WeChatDeviceCapabilities.ClipboardRead),
        )
        assertEquals(
            CapabilitySupport.Unsupported,
            host.capabilitySupport(WeChatDeviceCapabilities.ClipboardWrite),
        )
        assertEquals(
            CapabilitySupport.Unsupported,
            host.capabilitySupport(WeChatDeviceCapabilities.VibrateShort),
        )
        assertEquals(
            CapabilitySupport.Supported,
            host.capabilitySupport(WeChatDeviceCapabilities.VibrateLong),
        )
    }

    @Test
    fun aCapabilityOutsideTheCatalogueIsUnsupported() {
        val host = fakeWechatHost()

        assertEquals(
            CapabilitySupport.Unsupported,
            host.capabilitySupport(CapabilityKey("wechat.not-a-capability")),
        )
    }

    @Test
    fun aHostThatCannotBeProbedAtAllReportsUnsupported() {
        val host = fakeWechatHost(
            runtimeInfoHost = FakeWechatRuntimeInfoHost(canIUseAvailable = false),
        )

        assertEquals(
            CapabilitySupport.Unsupported,
            host.capabilitySupport(WeChatDeviceCapabilities.ClipboardRead),
        )
        assertEquals(
            CapabilitySupport.Unsupported,
            host.capabilitySupport(WeChatDeviceCapabilities.VibrateLong),
        )
    }

    @Test
    fun theClipboardDoesNotKeepWhatItReads() = runTest {
        val clipboardHost = FakeWechatClipboardHost(clipboardText = "the user's own text")

        val read = fakeWechatHost(clipboardHost = clipboardHost).platform.clipboard.readText()

        // The text is returned to the caller and nowhere else: a second read asks
        // the host again rather than replaying a stored copy.
        assertEquals("the user's own text", read)
        assertEquals(1, clipboardHost.readCalls)

        fakeWechatHost(clipboardHost = clipboardHost).platform.clipboard.readText()
        assertEquals(2, clipboardHost.readCalls)
    }

    @Test
    fun aClipboardOperationDoesNotTouchHapticsAndTheReverse() = runTest {
        val clipboardHost = FakeWechatClipboardHost()
        val hapticsHost = FakeWechatHapticsHost()
        val host = fakeWechatHost(clipboardHost = clipboardHost, hapticsHost = hapticsHost)

        host.platform.clipboard.writeText("value")
        host.platform.haptics.vibrateShort()

        assertEquals(1, clipboardHost.writeCalls)
        assertEquals(0, clipboardHost.readCalls)
        assertEquals(1, hapticsHost.shortCalls)
        assertEquals(0, hapticsHost.longCalls)
    }
}
