package io.github.bobcgn.miniapp.host.wechat

import io.github.bobcgn.miniapp.capability.CapabilityKey
import io.github.bobcgn.miniapp.capability.CapabilitySupport
import io.github.bobcgn.miniapp.capability.lifecycle.LifecycleCapabilityProvider
import io.github.bobcgn.miniapp.capability.lifecycle.MiniAppLifecycle
import io.github.bobcgn.miniapp.capability.network.MiniAppHttpTransport
import io.github.bobcgn.miniapp.capability.network.NetworkCapabilityProvider
import io.github.bobcgn.miniapp.capability.permission.MiniAppPermissions
import io.github.bobcgn.miniapp.capability.permission.PermissionCapabilityProvider
import io.github.bobcgn.miniapp.capability.privacy.MiniAppPrivacy
import io.github.bobcgn.miniapp.capability.privacy.PrivacyCapabilityProvider
import io.github.bobcgn.miniapp.capability.storage.MiniAppStorage
import io.github.bobcgn.miniapp.capability.storage.StorageCapabilityProvider
import io.github.bobcgn.miniapp.host.HostPlatformApi
import io.github.bobcgn.miniapp.host.MiniAppHost
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatAuth
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatAuthHost
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatChooseMedia
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatChooseMediaHost
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatClipboard
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatClipboardHost
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatFileSystem
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatFileSystemHost
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatHaptics
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatHapticsHost
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatLocation
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatLocationHost
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatNavigation
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatNavigationHost
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatNetwork
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatNetworkHost
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatPermissionHost
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatPermissions
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatPrivacy
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatPrivacyHost
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatRuntimeInfoHost
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatScanCode
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatScanCodeHost
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatStorage
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatStorageHost
import io.github.bobcgn.miniapp.host.wechat.adapter.WxAuthHost
import io.github.bobcgn.miniapp.host.wechat.adapter.WxChooseMediaHost
import io.github.bobcgn.miniapp.host.wechat.adapter.WxClipboardHost
import io.github.bobcgn.miniapp.host.wechat.adapter.WxFileSystemHost
import io.github.bobcgn.miniapp.host.wechat.adapter.WxHapticsHost
import io.github.bobcgn.miniapp.host.wechat.adapter.WxLocationHost
import io.github.bobcgn.miniapp.host.wechat.adapter.WxNavigationHost
import io.github.bobcgn.miniapp.host.wechat.adapter.WxNetworkHost
import io.github.bobcgn.miniapp.host.wechat.adapter.WxPermissionHost
import io.github.bobcgn.miniapp.host.wechat.adapter.WxPrivacyHost
import io.github.bobcgn.miniapp.host.wechat.adapter.WxRuntimeInfoHost
import io.github.bobcgn.miniapp.host.wechat.adapter.WxScanCodeHost
import io.github.bobcgn.miniapp.host.wechat.adapter.WxStorageHost
import io.github.bobcgn.miniapp.host.wechat.runtime.WechatAppLifecycle
import io.github.bobcgn.miniapp.host.wechat.runtime.WechatCapabilityGate
import io.github.bobcgn.miniapp.host.wechat.runtime.WechatPageLifecycle
import io.github.bobcgn.miniapp.host.wechat.runtime.WechatRuntimeInfo

/**
 * Typed escape hatch for WeChat-only APIs that do not form common capabilities.
 *
 * A page stack and a page route exist in a DSL mini-program runtime and have no
 * equivalent in a WebView-based host, so navigation and page lifecycle stay here.
 * The app-level lifecycle is not host-specific and is therefore a common
 * capability; only the WeChat hooks that *drive* it appear here.
 */
