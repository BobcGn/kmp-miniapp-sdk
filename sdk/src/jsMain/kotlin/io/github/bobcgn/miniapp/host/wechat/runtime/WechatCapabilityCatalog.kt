package io.github.bobcgn.miniapp.host.wechat.runtime

import io.github.bobcgn.miniapp.capability.CapabilityKey
import io.github.bobcgn.miniapp.capability.lifecycle.MiniAppLifecycle
import io.github.bobcgn.miniapp.capability.network.MiniAppHttpTransport
import io.github.bobcgn.miniapp.capability.permission.MiniAppPermissions
import io.github.bobcgn.miniapp.capability.storage.MiniAppStorage
import io.github.bobcgn.miniapp.host.HostVersion

/**
 * What the SDK needs from a WeChat base library for one capability.
 *
 * @property canIUseSchemas schemas every one of which `wx.canIUse` must confirm;
 *   empty when the capability has no host API to probe
 * @property minimumBaseLibraryVersion oldest base library that provides the
 *   capability, or `null` when availability is settled by [canIUseSchemas] alone
 */
internal class WechatCapabilityRequirement(
    val canIUseSchemas: List<String> = emptyList(),
    val minimumBaseLibraryVersion: HostVersion? = null,
)

/**
 * The WeChat capabilities this SDK gates, and what each one needs.
 *
 * A capability that is not listed here is reported unsupported. A later WeChat
 * capability adds its entry when it is implemented, so no key is invented for a
 * capability that does not exist yet.
 *
 * Minimum versions are recorded only where the required base library is known
 * from WeChat's own documentation. Where it is not, the entry relies on
 * `wx.canIUse`, which answers for the host actually running rather than for a
 * figure copied from a table.
 */
internal object WechatCapabilityCatalog {
    /**
     * Identifies the runtime-inspection capability itself.
     *
     * It is WeChat-specific rather than a common capability, and it exists so the
     * SDK's own version gate can be observed and verified. It is the only entry
     * with a known minimum: 2.20.1 is the documented introduction version of the
     * modern `wx.getAppBaseInfo` path. The legacy `wx.getSystemInfoSync` fallback
     * lets an older host report which side of that boundary it is on.
     */
    val RuntimeDetectionKey: CapabilityKey = CapabilityKey("wechat.runtime-detection")

    private val requirements: Map<CapabilityKey, WechatCapabilityRequirement> = mapOf(
        MiniAppStorage.Key to WechatCapabilityRequirement(
            canIUseSchemas = listOf("getStorage", "setStorage", "removeStorage"),
        ),
        MiniAppHttpTransport.Key to WechatCapabilityRequirement(
            canIUseSchemas = listOf("request"),
        ),
        // The app lifecycle is driven by hooks the consumer forwards, so there is
        // no host API to probe and no base library that lacks it.
        MiniAppLifecycle.Key to WechatCapabilityRequirement(),
        // Whether the permission lifecycle works depends on all three APIs being
        // present, so all three are probed. A capability that *needs* a permission
        // is a different question and is not answered here.
        MiniAppPermissions.Key to WechatCapabilityRequirement(
            canIUseSchemas = listOf("getSetting", "authorize", "openSetting"),
        ),
        RuntimeDetectionKey to WechatCapabilityRequirement(
            canIUseSchemas = listOf("getAppBaseInfo"),
            // https://developers.weixin.qq.com/miniprogram/dev/api/base/system/wx.getAppBaseInfo.html
            minimumBaseLibraryVersion = requireNotNull(HostVersion.parse("2.20.1")) {
                "The recorded minimum base-library version must be a dotted numeric version"
            },
        ),
    )

    /** Returns the requirement for [key], or `null` when this SDK does not gate it. */
    fun requirementFor(key: CapabilityKey): WechatCapabilityRequirement? = requirements[key]
}
