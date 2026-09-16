@file:OptIn(ExperimentalJsExport::class)

package io.github.bobcgn.miniapp.export

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * What a completed WeChat payment interaction means, and nothing more.
 *
 * The host's success callback reports that the payment interaction finished. It is not an
 * order, not a receipt, and not proof that money moved, so the field below is the only
 * thing this type can say. The authoritative answer is the consumer backend's, from
 * WeChat Pay's server API, its asynchronous notification, or an order query — query it.
 *
 * @property interactionCompleted whether the host reported the payment interaction
 *   completed. It is `true` whenever this value exists at all: the field is present to
 *   name what resolving means, so that a caller cannot read a completed call as a
 *   completed order.
 */
@JsExport
public class JsPaymentOutcome internal constructor(
    /** Whether the host reported the payment interaction completed. */
    public val interactionCompleted: Boolean,
)
