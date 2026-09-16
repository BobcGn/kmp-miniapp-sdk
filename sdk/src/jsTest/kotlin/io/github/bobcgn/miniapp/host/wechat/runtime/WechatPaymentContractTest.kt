package io.github.bobcgn.miniapp.host.wechat.runtime

import io.github.bobcgn.miniapp.capability.CapabilityKey
import io.github.bobcgn.miniapp.capability.CapabilitySupport
import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.WeChatDeviceCapabilities
import io.github.bobcgn.miniapp.host.wechat.WeChatPaymentRequest
import io.github.bobcgn.miniapp.host.wechat.WeChatPaymentSignType
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatClipboardHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatDownloadFileHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatNetworkStatusHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatPaymentHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatPermissionHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatPrivacyHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatRuntimeInfoHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatUploadFileHost
import io.github.bobcgn.miniapp.host.wechat.testing.fakeWechatHost
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

/**
 * What the payment capability must hold for the SDK as a whole, driven through the real
 * `WechatHost`.
 *
 * These are not real-host facts. A real payment needs a merchant account, an order, a
 * signature from a trusted backend, and a device, so these checks state the boundary
 * around them instead — including the boundary that matters most, which is that nothing
 * in the SDK turns a client callback into an order fact.
 */
internal class WechatPaymentContractTest {
    @Test
    fun thePaymentCapabilityHasItsOwnWeChatSpecificKey() {
        val key = WeChatDeviceCapabilities.RequestPayment

        // Namespaced, so paying is not presented as a capability every host can be asked
        // for: a merchant account and a prepay order are WeChat concepts.
        assertEquals("wechat.request-payment", key.value)
        assertEquals(true, key.value.startsWith("wechat."))
        assertNotEquals(CapabilityKey("request-payment"), key)
        assertNotEquals(WeChatDeviceCapabilities.DownloadFile, key)
    }

    @Test
    fun aHostThatProvidesPaymentReportsItSupported() {
        val host = fakeWechatHost()

        assertEquals(
            CapabilitySupport.Supported,
            host.capabilitySupport(WeChatDeviceCapabilities.RequestPayment),
        )
    }

    @Test
    fun aHostWithoutTheApiReportsItUnsupported() {
        val host = fakeWechatHost(
            runtimeInfoHost = FakeWechatRuntimeInfoHost(
                availableSchemas = setOf("downloadFile"),
            ),
        )

        assertEquals(
            CapabilitySupport.Unsupported,
            host.capabilitySupport(WeChatDeviceCapabilities.RequestPayment),
        )
    }

    @Test
    fun aHostThatCannotBeProbedAtAllReportsUnsupported() {
        val host = fakeWechatHost(
            runtimeInfoHost = FakeWechatRuntimeInfoHost(canIUseAvailable = false),
        )

        assertEquals(
            CapabilitySupport.Unsupported,
            host.capabilitySupport(WeChatDeviceCapabilities.RequestPayment),
        )
    }

    @Test
    fun aPaymentFailureDoesNotChangeWhatTheCapabilityReports() = runTest {
        // The key answers whether the host exposes the API. A merchant that is not
        // configured, an order that does not exist, and parameters the host rejects are all
        // answers the host gives when it is called, and reporting any of them as an
        // unsupported host would send a consumer looking in the wrong place.
        val host = fakeWechatHost(
            paymentHost = FakeWechatPaymentHost().apply {
                failureMessage = "requestPayment:fail merchant not configured"
            },
        )

        assertFailsWith<MiniAppException.HostFailure> {
            host.platform.payment.request(payment())
        }

        assertEquals(
            CapabilitySupport.Supported,
            host.capabilitySupport(WeChatDeviceCapabilities.RequestPayment),
        )
    }

    @Test
    fun aPaymentAsksTheHostForNoPermission() = runTest {
        // The API names no signature scope and no permission in the offline sources, so the
        // SDK maps none and queries none.
        val permissionHost = FakeWechatPermissionHost()
        val privacyHost = FakeWechatPrivacyHost()
        val host = fakeWechatHost(permissionHost = permissionHost, privacyHost = privacyHost)

        host.platform.payment.request(payment())

        assertEquals(0, permissionHost.getSettingCalls)
        assertEquals(0, permissionHost.authorizeCalls)
        assertEquals(0, permissionHost.openSettingCalls)
        assertEquals(0, privacyHost.getPrivacySettingCalls)
        assertEquals(0, privacyHost.authorizeCalls)
    }

    @Test
    fun aPaymentDoesNotTouchTheOtherCapabilities() = runTest {
        val clipboardHost = FakeWechatClipboardHost()
        val networkStatusHost = FakeWechatNetworkStatusHost()
        val uploadHost = FakeWechatUploadFileHost()
        val downloadHost = FakeWechatDownloadFileHost()
        val paymentHost = FakeWechatPaymentHost()
        val host = fakeWechatHost(
            clipboardHost = clipboardHost,
            networkStatusHost = networkStatusHost,
            uploadFileHost = uploadHost,
            downloadFileHost = downloadHost,
            paymentHost = paymentHost,
        )

        host.platform.payment.request(payment())

        assertEquals(1, paymentHost.calls)
        assertEquals(0, clipboardHost.readCalls)
        assertEquals(0, networkStatusHost.queryCalls)
        assertEquals(0, uploadHost.calls)
        assertEquals(0, downloadHost.calls)
    }

    @Test
    fun anUnsupportedPaymentFailsBeforeTheHostIsCalled() = runTest {
        val paymentHost = FakeWechatPaymentHost(supported = false)
        val host = fakeWechatHost(paymentHost = paymentHost)

        val failure = assertFailsWith<MiniAppException.UnsupportedCapability> {
            host.platform.payment.request(payment())
        }

        assertEquals(WeChatDeviceCapabilities.RequestPayment, failure.capability)
        assertEquals(0, paymentHost.calls)
    }

    @Test
    fun aCompletedInteractionGivesTheCallerNoOrderFactToHold() = runTest {
        // A regression guard rather than a behaviour test. The Kotlin boundary resolves with
        // nothing, because the host's callback carries nothing about an order; if someone
        // later adds a value here named paid, settled, or confirmed, this assertion stops
        // compiling and the question gets asked again. The JavaScript boundary names the
        // same meaning explicitly, and the smoke test asserts that the value it resolves
        // has exactly one field, none of which is an order fact.
        assertEquals(Unit, io.github.bobcgn.miniapp.host.wechat.adapter.WechatPayment(
            FakeWechatPaymentHost(),
        ).request(payment()))
    }

    private fun payment(): WeChatPaymentRequest = WeChatPaymentRequest(
        timeStamp = "1700000000",
        nonceStr = "nonce",
        prepayPackage = "prepay_id=abc",
        signType = WeChatPaymentSignType.MD5,
        paySign = "signature",
    )
}
