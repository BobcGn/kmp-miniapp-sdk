package io.github.bobcgn.miniapp.buildlogic

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The baseline record, the comparison, and the rule that decides whether growth fails. */
class BundleSizeBaselineTest {

    private fun measurement(
        directory: String = "sdk/build/dist/js/productionLibrary",
        vararg files: Triple<String, Long, Long>,
    ) = BundleMeasurement(
        directory = directory,
        files = files.map { (path, raw, gzip) ->
            MeasuredFile(path = path, category = BundleClassifier.classify(path), bytes = Bytes(raw, gzip, 1))
        },
    )

    private fun baseline(measurement: BundleMeasurement) = BundleSizeBaseline(
        measurement = measurement,
        context = BaselineContext(
            commit = "abc1234",
            date = "2026-09-18",
            gradle = "9.3.1",
            kotlin = "2.4.20",
            sdk = "0.1.0-SNAPSHOT",
        ),
        gzipLevel = DeterministicGzip.LEVEL,
    )

    @Test
    fun `a baseline survives a round trip through the file format`() {
        val original = baseline(
            measurement(
                "sdk/build/dist/js/productionLibrary",
                Triple("kmp-miniapp-sdk-kotlin.js", 1000L, 200L),
                Triple("kotlin-kotlin-stdlib.js", 2000L, 400L),
                Triple("package.json", 20L, 30L),
            ),
        )

        val restored = BaselineJson.read(BaselineJson.write(original))

        assertEquals(original.measurement.directory, restored.measurement.directory)
        assertEquals(original.measurement.total, restored.measurement.total)
        assertEquals(original.measurement.files, restored.measurement.files)
        assertEquals(original.context, restored.context)
        assertEquals(original.gzipLevel, restored.gzipLevel)
    }

    @Test
    fun `a baseline written for another schema is rejected rather than guessed at`() {
        val failure = assertFailsWith<BundleMeasurementException> {
            BaselineJson.read("""{ "schema": "miniapp-bundle-size-baseline/2" }""")
        }

        assertContains(failure.message!!, "Regenerate")
    }

    @Test
    fun `a baseline with no files is rejected`() {
        val text = BaselineJson.write(baseline(measurement("dir", Triple("kmp-miniapp-sdk-kotlin.js", 1L, 1L))))
            .replace(Regex("""\s*\{ "path".*\n"""), "")

        val failure = assertFailsWith<BundleMeasurementException> { BaselineJson.read(text) }

        assertContains(failure.message!!, "lists no files")
    }

    @Test
    fun `a comparison reports growth, shrinkage and no change`() {
        val before = measurement(
            "dir",
            Triple("kmp-miniapp-sdk-kotlin.js", 1000L, 200L),
            Triple("kotlin-kotlin-stdlib.js", 2000L, 400L),
            Triple("kotlinx-atomicfu.js", 100L, 50L),
        )
        val after = measurement(
            "dir",
            Triple("kmp-miniapp-sdk-kotlin.js", 1500L, 250L),
            Triple("kotlin-kotlin-stdlib.js", 1000L, 200L),
            Triple("kotlinx-atomicfu.js", 100L, 50L),
        )

        val comparison = BundleSizeComparison.compare(before, after)
        val sdk = comparison.categories.single { it.label == "sdk" }
        val runtime = comparison.categories.single { it.label == "runtime" }
        val development = comparison.categories.single { it.label == "development" }

        assertEquals(500L, sdk.rawDelta)
        assertEquals(50.0, sdk.percentDelta!!, 0.001)
        assertEquals(-1000L, runtime.rawDelta)
        assertEquals(0L, development.rawDelta)
        assertEquals(listOf("kotlinx-atomicfu.js"), comparison.unchanged)
        assertTrue(comparison.changed.any { it.path == "kmp-miniapp-sdk-kotlin.js" })
        assertTrue(comparison.changed.any { it.path == "kotlin-kotlin-stdlib.js" })
        assertEquals(emptyList(), comparison.added)
        assertEquals(emptyList(), comparison.removed)
    }

