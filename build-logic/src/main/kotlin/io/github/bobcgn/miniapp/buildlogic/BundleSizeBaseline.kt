package io.github.bobcgn.miniapp.buildlogic

/** Context a baseline records about when it was taken. */
data class BaselineContext(
    val commit: String,
    val date: String,
    val gradle: String,
    val kotlin: String,
    val sdk: String,
)

/**
 * A committed baseline: the measurement plus the context that makes it interpretable later.
 *
 * The gzip level is part of the record, because a number produced at another level is not
 * comparable with it.
 */
data class BundleSizeBaseline(
    val measurement: BundleMeasurement,
    val context: BaselineContext,
    val gzipLevel: Int,
)

/** How one category, or the whole distribution, changed. */
data class SizeDelta(
    val label: String,
    val baseline: Bytes,
    val current: Bytes,
) {
    val rawDelta: Long = current.raw - baseline.raw
    val gzipDelta: Long = current.gzip - baseline.gzip
    val percentDelta: Double? = if (baseline.raw == 0L) null else rawDelta * 100.0 / baseline.raw
}

/** A file that appeared, disappeared, or changed size. */
data class FileDelta(val path: String, val baseline: Bytes?, val current: Bytes?)

data class BundleComparison(
    val categories: List<SizeDelta>,
    val total: SizeDelta,
    val added: List<FileDelta>,
    val removed: List<FileDelta>,
    val changed: List<FileDelta>,
    val unchanged: List<String>,
)

/**
 * The rule that decides whether growth fails the build.
 *
 * Both a floor and a percentage must be exceeded, and only *growth* counts. The floor exists
 * because a few hundred bytes are not worth a decision in a mini program whose package limit is
 * measured in megabytes; the percentage exists because the smallest category can double without
 * mattering. Documented in `docs/PERFORMANCE_BASELINE-en.md` with the same reasoning.
 */
object SizeThresholds {

    /** Growth below this is reported and never fails. */
    const val ABSOLUTE_BYTES: Long = 4 * 1024

    /** Growth below this share of the baseline is reported and never fails. */
    const val PERCENT: Double = 5.0

    fun breaches(baseline: Bytes, current: Bytes): Boolean {
        val delta = current.raw - baseline.raw
        if (delta < ABSOLUTE_BYTES) return false
        val percent = if (baseline.raw == 0L) Double.MAX_VALUE else delta * 100.0 / baseline.raw
        return percent >= PERCENT
    }
}

object BundleSizeComparison {

    /** Every category present in the assembled distribution participates in the size gate. */
    val guardedCategories: List<BundleCategory> = BundleCategory.entries

    /** Category breaches plus the aggregate breach, if any. */
    fun breaches(comparison: BundleComparison): List<SizeDelta> = buildList {
        addAll(
            comparison.categories.filter { delta ->
                guardedCategories.any { it.label == delta.label } &&
                    SizeThresholds.breaches(baseline = delta.baseline, current = delta.current)
            },
        )
        if (SizeThresholds.breaches(comparison.total.baseline, comparison.total.current)) {
            add(comparison.total)
        }
    }

    fun compare(baseline: BundleMeasurement, current: BundleMeasurement): BundleComparison {
        val categories = BundleCategory.entries.map { category ->
            SizeDelta(
                label = category.label,
                baseline = baseline.totalsByCategory.getValue(category),
                current = current.totalsByCategory.getValue(category),
            )
        }

        val baselineFiles = baseline.files.associateBy { it.path }
        val currentFiles = current.files.associateBy { it.path }

        return BundleComparison(
            categories = categories,
            total = SizeDelta(label = "total", baseline = baseline.total, current = current.total),
            added = (currentFiles.keys - baselineFiles.keys).sorted()
                .map { FileDelta(it, baseline = null, current = currentFiles.getValue(it).bytes) },
            removed = (baselineFiles.keys - currentFiles.keys).sorted()
                .map { FileDelta(it, baseline = baselineFiles.getValue(it).bytes, current = null) },
            changed = (currentFiles.keys intersect baselineFiles.keys).sorted()
                .mapNotNull { path ->
                    val before = baselineFiles.getValue(path).bytes
                    val after = currentFiles.getValue(path).bytes
                    if (before == after) null else FileDelta(path, baseline = before, current = after)
                },
            unchanged = (currentFiles.keys intersect baselineFiles.keys).sorted()
                .filter { currentFiles.getValue(it).bytes == baselineFiles.getValue(it).bytes },
        )
    }
}

/**
 * Reader and writer for the baseline file.
 *
 * The format is JSON because a baseline is reviewed by a person in a pull request as well as read
 * by this task. The reader understands the shape this writer produces and checks the schema field
 * rather than trying to be a general JSON parser; anything else is rejected instead of guessed at.
 */
object BaselineJson {

    const val SCHEMA: String = "miniapp-bundle-size-baseline/1"

