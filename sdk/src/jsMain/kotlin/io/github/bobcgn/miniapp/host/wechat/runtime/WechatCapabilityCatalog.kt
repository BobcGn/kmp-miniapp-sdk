package io.github.bobcgn.miniapp.host.wechat.runtime

import io.github.bobcgn.miniapp.capability.CapabilityKey
import io.github.bobcgn.miniapp.capability.lifecycle.MiniAppLifecycle
import io.github.bobcgn.miniapp.capability.network.MiniAppHttpTransport
import io.github.bobcgn.miniapp.capability.permission.MiniAppPermissions
import io.github.bobcgn.miniapp.capability.privacy.MiniAppPrivacy
import io.github.bobcgn.miniapp.capability.storage.MiniAppStorage
import io.github.bobcgn.miniapp.host.wechat.WeChatDeviceCapabilities
import io.github.bobcgn.miniapp.host.wechat.WeChatSessionState
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatRuntimeInfoHost
import io.github.bobcgn.miniapp.host.HostVersion

/**
 * What the SDK needs from a WeChat base library for one capability.
 *
 * @property canIUseSchemas schemas every one of which `wx.canIUse` must confirm;
 *   empty when the capability has no host API to probe
 * @property minimumBaseLibraryVersion oldest base library that provides the
 *   capability, or `null` when availability is settled by [canIUseSchemas] alone
 * @property presence an additional probe for a capability `wx.canIUse` cannot
 *   express, such as a method on a host object rather than on `wx` itself, or
 *   `null` when the schemas and version are enough
 */
