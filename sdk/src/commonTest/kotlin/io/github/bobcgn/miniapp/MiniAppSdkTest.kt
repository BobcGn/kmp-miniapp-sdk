package io.github.bobcgn.miniapp

import io.github.bobcgn.miniapp.api.GeneratedMiniAppSdkVersion
import io.github.bobcgn.miniapp.api.MiniAppSdk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

public class MiniAppSdkTest {
    @Test
    public fun versionComesFromTheGeneratedConstant(): Unit {
        // `gradle/libs.versions.toml` is the only place the number is written, so asserting the
        // public constant against a literal here would just add a second copy that can go stale.
        assertEquals(GeneratedMiniAppSdkVersion.VALUE, MiniAppSdk.VERSION)
        assertTrue(MiniAppSdk.VERSION.isNotBlank(), "the generated version must not be blank")
    }
}
