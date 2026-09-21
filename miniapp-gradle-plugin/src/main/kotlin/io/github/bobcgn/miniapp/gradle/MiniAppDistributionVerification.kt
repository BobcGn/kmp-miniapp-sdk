package io.github.bobcgn.miniapp.gradle

import groovy.json.JsonSlurper
import java.io.File
import java.nio.charset.StandardCharsets
import org.gradle.api.GradleException

/**
 * Reads an assembled Mini App distribution and reports what is wrong with it.
 *
 * Everything here operates on the distribution alone. It never reads the source host, the Gradle
 * build directory, or the Kotlin Gradle Plugin's output, because the question it answers is exactly
 * "does this directory stand on its own" — a check that consulted the project would pass on a
 * distribution that only works while the project is beside it.
 *
 * What it establishes, and what it deliberately does not:
 *
 * - It establishes structure and wiring. The WeChat project files are present and parse, the
 *   mini program root resolves, every declared page has its entry files, the compiled module and the
 *   modules it requires are all inside the distribution, and every `require()` in it resolves to a
 *   file that is actually there.
 * - It establishes portability. No absolute path, no Gradle build directory, no `node_modules`, and
 *   no reference to the project the distribution was built from.
 * - It does **not** establish that WeChat can run the result. Static reading and a Node smoke are
 *   module-wiring evidence; the real host is WeChat Developer Tools, and only a run there is host
 *   acceptance. Nothing in this file may be described as host acceptance.
 */
internal object MiniAppDistributionVerification {

    /** What the check read, reported so a passing run says what it looked at. */
    public data class Report(
        val problems: List<String>,
        val miniprogramRoot: String?,
        val moduleName: String?,
        val fileCount: Int,
    ) {
        public val passed: Boolean get() = problems.isEmpty()
    }

    private const val PROJECT_CONFIG: String = "project.config.json"
    private const val APP_CONFIG: String = "app.json"

    private val ABSOLUTE_WINDOWS_PATH: Regex = Regex("""^[A-Za-z]:[\\/]""")

    /**
     * @param distributionDirectory the assembled project.
     * @param projectDirectory the project it was built from, used only to recognise its path.
     * @param libraryPathInSource the generated library area's path, relative to the source host, which
     *   is where the assembled project carries it too.
     */
    public fun verify(
        distributionDirectory: File,
        projectDirectory: File,
        libraryPathInSource: String,
    ): Report {
        val problems = mutableListOf<String>()
        val distribution = distributionDirectory.canonicalFile

        if (!distribution.isDirectory) {
            return Report(
                problems = listOf("the distribution $distribution does not exist or is not a directory"),
                miniprogramRoot = null,
                moduleName = null,
                fileCount = 0,
            )
        }

        val files = distribution.walkTopDown().filter { it.isFile }.toList()
        val relativePaths = files.map { it.relativeTo(distribution).invariantSeparatorsPath }
        val root = distribution.toPath()

        requireNoExcludedDirectories(distribution, problems)
        requireNoExcludedEntries(relativePaths, problems)
        requireNoAbsoluteReferences(files, distribution, projectDirectory, problems)

        val miniprogramRoot = readMiniprogramRoot(distribution, problems)
        val miniprogramRootDirectory = miniprogramRoot?.let { root -> distribution.resolve(root) }

        if (miniprogramRoot != null && miniprogramRootDirectory != null) {
            requireMiniProgramFiles(miniprogramRootDirectory, miniprogramRoot, problems)
        }

        val libraryDirectory = distribution.resolve(libraryPathInSource)
        val moduleName = requireLibrary(
            libraryDirectory = libraryDirectory,
            libraryPathInSource = libraryPathInSource,
            miniprogramRootDirectory = miniprogramRootDirectory,
            problems = problems,
        )

        requireRequiresResolve(files, distribution, root, problems)
        requireSourceMapsAreWhole(files, problems)

        return Report(
            problems = problems,
            miniprogramRoot = miniprogramRoot,
            moduleName = moduleName,
            fileCount = files.size,
        )
    }

    public fun failure(problems: List<String>, distributionPath: String): GradleException = GradleException(
        buildString {
            appendLine("The assembled Mini App distribution is not a complete, self-contained host project:")
            problems.forEach { appendLine("  - $it") }
            appendLine()
            appendLine("The distribution is checked on its own: it must not need the project it was built")
            appendLine("from, Gradle, or any node_modules directory. Rebuild it with:")
            appendLine("    ./gradlew assembleMiniApp")
            appendLine()
            appendLine("Structure and wiring pass here are not WeChat host acceptance: only opening the")
            appendLine("distribution in WeChat Developer Tools establishes that. ($distributionPath)")
        },
    )

