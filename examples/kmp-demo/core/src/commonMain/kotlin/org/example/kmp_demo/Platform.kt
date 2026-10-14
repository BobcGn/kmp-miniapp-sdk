package org.example.kmp_demo

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform