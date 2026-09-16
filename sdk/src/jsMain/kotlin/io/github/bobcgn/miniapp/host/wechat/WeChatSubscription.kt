package io.github.bobcgn.miniapp.host.wechat

/**
 * One template identifier a subscription request may ask about.
 *
 * A template ID is an opaque host identifier. This SDK never invents one, never
 * derives one from content, and never logs one: what a caller passes is what the
 * host is asked about, and what comes back is correlated by the same value.
 */
internal typealias WeChatTemplateId = String

/**
 * A per-template answer this SDK is able to name.
 *
 * **Evidence and its limits.** WeChat does not document this vocabulary in
 * anything this repository can cite offline. The base library shipped with the
 * installed WeChat Developer Tools declares the API's option (`tmplIds`) and states
 * that the success result is keyed by template ID with `errMsg` beside it, but it
 * names no status value and ships no implementation of this API to read one from.
 * The single status string observed anywhere in that bundle is `accept`, in the
 * simulator's own subscription-prompt preview payload, which is prompt-rendering
 * data rather than an API result.
 *
 * This enumeration therefore contains only what could be evidenced, and it is
 * deliberately short. A status the host reports that is not a member is **not**
 * guessed at: it is preserved verbatim on [WeChatSubscriptionResult.hostStatus]
 * with [WeChatSubscriptionResult.status] left `null`, so a caller can act on what
 * the host actually said and a real-host run can extend this set from evidence.
 * That is the same policy the scan-format and media-kind vocabularies follow.
 */
internal enum class WeChatSubscriptionStatus(
    /** The value the host reports for this answer. */
    internal val hostValue: String,
) {
    /** The user allowed this template. It says nothing about delivery. */
    ACCEPT("accept"),
    ;

    internal companion object {
        /** Returns the status [hostValue] names, or `null` when the SDK cannot cite one. */
        internal fun fromHostValue(hostValue: String): WeChatSubscriptionStatus? =
            entries.firstOrNull { it.hostValue == hostValue }
    }
}

/**
 * What one subscription request asks the host for.
 *
 * The SDK cannot trigger this itself. WeChat requires the request to come from a
 * user gesture, so the caller owns that: this model describes what to ask about,
 * and the adapter never calls it on page load or from any path the caller did not
 * initiate. A consumer that calls it outside a user gesture gets whatever the host
 * does with it, and that failure is the host's answer, not something this SDK
 * works around.
 *
 * A template ID must be non-blank: an empty identifier names no template, so it is
 * a caller mistake and is refused here rather than being sent. Duplicate
 * identifiers are collapsed with the caller's order preserved, because asking the
 * host twice about one template means nothing.
 *
 * No count limit is enforced: the offline sources for this API state none, and
 * this repository does not encode a number it cannot cite. The host applies its own
 * limit.
 *
 * @property templateIds the templates to ask about, de-duplicated in caller order
 */
internal class WeChatSubscriptionRequest(
    templateIds: List<WeChatTemplateId>,
) {
    internal val templateIds: List<WeChatTemplateId> = templateIds.distinct()

    init {
        require(templateIds.isNotEmpty()) {
            "requestSubscribeMessage requires at least one template id, because a request with none asks nothing"
        }
        require(templateIds.all { it.isNotBlank() }) {
            "requestSubscribeMessage template ids must be non-blank, because a blank id names no template"
        }
    }
}

/**
 * The host's answer about one template the caller asked about.
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
internal class WeChatSubscriptionResult(
    internal val templateId: WeChatTemplateId,
    internal val status: WeChatSubscriptionStatus?,
    internal val hostStatus: String,
)