    // --- checks ---------------------------------------------------------------------------------

    /**
     * A directory that only a package manager, a version-control tool or an IDE creates has no place
     * in a distribution, whether or not it still holds anything.
     *
     * This reads directories rather than the files inside them on purpose: an empty `node_modules`
     * is a directory a file walk never sees, and it is exactly the leftover that tells a reader the
     * exclusion step did not run.
     */
    private fun requireNoExcludedDirectories(distribution: File, problems: MutableList<String>) {
        distribution.walkTopDown()
            .filter { it.isDirectory && it != distribution }
            .forEach { directory ->
                if (directory.name in MiniAppArtifactContract.excludedDirectoryNames) {
                    val relativePath = directory.relativeTo(distribution).invariantSeparatorsPath
                    problems += "$relativePath is a directory the distribution must not carry"
                }
            }
    }

    /** A distribution is uploaded whole, so anything a developer tool wrote locally must be gone. */
    private fun requireNoExcludedEntries(relativePaths: List<String>, problems: MutableList<String>) {
        relativePaths.forEach { relativePath ->
            val name = relativePath.substringAfterLast('/')
            if (MiniAppArtifactContract.isExcludedFileName(name)) {
                problems += "$relativePath is a machine-local or IDE file and must not be shipped"
            }
        }
    }

    /**
     * An absolute path makes the distribution work on exactly one machine, and a reference to the
     * project it was built from means it is not a distribution at all.
     *
     * Both spellings of the project path are checked, because a build directory reached through a
     * symlink — `/var` for `/private/var` on macOS — would otherwise hide a reference to the very
     * directory the distribution is supposed to be independent of.
     */
    private fun requireNoAbsoluteReferences(
        files: List<File>,
        distribution: File,
        projectDirectory: File,
        problems: MutableList<String>,
    ) {
        val projectPaths = listOf(
            projectDirectory.canonicalFile.absolutePath,
            projectDirectory.absoluteFile.absolutePath,
        ).distinct()
        val distributionPath = distribution.absolutePath

        files.forEach { file ->
            val name = file.name
            val isText = name.endsWith(".js") ||
                name.endsWith(".map") ||
                name.endsWith(".json") ||
                name.endsWith(".d.ts") ||
                name.endsWith(".ts") ||
                name.endsWith(".wxml") ||
                name.endsWith(".wxss")
            if (!isText) return@forEach

            val relativePath = file.relativeTo(distribution).invariantSeparatorsPath
            val text = file.readText()

            // One problem per file, even when the text names several of these paths: the
            // distribution's own directory is inside the project, so naming both would report the
            // same defect twice.
            val absoluteReferences = (projectPaths + distributionPath).filter { text.contains(it) }
            if (absoluteReferences.isNotEmpty()) {
                problems += "$relativePath hard-codes an absolute path from the build machine " +
                    "(${absoluteReferences.first()})"
            }
            if (name.endsWith(".map")) {
                absoluteSources(file, relativePath, problems)
            }
        }
    }

    /**
     * A source map's `sources` may name the original Kotlin files by relative path — that is debug
     * metadata and inert at runtime. An absolute one is not: it pins the artifact to the machine that
     * built it, and that is the case this check exists for.
     */
    private fun absoluteSources(mapFile: File, relativePath: String, problems: MutableList<String>) {
        val parsed = parseJson(mapFile, relativePath, problems) as? Map<*, *> ?: return
        val sources = parsed["sources"] as? List<*> ?: return
        sources.filterIsInstance<String>()
            .filterNot { isRelativeReference(it) }
            .forEach { source ->
                problems += "$relativePath names an absolute source path ($source)"
            }
    }

    private fun readMiniprogramRoot(distribution: File, problems: MutableList<String>): String? {
        val configFile = distribution.resolve(PROJECT_CONFIG)
        if (!configFile.isFile) {
            problems += "$PROJECT_CONFIG is missing from the distribution"
            return null
        }
        val parsed = parseJson(configFile, PROJECT_CONFIG, problems) as? Map<*, *> ?: return null
        val root = parsed["miniprogramRoot"]
        if (root !is String) {
            problems += "$PROJECT_CONFIG does not declare a string 'miniprogramRoot'"
            return null
        }
        val resolved = distribution.resolve(root).canonicalFile
        if (!resolved.toPath().startsWith(distribution.canonicalFile.toPath())) {
            problems += "$PROJECT_CONFIG declares 'miniprogramRoot': '$root', which is outside the distribution"
            return null
        }
        if (!resolved.isDirectory) {
            problems += "$PROJECT_CONFIG declares 'miniprogramRoot': '$root', which is not a directory in the distribution"
            return null
        }
        return root
    }

