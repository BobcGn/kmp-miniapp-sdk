package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.capability.permission.PermissionKey
import io.github.bobcgn.miniapp.capability.permission.PermissionState
import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatPermissionHost
import io.github.bobcgn.miniapp.host.wechat.testing.fakeWxAuthSetting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * How the WeChat adapter turns the host's permission callbacks into the
 * platform-neutral lifecycle, and what happens when calls overlap.
 *
 * The adapter's host calls run on the test scheduler, so an interleaving is
 * produced deliberately rather than by waiting.
 */
internal class WechatPermissionsTest {
    @Test
    fun aScopeTheHostHasNoDecisionForIsNotRequested() = runTest {
        assertEquals(PermissionState.NotRequested, permissionsWith(FakeWechatPermissionHost()).stateOf(MICROPHONE))
    }

    @Test
    fun aGrantedScopeReadsAsGranted() = runTest {
        val host = FakeWechatPermissionHost(authSetting = fakeWxAuthSetting(RECORD to true))

        assertEquals(PermissionState.Granted, permissionsWith(host).stateOf(MICROPHONE))
    }

    @Test
    fun aRefusedScopeReadsAsDenied() = runTest {
        val host = FakeWechatPermissionHost(authSetting = fakeWxAuthSetting(RECORD to false))

        assertEquals(PermissionState.Denied, permissionsWith(host).stateOf(MICROPHONE))
    }

    @Test
    fun aScopeValueThatIsNotABooleanIsAnInvalidResponse() = runTest {
        val host = FakeWechatPermissionHost(authSetting = fakeWxAuthSetting(RECORD to "yes"))

        assertFailsWith<MiniAppException.InvalidResponse> { permissionsWith(host).stateOf(MICROPHONE) }
    }

    @Test
    fun aResultWithoutAnAuthorizationMapIsAnInvalidResponse() = runTest {
        val host = FakeWechatPermissionHost(authSetting = null)

        assertFailsWith<MiniAppException.InvalidResponse> { permissionsWith(host).stateOf(MICROPHONE) }
    }

    @Test
    fun aFailedStateQueryIsAHostFailure() = runTest {
        val host = FakeWechatPermissionHost().apply { getSettingFailure = "getSetting:fail system error" }

        val failure = assertFailsWith<MiniAppException.HostFailure> { permissionsWith(host).stateOf(MICROPHONE) }

        assertEquals("getSetting", failure.metadata["operation"])
    }

    @Test
    fun aGrantedRequestReportsGrantedAndAsksForTheMappedScope() = runTest {
        val host = FakeWechatPermissionHost()

        assertEquals(PermissionState.Granted, permissionsWith(host).request(MICROPHONE))
        assertEquals(1, host.authorizeCalls)
        // The scope string is produced only inside the mapping boundary.
        assertEquals(RECORD, host.lastAuthorizeScope)
    }

    @Test
    fun aRefusedRequestIsAPermissionDenialRatherThanAHostFailure() = runTest {
        val host = FakeWechatPermissionHost().apply { authorizeFailure = "authorize:fail auth deny" }

        val denial = assertFailsWith<MiniAppException.PermissionDenied> {
            permissionsWith(host).request(MICROPHONE)
        }

        assertEquals(MICROPHONE.value, denial.permission)
    }

    @Test
    fun anAuthorizeFailureThatIsNotARefusalStaysAHostFailure() = runTest {
        val host = FakeWechatPermissionHost().apply { authorizeFailure = "authorize:fail invalid scope" }

        val failure = assertFailsWith<MiniAppException.HostFailure> {
            permissionsWith(host).request(MICROPHONE)
        }

        assertEquals("authorize", failure.metadata["operation"])
    }

    @Test
    fun anAlreadyGrantedPermissionIsNotSentToTheHostAgain() = runTest {
        val host = FakeWechatPermissionHost(authSetting = fakeWxAuthSetting(RECORD to true))

        assertEquals(PermissionState.Granted, permissionsWith(host).request(MICROPHONE))
        assertEquals(0, host.authorizeCalls)
    }

    @Test
    fun anAlreadyRefusedPermissionDoesNotPretendToPromptAgain() = runTest {
        val host = FakeWechatPermissionHost(authSetting = fakeWxAuthSetting(RECORD to false))

        assertFailsWith<MiniAppException.PermissionDenied> { permissionsWith(host).request(MICROPHONE) }
        // The refusal is read from the host's state, so no prompt is attempted.
        assertEquals(0, host.authorizeCalls)
    }

    @Test
    fun concurrentRequestsForOnePermissionReachTheHostOnce() = runTest {
        val host = FakeWechatPermissionHost().apply { holdAuthorize = true }
        val permissions = permissionsWith(host)

        val first = async(start = CoroutineStart.UNDISPATCHED) { permissions.request(MICROPHONE) }
        val second = async(start = CoroutineStart.UNDISPATCHED) { permissions.request(MICROPHONE) }
        yield()

        // The second caller joined the running call instead of starting another.
        assertEquals(1, host.authorizeCalls)
        assertEquals(1, host.getSettingCalls)

        host.releaseAuthorize()

        assertEquals(PermissionState.Granted, first.await())
        assertEquals(PermissionState.Granted, second.await())
    }

