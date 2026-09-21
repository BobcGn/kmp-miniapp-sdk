package io.github.bobcgn.miniapp.gradle

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.FileCopyDetails
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.TaskProvider

/**
 * Registers the four tasks that take a consumer from "I applied the plugin" to "I have a WeChat
 * project WeChat Developer Tools can open".
 *
 * They exist because the 0.1.0 bundle answers a narrower question than a developer asks. That bundle
 * is the compiler's output placed at a stable path; a WeChat project is that output *inside* the
 * host project, and nothing in 0.1.0 produced one — the consumer fixture carried a hand-written host
 * instead, which is exactly the step this replaces.
 *
 * The four tasks differ in what they own, and the difference is the whole design:
 *
 * ```text
 * initMiniApp    creates the source host, once, only when asked
 * syncMiniApp    the compiler's modules → the source host's generated library area
 * assembleMiniApp the source host + the compiler's modules → a standalone project
 * verifyMiniApp  reads that project, and only that project
 * ```
 *
 * Two rules run through all four. The source host is **source**: it is created by explicit request,
 * never overwritten, and never written to outside its generated library area. The distribution is
 * **output**: it is deleted and rebuilt, it is never read by anything that also reads the source
 * host, and it must work after being copied somewhere else.
 */
internal object MiniAppDistributionSupport {

    /** The task group every one of these appears under. */
    private const val GROUP: String = "mini app"

    public fun registerMiniAppDistributionTasks(project: Project) {
        val wechat = project.extensions.getByType(MiniAppExtension::class.java).wechat
        val distribution = project.tasks.named(MiniAppPluginDiagnostics.MINIAPP_DISTRIBUTION_TASK_NAME)
        val projectDirectory = project.layout.projectDirectory.asFile

        val sourceDirectory = wechat.sourceDirectory
        val generatedLibraryDirectory = wechat.generatedLibraryDirectory
        val distributionDirectory = wechat.distributionDirectory

        registerInitMiniApp(project, sourceDirectory)
        registerSyncMiniApp(project, distribution, sourceDirectory, generatedLibraryDirectory, projectDirectory)
        registerAssembleMiniApp(
            project = project,
            distribution = distribution,
            sourceDirectory = sourceDirectory,
            generatedLibraryDirectory = generatedLibraryDirectory,
            distributionDirectory = distributionDirectory,
            projectDirectory = projectDirectory,
        )
        registerVerifyMiniApp(
            project = project,
            sourceDirectory = sourceDirectory,
            generatedLibraryDirectory = generatedLibraryDirectory,
            distributionDirectory = distributionDirectory,
            projectDirectory = projectDirectory,
        )
    }

    /**
     * `initMiniApp` — writes the files of the source host that are not there yet.
     *
     * It is registered but never wired to anything, and it declares no outputs. Both are deliberate.
     * A task that creates source is one a build must ask for, not one that happens while a project
     * is being configured or an IDE is syncing; and a task whose job is "create what is absent" has
     * no output set it could be up to date against, because its correct answer depends on what is on
     * disk rather than on what it was given.
     */
    private fun registerInitMiniApp(
        project: Project,
        sourceDirectory: org.gradle.api.file.DirectoryProperty,
    ) {
        val projectName = project.name
        val projectDirectory = project.layout.projectDirectory.asFile

        project.tasks.register(MiniAppPluginDiagnostics.INIT_MINIAPP_TASK_NAME) { task ->
            task.group = GROUP
            task.description =
                "Creates the Mini App source host from the plugin's template, writing only the files " +
                    "that are missing and never overwriting one that exists."

            val source = sourceDirectory
            task.doLast { executed ->
                val sourceHost = source.get().asFile
                MiniAppPathDiagnostics.requireSourceInsideProject(sourceHost, projectDirectory)

                val result = MiniAppSourceHost.initialize(sourceHost, projectName)
                val logger = executed.logger

                logger.lifecycle("miniApp.init.sourceDirectory=${sourceHost.invariantSeparatorsPath}")
                result.created.forEach { logger.lifecycle("miniApp.init.created=$it") }
                result.skipped.forEach { logger.lifecycle("miniApp.init.skipped=$it") }
                logger.lifecycle("miniApp.init.createdCount=${result.created.size}")
                logger.lifecycle("miniApp.init.skippedCount=${result.skipped.size}")
                if (result.skipped.isNotEmpty()) {
                    logger.lifecycle(
                        "miniApp.init.note=existing files were left untouched; the plugin never " +
                            "rewrites a source-host file",
                    )
                }
            }
        }
    }

