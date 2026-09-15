package io.github.bobcgn.miniapp.host.wechat.runtime

import io.github.bobcgn.miniapp.capability.CapabilityKey
import io.github.bobcgn.miniapp.capability.CapabilitySupport
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatRuntimeInfoHost

/**
 * The single place that decides whether WeChat currently provides a capability.
 *
 * The answer comes from what the runtime reports about itself, so one SDK build
 * can answer differently on two hosts. A capability this SDK does not gate is
 * reported unsupported rather than assumed present.
 *
 * The gate is synchronous, which is possible because every WeChat inspection API
 * it needs is synchronous. It never runs during construction: the runtime reading
 * is taken on the first question, and a host that cannot be read at all — a Node
 * process, for instance — answers unsupported instead of throwing.
 */
internal class WechatCapabilityGate(
    private val runtimeInfo: WechatRuntimeInfo,
    private val runtimeHost: WechatRuntimeInfoHost,
) {
    fun supportFor(capability: CapabilityKey): CapabilitySupport {
        val requirement = WechatCapabilityCatalog.requirementFor(capability)
            ?: return CapabilitySupport.Unsupported

        val minimum = requirement.minimumBaseLibraryVersion
        if (minimum != null) {
            val current = runtimeInfo.baseLibraryVersion
            if (current != null && current < minimum) {
                return CapabilitySupport.VersionDependent(
                    requiredVersion = minimum,
                    currentVersion = current,
                )
            }
            // An unreadable version is not evidence of an old host, so it falls
            // through to the capability probe, which is the only other evidence
            // available here.
        }

        val unprobeable = requirement.canIUseSchemas.any { schema -> !runtimeHost.canIUse(schema) }
        if (unprobeable) return CapabilitySupport.Unsupported

        // Some capabilities live on a host object rather than on `wx` itself, so
        // canIUse cannot answer for them; the entry brings its own probe.
        val presence = requirement.presence
        if (presence != null && !presence(runtimeHost)) return CapabilitySupport.Unsupported

        return CapabilitySupport.Supported
    }
}
