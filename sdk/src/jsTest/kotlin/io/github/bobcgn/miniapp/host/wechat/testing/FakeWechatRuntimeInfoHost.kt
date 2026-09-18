package io.github.bobcgn.miniapp.host.wechat.testing

import io.github.bobcgn.miniapp.host.wechat.adapter.WechatRuntimeInfoHost

/**
 * The FakeAdapter boundary for runtime inspection: a stand-in for what WeChat
 * reports about itself.
 *
 * It implements the raw inspection port, so a test can drive the version gate
 * without a WeChat runtime and can reproduce conditions a real host rarely
 * presents, such as a base library that cannot report its version at all.
 *
 * @param baseLibraryVersion version the host reports, or `null` when it cannot
 *   report one
 * @param platform runtime platform the host reports, or `null`
 * @param availableSchemas schemas `canIUse` confirms; every other schema is refused
 * @param canIUseAvailable whether the host can be asked at all, which is false on a
 *   base library older than the one that introduced `canIUse`
 * @param availableFileSystemMethods file-manager methods this host exposes, so a
 *   test can reproduce a host that offers only some of them
 * @param userDataPathAvailable whether this host reports a sandbox root
 */
internal class FakeWechatRuntimeInfoHost(
    private val baseLibraryVersion: String? = "3.17.3",
    private val platform: String? = "devtools",
    private val availableSchemas: Set<String> = setOf(
        "getStorage",
        "setStorage",
        "removeStorage",
        "request",
        "getAppBaseInfo",
        "getSetting",
        "authorize",
        "openSetting",
        "getPrivacySetting",
        "requirePrivacyAuthorize",
        "checkSession",
        "getClipboardData",
        "setClipboardData",
        "vibrateShort",
        "vibrateLong",
        "getFileSystemManager",
        "getLocation",
        "scanCode",
        "chooseMedia",
        "requestSubscribeMessage",
        "getNetworkType",
        "onNetworkStatusChange",
        "offNetworkStatusChange",
        "uploadFile",
        "downloadFile",
        "requestPayment",
    ),
    private val canIUseAvailable: Boolean = true,
    private val availableFileSystemMethods: Set<String> = setOf(
        "readFile",
        "writeFile",
        "access",
        "unlink",
    ),
    private val userDataPathAvailable: Boolean = true,
    private val bluetoothAdapterAvailable: Boolean = true,
    private val bluetoothDiscoveryAvailable: Boolean = true,
    private val bluetoothConnectionAvailable: Boolean = true,
) : WechatRuntimeInfoHost {
    /** How many times the version was actually read, to prove it is read once. */
    var baseLibraryVersionReads: Int = 0
        private set

    override fun baseLibraryVersion(): String? {
        baseLibraryVersionReads += 1
        return baseLibraryVersion
    }

    override fun platform(): String? = platform

    override fun canIUse(schema: String): Boolean = canIUseAvailable && schema in availableSchemas

    override fun hasFileSystemMethod(method: String): Boolean =
        canIUseAvailable && method in availableFileSystemMethods

    override fun hasUserDataPath(): Boolean = canIUseAvailable && userDataPathAvailable

    override fun hasBluetoothAdapter(): Boolean = bluetoothAdapterAvailable

    override fun hasBluetoothDiscovery(): Boolean = bluetoothDiscoveryAvailable

    override fun hasBluetoothConnection(): Boolean = bluetoothConnectionAvailable
}
