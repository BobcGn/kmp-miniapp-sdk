package org.example.kmp_demo

import kotlin.test.Test
import kotlin.test.assertEquals

class MiniAppPlatformTest {
    @Test
    fun greetingUsesReportedPlatformAndBaseLibraryVersion() {
        assertEquals("Hello, WeChat · devtools · 基础库 3.17.3!", sayHello(MiniAppPlatform("devtools", "3.17.3").name))
        assertEquals("Hello, WeChat · android · 基础库 3.18.0!", sayHello(MiniAppPlatform("android", "3.18.0").name))
    }

    @Test
    fun missingHostInformationIsNotReplacedByASdkVersion() {
        assertEquals("Hello, WeChat · 未知 · 基础库 未知!", sayHello(MiniAppPlatform(null, null).name))
        assertEquals("Hello, WeChat · 未知 · 基础库 未知!", sayHello(MiniAppPlatform(" ", "").name))
    }
}