    @Test
    fun `a comparison names files that appeared and disappeared`() {
        val before = measurement("dir", Triple("kmp-miniapp-sdk-kotlin.js", 1000L, 200L))
        val after = measurement(
            "dir",
            Triple("kmp-miniapp-sdk-kotlin.js", 1000L, 200L),
            Triple("kotlinx-new-runtime.js", 500L, 100L),
        )

        val comparison = BundleSizeComparison.compare(before, after)

        assertEquals(listOf("kotlinx-new-runtime.js"), comparison.added.map { it.path })
        assertEquals(emptyList(), comparison.removed)

        val removal = BundleSizeComparison.compare(after, before)
        assertEquals(listOf("kotlinx-new-runtime.js"), removal.removed.map { it.path })
    }

    @Test
    fun `growth below the floor never fails, however large the share`() {
        val before = Bytes(raw = 100, gzip = 50, files = 1)
        val after = Bytes(raw = 100 + SizeThresholds.ABSOLUTE_BYTES - 1, gzip = 50, files = 1)

        assertFalse(SizeThresholds.breaches(before, after))
    }

    @Test
    fun `growth above the floor but below the share never fails`() {
        val before = Bytes(raw = 1_000_000, gzip = 1, files = 1)
        // 5000 bytes is above the 4 KiB floor and below 5% of a megabyte.
        val after = Bytes(raw = 1_005_000, gzip = 1, files = 1)

        assertFalse(SizeThresholds.breaches(before, after))
    }

    @Test
    fun `growth above both the floor and the share fails`() {
        val before = Bytes(raw = 100_000, gzip = 1, files = 1)
        val after = Bytes(raw = 100_000 + 5_000, gzip = 1, files = 1)

        assertTrue(SizeThresholds.breaches(before, after))
    }

    @Test
    fun `exactly at both thresholds is a breach, and one byte below is not`() {
        val before = Bytes(raw = 100_000, gzip = 1, files = 1)

        assertTrue(SizeThresholds.breaches(before, Bytes(raw = 105_000, gzip = 1, files = 1)))
        assertFalse(SizeThresholds.breaches(before, Bytes(raw = 104_999, gzip = 1, files = 1)))
    }

    @Test
    fun `a smaller bundle never fails`() {
        val before = Bytes(raw = 1_000_000, gzip = 1, files = 1)
        val after = Bytes(raw = 1, gzip = 1, files = 1)

        assertFalse(SizeThresholds.breaches(before, after))
    }

    @Test
    fun `every assembled bundle category participates in the failure rule`() {
        assertEquals(
            listOf("sdk", "runtime", "consumer", "development"),
            BundleSizeComparison.guardedCategories.map { it.label },
        )
        assertTrue(BundleSizeComparison.guardedCategories.contains(BundleCategory.DevelopmentAndMetadata))
    }

    @Test
    fun `aggregate growth fails even when every category stays below the absolute floor`() {
        val before = measurement(
            "dir",
            Triple("kmp-miniapp-sdk-kotlin.js", 10_000L, 1L),
            Triple("kotlin-kotlin-stdlib.js", 10_000L, 1L),
            Triple("consumer.js", 10_000L, 1L),
            Triple("package.json", 10_000L, 1L),
        )
        val after = measurement(
            "dir",
            Triple("kmp-miniapp-sdk-kotlin.js", 13_000L, 1L),
            Triple("kotlin-kotlin-stdlib.js", 13_000L, 1L),
            Triple("consumer.js", 13_000L, 1L),
            Triple("package.json", 13_000L, 1L),
        )

        val breaches = BundleSizeComparison.breaches(BundleSizeComparison.compare(before, after))

        assertEquals(listOf("total"), breaches.map { it.label })
    }

    @Test
    fun `the readable summary names every category and the total`() {
        val comparison = BundleSizeComparison.compare(
            measurement("dir", Triple("kmp-miniapp-sdk-kotlin.js", 1000L, 200L)),
            measurement("dir", Triple("kmp-miniapp-sdk-kotlin.js", 1000L, 200L)),
        )

        val summary = BaselineJson.describe(comparison)

        BundleCategory.entries.forEach { assertContains(summary, it.label) }
        assertContains(summary, "total")
    }
}