    fun write(baseline: BundleSizeBaseline): String = buildString {
        val measurement = baseline.measurement
        appendLine("{")
        appendLine("""  "schema": "$SCHEMA",""")
        appendLine("""  "directory": ${quote(measurement.directory)},""")
        appendLine("""  "gzipLevel": ${baseline.gzipLevel},""")
        appendLine("""  "recorded": {""")
        appendLine("""    "commit": ${quote(baseline.context.commit)},""")
        appendLine("""    "date": ${quote(baseline.context.date)},""")
        appendLine("""    "gradle": ${quote(baseline.context.gradle)},""")
        appendLine("""    "kotlin": ${quote(baseline.context.kotlin)},""")
        appendLine("""    "sdk": ${quote(baseline.context.sdk)}""")
        appendLine("""  },""")
        appendLine("""  "total": ${bytesJson(measurement.total)},""")
        appendLine("""  "categories": [""")
        BundleCategory.entries.forEachIndexed { index, category ->
            val comma = if (index == BundleCategory.entries.lastIndex) "" else ","
            appendLine(
                """    { "category": "${category.label}", "raw": ${measurement.totalsByCategory.getValue(category).raw}, """ +
                    """"gzip": ${measurement.totalsByCategory.getValue(category).gzip}, """ +
                    """"files": ${measurement.totalsByCategory.getValue(category).files} }$comma""",
            )
        }
        appendLine("""  ],""")
        appendLine("""  "files": [""")
        measurement.files.forEachIndexed { index, file ->
            val comma = if (index == measurement.files.lastIndex) "" else ","
            appendLine(
                """    { "path": ${quote(file.path)}, "category": "${file.category.label}", """ +
                    """"raw": ${file.bytes.raw}, "gzip": ${file.bytes.gzip} }$comma""",
            )
        }
        appendLine("""  ]""")
        appendLine("}")
    }

    fun read(text: String): BundleSizeBaseline {
        val schema = text.stringValue("schema")
            ?: throw BundleMeasurementException("The baseline file has no schema field.")
        if (schema != SCHEMA) {
            throw BundleMeasurementException(
                "The baseline uses schema '$schema', and this task writes '$SCHEMA'. " +
                    "Regenerate it with updateMiniAppSizeBaseline.",
            )
        }

        val directory = text.stringValue("directory")
            ?: throw BundleMeasurementException("The baseline file has no directory field.")

        val gzipLevel = text.numberValue("gzipLevel")
            ?: throw BundleMeasurementException("The baseline file has no gzipLevel field.")

        val context = BaselineContext(
            commit = text.stringValue("commit").orEmpty(),
            date = text.stringValue("date").orEmpty(),
            gradle = text.stringValue("gradle").orEmpty(),
            kotlin = text.stringValue("kotlin").orEmpty(),
            sdk = text.stringValue("sdk").orEmpty(),
        )

        val files = text.lineSequence()
            .filter { it.contains("\"path\"") }
            .map { line ->
                MeasuredFile(
                    path = line.stringValue("path")
                        ?: throw BundleMeasurementException("A baseline file entry has no path: $line"),
                    category = BundleCategory.entries.firstOrNull { it.label == line.stringValue("category") }
                        ?: throw BundleMeasurementException("A baseline file entry has an unknown category: $line"),
                    bytes = Bytes(
                        raw = line.numberValue("raw")
                            ?: throw BundleMeasurementException("A baseline file entry has no raw size: $line"),
                        gzip = line.numberValue("gzip")
                            ?: throw BundleMeasurementException("A baseline file entry has no gzip size: $line"),
                        files = 1,
                    ),
                )
            }
            .toList()

        if (files.isEmpty()) {
            throw BundleMeasurementException("The baseline file lists no files.")
        }

        return BundleSizeBaseline(
            measurement = BundleMeasurement(directory = directory, files = files),
            context = context,
            gzipLevel = gzipLevel.toInt(),
        )
    }

    /** Renders the readable summary a person reads in the build output and in a pull request. */
    fun describe(comparison: BundleComparison): String = buildString {
        appendLine("category      baseline raw   current raw   delta raw   delta %   baseline gzip   current gzip")
        comparison.categories.forEach { delta ->
            appendLine(
                "%-13s %12d   %11d   %9d   %7s   %13d   %12d".format(
                    delta.label,
                    delta.baseline.raw,
                    delta.current.raw,
                    delta.rawDelta,
                    delta.percentDelta?.let { "%.2f".format(it) } ?: "n/a",
                    delta.baseline.gzip,
                    delta.current.gzip,
                ),
            )
        }
        appendLine(
            "%-13s %12d   %11d   %9d   %7s   %13d   %12d".format(
                comparison.total.label,
                comparison.total.baseline.raw,
                comparison.total.current.raw,
                comparison.total.rawDelta,
                comparison.total.percentDelta?.let { "%.2f".format(it) } ?: "n/a",
                comparison.total.baseline.gzip,
                comparison.total.current.gzip,
            ),
        )
    }

    private fun bytesJson(bytes: Bytes): String =
        """{ "raw": ${bytes.raw}, "gzip": ${bytes.gzip}, "files": ${bytes.files} }"""

    private fun quote(value: String): String = "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""

    private fun String.stringValue(field: String): String? {
        val match = Regex(""""$field"\s*:\s*"((?:[^"\\]|\\.)*)"""").find(this) ?: return null
        return match.groupValues[1].replace("\\\"", "\"").replace("\\\\", "\\")
    }

    private fun String.numberValue(field: String): Long? =
        Regex(""""$field"\s*:\s*(-?\d+)""").find(this)?.groupValues?.get(1)?.toLong()
}
