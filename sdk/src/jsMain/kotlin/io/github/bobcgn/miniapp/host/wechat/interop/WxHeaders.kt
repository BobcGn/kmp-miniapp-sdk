package io.github.bobcgn.miniapp.host.wechat.interop

/**
 * Builds the plain JavaScript header object accepted by [wx.request].
 *
 * This exists because JavaScript objects cannot be produced from platform-neutral
 * Kotlin code. It performs representation only: no header policy, no validation,
 * and no business rules.
 *
 * @param headers header names and values to represent
 */
internal fun wxRequestHeader(headers: Map<String, String>): Any? {
    val header: Any = js("({})")
    for ((name, value) in headers) {
        js("header[name] = value")
    }
    return header
}

/**
 * Builds a plain JavaScript object of string values.
 *
 * This is the same representation [wxRequestHeader] produces, exposed for the other
 * host fields that take a string map — an upload's form fields, for instance. It
 * performs representation only.
 *
 * @param entries names and values to represent
 */
internal fun wxStringMapObject(entries: Map<String, String>): Any {
    val target: Any = js("({})")
    for ((name, value) in entries) {
        js("target[name] = value")
    }
    return target
}

/**
 * Reads a raw host header object into platform-neutral Kotlin strings.
 *
 * WeChat reports some headers, such as `Set-Cookie`, as JavaScript arrays rather
 * than strings. Only single string values can be represented by the transport
 * contract; every other value shape is skipped rather than stringified.
 *
 * @param raw host header object, or `null` when the host supplied none
 */
internal fun wxResponseHeaders(raw: Any?): Map<String, String> {
    if (raw == null) return emptyMap()
    val headers = mutableMapOf<String, String>()
    val names: Array<String> = js("Object.keys(raw)")
    for (name in names) {
        val value: Any? = js("raw[name]")
        if (value is String) {
            headers[name] = value
        }
    }
    return headers
}
