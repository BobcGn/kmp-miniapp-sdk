@file:OptIn(ExperimentalJsExport::class)

package io.github.bobcgn.miniapp.export

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * JavaScript-facing view of what the host answered about one template.
 *
 * An answer says what the user decided about a template's messages. It never says
 * that a message was sent, was delivered, or will be: acceptance is a subscription
 * state, and delivery is the host's and the consumer backend's business.
 *
 * @property templateId the template this entry answers, echoed from the request
 * @property status the answer this SDK can name, or `null` when the host named one
 *   this SDK has no evidence for; [hostStatus] still carries it
 * @property hostStatus the host's own non-blank answer, verbatim
 */
@JsExport
public class JsSubscriptionResult internal constructor(
    /** The template this entry answers. */
    public val templateId: String,
    /** The answer resolved against this SDK's known set, when it recognizes it. */
    public val status: String?,
    /** The host's own answer, verbatim, when it reported one. */
    public val hostStatus: String,
)
