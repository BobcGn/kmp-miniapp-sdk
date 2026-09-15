@file:OptIn(ExperimentalJsExport::class)

package io.github.bobcgn.miniapp.export

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * JavaScript-facing result of a completed HTTP exchange.
 *
 * [headers] is a flat sequence of alternating name and value strings. The
 * platform-neutral transport carries a Kotlin map, and Kotlin/JS collections
 * cannot cross this boundary because a JavaScript caller has no way to construct
 * one. The bundled CommonJS wrapper restores a plain object shape for consumers.
 */
@JsExport
public class MiniAppHttpResult internal constructor(
    /** HTTP status code reported by the host, including `4xx` and `5xx`. */
    public val statusCode: Int,
    /** Response headers as alternating `name`, `value` string entries. */
    public val headers: Array<String>,
    /** Response body decoded as text. */
    public val body: String,
)
