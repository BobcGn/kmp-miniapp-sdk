package io.github.bobcgn.miniapp.async

import io.github.bobcgn.miniapp.error.MiniAppException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

internal class HostCallbackAdapterTest {
    @Test
    fun successCallbackCompletesCoroutine() = runTest {
        val result = awaitHostCallback<String> { success, _ ->
            success("value")
            null
        }

        assertEquals("value", result)
    }

    @Test
    fun failureCallbackThrowsExpectedMiniAppException() = runTest {
        val expected = MiniAppException.HostFailure(
            host = "fake",
            code = "E_FAIL",
            hostMessage = "failed",
        )

        val actual = assertFailsWith<MiniAppException.HostFailure> {
            awaitHostCallback<String> { _, failure ->
                failure(expected)
                null
            }
        }

        assertEquals(expected, actual)
    }

    @Test
    fun callbackAfterCancellationIsIgnored() = runTest {
        lateinit var succeed: (String) -> Unit
        val deferred = async(start = CoroutineStart.UNDISPATCHED) {
            awaitHostCallback<String> { success, _ ->
                succeed = success
                null
            }
        }

        deferred.cancelAndJoin()
        succeed("late")

        assertTrue(deferred.isCancelled)
    }

    @Test
    fun onlyFirstTerminalCallbackCompletesCoroutine() = runTest {
        lateinit var succeed: (String) -> Unit
        lateinit var fail: (MiniAppException) -> Unit
        val deferred = async(start = CoroutineStart.UNDISPATCHED) {
            awaitHostCallback<String> { success, failure ->
                succeed = success
                fail = failure
                null
            }
        }

        succeed("first")
        fail(MiniAppException.InternalFailure("late failure"))

        assertEquals("first", deferred.await())
    }

    @Test
    fun cancellationInvokesAbortHookOnlyOnce() = runTest {
        var abortCount = 0
        val deferred = async(start = CoroutineStart.UNDISPATCHED) {
            awaitHostCallback<String> { _, _ ->
                HostOperationAborter { abortCount += 1 }
            }
        }

        deferred.cancel()
        deferred.cancelAndJoin()

        assertEquals(1, abortCount)
    }

    @Test
    fun registrationFailureIsMappedToInternalFailure() = runTest {
        val actual = assertFailsWith<MiniAppException.InternalFailure> {
            awaitHostCallback<String> { _, _ -> error("registration failed") }
        }

        assertEquals("registration failed", actual.cause?.message)
    }
}
