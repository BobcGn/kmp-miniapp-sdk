package io.github.bobcgn.miniapp.host.wechat

import io.github.bobcgn.miniapp.capability.CapabilityKey

/**
 * Whether the WeChat login session is still usable.
 *
 * This is deliberately WeChat-specific and deliberately narrow. It reports what
 * `wx.checkSession` reports: whether the client login state WeChat issued is
 * still within the lifetime WeChat defines. It says nothing about the user,
 * about a consumer's backend session, or about any credential a consumer holds.
 *
 * A common, host-neutral authentication concept is not modelled, because no other
 * host has been shown to share these semantics and a generic `isAuthenticated()`
 * would promise a guarantee this API cannot give.
 */
public enum class WeChatSessionState {
    /** WeChat still holds a usable login session for this mini program. */
    VALID,

    /** WeChat reports the login session as no longer usable. */
    INVALID;

    public companion object {
        /**
         * Stable identity used for host capability-support queries.
         *
         * The key is namespaced because session checking is a WeChat condition
         * rather than a capability every host can be asked for.
         */
        public val Key: CapabilityKey = CapabilityKey("wechat.check-session")
    }
}
