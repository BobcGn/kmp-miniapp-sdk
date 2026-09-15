package io.github.bobcgn.miniapp.testing

import io.github.bobcgn.miniapp.capability.CapabilityKey
import io.github.bobcgn.miniapp.capability.CapabilitySupport
import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.MiniAppHost
import io.github.bobcgn.miniapp.host.requireSupported
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * The capability-support contract, expressed once for every host.
 *
 * What a host *answers* depends on its runtime, which is why the checks take the
 * capability to exercise rather than asserting one particular state. What must
 * hold everywhere is that an ungated capability is never assumed present, and
 * that every state other than `Supported` fails the requirement guard instead of
 * handing the caller something unusable.
 *
 * The WeChat host runs the same checks in `WechatRuntimeDetectionContractTest`.
 */
internal object CapabilitySupportContractChecks {
    /** A capability the host does not gate is reported unsupported, never assumed present. */
    fun anUnknownCapabilityIsUnsupported(host: MiniAppHost<*>) {
        assertEquals(
            CapabilitySupport.Unsupported,
            host.capabilitySupport(CapabilityKey("capability-this-host-does-not-gate")),
        )
    }

    /** A supported capability is reported as such and passes the requirement guard. */
    fun aSupportedCapabilitySatisfiesTheRequirementGuard(
        host: MiniAppHost<*>,
        capability: CapabilityKey,
    ) {
        assertEquals(CapabilitySupport.Supported, host.capabilitySupport(capability))

        host.requireSupported(capability)
    }

    /** Any state other than supported fails the guard, whatever the reason is. */
    fun aNonSupportedCapabilityFailsTheRequirementGuard(
        host: MiniAppHost<*>,
        capability: CapabilityKey,
    ) {
        assertFailsWith<MiniAppException.UnsupportedCapability> {
            host.requireSupported(capability)
        }
    }
}
