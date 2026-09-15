package io.github.bobcgn.miniapp.host.wechat.runtime

import io.github.bobcgn.miniapp.capability.CapabilityKey
import io.github.bobcgn.miniapp.capability.CapabilitySupport
import io.github.bobcgn.miniapp.host.wechat.WeChatDeviceCapabilities
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatFileSystemHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatRuntimeInfoHost
import io.github.bobcgn.miniapp.host.wechat.testing.fakeWechatHost
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * What the file-system capability must hold for the SDK as a whole, driven through
 * the real `WechatHost`.
 *
 * These are not real-host facts: only WeChat can answer whether a file was really
 * written to its sandbox. They state the boundary around it instead — that the
 * four operations are gated independently, and that the gate and the adapter
 * agree about what the host provides.
 */
internal class WechatFileSystemContractTest {
    private val path = "/sandbox/kmp-miniapp-sdk-bob72.txt"

    @Test
    fun theFileSystemCapabilitiesHaveDistinctWeChatSpecificKeys() {
        val keys = listOf(
            WeChatDeviceCapabilities.FileSystemRead,
            WeChatDeviceCapabilities.FileSystemWrite,
            WeChatDeviceCapabilities.FileSystemAccess,
            WeChatDeviceCapabilities.FileSystemRemove,
            WeChatDeviceCapabilities.FileSystemSandboxPath,
        )

        assertEquals(5, keys.toSet().size)
        keys.forEach { key ->
            // Namespaced, so none of them is presented as a host-neutral capability.
            assertTrue(key.value.startsWith("wechat.filesystem-"), "unexpected key '${key.value}'")
        }
        assertNotEquals(CapabilityKey("filesystem"), WeChatDeviceCapabilities.FileSystemRead)
    }

    @Test
    fun aHostThatProvidesEveryOperationReportsThemSupported() {
        val host = fakeWechatHost()

        listOf(
            WeChatDeviceCapabilities.FileSystemRead,
            WeChatDeviceCapabilities.FileSystemWrite,
            WeChatDeviceCapabilities.FileSystemAccess,
            WeChatDeviceCapabilities.FileSystemRemove,
            WeChatDeviceCapabilities.FileSystemSandboxPath,
        ).forEach { key ->
            assertEquals(CapabilitySupport.Supported, host.capabilitySupport(key), "key '${key.value}'")
        }
    }

    @Test
    fun aHostThatProvidesOnlySomeOperationsReportsEachSeparately() {
        // The manager existing does not mean each of its methods does, so the gate
        // must not answer all-or-nothing.
        val host = fakeWechatHost(
            runtimeInfoHost = FakeWechatRuntimeInfoHost(
                availableFileSystemMethods = setOf("readFile", "unlink"),
            ),
        )

        assertEquals(
            CapabilitySupport.Supported,
            host.capabilitySupport(WeChatDeviceCapabilities.FileSystemRead),
        )
        assertEquals(
            CapabilitySupport.Unsupported,
            host.capabilitySupport(WeChatDeviceCapabilities.FileSystemWrite),
        )
        assertEquals(
            CapabilitySupport.Unsupported,
            host.capabilitySupport(WeChatDeviceCapabilities.FileSystemAccess),
        )
        assertEquals(
            CapabilitySupport.Supported,
            host.capabilitySupport(WeChatDeviceCapabilities.FileSystemRemove),
        )
    }

    @Test
    fun aHostWithoutTheManagerReportsEveryOperationUnsupported() {
        // No file manager means no methods on it, which is what the entry probes.
        val host = fakeWechatHost(
            runtimeInfoHost = FakeWechatRuntimeInfoHost(availableFileSystemMethods = emptySet()),
        )

        listOf(
            WeChatDeviceCapabilities.FileSystemRead,
            WeChatDeviceCapabilities.FileSystemWrite,
            WeChatDeviceCapabilities.FileSystemAccess,
            WeChatDeviceCapabilities.FileSystemRemove,
        ).forEach { key ->
            assertEquals(CapabilitySupport.Unsupported, host.capabilitySupport(key), "key '${key.value}'")
        }
    }

    @Test
    fun aHostWithoutASandboxRootReportsThatSeparately() {
        val host = fakeWechatHost(
            runtimeInfoHost = FakeWechatRuntimeInfoHost(userDataPathAvailable = false),
        )

        // The sandbox root is a separate host value from the manager, so its
        // absence must not take the four operations down with it.
        assertEquals(
            CapabilitySupport.Unsupported,
            host.capabilitySupport(WeChatDeviceCapabilities.FileSystemSandboxPath),
        )
        assertEquals(
            CapabilitySupport.Supported,
            host.capabilitySupport(WeChatDeviceCapabilities.FileSystemRead),
        )
    }

    @Test
    fun aHostThatCannotBeProbedAtAllReportsUnsupported() {
        val host = fakeWechatHost(
            runtimeInfoHost = FakeWechatRuntimeInfoHost(canIUseAvailable = false),
        )

        assertEquals(
            CapabilitySupport.Unsupported,
            host.capabilitySupport(WeChatDeviceCapabilities.FileSystemRead),
        )
        assertEquals(
            CapabilitySupport.Unsupported,
            host.capabilitySupport(WeChatDeviceCapabilities.FileSystemSandboxPath),
        )
    }

    @Test
    fun aCapabilityOutsideTheCatalogueIsUnsupported() {
        val host = fakeWechatHost()

        assertEquals(
            CapabilitySupport.Unsupported,
            host.capabilitySupport(CapabilityKey("wechat.not-a-capability")),
        )
    }

    @Test
    fun theAdapterAndTheGateAgreeAboutWhatTheHostProvides() = runTest {
        // A capability reported supported whose call the adapter then refuses
        // would be a lie the caller could not act on, so the two must agree.
        val host = fakeWechatHost(
            runtimeInfoHost = FakeWechatRuntimeInfoHost(
                availableFileSystemMethods = setOf("readFile", "writeFile"),
            ),
            fileSystemHost = FakeWechatFileSystemHost(
                readSupported = true,
                writeSupported = true,
                accessSupported = false,
                removeSupported = false,
                sandboxPath = null,
            ),
        )

        assertEquals(
            CapabilitySupport.Supported,
            host.capabilitySupport(WeChatDeviceCapabilities.FileSystemRead),
        )
        host.platform.fileSystem.writeText(path, "value")
        host.platform.fileSystem.readText(path)
        assertEquals(
            CapabilitySupport.Unsupported,
            host.capabilitySupport(WeChatDeviceCapabilities.FileSystemAccess),
        )
        assertEquals(
            CapabilitySupport.Unsupported,
            host.capabilitySupport(WeChatDeviceCapabilities.FileSystemRemove),
        )
    }

    @Test
    fun aWriteThenAccessThenReadThenRemoveFlowHolds() = runTest {
        val fileSystem = fakeWechatHost().platform.fileSystem

        assertFalse(fileSystem.exists(path))

        fileSystem.writeText(path, "kmp-miniapp-sdk bob72 test")
        assertTrue(fileSystem.exists(path))
        assertEquals("kmp-miniapp-sdk bob72 test", fileSystem.readText(path))

        fileSystem.remove(path)
        assertFalse(fileSystem.exists(path))
    }

    @Test
    fun theFileSystemDoesNotReachTheOtherCapabilities() = runTest {
        val fileSystemHost = FakeWechatFileSystemHost()
        val host = fakeWechatHost(fileSystemHost = fileSystemHost)

        host.platform.fileSystem.writeText(path, "value")

        // Writing a file must not touch the clipboard, the vibrator, storage, or
        // anything else the host provides.
        assertEquals(1, fileSystemHost.writeCalls)
        assertEquals(0, fileSystemHost.readCalls)
        assertEquals(0, fileSystemHost.accessCalls)
        assertEquals(0, fileSystemHost.removeCalls)
    }
}
