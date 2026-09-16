package io.github.bobcgn.miniapp.host.wechat

import io.github.bobcgn.miniapp.capability.CapabilityKey
import io.github.bobcgn.miniapp.capability.CapabilitySupport
import io.github.bobcgn.miniapp.capability.lifecycle.MiniAppLifecycle
import io.github.bobcgn.miniapp.capability.lifecycle.MiniAppLifecycleState
import io.github.bobcgn.miniapp.capability.network.MiniAppHttpTransport
import io.github.bobcgn.miniapp.capability.permission.MiniAppPermissions
import io.github.bobcgn.miniapp.capability.privacy.MiniAppPrivacy
import io.github.bobcgn.miniapp.capability.storage.MiniAppStorage
import io.github.bobcgn.miniapp.host.HostVersion
import io.github.bobcgn.miniapp.host.wechat.WeChatDeviceCapabilities
import io.github.bobcgn.miniapp.host.wechat.runtime.WechatCapabilityCatalog
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatRuntimeInfoHost
import io.github.bobcgn.miniapp.host.wechat.testing.fakeWechatHost
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * The WeChat host is a capability provider, not a platform switch, and it answers
 * support from the runtime it found rather than from a hardcoded list.
 *
 * This test drives it entirely through the FakeAdapter boundary, so it never
 * touches `wx`.
 */
internal class WechatHostTest {
    @Test
    fun hostReportsSupportForTheCapabilitiesItProvides() {
        val host = fakeWechatHost()

        assertEquals(CapabilitySupport.Supported, host.capabilitySupport(MiniAppStorage.Key))
        assertEquals(CapabilitySupport.Supported, host.capabilitySupport(MiniAppHttpTransport.Key))
        assertEquals(CapabilitySupport.Supported, host.capabilitySupport(MiniAppLifecycle.Key))
        // The permission capability is supported when its three host APIs exist;
        // it is never reported as depending on a permission itself.
        assertEquals(CapabilitySupport.Supported, host.capabilitySupport(MiniAppPermissions.Key))
        assertEquals(CapabilitySupport.Supported, host.capabilitySupport(MiniAppPrivacy.Key))
        // Session checking is a WeChat-specific condition, so its key is namespaced.
        assertEquals(CapabilitySupport.Supported, host.capabilitySupport(WeChatSessionState.Key))
        // The four device capabilities are WeChat-specific too, and each is gated
        // separately rather than through one all-or-nothing switch.
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
        // The file operations are separate host methods, each gated on its own.
        assertEquals(
            CapabilitySupport.Supported,
            host.capabilitySupport(WeChatDeviceCapabilities.FileSystemRead),
        )
        assertEquals(
            CapabilitySupport.Supported,
            host.capabilitySupport(WeChatDeviceCapabilities.FileSystemWrite),
        )
        assertEquals(
            CapabilitySupport.Supported,
            host.capabilitySupport(WeChatDeviceCapabilities.FileSystemAccess),
        )
        assertEquals(
            CapabilitySupport.Supported,
            host.capabilitySupport(WeChatDeviceCapabilities.FileSystemRemove),
        )
        // Location availability is gated on its own; the permission it needs and
        // the host's privacy contract are separate questions.
        assertEquals(
            CapabilitySupport.Supported,
            host.capabilitySupport(WeChatDeviceCapabilities.Location),
        )
        // Scanning is gated on the API alone. No permission precondition for it
        // could be established, so there is nothing else to answer here.
        assertEquals(
            CapabilitySupport.Supported,
            host.capabilitySupport(WeChatDeviceCapabilities.ScanCode),
        )
        // Media selection is gated on the API alone, for the same reason.
        assertEquals(
            CapabilitySupport.Supported,
            host.capabilitySupport(WeChatDeviceCapabilities.ChooseMedia),
        )
        assertEquals(
            CapabilitySupport.Unsupported,
            host.capabilitySupport(CapabilityKey("unknown")),
        )
        assertNotNull(host.storage)
        assertNotNull(host.network)
        assertNotNull(host.lifecycle)
        assertNotNull(host.permissions)
        assertNotNull(host.privacy)
        assertNotNull(host.platform.auth)
    }

    @Test
    fun hostReportsVersionDependenceFromTheRuntimeItFound() {
        val host = fakeWechatHost(
            runtimeInfoHost = FakeWechatRuntimeInfoHost(
                baseLibraryVersion = "2.19.4",
                // A base library this old cannot confirm the newer inspection API.
                availableSchemas = emptySet(),
            ),
        )

        val support = host.capabilitySupport(WechatCapabilityCatalog.RuntimeDetectionKey)

        val dependent = assertIs<CapabilitySupport.VersionDependent>(support)
        assertEquals(version("2.20.1"), dependent.requiredVersion)
        assertEquals(version("2.19.4"), dependent.currentVersion)
    }

    @Test
    fun navigationAndPageLifecycleStayBehindTheEscapeHatch() {
        val host = fakeWechatHost()

        // A page stack and a page route are WeChat semantics, not common capabilities.
        assertEquals(
            CapabilitySupport.Unsupported,
            host.capabilitySupport(CapabilityKey("navigation")),
        )
        assertNotNull(host.platform.navigation)
        assertNotNull(host.platform.pageLifecycle)
    }

    @Test
    fun theRuntimeEscapeHatchReportsWhatTheRuntimeSaid() {
        val host = fakeWechatHost(
            runtimeInfoHost = FakeWechatRuntimeInfoHost(
                baseLibraryVersion = "3.17.3",
                platform = "devtools",
            ),
        )
        val runtimeInfo = host.platform.runtimeInfo

        assertEquals("3.17.3", runtimeInfo.baseLibraryVersion?.toString())
        assertEquals("devtools", runtimeInfo.platform)
        assertTrue(runtimeInfo.isDeveloperTools)
        assertTrue(runtimeInfo.canIUse("getStorage"))
        // A schema this fake host does not confirm still answers false.
        assertFalse(runtimeInfo.canIUse("chooseLocation"))
    }

    @Test
    fun theCommonLifecycleIsDrivenByTheWechatAppHooks() {
        val host = fakeWechatHost()

        assertEquals(MiniAppLifecycleState.BACKGROUND, host.lifecycle.state)

        host.platform.appLifecycle.appShown()
        assertEquals(MiniAppLifecycleState.FOREGROUND, host.lifecycle.state)
    }

    private fun version(raw: String): HostVersion = requireNotNull(HostVersion.parse(raw))
}
