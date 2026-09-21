package io.github.bobcgn.miniapp.gradle

import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit coverage for the distribution verifier.
 *
 * The verifier is a pure function over a directory, so its negative cases are built here rather than
 * driven through Gradle: a TestKit test that had to reproduce a broken `require()` would
 * spend minutes compiling Kotlin/JS to assert something a five-line file produces instantly. The
 * Gradle side of the contract — that `verifyMiniApp` assembles first and reads only the
 * distribution — is covered by [MiniAppDistributionTest].
 *
 * Every case below states one defect and asserts the message names it, because a verifier that only
 * says "invalid" is not usable when a build stops.
 */
class MiniAppDistributionVerificationTest {

    private val moduleName = "sample-miniapp"
    private val libraryPath = "miniprogram/libs"

    @Test
    fun `a well-formed distribution passes and reports what it read`() {
        val project = newProject()
        val distribution = writeDistribution(project)

        val report = verify(project, distribution)

        assertTrue(report.passed, "expected no problems, got ${report.problems}")
        assertEquals("miniprogram/", report.miniprogramRoot)
        assertEquals(moduleName, report.moduleName)
        assertTrue(report.fileCount > 0)
    }

    @Test
    fun `a distribution without project config is rejected`() {
        val problems = problemsAfter { it.resolve("project.config.json").delete() }
        assertProblem(problems, "project.config.json is missing")
    }

    @Test
    fun `a miniprogramRoot pointing outside the distribution is rejected`() {
        val problems = problemsAfter {
            it.resolve("project.config.json").writeText("""{"miniprogramRoot": "../elsewhere/"}""")
        }
        assertProblem(problems, "outside the distribution")
    }

    @Test
    fun `a miniprogramRoot that is not a directory is rejected`() {
        val problems = problemsAfter {
            it.resolve("project.config.json").writeText("""{"miniprogramRoot": "miniprogram/app.js"}""")
        }
        assertProblem(problems, "is not a directory")
    }

    @Test
    fun `an app config with no pages is rejected`() {
        val problems = problemsAfter {
            it.resolve("miniprogram/app.json").writeText("""{"pages": []}""")
        }
        assertProblem(problems, "declares no pages")
    }

    @Test
    fun `a page without its entry files is rejected`() {
        val missingWxml = problemsAfter { it.resolve("miniprogram/pages/index/index.wxml").delete() }
        assertProblem(missingWxml, "has no '.wxml' file")

        val missingScript = problemsAfter { it.resolve("miniprogram/pages/index/index.js").delete() }
        assertProblem(missingScript, "has no '.js' or '.ts' entry file")
    }

    @Test
    fun `a TypeScript page is a valid WeChat entry file`() {
        val project = newProject()
        val distribution = writeDistribution(project)
        val pageDirectory = distribution.resolve("miniprogram/pages/index")
        pageDirectory.resolve("index.js").renameTo(pageDirectory.resolve("index.ts"))

        val report = verify(project, distribution)

        assertTrue(report.passed, "a TypeScript host page must be accepted, got ${report.problems}")
    }

    @Test
    fun `an unreachable sitemap is rejected`() {
        val problems = problemsAfter { it.resolve("miniprogram/sitemap.json").delete() }
        assertProblem(problems, "sitemapLocation': 'sitemap.json', which does not exist")
    }

    @Test
    fun `invalid JSON is named rather than silently accepted`() {
        val problems = problemsAfter { it.resolve("miniprogram/app.json").writeText("{ not json") }
        assertProblem(problems, "miniprogram/app.json is not valid JSON")
    }

    @Test
    fun `a library without a module declaration is rejected`() {
        val problems = problemsAfter { it.resolve(libraryPath).resolve("$moduleName.d.ts").delete() }
        assertProblem(problems, "exactly one '.d.ts'")
    }

    @Test
    fun `a library without the modules the module requires is rejected`() {
        val problems = problemsAfter { it.resolve(libraryPath).resolve("kotlin-kotlin-stdlib.js").delete() }
        assertProblem(problems, "requires './kotlin-kotlin-stdlib.js', which is not in the distribution")
    }

