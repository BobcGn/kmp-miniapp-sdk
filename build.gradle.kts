plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
}

val miniAppSdkArtifacts = listOf(
    "kmp-miniapp-sdk-kotlin.js",
    "kmp-miniapp-sdk-kotlin.js.map",
    "kmp-miniapp-sdk-kotlin.d.ts",
    "kotlin-kotlin-stdlib.js",
    "kotlin-kotlin-stdlib.js.map",
    "kotlinx-atomicfu.js",
    "kotlinx-atomicfu.js.map",
    "kotlinx-coroutines-core.js",
    "kotlinx-coroutines-core.js.map",
)

tasks.register<Sync>("buildMiniAppSdk") {
    group = "distribution"
    description = "Builds and copies the Kotlin/JS SDK artifacts into the WeChat integration host."

    dependsOn(":kmp-miniapp-sdk:jsNodeProductionLibraryDistribution")

    val compilerDistribution = project(":kmp-miniapp-sdk").layout.buildDirectory.dir("dist/js/productionLibrary")
    val consumerDistribution = layout.projectDirectory.dir(
        "examples/wechat-miniprogram/miniprogram/libs",
    )

    from(compilerDistribution) {
        include(miniAppSdkArtifacts)
    }
    into(consumerDistribution)

    preserve {
        include("kmp-miniapp-sdk.js", "kmp-miniapp-sdk.d.ts")
    }
}

// The consumer integration fixture is an ordinary Gradle build beside this one, not a module of it:
// it must consume the plugin and the runtime the way an external project does. These tasks run it
// from the repository so the fixture is part of the repository's verification rather than a
// directory somebody remembers to open. They are not wired into `check`, because they compile
// Kotlin/JS and install npm dependencies.
val consumerFixtureDirectory = layout.projectDirectory.dir("fixtures/miniapp-consumer")
val consumerFixtureWrapper = layout.projectDirectory.file("gradlew").asFile.absolutePath

tasks.register<Exec>("verifyMiniAppConsumer") {
    group = "verification"
    description = "Builds the consumer fixture from clean and asserts the default bundle contract."

    workingDir(consumerFixtureDirectory)
    commandLine(
        consumerFixtureWrapper,
        "--no-daemon",
        "--console=plain",
        "clean",
        "miniappTest",
        "assembleMiniAppBundle",
        "verifyConsumerContract",
    )
}

tasks.register<Exec>("verifyMiniAppConsumerHostBundle") {
    group = "verification"
    description = "Assembles the consumer fixture's bundle into the WeChat host's mini program."

    workingDir(consumerFixtureDirectory)
    commandLine(
        consumerFixtureWrapper,
        "--no-daemon",
        "--console=plain",
        "clean",
        "assembleMiniAppBundle",
        "verifyConsumerContract",
        "-PminiappBundleDirectory=host/miniprogram/libs",
    )
}

tasks.register<Exec>("verifyMiniAppConsumerHostSmoke") {
    group = "verification"
    description = "Loads the consumer fixture's bundle the way the WeChat host's page does."

    dependsOn("verifyMiniAppConsumerHostBundle")
    workingDir(consumerFixtureDirectory.dir("host"))
    commandLine("node", "scripts/host-smoke.cjs")
}

tasks.register("verifyMiniAppConsumerFixture") {
    group = "verification"
    description = "Runs every consumer integration fixture check from clean."

    dependsOn("verifyMiniAppConsumer", "verifyMiniAppConsumerHostSmoke")
}

// The single entry point CI calls for the Mini App plugin's integration suite: the plugin's own
// contract and TestKit suites, the architecture boundary that keeps the SDK free of a renderer, and
// the consumer fixture above.
//
// It is deliberately not wired into `check`. The fixture drives a nested Gradle build from clean
// that compiles Kotlin/JS and installs npm dependencies, so making `check` depend on it would put
// several minutes and a network dependency in front of every ordinary build, and would make `check`
// re-enter Gradle. `:miniapp-gradle-plugin:test` is already part of `check` through that project's
// own `check` task; this entry adds the fixture, which `check` does not run.
tasks.register("verifyMiniAppGradlePluginIntegration") {
    group = "verification"
    description =
        "Runs the plugin test suite, the SDK architecture boundary check and the consumer fixture."

    dependsOn(
        ":miniapp-gradle-plugin:test",
        ":kmp-miniapp-sdk:checkArchitectureBoundaries",
        "verifyMiniAppConsumerFixture",
    )
}