    /**
     * `syncMiniApp` — the development loop.
     *
     * It republishes the compiler's modules into the generated area of the source host, so a
     * developer who changed Kotlin runs one task and recompiles in WeChat Developer Tools. It is a
     * `Sync`, not a `Copy`, because the generated area is the plugin's to own: a module that left the
     * bundle must leave the source host too, or the host keeps loading code the build no longer
     * produces.
     *
     * The source is the distribution task's *declared output*, not a path written here, so a Kotlin
     * Gradle Plugin layout change moves the bundle with it instead of silently emptying it.
     */
    private fun registerSyncMiniApp(
        project: Project,
        distribution: TaskProvider<*>,
        sourceDirectory: org.gradle.api.file.DirectoryProperty,
        generatedLibraryDirectory: org.gradle.api.file.DirectoryProperty,
        projectDirectory: java.io.File,
    ) {
        project.tasks.register(MiniAppPluginDiagnostics.SYNC_MINIAPP_TASK_NAME, Sync::class.java) { task ->
            task.group = GROUP
            task.description =
                "Copies the compiled Mini App modules into the source host's generated library " +
                    "area, replacing whatever the previous bundle left there."

            task.dependsOn(MiniAppPluginDiagnostics.CHECK_MINIAPP_HOST_BOUNDARY_TASK_NAME)
            task.into(generatedLibraryDirectory)
            task.from(distribution.map { it.outputs.files })

            val source = sourceDirectory
            val generated = generatedLibraryDirectory
            // `doFirst`, so a destination the build must not own fails before Sync deletes anything.
            task.doFirst {
                MiniAppPathDiagnostics.requireSourceInsideProject(source.get().asFile, projectDirectory)
                MiniAppPathDiagnostics.requireGeneratedLibraryInsideSource(
                    generated.get().asFile,
                    source.get().asFile,
                )
            }
        }
    }

    /**
     * `assembleMiniApp` — the acceptance artifact.
     *
     * It produces a whole WeChat project: the developer's source host, minus what is local to a
     * machine, plus the current compiler output placed exactly where the source host keeps it. It
     * does not require `syncMiniApp` to have run, and it never writes to the source host, so the
     * acceptance artifact cannot be mistaken for — or disturb — the development one.
     *
     * The exclusion set is what separates a project from a working copy: WeChat's own machine-local
     * preferences, anything a package manager or an IDE wrote, and Gradle's own directories. What
     * survives is what WeChat packages.
     */
    private fun registerAssembleMiniApp(
        project: Project,
        distribution: TaskProvider<*>,
        sourceDirectory: org.gradle.api.file.DirectoryProperty,
        generatedLibraryDirectory: org.gradle.api.file.DirectoryProperty,
        distributionDirectory: org.gradle.api.file.DirectoryProperty,
        projectDirectory: java.io.File,
    ) {
        val libraryPathInDistribution: Provider<String> = libraryPathInDistribution(
            sourceDirectory,
            generatedLibraryDirectory,
        )
        val generatedLibraryPath: Provider<String> =
            generatedLibraryDirectory.map { it.asFile.canonicalPath }

        project.tasks.register(MiniAppPluginDiagnostics.ASSEMBLE_MINIAPP_TASK_NAME, Sync::class.java) { task ->
            task.group = GROUP
            task.description =
                "Assembles the standalone Mini App distribution: the source host plus the current " +
                    "compiled modules, with everything machine-local left out."

            task.dependsOn(MiniAppPluginDiagnostics.CHECK_MINIAPP_HOST_BOUNDARY_TASK_NAME)
            task.into(distributionDirectory)

            // The generated library area lives inside the source host, so Gradle sees `syncMiniApp`'s
            // output inside this task's input directory and requires the relationship to be stated.
            // `mustRunAfter` rather than `dependsOn`, because that is what the relationship is:
            // `assembleMiniApp` injects the compiler's current output and ignores whatever
            // `syncMiniApp` last wrote, so asking for the distribution must neither wait for a sync
            // nor trigger one.
            task.mustRunAfter(MiniAppPluginDiagnostics.SYNC_MINIAPP_TASK_NAME)

            task.from(sourceDirectory) { spec ->
                // The generated area is excluded from the source copy and injected below instead.
                // Copying it here and then injecting over it would keep whichever modules the last
                // `syncMiniApp` wrote and never ran, which is not what the compiler just produced.
                val generated = generatedLibraryPath
                spec.eachFile { details: FileCopyDetails ->
                    if (details.file.canonicalPath.startsWith(generated.get())) {
                        details.exclude()
                    }
                }

                MiniAppArtifactContract.excludedDirectoryNames.forEach { name ->
                    spec.exclude("**/$name/**")
                }
                spec.exclude("**/build/**")
                spec.exclude("**/*.iml")
                spec.exclude("**/.DS_Store")
                spec.exclude("**/project.private.config.json")
                // Gradle's default excludes already drop a version-control ignore file, but a
                // distribution is an artifact rather than a working copy, so the exclusion is stated
                // here too: it is a rule of this copy, not an accident of the copy mechanism. Every
                // other dotfile is kept, because a mini program may legitimately carry one.
                spec.exclude("**/.gitignore")
            }

            task.from(distribution.map { it.outputs.files }) { spec ->
                val libraryPath = libraryPathInDistribution
                spec.eachFile { details: FileCopyDetails ->
                    details.path = "${libraryPath.get()}/${details.name}"
                }
            }

            val source = sourceDirectory
            val generated = generatedLibraryDirectory
            val assembled = distributionDirectory
            task.doFirst {
                val sourceHost = source.get().asFile
                MiniAppPathDiagnostics.requireSourceInsideProject(sourceHost, projectDirectory)
                MiniAppPathDiagnostics.requireGeneratedLibraryInsideSource(generated.get().asFile, sourceHost)
                MiniAppPathDiagnostics.requireDistributionInsideProject(
                    assembled.get().asFile,
                    projectDirectory,
                )
                if (!sourceHost.isDirectory) {
                    throw GradleException(
                        buildString {
                            appendLine("The Mini App source host does not exist, so there is nothing to assemble:")
                            appendLine("  configured: ${sourceHost.invariantSeparatorsPath}")
                            appendLine()
                            appendLine("Create it once, then edit it freely:")
                            appendLine("    ./gradlew ${MiniAppPluginDiagnostics.INIT_MINIAPP_TASK_NAME}")
                            appendLine()
                            appendLine("If your mini program already exists, point the build at it instead:")
                            appendLine("    miniapp {")
                            appendLine("        wechat {")
                            appendLine("            sourceDirectory.set(layout.projectDirectory.dir(\"<your project>\"))")
                            appendLine("        }")
                            append("    }")
                        },
                    )
                }
            }
        }
    }

