package io.github.bobcgn.miniapp.host.wechat.runtime

import kotlinx.coroutines.ExperimentalCoroutinesApi

import io.github.bobcgn.miniapp.capability.CapabilityKey
import io.github.bobcgn.miniapp.capability.CapabilitySupport
import io.github.bobcgn.miniapp.capability.network.MiniAppNetworkStatus
import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.WeChatDeviceCapabilities
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatClipboardHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatDownloadFileHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatNetworkStatusHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatPermissionHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatPrivacyHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatRuntimeInfoHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatUploadFileHost
import io.github.bobcgn.miniapp.host.wechat.testing.fakeWechatHost
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * What the network extensions must hold for the SDK as a whole, driven through the
 * real `WechatHost`.
 *
 * These are not real-host facts. A real transfer needs an HTTPS endpoint and a device,
 * and a network change needs a device whose connection can actually be switched; these
 * checks state the boundary around them instead.
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class WechatNetworkContractTest {
    @Test
    fun theNetworkCapabilitiesHaveTheirOwnKeys() {
        // The query and the listener are separate keys on purpose: a host may answer the
        // question without offering change events.
        assertEquals(
            "network-status-query",
            MiniAppNetworkStatus.QueryKey.value,
        )
        assertEquals(
            "network-status-listener",
            MiniAppNetworkStatus.ListenerKey.value,
        )
        assertNotEquals(MiniAppNetworkStatus.QueryKey, MiniAppNetworkStatus.ListenerKey)

        // The transfers are WeChat-specific, because their models are built on host file
        // paths and no other host has been shown to share them.
        assertEquals("wechat.upload-file", WeChatDeviceCapabilities.UploadFile.value)
        assertEquals("wechat.download-file", WeChatDeviceCapabilities.DownloadFile.value)
        assertNotEquals(WeChatDeviceCapabilities.UploadFile, WeChatDeviceCapabilities.DownloadFile)
    }

    @Test
    fun aHostThatProvidesAllFourReportsThemSupported() {
        val host = fakeWechatHost()

        assertEquals(
            CapabilitySupport.Supported,
            host.capabilitySupport(MiniAppNetworkStatus.QueryKey),
        )
        assertEquals(
            CapabilitySupport.Supported,
            host.capabilitySupport(MiniAppNetworkStatus.ListenerKey),
        )
        assertEquals(
            CapabilitySupport.Supported,
            host.capabilitySupport(WeChatDeviceCapabilities.UploadFile),
        )
        assertEquals(
            CapabilitySupport.Supported,
            host.capabilitySupport(WeChatDeviceCapabilities.DownloadFile),
        )
    }

    @Test
    fun eachNetworkCapabilityIsGatedOnItsOwnApi() {
        // One API being present says nothing about the others.
        val host = fakeWechatHost(
            runtimeInfoHost = FakeWechatRuntimeInfoHost(
                availableSchemas = setOf("downloadFile"),
            ),
        )

        assertEquals(
            CapabilitySupport.Unsupported,
            host.capabilitySupport(MiniAppNetworkStatus.QueryKey),
        )
        assertEquals(
            CapabilitySupport.Unsupported,
            host.capabilitySupport(MiniAppNetworkStatus.ListenerKey),
        )
        assertEquals(
            CapabilitySupport.Unsupported,
            host.capabilitySupport(WeChatDeviceCapabilities.UploadFile),
        )
        assertEquals(
            CapabilitySupport.Supported,
            host.capabilitySupport(WeChatDeviceCapabilities.DownloadFile),
        )
    }

    @Test
    fun theListenerNeedsBothHalvesToBeSupported() {
        // A host that could register a listener but not remove one would leak it, so
        // only having `on` is not enough to offer the capability.
        val host = fakeWechatHost(
            runtimeInfoHost = FakeWechatRuntimeInfoHost(
                availableSchemas = setOf("onNetworkStatusChange"),
            ),
        )

        assertEquals(
            CapabilitySupport.Unsupported,
            host.capabilitySupport(MiniAppNetworkStatus.ListenerKey),
        )
    }

    @Test
    fun aCapabilityOutsideTheCatalogueIsUnsupported() {
        val host = fakeWechatHost()

        assertEquals(
            CapabilitySupport.Unsupported,
            host.capabilitySupport(CapabilityKey("websocket")),
        )
    }

    @Test
    fun aHostThatCannotBeProbedAtAllReportsUnsupported() {
        val host = fakeWechatHost(
            runtimeInfoHost = FakeWechatRuntimeInfoHost(canIUseAvailable = false),
        )

        assertEquals(
            CapabilitySupport.Unsupported,
            host.capabilitySupport(MiniAppNetworkStatus.QueryKey),
        )
        assertEquals(
            CapabilitySupport.Unsupported,
            host.capabilitySupport(WeChatDeviceCapabilities.DownloadFile),
        )
    }

    @Test
    fun theNetworkExtensionsAskTheHostForNoPermission() = runTest {
        // These APIs are gated by the request domain whitelist the consumer configures,
        // not by a user permission, so the SDK neither queries nor requests one.
        val permissionHost = FakeWechatPermissionHost()
        val privacyHost = FakeWechatPrivacyHost()
        val host = fakeWechatHost(permissionHost = permissionHost, privacyHost = privacyHost)

        host.networkStatus.current()
        host.platform.uploadFile.start(uploadRequest())
        host.platform.downloadFile.start(downloadRequest())

        assertEquals(0, permissionHost.getSettingCalls)
        assertEquals(0, permissionHost.authorizeCalls)
        assertEquals(0, permissionHost.openSettingCalls)
        assertEquals(0, privacyHost.getPrivacySettingCalls)
        assertEquals(0, privacyHost.authorizeCalls)
    }

    @Test
    fun aNetworkQueryDoesNotTouchTheOtherCapabilities() = runTest {
        val clipboardHost = FakeWechatClipboardHost()
        val networkStatusHost = FakeWechatNetworkStatusHost()
        val uploadHost = FakeWechatUploadFileHost()
        val downloadHost = FakeWechatDownloadFileHost()
        val host = fakeWechatHost(
            clipboardHost = clipboardHost,
            networkStatusHost = networkStatusHost,
            uploadFileHost = uploadHost,
            downloadFileHost = downloadHost,
        )

        host.networkStatus.current()

        assertEquals(1, networkStatusHost.queryCalls)
        assertEquals(0, uploadHost.calls)
        assertEquals(0, downloadHost.calls)
        assertEquals(0, clipboardHost.readCalls)
    }

    @Test
    fun anUploadDoesNotTouchTheDownloadOrTheQuery() = runTest {
        val networkStatusHost = FakeWechatNetworkStatusHost()
        val uploadHost = FakeWechatUploadFileHost()
        val downloadHost = FakeWechatDownloadFileHost()
        val host = fakeWechatHost(
            networkStatusHost = networkStatusHost,
            uploadFileHost = uploadHost,
            downloadFileHost = downloadHost,
        )

        host.platform.uploadFile.start(uploadRequest()).await()

        assertEquals(1, uploadHost.calls)
        assertEquals(0, downloadHost.calls)
        assertEquals(0, networkStatusHost.queryCalls)
    }

    @Test
    fun anUnsupportedNetworkCapabilityFailsBeforeTheHostIsCalled() = runTest {
        val networkStatusHost = FakeWechatNetworkStatusHost(querySupported = false)
        val host = fakeWechatHost(networkStatusHost = networkStatusHost)

        val failure = assertFailsWith<MiniAppException.UnsupportedCapability> {
            host.networkStatus.current()
        }

        assertEquals(MiniAppNetworkStatus.QueryKey, failure.capability)
        assertEquals(0, networkStatusHost.queryCalls)
    }

    @Test
    fun aCollectorEndsWithNoListenerLeftOnTheHost() = runTest {
        // The end-to-end version of the pairing rule, through the real host.
        val networkStatusHost = FakeWechatNetworkStatusHost()
        val host = fakeWechatHost(networkStatusHost = networkStatusHost)

        val job = launch { host.networkStatus.changes.collect { } }
        runCurrent()
        assertEquals(1, networkStatusHost.activeListeners)

        job.cancel()
        advanceUntilIdle()

        assertEquals(0, networkStatusHost.activeListeners)
        assertEquals(1, networkStatusHost.addCalls)
        assertEquals(1, networkStatusHost.removeCalls)
        assertTrue(host.capabilitySupport(MiniAppNetworkStatus.ListenerKey) is CapabilitySupport.Supported)
    }

    private fun uploadRequest() = io.github.bobcgn.miniapp.host.wechat.WeChatUploadRequest(
        url = "https://example.com/upload",
        filePath = "/sandbox/file.txt",
        name = "file",
    )

    private fun downloadRequest() = io.github.bobcgn.miniapp.host.wechat.WeChatDownloadRequest(
        url = "https://example.com/file.bin",
    )
}
