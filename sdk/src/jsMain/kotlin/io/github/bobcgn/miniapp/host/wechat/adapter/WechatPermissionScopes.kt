package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.capability.permission.PermissionKey

/**
 * The single boundary where a host-neutral permission becomes a WeChat scope.
 *
 * A `scope.*` string exists nowhere else in the SDK, and a key that is not listed
 * here is refused instead of being forwarded to `wx.authorize`: the host would
 * treat an unknown scope as a request rather than as a mistake the caller can fix.
 *
 * `scope.record` backs the microphone. It needs no declaration in `app.json` and
 * its decision is an ordinary runtime permission on both mobile platforms.
 *
 * `scope.userLocation` backs location. WeChat additionally requires the mini
 * program to state a purpose in `app.json.permission` and to list `getLocation`
 * in `app.json.requiredPrivateInfos`, and the location interfaces need a category
 * and an interface activation in the MP backend; none of that changes the scope
 * itself, which is still the string the permission lifecycle asks about.
 */
internal object WechatPermissionScopes {
    private val scopes: Map<PermissionKey, String> = mapOf(
        PermissionKey.Microphone to "scope.record",
        PermissionKey.Location to "scope.userLocation",
    )

    /**
     * Returns the WeChat scope that backs [permission].
     *
     * @throws IllegalArgumentException when this adapter has no mapping for the key,
     *   which is a caller mistake rather than a host condition
     */
    fun scopeFor(permission: PermissionKey): String =
        scopes[permission] ?: throw IllegalArgumentException(
            "The WeChat adapter has no scope mapping for permission '${permission.value}'",
        )
}
