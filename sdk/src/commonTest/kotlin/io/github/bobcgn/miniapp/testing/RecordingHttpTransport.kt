package io.github.bobcgn.miniapp.testing

import io.github.bobcgn.miniapp.capability.network.MiniAppHttpRequest
import io.github.bobcgn.miniapp.capability.network.MiniAppHttpResponse
import io.github.bobcgn.miniapp.capability.network.MiniAppHttpTransport

/**
 * Host-neutral [MiniAppHttpTransport] that records what it was asked to send and
 * replies with one scripted response.
 *
 * This is the reference implementation the shared transport contract is written
 * against. It deliberately performs no inspection or transformation of the
 * request, so a check can distinguish "the contract carried this" from "the
 * implementation rewrote it".
 *
 * @param response the exchange result every call resolves with
 */
internal class RecordingHttpTransport(
    private val response: MiniAppHttpResponse = MiniAppHttpResponse(
        statusCode = 200,
        headers = emptyMap(),
        body = "ok",
    ),
) : MiniAppHttpTransport {
    /** Every request this transport has been asked to perform, in order. */
    val requests: MutableList<MiniAppHttpRequest> = mutableListOf()

    override suspend fun request(request: MiniAppHttpRequest): MiniAppHttpResponse {
        requests += request
        return response
    }
}
