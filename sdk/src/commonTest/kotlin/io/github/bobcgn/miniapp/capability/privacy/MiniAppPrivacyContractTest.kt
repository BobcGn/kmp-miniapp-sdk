package io.github.bobcgn.miniapp.capability.privacy

import io.github.bobcgn.miniapp.capability.CapabilityKey
import io.github.bobcgn.miniapp.capability.permission.MiniAppPermissions
import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.testing.FakeMiniAppPrivacy
import io.github.bobcgn.miniapp.testing.PrivacyContractChecks
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotEquals

/**
 * Runs the shared privacy contract against the host-neutral reference
 * implementation, plus the model guards that keep privacy apart from permission.
 *
 * The WeChat adapter runs the same checks in `WechatPrivacyContractTest`.
 */
internal class MiniAppPrivacyContractTest {
    @Test
    fun privacyIsNotPermission() {
        assertEquals(CapabilityKey("privacy"), MiniAppPrivacy.Key)
        // Two separate capabilities, so neither can stand in for the other.
        assertNotEquals(MiniAppPrivacy.Key, MiniAppPermissions.Key)
    }

    @Test
    fun aRequirementTheHostReportsIsReturnedUnchanged() = runTest {
        PrivacyContractChecks.theHostsRequirementIsReportedUnchanged(
            privacy = FakeMiniAppPrivacy(
                status = PrivacyStatus(PrivacyAuthorizationRequirement.REQUIRED, CONTRACT),
            ),
            expected = PrivacyAuthorizationRequirement.REQUIRED,
            expectedContractName = CONTRACT,
        )
    }

    @Test
    fun aMissingContractNameStaysMissing() = runTest {
        PrivacyContractChecks.theHostsRequirementIsReportedUnchanged(
            privacy = FakeMiniAppPrivacy(
                status = PrivacyStatus(PrivacyAuthorizationRequirement.NOT_REQUIRED, null),
            ),
            expected = PrivacyAuthorizationRequirement.NOT_REQUIRED,
            expectedContractName = null,
        )
    }

    @Test
    fun thePreconditionPassesWhenNothingIsRequired() = runTest {
        PrivacyContractChecks.thePreconditionPassesWhenNothingIsRequired(FakeMiniAppPrivacy())
    }

    @Test
    fun thePreconditionFailsWhenAuthorizationIsRequired() = runTest {
        PrivacyContractChecks.thePreconditionFailsWhenAuthorizationIsRequired(requiring())
    }

    @Test
    fun aPrivacyFailureIsNotAPermissionDenial() = runTest {
        // Widened on purpose: the point of the check is that the two failures are
        // unrelated, so the static type must not already decide the answer.
        val failure: MiniAppException = assertFailsWith<MiniAppException.PrivacyAuthorizationRequired> {
            requiring().requireSatisfied()
        }

        // A privacy contract is the host's own condition, so telling a consumer
        // otherwise would send it to ask for the wrong thing.
        assertNotEquals(true, failure is MiniAppException.PermissionDenied)
        assertEquals(CONTRACT, (failure as MiniAppException.PrivacyAuthorizationRequired).contractName)
    }

    @Test
    fun aRefusalIsAnOutcomeRatherThanAnError() = runTest {
        PrivacyContractChecks.aRefusalIsAnOutcomeRatherThanAnError(
            privacy = FakeMiniAppPrivacy(outcome = PrivacyAuthorizationOutcome.Refused),
            expected = PrivacyAuthorizationOutcome.Refused,
        )
    }

    @Test
    fun acceptingTheContractClearsTheRequirement() = runTest {
        PrivacyContractChecks.acceptingTheContractClearsTheRequirement(
            FakeMiniAppPrivacy(
                status = PrivacyStatus(PrivacyAuthorizationRequirement.REQUIRED, CONTRACT),
            ),
        )
    }

    @Test
    fun aRefusalLeavesTheRequirementInPlace() = runTest {
        PrivacyContractChecks.aRefusalLeavesTheRequirementInPlace(
            FakeMiniAppPrivacy(
                status = PrivacyStatus(PrivacyAuthorizationRequirement.REQUIRED, CONTRACT),
                outcome = PrivacyAuthorizationOutcome.Refused,
            ),
        )
    }

    @Test
    fun theGateQueriesTheHostRatherThanRememberingAnAnswer() = runTest {
        val privacy = FakeMiniAppPrivacy()

        repeat(3) { privacy.requireSatisfied() }

        assertEquals(3, privacy.statusQueries)
        assertEquals(0, privacy.authorizationRequests)
    }

    @Test
    fun theGateNeverRequestsAuthorizationOnItsOwn() = runTest {
        val privacy = requiring()

        assertFailsWith<MiniAppException.PrivacyAuthorizationRequired> {
            privacy.requireSatisfied()
        }

        // Showing a prompt is the consumer's job, from a user gesture.
        assertEquals(0, privacy.authorizationRequests)
    }

    private fun requiring(): FakeMiniAppPrivacy = FakeMiniAppPrivacy(
        status = PrivacyStatus(PrivacyAuthorizationRequirement.REQUIRED, CONTRACT),
    )

    private companion object {
        private const val CONTRACT = "《example privacy contract》"
    }
}
