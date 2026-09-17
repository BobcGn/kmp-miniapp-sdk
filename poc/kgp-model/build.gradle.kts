import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest

plugins {
    id("org.jetbrains.kotlin.multiplatform") version "2.4.20" apply false
}

// Model modules compared by BOB-83. Each one is a self-contained PoC; the root project only
// adds the inspection/verification tooling, so no module influences another.
val modelPaths = listOf("model-a", "model-b", "model-c", "model-c2")

fun transitiveDependenciesOf(sourceSet: KotlinSourceSet): Set<String> {
    val seen = mutableSetOf<String>()
    val queue = ArrayDeque(sourceSet.dependsOn)
    while (queue.isNotEmpty()) {
        val next = queue.removeFirst()
        if (seen.add(next.name)) {
            queue.addAll(next.dependsOn)
        }
    }
    return seen
}

fun sourceSetFacts(kotlin: KotlinMultiplatformExtension, model: Project, name: String): List<String> {
    val sourceSet = kotlin.sourceSets.findByName(name)
    if (sourceSet == null) {
        return listOf("$name=MISSING")
    }
    val owningCompilations = kotlin.targets.flatMap { target ->
        target.compilations
            .filter { compilation -> compilation.allKotlinSourceSets.any { it.name == name } }
            .map { "${target.name}:${it.name}" }
    }.sorted()
    return listOf(
        "$name.exists=true",
        "$name.dependsOn=${sourceSet.dependsOn.map { it.name }.sorted()}",
        "$name.transitive=${transitiveDependenciesOf(sourceSet).sorted()}",
        "$name.owningCompilations=$owningCompilations",
    )
}

fun modelFacts(model: Project): List<String> {
    val kotlin = model.extensions.getByType(KotlinMultiplatformExtension::class.java)
    val facts = mutableListOf<String>()
    facts += "module=${model.name}"
    facts += "targets=${kotlin.targets.map { "${it.name}:${it.platformType}" }.sorted()}"
    facts += "sourceSets=${kotlin.sourceSets.names.sorted()}"
    facts += sourceSetFacts(kotlin, model, "miniappMain")
    facts += sourceSetFacts(kotlin, model, "miniappTest")
    kotlin.targets.sortedBy { it.name }.forEach { target ->
        target.compilations.sortedBy { it.name }.forEach { compilation ->
            facts += "compilation.${target.name}:${compilation.name}.sourceSets=" +
                compilation.allKotlinSourceSets.map { it.name }.sorted()
            facts += "compilation.${target.name}:${compilation.name}.compileTask=${compilation.compileKotlinTaskName}"
            facts += "compilation.${target.name}:${compilation.name}.defaultSourceSet=${compilation.defaultSourceSet.name}"
        }
    }
    facts += "tasks.miniapp=${model.tasks.names.filter { it.contains("miniApp", ignoreCase = true) }.sorted()}"
    facts += "tasks.jsTestTasks=${model.tasks.withType(KotlinJsTest::class.java).names.sorted()}"
    facts += "configurations.miniapp=" +
        model.configurations.names.filter { it.contains("miniapp", ignoreCase = true) }.sorted()
    return facts
}

fun checkFacts(model: Project): List<Pair<String, Boolean>> {
    val kotlin = model.extensions.getByType(KotlinMultiplatformExtension::class.java)
    val sourceSetNames = kotlin.sourceSets.names
    val buildScript = model.layout.projectDirectory.file("build.gradle.kts").asFile.readText()

    val miniappMain = kotlin.sourceSets.findByName("miniappMain")
    val miniappTest = kotlin.sourceSets.findByName("miniappTest")

    fun ownsCompilation(name: String): Boolean = kotlin.targets.any { target ->
        target.compilations.any { compilation ->
            compilation.allKotlinSourceSets.any { it.name == name }
        }
    }

    val manualJsWiringTokens = listOf(
        "useCommonJs(",
        "generateTypeScriptDefinitions(",
        "binaries.library(",
        "nodejs(",
        "sourceSets.create(",
        "js {",
        "js(",
    )
    val leakedTokens = manualJsWiringTokens.filter { buildScript.contains(it) }

    return listOf(
        "source set 'miniappMain' exists" to (miniappMain != null),
        "source set 'miniappTest' exists" to (miniappTest != null),
        "miniappMain depends on commonMain" to
            (miniappMain != null && "commonMain" in transitiveDependenciesOf(miniappMain)),
        "miniappTest depends on commonTest" to
            (miniappTest != null && "commonTest" in transitiveDependenciesOf(miniappTest)),
        "a compilation consumes miniappMain" to ownsCompilation("miniappMain"),
        "a compilation consumes miniappTest" to ownsCompilation("miniappTest"),
        "lifecycle task 'miniappTest' exists" to model.tasks.names.contains("miniappTest"),
        "a real Kotlin/JS test task exists for the miniapp target" to
            model.tasks.withType(KotlinJsTest::class.java).names.any { it.contains("miniapp") },
        "no 'jsMain' / 'jsTest' source set is exposed to the consumer" to
            ("jsMain" !in sourceSetNames && "jsTest" !in sourceSetNames),
        "consumer build script contains no manual Kotlin/JS wiring $leakedTokens" to
            leakedTokens.isEmpty(),
    )
}

// The hierarchy template is applied in KGP's own afterEvaluate, so the model is only complete after
// every project has been evaluated. Values are captured as plain strings so the task actions stay
// serializable and the chosen model can be verified under the configuration cache.
gradle.projectsEvaluated {
    modelPaths.forEach { path ->
        val model = project(":$path")
        if (!model.pluginManager.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
            return@forEach
        }

        val moduleName = model.name
        val facts = modelFacts(model)
        val checks = checkFacts(model)

        model.tasks.register("reportKgpModel") {
            group = "verification"
            description = "Prints the KGP model shape of this module for BOB-83 comparison."
            val snapshot = facts
            doLast { snapshot.forEach { System.out.println(it) } }
        }

        model.tasks.register("checkMiniAppModel") {
            group = "verification"
            description = "Asserts the miniappMain / miniappTest model requirements of BOB-83."
            val snapshot = checks
            val name = moduleName
            doLast {
                snapshot.forEach { (requirement, passed) ->
                    System.out.println("${if (passed) "PASS" else "FAIL"} $requirement")
                }
                val failed = snapshot.filterNot { it.second }
                if (failed.isNotEmpty()) {
                    throw GradleException(
                        "$name: ${failed.size} miniapp model requirement(s) not met: " +
                            failed.joinToString("; ") { it.first },
                    )
                }
            }
        }
    }
}
