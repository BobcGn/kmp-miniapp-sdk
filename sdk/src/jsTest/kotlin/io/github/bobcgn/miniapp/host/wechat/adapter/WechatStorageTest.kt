package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.error.MiniAppException
import io.github.bobcgn.miniapp.host.wechat.interop.WxGeneralCallbackResult
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatStorageHost
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Adapter-specific storage behavior: how the WeChat callback port is mapped onto
 * the SDK contract and onto the common error model.
 *
 * Contract semantics themselves are covered once by the shared contract checks,
 * which `WechatStorageContractTest` runs against this adapter.
 */
internal class WechatStorageTest {
    @Test
    fun hostFailureMapsToCommonError() = runTest {
        val host = FakeWechatStorageHost().apply {
            failNextCallWith("setStorage:fail quota exceeded")
        }

        val failure = assertFailsWith<MiniAppException.HostFailure> {
            WechatStorage(host).set("key", "value")
        }

        assertEquals("wechat", failure.host)
        assertEquals("setStorage:fail quota exceeded", failure.hostMessage)
        assertEquals("setStorage", failure.metadata["operation"])
    }

    @Test
    fun nonStringHostValueIsInvalidResponse() = runTest {
        val host = FakeWechatStorageHost().apply {
            putRawValue("key", 42)
        }

        assertFailsWith<MiniAppException.InvalidResponse> {
            WechatStorage(host).get("key")
        }
    }

    @Test
    fun callbackAfterCancellationIsIgnored() = runTest {
        val host = DeferredGetStorageHost()
        val deferred = async(start = CoroutineStart.UNDISPATCHED) {
            WechatStorage(host).get("key")
        }

        deferred.cancelAndJoin()
        host.succeed("late")

        assertTrue(deferred.isCancelled)
    }

    /** A port that stays silent until the test completes it, for cancellation scenarios. */
    private class DeferredGetStorageHost : WechatStorageHost {
        lateinit var succeed: (Any?) -> Unit

        override fun get(
            key: String,
            success: (Any?) -> Unit,
            failure: (WxGeneralCallbackResult) -> Unit,
        ) {
            succeed = success
        }

        override fun set(
            key: String,
            value: String,
            success: () -> Unit,
            failure: (WxGeneralCallbackResult) -> Unit,
        ): Unit = error("Not used")

        override fun remove(
            key: String,
            success: () -> Unit,
            failure: (WxGeneralCallbackResult) -> Unit,
        ): Unit = error("Not used")
    }
}
