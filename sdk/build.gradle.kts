plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

group = "io.github.bobcgn"
version = "0.1.0-SNAPSHOT"

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

val forbiddenUiDependencyGroups = listOf(
    "androidx.compose",
    "org.jetbrains.compose",
    "androidx.appcompat",
    "androidx.activity",
    "androidx.fragment",
    "com.android",
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

    inputs.dir(sourceRoot)
    inputs.property("declaredDependencies", dependencyCoordinates)

    doLast {
        val violations = mutableListOf<String>()
        val sourceRootFile = sourceRoot.asFile

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
            .filter { coordinate ->
                val group = coordinate.substringBefore(':')
                dependencyGroups.any { group == it || group.startsWith("$it.") }
            }
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
