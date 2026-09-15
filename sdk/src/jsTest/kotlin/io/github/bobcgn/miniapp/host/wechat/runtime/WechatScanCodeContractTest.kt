package io.github.bobcgn.miniapp.host.wechat.runtime

import io.github.bobcgn.miniapp.capability.CapabilityKey
import io.github.bobcgn.miniapp.capability.CapabilitySupport
import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.WeChatDeviceCapabilities
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatClipboardHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatHapticsHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatLocationHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatPermissionHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatPrivacyHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatRuntimeInfoHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatScanCodeHost
import io.github.bobcgn.miniapp.host.wechat.testing.fakeWechatHost
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

/**
 * What the scan capability must hold for the SDK as a whole, driven through the
 * real `WechatHost`.
 *
 * These are not real-host facts. Opening a camera and decoding a code needs a
 * device, and a dismissed interface needs a person to dismiss it; these checks
 * state the boundary around them instead.
 */
internal class WechatScanCodeContractTest {
    @Test
    fun theScanCapabilityHasAWeChatSpecificKey() {
        val key = WeChatDeviceCapabilities.ScanCode

        // Namespaced, so scanning is not presented as a capability every host can
        // be asked for.
        assertEquals("wechat.scan-code", key.value)
        assertNotEquals(CapabilityKey("scan-code"), key)
        assertNotEquals(WeChatDeviceCapabilities.Location, key)
    }

    @Test
    fun aHostThatProvidesScanningReportsItSupported() {
        val host = fakeWechatHost()

        assertEquals(
            CapabilitySupport.Supported,
            host.capabilitySupport(WeChatDeviceCapabilities.ScanCode),
        )
    }

    @Test
    fun aHostWithoutTheScanApiReportsItUnsupported() {
        val host = fakeWechatHost(
            runtimeInfoHost = FakeWechatRuntimeInfoHost(
                availableSchemas = setOf("getLocation"),
            ),
        )

        assertEquals(
            CapabilitySupport.Unsupported,
            host.capabilitySupport(WeChatDeviceCapabilities.ScanCode),
        )
    }

    @Test
    fun aHostThatCannotBeProbedAtAllReportsUnsupported() {
        val host = fakeWechatHost(
            runtimeInfoHost = FakeWechatRuntimeInfoHost(canIUseAvailable = false),
        )

        assertEquals(
            CapabilitySupport.Unsupported,
            host.capabilitySupport(WeChatDeviceCapabilities.ScanCode),
        )
    }

    @Test
    fun aScanAsksTheHostForNoPermission() = runTest {
        // `wx.scanCode` drives WeChat's own interface. Nothing in the host contract
        // ties a permission to it, so the SDK must not query or request one: doing
        // so would prompt for something the API may not even need.
        val permissionHost = FakeWechatPermissionHost()
        val privacyHost = FakeWechatPrivacyHost()
        val host = fakeWechatHost(permissionHost = permissionHost, privacyHost = privacyHost)

        host.platform.scanCode.scan()

        assertEquals(0, permissionHost.getSettingCalls)
        assertEquals(0, permissionHost.authorizeCalls)
        assertEquals(0, permissionHost.openSettingCalls)
        assertEquals(0, privacyHost.getPrivacySettingCalls)
        assertEquals(0, privacyHost.authorizeCalls)
    }

    @Test
    fun aScanDoesNotTouchTheOtherCapabilities() = runTest {
        val clipboardHost = FakeWechatClipboardHost()
        val hapticsHost = FakeWechatHapticsHost()
        val locationHost = FakeWechatLocationHost()
        val scanCodeHost = FakeWechatScanCodeHost()
        val host = fakeWechatHost(
            clipboardHost = clipboardHost,
            hapticsHost = hapticsHost,
            locationHost = locationHost,
            scanCodeHost = scanCodeHost,
        )

        host.platform.scanCode.scan()

        assertEquals(1, scanCodeHost.calls)
        assertEquals(0, clipboardHost.readCalls)
        assertEquals(0, clipboardHost.writeCalls)
        assertEquals(0, hapticsHost.shortCalls)
        assertEquals(0, hapticsHost.longCalls)
        assertEquals(0, locationHost.calls)
    }

    @Test
    fun dismissingAScanIsNotReportedAsAMissingCapability() = runTest {
        // Two different instructions to a consumer: one says the user changed their
        // mind, the other says this host cannot scan at all. A third, permission
        // denial, would send them to a settings page that has nothing to do with it.
        val host = fakeWechatHost(
            scanCodeHost = FakeWechatScanCodeHost().apply { failureMessage = "scanCode:cancel" },
        )

        val failure: MiniAppException = assertFailsWith<MiniAppException.HostInteractionInterrupted> {
            host.platform.scanCode.scan()
        }

        assertEquals(false, failure is MiniAppException.UnsupportedCapability)
        assertEquals(false, failure is MiniAppException.PermissionDenied)
        assertEquals(
            CapabilitySupport.Supported,
            host.capabilitySupport(WeChatDeviceCapabilities.ScanCode),
        )
    }
}
