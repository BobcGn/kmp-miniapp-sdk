package io.github.bobcgn.miniapp.gradle

import java.io.IOException
import java.nio.file.Files
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.result.UnresolvedDependencyResult
import org.gradle.api.tasks.Sync
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl

/**
 * Registers the Mini App platform target, its source sets, its local test run, its runtime, and the
 * consumer-facing assembly task.
 *
 * ADR 0010 chose a Kotlin/JS target named after the platform: the Kotlin Gradle Plugin then derives
 * the real `miniappMain` and `miniappTest` source sets, the compilations that own them, and the
 * `commonMain` / `commonTest` edges, from that name. The plugin creates the target, the host output
 * shape, the test run, the runtime dependency and the bundle task. It also materializes the two
 * conventional Kotlin source directories so an IDE import exposes writable `miniappMain` and
 * `miniappTest` folders even in an otherwise empty project. It never creates a source-set model or
 * a `dependsOn` edge, because hand-built ones are exactly the "unused source set" model ADR 0010
 * rejected; the Kotlin Gradle Plugin still owns that model.
 *
 * `js(name, configure)` creates the target when it is absent and reconfigures it when an existing
 * target was produced from the same preset. That is what makes applying this plugin twice harmless:
 * the second call reaches the same target rather than adding a second one.
 *
 * Deliberately still absent, because each belongs to a later issue:
 *
 * - host business configuration, including anything wechat-specific, is BOB-84's DSL.
 * - No publication to any repository happens here.
 *
 * This class references Kotlin Gradle Plugin types, so it is kept separate from
 * [MiniAppGradlePlugin]: the plugin must be able to report a missing Kotlin Multiplatform plugin
 * without the Kotlin Gradle Plugin being on the build classpath at all.
 */
internal object MiniAppPlatformSupport {

    public fun registerMiniAppPlatformTarget(project: Project) {
        val kotlin = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
        kotlin.js(MiniAppPluginDiagnostics.MINIAPP_TARGET_NAME) {
            configureMiniAppHostOutput()
        }
        createMiniAppSourceDirectories(project)
        addMiniAppRuntimeDependency(project)
        registerMiniAppHostBoundaryCheck(project)
        registerMiniAppBundleTask(project)
    }

    /**
     * Creates the conventional source directories beside `commonMain` and `commonTest`.
     *
     * Kotlin Gradle Plugin provisions the source-set model but intentionally does not create empty
     * directories on disk. Creating the default `kotlin` directories during plugin application
     * makes the source sets visible and immediately usable after an IDE Gradle import, regardless
     * of which project wizard produced the consumer. [Files.createDirectories] is idempotent and
     * preserves existing sources, so repeated imports and repeated plugin application are safe.
     */
    private fun createMiniAppSourceDirectories(project: Project) {
        listOf(
            MiniAppPluginDiagnostics.MINIAPP_MAIN_KOTLIN_DIRECTORY,
            MiniAppPluginDiagnostics.MINIAPP_TEST_KOTLIN_DIRECTORY,
        ).forEach { relativePath ->
            val directory = project.layout.projectDirectory.dir(relativePath).asFile.toPath()
            try {
                Files.createDirectories(directory)
            } catch (exception: IOException) {
                throw GradleException(
                    "Unable to create the Mini App source directory '$relativePath' " +
                        "in project '${project.path}'.",
                    exception,
                )
            }
        }
    }

    /**
     * Configures the Kotlin/JS output shape a mini app host integration is expected to consume.
     * Whether a real host can load the result is not established here and must not be implied.
     *
     * The host needs a CommonJS module it can `require`, the TypeScript declaration for the thin host
     * code that calls it, and the runtime modules its compilation needs. All three are Kotlin/JS
     * build configuration, which is exactly what this plugin exists to hide: a consumer writing
     * `useCommonJs()` or `generateTypeScriptDefinitions()` by hand is the failure mode this replaces.
     *
     * CommonJS is the current WeChat distribution strategy rather than the permanent ABI of every
     * future host. When a host needs a different shape, that belongs to the host configuration
     * (BOB-84) and must not rename the platform's source sets.
     *
     * `nodejs()` is the local test environment, not the deployment host.
     */
    private fun KotlinJsTargetDsl.configureMiniAppHostOutput() {
        nodejs()
        useCommonJs()
        binaries.library()
        generateTypeScriptDefinitions()
    }

