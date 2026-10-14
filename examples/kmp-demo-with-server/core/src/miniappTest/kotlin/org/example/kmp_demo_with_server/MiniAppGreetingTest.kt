package org.example.kmp_demo_with_server

import kotlin.test.Test
import kotlin.test.assertEquals

class MiniAppGreetingTest {
    @Test
    fun greetingUsesHostPlatformAndBaseLibraryVersion() {
        assertEquals("Hello, WeChat · devtools · 基础库 3.17.4!", wechatGreeting("devtools", "3.17.4"))
        assertEquals("Hello, WeChat · android · 基础库 3.18.0!", wechatGreeting("android", "3.18.0"))
    }

    @Test
    fun missingHostInfoIsShownAsUnknownRatherThanAnSdkVersion() {
        assertEquals("Hello, WeChat · 未知 · 基础库 未知!", wechatGreeting(null, null))
        assertEquals("Hello, WeChat · 未知 · 基础库 未知!", wechatGreeting(" ", ""))
    }
}
