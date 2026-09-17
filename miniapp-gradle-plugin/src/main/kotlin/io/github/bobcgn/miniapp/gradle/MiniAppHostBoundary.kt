package io.github.bobcgn.miniapp.gradle

/**
 * What a Mini App host bundle must not carry.
 *
 * ADR 0011 fixes the boundary this class enforces: Compose is a client UI consumer, Skiko is its
 * renderer, and a Mini App host renders with its own native UI — WXML and WXSS for WeChat. A Mini
 * App compilation that inherits a source set containing Compose UI therefore produces a bundle full
 * of a renderer the host cannot run, and it does so without any error: the build succeeds and the
 * artifact is simply unusable.
 *
 * This is a *dependency* rule, deliberately not a file rule. Files in the Kotlin/JS distribution are
 * reached through `require()` from one another, so deleting the ones that look like a renderer would
 * leave a bundle that fails at load instead of one that fails at build.
 */
internal object MiniAppHostBoundary {

    /** Groups that exist only to serve a client-side renderer or UI framework. */
    public val forbiddenGroupPrefixes: List<String> = listOf(
        "org.jetbrains.compose",
        "androidx.compose",
        "org.jetbrains.skiko",
    )

    /**
     * Exact modules that carry a browser or markup runtime a Mini App host does not have.
     *
     * Matched by module coordinate rather than by group because their groups also hold dependencies a
     * Mini App legitimately uses: `org.jetbrains.kotlinx` carries `kotlinx-coroutines-core`.
     */
    public val forbiddenModuleCoordinates: List<String> = listOf(
        "org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose",
        "org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose",
        "org.jetbrains.androidx.savedstate:savedstate-compose",
        "org.jetbrains.androidx.navigationevent:navigationevent-compose",
        "org.jetbrains.kotlinx:kotlinx-browser",
        "org.jetbrains.kotlinx:kotlinx-html",
    )

    /**
     * True when a resolved module belongs to a client renderer rather than to a Mini App host.
     */
    public fun isForbiddenModule(group: String, name: String): Boolean {
        if (forbiddenGroupPrefixes.any { group == it || group.startsWith("$it.") }) return true
        val coordinate = "$group:$name"
        return forbiddenModuleCoordinates.any { coordinate == it || coordinate.startsWith("$it-") }
    }

    public fun describeOffenders(offenders: List<String>): String = buildString {
        appendLine("The Mini App runtime classpath carries dependencies that belong to a client renderer,")
        appendLine("not to a Mini App host:")
        offenders.forEach { appendLine("  - $it") }
        appendLine()
        appendLine("Compose is a client UI consumer, and Skiko is its renderer. A Mini App host renders")
        appendLine("with its own native UI — WXML and WXSS for WeChat — so a Mini App bundle must not")
        appendLine("carry Compose, Skiko or a browser UI runtime. The bundle would build successfully and")
        appendLine("still be unusable in the host.")
        appendLine()
        appendLine("This usually means a Mini App source set inherits a common source set that holds")
        appendLine("Compose UI. Keep the Mini App source set to shared behaviour, host integration and")
        appendLine("binding, and move the Compose UI into a client module or source set that is not a")
        appendLine("parent of 'miniappMain'.")
    }
}
