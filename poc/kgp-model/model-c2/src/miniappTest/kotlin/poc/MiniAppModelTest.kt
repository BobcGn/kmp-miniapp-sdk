package poc

import kotlin.test.Test
import kotlin.test.assertEquals

class MiniAppModelTest {

    @Test
    fun miniappMainIsCompiledAndSeesCommonMain() {
        assertEquals("miniapp:hello from commonMain", miniAppEntry())
    }

    @Test
    fun miniappMainIsCompiledByTheKotlinJsCompiler() {
        assertEquals("kotlin-js", jsRuntimeMarker())
    }

    @Test
    fun miniappTestSeesCommonTest() {
        verifyCommonGreeting()
    }
}
