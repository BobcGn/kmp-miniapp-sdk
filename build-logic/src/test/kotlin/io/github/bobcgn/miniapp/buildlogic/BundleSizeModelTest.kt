package io.github.bobcgn.miniapp.buildlogic

import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/** The measurement itself: classification, sizing, ordering, and the refusals. */
class BundleSizeModelTest {

    private fun directory(vararg files: Pair<String, String>): File =
        Files.createTempDirectory("bundle-size").toFile().apply {
            files.forEach { (name, content) ->
                File(this, name).apply {
                    parentFile.mkdirs()
                    writeText(content)
                }
            }
        }

    @Test
    fun `the report covers every file in the directory`() {
        val directory = directory(
            "kmp-miniapp-sdk-kotlin.js" to "sdk",
            "kotlin-kotlin-stdlib.js" to "stdlib",
            "kotlinx-coroutines-core.js" to "coroutines",
            "kmp-miniapp-sdk-kotlin.d.ts" to "declaration",
            "package.json" to "{}",
        )

        val measurement = BundleSizeMeasurer.measure(directory, "test")

        assertEquals(
            listOf(
                "kmp-miniapp-sdk-kotlin.d.ts",
                "kmp-miniapp-sdk-kotlin.js",
                "kotlin-kotlin-stdlib.js",
                "kotlinx-coroutines-core.js",
                "package.json",
            ),
            measurement.files.map { it.path },
        )
    }

    @Test
    fun `files are reported in a stable order`() {
        val directory = directory("kotlinx-coroutines-core.js" to "b", "kmp-miniapp-sdk-kotlin.js" to "a")

        val first = BundleSizeMeasurer.measure(directory, "test").files.map { it.path }
        val second = BundleSizeMeasurer.measure(directory, "test").files.map { it.path }

        assertEquals(listOf("kmp-miniapp-sdk-kotlin.js", "kotlinx-coroutines-core.js"), first)
        assertEquals(first, second)
    }

    @Test
    fun `raw sizes are the real byte counts`() {
        val directory = directory("kmp-miniapp-sdk-kotlin.js" to "0123456789", "kotlin-kotlin-stdlib.js" to "abc")

        val measurement = BundleSizeMeasurer.measure(directory, "test")
        val byPath = measurement.files.associateBy { it.path }

        assertEquals(10L, byPath.getValue("kmp-miniapp-sdk-kotlin.js").bytes.raw)
        assertEquals(3L, byPath.getValue("kotlin-kotlin-stdlib.js").bytes.raw)
    }

    @Test
    fun `the same bytes always produce the same gzip size`() {
        val bytes = "kotlin javascript output that is long enough to compress".repeat(50).toByteArray()

        val first = DeterministicGzip.sizeOf(bytes)
        val second = DeterministicGzip.sizeOf(bytes.copyOf())

        assertEquals(first, second)
        assertTrue(first > 0, "compressed size must be positive")
        assertTrue(first < bytes.size.toLong(), "repeated text must compress")
    }

    @Test
    fun `the sdk and the runtime are separate categories`() {
        assertEquals(BundleCategory.SdkCode, BundleClassifier.classify("kmp-miniapp-sdk-kotlin.js"))
        assertEquals(BundleCategory.RuntimeDependencies, BundleClassifier.classify("kotlin-kotlin-stdlib.js"))
        assertEquals(BundleCategory.RuntimeDependencies, BundleClassifier.classify("kotlinx-coroutines-core.js"))
        assertEquals(BundleCategory.RuntimeDependencies, BundleClassifier.classify("kotlinx-atomicfu.js"))
        assertEquals(
            BundleCategory.RuntimeDependencies,
            BundleClassifier.classify("kotlin_org_jetbrains_kotlin_kotlin_dom_api_compat.js"),
        )
        assertEquals(BundleCategory.ConsumerCode, BundleClassifier.classify("my-app-miniapp.js"))
    }

