package io.github.bobcgn.miniapp.host.wechat.interop

import kotlin.test.Test
import kotlin.test.assertFalse

/**
 * The runtime-inspection guards must be safe to evaluate with no WeChat global at
 * all, because the Node test environment and the CommonJS smoke test both load
 * the SDK outside WeChat.
 *
 * The Kotlin/JS Node runtime has no `wx`, so every guard is expected to answer
 * `false` here instead of throwing on an absent member.
 */
internal class WxRuntimeInfoTest {
    @Test
    fun presenceGuardsAnswerFalseWithoutAWxGlobal() {
        assertFalse(hasWxCanIUse())
        assertFalse(hasWxGetAppBaseInfo())
        assertFalse(hasWxGetSystemInfoSync())
        assertFalse(hasWxGetDeviceInfo())
    }
}
