package io.github.bobcgn.miniapp.host.wechat.interop

import io.github.bobcgn.miniapp.host.wechat.testing.fakeReadFileSuccess
import kotlin.js.jsTypeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class WxFileSystemTest {
    @Test
    fun readFileOptionsAlwaysAskForText() {
        val options = wxReadFileOptions("/sandbox/note.txt")

        assertEquals("/sandbox/note.txt", options.filePath)
        // Without an encoding the host returns an ArrayBuffer, which the text
        // contract cannot carry, so the SDK always sets one.
        assertEquals("utf8", options.encoding)
        assertEquals("undefined", jsTypeOf(options.success))
        assertEquals("undefined", jsTypeOf(options.fail))
    }

    @Test
    fun writeFileOptionsCarryTheTextAndEncoding() {
        val options = wxWriteFileOptions("/sandbox/note.txt", "hello")

        assertEquals("/sandbox/note.txt", options.filePath)
        assertEquals("hello", options.data)
        assertEquals("utf8", options.encoding)
        assertEquals("undefined", jsTypeOf(options.success))
    }

    @Test
    fun accessOptionsCarryThePath() {
        val options = wxAccessOptions("/sandbox/note.txt")

        assertEquals("/sandbox/note.txt", options.path)
        assertEquals("undefined", jsTypeOf(options.success))
        assertEquals("undefined", jsTypeOf(options.fail))
    }

    @Test
    fun unlinkOptionsCarryThePath() {
        val options = wxUnlinkOptions("/sandbox/note.txt")

        assertEquals("/sandbox/note.txt", options.filePath)
        assertEquals("undefined", jsTypeOf(options.complete))

        options.success = { result -> result.errMsg.length }
        assertNotNull(options.success)
    }

    @Test
    fun thePresenceGuardsAnswerFalseWithoutAWxGlobal() {
        // The Node test environment has no wx, so every guard must answer rather
        // than throw on a missing global or a missing manager.
        assertFalse(hasWxGetFileSystemManager())
        assertFalse(hasWxFileSystemMethod("readFile"))
        assertFalse(hasWxFileSystemMethod("writeFile"))
        assertFalse(hasWxFileSystemMethod("access"))
        assertFalse(hasWxFileSystemMethod("unlink"))
        assertFalse(hasWxUserDataPath())
    }

    @Test
    fun aTextAnswerIsRead() {
        val result = fakeReadFileSuccess("file contents")

        assertEquals(WxFileText.Present("file contents"), wxFileText(result))
    }

    @Test
    fun anEmptyFileIsAValueRatherThanAnUnreadableAnswer() {
        val result = fakeReadFileSuccess("")

        // An empty file and binary content are different things; only the second
        // is unreadable.
        assertEquals(WxFileText.Present(""), wxFileText(result))
    }

    @Test
    fun aMissingAnswerIsUnreadable() {
        val result = fakeReadFileSuccess(null)

        assertEquals(WxFileText.Unreadable, wxFileText(result))
    }

    @Test
    fun binaryContentIsUnreadableRatherThanStringified() {
        // A read without an encoding comes back as an ArrayBuffer. Rendering it as
        // text would invent content the file does not hold.
        val bytes: Any = js("(new Uint8Array([104, 105]).buffer)")

        assertEquals(WxFileText.Unreadable, wxFileText(fakeReadFileSuccess(bytes)))
    }
}
