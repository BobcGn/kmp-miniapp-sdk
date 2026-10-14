@file:OptIn(kotlin.js.ExperimentalJsExport::class)

package org.example.kmp_demo

import kotlin.js.JsExport

/** Returns the same shared greeting used by Android and iOS, with the current WeChat environment. */
@JsExport
public fun platformGreeting(): String = Greeting().greet()
