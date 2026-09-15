package io.github.bobcgn.miniapp.host.wechat.runtime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * WeChat page-level lifecycle behavior.
 *
 * Page lifecycle is deliberately WeChat-specific, so these tests state the
 * behavior of WeChat's own hooks rather than a platform-neutral contract.
 */
internal class WechatPageLifecycleTest {
    @Test
    fun theRouteComesFromThePageThatReportsItselfShown() {
        val page = WechatPageLifecycle()
        assertNull(page.currentRoute)
        assertFalse(page.isVisible)

        page.pageShown("pages/index/index")

        assertEquals("pages/index/index", page.currentRoute)
        assertTrue(page.isVisible)
    }

    @Test
    fun hidingKeepsTheRouteBecauseThePageStaysOnTheStack() {
        val page = WechatPageLifecycle()
        page.pageShown("pages/index/index")

        page.pageHidden()

        assertFalse(page.isVisible)
        assertEquals("pages/index/index", page.currentRoute)
    }

    @Test
    fun unloadingTheCurrentPageClearsTheRoute() {
        val page = WechatPageLifecycle()
        page.pageShown("pages/second/index")

        page.pageUnloaded("pages/second/index")

        assertNull(page.currentRoute)
        assertFalse(page.isVisible)
    }

    @Test
    fun unloadingAnotherPageLeavesTheCurrentRouteAlone() {
        val page = WechatPageLifecycle()
        page.pageShown("pages/third/index")

        // An earlier page can unload while a deeper page is the current one.
        page.pageUnloaded("pages/second/index")

        assertEquals("pages/third/index", page.currentRoute)
        assertTrue(page.isVisible)
    }

    @Test
    fun returningFromADeeperPageRestoresTheRevealedRoute() {
        val page = WechatPageLifecycle()
        page.pageShown("pages/index/index")
        page.pageShown("pages/second/index")

        // Going back unloads the page being left and then shows the revealed one.
        page.pageUnloaded("pages/second/index")
        assertNull(page.currentRoute)

        page.pageShown("pages/index/index")

        assertEquals("pages/index/index", page.currentRoute)
        assertTrue(page.isVisible)
    }
}
