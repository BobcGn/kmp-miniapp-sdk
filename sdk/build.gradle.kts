plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.maven.publish)
}

group = "io.github.bobcgn"
version = libs.versions.miniapp.get()

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    // Maven Central always uses signed publications. The opt-out exists solely so a maintainer can
    // publish to Maven Local and prove consumer resolution without copying a private key into the
    // development process; release CI never sets it.
    if (!providers.gradleProperty("miniappLocalPublicationWithoutSigning").isPresent) {
        signAllPublications()
    }

    coordinates(group.toString(), project.name, version.toString())

    pom {
        name.set("KMP Mini App SDK")
        description.set(
            "Kotlin Multiplatform runtime and host-capability contracts for Mini App platforms.",
        )
        inceptionYear.set("2026")
        url.set("https://github.com/BobcGn/kmp-miniapp-sdk")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("BobcGn")
                name.set("BobcGn")
                url.set("https://github.com/BobcGn")
            }
        }
        scm {
            url.set("https://github.com/BobcGn/kmp-miniapp-sdk")
            connection.set("scm:git:git://github.com/BobcGn/kmp-miniapp-sdk.git")
            developerConnection.set("scm:git:ssh://git@github.com/BobcGn/kmp-miniapp-sdk.git")
        }
    }
}

// The SDK's public version statement is generated from the version catalog rather than written in
// MiniAppSdk.kt, so the constant a consumer reads cannot drift from the version this module is built
// and published as. Anything that wants to assert the version would otherwise need a second copy of
// it, which is exactly the drift this avoids.
val sdkVersionSourceDir = layout.buildDirectory.dir("generated/miniappVersion/kotlin")

val generateSdkVersionSource by tasks.registering {
    group = "build"
    description = "Generates the SDK's public version constant from gradle/libs.versions.toml."

    val versionValue = project.version.toString()
    val outputDir = sdkVersionSourceDir

    inputs.property("miniappVersion", versionValue)
    outputs.dir(outputDir)

    doLast {
        val sourceFile = outputDir.get().asFile.resolve(
            "io/github/bobcgn/miniapp/api/GeneratedMiniAppSdkVersion.kt",
        )
        sourceFile.parentFile.mkdirs()
        sourceFile.writeText(
            """
            |package io.github.bobcgn.miniapp.api
            |
            |/** Generated from gradle/libs.versions.toml. Do not edit. */
            |internal object GeneratedMiniAppSdkVersion {
            |    internal const val VALUE: String = "$versionValue"
            |}
            |
            """.trimMargin(),
        )
    }
}

