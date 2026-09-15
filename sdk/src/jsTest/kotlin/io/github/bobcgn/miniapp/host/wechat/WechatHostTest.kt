package io.github.bobcgn.miniapp.host.wechat

import io.github.bobcgn.miniapp.capability.CapabilityKey
import io.github.bobcgn.miniapp.capability.CapabilitySupport
import io.github.bobcgn.miniapp.capability.lifecycle.MiniAppLifecycle
import io.github.bobcgn.miniapp.capability.lifecycle.MiniAppLifecycleState
import io.github.bobcgn.miniapp.capability.network.MiniAppHttpTransport
import io.github.bobcgn.miniapp.capability.storage.MiniAppStorage
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatAuthHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatNavigationHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatNetworkHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatStorageHost
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * The WeChat host is a capability provider, not a platform switch. This test
 * drives it entirely through the FakeAdapter boundary, so it never touches `wx`.
 */
internal class WechatHostTest {
    @Test
    fun hostProvidesStorageThroughCapabilityFacet() {
        val host = wechatHost()

        assertEquals(CapabilitySupport.Supported, host.capabilitySupport(MiniAppStorage.Key))
        assertEquals(CapabilitySupport.Supported, host.capabilitySupport(MiniAppHttpTransport.Key))
        assertEquals(CapabilitySupport.Supported, host.capabilitySupport(MiniAppLifecycle.Key))
        assertEquals(
            CapabilitySupport.Unsupported,
            host.capabilitySupport(CapabilityKey("unknown")),
        )
        assertNotNull(host.storage)
        assertNotNull(host.network)
        assertNotNull(host.lifecycle)
        assertNotNull(host.platform.auth)
    }

    @Test
    fun navigationAndPageLifecycleStayBehindTheEscapeHatch() {
        val host = wechatHost()

        // A page stack and a page route are WeChat semantics, not common capabilities.
        assertEquals(CapabilitySupport.Unsupported, host.capabilitySupport(CapabilityKey("navigation")))
        assertNotNull(host.platform.navigation)
        assertNotNull(host.platform.pageLifecycle)
    }

    @Test
    fun theCommonLifecycleIsDrivenByTheWechatAppHooks() {
        val host = wechatHost()

        assertEquals(MiniAppLifecycleState.BACKGROUND, host.lifecycle.state)

        host.platform.appLifecycle.appShown()
        assertEquals(MiniAppLifecycleState.FOREGROUND, host.lifecycle.state)
    }

    private fun wechatHost(): WechatHost = WechatHost(
        storageHost = FakeWechatStorageHost(),
        authHost = FakeWechatAuthHost(),
        networkHost = FakeWechatNetworkHost(),
        navigationHost = FakeWechatNavigationHost(),
    )
}
