package io.github.bobcgn.miniapp.buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Shared inputs of the size tasks.
 *
 * Everything is a Gradle property, so a task can run under the configuration cache without holding
 * on to a `Project`, and no input is an absolute path: the measured directory is labelled with the
 * repository-relative name the report and the baseline both use.
 */
abstract class MiniAppSizeTaskBase : DefaultTask() {

    /** The distribution to measure. */
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val bundleDirectory: DirectoryProperty

    /** The repository-relative name of the measured directory, recorded in the report. */
    @get:Input
    abstract val directoryLabel: Property<String>

    /** Files that must be present, so a distribution that lost one is not reported as smaller. */
    @get:Input
    abstract val requiredArtifacts: ListProperty<String>

    /** The gzip level, recorded so two reports can be compared honestly. */
    @get:Input
    abstract val gzipLevel: Property<Int>

    @get:Input
    abstract val recordedCommit: Property<String>

    /**
     * The measurement date, which only a baseline records.
     *
     * The report of a verification run leaves it empty on purpose: a report is a measurement, and a
     * value that changes every day would make the task never up to date.
     */
    @get:Input
    @get:Optional
    abstract val recordedDate: Property<String>

    @get:Input
    abstract val recordedGradle: Property<String>

    @get:Input
    abstract val recordedKotlin: Property<String>

    @get:Input
    abstract val recordedSdk: Property<String>

    protected fun measure(): BundleMeasurement = BundleSizeMeasurer.measure(
        directory = bundleDirectory.get().asFile,
        directoryLabel = directoryLabel.get(),
        requiredArtifacts = requiredArtifacts.get(),
    )

    protected fun context(): BaselineContext = BaselineContext(
        commit = recordedCommit.get(),
        date = recordedDate.getOrElse(""),
        gradle = recordedGradle.get(),
        kotlin = recordedKotlin.get(),
        sdk = recordedSdk.get(),
    )
}

/**
 * Measures a distribution, writes the machine-readable report, and compares it with the committed
 * baseline.
 *
 * The comparison fails only on growth that crosses both documented thresholds, and only for the
 * categories a host receives. Source maps and declarations are reported and never fail.
 */
abstract class MiniAppBundleSizeReportTask : MiniAppSizeTaskBase() {

    /** The committed baseline, or absent when this is a measurement-only run. */
    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val baselineFile: RegularFileProperty

    @get:OutputFile
    abstract val reportFile: RegularFileProperty

    @TaskAction
    fun report() {
        val measurement = measure()
        val baseline = BundleSizeBaseline(measurement = measurement, context = context(), gzipLevel = gzipLevel.get())

        reportFile.get().asFile.apply {
            parentFile.mkdirs()
            writeText(BaselineJson.write(baseline))
        }

        logger.lifecycle("Mini App bundle size — ${measurement.directory}")
        measurement.files.forEach { file ->
            logger.lifecycle(
                "  %-13s %9d raw  %8d gzip  %s".format(file.category.label, file.bytes.raw, file.bytes.gzip, file.path),
            )
        }
        BundleCategory.entries.forEach { category ->
            val totals = measurement.totalsByCategory.getValue(category)
            logger.lifecycle(
                "  %-13s %9d raw  %8d gzip  %d files".format(category.label, totals.raw, totals.gzip, totals.files),
            )
        }
        logger.lifecycle(
            "  %-13s %9d raw  %8d gzip  %d files".format(
                "total",
                measurement.total.raw,
                measurement.total.gzip,
                measurement.total.files,
            ),
        )
        logger.lifecycle("Mini App bundle size report written to ${reportFile.get().asFile.name}")

        val baselineFile = baselineFile.orNull?.asFile
        if (baselineFile == null || !baselineFile.isFile) {
            logger.lifecycle("No baseline to compare against; this run only measured.")
            return
        }

        val recorded = BaselineJson.read(baselineFile.readText())
        if (recorded.measurement.directory != measurement.directory) {
            logger.lifecycle(
                "The baseline is for '${recorded.measurement.directory}' and this run measured " +
                    "'${measurement.directory}', so the two are not compared.",
            )
            return
        }
        if (recorded.gzipLevel != gzipLevel.get()) {
            logger.lifecycle(
                "The baseline was recorded at gzip level ${recorded.gzipLevel} and this run used " +
                    "${gzipLevel.get()}, so the two are not compared.",
            )
            return
        }

        val comparison = BundleSizeComparison.compare(recorded.measurement, measurement)
        logger.lifecycle(BaselineJson.describe(comparison))
        logger.lifecycle(
            "Baseline ${recorded.context.commit} (${recorded.context.date}); " +
                "added=${comparison.added.map { it.path }}, removed=${comparison.removed.map { it.path }}, " +
                "unchanged=${comparison.unchanged.size}",
        )

        val breached = BundleSizeComparison.breaches(comparison)

        if (breached.isEmpty()) {
            logger.lifecycle(
                "Mini App bundle size: PASS — no category or total grew by both " +
                    "${SizeThresholds.ABSOLUTE_BYTES} bytes and ${SizeThresholds.PERCENT}%.",
            )
            return
        }

        throw GradleException(
            buildString {
                appendLine("The Mini App bundle grew beyond its baseline.")
                breached.forEach { delta ->
                    appendLine(
                        "  - ${delta.label}: ${delta.baseline.raw} -> ${delta.current.raw} bytes " +
                            "(+${delta.rawDelta}, ${"%.2f".format(delta.percentDelta)}%)",
                    )
                }
                appendLine()
                appendLine("The rule requires both a floor and a share, and both were crossed:")
                appendLine("  floor  ${SizeThresholds.ABSOLUTE_BYTES} bytes")
                appendLine("  share  ${SizeThresholds.PERCENT}%")
                appendLine()
                appendLine("If the growth is intended, review it and accept it explicitly:")
                appendLine("  ./gradlew updateMiniAppSizeBaseline")
            },
        )
    }
}

/** Rewrites the committed baseline from a fresh measurement. A maintainer action, never automatic. */
abstract class MiniAppSizeBaselineTask : MiniAppSizeTaskBase() {

    @get:OutputFile
    abstract val baselineFile: RegularFileProperty

    @TaskAction
    fun update() {
        val measurement = measure()
        baselineFile.get().asFile.apply {
            parentFile.mkdirs()
            writeText(
                BaselineJson.write(
                    BundleSizeBaseline(measurement = measurement, context = context(), gzipLevel = gzipLevel.get()),
                ),
            )
        }
        logger.lifecycle(
            "Mini App bundle size baseline updated: ${measurement.total.raw} bytes raw, " +
                "${measurement.total.gzip} bytes gzip, ${measurement.total.files} files.",
        )
    }
}