// Both fixture runs start with `clean`, so their order is fixed rather than left to the task graph.
tasks.named("verifyMiniAppConsumerHostBundle") {
    mustRunAfter("verifyMiniAppConsumer")
}

// The fixture is a nested build that composite-includes this repository, so it writes to the same
// build directories as the repository's own plugin and SDK tasks. Running them at the same time
// would put two Gradle builds on one project directory; ordering them keeps that impossible.
tasks.named("verifyMiniAppConsumer") {
    mustRunAfter(":miniapp-gradle-plugin:test", ":kmp-miniapp-sdk:checkArchitectureBoundaries")
}

// The READMEs are the consumer's entry point and the first place to go stale: a renamed fixture
// path, a code block added to one language only, or a documented command that stops existing. This
// checks structure and paths, not prose, and it does not read the snippets' contents — the fixture
// stays the code authority, and `verifyMiniAppConsumerFixture` runs the commands the READMEs
// document. It is cheap and offline, so `check` depends on it.
val consumerReadmes = listOf("README-en.md", "README-ch.md").map { layout.projectDirectory.file(it) }

tasks.register("verifyMiniAppConsumerDocs") {
    group = "verification"
    description = "Checks that both READMEs stay parallel and reference paths that exist."

    val readmes = consumerReadmes
    val projectDirectory = layout.projectDirectory.asFile
    val documentedCommands = listOf("./gradlew miniappTest", "./gradlew assembleMiniAppBundle")

    inputs.files(readmes).withPropertyName("readmes")
    inputs.property("documentedCommands", documentedCommands)

    doLast {
        // file, headings, code-block languages — the text is re-read from the file when needed.
        val profiles: List<Triple<File, List<String>, List<String>>> = readmes.map { readme ->
            val file = readme.asFile
            check(file.isFile) { "the README $file does not exist" }
            val lines = file.readLines()
            // Fences come in pairs, so the opening ones are the even indexes.
            val fences = lines.filter { it.trimStart().startsWith("```") }.map { it.trim() }
            check(fences.size % 2 == 0) { "$file has an unclosed code fence" }
            Triple(
                file,
                lines.filter { it.startsWith("#") },
                fences.filterIndexed { index, _ -> index % 2 == 0 }.map { it.drop(3).ifBlank { "(none)" } },
            )
        }

        profiles.forEach { (file, _, _) ->
            val text = file.readText()
            documentedCommands.forEach { command ->
                check(text.contains(command)) {
                    "${file.name} must document '$command'; it is the command a consumer runs."
                }
            }

            Regex("fixtures/miniapp-consumer[^\\s`)\\]\\[|]*")
                .findAll(text)
                .map { it.value.trimEnd('.', ',', ':', ';') }
                .distinct()
                .sorted()
                .forEach { path ->
                    check(File(projectDirectory, path).exists()) {
                        "${file.name} refers to '$path', which does not exist."
                    }
                }
        }

        val (english, chinese) = profiles
        check(english.second.size == chinese.second.size) {
            "the READMEs document a different number of sections: " +
                "${english.first.name} has ${english.second.size}, ${chinese.first.name} has ${chinese.second.size}."
        }
        check(english.third == chinese.third) {
            "the READMEs document a different sequence of code blocks:\n" +
                "  ${english.first.name}: ${english.third}\n" +
                "  ${chinese.first.name}: ${chinese.third}"
        }

        logger.lifecycle(
            "consumerDocs.readmes=${profiles.size} " +
                "consumerDocs.sections=${english.second.size} " +
                "consumerDocs.codeBlocks=${english.third.size} " +
                "consumerDocs.verified=true",
        )
    }
}

tasks.matching { it.name == "check" }.configureEach {
    dependsOn("verifyMiniAppConsumerDocs")
}
