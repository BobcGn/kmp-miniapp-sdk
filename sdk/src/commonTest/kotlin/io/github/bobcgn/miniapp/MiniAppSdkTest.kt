package io.github.bobcgn.miniapp

import io.github.bobcgn.miniapp.api.MiniAppSdk
import kotlin.test.Test
import kotlin.test.assertEquals

public class MiniAppSdkTest {
    @Test
    public fun versionMatchesProjectVersion(): Unit {
        assertEquals("0.1.0-SNAPSHOT", MiniAppSdk.VERSION)
    }
}
