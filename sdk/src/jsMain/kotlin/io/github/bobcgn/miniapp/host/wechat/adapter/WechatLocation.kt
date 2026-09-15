package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.async.awaitHostCallback
import io.github.bobcgn.miniapp.capability.permission.MiniAppPermissions
import io.github.bobcgn.miniapp.capability.permission.PermissionKey
import io.github.bobcgn.miniapp.capability.permission.PermissionState
import io.github.bobcgn.miniapp.capability.privacy.MiniAppPrivacy
import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.WeChatCoordinateSystem
import io.github.bobcgn.miniapp.host.wechat.WeChatDeviceCapabilities
import io.github.bobcgn.miniapp.host.wechat.WeChatGeoPosition
import io.github.bobcgn.miniapp.host.wechat.interop.WxLocationSample
import io.github.bobcgn.miniapp.host.wechat.interop.wxLocationSample

/**
 * WeChat position access, deliberately WeChat-specific.
 *
 * The coordinate system is a WeChat concept, so the whole capability stays behind
 * the platform escape hatch rather than pretending to be host-neutral. It reads a
 * position once; it does not watch, cache, or upload one.
 *
 * Three separate questions decide whether a call succeeds, and this keeps them
 * apart: whether the API exists at all ([WeChatDeviceCapabilities.Location]),
 * whether `scope.userLocation` is granted, and whether the host's privacy
 * contract has been accepted. The permission lifecycle is the caller's to drive;
 * this adapter queries both preconditions but never requests either one. That
 * prevents a position read from becoming an implicit prompt.
 *
 * WeChat reports no task handle for this call, so coroutine cancellation only
 * stops the caller waiting; the host operation is not aborted and no abort hook
 * is invented for it.
 */
internal class WechatLocation(
    private val host: WechatLocationHost = WxLocationHost,
    private val privacy: MiniAppPrivacy,
    private val permissions: MiniAppPermissions,
) {
    /**
     * Obtains the device's current position.
     *
     * The host's privacy contract is a precondition of the call rather than an
     * errand the caller is trusted to have run first, so it is checked here. That
     * check only queries: it presents nothing and accepts nothing on the user's
     * behalf.
     *
     * @throws MiniAppException.UnsupportedCapability when the host has no position API
     * @throws MiniAppException.PrivacyAuthorizationRequired when the host still
     *   requires the user to accept its privacy contract
     * @throws MiniAppException.PermissionDenied when the user has refused
     *   `scope.userLocation`
     * @throws MiniAppException.InvalidResponse when the host answers with something
     *   that is not a usable coordinate
     * @throws MiniAppException.HostFailure when the host cannot obtain a position
     */
    suspend fun currentPosition(
        coordinateSystem: WeChatCoordinateSystem = WeChatCoordinateSystem.GCJ02,
    ): WeChatGeoPosition {
        if (!host.isSupported()) {
            throw MiniAppException.UnsupportedCapability(WeChatDeviceCapabilities.Location)
        }

        privacy.requireSatisfied()

        // A location read must never become an implicit permission prompt. The
        // consumer owns that user-gesture flow through MiniAppPermissions, so
        // this adapter only proceeds after the host reports an existing grant.
        when (permissions.stateOf(PermissionKey.Location)) {
            PermissionState.Granted -> Unit
            PermissionState.NotRequested -> throw MiniAppException.PermissionDenied(
                permission = PermissionKey.Location.value,
                message = "Location permission has not been requested",
            )
            PermissionState.Denied -> throw MiniAppException.PermissionDenied(
                permission = PermissionKey.Location.value,
                message = "Location permission was denied",
            )
        }

        val sample = awaitHostCallback { success, failure ->
            host.currentPosition(
                coordinateSystem = coordinateSystem,
                success = { result -> success(wxLocationSample(result)) },
                failure = { result ->
                    failure(mapWechatHostFailure(operation = "getLocation", result = result))
                },
            )
            null
        }

        return when (sample) {
            is WxLocationSample.Present -> WeChatGeoPosition(
                latitude = sample.latitude,
                longitude = sample.longitude,
                accuracyMeters = sample.accuracyMeters,
                coordinateSystem = coordinateSystem,
            )

            WxLocationSample.Unreadable -> throw MiniAppException.InvalidResponse(
                "The WeChat host answered getLocation with coordinates the SDK cannot read",
            )
        }
    }
}
