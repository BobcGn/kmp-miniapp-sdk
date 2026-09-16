package io.github.bobcgn.miniapp.host.wechat.runtime

import io.github.bobcgn.miniapp.capability.CapabilityKey
import io.github.bobcgn.miniapp.capability.CapabilitySupport
import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.WeChatDeviceCapabilities
import io.github.bobcgn.miniapp.host.wechat.WeChatSubscriptionRequest
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatChooseMediaHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatClipboardHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatHapticsHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatLocationHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatPermissionHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatPrivacyHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatRequestSubscribeMessageHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatRuntimeInfoHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatScanCodeHost
import io.github.bobcgn.miniapp.host.wechat.testing.fakeSubscribeMessageSuccess
import io.github.bobcgn.miniapp.host.wechat.testing.fakeWechatHost
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

/**
 * What the subscription capability must hold for the SDK as a whole, driven through
 * the real `WechatHost`.
 *
 * These are not real-host facts. Putting templates in front of a user needs a device,
 * a WeChat backend template, and a person to answer the prompt; these checks state the
 * boundary around them instead.
 */
internal class WechatSubscriptionContractTest {
    @Test
    fun theSubscriptionCapabilityHasAWeChatSpecificKey() {
        val key = WeChatDeviceCapabilities.RequestSubscribeMessage

        // Namespaced, so asking about templates is not presented as a capability every
        // host can be asked for, and certainly not as a general push-notification one.
        assertEquals("wechat.request-subscribe-message", key.value)
        assertNotEquals(CapabilityKey("subscribe-message"), key)
        assertNotEquals(WeChatDeviceCapabilities.ChooseMedia, key)
    }

    @Test
    fun aHostThatProvidesSubscriptionReportsItSupported() {
        val host = fakeWechatHost()

        assertEquals(
            CapabilitySupport.Supported,
            host.capabilitySupport(WeChatDeviceCapabilities.RequestSubscribeMessage),
        )
    }

    @Test
    fun aHostWithoutTheApiReportsItUnsupported() {
        val host = fakeWechatHost(
            runtimeInfoHost = FakeWechatRuntimeInfoHost(
                availableSchemas = setOf("chooseMedia"),
            ),
        )

        assertEquals(
            CapabilitySupport.Unsupported,
            host.capabilitySupport(WeChatDeviceCapabilities.RequestSubscribeMessage),
        )
    }

    @Test
    fun aHostThatCannotBeProbedAtAllReportsUnsupported() {
        val host = fakeWechatHost(
            runtimeInfoHost = FakeWechatRuntimeInfoHost(canIUseAvailable = false),
        )

        assertEquals(
            CapabilitySupport.Unsupported,
            host.capabilitySupport(WeChatDeviceCapabilities.RequestSubscribeMessage),
        )
    }

    @Test
    fun aSubscriptionRequestAsksTheHostForNoPermission() = runTest {
        // The offline sources for this API name no scope for it, so the SDK maps none
        // and queries none. That is a statement about the evidence, not a claim that
        // this host has no privacy condition — which is why the manual run watches for
        // a prompt rather than assuming either way.
        val permissionHost = FakeWechatPermissionHost()
        val privacyHost = FakeWechatPrivacyHost()
        val host = fakeWechatHost(permissionHost = permissionHost, privacyHost = privacyHost)

        host.platform.requestSubscribeMessage.request(
            WeChatSubscriptionRequest(listOf("template-one")),
        )

        assertEquals(0, permissionHost.getSettingCalls)
        assertEquals(0, permissionHost.authorizeCalls)
        assertEquals(0, permissionHost.openSettingCalls)
        assertEquals(0, privacyHost.getPrivacySettingCalls)
        assertEquals(0, privacyHost.authorizeCalls)
    }

    @Test
    fun aSubscriptionRequestDoesNotTouchTheOtherCapabilities() = runTest {
        val clipboardHost = FakeWechatClipboardHost()
        val hapticsHost = FakeWechatHapticsHost()
        val locationHost = FakeWechatLocationHost()
        val scanCodeHost = FakeWechatScanCodeHost()
        val chooseMediaHost = FakeWechatChooseMediaHost()
        val subscribeHost = FakeWechatRequestSubscribeMessageHost(
            answer = fakeSubscribeMessageSuccess("template-one" to "accept"),
        )
        val host = fakeWechatHost(
            clipboardHost = clipboardHost,
            hapticsHost = hapticsHost,
            locationHost = locationHost,
            scanCodeHost = scanCodeHost,
            chooseMediaHost = chooseMediaHost,
            requestSubscribeMessageHost = subscribeHost,
        )

        host.platform.requestSubscribeMessage.request(
            WeChatSubscriptionRequest(listOf("template-one")),
        )

        assertEquals(1, subscribeHost.calls)
        assertEquals(0, clipboardHost.readCalls)
        assertEquals(0, clipboardHost.writeCalls)
        assertEquals(0, hapticsHost.shortCalls)
        assertEquals(0, hapticsHost.longCalls)
        assertEquals(0, locationHost.calls)
        assertEquals(0, scanCodeHost.calls)
        assertEquals(0, chooseMediaHost.calls)
    }

    @Test
    fun anUnverifiedCancellationSignalRemainsAHostFailure() = runTest {
        // Another API's cancellation convention is not evidence for this one. Until a
        // real host produces an exact signal, the SDK must not invent an interruption.
        val host = fakeWechatHost(
            requestSubscribeMessageHost = FakeWechatRequestSubscribeMessageHost()
                .apply { failureMessage = "requestSubscribeMessage:cancel" },
        )

        val failure: MiniAppException = assertFailsWith<MiniAppException.HostFailure> {
            host.platform.requestSubscribeMessage.request(
                WeChatSubscriptionRequest(listOf("template-one")),
            )
        }

        assertFalse(failure is MiniAppException.UnsupportedCapability)
        assertFalse(failure is MiniAppException.PermissionDenied)
        assertFalse(failure is MiniAppException.UserCancelled)
        assertEquals(
            CapabilitySupport.Supported,
            host.capabilitySupport(WeChatDeviceCapabilities.RequestSubscribeMessage),
        )
    }
}
