plugins {
    kotlin("multiplatform") version "2.4.20"
    id("io.github.bobcgn.miniapp")
}

// One producer, explicit opt-in. Kotlin sources and the bundle remain owned by this module.
miniapp {
    wechat {
        rootHost.set(true)
    }
}
