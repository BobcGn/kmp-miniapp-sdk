package io.github.bobcgn.miniapp.testing

import io.github.bobcgn.miniapp.capability.privacy.MiniAppPrivacy
import io.github.bobcgn.miniapp.capability.privacy.PrivacyAuthorizationOutcome
import io.github.bobcgn.miniapp.capability.privacy.PrivacyAuthorizationRequirement
import io.github.bobcgn.miniapp.error.MiniAppException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

/**
 * The privacy authorization contract, expressed once for every host.
 *
 * What the host requires depends on the user and on the mini program's declared
 * collection, so each check states what must hold for a requirement the caller has
 * already arranged. The WeChat adapter runs the same checks in
 * `WechatPrivacyContractTest`.
 */
internal object PrivacyContractChecks {
    /** The host's stated requirement is reported unchanged, with its contract name. */
    suspend fun theHostsRequirementIsReportedUnchanged(
        privacy: MiniAppPrivacy,
        expected: PrivacyAuthorizationRequirement,
        expectedContractName: String?,
    ) {
        val status = privacy.status()

        assertEquals(expected, status.requirement)
        assertEquals(expectedContractName, status.contractName)
    }

    /** Nothing is required, so the precondition point passes without doing anything. */
    suspend fun thePreconditionPassesWhenNothingIsRequired(privacy: MiniAppPrivacy) {
        privacy.requireSatisfied()
    }

    /**
     * Authorization is required, so the precondition point fails with the privacy
     * error rather than showing a prompt or inventing an outcome.
     */
    suspend fun thePreconditionFailsWhenAuthorizationIsRequired(privacy: MiniAppPrivacy) {
        assertFailsWith<MiniAppException.PrivacyAuthorizationRequired> {
            privacy.requireSatisfied()
        }
    }

    /** A refusal is the user's answer and is returned, not raised. */
    suspend fun aRefusalIsAnOutcomeRatherThanAnError(
        privacy: MiniAppPrivacy,
        expected: PrivacyAuthorizationOutcome,
    ) {
        val outcome = privacy.requestAuthorization()

        assertEquals(expected, outcome)
        assertIs<PrivacyAuthorizationOutcome.Refused>(outcome)
    }

    /** Accepting the contract clears the host's requirement. */
    suspend fun acceptingTheContractClearsTheRequirement(privacy: MiniAppPrivacy) {
        assertEquals(PrivacyAuthorizationOutcome.Authorized, privacy.requestAuthorization())

        assertEquals(
            PrivacyAuthorizationRequirement.NOT_REQUIRED,
            privacy.status().requirement,
        )
    }

    /** A refusal leaves the requirement in place; only the user can clear it. */
    suspend fun aRefusalLeavesTheRequirementInPlace(privacy: MiniAppPrivacy) {
        privacy.requestAuthorization()

        assertEquals(
            PrivacyAuthorizationRequirement.REQUIRED,
            privacy.status().requirement,
        )
    }
}
