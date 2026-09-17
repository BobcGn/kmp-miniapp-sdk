package consumer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Mini App only tests. Together with `SharedTest` in `commonTest`, the report this class belongs to
 * proves both halves of the test contract: the Mini App tests run, and the shared tests run with
 * them.
 */
public class MiniAppTest {

    @Test
    public fun miniAppMainReusesTheSharedBehaviour() {
        assertEquals("Hello, Ada, from commonMain", hostGreeting("Ada"))
    }

    @Test
    public fun miniAppMainIsCompiledByTheKotlinJsCompiler() {
        assertEquals("consumer-miniapp", runtimeMarker())
    }

    @Test
    public fun theRuntimeSdkIsOnTheMiniAppClasspath() {
        assertTrue(sdkVersion().isNotBlank(), "the runtime SDK must report a version")
    }
}