    /**
     * `verifyMiniApp` — reads the assembled distribution and nothing else.
     *
     * It depends on `assembleMiniApp` rather than asking the developer to have run it, because a
     * verification that can be pointed at a stale directory is a verification of nothing.
     */
    private fun registerVerifyMiniApp(
        project: Project,
        sourceDirectory: org.gradle.api.file.DirectoryProperty,
        generatedLibraryDirectory: org.gradle.api.file.DirectoryProperty,
        distributionDirectory: org.gradle.api.file.DirectoryProperty,
        projectDirectory: java.io.File,
    ) {
        val libraryPathInDistribution = libraryPathInDistribution(sourceDirectory, generatedLibraryDirectory)

        project.tasks.register(MiniAppPluginDiagnostics.VERIFY_MINIAPP_TASK_NAME) { task ->
            task.group = GROUP
            task.description =
                "Checks that the assembled Mini App distribution is a complete, self-contained " +
                    "WeChat project. Structural evidence only: not WeChat host acceptance."

            task.dependsOn(MiniAppPluginDiagnostics.ASSEMBLE_MINIAPP_TASK_NAME)

            val assembled = distributionDirectory
            val libraryPath = libraryPathInDistribution
            task.doLast { executed ->
                val directory = assembled.get().asFile
                val report = MiniAppDistributionVerification.verify(
                    distributionDirectory = directory,
                    projectDirectory = projectDirectory,
                    libraryPathInSource = libraryPath.get(),
                )

                if (!report.passed) {
                    throw MiniAppDistributionVerification.failure(
                        report.problems,
                        directory.invariantSeparatorsPath,
                    )
                }

                val logger = executed.logger
                logger.lifecycle("miniApp.verify.distribution=${directory.invariantSeparatorsPath}")
                logger.lifecycle("miniApp.verify.miniprogramRoot=${report.miniprogramRoot}")
                logger.lifecycle("miniApp.verify.module=${report.moduleName}")
                logger.lifecycle("miniApp.verify.files=${report.fileCount}")
                logger.lifecycle("miniApp.verify.verified=true")
                logger.lifecycle(
                    "miniApp.verify.scope=structure and module wiring only; WeChat Developer Tools " +
                        "acceptance is a separate, user-run step",
                )
            }
        }
    }

    /**
     * Where the generated library area sits relative to the source host, and therefore where it sits
     * in the assembled project.
     *
     * Computed as a provider so both tasks read the consumer's configuration rather than a snapshot
     * of it, and so a path that is not a descendant fails with the diagnostic that explains what a
     * `Sync` would have deleted.
     */
    private fun libraryPathInDistribution(
        sourceDirectory: org.gradle.api.file.DirectoryProperty,
        generatedLibraryDirectory: org.gradle.api.file.DirectoryProperty,
    ): Provider<String> = sourceDirectory.flatMap { source ->
        generatedLibraryDirectory.map { generated ->
            MiniAppPathDiagnostics.relativeGeneratedLibraryPath(generated.asFile, source.asFile)
        }
    }
}
