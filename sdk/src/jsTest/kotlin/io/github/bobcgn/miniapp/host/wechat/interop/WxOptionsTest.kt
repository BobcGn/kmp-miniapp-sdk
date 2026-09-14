package io.github.bobcgn.miniapp.host.wechat.interop

import kotlin.js.jsTypeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class WxOptionsTest {
    @Test
    fun showToastFactoryCreatesPlainOptionsWithOnlyRequiredField() {
        val options = wxShowToastOptions(title = "Ready")

        assertEquals("Ready", options.title)
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
}
