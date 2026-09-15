@file:OptIn(ExperimentalJsExport::class)

package io.github.bobcgn.miniapp.host.wechat

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * Short-lived login credential returned by the WeChat Mini Program runtime.
 *
 * [code] is not an authenticated user identity, session, or SDK access token.
 * A consumer-controlled backend must exchange it with WeChat and establish its
 * own trusted session before treating the user as authenticated.
 */
@JsExport
public class WeChatLoginResult internal constructor(
    /** Raw client login code that must be sent to a trusted backend promptly. */
    public val code: String,
)
