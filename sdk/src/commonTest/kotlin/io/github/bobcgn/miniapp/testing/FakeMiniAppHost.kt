package io.github.bobcgn.miniapp.testing

import io.github.bobcgn.miniapp.capability.CapabilityKey
import io.github.bobcgn.miniapp.capability.CapabilitySupport
import io.github.bobcgn.miniapp.capability.lifecycle.LifecycleCapabilityProvider
import io.github.bobcgn.miniapp.capability.lifecycle.MiniAppLifecycle
import io.github.bobcgn.miniapp.capability.network.MiniAppHttpTransport
import io.github.bobcgn.miniapp.capability.network.NetworkCapabilityProvider
import io.github.bobcgn.miniapp.capability.permission.MiniAppPermissions
import io.github.bobcgn.miniapp.capability.permission.PermissionCapabilityProvider
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
 * A real host answers capability support by inspecting its runtime, so this fake
 * takes the answer directly. That is what lets a test reproduce every support
 * state — including the ones WeChat does not produce yet — without a runtime.
 *
 * @param platform escape hatch instance, exposed unchanged so tests can assert identity
 * @param storage capability facet this host provides
 * @param network capability facet this host provides
 * @param lifecycle capability facet this host provides
 * @param permissions capability facet this host provides
 * @param support the support state reported for each key; unlisted keys are unsupported
 */
internal class FakeMiniAppHost(
    override val platform: FakePlatformApi = FakePlatformApi(),
    override val storage: MiniAppStorage = InMemoryStorage(),
    override val network: MiniAppHttpTransport = RecordingHttpTransport(),
    override val lifecycle: MiniAppLifecycle = FakeMiniAppLifecycle(),
    override val permissions: MiniAppPermissions = FakeMiniAppPermissions(),
    private val support: Map<CapabilityKey, CapabilitySupport> = mapOf(
        MiniAppStorage.Key to CapabilitySupport.Supported,
        MiniAppHttpTransport.Key to CapabilitySupport.Supported,
        MiniAppLifecycle.Key to CapabilitySupport.Supported,
        MiniAppPermissions.Key to CapabilitySupport.Supported,
    ),
) : MiniAppHost<FakePlatformApi>,
    StorageCapabilityProvider,
    NetworkCapabilityProvider,
    LifecycleCapabilityProvider,
    PermissionCapabilityProvider {
    override fun capabilitySupport(capability: CapabilityKey): CapabilitySupport =
        support[capability] ?: CapabilitySupport.Unsupported
}
