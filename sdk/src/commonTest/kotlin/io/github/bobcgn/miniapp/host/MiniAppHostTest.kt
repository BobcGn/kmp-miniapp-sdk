package io.github.bobcgn.miniapp.host

import io.github.bobcgn.miniapp.capability.CapabilityKey
import io.github.bobcgn.miniapp.capability.CapabilitySupport
import io.github.bobcgn.miniapp.capability.lifecycle.MiniAppLifecycle
import io.github.bobcgn.miniapp.capability.network.MiniAppHttpTransport
import io.github.bobcgn.miniapp.capability.permission.MiniAppPermissions
import io.github.bobcgn.miniapp.capability.storage.MiniAppStorage
import io.github.bobcgn.miniapp.testing.CapabilitySupportContractChecks
import io.github.bobcgn.miniapp.testing.FakeMiniAppHost
import io.github.bobcgn.miniapp.testing.FakePlatformApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame

/**
 * The host boundary reads capability support from the host rather than switching
 * on a platform, and exposes the platform escape hatch unchanged.
 *
 * This runs against the FakeHost boundary, so it holds for any host.
 */
public class MiniAppHostTest {
    @Test
    public fun hostReportsCapabilitySupportWithoutPlatformSwitching(): Unit {
        val platformApi = FakePlatformApi()
        val host = FakeMiniAppHost(
            platform = platformApi,
            support = mapOf(CapabilityKey("test.supported") to CapabilitySupport.Supported),
        )

        assertEquals(
            CapabilitySupport.Supported,
            host.capabilitySupport(CapabilityKey("test.supported")),
        )
        assertEquals(
            CapabilitySupport.Unsupported,
            host.capabilitySupport(CapabilityKey("test.unsupported")),
        )
        assertSame(platformApi, host.platform)
    }

    @Test
    public fun defaultFakeHostProvidesTheImplementedCapabilities(): Unit {
        val host = FakeMiniAppHost()

        assertEquals(CapabilitySupport.Supported, host.capabilitySupport(MiniAppStorage.Key))
        assertEquals(CapabilitySupport.Supported, host.capabilitySupport(MiniAppHttpTransport.Key))
        assertEquals(CapabilitySupport.Supported, host.capabilitySupport(MiniAppLifecycle.Key))
        assertEquals(CapabilitySupport.Supported, host.capabilitySupport(MiniAppPermissions.Key))
        assertNotNull(host.storage)
        assertNotNull(host.network)
        assertNotNull(host.lifecycle)
        assertNotNull(host.permissions)
    }

    @Test
    public fun anUngatedCapabilityIsNeverAssumedPresent(): Unit {
        CapabilitySupportContractChecks.anUnknownCapabilityIsUnsupported(FakeMiniAppHost())
    }

    @Test
    public fun aSupportedCapabilitySatisfiesTheRequirementGuard(): Unit {
        CapabilitySupportContractChecks.aSupportedCapabilitySatisfiesTheRequirementGuard(
            host = FakeMiniAppHost(),
            capability = MiniAppStorage.Key,
        )
    }

    @Test
    public fun anUnsupportedCapabilityFailsTheRequirementGuard(): Unit {
        assertRequirementGuardFails(CapabilitySupport.Unsupported)
    }

    @Test
    public fun aVersionDependentCapabilityFailsTheRequirementGuard(): Unit {
        assertRequirementGuardFails(
            CapabilitySupport.VersionDependent(
                requiredVersion = requireNotNull(HostVersion.parse("2.20.1")),
                currentVersion = HostVersion.parse("2.19.4"),
            ),
        )
    }

    @Test
    public fun aPermissionDependentCapabilityFailsTheRequirementGuard(): Unit {
        assertRequirementGuardFails(
            CapabilitySupport.PermissionDependent(permission = "microphone"),
        )
    }

    private fun assertRequirementGuardFails(state: CapabilitySupport): Unit {
        CapabilitySupportContractChecks.aNonSupportedCapabilityFailsTheRequirementGuard(
            host = FakeMiniAppHost(support = mapOf(MiniAppStorage.Key to state)),
            capability = MiniAppStorage.Key,
        )
    }
}
