// One explicit producer; the root coordinates and does not compile Kotlin itself.
plugins {
    kotlin("multiplatform") version "2.4.20" apply false
    id("io.github.bobcgn.miniapp")
}

miniapp {
    logicProject.set(project(":shared"))
}
