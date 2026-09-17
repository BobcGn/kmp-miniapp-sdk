import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `java-gradle-plugin`
    // Version comes from the root build's `libs.plugins.kotlin.multiplatform` alias, which already
    // puts the whole Kotlin Gradle Plugin distribution on this build's classpath.
    id("org.jetbrains.kotlin.jvm")
}

group = "io.github.bobcgn"
version = libs.versions.miniapp.get()

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    explicitApi()

    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    // Supplied by the consumer's build at runtime; needed only to compile against the Kotlin
    // Multiplatform DSL. The plugin must not ship the Kotlin Gradle Plugin.
    compileOnly(libs.kotlin.gradle.plugin)

    testImplementation(kotlin("test"))
    testImplementation(gradleTestKit())
}

gradlePlugin {
    plugins {
        create("miniapp") {
            id = "io.github.bobcgn.miniapp"
            implementationClass = "io.github.bobcgn.miniapp.gradle.MiniAppGradlePlugin"
            displayName = "Mini App Gradle plugin"
            description = "Adds Mini App platform build integration to a Kotlin Multiplatform project."
        }
    }
}

// The Kotlin Multiplatform fixtures the plugin tests apply must use the same Kotlin version as this
// repository, so the version is read from the catalog rather than duplicated in a fixture.
tasks.withType<Test>().configureEach {
    systemProperty("miniappPlugin.kotlinVersion", libs.versions.kotlin.get())
    // The fixtures consume the runtime SDK as a public module coordinate, so they need the
    // repository that produces it. Only the tests know this path; the plugin never does.
    systemProperty("miniappPlugin.sdkRepository", rootProject.layout.projectDirectory.asFile.absolutePath)
}

// The runtime coordinate the plugin adds to a consumer's `miniappMain`. It is generated from the
// version catalog rather than written in the plugin's source, so the version has exactly one source
// and cannot drift between the plugin and the runtime it wires in. The artifact id is the runtime
// module's project name (`:kmp-miniapp-sdk`), which is also what composite-build substitution
// matches and what a future publication would publish.
val miniAppSdkGroup = "io.github.bobcgn"
val miniAppSdkName = "kmp-miniapp-sdk"
val miniAppMetadataDir = layout.buildDirectory.dir("generated/miniappMetadata")

val generateMiniAppMetadata by tasks.registering(WriteProperties::class) {
    destinationFile.set(miniAppMetadataDir.map { it.file("miniapp-plugin-metadata.properties") })
    property("miniapp.plugin.version", version.toString())
    property("miniapp.sdk.group", miniAppSdkGroup)
    property("miniapp.sdk.name", miniAppSdkName)
    property("miniapp.sdk.version", libs.versions.miniapp.get())
}

sourceSets.named("main") {
    resources.srcDir(miniAppMetadataDir)
}

tasks.named("processResources") {
    dependsOn(generateMiniAppMetadata)
}
