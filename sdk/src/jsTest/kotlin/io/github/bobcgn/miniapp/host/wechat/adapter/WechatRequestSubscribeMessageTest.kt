package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.WeChatSubscriptionRequest
import io.github.bobcgn.miniapp.host.wechat.WeChatSubscriptionStatus
import io.github.bobcgn.miniapp.host.wechat.WeChatTemplateId
import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.interop.WxRequestSubscribeMessageSuccessResult
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatRequestSubscribeMessageHost
import io.github.bobcgn.miniapp.host.wechat.testing.fakeSubscribeMessageSuccess
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * How the WeChat adapter turns the host's subscription callbacks into SDK behaviour.
 *
 * These are the raw-callback behaviours; `WechatSubscriptionContractTest` states
 * what must hold for the SDK as a whole.
 */
internal class WechatRequestSubscribeMessageTest {
    @Test
    fun theTemplateIdsReachTheHostInOrder() = runTest {
        val host = FakeWechatRequestSubscribeMessageHost(
            answer = fakeSubscribeMessageSuccess(
                "template-one" to "accept",
                "template-two" to "accept",
            ),
        )

        request(host, "template-one", "template-two")

        assertEquals(listOf("template-one", "template-two"), host.lastTemplateIds)
        assertEquals(1, host.calls)
    }

    @Test
    fun duplicateTemplateIdsAreSentOnce() = runTest {
        // Asking the host twice about one template means nothing, so duplicates are
        // collapsed here with the caller's order kept.
        val host = FakeWechatRequestSubscribeMessageHost(
            answer = fakeSubscribeMessageSuccess(
                "template-two" to "accept",
                "template-one" to "accept",
            ),
        )

        request(host, "template-two", "template-one", "template-two")

        assertEquals(listOf("template-two", "template-one"), host.lastTemplateIds)
    }

    @Test
    fun aRequestWithNoTemplateIsRejected() {
        val failure = assertFailsWith<IllegalArgumentException> {
            WeChatSubscriptionRequest(emptyList())
        }

        assertTrue(failure.message!!.contains("at least one template id"))
    }

    @Test
    fun aBlankTemplateIdIsRejected() {
        // A blank identifier names no template, so it is a caller mistake rather than
        // something to forward and let the host puzzle over.
        listOf("", "   ", "\t").forEach { blank ->
            val failure = assertFailsWith<IllegalArgumentException>("'$blank'") {
                WeChatSubscriptionRequest(listOf("template-one", blank))
            }

            assertTrue(failure.message!!.contains("non-blank"))
        }
    }

    @Test
    fun aRequestIsRefusedBeforeTheHostIsCalled() = runTest {
        val host = FakeWechatRequestSubscribeMessageHost()

        assertFailsWith<IllegalArgumentException> { request(host) }

        assertEquals(0, host.calls)
    }

    @Test
    fun noCountLimitIsImposedOnTheCaller() = runTest {
        // No offline source for this API states a limit, and this repository does not
        // encode a number it cannot cite, so the list is forwarded as the caller wrote it.
        val many = (1..12).map { "template-$it" }
        val host = FakeWechatRequestSubscribeMessageHost(
            answer = fakeSubscribeMessageSuccess(
                *many.map { it to "accept" }.toTypedArray(),
            ),
        )

        request(host, *many.toTypedArray())

        assertEquals(many, host.lastTemplateIds)
    }

    @Test
    fun anAcceptedTemplateIsReported() = runTest {
        val host = FakeWechatRequestSubscribeMessageHost(
            answer = fakeSubscribeMessageSuccess("template-one" to "accept"),
        )

        val results = request(host, "template-one")

        assertEquals(1, results.size)
        assertEquals("template-one", results[0].templateId)
        assertEquals(WeChatSubscriptionStatus.ACCEPT, results[0].status)
        assertEquals("accept", results[0].hostStatus)
    }

    @Test
    fun aStatusTheSdkCannotNameIsPreservedVerbatim() = runTest {
        // This SDK can name only the statuses it could evidence, and `reject` was not
        // among them. The host's own word is kept rather than guessed at or dropped,
        // so the caller can act on what the host actually said.
        val host = FakeWechatRequestSubscribeMessageHost(
            answer = fakeSubscribeMessageSuccess("template-one" to "reject"),
        )

        val result = request(host, "template-one")[0]

        assertNull(result.status)
        assertEquals("reject", result.hostStatus)
    }