kotlin {
    explicitApi()

    js {
        outputModuleName.set("kmp-miniapp-sdk-kotlin")
        nodejs()
        useCommonJs()
        binaries.library()
        generateTypeScriptDefinitions()
    }

    sourceSets {
        commonMain {
            kotlin.srcDir(generateSdkVersionSource)
        }

        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

// Architecture guard for ADR 0011: the runtime SDK shares client behaviour, host capabilities and
// presentation state, and never renders. The Compose compiler plugin, Material and the host markup
// vocabularies are all one renderer's vocabulary and must not reach this module.
val forbiddenUiNamespacePrefixes = listOf(
    "androidx.compose",
    "org.jetbrains.compose",
    "androidx.appcompat",
    "androidx.activity",
    "androidx.fragment",
    "android.view",
    "android.widget",
    "com.android",
    "kotlinx.html",
    "react",
    "preact",
)

// Groups that exist only to serve a UI framework or a host markup vocabulary, so the whole group is
// forbidden.
val forbiddenUiDependencyGroups = listOf(
    "androidx.compose",
    "org.jetbrains.compose",
    "androidx.appcompat",
    "androidx.activity",
    "androidx.fragment",
    "com.android",
)

// Libraries whose group also carries dependencies this SDK legitimately uses. `org.jetbrains.kotlinx`
// carries kotlinx-coroutines-core, so forbidding that group would ban a real dependency; only the
// exact module can be forbidden. A coordinate matches when it equals the entry or continues with
// "-", which covers the `-js`, `-jvm` and `-common` variants of the same library.
val forbiddenUiDependencyModules = listOf(
    "org.jetbrains.kotlinx:kotlinx-html",
)

// Pins the classifier's semantics. The task fails if any of these is classified against
// expectation, so a later edit cannot silently widen or narrow the rule, and the shared-group case
// above cannot regress into banning a whole group.
val dependencyClassifierExpectations = listOf(
    "org.jetbrains.compose.runtime:runtime" to true,
    "androidx.compose.ui:ui" to true,
    "androidx.compose.ui:ui-graphics" to true,
    "com.android.tools.build:gradle" to true,
    "org.jetbrains.kotlinx:kotlinx-html" to true,
    "org.jetbrains.kotlinx:kotlinx-html-js" to true,
    "org.jetbrains.kotlinx:kotlinx-html-jvm" to true,
    "org.jetbrains.kotlinx:kotlinx-coroutines-core" to false,
    "org.jetbrains.kotlinx:kotlinx-coroutines-test" to false,
    "org.jetbrains.kotlinx:kotlinx-serialization-json" to false,
    "org.jetbrains.kotlin:kotlin-test" to false,
    "org.jetbrains.kotlin:kotlin-stdlib" to false,
)

val declaredDependencyCoordinates: List<String> = configurations
    .flatMap { configuration ->
        configuration.dependencies.mapNotNull { dependency ->
            val group = dependency.group ?: return@mapNotNull null
            "$group:${dependency.name}"
        }
    }
    .distinct()
    .sorted()

val sdkSourceDirectory = layout.projectDirectory.dir("src")

val checkArchitectureBoundaries by tasks.registering {
    group = "verification"
    description =
        "Asserts that the runtime SDK declares and imports no UI framework or renderer (ADR 0011)."

    // Copied into local values so the task action captures plain data rather than the build script.
    val modulePath = path
    val sourceRoot = sdkSourceDirectory
    val dependencyCoordinates = declaredDependencyCoordinates
    val namespacePrefixes = forbiddenUiNamespacePrefixes
    val dependencyGroups = forbiddenUiDependencyGroups
    val dependencyModules = forbiddenUiDependencyModules
    val classifierExpectations = dependencyClassifierExpectations

    inputs.dir(sourceRoot)
    inputs.property("declaredDependencies", dependencyCoordinates)

    doLast {
        fun isForbiddenUiDependency(coordinate: String): Boolean {
            val group = coordinate.substringBefore(':')
            if (dependencyGroups.any { group == it || group.startsWith("$it.") }) return true
            return dependencyModules.any { coordinate == it || coordinate.startsWith("$it-") }
        }

        val violations = mutableListOf<String>()
        val sourceRootFile = sourceRoot.asFile

        // The classifier is checked against its expectations before it is trusted on the model.
        classifierExpectations
            .filter { (coordinate, expected) -> isForbiddenUiDependency(coordinate) != expected }
            .forEach { (coordinate, expected) ->
                violations += "classifier expectation broken: '$coordinate' should be " +
                    (if (expected) "forbidden" else "allowed") + " but is not"
            }

        sourceRootFile.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .forEach { file ->
                file.readLines().forEachIndexed { index, line ->
                    val statement = line.trim()
                    val namespace = when {
                        statement.startsWith("import ") -> statement.removePrefix("import ").trim()
                        statement.startsWith("package ") -> statement.removePrefix("package ").trim()
                        else -> return@forEachIndexed
                    }
                    val forbidden = namespacePrefixes.firstOrNull {
                        namespace == it || namespace.startsWith("$it.")
                    }
                    if (forbidden != null) {
                        violations += "${file.relativeTo(sourceRootFile)}:${index + 1} imports $namespace"
                    }
                }
            }

        dependencyCoordinates
            .filter { isForbiddenUiDependency(it) }
            .forEach { coordinate -> violations += "declares a UI framework dependency: $coordinate" }

        if (violations.isNotEmpty()) {
            throw GradleException(
                "$modulePath must not depend on a UI framework or contain a renderer (ADR 0011):\n" +
                    violations.joinToString("\n") { "  - $it" },
            )
        }
    }
}

tasks.named("check") {
    dependsOn(checkArchitectureBoundaries)
}
