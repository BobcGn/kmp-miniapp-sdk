package io.github.bobcgn.miniapp.host.wechat.runtime

import io.github.bobcgn.miniapp.capability.storage.MiniAppStorage
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatRuntimeInfoHost
import io.github.bobcgn.miniapp.host.wechat.testing.fakeWechatHost
import io.github.bobcgn.miniapp.testing.CapabilitySupportContractChecks
import kotlin.test.Test

/**
 * Runs the shared capability-support contract against the WeChat host.
 *
 * These are the same checks `MiniAppHostTest` runs against the host-neutral
 * FakeHost. Passing here means the real `WechatHost` answers support through the
 * gate rather than from a hardcoded list; it is not real-host evidence, because
 * the runtime port is a fake.
 */
internal class WechatDetectionContractTest {
    @Test
    fun anUngatedCapabilityIsNeverAssumedPresent() {
        CapabilitySupportContractChecks.anUnknownCapabilityIsUnsupported(fakeWechatHost())
    }

    @Test
    fun aSupportedCapabilitySatisfiesTheRequirementGuard() {
        CapabilitySupportContractChecks.aSupportedCapabilitySatisfiesTheRequirementGuard(
            host = fakeWechatHost(),
            capability = MiniAppStorage.Key,
        )
    }

    @Test
    fun aCapabilityTheRuntimeRefusesFailsTheRequirementGuard() {
        CapabilitySupportContractChecks.aNonSupportedCapabilityFailsTheRequirementGuard(
            host = fakeWechatHost(
                runtimeInfoHost = FakeWechatRuntimeInfoHost(availableSchemas = emptySet()),
            ),
            capability = MiniAppStorage.Key,
        )
    }
}
