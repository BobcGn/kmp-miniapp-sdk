package io.github.bobcgn.miniapp.host.wechat.runtime

import io.github.bobcgn.miniapp.capability.CapabilityKey
import io.github.bobcgn.miniapp.capability.CapabilitySupport
import io.github.bobcgn.miniapp.capability.permission.PermissionKey
import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.WeChatCoordinateSystem
import io.github.bobcgn.miniapp.host.wechat.WeChatDeviceCapabilities
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatLocationHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatPermissionHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatPrivacyHost
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatRuntimeInfoHost
import io.github.bobcgn.miniapp.host.wechat.testing.fakeWechatHost
import io.github.bobcgn.miniapp.host.wechat.testing.fakeWxAuthSetting
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * What the location capability must hold for the SDK as a whole, driven through
 * the real `WechatHost`.
 *
 * These are not real-host facts: only WeChat can say whether a device reported a
 * real position. They state the boundaries around it instead — that the capability
 * is gated on its own, and that API availability, permission, and privacy stay
 * three separate answers.
 */
internal class WechatLocationContractTest {
    @Test
    fun theLocationCapabilityHasAWeChatSpecificKey() {
        val key = WeChatDeviceCapabilities.Location

        assertTrue(key.value.startsWith("wechat."), "unexpected key '${key.value}'")
        assertEquals("wechat.location", key.value)
        assertNotEquals(CapabilityKey("location"), key)
    }

    @Test
    fun thePermissionKeyIsHostNeutralAndMapsToTheLocationScope() {
        // The permission is named for what it is for, not for the host's scope
        // string, which exists only inside the adapter's mapping.
        assertEquals("location", PermissionKey.Location.value)
        assertNotEquals(PermissionKey.Location, PermissionKey.Microphone)
    }

    @Test
    fun aHostThatProvidesTheApiReportsItSupported() {
        val host = fakeWechatHost()

        assertEquals(
            CapabilitySupport.Supported,
            host.capabilitySupport(WeChatDeviceCapabilities.Location),
        )
    }

    @Test
    fun aHostWithoutTheApiReportsItUnsupported() {
        val host = fakeWechatHost(
            runtimeInfoHost = FakeWechatRuntimeInfoHost(availableSchemas = emptySet()),
        )

        assertEquals(
            CapabilitySupport.Unsupported,
            host.capabilitySupport(WeChatDeviceCapabilities.Location),
        )
    }

    @Test
    fun aHostThatCannotBeProbedAtAllReportsUnsupported() {
        val host = fakeWechatHost(
            runtimeInfoHost = FakeWechatRuntimeInfoHost(canIUseAvailable = false),
        )

        assertEquals(
            CapabilitySupport.Unsupported,
            host.capabilitySupport(WeChatDeviceCapabilities.Location),
        )
    }

    @Test
    fun anUnreadableVersionStillAnswersFromTheProbe() {
        // No minimum base library is recorded for this API, so an unreadable
        // version is not evidence of an old host.
        val host = fakeWechatHost(
            runtimeInfoHost = FakeWechatRuntimeInfoHost(baseLibraryVersion = null),
        )

        assertEquals(
            CapabilitySupport.Supported,
            host.capabilitySupport(WeChatDeviceCapabilities.Location),
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
    fun aRefusedPermissionIsNotReportedAsAnUnavailableApi() = runTest {
        // WeChat exposes the API; what the user did with the permission prompt is a
        // different question and must not be answered as "unsupported".
        val host = fakeWechatHost(
            permissionHost = FakeWechatPermissionHost(
                authSetting = fakeWxAuthSetting("scope.userLocation" to false),
            ),
        )

        assertEquals(
            CapabilitySupport.Supported,
            host.capabilitySupport(WeChatDeviceCapabilities.Location),
        )

        val denial = assertFailsWith<MiniAppException.PermissionDenied> {
            host.permissions.request(PermissionKey.Location)
        }
        assertEquals("location", denial.permission)
    }

    @Test
    fun anUnsatisfiedPrivacyContractIsNotReportedAsAnUnavailableApi() = runTest {
        val host = fakeWechatHost(
            privacyHost = FakeWechatPrivacyHost(
                needAuthorization = true,
                privacyContractName = CONTRACT,
            ),
        )

        assertEquals(
            CapabilitySupport.Supported,
            host.capabilitySupport(WeChatDeviceCapabilities.Location),
        )

        assertFailsWith<MiniAppException.PrivacyAuthorizationRequired> {
            host.platform.location.currentPosition()
        }
    }

    @Test
    fun aPositionRoundTripsThroughTheHost() = runTest {
        val host = fakeWechatHost(
            locationHost = FakeWechatLocationHost(latitude = 31.5, longitude = 121.5, accuracy = 15.0),
            // The shared fake reports an unsatisfied contract by default so a call
            // cannot pass vacuously; this test is about the position itself.
            privacyHost = FakeWechatPrivacyHost(needAuthorization = false),
            permissionHost = FakeWechatPermissionHost(
                authSetting = fakeWxAuthSetting("scope.userLocation" to true),
            ),
        )

        val position = host.platform.location.currentPosition(WeChatCoordinateSystem.WGS84)

        assertEquals(31.5, position.latitude)
        assertEquals(121.5, position.longitude)
        assertEquals(15.0, position.accuracyMeters)
        assertEquals(WeChatCoordinateSystem.WGS84, position.coordinateSystem)
    }

    @Test
    fun readingAPositionDoesNotTouchTheOtherCapabilities() = runTest {
        val permissionHost = FakeWechatPermissionHost(
            authSetting = fakeWxAuthSetting("scope.userLocation" to true),
        )
        val host = fakeWechatHost(
            permissionHost = permissionHost,
            privacyHost = FakeWechatPrivacyHost(needAuthorization = false),
        )

        host.platform.location.currentPosition()

        // The adapter checks permission but never drives its prompt. This keeps a
        // position read from becoming an implicit authorization request.
        assertEquals(1, permissionHost.getSettingCalls)
        assertEquals(0, permissionHost.authorizeCalls)
    }

    private companion object {
        private const val CONTRACT = "《example privacy contract》"
    }
}
