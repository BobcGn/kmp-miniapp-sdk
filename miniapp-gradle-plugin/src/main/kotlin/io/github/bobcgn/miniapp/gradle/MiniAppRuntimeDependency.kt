package io.github.bobcgn.miniapp.gradle

import java.util.Properties

/**
 * The Mini App runtime coordinate this plugin adds to a consumer's `miniappMain`.
 *
 * The coordinate is deliberately absent from this file. `miniapp-gradle-plugin/build.gradle.kts`
 * generates `miniapp-plugin-metadata.properties` from `gradle/libs.versions.toml`, which is the
 * single source of the version the runtime SDK and this plugin share. Writing the version here — or
 * into the plugin's tests — would create a second copy that can drift from the artifact actually
 * published.
 *
 * The coordinate is a public module coordinate on purpose. The plugin runs inside a consumer build
 * that has no access to this repository's project paths, so `project(":sdk")` is not available to
 * it; today the coordinate resolves through a composite build, and after publication it resolves
 * from a repository, with the same group, name and version either way.
 */
internal object MiniAppRuntimeDependency {

    /** The configuration a consumer's `miniappMain` compiles and runs against. */
    public const val CONSUMER_CONFIGURATION_NAME: String = "miniappMainApi"

    private const val METADATA_RESOURCE = "miniapp-plugin-metadata.properties"

    private val metadata: Properties = Properties().apply {
        val stream = MiniAppGradlePlugin::class.java.classLoader.getResourceAsStream(METADATA_RESOURCE)
            ?: error(
                "The Mini App plugin is missing its generated '$METADATA_RESOURCE'. " +
                    "The plugin build generates it from gradle/libs.versions.toml.",
            )
        stream.use { load(it) }
    }

    public val group: String get() = require("miniapp.sdk.group")

    public val name: String get() = require("miniapp.sdk.name")

    public val version: String get() = require("miniapp.sdk.version")

    public val coordinate: String get() = "$group:$name:$version"

    private fun require(key: String): String = checkNotNull(metadata.getProperty(key)) {
        "The generated '$METADATA_RESOURCE' does not define '$key'."
    }
}