    @Test
    fun `a module is never alone in the library`() {
        val problems = problemsAfter { root ->
            val library = root.resolve(libraryPath)
            library.listFiles()!!.filter { it.isFile && it.name != "$moduleName.js" }.forEach { it.delete() }
        }
        assertTrue(
            problems.any { it.contains("the consumer's module is never alone") },
            "expected the isolated-module finding, got $problems",
        )
    }

    @Test
    fun `a bare require is rejected because a distribution ships no node_modules`() {
        val problems = problemsAfter {
            it.appendTo("$libraryPath/$moduleName.js", "\nvar helper = require('lodash');\n")
        }
        assertProblem(problems, "requires 'lodash', which is not a relative path")
    }

    @Test
    fun `a require that escapes the distribution is rejected`() {
        val problems = problemsAfter {
            it.appendTo("$libraryPath/$moduleName.js", "\nvar outside = require('../../../outside.js');\n")
        }
        assertProblem(problems, "points outside the distribution")
    }

    @Test
    fun `a package manifest whose main is not the module is rejected`() {
        val problems = problemsAfter {
            it.resolve(libraryPath).resolve("package.json")
                .writeText("""{"name": "$moduleName", "main": "something-else.js"}""")
        }
        assertProblem(problems, "'main': 'something-else.js'")
    }

    @Test
    fun `host markup in the library is rejected`() {
        val problems = problemsAfter { it.resolve(libraryPath).resolve("leak.wxml").writeText("<view/>") }
        assertProblem(problems, "carries host markup")
    }

    @Test
    fun `a client renderer module in the library is rejected`() {
        val problems = problemsAfter {
            it.resolve(libraryPath).resolve("compose-runtime.js").writeText("module.exports = {};\n")
        }
        assertProblem(problems, "carries a client renderer module")
    }

    @Test
    fun `a library outside the mini program root is rejected`() {
        val project = newProject()
        val distribution = writeDistribution(project)
        // The library reaches the distribution but not the directory WeChat packages, which is the
        // case that builds and verifies structurally while the host would never load it.
        val library = distribution.resolve("miniprogram/libs")
        val outside = distribution.resolve("libs").apply { mkdirs() }
        library.listFiles()!!.forEach { it.renameTo(outside.resolve(it.name)) }
        library.delete()

        val report = MiniAppDistributionVerification.verify(
            distributionDirectory = distribution,
            projectDirectory = project,
            libraryPathInSource = "libs",
        )

        assertProblem(report.problems, "is outside the mini program root")
    }

    @Test
    fun `an absolute path in a distribution file is rejected`() {
        val problems = problemsAfter { root ->
            root.appendTo("$libraryPath/$moduleName.js", "\n// built at ${root.absolutePath}\n")
        }
        assertProblem(problems, "hard-codes an absolute path from the build machine")
    }

    @Test
    fun `an absolute path in a source map is rejected`() {
        val problems = problemsAfter {
            it.resolve(libraryPath).resolve("$moduleName.js.map")
                .writeText(sourceMap(sources = listOf("/Users/somebody/src/Shared.kt")))
        }
        assertProblem(problems, "names an absolute source path")
    }

    @Test
    fun `a source map that points nowhere is rejected`() {
        val problems = problemsAfter { it.resolve(libraryPath).resolve("$moduleName.js.map").delete() }
        assertProblem(problems, "points its sourceMappingURL at '$moduleName.js.map'")
    }

    @Test
    fun `machine-local and IDE files are rejected`() {
        assertProblem(
            problemsAfter { it.resolve(libraryPath).resolve("node_modules").mkdirs() },
            "node_modules is a directory the distribution must not carry",
        )
        assertProblem(
            problemsAfter { it.resolve("project.private.config.json").writeText("{}") },
            "project.private.config.json is a machine-local or IDE file",
        )
        assertProblem(
            problemsAfter { it.resolve("miniprogram/pages/index/stray.iml").writeText("") },
            "stray.iml is a machine-local or IDE file",
        )
    }

    @Test
    fun `the library path is refused when it is not inside the source host`() {
        val project = newProject()
        val source = project.resolve("miniApp").apply { mkdirs() }
        val generated = project.resolve("elsewhere/libs").apply { mkdirs() }
        val failure = runCatching {
            MiniAppPathDiagnostics.relativeGeneratedLibraryPath(generated, source)
        }.exceptionOrNull()

        assertTrue(failure != null, "a generated area outside the source host must be refused")
        assertContains(failure.message!!, MiniAppPathDiagnostics.GENERATED_LIBRARY_HEADER)
    }

    // --- fixtures -------------------------------------------------------------------------------

    private fun newProject(): File = createTempDirectory("miniapp-verify").toFile()

    /**
     * A problem is asserted by fragment rather than by equality: the message a developer reads is the
     * point, and a check that matched whole strings would let an unhelpful reword pass.
     */
    private fun assertProblem(problems: List<String>, fragment: String) {
        assertTrue(
            problems.any { it.contains(fragment) },
            "expected a problem mentioning '$fragment', got:\n${problems.joinToString("\n")}",
        )
    }

    private fun problemsAfter(mutation: (File) -> Unit): List<String> {
        val project = newProject()
        val distribution = writeDistribution(project)
        mutation(distribution)
        return verify(project, distribution).problems
    }

    private fun verify(project: File, distribution: File) = MiniAppDistributionVerification.verify(
        distributionDirectory = distribution,
        projectDirectory = project,
        libraryPathInSource = libraryPath,
    )

    private fun File.appendTo(relativePath: String, text: String) {
        resolve(relativePath).appendText(text)
    }

    /**
     * A distribution shaped the way the pipeline produces one: a WeChat project whose mini program
     * root holds the consumer's page and the generated library area beside it.
     */
    private fun writeDistribution(project: File): File {
        val distribution = project.resolve("build/miniapp-dist").apply { mkdirs() }
        val miniprogram = distribution.resolve("miniprogram").apply { mkdirs() }
        val page = miniprogram.resolve("pages/index").apply { mkdirs() }
        val library = miniprogram.resolve("libs").apply { mkdirs() }

        distribution.resolve("project.config.json")
            .writeText("""{"miniprogramRoot": "miniprogram/", "compileType": "miniprogram"}""")
        miniprogram.resolve("app.js").writeText("App({});\n")
        miniprogram.resolve("app.json")
            .writeText("""{"pages": ["pages/index/index"], "sitemapLocation": "sitemap.json"}""")
        miniprogram.resolve("app.wxss").writeText("page { background: #fff; }\n")
        miniprogram.resolve("sitemap.json").writeText("""{"rules": [{"action": "allow", "page": "*"}]}""")
        page.resolve("index.js").writeText("Page({ data: {}, onLoad() {} });\n")
        page.resolve("index.json").writeText("""{"navigationBarTitleText": "Sample"}""")
        page.resolve("index.wxml").writeText("<view>sample</view>\n")
        page.resolve("index.wxss").writeText(".sample { color: #000; }\n")

        // The consumer's module requires its runtime, the way `useCommonJs()` emits it.
        library.resolve("$moduleName.js").writeText(
            """
            |(function (_, kotlin_kotlin, kotlin_sdk) {
            |  'use strict';
            |  return _;
            |}(module.exports, require('./kotlin-kotlin-stdlib.js'), require('./kmp-miniapp-sdk-kotlin.js')));
            |
            |//# sourceMappingURL=$moduleName.js.map
            """.trimMargin(),
        )
        library.resolve("$moduleName.d.ts").writeText("export declare namespace consumer {\n}\n")
        library.resolve("package.json")
            .writeText("""{"name": "$moduleName", "main": "$moduleName.js", "types": "$moduleName.d.ts"}""")
        library.resolve("$moduleName.js.map")
            .writeText(sourceMap(sources = listOf("../../../../src/miniappMain/kotlin/consumer/Entry.kt")))

        listOf("kotlin-kotlin-stdlib.js", "kmp-miniapp-sdk-kotlin.js").forEach { runtimeModule ->
            library.resolve(runtimeModule).writeText(
                "module.exports = {};\n//# sourceMappingURL=${runtimeModule}.map\n",
            )
            library.resolve("${runtimeModule}.map")
                .writeText(sourceMap(sources = listOf("NATIVE_IMPLEMENTATIONS.kt")))
        }

        return distribution
    }

    private fun sourceMap(sources: List<String>): String =
        """{"version": 3, "file": "x.js", "sources": ${sources.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }}, "mappings": ""}"""
}
