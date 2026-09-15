package io.github.bobcgn.miniapp.testing

import io.github.bobcgn.miniapp.capability.CapabilityKey
import io.github.bobcgn.miniapp.capability.CapabilitySupport
import io.github.bobcgn.miniapp.capability.lifecycle.LifecycleCapabilityProvider
import io.github.bobcgn.miniapp.capability.lifecycle.MiniAppLifecycle
import io.github.bobcgn.miniapp.capability.network.MiniAppHttpTransport
import io.github.bobcgn.miniapp.capability.network.NetworkCapabilityProvider
import io.github.bobcgn.miniapp.capability.storage.MiniAppStorage
import io.github.bobcgn.miniapp.capability.storage.StorageCapabilityProvider
import io.github.bobcgn.miniapp.host.HostPlatformApi
import io.github.bobcgn.miniapp.host.MiniAppHost

/** Host-specific escape hatch for the fake host; it carries no behavior. */
internal class FakePlatformApi : HostPlatformApi

/**
 * The FakeHost boundary: a host-neutral [MiniAppHost] backed entirely by
 * in-memory implementations.
 *
 * Shared code and capability contracts are tested against this instead of the
 * WeChat host, so a failing contract check identifies the contract rather than
 * the active runtime. Host adapters are then verified separately against the
 * same contracts through their own callback ports.
 *
 * @param platform escape hatch instance, exposed unchanged so tests can assert identity
 * @param storage capability facet this host provides
 * @param network capability facet this host provides
 * @param lifecycle capability facet this host provides
 * @param supported capability keys this host reports as [CapabilitySupport.Supported]
 */
internal class FakeMiniAppHost(
    override val platform: FakePlatformApi = FakePlatformApi(),
    override val storage: MiniAppStorage = InMemoryStorage(),
    override val network: MiniAppHttpTransport = RecordingHttpTransport(),
    override val lifecycle: MiniAppLifecycle = FakeMiniAppLifecycle(),
    private val supported: Set<CapabilityKey> = setOf(
        MiniAppStorage.Key,
        MiniAppHttpTransport.Key,
        MiniAppLifecycle.Key,
    ),
) : MiniAppHost<FakePlatformApi>,
    StorageCapabilityProvider,
    NetworkCapabilityProvider,
    LifecycleCapabilityProvider {
    override fun capabilitySupport(capability: CapabilityKey): CapabilitySupport =
        if (capability in supported) {
            CapabilitySupport.Supported
        } else {
            CapabilitySupport.Unsupported
        }
}