internal class WechatCapabilityRequirement(
    val canIUseSchemas: List<String> = emptyList(),
    val minimumBaseLibraryVersion: HostVersion? = null,
    val presence: ((WechatRuntimeInfoHost) -> Boolean)? = null,
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

    /**
     * Oldest base library WeChat documents for `wx.getFileSystemManager` and the
     * read, write, access, and unlink methods this SDK uses.
     *
     * https://developers.weixin.qq.com/miniprogram/dev/api/file/wx.getFileSystemManager.html
     */
    private val FILESYSTEM_MINIMUM: HostVersion = requireNotNull(HostVersion.parse("1.9.9")) {
        "The recorded minimum base-library version must be a dotted numeric version"
    }

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
        // Session checking is a WeChat condition rather than something every host
        // can be asked for, so its key is namespaced. No minimum base library is
        // recorded: WeChat's page for this API states none, and this repository does
        // not infer one, so the probe below is the authority.
        WeChatSessionState.Key to WechatCapabilityRequirement(
            canIUseSchemas = listOf("checkSession"),
        ),
        // The file manager exposes four operations, and a host may offer any
        // subset: the manager existing does not mean each method does. Each entry
        // probes its own method, which `wx.canIUse` cannot express because these
        // live on a host object rather than on `wx` itself. WeChat documents 1.9.9
        // for the manager and its read, write, access, and unlink methods.
        WeChatDeviceCapabilities.FileSystemRead to WechatCapabilityRequirement(
            minimumBaseLibraryVersion = FILESYSTEM_MINIMUM,
            presence = { host -> host.hasFileSystemMethod("readFile") },
        ),
        WeChatDeviceCapabilities.FileSystemWrite to WechatCapabilityRequirement(
            minimumBaseLibraryVersion = FILESYSTEM_MINIMUM,
            presence = { host -> host.hasFileSystemMethod("writeFile") },
        ),
        WeChatDeviceCapabilities.FileSystemAccess to WechatCapabilityRequirement(
            minimumBaseLibraryVersion = FILESYSTEM_MINIMUM,
            presence = { host -> host.hasFileSystemMethod("access") },
        ),
        WeChatDeviceCapabilities.FileSystemRemove to WechatCapabilityRequirement(
            minimumBaseLibraryVersion = FILESYSTEM_MINIMUM,
            presence = { host -> host.hasFileSystemMethod("unlink") },
        ),
        // The sandbox root is a separate host value from the manager, so it is
        // gated on its own rather than assumed to accompany the four operations.
        WeChatDeviceCapabilities.FileSystemSandboxPath to WechatCapabilityRequirement(
            minimumBaseLibraryVersion = FILESYSTEM_MINIMUM,
            presence = { host -> host.hasUserDataPath() },
        ),
        // WeChat documents no introduction version for getLocation itself, so no
        // number is recorded and the probe is the authority; a version figure the
        // repository cannot cite would be a guess. Permission and privacy are
        // separate questions and are not answered here.
        WeChatDeviceCapabilities.Location to WechatCapabilityRequirement(
            canIUseSchemas = listOf("getLocation"),
        ),
        // WeChat documents no introduction version for scanCode either, so no
        // number is recorded and the probe is the authority. The API needs no
        // permission the SDK could establish, so nothing else is gated here.
        WeChatDeviceCapabilities.ScanCode to WechatCapabilityRequirement(
            canIUseSchemas = listOf("scanCode"),
        ),
        // WeChat documents no introduction version for chooseMedia either, so no
        // number is recorded and the probe is the authority. Reading the media
        // library needs no permission the host ties to this API, so nothing else is
        // gated here.
        WeChatDeviceCapabilities.ChooseMedia to WechatCapabilityRequirement(
            canIUseSchemas = listOf("chooseMedia"),
        ),
        // WeChat documents no introduction version for requestSubscribeMessage either,
        // so no number is recorded and the probe is the authority. The offline sources
        // for this API name no permission scope for it, so nothing else is gated here.
        WeChatDeviceCapabilities.RequestSubscribeMessage to WechatCapabilityRequirement(
            canIUseSchemas = listOf("requestSubscribeMessage"),
        ),
        // The four device capabilities are gated separately: a host may expose one
        // clipboard direction or one vibration length without the other. WeChat
        // documents 1.1.0 for the clipboard APIs and 1.2.0 for the vibration APIs,
        // and canIUse is probed as well because a version figure alone does not
        // prove the function exists.
        WeChatDeviceCapabilities.ClipboardRead to WechatCapabilityRequirement(
            canIUseSchemas = listOf("getClipboardData"),
            // https://developers.weixin.qq.com/miniprogram/dev/api/device/clipboard/wx.getClipboardData.html
            minimumBaseLibraryVersion = requireNotNull(HostVersion.parse("1.1.0")) {
                "The recorded minimum base-library version must be a dotted numeric version"
            },
        ),
        WeChatDeviceCapabilities.ClipboardWrite to WechatCapabilityRequirement(
            canIUseSchemas = listOf("setClipboardData"),
            // https://developers.weixin.qq.com/miniprogram/dev/api/device/clipboard/wx.setClipboardData.html
            minimumBaseLibraryVersion = requireNotNull(HostVersion.parse("1.1.0")) {
                "The recorded minimum base-library version must be a dotted numeric version"
            },
        ),
        WeChatDeviceCapabilities.VibrateShort to WechatCapabilityRequirement(
            canIUseSchemas = listOf("vibrateShort"),
            // https://developers.weixin.qq.com/miniprogram/dev/api/device/vibrate/wx.vibrateShort.html
            minimumBaseLibraryVersion = requireNotNull(HostVersion.parse("1.2.0")) {
                "The recorded minimum base-library version must be a dotted numeric version"
            },
        ),
        WeChatDeviceCapabilities.VibrateLong to WechatCapabilityRequirement(
            canIUseSchemas = listOf("vibrateLong"),
            // https://developers.weixin.qq.com/miniprogram/dev/api/device/vibrate/wx.vibrateLong.html
            minimumBaseLibraryVersion = requireNotNull(HostVersion.parse("1.2.0")) {
                "The recorded minimum base-library version must be a dotted numeric version"
            },
        ),
        // WeChat integrated its privacy APIs in a single base library and does not
        // intercept privacy-gated calls below it, so this is both the documented
        // minimum and a real availability boundary. Both APIs are probed as well,
        // because a version number alone does not prove they exist.
        MiniAppPrivacy.Key to WechatCapabilityRequirement(
            canIUseSchemas = listOf("getPrivacySetting", "requirePrivacyAuthorize"),
            // https://developers.weixin.qq.com/miniprogram/dev/api/open-api/privacy/wx.getPrivacySetting.html
            minimumBaseLibraryVersion = requireNotNull(HostVersion.parse("2.32.3")) {
                "The recorded minimum base-library version must be a dotted numeric version"
            },
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
