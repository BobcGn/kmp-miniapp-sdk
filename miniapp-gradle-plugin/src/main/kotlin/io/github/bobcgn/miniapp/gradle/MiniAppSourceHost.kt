package io.github.bobcgn.miniapp.gradle

import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import org.gradle.api.GradleException

/**
 * The Mini App source host: the small WeChat project a developer edits by hand.
 *
 * It is created once, explicitly, and never again. Three properties matter and each is a deliberate
 * choice:
 *
 * - **Create only what is absent.** Every file is written with a create-if-absent step, so a second
 *   run changes nothing, a half-initialized project is completed rather than restarted, and a file
 *   the developer edited is left exactly as they left it. The plugin has no notion of the template's
 *   "correct" content once it has been written, and it never restores one.
 * - **Nothing is implicit.** Only [MiniAppPluginDiagnostics.INIT_MINIAPP_TASK_NAME] calls this. Plugin
 *   application, Gradle configuration, and an IDE sync create no part of the source host, because a
 *   build that writes source when it is merely configured is a build nobody can reason about.
 * - **The template is a plugin resource.** The template tree and the created tree have the same
 *   shape; a template's resource name is its destination path relative to the source host, so there
 *   is one list to keep in step rather than two.
 *
 * The template is deliberately minimal. It contains no business page, no renderer, no UI framework,
 * and no capability: the SDK shares behaviour and the host renders, so what a generated project can
 * contain without lying about the platform is a project that opens, loads a module, and says what it
 * found.
 */
internal object MiniAppSourceHost {

    /**
     * One template file: where it is written, and the resource that holds its text.
     *
     * These are two names because they are not always the same one. Gradle's `processResources`
     * applies its default excludes, which cover dotfiles, so a template whose destination begins with
     * a dot has to be shipped under a name that does not. `gitignore` here is that one case.
     */
    public data class Template(val destination: String, val resource: String = destination)

    /**
     * Files the template creates, as paths relative to the source host.
     *
     * `miniprogram/libs/` is absent on purpose. It is the generated library area, so it appears when
     * `syncMiniApp` or `assembleMiniApp` first has a module to put there — creating an empty one
     * would make an unbuilt project look built.
     */
    public val templates: List<Template> = listOf(
        Template(destination = ".gitignore", resource = "gitignore"),
        Template("project.config.json"),
        Template("miniprogram/app.js"),
        Template("miniprogram/app.json"),
        Template("miniprogram/app.wxss"),
        Template("miniprogram/sitemap.json"),
        Template("miniprogram/pages/index/index.js"),
        Template("miniprogram/pages/index/index.json"),
        Template("miniprogram/pages/index/index.wxml"),
        Template("miniprogram/pages/index/index.wxss"),
    )

    /** Where each template is written, in creation order. */
    public val templateDestinations: List<String> = templates.map { it.destination }

    /** Substituted with the consumer's Kotlin/JS output module name, without its extension. */
    private const val MODULE_PLACEHOLDER: String = "__MINIAPP_MODULE__"

    /** Substituted with the Gradle project's own name, so the created project is identifiable. */
    private const val PROJECT_PLACEHOLDER: String = "__MINIAPP_PROJECT__"

    /**
     * The module name `initMiniApp` writes into the generated page.
     *
     * This is a *hint*, not a contract, and the distinction is worth keeping: the Kotlin Gradle
     * Plugin derives `<project name>-<target name>` for a Kotlin/JS library, and the value is a DSL
     * setting a consumer may change. The page is source the developer owns, so a project that sets a
     * different `outputModuleName` edits one line of it. Nothing the build *verifies* depends on this
     * rule — [MiniAppDistributionVerification] reads the module's real name from the distribution.
     */
    public fun defaultModuleName(projectName: String): String =
        "$projectName-${MiniAppPluginDiagnostics.MINIAPP_TARGET_NAME}"

    /** One created or skipped file, reported so a run says what it did rather than only that it ran. */
    public data class Result(val created: List<String>, val skipped: List<String>)

    /**
     * Writes every missing template file under [sourceDirectory], and reports which ones it left
     * alone.
     *
     * The caller has already established that [sourceDirectory] is inside the project — see
     * [MiniAppPathDiagnostics.requireSourceInsideProject] — so this method's only job is to be
     * correct about absence.
     */
    public fun initialize(
        sourceDirectory: File,
        projectName: String,
        moduleName: String = defaultModuleName(projectName),
    ): Result {
        val created = mutableListOf<String>()
        val skipped = mutableListOf<String>()
        templateDestinations.forEach { relativePath ->
            val target = File(sourceDirectory, relativePath)
            if (target.exists()) {
                skipped += relativePath
                return@forEach
            }
            try {
                target.parentFile?.mkdirs()
                target.writeText(render(relativePath, moduleName, projectName), StandardCharsets.UTF_8)
            } catch (exception: IOException) {
                throw GradleException(
                    "Unable to write the Mini App source template '$relativePath' " +
                        "to '$target'.",
                    exception,
                )
            }
            created += relativePath
        }

        return Result(created = created, skipped = skipped)
    }

    /** The template's text with the two placeholders substituted. */
    public fun render(relativePath: String, moduleName: String, projectName: String): String =
        templateText(relativePath)
            .replace(MODULE_PLACEHOLDER, moduleName)
            .replace(PROJECT_PLACEHOLDER, projectName)

    /** The template's text exactly as the plugin ships it, with placeholders intact. */
    public fun templateText(destination: String): String {
        val template = templates.singleOrNull { it.destination == destination }
            ?: error("'$destination' is not a Mini App source template; see MiniAppSourceHost.templates.")
        val resourceName =
            "${MiniAppPluginDiagnostics.MINIAPP_TEMPLATE_RESOURCE_ROOT}/${template.resource}"
        val stream = MiniAppSourceHost::class.java.classLoader.getResourceAsStream(resourceName)
            ?: error(
                "The Mini App plugin is missing its source template '$resourceName'. " +
                    "The template tree is part of the plugin's resources.",
            )
        return stream.use { it.readBytes().toString(StandardCharsets.UTF_8) }
    }
}