    @Test
    fun everyUnnamedStatusIsPreservedTheSameWay() = runTest {
        // Deliberately covers the values a device is likely to produce, so the policy
        // is pinned whichever of them the host turns out to use.
        listOf("reject", "ban", "filter", "accept-and-something-new").forEach { status ->
            val host = FakeWechatRequestSubscribeMessageHost(
                answer = fakeSubscribeMessageSuccess("template-one" to status),
            )

            val result = request(host, "template-one")[0]

            assertNull(result.status, status)
            assertEquals(status, result.hostStatus, status)
        }
    }

    @Test
    fun aMissingRequestedTemplateIsAnInvalidResponse() = runTest {
        val host = FakeWechatRequestSubscribeMessageHost(
            answer = fakeSubscribeMessageSuccess("template-one" to "accept"),
        )

        assertFailsWith<MiniAppException.InvalidResponse> {
            request(host, "template-one", "template-two")
        }
    }

    @Test
    fun anUnexpectedTemplateIsAnInvalidResponse() = runTest {
        val host = FakeWechatRequestSubscribeMessageHost(
            answer = fakeSubscribeMessageSuccess(
                "template-one" to "accept",
                "template-someone-else" to "ban",
            ),
        )

        assertFailsWith<MiniAppException.InvalidResponse> {
            request(host, "template-one")
        }
    }

    @Test
    fun anEmptyAnswerIsAnInvalidResponse() = runTest {
        val host = FakeWechatRequestSubscribeMessageHost(answer = fakeSubscribeMessageSuccess())

        assertFailsWith<MiniAppException.InvalidResponse> {
            request(host, "template-one")
        }
    }

    @Test
    fun aBlankStatusIsAnInvalidResponse() = runTest {
        listOf("", "   ", "\t").forEach { status ->
            val host = FakeWechatRequestSubscribeMessageHost(
                answer = fakeSubscribeMessageSuccess("template-one" to status),
            )

            assertFailsWith<MiniAppException.InvalidResponse> {
                request(host, "template-one")
            }
        }
    }

    @Test
    fun aStatusThatIsNotTextIsAnInvalidResponse() = runTest {
        val shapes = listOf<Any?>(7, true, null)

        shapes.forEach { status ->
            val host = FakeWechatRequestSubscribeMessageHost(
                answer = fakeSubscribeMessageSuccess("template-one" to status),
            )

            assertFailsWith<MiniAppException.InvalidResponse>("status $status") {
                request(host, "template-one")
            }
        }
    }

    @Test
    fun aStatusThatIsNotTextForAnUnexpectedTemplateIsInvalid() = runTest {
        val host = FakeWechatRequestSubscribeMessageHost(
            answer = fakeSubscribeMessageSuccess(
                "template-one" to "accept",
                "template-unasked" to 7,
            ),
        )

        assertFailsWith<MiniAppException.InvalidResponse> {
            request(host, "template-one")
        }
    }

    @Test
    fun anAnswerThatIsNotAnObjectIsAnInvalidResponse() = runTest {
        assertFailsWith<MiniAppException.InvalidResponse> {
            request(MalformedAnswerHost(), "template-one")
        }
    }

    @Test
    fun conventionalCancelMessagesRemainHostFailuresUntilObserved() = runTest {
        // Scan and media messages are not evidence for this API. These candidates stay
        // ordinary host failures until a real subscription request produces one.
        listOf(
            "requestSubscribeMessage:cancel",
            "requestSubscribeMessage:fail cancel",
        ).forEach { message ->
            val host = FakeWechatRequestSubscribeMessageHost()
                .apply { failureMessage = message }

            val failure =
                assertFailsWith<MiniAppException.HostFailure>(message) {
                    request(host, "template-one")
                }

            assertEquals("requestSubscribeMessage", failure.metadata["operation"])
            assertEquals(message, failure.hostMessage)
        }
    }

