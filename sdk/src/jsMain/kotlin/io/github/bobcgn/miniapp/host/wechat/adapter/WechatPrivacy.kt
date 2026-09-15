package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.async.awaitHostCallback
import io.github.bobcgn.miniapp.capability.privacy.MiniAppPrivacy
import io.github.bobcgn.miniapp.capability.privacy.PrivacyAuthorizationOutcome
import io.github.bobcgn.miniapp.capability.privacy.PrivacyAuthorizationRequirement
import io.github.bobcgn.miniapp.capability.privacy.PrivacyStatus
import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.interop.WxPrivacyRequirement
import io.github.bobcgn.miniapp.host.wechat.interop.wxPrivacyRequirement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async

/**
 * WeChat implementation of the platform-neutral privacy authorization.
 *
 * Kept deliberately apart from [WechatPermissions]: WeChat tracks a privacy
 * contract and a system permission separately, prompts for them separately, and
 * reports them separately, so one adapter must not stand in for the other.
 *
 * Nothing is cached. The host's requirement changes when the user answers its
 * prompt and when the mini program's declared collection changes, so every call
 * asks the host.
 *
 * @param host callback port this adapter drives
 * @param hostCalls scope that owns an in-flight authorization. It is injected so a
 *   test can drive the interleaving deterministically; production uses its own
 *   scope, because the call must outlive whichever caller started it.
 */
internal class WechatPrivacy(
    private val host: WechatPrivacyHost = WxPrivacyHost,
    private val hostCalls: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : MiniAppPrivacy {
    /**
     * The authorization request currently in flight, if any.
     *
     * WeChat owns one privacy prompt, so a second request while one is running
     * joins it rather than asking the host twice; every caller then receives the
     * same answer. The host offers no way to cancel the prompt, so this is not an
     * abort handle and cancellation is never reported as cancelling the host.
     */
    private var inFlight: Deferred<PrivacyAuthorizationOutcome>? = null

    override suspend fun status(): PrivacyStatus {
        requireSupported()

        val requirement = awaitHostCallback { success, failure ->
            host.getPrivacySetting(
                success = { result -> success(wxPrivacyRequirement(result)) },
                failure = { result ->
                    failure(mapWechatHostFailure(operation = "getPrivacySetting", result = result))
                },
            )
            null
        }

        return when (requirement) {
            is WxPrivacyRequirement.Required -> PrivacyStatus(
                requirement = PrivacyAuthorizationRequirement.REQUIRED,
                contractName = requirement.contractName,
            )

            is WxPrivacyRequirement.NotRequired -> PrivacyStatus(
                requirement = PrivacyAuthorizationRequirement.NOT_REQUIRED,
                contractName = requirement.contractName,
            )

            WxPrivacyRequirement.Unreadable -> throw MiniAppException.InvalidResponse(
                "The WeChat host answered getPrivacySetting with a value the SDK cannot read",
            )
        }
    }

    override suspend fun requestAuthorization(): PrivacyAuthorizationOutcome {
        requireSupported()

        // No status query is made first: the host answers success immediately when
        // it requires nothing, so a preceding query would only double the calls.
        val pending = inFlight ?: hostCalls.async {
            performAuthorization()
        }.also { call ->
            // No suspension between reading and writing, so registering cannot race
            // with another caller doing the same.
            inFlight = call
            call.invokeOnCompletion { inFlight = null }
        }

        return pending.await()
    }

    override suspend fun requireSatisfied() {
        val current = status()
        if (current.requirement == PrivacyAuthorizationRequirement.REQUIRED) {
            throw MiniAppException.PrivacyAuthorizationRequired(
                contractName = current.contractName,
            )
        }
    }

    private suspend fun performAuthorization(): PrivacyAuthorizationOutcome =
        awaitHostCallback { success, failure ->
            host.requirePrivacyAuthorize(
                success = { success(PrivacyAuthorizationOutcome.Authorized) },
                failure = { result ->
                    // The failure path carries both user answers and operational
                    // failures, so the mapper accepts only a recognizable refusal.
                    when (val classified = mapWechatPrivacyAuthorizeFailure(result)) {
                        WxPrivacyAuthorizeFailure.Refused ->
                            success(PrivacyAuthorizationOutcome.Refused)

                        is WxPrivacyAuthorizeFailure.Failed ->
                            failure(classified.error)
                    }
                },
            )
            null
        }

    /**
     * Fails with the shared unsupported-capability error rather than letting a
     * missing host function surface as a JavaScript type error.
     */
    private fun requireSupported() {
        if (!host.isSupported()) {
            throw MiniAppException.UnsupportedCapability(MiniAppPrivacy.Key)
        }
    }
}
