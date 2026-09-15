package io.github.bobcgn.miniapp.host

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class HostVersionTest {
    @Test
    fun comparisonCountsEachSegmentNumerically() {
        // Comparing the text would put 2.20.1 before 2.3.0, which is the wrong release.
        assertTrue(version("2.20.1") > version("2.3.0"))
        assertTrue(version("2.3.0") > version("2.2.9"))
        assertTrue(version("3.0.0") > version("2.99.99"))
        assertTrue(version("1.1.1") > version("1.0.0"))
    }

    @Test
    fun aMissingSegmentCountsAsZero() {
        assertEquals(0, version("1.0").compareTo(version("1.0.0")))
        assertEquals(0, version("2").compareTo(version("2.0.0")))
        assertTrue(version("2.20") > version("2.9"))
    }

    @Test
    fun hashingAgreesWithEquality() {
        assertEquals(version("1.0"), version("1.0.0"))
        assertEquals(version("1.0").hashCode(), version("1.0.0").hashCode())
    }

    @Test
    fun theReportedTextIsKeptForDiagnostics() {
        assertEquals("2.20.1", version("2.20.1").toString())
        assertEquals("3.17.3", version("  3.17.3  ").toString())
    }

    @Test
    fun malformedVersionsAreUndeterminable() {
        // A wrong comparison would misreport support, so anything unexpected is
        // reported as undeterminable rather than guessed at.
        assertNull(HostVersion.parse(""))
        assertNull(HostVersion.parse("v2.20.1"))
        assertNull(HostVersion.parse("2.20.1-beta"))
        assertNull(HostVersion.parse("2..1"))
        assertNull(HostVersion.parse("latest"))
    }

    private fun version(raw: String): HostVersion = requireNotNull(HostVersion.parse(raw))
}
