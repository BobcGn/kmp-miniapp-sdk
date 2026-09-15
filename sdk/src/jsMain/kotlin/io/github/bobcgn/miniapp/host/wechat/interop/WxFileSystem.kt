package io.github.bobcgn.miniapp.host.wechat.interop

import kotlin.js.jsTypeOf

/** Raw callback-style options accepted by `FileSystemManager.readFile`. */
internal external interface WxReadFileOptions {
    /** Path of the file to read, inside the mini program file sandbox. */
    var filePath: String

    /**
     * Character encoding of the result.
     *
     * The SDK always sets this, because a `readFile` without it returns binary
     * content the text contract cannot carry.
     */
    var encoding: String?

    /** Called only when the host read the file. */
    var success: WxReadFileSuccessCallback?

    /** Called when the file cannot be read. */
    var fail: WxGeneralCallback?

    /** Called after either [success] or [fail]. */
    var complete: WxGeneralCallback?
}

/**
 * Raw success result returned by `FileSystemManager.readFile`.
 *
 * The documented type is `string | ArrayBuffer`, so it is declared [Any] here and
 * read only through [wxFileText].
 */
internal external interface WxReadFileSuccessResult : WxGeneralCallbackResult {
    /** Documented type is `string | ArrayBuffer`: the file contents. */
    val data: Any?
}

/** Raw callback-style options accepted by `FileSystemManager.writeFile`. */
internal external interface WxWriteFileOptions {
    /** Path of the file to write, inside the mini program file sandbox. */
    var filePath: String

    /** Text to write. */
    var data: String

    /** Character encoding of [data]. The SDK always sets it. */
    var encoding: String?

    /** Called only when the host wrote the file. */
    var success: WxGeneralCallback?

    /** Called when the file cannot be written. */
    var fail: WxGeneralCallback?

    /** Called after either [success] or [fail]. */
    var complete: WxGeneralCallback?
}

/** Raw callback-style options accepted by `FileSystemManager.access`. */
internal external interface WxAccessOptions {
    /** Path to test, inside the mini program file sandbox. */
    var path: String

    /** Called only when the path exists and is accessible. */
    var success: WxGeneralCallback?

    /** Called when the path does not exist or cannot be accessed. */
    var fail: WxGeneralCallback?

    /** Called after either [success] or [fail]. */
    var complete: WxGeneralCallback?
}

/** Raw callback-style options accepted by `FileSystemManager.unlink`. */
internal external interface WxUnlinkOptions {
    /** Path of the file to remove, inside the mini program file sandbox. */
    var filePath: String

    /** Called only when the host removed the file. */
    var success: WxGeneralCallback?

    /** Called when the file cannot be removed. */
    var fail: WxGeneralCallback?

    /** Called after either [success] or [fail]. */
    var complete: WxGeneralCallback?
}

/**
 * The subset of `FileSystemManager` this SDK uses.
 *
 * Every member is optional because the manager is a host object: a base library
 * may expose the manager without exposing one of its methods.
 */
internal external interface WxFileSystemManager {
    var readFile: ((WxReadFileOptions) -> Unit)?

    var writeFile: ((WxWriteFileOptions) -> Unit)?

    var access: ((WxAccessOptions) -> Unit)?

    var unlink: ((WxUnlinkOptions) -> Unit)?
}

/** Success callback accepted by [WxReadFileOptions]. */
internal typealias WxReadFileSuccessCallback = (WxReadFileSuccessResult) -> Unit

/**
 * What a raw `readFile` answer carries.
 *
 * [Unreadable] is deliberately separate from an empty file: an empty file is a
 * value the contract carries, while binary content or a missing field is not.
 */
internal sealed interface WxFileText {
    /** The host returned text, which may be empty. */
    data class Present(val text: String) : WxFileText

    /** The host's answer is not a value this contract can carry. */
    data object Unreadable : WxFileText
}

/**
 * Reads a raw `readFile` answer.
 *
 * This is the only place the raw result is inspected. A file read without an
 * encoding comes back as an `ArrayBuffer`, which this reports as unreadable
 * rather than stringifying, which would produce bytes rendered as text.
 */
internal fun wxFileText(result: WxReadFileSuccessResult): WxFileText {
    val data = result.data
    return when {
        data == null || jsTypeOf(data) == "undefined" -> WxFileText.Unreadable
        data is String -> WxFileText.Present(data)
        else -> WxFileText.Unreadable
    }
}

/** The UTF-8 encoding name the SDK sends for both reading and writing. */
internal const val WX_UTF8_ENCODING: String = "utf8"

/** Whether `wx.getFileSystemManager` exists. */
internal fun hasWxGetFileSystemManager(): Boolean =
    js("typeof wx !== 'undefined' && typeof wx.getFileSystemManager === 'function'")

/**
 * Whether the host's file manager exposes [method].
 *
 * The manager is a host object, so the API existing does not mean each of its
 * methods does; each one is probed on its own.
 */
internal fun hasWxFileSystemMethod(method: String): Boolean =
    js(
        "typeof wx !== 'undefined' && typeof wx.getFileSystemManager === 'function' " +
            "&& typeof wx.getFileSystemManager()[method] === 'function'",
    )

/**
 * Whether `wx.env.USER_DATA_PATH` is a usable string.
 *
 * This is the only sandbox root the SDK knows about, and it is read rather than
 * assumed, so a host without it reports "unavailable" instead of producing paths
 * against `undefined`.
 */
internal fun hasWxUserDataPath(): Boolean =
    js(
        "typeof wx !== 'undefined' && typeof wx.env === 'object' && wx.env !== null " +
            "&& typeof wx.env.USER_DATA_PATH === 'string'",
    )

/**
 * Creates a plain JavaScript option bag for `FileSystemManager.readFile`.
 *
 * The encoding is always set, so the host returns text rather than an
 * `ArrayBuffer` the contract cannot carry.
 *
 * @param filePath path inside the mini program file sandbox
 */
internal fun wxReadFileOptions(filePath: String): WxReadFileOptions {
    val options: WxReadFileOptions = js("({})")
    options.filePath = filePath
    options.encoding = WX_UTF8_ENCODING
    return options
}

/**
 * Creates a plain JavaScript option bag for `FileSystemManager.writeFile`.
 *
 * @param filePath path inside the mini program file sandbox
 * @param data text to write
 */
internal fun wxWriteFileOptions(filePath: String, data: String): WxWriteFileOptions {
    val options: WxWriteFileOptions = js("({})")
    options.filePath = filePath
    options.data = data
    options.encoding = WX_UTF8_ENCODING
    return options
}

/**
 * Creates a plain JavaScript option bag for `FileSystemManager.access`.
 *
 * @param path path inside the mini program file sandbox
 */
internal fun wxAccessOptions(path: String): WxAccessOptions {
    val options: WxAccessOptions = js("({})")
    options.path = path
    return options
}

/**
 * Creates a plain JavaScript option bag for `FileSystemManager.unlink`.
 *
 * @param filePath path inside the mini program file sandbox
 */
internal fun wxUnlinkOptions(filePath: String): WxUnlinkOptions {
    val options: WxUnlinkOptions = js("({})")
    options.filePath = filePath
    return options
}
