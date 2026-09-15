package io.github.bobcgn.miniapp.host.wechat.interop

import kotlin.js.jsTypeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class WxOptionsTest {
    @Test
    fun loginFactoryPreservesUndefinedDefaultsAndTypedCallbacks() {
        val options = wxLoginOptions()

        assertEquals("undefined", jsTypeOf(options.timeout))
        assertEquals("undefined", jsTypeOf(options.success))
        assertEquals("undefined", jsTypeOf(options.fail))

        val timedOptions = wxLoginOptions(timeoutMillis = 5_000)
        timedOptions.success = { result -> result.code.length + result.errMsg.length }
        timedOptions.fail = { result -> result.errno ?: result.errMsg.length }

        assertEquals(5_000, timedOptions.timeout)
        assertNotNull(timedOptions.success)
        assertNotNull(timedOptions.fail)
    }

    @Test
    fun showToastFactoryCreatesPlainOptionsWithOnlyRequiredField() {
        val options = wxShowToastOptions(title = "Ready")

        assertEquals("Ready", options.title)
        // Absence matters here: assigning null would change the raw JS option bag.
        assertEquals("undefined", jsTypeOf(options.icon))
        assertEquals("undefined", jsTypeOf(options.success))
    }

    @Test
    fun showToastOptionsAcceptTypedOptionalFieldsAndCallbacks() {
        val options = wxShowToastOptions(title = "Saved")
        options.icon = "success"
        options.duration = 1_500
        options.mask = true
        options.success = { result -> result.errMsg.length }

        assertEquals("success", options.icon)
        assertEquals(1_500, options.duration)
        assertEquals(true, options.mask)
        assertNotNull(options.success)
    }

    @Test
    fun storageFactoriesPreserveRequiredFields() {
        val getOptions = wxGetStorageOptions(key = "session")
        val setOptions = wxSetStorageOptions(key = "session", data = "value")
        val removeOptions = wxRemoveStorageOptions(key = "session")

        assertEquals("session", getOptions.key)
        assertEquals("session", setOptions.key)
        assertEquals("value", setOptions.data)
        assertEquals("session", removeOptions.key)
    }

    @Test
    fun getStorageSuccessUsesTypedResultContract() {
        val options = wxGetStorageOptions(key = "session")
        options.success = { result ->
            result.data
            result.errMsg
        }

        assertNotNull(options.success)
    }

    @Test
    fun requestFactoryAlwaysRequestsRawTextAndLeavesOptionalsAbsent() {
        val options = wxRequestOptions(url = "https://example.com/ping", method = "GET")

        assertEquals("https://example.com/ping", options.url)
        assertEquals("GET", options.method)
        // The transport contract carries an encoded string, so WeChat must not parse JSON.
        assertEquals("text", options.dataType)
        assertEquals("undefined", jsTypeOf(options.data))
        assertEquals("undefined", jsTypeOf(options.timeout))
        assertEquals("undefined", jsTypeOf(options.header))
        assertEquals("undefined", jsTypeOf(options.success))

        val full = wxRequestOptions(
            url = "https://example.com/submit",
            method = "POST",
            data = "{\"hello\":\"world\"}",
            timeoutMillis = 3_000,
        )

        assertEquals("{\"hello\":\"world\"}", full.data)
        assertEquals(3_000, full.timeout)
    }

    @Test
    fun requestHeaderObjectIsAJavaScriptObjectOfStringValues() {
        val header = wxRequestHeader(
            mapOf("Content-Type" to "application/json", "X-Token" to "abc"),
        )

        assertEquals("application/json", js("header['Content-Type']"))
        assertEquals("abc", js("header['X-Token']"))
        assertEquals("object", jsTypeOf(header))
    }

    @Test
    fun responseHeadersKeepOnlyValuesTheStringContractCanCarry() {
        val raw: Any = js(
            "({ 'Content-Type': 'application/json', 'Set-Cookie': ['a=1'], 'X-Count': 3 })",
        )

        val headers = wxResponseHeaders(raw)

        assertEquals("application/json", headers["Content-Type"])
        assertNull(headers["Set-Cookie"])
        assertNull(headers["X-Count"])
        assertEquals(1, headers.size)
    }

    @Test
    fun responseHeadersWithoutAHostObjectAreEmpty() {
        assertEquals(emptyMap<String, String>(), wxResponseHeaders(null))
    }

    @Test
    fun navigationFactoriesLeaveOptionalsAbsent() {
        val navigateTo = wxNavigateToOptions(url = "pages/second/index")
        assertEquals("pages/second/index", navigateTo.url)
        assertEquals("undefined", jsTypeOf(navigateTo.success))
        assertEquals("undefined", jsTypeOf(navigateTo.fail))
        assertEquals("undefined", jsTypeOf(navigateTo.complete))

        val redirectTo = wxRedirectToOptions(url = "pages/index/index")
        assertEquals("pages/index/index", redirectTo.url)
        assertEquals("undefined", jsTypeOf(redirectTo.success))

        // Leaving delta absent keeps WeChat's own default of one page.
        val defaultedBack = wxNavigateBackOptions()
        assertEquals("undefined", jsTypeOf(defaultedBack.delta))

        val explicitBack = wxNavigateBackOptions(delta = 2)
        assertEquals(2, explicitBack.delta)
    }

    @Test
    fun navigationOptionsAcceptTypedCallbacks() {
        val options = wxNavigateToOptions(url = "pages/second/index")
        options.success = { result -> result.errMsg.length }
        options.fail = { result -> result.errMsg.length }

        assertNotNull(options.success)
        assertNotNull(options.fail)
    }
}
