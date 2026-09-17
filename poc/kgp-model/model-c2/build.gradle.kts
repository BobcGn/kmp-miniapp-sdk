// Model C variant — the plugin keeps the internal Kotlin/JS target named 'js' and tries to expose
// 'miniappMain' / 'miniappTest' as if they were the consumer's source sets.
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("io.github.bobcgn.miniapp.poc.hidden")
}