    private fun requireMiniProgramFiles(
        rootDirectory: File,
        miniprogramRoot: String,
        problems: MutableList<String>,
    ) {
        val appConfig = rootDirectory.resolve(APP_CONFIG)
        if (!appConfig.isFile) {
            problems += "$miniprogramRoot$APP_CONFIG is missing; 'miniprogramRoot' does not point at a mini program"
            return
        }
        val parsed = parseJson(appConfig, "$miniprogramRoot$APP_CONFIG", problems) as? Map<*, *> ?: return

        val pages = (parsed["pages"] as? List<*>)?.filterIsInstance<String>().orEmpty()
        if (pages.isEmpty()) {
            problems += "$miniprogramRoot$APP_CONFIG declares no pages, so the mini program has no entry"
        }

        pages.forEach { page ->
            if (page.startsWith("/") || page.split('/').contains("..")) {
                problems += "$miniprogramRoot$APP_CONFIG declares page '$page', which is not inside the mini program root"
                return@forEach
            }
            val scriptFiles = listOf(".js", ".ts").map { extension ->
                rootDirectory.resolve("$page$extension")
            }
            if (scriptFiles.none { it.isFile }) {
                problems += "page '$page' has no '.js' or '.ts' entry file at $miniprogramRoot$page"
            }
            val markupFile = rootDirectory.resolve("$page.wxml")
            if (!markupFile.isFile) {
                problems += "page '$page' has no '.wxml' file at $miniprogramRoot$page.wxml"
            }
            val pageConfig = rootDirectory.resolve("$page.json")
            if (pageConfig.isFile) {
                parseJson(pageConfig, "$miniprogramRoot$page.json", problems)
            }
        }

        val sitemapLocation = parsed["sitemapLocation"]
        if (sitemapLocation is String && sitemapLocation.isNotBlank()) {
            if (!rootDirectory.resolve(sitemapLocation).isFile) {
                problems += "$miniprogramRoot$APP_CONFIG declares 'sitemapLocation': '$sitemapLocation', which does not exist"
            }
        }
    }

    /**
     * The generated library area, checked against the contract the plugin publishes: one module, one
     * declaration, one npm manifest, the modules the module requires, and no markup.
     *
     * The module's *name* is read from the declaration rather than assembled from a project name and
     * a naming rule. The Kotlin Gradle Plugin derives the default, and a consumer may change it; a
     * verifier that recomputed it would reject a project it should accept, and would accept one it
     * should not.
     */
    private fun requireLibrary(
        libraryDirectory: File,
        libraryPathInSource: String,
        miniprogramRootDirectory: File?,
        problems: MutableList<String>,
    ): String? {
        if (!libraryDirectory.isDirectory) {
            problems += "the generated library area $libraryPathInSource is missing from the distribution"
            return null
        }

        if (miniprogramRootDirectory != null &&
            !libraryDirectory.canonicalFile.toPath().startsWith(miniprogramRootDirectory.canonicalFile.toPath())
        ) {
            problems += "the generated library area $libraryPathInSource is outside the mini program root, " +
                "so WeChat would never package it"
        }

        val files = libraryDirectory.walkTopDown().filter { it.isFile }.toList()
        val names = files.map { it.name }

        val declarations = files.filter { it.name.endsWith(MiniAppArtifactContract.DECLARATION_EXTENSION) }
        val moduleName = when (declarations.size) {
            1 -> declarations.single().name.removeSuffix(MiniAppArtifactContract.DECLARATION_EXTENSION)
            else -> {
                problems += "$libraryPathInSource must contain exactly one " +
                    "'${MiniAppArtifactContract.DECLARATION_EXTENSION}' declaring the consumer's module; " +
                    "found ${declarations.map { it.name }.sorted()}"
                null
            }
        }

        val modules = names.filter { it.endsWith(MiniAppArtifactContract.MODULE_EXTENSION) }
        if (modules.isEmpty()) {
            problems += "$libraryPathInSource contains no compiled module, so the host has nothing to require"
        } else if (modules.size < 2) {
            problems += "$libraryPathInSource carries only ${modules.single()}: the consumer's module is never " +
                "alone, because the runtime SDK and the Kotlin runtime travel with it"
        }

        if (moduleName != null && !names.contains("$moduleName${MiniAppArtifactContract.MODULE_EXTENSION}")) {
            problems += "'$moduleName${MiniAppArtifactContract.DECLARATION_EXTENSION}' has no " +
                "'$moduleName${MiniAppArtifactContract.MODULE_EXTENSION}' beside it"
        }

        val manifest = libraryDirectory.resolve(MiniAppArtifactContract.PACKAGE_MANIFEST)
        if (!manifest.isFile) {
            problems += "$libraryPathInSource has no ${MiniAppArtifactContract.PACKAGE_MANIFEST}"
        } else if (moduleName != null) {
            (parseJson(manifest, "$libraryPathInSource/${MiniAppArtifactContract.PACKAGE_MANIFEST}", problems) as? Map<*, *>)
                ?.let { parsed ->
                    if (parsed["main"] != "$moduleName${MiniAppArtifactContract.MODULE_EXTENSION}") {
                        problems += "${MiniAppArtifactContract.PACKAGE_MANIFEST} declares 'main': " +
                            "'${parsed["main"]}', not '$moduleName${MiniAppArtifactContract.MODULE_EXTENSION}'"
                    }
                }
        }

        files.filter { file ->
            MiniAppArtifactContract.hostMarkupExtensions.any { file.name.endsWith(it) }
        }.forEach { file ->
            problems += "$libraryPathInSource carries host markup (${file.name}); the SDK does not render"
        }

        names.filter { name ->
            val lower = name.lowercase()
            MiniAppArtifactContract.rendererNameFragments.any { lower.contains(it) }
        }.forEach { name ->
            problems += "$libraryPathInSource carries a client renderer module ($name)"
        }

        return moduleName
    }

