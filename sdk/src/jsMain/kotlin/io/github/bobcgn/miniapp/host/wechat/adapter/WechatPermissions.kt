package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.async.awaitHostCallback
import io.github.bobcgn.miniapp.capability.permission.MiniAppPermissions
import io.github.bobcgn.miniapp.capability.permission.PermissionKey
import io.github.bobcgn.miniapp.capability.permission.PermissionState
import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.interop.WxScopeEntry
import io.github.bobcgn.miniapp.host.wechat.interop.wxScopeEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async

/**
 * WeChat implementation of the platform-neutral permission lifecycle.
 *
 * Every answer comes from the host. Nothing is cached as a lasting fact, because
 * the user can change a permission in the host's own settings at any time and a
 * cached value would become a lie the moment they do.
 *
 * A refusal is reported as [MiniAppException.PermissionDenied], never as a host
 * failure: the host did exactly what it is supposed to do.
 *
 * @param host callback port this adapter drives
 * @param scopeOf maps a host-neutral permission to its WeChat scope. The
 *   production value is [WechatPermissionScopes], the single place a `scope.*`
 *   string exists; it is injectable so a test can exercise more than the one
 *   permission this SDK currently maps.
 * @param hostCalls scope that owns the in-flight host calls. It is injected so a
 *   test can drive the interleaving deterministically; production uses its own
 *   scope, because the calls must outlive whichever caller started them.
 */
internal class WechatPermissions(
    private val host: WechatPermissionHost = WxPermissionHost,
    private val scopeOf: (PermissionKey) -> String = WechatPermissionScopes::scopeFor,
    private val hostCalls: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : MiniAppPermissions {
    /**
     * The host call for one permission while it is running.
     *
     * A second request for a permission that is already being requested joins the
     * running call instead of starting another one, so the user sees one prompt
     * and every caller receives the same answer.
     */
    private val inFlight: MutableMap<PermissionKey, Deferred<PermissionState>> = mutableMapOf()

    override suspend fun stateOf(permission: PermissionKey): PermissionState =
        readState(permission, scopeOf(permission))

    override suspend fun request(permission: PermissionKey): PermissionState {
        // Resolved first so an unmapped permission fails the same way whether or
        // not a request for it is already running.
        val scope = scopeOf(permission)

        val pending = inFlight[permission] ?: hostCalls.async {
            performRequest(permission, scope)
        }.also { call ->
            // No suspension between reading and writing the map, so registering
            // cannot race with another caller doing the same.
            inFlight[permission] = call
            call.invokeOnCompletion { inFlight.remove(permission) }
        }

        return pending.await()
    }

    override suspend fun openSettings(permission: PermissionKey): PermissionState {
        val scope = scopeOf(permission)

        val reported = awaitHostCallback { success, failure ->
            host.openSetting(
                success = { result -> success(wxScopeEntry(result.authSetting, scope)) },
                failure = { result ->
                    failure(mapWechatHostFailure(operation = "openSetting", result = result))
                },
            )
            null
        }

        // Reaching here only means the settings page closed. The decision is the
        // host's, and a map that does not carry the scope is not a decision, so
        // the state is read from the host in that case rather than assumed.
        return stateFromEntry(reported) ?: readState(permission, scope)
    }

    private suspend fun performRequest(
        permission: PermissionKey,
        scope: String,
    ): PermissionState {
        // The host's own state decides whether asking again is worth anything. A
        // granted permission would not prompt a second time, and a refused one
        // cannot be re-prompted at all, so both are answered from the state rather
        // than from a host call whose outcome is already known. Only an undecided
        // permission can still produce a prompt.
        val current = readState(permission, scope)
        if (current == PermissionState.Granted) return current
        if (current == PermissionState.Denied) {
            throw MiniAppException.PermissionDenied(
                permission = permission.value,
                message = "The host has already refused permission '${permission.value}'",
            )
        }

        awaitHostCallback { success, failure ->
            host.authorize(
                scope = scope,
                success = { success(Unit) },
                failure = { result -> failure(mapWechatAuthorizeFailure(permission, result)) },
            )
            null
        }

        // The host only reports success once it has recorded the permission, so
        // the answer is known without a second query.
        return PermissionState.Granted
    }

    private suspend fun readState(
        permission: PermissionKey,
        scope: String,
    ): PermissionState {
        val entry = awaitHostCallback { success, failure ->
            host.getSetting(
                success = { result -> success(wxScopeEntry(result.authSetting, scope)) },
                failure = { result ->
                    failure(mapWechatHostFailure(operation = "getSetting", result = result))
                },
            )
            null
        }

        return stateFromEntry(entry) ?: throw MiniAppException.InvalidResponse(
            "The WeChat host answered getSetting with a value the SDK cannot read " +
                "for permission '${permission.value}'",
        )
    }

    /**
     * Converts a host scope entry into a state, or `null` when the entry is not an
     * answer the contract can carry.
     *
     * The host scope name is deliberately not part of any message produced here:
     * diagnostics use the host-neutral permission key, so a WeChat scope string
     * never reaches the shared error model.
     */
    private fun stateFromEntry(entry: WxScopeEntry): PermissionState? = when (entry) {
        WxScopeEntry.Absent -> PermissionState.NotRequested

        WxScopeEntry.Unreadable -> null

        is WxScopeEntry.Decided -> if (entry.granted) {
            PermissionState.Granted
        } else {
            PermissionState.Denied
        }
    }
}
