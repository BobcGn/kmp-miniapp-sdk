package org.example.kmp_demo

import kotlin.test.Test
import kotlin.test.assertEquals

class SharedCommonTest {
    @Test
    fun greetingPreservesEachPlatformsIdentityAndVersion() {
        for (platform in listOf("Android 37", "iOS 19.0", "WeChat · devtools · 基础库 3.17.3")) {
            assertEquals("Hello, $platform!", sayHello(platform))
        }
    }
}
