package io.github.bobcgn.miniapp.host.wechat.runtime

import io.github.bobcgn.miniapp.capability.CapabilityKey
import io.github.bobcgn.miniapp.capability.CapabilitySupport
import io.github.bobcgn.miniapp.capability.lifecycle.MiniAppLifecycle
import io.github.bobcgn.miniapp.capability.network.MiniAppHttpTransport
import io.github.bobcgn.miniapp.capability.storage.MiniAppStorage
import io.github.bobcgn.miniapp.host.HostVersion
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatRuntimeInfoHost
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * How the WeChat gate turns what a runtime reports into a support state.
 *
 * These are the version- and capability-probe rules that make
 * `WechatCapabilityGate` the single place deciding support; the shared contract
 * guarantees are covered separately by `WechatDetectionContractTest`.
 */
internal class WechatCapabilityGateTest {
    @Test
    fun aGatedCapabilityOnANewEnoughRuntimeIsSupported() {
        val gate = gateWith(FakeWechatRuntimeInfoHost(baseLibraryVersion = "3.17.3"))

        assertEquals(CapabilitySupport.Supported, gate.supportFor(MiniAppStorage.Key))
        assertEquals(CapabilitySupport.Supported, gate.supportFor(MiniAppHttpTransport.Key))
        assertEquals(CapabilitySupport.Supported, gate.supportFor(MiniAppLifecycle.Key))
    }

    @Test
    fun aCapabilityTheRuntimeDoesNotProvideIsUnsupported() {
        val host = FakeWechatRuntimeInfoHost(
            availableSchemas = setOf("getStorage", "getAppBaseInfo"),
        )
        val gate = gateWith(host)

        // The runtime answered, and its answer was no.
        assertEquals(CapabilitySupport.Unsupported, gate.supportFor(MiniAppHttpTransport.Key))
    }

    @Test
    fun aRuntimeOlderThanTheRequiredVersionIsVersionDependent() {
        val host = FakeWechatRuntimeInfoHost(
            baseLibraryVersion = "2.19.4",
            // canIUse exists here, but correctly reports that getAppBaseInfo is
            // absent before its documented 2.20.1 boundary.
            availableSchemas = emptySet(),
        )
        val gate = gateWith(host)

        val support = gate.supportFor(WechatCapabilityCatalog.RuntimeDetectionKey)

        val dependent = assertIs<CapabilitySupport.VersionDependent>(support)
        assertEquals(version("2.20.1"), dependent.requiredVersion)
        assertEquals(version("2.19.4"), dependent.currentVersion)
    }

    @Test
    fun aRuntimeAtTheRequiredVersionIsSupportedRatherThanDependent() {
        val versionDependentRuntime = FakeWechatRuntimeInfoHost(baseLibraryVersion = "2.20.1")
        val newerRuntime = FakeWechatRuntimeInfoHost(baseLibraryVersion = "3.0.0")

        // The boundary itself must not be off by one.
        assertEquals(
            CapabilitySupport.Supported,
            gateWith(versionDependentRuntime).supportFor(WechatCapabilityCatalog.RuntimeDetectionKey),
        )
        assertEquals(
            CapabilitySupport.Supported,
            gateWith(newerRuntime).supportFor(WechatCapabilityCatalog.RuntimeDetectionKey),
        )
    }

    @Test
    fun aCapabilityThisSdkDoesNotGateIsUnsupported() {
        val gate = gateWith(FakeWechatRuntimeInfoHost())

        assertEquals(
            CapabilitySupport.Unsupported,
            gate.supportFor(CapabilityKey("capability-this-sdk-does-not-gate")),
        )
    }

    @Test
    fun anUnreadableVersionFallsBackToTheCapabilityProbe() {
        val readableApis = FakeWechatRuntimeInfoHost(baseLibraryVersion = null)
        val unreadableApis = FakeWechatRuntimeInfoHost(
            baseLibraryVersion = null,
            canIUseAvailable = false,
        )

        // An unreadable version is not evidence of an old host, so the probe decides.
        assertEquals(
            CapabilitySupport.Supported,
            gateWith(readableApis).supportFor(WechatCapabilityCatalog.RuntimeDetectionKey),
        )
        assertEquals(
            CapabilitySupport.Unsupported,
            gateWith(unreadableApis).supportFor(WechatCapabilityCatalog.RuntimeDetectionKey),
        )
    }

    @Test
    fun theRuntimeVersionIsReadOnlyOnce() {
        val host = FakeWechatRuntimeInfoHost()
        val gate = gateWith(host)

        gate.supportFor(MiniAppStorage.Key)
        gate.supportFor(MiniAppHttpTransport.Key)
        gate.supportFor(WechatCapabilityCatalog.RuntimeDetectionKey)

        assertEquals(1, host.baseLibraryVersionReads)
    }

    @Test
    fun constructingTheRuntimeDoesNotReadTheHost() {
        val host = FakeWechatRuntimeInfoHost()

        // The export facade builds the host while the module loads, and a Node
        // process has no wx to read, so nothing may be probed up front.
        WechatRuntimeInfo(host)

        assertEquals(0, host.baseLibraryVersionReads)
    }

    @Test
    fun theRuntimeReportsWhatTheHostSaidOnceAsked() {
        val runtime = WechatRuntimeInfo(
            FakeWechatRuntimeInfoHost(baseLibraryVersion = "3.17.3", platform = "devtools"),
        )

        assertEquals(version("3.17.3"), runtime.baseLibraryVersion)
        assertEquals("devtools", runtime.platform)
        assertTrue(runtime.isDeveloperTools)
    }

    @Test
    fun aDeviceRuntimeIsNotDeveloperTools() {
        val runtime = WechatRuntimeInfo(FakeWechatRuntimeInfoHost(platform = "android"))

        assertFalse(runtime.isDeveloperTools)
    }

    private fun gateWith(host: FakeWechatRuntimeInfoHost): WechatCapabilityGate =
        WechatCapabilityGate(runtimeInfo = WechatRuntimeInfo(host), runtimeHost = host)

    private fun version(raw: String): HostVersion = requireNotNull(HostVersion.parse(raw))
}
