package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.capability.permission.PermissionKey

/**
 * The single boundary where a host-neutral permission becomes a WeChat scope.
 *
 * A `scope.*` string exists nowhere else in the SDK, and a key that is not listed
 * here is refused instead of being forwarded to `wx.authorize`: the host would
 * treat an unknown scope as a request rather than as a mistake the caller can fix.
 *
 * Only `scope.record` is mapped. It is the scope WeChat's own `wx.authorize`
 * documentation demonstrates, it needs no declaration in `app.json` (unlike
 * `scope.userLocation`, which also requires a stated purpose and an entry in
 * `requiredPrivateInfos`), and its decision is an ordinary runtime permission on
 * both mobile platforms, so all three states are reachable during acceptance.
 * Later capabilities add their own mapping when they are implemented.
 */
internal object WechatPermissionScopes {
    private val scopes: Map<PermissionKey, String> = mapOf(
        PermissionKey.Microphone to "scope.record",
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