internal class WechatPlatformApi(
    /** WeChat-specific client authentication bootstrap. */
    internal val auth: WechatAuth,
    /** WeChat page-stack navigation. */
    internal val navigation: WechatNavigation,
    /** WeChat App hooks that drive the common lifecycle capability. */
    internal val appLifecycle: WechatAppLifecycle,
    /** WeChat page-level lifecycle, which is not a common concept. */
    internal val pageLifecycle: WechatPageLifecycle,
    /** What this runtime reports about itself, including the base-library version. */
    internal val runtimeInfo: WechatRuntimeInfo,
    /** WeChat system clipboard access. */
    internal val clipboard: WechatClipboard,
    /** WeChat short and long vibrations. */
    internal val haptics: WechatHaptics,
    /** WeChat file-sandbox access. */
    internal val fileSystem: WechatFileSystem,
    /** WeChat position access. */
    internal val location: WechatLocation,
    /** Scanning through WeChat's own scanning interface. */
    internal val scanCode: WechatScanCode,
    /** Choosing images or videos through WeChat's own picker. */
    internal val chooseMedia: WechatChooseMedia,
) : HostPlatformApi

/** First concrete [MiniAppHost], backed by the WeChat Mini Program runtime. */
internal class WechatHost(
    storageHost: WechatStorageHost = WxStorageHost,
    authHost: WechatAuthHost = WxAuthHost,
    networkHost: WechatNetworkHost = WxNetworkHost,
    navigationHost: WechatNavigationHost = WxNavigationHost,
    runtimeInfoHost: WechatRuntimeInfoHost = WxRuntimeInfoHost,
    permissionHost: WechatPermissionHost = WxPermissionHost,
    privacyHost: WechatPrivacyHost = WxPrivacyHost,
    clipboardHost: WechatClipboardHost = WxClipboardHost,
    hapticsHost: WechatHapticsHost = WxHapticsHost,
    fileSystemHost: WechatFileSystemHost = WxFileSystemHost,
    locationHost: WechatLocationHost = WxLocationHost,
    scanCodeHost: WechatScanCodeHost = WxScanCodeHost,
    chooseMediaHost: WechatChooseMediaHost = WxChooseMediaHost,
) : MiniAppHost<WechatPlatformApi>,
    StorageCapabilityProvider,
    NetworkCapabilityProvider,
    LifecycleCapabilityProvider,
    PermissionCapabilityProvider,
    PrivacyCapabilityProvider {
    private val appLifecycle: WechatAppLifecycle = WechatAppLifecycle()

    private val runtimeInfo: WechatRuntimeInfo = WechatRuntimeInfo(runtimeInfoHost)

    private val capabilityGate: WechatCapabilityGate =
        WechatCapabilityGate(runtimeInfo = runtimeInfo, runtimeHost = runtimeInfoHost)

    // One privacy instance, because the location adapter enforces the privacy
    // precondition on the same state the caller queries through the capability.
    private val privacyAdapter: WechatPrivacy = WechatPrivacy(privacyHost)

    // One permission instance, so location observes the same live host state the
    // public permission capability reports and never starts a prompt itself.
    private val permissionsAdapter: WechatPermissions = WechatPermissions(permissionHost)

    override val platform: WechatPlatformApi = WechatPlatformApi(
        auth = WechatAuth(authHost),
        navigation = WechatNavigation(navigationHost),
        appLifecycle = appLifecycle,
        pageLifecycle = WechatPageLifecycle(),
        runtimeInfo = runtimeInfo,
        clipboard = WechatClipboard(clipboardHost),
        haptics = WechatHaptics(hapticsHost),
        fileSystem = WechatFileSystem(fileSystemHost),
        location = WechatLocation(locationHost, privacyAdapter, permissionsAdapter),
        scanCode = WechatScanCode(scanCodeHost),
        chooseMedia = WechatChooseMedia(chooseMediaHost),
    )

    override val storage: MiniAppStorage = WechatStorage(storageHost)

    override val network: MiniAppHttpTransport = WechatNetwork(networkHost)

    override val lifecycle: MiniAppLifecycle = appLifecycle

    override val permissions: MiniAppPermissions = permissionsAdapter

    override val privacy: MiniAppPrivacy = privacyAdapter

    override fun capabilitySupport(capability: CapabilityKey): CapabilitySupport =
        capabilityGate.supportFor(capability)
}
