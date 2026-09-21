package io.github.bobcgn.miniapp.gradle

/**
 * The shape of a Kotlin/JS production library, as the Mini App build depends on it.
 *
 * These are the facts the distribution pipeline reads out of the compiler's output rather than
 * re-deriving: the module format the host loads, the file extensions that carry each part of the
 * contract, and the two comment forms the compiler emits that a host integration has to keep whole.
 *
 * They are recorded in one place because two of them are read at *build* time — a `Sync` copies the
 * distribution verbatim — and the rest only at *verification* time, and a rule that exists in one and
 * not the other is how a bundle stops matching what is checked. Nothing here names a Kotlin Gradle
 * Plugin build directory or a module: the task graph supplies those.
 */
internal object MiniAppArtifactContract {

    /** The host's module system. `useCommonJs()` is what the platform configures. */
    public const val MODULE_SYSTEM: String = "CommonJS"

    /** The consumer's compiled module. */
    public const val MODULE_EXTENSION: String = ".js"

    /** The consumer's TypeScript declaration, inlined from the runtime SDK's own exports. */
    public const val DECLARATION_EXTENSION: String = ".d.ts"

    /** The compiler's external source map. */
    public const val SOURCE_MAP_EXTENSION: String = ".js.map"

    /** The npm manifest the Kotlin Gradle Plugin publishes beside the module. */
    public const val PACKAGE_MANIFEST: String = "package.json"

    /**
     * Host markup. A Mini App bundle must never carry it, whichever side of the boundary produced it:
     * WXML and WXSS belong to the host project, and the SDK does not render.
     */
    public val hostMarkupExtensions: List<String> = listOf(".wxml", ".wxss", ".axml", ".acss")

    /**
     * Filename fragments that only a client renderer produces.
     *
     * This is a secondary signal, not the rule: `checkMiniAppHostBoundary` reads the dependency
     * graph, which is where the rule belongs. It is re-read here because a distribution is the last
     * place a renderer could appear without anybody noticing.
     */
    public val rendererNameFragments: List<String> = listOf("compose", "skiko", "kotlinx-browser")

    private val REQUIRE_PATTERN: Regex = Regex("""require\s*\(\s*(['"])([^'"]*)\1\s*\)""")

    private val SOURCE_MAPPING_PATTERN: Regex = Regex("""//[#@]\s*sourceMappingURL=(\S+)""")

    /** Every `require('…')` target in [javascriptText], in the order the compiler emitted them. */
    public fun requires(javascriptText: String): List<String> =
        REQUIRE_PATTERN.findAll(javascriptText).map { match -> match.groupValues[2] }.toList()

    /** The file a `//# sourceMappingURL=` comment names, or `null` when there is none. */
    public fun sourceMappingUrl(javascriptText: String): String? =
        SOURCE_MAPPING_PATTERN.find(javascriptText)?.groupValues?.get(1)

    /** True when the path's last segment is one the distribution must never contain. */
    public fun isExcludedFileName(name: String): Boolean =
        name == "project.private.config.json" || name == ".DS_Store" || name.endsWith(".iml")

    /** Directory names the distribution must never contain, at any depth. */
    public val excludedDirectoryNames: List<String> = listOf(
        "node_modules",
        ".git",
        ".gradle",
        ".kotlin",
        ".idea",
    )
}
