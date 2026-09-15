package io.github.bobcgn.miniapp.error

import io.github.bobcgn.miniapp.capability.CapabilityKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertIsNot
import kotlin.test.assertNull

public class MiniAppExceptionTest {
    @Test
    public fun semanticErrorsPreserveUsefulContext(): Unit {
        val unsupported = MiniAppException.UnsupportedCapability(CapabilityKey("storage"))
        val permission = MiniAppException.PermissionDenied(permission = "location")
        val cancelled = MiniAppException.UserCancelled()
        val interrupted = MiniAppException.HostInteractionInterrupted(
            host = "test-host",
            operation = "scan",
            hostMessage = "scan:cancel",
        )
        val invalid = MiniAppException.InvalidResponse("Missing field")
        val timeout = MiniAppException.Timeout(
            operation = "request",
            hostMessage = "request:fail timeout",
        )

        assertEquals("storage", unsupported.capability.value)
        assertEquals("location", permission.permission)
        assertIs<MiniAppException.UserCancelled>(cancelled)
        assertEquals("test-host", interrupted.host)
        assertEquals("scan", interrupted.operation)
        assertEquals("scan:cancel", interrupted.hostMessage)
        assertIsNot<MiniAppException.UserCancelled>(interrupted)
        assertEquals("Missing field", invalid.message)
        assertNull(invalid.cause)
        assertEquals("request", timeout.operation)
        assertEquals("request:fail timeout", timeout.hostMessage)
        // A timeout is not a host failure: the operation is fine, the host stopped waiting.
        assertIsNot<MiniAppException.HostFailure>(timeout)
    }

    @Test
    public fun hostFailureKeepsSanitizedDiagnosticValues(): Unit {
        val failure = MiniAppException.HostFailure(
            host = "test-host",
            code = "E_TEST",
            hostMessage = "operation failed",
            metadata = mapOf("operation" to "test"),
        )

        assertEquals("test-host", failure.host)
        assertEquals("E_TEST", failure.code)
        assertEquals("operation failed", failure.hostMessage)
        assertEquals("test", failure.metadata["operation"])
    }
}
