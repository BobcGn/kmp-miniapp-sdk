package poc

import kotlin.test.Test
import kotlin.test.assertEquals

fun verifyCommonGreeting() {
    assertEquals("hello from commonMain", commonGreeting())
}

// Declared in commonTest on purpose: it can only appear in the miniapp test report if
// miniappTest really depends on commonTest.
class CommonModelTest {

    @Test
    fun commonTestIsAvailableToTheMiniAppTestCompilation() {
        verifyCommonGreeting()
    }
}
