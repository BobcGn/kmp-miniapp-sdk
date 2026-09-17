// Model C — the Mini App plugin owns the Kotlin/JS compilation; the consumer writes no Kotlin/JS
// DSL at all. The only Kotlin block here would be business/metadata configuration.
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("io.github.bobcgn.miniapp.poc")
}
