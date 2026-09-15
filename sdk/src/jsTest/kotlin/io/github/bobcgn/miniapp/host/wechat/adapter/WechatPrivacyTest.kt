package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.capability.privacy.PrivacyAuthorizationOutcome
import io.github.bobcgn.miniapp.capability.privacy.PrivacyAuthorizationRequirement
import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatPrivacyHost
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
 * How the WeChat adapter turns the host's privacy callbacks into the
 * platform-neutral model, including what happens when calls overlap.
 *
 * The adapter's host calls run on the test scheduler, so an interleaving is
 * produced deliberately rather than by waiting.
 */
internal class WechatPrivacyTest {
    @Test
    fun aHostThatRequiresAuthorizationIsReportedAsSuch() = runTest {
        val host = FakeWechatPrivacyHost(needAuthorization = true, privacyContractName = CONTRACT)

        val status = privacyWith(host).status()

        assertEquals(PrivacyAuthorizationRequirement.REQUIRED, status.requirement)
        assertEquals(CONTRACT, status.contractName)
    }

    @Test
    fun aHostThatRequiresNothingIsReportedAsNotRequired() = runTest {
        val host = FakeWechatPrivacyHost(needAuthorization = false)

        assertEquals(
            PrivacyAuthorizationRequirement.NOT_REQUIRED,
            privacyWith(host).status().requirement,
        )
    }

    @Test
    fun aMissingContractNameIsReportedAsAbsent() = runTest {
        val host = FakeWechatPrivacyHost(needAuthorization = true, privacyContractName = null)

        assertEquals(null, privacyWith(host).status().contractName)
    }

    @Test
    fun anAnswerWithoutAUsableFlagIsAnInvalidResponse() = runTest {
        val host = FakeWechatPrivacyHost(needAuthorization = null)

        assertFailsWith<MiniAppException.InvalidResponse> { privacyWith(host).status() }
    }

    @Test
    fun aFlagThatIsNotABooleanIsAnInvalidResponse() = runTest {
        val host = FakeWechatPrivacyHost(needAuthorization = "required")

        assertFailsWith<MiniAppException.InvalidResponse> { privacyWith(host).status() }
    }

    @Test
    fun aFailedStatusQueryIsAHostFailure() = runTest {
        val host = FakeWechatPrivacyHost().apply {
            getPrivacySettingFailure = "getPrivacySetting:fail system error"
        }

        val failure = assertFailsWith<MiniAppException.HostFailure> { privacyWith(host).status() }

        assertEquals("getPrivacySetting", failure.metadata["operation"])
    }

    @Test
    fun aHostWithoutThePrivacyApisIsAnUnsupportedCapability() = runTest {
        val host = FakeWechatPrivacyHost(supported = false)

        val onStatus = assertFailsWith<MiniAppException.UnsupportedCapability> {
            privacyWith(host).status()
        }
        val onRequest = assertFailsWith<MiniAppException.UnsupportedCapability> {
            privacyWith(host).requestAuthorization()
        }

        // The shared capability error, rather than a JavaScript type error from a
        // missing host function.
        assertEquals("privacy", onStatus.capability.value)
        assertEquals("privacy", onRequest.capability.value)
        // Nothing reached the host.
        assertEquals(0, host.getPrivacySettingCalls)
        assertEquals(0, host.authorizeCalls)
    }

    @Test
    fun anAcceptedContractIsReportedAsAuthorized() = runTest {
        val host = FakeWechatPrivacyHost()

        assertEquals(PrivacyAuthorizationOutcome.Authorized, privacyWith(host).requestAuthorization())
        assertEquals(1, host.authorizeCalls)
    }

    @Test
    fun aDeclinedContractIsAnOutcomeRatherThanAnError() = runTest {
        // WeChat reports a declined contract through the failure callback.
        val host = FakeWechatPrivacyHost().apply {
            authorizeFailure = "requirePrivacyAuthorize:fail privacy permission is not authorized"
        }

        val outcome = privacyWith(host).requestAuthorization()

        assertEquals(PrivacyAuthorizationOutcome.Refused, outcome)
    }

    @Test
    fun anUnclassifiedAuthorizationFailureRemainsAHostFailure() = runTest {
        val host = FakeWechatPrivacyHost().apply {
            authorizeFailure = "requirePrivacyAuthorize:fail"
        }

        val failure = assertFailsWith<MiniAppException.HostFailure> {
            privacyWith(host).requestAuthorization()
        }

        assertEquals("requirePrivacyAuthorize", failure.metadata["operation"])
    }

    @Test
    fun anUndeclaredCollectionIsAHostFailureRatherThanARefusal() = runTest {
        // The host never had a prompt to show, so this is configuration rather
        // than the user's answer.
        val host = FakeWechatPrivacyHost().apply {
            authorizeFailure =
                "requirePrivacyAuthorize:fail api scope is not declared in the privacy agreement"
        }

        val failure = assertFailsWith<MiniAppException.HostFailure> {
            privacyWith(host).requestAuthorization()
        }

        assertEquals("requirePrivacyAuthorize", failure.metadata["operation"])
    }

    @Test
    fun aRefusalNeverBecomesAPermissionDenial() = runTest {
        val host = FakeWechatPrivacyHost().apply {
            authorizeFailure = "requirePrivacyAuthorize:fail privacy permission is not authorized"
        }

        val outcome = privacyWith(host).requestAuthorization()

        // A refusal is an answer, and privacy is not a system permission.
        assertIs<PrivacyAuthorizationOutcome.Refused>(outcome)
    }

    @Test
    fun aRepeatedTerminalCallbackCompletesOnlyOnce() = runTest {
        val host = FakeWechatPrivacyHost().apply { completeTwice = true }

        assertEquals(
            PrivacyAuthorizationOutcome.Authorized,
            privacyWith(host).requestAuthorization(),
        )
    }

    @Test
    fun concurrentRequestsShareOneHostCall() = runTest {
        val host = FakeWechatPrivacyHost().apply { holdAuthorize = true }
        val privacy = privacyWith(host)

        val first = async(start = CoroutineStart.UNDISPATCHED) { privacy.requestAuthorization() }
        val second = async(start = CoroutineStart.UNDISPATCHED) { privacy.requestAuthorization() }
        yield()

        // One prompt, one host call, one answer for both callers.
        assertEquals(1, host.authorizeCalls)

        host.releaseAuthorize()

        assertEquals(PrivacyAuthorizationOutcome.Authorized, first.await())
        assertEquals(PrivacyAuthorizationOutcome.Authorized, second.await())
    }

    @Test
    fun aCallerThatIsCancelledDoesNotStrandAnotherCaller() = runTest {
        val host = FakeWechatPrivacyHost().apply { holdAuthorize = true }
        val privacy = privacyWith(host)

        val abandoned = async(start = CoroutineStart.UNDISPATCHED) { privacy.requestAuthorization() }
        val waiting = async(start = CoroutineStart.UNDISPATCHED) { privacy.requestAuthorization() }
        yield()

        abandoned.cancelAndJoin()
        host.releaseAuthorize()

        // The host offers no abort, so cancellation stops only the caller that
        // asked for it, and the other still receives the host's answer.
        assertEquals(PrivacyAuthorizationOutcome.Authorized, waiting.await())
        assertTrue(abandoned.isCancelled)
    }

    @Test
    fun anInFlightAuthorizationIsClearedOnceItCompletes() = runTest {
        val host = FakeWechatPrivacyHost()
        val privacy = privacyWith(host)

        privacy.requestAuthorization()
        privacy.requestAuthorization()

        // A later request is a new host call rather than a remembered answer.
        assertEquals(2, host.authorizeCalls)
    }

    @Test
    fun aRequestOnANewlySatisfiedHostDoesNotQueryStatusFirst() = runTest {
        val host = FakeWechatPrivacyHost(needAuthorization = false)

        assertEquals(PrivacyAuthorizationOutcome.Authorized, privacyWith(host).requestAuthorization())

        // The host answers success immediately when it requires nothing, so a
        // preceding query would only double the host calls.
        assertEquals(0, host.getPrivacySettingCalls)
    }

    @Test
    fun thePreconditionPassesWhenTheHostRequiresNothing() = runTest {
        privacyWith(FakeWechatPrivacyHost(needAuthorization = false)).requireSatisfied()
    }

    @Test
    fun thePreconditionFailsWithTheContractNameWhenAuthorizationIsRequired() = runTest {
        val host = FakeWechatPrivacyHost(needAuthorization = true, privacyContractName = CONTRACT)

        val failure = assertFailsWith<MiniAppException.PrivacyAuthorizationRequired> {
            privacyWith(host).requireSatisfied()
        }

        assertEquals(CONTRACT, failure.contractName)
    }

    @Test
    fun thePreconditionNeverStartsAPromptByItself() = runTest {
        val host = FakeWechatPrivacyHost(needAuthorization = true)

        assertFailsWith<MiniAppException.PrivacyAuthorizationRequired> {
            privacyWith(host).requireSatisfied()
        }

        assertEquals(0, host.authorizeCalls)
    }

    private fun TestScope.privacyWith(host: FakeWechatPrivacyHost): WechatPrivacy =
        WechatPrivacy(
            host = host,
            // Mirrors production, where an in-flight call lives under a supervisor
            // so a failed call reaches its callers without cancelling the scope.
            hostCalls = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)),
        )

    private companion object {
        private const val CONTRACT = "《example privacy contract》"
    }
}