    @Test
    fun concurrentRequestsForDifferentPermissionsDoNotShareAnAnswer() = runTest {
        val host = FakeWechatPermissionHost(authSetting = fakeWxAuthSetting(RECORD to true))
        val permissions = permissionsWith(host, scopeOf = TWO_PERMISSIONS)

        // The result is captured rather than thrown, so the interesting assertion
        // is about which permission got which answer.
        val granted = async(start = CoroutineStart.UNDISPATCHED) {
            runCatching { permissions.request(MICROPHONE) }
        }
        host.authorizeFailure = "authorize:fail auth deny"
        val refused = async(start = CoroutineStart.UNDISPATCHED) {
            runCatching { permissions.request(SPEAKER) }
        }
        yield()

        assertEquals(PermissionState.Granted, granted.await().getOrNull())

        val denial = refused.await().exceptionOrNull()
        assertIs<MiniAppException.PermissionDenied>(denial)
        // Only the undecided permission reached the host for a prompt.
        assertEquals(1, host.authorizeCalls)
    }

    @Test
    fun aCallerThatIsCancelledDoesNotStrandAnotherCaller() = runTest {
        val host = FakeWechatPermissionHost().apply { holdAuthorize = true }
        val permissions = permissionsWith(host)

        val abandoned = async(start = CoroutineStart.UNDISPATCHED) { permissions.request(MICROPHONE) }
        val waiting = async(start = CoroutineStart.UNDISPATCHED) { permissions.request(MICROPHONE) }
        yield()

        abandoned.cancelAndJoin()
        host.releaseAuthorize()

        // The remaining caller still receives the answer the host gave.
        assertEquals(PermissionState.Granted, waiting.await())
        assertTrue(abandoned.isCancelled)
    }

    @Test
    fun anInFlightCallIsClearedOnceItCompletes() = runTest {
        val host = FakeWechatPermissionHost()
        val permissions = permissionsWith(host)

        permissions.request(MICROPHONE)
        // A later request starts a fresh call rather than reusing a stored answer.
        permissions.request(MICROPHONE)

        assertEquals(2, host.getSettingCalls)
    }

    @Test
    fun openSettingsReportsTheDecisionTheHostReturned() = runTest {
        val host = FakeWechatPermissionHost(authSetting = fakeWxAuthSetting(RECORD to true))

        assertEquals(PermissionState.Granted, permissionsWith(host).openSettings(MICROPHONE))
        assertEquals(1, host.openSettingCalls)
    }

    @Test
    fun openSettingsThatLeavesTheScopeRefusedReportsDenied() = runTest {
        val host = FakeWechatPermissionHost(authSetting = fakeWxAuthSetting(RECORD to false))

        assertEquals(PermissionState.Denied, permissionsWith(host).openSettings(MICROPHONE))
    }

    @Test
    fun openSettingsWithoutAnAnswerForTheScopeReportsNotRequested() = runTest {
        val host = FakeWechatPermissionHost(authSetting = fakeWxAuthSetting())
        val permissions = permissionsWith(host)

        // The settings page answered, and its answer carries no decision for the
        // scope, so the permission simply has not been requested yet.
        assertEquals(PermissionState.NotRequested, permissions.openSettings(MICROPHONE))
        assertEquals(0, host.getSettingCalls)
    }

    @Test
    fun openSettingsWithAnUnreadableAnswerReadsTheHostAgain() = runTest {
        val host = FakeWechatPermissionHost(authSetting = fakeWxAuthSetting(RECORD to true)).apply {
            openSettingAuthSetting = "not a map"
        }

        // An unreadable answer is not a decision, so the state comes from the host.
        assertEquals(PermissionState.Granted, permissionsWith(host).openSettings(MICROPHONE))
        assertEquals(1, host.getSettingCalls)
    }

    @Test
    fun aFailedSettingsVisitIsAHostFailure() = runTest {
        val host = FakeWechatPermissionHost().apply { openSettingFailure = "openSetting:fail cancel" }

        val failure = assertFailsWith<MiniAppException.HostFailure> {
            permissionsWith(host).openSettings(MICROPHONE)
        }

        assertEquals("openSetting", failure.metadata["operation"])
    }

    @Test
    fun anUnmappedPermissionIsRefusedBeforeTheHostIsAsked() = runTest {
        val host = FakeWechatPermissionHost()

        assertFailsWith<IllegalArgumentException> {
            permissionsWith(host).request(PermissionKey("permission-this-sdk-does-not-map"))
        }
        assertEquals(0, host.authorizeCalls)
        assertEquals(0, host.getSettingCalls)
    }

    private fun TestScope.permissionsWith(
        host: FakeWechatPermissionHost,
        scopeOf: (PermissionKey) -> String = WechatPermissionScopes::scopeFor,
    ): WechatPermissions = WechatPermissions(
        host = host,
        scopeOf = scopeOf,
        // Mirrors production, where the host calls live under a supervisor so a
        // failed call is delivered to its callers and does not cancel the scope.
        hostCalls = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)),
    )

    private companion object {
        private const val RECORD = "scope.record"
        private const val VOICE = "scope.voice"

        private val MICROPHONE = PermissionKey.Microphone
        private val SPEAKER = PermissionKey("speaker")

        /** A mapping with two entries, so the merge rule can be exercised. */
        private val TWO_PERMISSIONS: (PermissionKey) -> String = { permission ->
            if (permission == MICROPHONE) RECORD else VOICE
        }
    }
}
