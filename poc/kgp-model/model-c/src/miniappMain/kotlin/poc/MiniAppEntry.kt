package poc

// Only compiles if this source set is consumed by the Kotlin/JS compilation, and only links if it
// can also see `commonGreeting` from commonMain.
fun miniAppEntry(): String = "miniapp:" + commonGreeting()

// Uses the Kotlin/JS-only `js()` construct, so this file can only be compiled by the JS compiler.
fun jsRuntimeMarker(): String = js("'kotlin-js'") as String
