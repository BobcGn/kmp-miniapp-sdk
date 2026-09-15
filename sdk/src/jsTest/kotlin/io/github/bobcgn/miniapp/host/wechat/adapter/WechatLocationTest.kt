package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.capability.privacy.PrivacyAuthorizationRequirement
import io.github.bobcgn.miniapp.capability.privacy.PrivacyStatus
import io.github.bobcgn.miniapp.capability.permission.PermissionKey
import io.github.bobcgn.miniapp.capability.permission.PermissionState
import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.WeChatCoordinateSystem
import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxGetLocationSuccessResult
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatLocationHost
import io.github.bobcgn.miniapp.host.wechat.testing.fakeGetLocationSuccess
import io.github.bobcgn.miniapp.testing.FakeMiniAppPrivacy
import io.github.bobcgn.miniapp.testing.FakeMiniAppPermissions
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * How the WeChat adapter turns the position callbacks into the SDK behaviour,
 * including the privacy precondition it enforces.
 *
 * These are the raw-callback behaviours; `WechatLocationContractTest` states what
 * must hold for the SDK as a whole.
 */
internal class WechatLocationTest {
    @Test
    fun aPositionTheHostReturnsIsReported() = runTest {
        val host = FakeWechatLocationHost(latitude = 31.5, longitude = 121.5, accuracy = 15.0)

        val position = location(host).currentPosition()

        assertEquals(31.5, position.latitude)
        assertEquals(121.5, position.longitude)
        assertEquals(15.0, position.accuracyMeters)
    }

    @Test
    fun theCoordinateSystemReachesTheHost() = runTest {
        val host = FakeWechatLocationHost()

        location(host).currentPosition(WeChatCoordinateSystem.WGS84)

        // The SDK never takes the host default silently.
        assertEquals(WeChatCoordinateSystem.WGS84, host.lastCoordinateSystem)
    }

    @Test
    fun theDefaultCoordinateSystemIsTheOneWeChatMapsUse() = runTest {
        val host = FakeWechatLocationHost()

        location(host).currentPosition()

        assertEquals(WeChatCoordinateSystem.GCJ02, host.lastCoordinateSystem)
    }

    @Test
    fun anUnusableAnswerIsAnInvalidResponse() = runTest {
        val host = FakeWechatLocationHost(latitude = null)

        assertFailsWith<MiniAppException.InvalidResponse> { location(host).currentPosition() }
    }

    @Test
    fun aFailedPositionIsAHostFailure() = runTest {
        val host = FakeWechatLocationHost().apply { failureMessage = "getLocation:fail system error" }

        val failure = assertFailsWith<MiniAppException.HostFailure> {
            location(host).currentPosition()
        }

        assertEquals("getLocation", failure.metadata["operation"])
    }

    @Test
    fun aHostWithoutTheApiIsAnUnsupportedCapability() = runTest {
        val host = FakeWechatLocationHost(supported = false)

        val failure = assertFailsWith<MiniAppException.UnsupportedCapability> {
            location(host).currentPosition()
        }

        assertEquals("wechat.location", failure.capability.value)
        assertEquals(0, host.calls)
    }

    @Test
    fun anUnsatisfiedPrivacyContractStopsTheCallBeforeItReachesTheHost() = runTest {
        val host = FakeWechatLocationHost()
        val privacy = FakeMiniAppPrivacy(
            status = PrivacyStatus(PrivacyAuthorizationRequirement.REQUIRED, CONTRACT),
        )

        val failure = assertFailsWith<MiniAppException.PrivacyAuthorizationRequired> {
            location(host, privacy).currentPosition()
        }

        assertEquals(CONTRACT, failure.contractName)
        // The privacy precondition is checked first, so the host is never asked.
        assertEquals(0, host.calls)
    }

    @Test
    fun aPermissionThatWasNotRequestedStopsTheCallWithoutPrompting() = runTest {
        val host = FakeWechatLocationHost()
        val permissions = FakeMiniAppPermissions()

        val failure = assertFailsWith<MiniAppException.PermissionDenied> {
            location(host, permissions = permissions).currentPosition()
        }

        assertEquals(PermissionKey.Location.value, failure.permission)
        assertEquals(0, permissions.hostRequests)
        assertEquals(0, host.calls)
    }

