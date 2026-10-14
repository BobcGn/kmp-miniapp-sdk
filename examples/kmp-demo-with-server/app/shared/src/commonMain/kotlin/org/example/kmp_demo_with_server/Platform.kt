package org.example.kmp_demo_with_server

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform