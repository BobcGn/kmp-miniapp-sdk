package io.github.bobcgn.miniapp.host.wechat.runtime

import io.github.bobcgn.miniapp.capability.CapabilityKey
import io.github.bobcgn.miniapp.capability.CapabilitySupport
import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.WeChatDeviceCapabilities
import io.github.bobcgn.miniapp.host.wechat.WeChatMediaRequest
import io.github.bobcgn.miniapp.host.wechat.WeChatMediaType
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatChooseMediaHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatClipboardHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatHapticsHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatLocationHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatPermissionHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatPrivacyHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatRuntimeInfoHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatScanCodeHost
import io.github.bobcgn.miniapp.host.wechat.testing.fakeChooseMediaFiles
import io.github.bobcgn.miniapp.host.wechat.testing.fakeChooseMediaVideoEntry
import io.github.bobcgn.miniapp.host.wechat.testing.fakeWechatHost
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

/**
 * What the media capability must hold for the SDK as a whole, driven through the
 * real `WechatHost`.
 *
 * These are not real-host facts. Choosing media needs a device and a person to
 * choose something; these checks state the boundary around them instead.
 */
internal class WechatChooseMediaContractTest {
    @Test
    fun theMediaCapabilityHasAWeChatSpecificKey() {
        val key = WeChatDeviceCapabilities.ChooseMedia

        // Namespaced, so selecting media is not presented as a capability every host
        // can be asked for.
        assertEquals("wechat.choose-media", key.value)
        assertNotEquals(CapabilityKey("choose-media"), key)
        assertNotEquals(WeChatDeviceCapabilities.ScanCode, key)
    }

    @Test
    fun aHostThatProvidesMediaSelectionReportsItSupported() {
        val host = fakeWechatHost()

        assertEquals(
            CapabilitySupport.Supported,
            host.capabilitySupport(WeChatDeviceCapabilities.ChooseMedia),
        )
    }

    @Test
    fun aHostWithoutTheMediaApiReportsItUnsupported() {
        val host = fakeWechatHost(
            runtimeInfoHost = FakeWechatRuntimeInfoHost(
                availableSchemas = setOf("scanCode"),
            ),
        )

        assertEquals(
            CapabilitySupport.Unsupported,
            host.capabilitySupport(WeChatDeviceCapabilities.ChooseMedia),
        )
    }

    @Test
    fun aHostThatCannotBeProbedAtAllReportsUnsupported() {
        val host = fakeWechatHost(
            runtimeInfoHost = FakeWechatRuntimeInfoHost(canIUseAvailable = false),
        )

        assertEquals(
            CapabilitySupport.Unsupported,
            host.capabilitySupport(WeChatDeviceCapabilities.ChooseMedia),
        )
    }

    @Test
    fun aSelectionAsksTheHostForNoPermission() = runTest {
        // WeChat's own picker needs no permission, and the host's scope list holds no
        // scope for reading the media library. Querying or requesting one would prompt
        // for something the API does not require.
        val permissionHost = FakeWechatPermissionHost()
        val privacyHost = FakeWechatPrivacyHost()
        val host = fakeWechatHost(permissionHost = permissionHost, privacyHost = privacyHost)

        host.platform.chooseMedia.choose(
            WeChatMediaRequest(mediaType = listOf(WeChatMediaType.IMAGE)),
        )

        assertEquals(0, permissionHost.getSettingCalls)
        assertEquals(0, permissionHost.authorizeCalls)
        assertEquals(0, permissionHost.openSettingCalls)
        assertEquals(0, privacyHost.getPrivacySettingCalls)
        assertEquals(0, privacyHost.authorizeCalls)
    }

    @Test
    fun aSelectionDoesNotTouchTheOtherCapabilities() = runTest {
        val clipboardHost = FakeWechatClipboardHost()
        val hapticsHost = FakeWechatHapticsHost()
        val locationHost = FakeWechatLocationHost()
        val scanCodeHost = FakeWechatScanCodeHost()
        val chooseMediaHost = FakeWechatChooseMediaHost(
            tempFiles = fakeChooseMediaFiles(fakeChooseMediaVideoEntry()),
        )
        val host = fakeWechatHost(
            clipboardHost = clipboardHost,
            hapticsHost = hapticsHost,
            locationHost = locationHost,
            scanCodeHost = scanCodeHost,
            chooseMediaHost = chooseMediaHost,
        )

        host.platform.chooseMedia.choose(
            WeChatMediaRequest(mediaType = listOf(WeChatMediaType.VIDEO)),
        )

        assertEquals(1, chooseMediaHost.calls)
        assertEquals(0, clipboardHost.readCalls)
        assertEquals(0, clipboardHost.writeCalls)
        assertEquals(0, hapticsHost.shortCalls)
        assertEquals(0, hapticsHost.longCalls)
        assertEquals(0, locationHost.calls)
        assertEquals(0, scanCodeHost.calls)
    }

    @Test
    fun anInterruptionIsNotReportedAsAMissingCapabilityOrARefusal() = runTest {
        // Three different instructions to a consumer: one says try again later, one
        // says this host cannot do it, and one says ask the user for a permission.
        // None of them is what an interruption means.
        val host = fakeWechatHost(
            chooseMediaHost = FakeWechatChooseMediaHost().apply {
                failureMessage = "chooseMedia:cancel"
            },
        )

        val failure: MiniAppException = assertFailsWith<MiniAppException.HostInteractionInterrupted> {
            host.platform.chooseMedia.choose(
                WeChatMediaRequest(mediaType = listOf(WeChatMediaType.IMAGE)),
            )
        }

        assertFalse(failure is MiniAppException.UnsupportedCapability)
        assertFalse(failure is MiniAppException.PermissionDenied)
        assertFalse(failure is MiniAppException.UserCancelled)
        assertEquals(
            CapabilitySupport.Supported,
            host.capabilitySupport(WeChatDeviceCapabilities.ChooseMedia),
        )
    }
}
