package consumer

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Shared tests. They run in `miniappTest` too, which is what proves the Mini App test compilation
 * inherits the common one rather than replacing it.
 */
public class SharedTest {

    @Test
    public fun greetingIsShared() {
        assertEquals("Hello, Ada, from commonMain", greeting("Ada"))
    }

    @Test
    public fun counterCounts() {
        val counter = Counter()
        assertEquals(1, counter.increment())
        assertEquals(2, counter.increment())
        assertEquals(2, counter.current())
    }
}