    @Test
    fun aDeniedPermissionStopsTheCallWithoutPromptingAgain() = runTest {
        val host = FakeWechatLocationHost()
        val permissions = grantedPermissions().apply {
            decide(PermissionKey.Location, PermissionState.Denied)
        }

        val failure = assertFailsWith<MiniAppException.PermissionDenied> {
            location(host, permissions = permissions).currentPosition()
        }

        assertEquals(PermissionKey.Location.value, failure.permission)
        assertEquals(0, permissions.hostRequests)
        assertEquals(0, host.calls)
    }

    @Test
    fun aPrivacyRefusalIsNotReportedAsAnApiFailure() = runTest {
        val host = FakeWechatLocationHost()
        val privacy = FakeMiniAppPrivacy(
            status = PrivacyStatus(PrivacyAuthorizationRequirement.REQUIRED, CONTRACT),
        )

        // Widened on purpose: the point of the check is that the two failures are
        // unrelated, so the static type must not already decide the answer.
        val failure: MiniAppException = assertFailsWith<MiniAppException.PrivacyAuthorizationRequired> {
            location(host, privacy).currentPosition()
        }

        // The API exists; the obstacle is the host's privacy contract, and saying
        // otherwise would send the caller to fix the wrong thing.
        assertTrue(failure !is MiniAppException.UnsupportedCapability)
    }

    @Test
    fun thePrivacyPreconditionIsQueriedWithoutPrompting() = runTest {
        val privacy = FakeMiniAppPrivacy()

        location(FakeWechatLocationHost(), privacy).currentPosition()

        assertEquals(1, privacy.statusQueries)
        // Reading a position is not an occasion to accept a contract on the user's
        // behalf.
        assertEquals(0, privacy.authorizationRequests)
    }

    @Test
    fun aRepeatedTerminalCallbackCompletesOnlyOnce() = runTest {
        val host = FakeWechatLocationHost(accuracy = 9.0).apply { completeTwice = true }

        assertEquals(9.0, location(host).currentPosition().accuracyMeters)
    }

    @Test
    fun aCallbackAfterCancellationIsIgnored() = runTest {
        val host = DeferredLocationHost()
        val deferred = async(start = CoroutineStart.UNDISPATCHED) {
            location(host).currentPosition()
        }

        deferred.cancelAndJoin()
        host.succeed()

        assertTrue(deferred.isCancelled)
    }

    @Test
    fun cancellationStopsOnlyTheWaitingCaller() = runTest {
        // WeChat reports no task handle, so nothing is invented to make
        // cancellation look like host cancellation.
        val host = DeferredLocationHost()

        val deferred = async(start = CoroutineStart.UNDISPATCHED) {
            location(host).currentPosition()
        }
        deferred.cancelAndJoin()

        assertEquals(1, host.calls)
        assertTrue(deferred.isCancelled)
    }

    private fun location(
        host: WechatLocationHost,
        privacy: FakeMiniAppPrivacy = FakeMiniAppPrivacy(),
        permissions: FakeMiniAppPermissions = grantedPermissions(),
    ): WechatLocation = WechatLocation(host = host, privacy = privacy, permissions = permissions)

    private fun grantedPermissions(): FakeMiniAppPermissions = FakeMiniAppPermissions().apply {
        decide(PermissionKey.Location, PermissionState.Granted)
    }

    /** A port that stays silent until the test completes it. */
    private class DeferredLocationHost : WechatLocationHost {
        var calls: Int = 0
            private set

        private var succeedCallback: ((WxGetLocationSuccessResult) -> Unit)? = null

        fun succeed() {
            val callback = requireNotNull(succeedCallback) {
                "The adapter has not started a position request yet"
            }
            callback(fakeGetLocationSuccess(31.0, 121.0, 12.0))
        }

        override fun isSupported(): Boolean = true

        override fun currentPosition(
            coordinateSystem: WeChatCoordinateSystem,
            success: (WxGetLocationSuccessResult) -> Unit,
            failure: (WxGeneralCallbackResult) -> Unit,
        ) {
            calls += 1
            succeedCallback = success
        }
    }

    private companion object {
        private const val CONTRACT = "《example privacy contract》"
    }
}
