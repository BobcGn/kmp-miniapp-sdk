import io.github.bobcgn.miniapp.gradle.MiniAppExtension
import org.gradle.api.GradleException

plugins {
    // A consumer chooses its own Kotlin version. This fixture pins the one the Mini App plugin is
    // built and tested against, so the fixture proves that pair rather than a mixture.
    kotlin("multiplatform") version "2.4.20"
    // Resolved from the composite build of this repository, with no version, exactly as a published
    // plugin would be resolved from a plugin repository.
    id("io.github.bobcgn.miniapp")
}

// An ordinary consumer: the Kotlin Multiplatform plugin, the Mini App plugin, and nothing else. No
// source sets are declared, no Kotlin/JS is configured, no runtime artifact is named, and no
// generated file is copied by hand.

// The consumer declares its own test dependency, as every Kotlin Multiplatform project does. No
// source set is created or connected here: `miniappMain`, `miniappTest` and their edges to the common
// source sets come from the plugin.
kotlin {
    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

miniapp {
    wechat {
        // The default is `build/miniapp/bundle`. This fixture doubles as the source of artifacts for
        // the WeChat host beside it, so the same build can retarget the bundle into the host's mini
        // program directory:
        //     ./gradlew assembleMiniAppBundle -PminiappBundleDirectory=host/miniprogram/libs
        providers.gradleProperty("miniappBundleDirectory").orNull?.let { target ->
            bundleDirectory.set(layout.projectDirectory.dir(target))
        }
    }
}

val bundleDirectory = extensions.getByType(MiniAppExtension::class.java).wechat.bundleDirectory
val defaultBundleDirectory = layout.buildDirectory.dir("miniapp/bundle").get().asFile
val consumerModuleName = "${rootProject.name}-miniapp"
val customBundleRequested = providers.gradleProperty("miniappBundleDirectory").isPresent

/**
 * Asserts the consumer-facing artifact contract against the bundle this build just produced.
 *
 * It checks contents and behaviour rather than task names: which files exist, what the consumer's
 * own module contains, and where the bundle landed. The renderer guarantee is not re-derived here —
 * `checkMiniAppHostBoundary` owns the dependency-graph rule and runs before assembly — but the file
 * list is scanned as a secondary signal, because a bundle that carried a renderer would carry its
 * files too.
 */
val verifyConsumerContract by tasks.registering {
    group = "mini app"
    description = "Asserts that the assembled Mini App bundle satisfies the consumer contract."

    dependsOn(tasks.named("assembleMiniAppBundle"))
    dependsOn(tasks.named("miniappTest"))

    val bundle = bundleDirectory
    val expectedDefault = defaultBundleDirectory
    val moduleName = consumerModuleName
    val customRequested = customBundleRequested

    doLast {
        fun checkContract(condition: Boolean, message: () -> String) {
            if (!condition) throw GradleException(message())
        }

        val directory = bundle.get().asFile
        checkContract(directory.isDirectory) { "the bundle was not assembled at $directory" }
        val files = directory.listFiles()!!.map { it.name }.sorted()

        if (customRequested) {
            checkContract(directory != expectedDefault) {
                "the configured bundle directory was ignored; the bundle landed at $directory"
            }
            checkContract(!expectedDefault.exists()) {
                "the default bundle directory $expectedDefault was used even though one was configured"
            }
        } else {
            checkContract(directory == expectedDefault) {
                "with no configuration the bundle must default to $expectedDefault, but landed at $directory"
            }
        }

        // The consumer's own module and the declaration a thin host compiles against.
        val declarations = files.filter { it.endsWith(".d.ts") }
        checkContract(declarations.size == 1) {
            "expected exactly one TypeScript declaration in $files, found $declarations"
        }
        checkContract(declarations.single() == "$moduleName.d.ts") {
            "the declaration must be named for the consumer module, found ${declarations.single()}"
        }
        checkContract(files.contains("$moduleName.js")) { "the consumer module is missing from $files" }

        // The runtime the compilation resolved travels with it.
        checkContract(files.contains("kmp-miniapp-sdk-kotlin.js")) { "the Mini App runtime SDK is missing from $files" }
        checkContract(files.contains("kotlin-kotlin-stdlib.js")) { "the Kotlin runtime is missing from $files" }
        checkContract(files.contains("kotlinx-coroutines-core.js")) { "the coroutines runtime is missing from $files" }
        checkContract(files.contains("package.json")) { "package.json is missing from $files" }

        // No host markup: the SDK does not render, and neither does this fixture.
        val markup = files.filter { it.endsWith(".wxml") || it.endsWith(".wxss") }
        checkContract(markup.isEmpty()) { "a Mini App bundle must not carry host markup, found $markup" }

        // Secondary renderer signal. The authoritative rule is the host-boundary check, which reads
        // the dependency graph; this catches a renderer that reached the file list anyway.
        val renderers = files.filter { name ->
            val lower = name.lowercase()
            lower.contains("compose") || lower.contains("skiko") || lower.contains("kotlinx-browser")
        }
        checkContract(renderers.isEmpty()) { "a Mini App bundle must not carry a client renderer, found $renderers" }

        // The consumer's own code and the SDK it calls are really in the module.
        val moduleJs = directory.resolve("$moduleName.js").readText()
        checkContract(moduleJs.contains("hostGreeting")) {
            "$moduleName.js does not export the consumer's host entry point"
        }
        checkContract(moduleJs.contains("sdkVersion")) {
            "$moduleName.js does not export the SDK-backed entry point"
        }
        checkContract(moduleJs.contains("Hello,")) { "$moduleName.js does not carry the consumer's shared code" }
        checkContract(Regex("""\d+\.\d+\.\d+""").containsMatchIn(moduleJs)) {
            "$moduleName.js does not carry the runtime SDK version"
        }

        println("consumerContract.bundleDirectory=${directory.invariantSeparatorsPath}")
        println("consumerContract.files=${files.size}")
        println("consumerContract.verified=true")
    }
}