    /**
     * Every module the distribution loads must be in the distribution.
     *
     * The compiler emits CommonJS, so the whole wiring is `require('./x.js')` against a sibling file.
     * A bare specifier needs `node_modules`, which the distribution does not carry; a path that
     * climbs out of the distribution needs the project it was built from.
     */
    private fun requireRequiresResolve(
        files: List<File>,
        distribution: File,
        distributionRoot: java.nio.file.Path,
        problems: MutableList<String>,
    ) {
        val distributionPath = distribution.canonicalFile.absolutePath

        files.filter { it.name.endsWith(MiniAppArtifactContract.MODULE_EXTENSION) }.forEach { file ->
            val relativePath = file.relativeTo(distribution).invariantSeparatorsPath
            MiniAppArtifactContract.requires(file.readText()).forEach { target ->
                if (!target.startsWith("./") && !target.startsWith("../")) {
                    problems += "$relativePath requires '$target', which is not a relative path; " +
                        "a distribution has no node_modules to resolve it from"
                    return@forEach
                }
                val resolved = File(file.parentFile, target).canonicalFile
                if (!resolved.toPath().startsWith(distributionRoot)) {
                    problems += "$relativePath requires '$target', which points outside the distribution"
                    return@forEach
                }
                if (!resolved.isFile) {
                    problems += "$relativePath requires '$target', which is not in the distribution"
                }
                if (resolved.absolutePath.startsWith("$distributionPath/")) {
                    val resolvedRelative = distribution.toPath().relativize(resolved.toPath()).toString()
                    if (resolvedRelative.contains("node_modules")) {
                        problems += "$relativePath requires '$target' from node_modules, which is not shipped"
                    }
                }
            }
        }
    }

    /** A `.js` that names a source map must have one, and every map must be readable. */
    private fun requireSourceMapsAreWhole(files: List<File>, problems: MutableList<String>) {
        files.filter { it.name.endsWith(MiniAppArtifactContract.MODULE_EXTENSION) }.forEach { file ->
            val relativePath = file.name
            val target = MiniAppArtifactContract.sourceMappingUrl(file.readText()) ?: return@forEach
            if (!isRelativeReference(target)) {
                problems += "$relativePath points its sourceMappingURL at '$target' instead of a shipped file"
                return@forEach
            }
            if (!File(file.parentFile, target).isFile) {
                problems += "$relativePath points its sourceMappingURL at '$target', which is not in the distribution"
            }
        }

        files.filter { it.name.endsWith(MiniAppArtifactContract.SOURCE_MAP_EXTENSION) }.forEach { file ->
            val relativePath = file.name
            val parsed = parseJson(file, relativePath, problems) as? Map<*, *> ?: return@forEach
            if (parsed["sources"] !is List<*>) {
                problems += "$relativePath is not a source map: it declares no 'sources'"
            }
        }
    }

    private fun isRelativeReference(target: String): Boolean =
        !target.startsWith("/") &&
            !target.startsWith("\\") &&
            !target.startsWith("http://") &&
            !target.startsWith("https://") &&
            !ABSOLUTE_WINDOWS_PATH.containsMatchIn(target)

    private fun parseJson(file: File, relativePath: String, problems: MutableList<String>): Any? = try {
        JsonSlurper().parseText(file.readText(StandardCharsets.UTF_8))
    } catch (exception: Exception) {
        problems += "$relativePath is not valid JSON (${exception.message})"
        null
    }
}