    @Test
    fun aFailureThatMerelyMentionsCancellingIsAHostFailure() = runTest {
        // No cancellation vocabulary is classified for this API yet. A loose match
        // would turn an unverified host failure into an invented user outcome.
        val nearMisses = listOf(
            "requestSubscribeMessage:fail user cancel",
            "requestSubscribeMessage:fail cancel by system",
            "requestSubscribeMessage:cancelX",
            "RequestSubscribeMessage:cancel",
            "requestsubscribemessage:cancel",
            "requestSubscribeMessage:fail",
            "requestSubscribeMessage:fail canceled",
            "requestSubscribeMessage:fail auth deny",
            "requestSubscribeMessage:fail template not found",
            "cancel",
            "",
        )

        nearMisses.forEach { message ->
            val host = FakeWechatRequestSubscribeMessageHost().apply { failureMessage = message }

            val failure = assertFailsWith<MiniAppException.HostFailure>("'$message'") {
                request(host, "template-one")
            }

            assertEquals("requestSubscribeMessage", failure.metadata["operation"])
            assertEquals(message, failure.hostMessage)
        }
    }

    @Test
    fun aHostWithoutTheApiIsAnUnsupportedCapability() = runTest {
        val host = FakeWechatRequestSubscribeMessageHost(supported = false)

        val failure = assertFailsWith<MiniAppException.UnsupportedCapability> {
            request(host, "template-one")
        }

        assertEquals("wechat.request-subscribe-message", failure.capability.value)
        // The host was never asked, so no prompt could have appeared.
        assertEquals(0, host.calls)
    }

    @Test
    fun aRepeatedTerminalCallbackCompletesOnlyOnce() = runTest {
        val host = FakeWechatRequestSubscribeMessageHost().apply { completeTwice = true }

        assertEquals(1, request(host, "template-one").size)
    }

    @Test
    fun aCallbackAfterCancellationIsIgnored() = runTest {
        val host = DeferredSubscriptionHost()
        val deferred = async(start = CoroutineStart.UNDISPATCHED) {
            WechatRequestSubscribeMessage(host).request(WeChatSubscriptionRequest(listOf("template-one")))
        }

        deferred.cancelAndJoin()
        host.succeed()

        assertTrue(deferred.isCancelled)
    }

    @Test
    fun aSecondRequestAsksTheHostAgain() = runTest {
        // Nothing about the previous answer is kept between calls.
        val host = FakeWechatRequestSubscribeMessageHost(
            answer = fakeSubscribeMessageSuccess("template-one" to "accept"),
        )
        val adapter = WechatRequestSubscribeMessage(host)

        adapter.request(WeChatSubscriptionRequest(listOf("template-one")))

        host.answer = fakeSubscribeMessageSuccess("template-one" to "reject")
        val second = adapter.request(WeChatSubscriptionRequest(listOf("template-one")))

        assertEquals("reject", second[0].hostStatus)
        assertEquals(2, host.calls)
    }

    private suspend fun request(
        host: WechatRequestSubscribeMessageHost,
        vararg templateIds: WeChatTemplateId,
    ) = WechatRequestSubscribeMessage(host).request(WeChatSubscriptionRequest(templateIds.toList()))

    /** A port that answers with a value that is not an object at all. */
    private class MalformedAnswerHost : WechatRequestSubscribeMessageHost {
        override fun isSupported(): Boolean = true

        override fun request(
            templateIds: List<WeChatTemplateId>,
            success: (WxRequestSubscribeMessageSuccessResult) -> Unit,
            failure: (WxGeneralCallbackResult) -> Unit,
        ) {
            val notAnObject: WxRequestSubscribeMessageSuccessResult =
                js("'requestSubscribeMessage:ok'")
            success(notAnObject)
        }
    }

    /** A port that stays silent until the test completes it. */
    private class DeferredSubscriptionHost : WechatRequestSubscribeMessageHost {
        private var succeedCallback: ((WxRequestSubscribeMessageSuccessResult) -> Unit)? = null

        fun succeed() {
            val callback = requireNotNull(succeedCallback) {
                "The adapter has not started a request yet"
            }
            callback(fakeSubscribeMessageSuccess("template-one" to "accept"))
        }

        override fun isSupported(): Boolean = true

        override fun request(
            templateIds: List<WeChatTemplateId>,
            success: (WxRequestSubscribeMessageSuccessResult) -> Unit,
            failure: (WxGeneralCallbackResult) -> Unit,
        ) {
            succeedCallback = success
        }
    }
}
