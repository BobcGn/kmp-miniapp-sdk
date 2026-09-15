package io.github.bobcgn.miniapp.testing

import io.github.bobcgn.miniapp.capability.privacy.MiniAppPrivacy
import io.github.bobcgn.miniapp.capability.privacy.PrivacyAuthorizationOutcome
import io.github.bobcgn.miniapp.capability.privacy.PrivacyAuthorizationRequirement
import io.github.bobcgn.miniapp.capability.privacy.PrivacyStatus
import io.github.bobcgn.miniapp.error.MiniAppException

/**
 * Host-neutral [MiniAppPrivacy] used to express the privacy contract.
 *
 * It reports what a real host would and applies the same rule an adapter does: the
 * precondition gate fails while the host requires authorization, and accepting the
 * contract clears that requirement.
 *
 * @param status what this host currently reports
 * @param outcome what the next authorization attempt will report
 */
internal class FakeMiniAppPrivacy(
    var status: PrivacyStatus = PrivacyStatus(
        requirement = PrivacyAuthorizationRequirement.NOT_REQUIRED,
        contractName = null,
    ),
    var outcome: PrivacyAuthorizationOutcome = PrivacyAuthorizationOutcome.Authorized,
) : MiniAppPrivacy {
    /** How many times the host was asked for its requirement. */
    var statusQueries: Int = 0
        private set

    /** How many authorization requests reached the host. */
    var authorizationRequests: Int = 0
        private set

    override suspend fun status(): PrivacyStatus {
        statusQueries += 1
        return status
    }

    override suspend fun requestAuthorization(): PrivacyAuthorizationOutcome {
        authorizationRequests += 1
        if (outcome is PrivacyAuthorizationOutcome.Authorized) {
            // An accepted contract removes the host's requirement, as it does on a
            // real host; a refusal leaves it in place.
            status = PrivacyStatus(
                requirement = PrivacyAuthorizationRequirement.NOT_REQUIRED,
                contractName = status.contractName,
            )
        }
        return outcome
    }

    override suspend fun requireSatisfied() {
        val current = status()
        if (current.requirement == PrivacyAuthorizationRequirement.REQUIRED) {
            throw MiniAppException.PrivacyAuthorizationRequired(
                contractName = current.contractName,
            )
        }
    }
}