    /**
     * Rejects a Mini App runtime classpath that would produce a distribution carrying a client renderer.
     *
     * Without this, the failure mode is silent: a consumer whose `commonMain` contains Compose UI
     * compiles, distributes and bundles successfully, and the artifact is simply unusable in the
     * host — carrying Compose, Skiko and a browser runtime. A build that cannot work should say so
     * instead of producing a large artifact that looks finished.
     *
     * The check inspects the *dependency graph*, not the bundle's files. Files in a Kotlin/JS
     * distribution are reached through `require()` from one another, so deleting the ones that look
     * like a renderer would trade a build failure for a load failure.
     *
     * It reads `miniappRuntimeClasspath`, which is exactly the set the Kotlin/JS production library
     * distribution is built from, and resolves it at execution time so no other task pays for it.
     *
     * A classpath that did not fully resolve is refused rather than read, because
     * [ResolutionResult.allComponents] omits unresolved dependencies: without that refusal the check
     * would report "no renderer" for a graph it never saw.
     */
    private fun registerMiniAppHostBoundaryCheck(project: Project) {
        val runtimeClasspath = project.configurations
            .named(MiniAppPluginDiagnostics.MINIAPP_RUNTIME_CLASSPATH_CONFIGURATION_NAME)
            .map { configuration ->
                val resolution = configuration.incoming.resolutionResult
                MiniAppRuntimeClasspath(
                    coordinates = resolution.allComponents
                        .mapNotNull { component ->
                            (component.id as? ModuleComponentIdentifier)?.let { "${it.group}:${it.module}" }
                        }
                        .distinct()
                        .sorted(),
                    unresolved = resolution.allDependencies
                        .filterIsInstance<UnresolvedDependencyResult>()
                        .map { it.requested.displayName }
                        .distinct()
                        .sorted(),
                )
            }

        project.tasks.register(MiniAppPluginDiagnostics.CHECK_MINIAPP_HOST_BOUNDARY_TASK_NAME) { task ->
            task.group = "mini app"
            task.description =
                "Fails when the Mini App runtime classpath carries a client renderer such as " +
                    "Compose or Skiko, which a Mini App host cannot run."

            val classpath = runtimeClasspath
            task.doLast {
                val resolved = classpath.get()
                if (resolved.unresolved.isNotEmpty()) {
                    throw GradleException(MiniAppHostBoundary.describeUnresolved(resolved.unresolved))
                }
                val offenders = resolved.coordinates.filter { coordinate ->
                    MiniAppHostBoundary.isForbiddenModule(
                        coordinate.substringBefore(':'),
                        coordinate.substringAfter(':'),
                    )
                }
                if (offenders.isNotEmpty()) {
                    throw GradleException(MiniAppHostBoundary.describeOffenders(offenders))
                }
            }
        }
    }

    /**
     * Registers the stable, documented assembly task a consumer runs to get the compiler-managed
     * distribution. That the distribution is *loadable* by a real host is a separate question this
     * plugin does not answer, and must not be implied by the task's existence.
     *
     * The Kotlin/JS distribution already contains everything the host needs — the consumer's module,
     * its declaration, and the runtime modules its compilation requires — but its path and layout
     * belong to the Kotlin Gradle Plugin. This task republishes that output at the host's configured
     * path, so a host integration depends on a contract instead of on a plugin's build directory.
     *
     * `Sync` rather than `Copy`: a removed runtime module must disappear from the bundle, and a
     * stale file in a directory a host loads verbatim is worse than an extra delete. Because `Sync`
     * also deletes, the destination is validated before it runs.
     *
     * The source is the distribution task's *declared output*, not a hand-written path, so a Kotlin
     * Gradle Plugin layout change moves the bundle with it instead of silently emptying it.
     *
     * The host-boundary check runs first, so a bundle that could not work is never produced.
     */
    private fun registerMiniAppBundleTask(project: Project) {
        val distribution = project.tasks.named(MiniAppPluginDiagnostics.MINIAPP_DISTRIBUTION_TASK_NAME)
        val hostBundleDirectory = project.extensions.getByType(MiniAppExtension::class.java)
            .wechat.bundleDirectory
        val projectDirectory = project.layout.projectDirectory.asFile

        project.tasks.register(
            MiniAppPluginDiagnostics.ASSEMBLE_MINIAPP_BUNDLE_TASK_NAME,
            Sync::class.java,
        ) { task ->
            task.group = "mini app"
            task.description =
                "Assembles the Mini App host bundle: the compiled module, its TypeScript " +
                    "declaration and the runtime modules it needs."
            task.dependsOn(MiniAppPluginDiagnostics.CHECK_MINIAPP_HOST_BOUNDARY_TASK_NAME)
            task.into(hostBundleDirectory)
            task.from(distribution.map { it.outputs.files })

            // `doFirst`, so an unusable destination fails before Sync deletes anything.
            val configuredBundleDirectory = hostBundleDirectory
            val projectDir = projectDirectory
            task.doFirst {
                MiniAppBundleDirectoryDiagnostics.requireInsideProject(
                    configuredBundleDirectory.get().asFile,
                    projectDir,
                )
            }
        }
    }

    /**
     * Adds the runtime SDK to `miniappMain`, and to nothing else.
     *
     * `miniappTest` reaches the runtime through the source-set hierarchy: the test compilation
     * depends on the main compilation, so one declaration on `miniappMain` gives the test classpath
     * the same artifact. Declaring it twice would let the two resolve to different things.
     *
     * `commonMain`, the metadata compilation and every other target are untouched, so a project that
     * also targets Android, iOS or the JVM does not acquire a Mini App runtime on those platforms.
     *
     * The configuration is matched lazily because the Kotlin Gradle Plugin creates a source set's
     * dependency configurations as the target is configured, and an existing declaration is left
     * alone, so applying the plugin twice — or a consumer that declared the runtime itself — still
     * yields exactly one dependency.
     */
    private fun addMiniAppRuntimeDependency(project: Project) {
        val runtime = MiniAppRuntimeDependency
        project.configurations
            .matching { configuration ->
                configuration.name == MiniAppRuntimeDependency.CONSUMER_CONFIGURATION_NAME
            }
            .configureEach { configuration ->
                val alreadyDeclared = configuration.dependencies.any { dependency ->
                    dependency.group == runtime.group && dependency.name == runtime.name
                }
                if (!alreadyDeclared) {
                    configuration.dependencies.add(project.dependencies.create(runtime.coordinate))
                }
            }
    }
}