    @Test
    fun `declarations, maps and metadata are development files, not runtime`() {
        listOf("kmp-miniapp-sdk-kotlin.js.map", "kmp-miniapp-sdk-kotlin.d.ts", "package.json").forEach { path ->
            assertEquals(BundleCategory.DevelopmentAndMetadata, BundleClassifier.classify(path), path)
        }
    }

    @Test
    fun `category totals add up to the file totals`() {
        val directory = directory(
            "kmp-miniapp-sdk-kotlin.js" to "0123456789",
            "kotlin-kotlin-stdlib.js" to "abc",
            "my-app-miniapp.js" to "hello",
            "kmp-miniapp-sdk-kotlin.js.map" to "map",
        )

        val measurement = BundleSizeMeasurer.measure(directory, "test")

        BundleCategory.entries.forEach { category ->
            val expected = measurement.files.filter { it.category == category }.sumOf { it.bytes.raw }
            assertEquals(expected, measurement.totalsByCategory.getValue(category).raw, category.label)
        }
    }

    @Test
    fun `the total adds up to the category totals`() {
        val directory = directory(
            "kmp-miniapp-sdk-kotlin.js" to "0123456789",
            "kotlin-kotlin-stdlib.js" to "abc",
            "my-app-miniapp.js" to "hello",
            "kmp-miniapp-sdk-kotlin.js.map" to "map",
        )

        val measurement = BundleSizeMeasurer.measure(directory, "test")

        assertEquals(
            BundleCategory.entries.sumOf { measurement.totalsByCategory.getValue(it).raw },
            measurement.total.raw,
        )
        assertEquals(
            BundleCategory.entries.sumOf { measurement.totalsByCategory.getValue(it).files },
            measurement.total.files,
        )
    }

    @Test
    fun `an empty directory fails with a clear message`() {
        val directory = directory()

        val failure = assertFailsWith<BundleMeasurementException> {
            BundleSizeMeasurer.measure(directory, "empty")
        }

        assertContains(failure.message!!, "empty")
        assertContains(failure.message!!, "no files")
    }

    @Test
    fun `a missing directory fails with a clear message`() {
        val failure = assertFailsWith<BundleMeasurementException> {
            BundleSizeMeasurer.measure(File("/does/not/exist"), "missing")
        }

        assertContains(failure.message!!, "does not exist")
    }

    @Test
    fun `a missing expected artifact fails instead of reporting a smaller bundle`() {
        val directory = directory("kotlin-kotlin-stdlib.js" to "abc")

        val failure = assertFailsWith<BundleMeasurementException> {
            BundleSizeMeasurer.measure(directory, "test", requiredArtifacts = listOf("kmp-miniapp-sdk-kotlin.js"))
        }

        assertContains(failure.message!!, "missing required artifacts")
        assertContains(failure.message!!, "kmp-miniapp-sdk-kotlin.js")
    }

    @Test
    fun `host markup is refused rather than counted`() {
        listOf("pages/index/index.wxml", "app.wxss", "index.axml", "index.html").forEach { markup ->
            val directory = directory("kmp-miniapp-sdk-kotlin.js" to "sdk", markup to "<view/>")

            val failure = assertFailsWith<BundleMeasurementException> {
                BundleSizeMeasurer.measure(directory, "test")
            }

            assertContains(failure.message!!, "Host markup", message = "the refusal must name the reason")
        }
    }

    @Test
    fun `the measurement records the directory by its relative label, never an absolute path`() {
        val directory = directory("kmp-miniapp-sdk-kotlin.js" to "sdk")

        val measurement = BundleSizeMeasurer.measure(directory, "sdk/build/dist/js/productionLibrary")

        assertEquals("sdk/build/dist/js/productionLibrary", measurement.directory)
        assertTrue(
            measurement.files.none { it.path.startsWith("/") },
            "file paths in the report must be relative",
        )
    }
}
