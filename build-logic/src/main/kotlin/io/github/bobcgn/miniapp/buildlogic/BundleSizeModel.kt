package io.github.bobcgn.miniapp.buildlogic

import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.Deflater
import java.util.zip.GZIPOutputStream

/**
 * What a file in a Mini App distribution is, for the purpose of a size report.
 *
 * The categories exist so the report answers "how much is the SDK itself" rather than only "how
 * big is the directory": a Kotlin/JS distribution is mostly the Kotlin runtime, and a single
 * number would hide that.
 */
enum class BundleCategory(val label: String) {
    /** The SDK's own compiled module and its hand-written CommonJS normalization. */
    SdkCode("sdk"),

    /** The Kotlin, coroutines and atomicfu runtimes the compilation resolved. */
    RuntimeDependencies("runtime"),

    /** Application code a consumer compiled, when a consumer bundle is measured. */
    ConsumerCode("consumer"),

    /** Auxiliary source maps, TypeScript declarations and module metadata in the assembled bundle. */
    DevelopmentAndMetadata("development"),
}

/** Byte sizes of one file, or of a set of them. */
data class Bytes(val raw: Long, val gzip: Long, val files: Int) {
    operator fun plus(other: Bytes): Bytes =
        Bytes(raw = raw + other.raw, gzip = gzip + other.gzip, files = files + other.files)

    companion object {
        val ZERO: Bytes = Bytes(raw = 0, gzip = 0, files = 0)
    }
}

/** One measured file. [path] is always relative to the measured directory. */
data class MeasuredFile(
    val path: String,
    val category: BundleCategory,
    val bytes: Bytes,
)

/** A measured distribution, with its files in a stable order. */
data class BundleMeasurement(
    val directory: String,
    val files: List<MeasuredFile>,
) {
    val totalsByCategory: Map<BundleCategory, Bytes> = BundleCategory.entries.associateWith { category ->
        files.filter { it.category == category }.fold(Bytes.ZERO) { sum, file -> sum + file.bytes }
    }

    val total: Bytes = files.fold(Bytes.ZERO) { sum, file -> sum + file.bytes }
}

/**
 * Classifies a distribution file by name.
 *
 * The rules are name-based on purpose: they must work on a directory the compiler produced, without
 * reading its contents, and they must classify a consumer bundle the same way they classify the
 * SDK's own.
 */
object BundleClassifier {

    /** Host markup belongs to the host UI, never to a measured distribution. */
    private val hostMarkupSuffixes = listOf(".wxml", ".wxss", ".axml", ".acss", ".html")

    private val developmentSuffixes = listOf(".map", ".d.ts")
    private val metadataNames = listOf("package.json")

    fun isHostMarkup(path: String): Boolean = hostMarkupSuffixes.any { path.endsWith(it) }

    fun classify(path: String): BundleCategory = when {
        isHostMarkup(path) -> error("Host markup is not part of a Mini App distribution: $path")
        developmentSuffixes.any { path.endsWith(it) } || metadataNames.any { path.endsWith(it) } ->
            BundleCategory.DevelopmentAndMetadata
        path.startsWith("kmp-miniapp-sdk") -> BundleCategory.SdkCode
        path.startsWith("kotlin-") || path.startsWith("kotlinx-") || path.startsWith("kotlin_") ->
            BundleCategory.RuntimeDependencies
        else -> BundleCategory.ConsumerCode
    }
}

/**
 * Gzip sizes for the report.
 *
 * The numbers are a comparison metric, not a delivery size: a real host serves the files through
 * its own transport, with its own level and its own options.
 *
 * The measurement is deterministic: the level is fixed here, and the JDK's gzip header carries no
 * timestamp, so the same bytes always produce the same size. [LEVEL] is recorded in the baseline,
 * because a report produced at a different level is not comparable with it.
 */
object DeterministicGzip {

    /** The level every measurement uses. `-1` is zlib's default, which is what servers use. */
    const val LEVEL: Int = Deflater.DEFAULT_COMPRESSION

    fun sizeOf(bytes: ByteArray): Long {
        val sink = ByteArrayOutputStream()
        val gzip = object : GZIPOutputStream(sink) {
            init {
                def.setLevel(LEVEL)
            }
        }
        gzip.use { it.write(bytes) }
        return sink.size().toLong()
    }
}

/** Raised when a directory cannot be measured as a distribution. */
class BundleMeasurementException(message: String) : IllegalStateException(message)

object BundleSizeMeasurer {

    /**
     * Measures every file under [directory].
     *
     * @param requiredArtifacts relative paths that must be present, so a distribution that lost a
     *        runtime file fails the report instead of reported as a smaller one
     * @throws BundleMeasurementException when the directory is missing or empty, when it carries
     *         host markup, or when a required artifact is absent
     */
    fun measure(
        directory: File,
        directoryLabel: String,
        requiredArtifacts: List<String> = emptyList(),
    ): BundleMeasurement {
        if (!directory.isDirectory) {
            throw BundleMeasurementException("The measured directory does not exist: $directoryLabel")
        }

        val files = directory.walkTopDown()
            .filter { it.isFile }
            .map { it.relativeTo(directory).invariantSeparatorsPath }
            .sorted()
            .toList()

        if (files.isEmpty()) {
            throw BundleMeasurementException(
                "The measured directory $directoryLabel holds no files, so there is nothing to report.",
            )
        }

        val markup = files.filter { BundleClassifier.isHostMarkup(it) }
        if (markup.isNotEmpty()) {
            throw BundleMeasurementException(
                "Host markup belongs to the host UI and is not part of a distribution: $markup",
            )
        }

        val missing = requiredArtifacts.filterNot { it in files }
        if (missing.isNotEmpty()) {
            throw BundleMeasurementException(
                "The distribution $directoryLabel is missing required artifacts: $missing",
            )
        }

        return BundleMeasurement(
            directory = directoryLabel,
            files = files.map { path ->
                val bytes = File(directory, path).readBytes()
                MeasuredFile(
                    path = path,
                    category = BundleClassifier.classify(path),
                    bytes = Bytes(raw = bytes.size.toLong(), gzip = DeterministicGzip.sizeOf(bytes), files = 1),
                )
            },
        )
    }
}
