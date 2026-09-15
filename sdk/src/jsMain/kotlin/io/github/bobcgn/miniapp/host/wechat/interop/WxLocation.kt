package io.github.bobcgn.miniapp.host.wechat.interop

import kotlin.js.jsTypeOf

/** Raw callback-style options accepted by [wx.getLocation]. */
internal external interface WxGetLocationOptions {
    /**
     * Coordinate system the host should answer in: `wgs84` or `gcj02`.
     *
     * The SDK always sets it, because the two systems are not interchangeable
     * and silently taking the host default would hand a caller coordinates it
     * cannot use with WeChat's own map views.
     */
    var type: String?

    /** Called only when the host obtained a position. */
    var success: WxGetLocationSuccessCallback?

    /** Called when the host cannot obtain a position. */
    var fail: WxGeneralCallback?

    /** Called after either [success] or [fail]. */
    var complete: WxGeneralCallback?
}

/**
 * Raw success result returned by [wx.getLocation].
 *
 * The documented fields are numbers, but they are declared [Any] here for the
 * same reason WeChat storage declares its value that way: an external property
 * the host did not set reads as `undefined`, and a value of the wrong type would
 * otherwise be accepted silently. [wxLocationSample] is the only reader.
 */
internal external interface WxGetLocationSuccessResult : WxGeneralCallbackResult {
    /** Documented type is `number`, in degrees. */
    val latitude: Any?

    /** Documented type is `number`, in degrees. */
    val longitude: Any?

    /** Documented type is `number`, in metres. */
    val accuracy: Any?
}

/** Success callback accepted by [WxGetLocationOptions]. */
internal typealias WxGetLocationSuccessCallback = (WxGetLocationSuccessResult) -> Unit

/**
 * What a raw `getLocation` answer carries.
 *
 * Only the three fields the SDK models are read. A raw JavaScript object cannot
 * cross out of this package, so this is the typed shape the adapter consumes.
 */
internal sealed interface WxLocationSample {
    /** The host returned a position with the fields the contract requires. */
    data class Present(
        val latitude: Double,
        val longitude: Double,
        val accuracyMeters: Double,
    ) : WxLocationSample

    /**
     * The host did not answer with a usable position.
     *
     * This covers a field that is absent or not a number, and a number that is
     * not finite or lies outside the range the coordinate system defines.
     */
    data object Unreadable : WxLocationSample
}

/** The coordinate-system names WeChat accepts for `getLocation`. */
internal const val WX_COORDINATE_WGS84: String = "wgs84"
internal const val WX_COORDINATE_GCJ02: String = "gcj02"

/** Whether [wx.getLocation] exists. */
internal fun hasWxGetLocation(): Boolean =
    js("typeof wx !== 'undefined' && typeof wx.getLocation === 'function'")

/**
 * Reads a raw [wx.getLocation] answer.
 *
 * This is the only place the raw result is inspected, so a missing field, a
 * non-number, a non-finite value, or a coordinate outside its valid range all
 * become [WxLocationSample.Unreadable] instead of being defaulted to zero. Zero
 * would be a real, and wrong, position off the coast of Africa.
 */
internal fun wxLocationSample(result: WxGetLocationSuccessResult): WxLocationSample {
    val latitude = finiteDoubleOrNull(result.latitude) ?: return WxLocationSample.Unreadable
    val longitude = finiteDoubleOrNull(result.longitude) ?: return WxLocationSample.Unreadable
    val accuracy = finiteDoubleOrNull(result.accuracy) ?: return WxLocationSample.Unreadable

    if (latitude !in MIN_LATITUDE..MAX_LATITUDE) return WxLocationSample.Unreadable
    if (longitude !in MIN_LONGITUDE..MAX_LONGITUDE) return WxLocationSample.Unreadable
    // A negative accuracy is not a distance; the host has reported something the
    // contract cannot carry.
    if (accuracy < 0.0) return WxLocationSample.Unreadable

    return WxLocationSample.Present(
        latitude = latitude,
        longitude = longitude,
        accuracyMeters = accuracy,
    )
}

/**
 * Reads one numeric field, rejecting anything that is not a finite number.
 *
 * `NaN` and the infinities are numbers in JavaScript, so a type check alone would
 * let them through and produce a coordinate that compares as neither valid nor
 * invalid.
 */
private fun finiteDoubleOrNull(value: Any?): Double? {
    if (value == null || jsTypeOf(value) == "undefined") return null
    if (value !is Number) return null

    val number = value.toDouble()
    if (number.isNaN() || number.isInfinite()) return null
    return number
}

private const val MIN_LATITUDE: Double = -90.0
private const val MAX_LATITUDE: Double = 90.0
private const val MIN_LONGITUDE: Double = -180.0
private const val MAX_LONGITUDE: Double = 180.0

/**
 * Creates a plain JavaScript option bag for [wx.getLocation].
 *
 * The coordinate system is always set, so the caller never has to guess which one
 * the host used.
 *
 * @param coordinateSystem `wgs84` or `gcj02`
 */
internal fun wxGetLocationOptions(coordinateSystem: String): WxGetLocationOptions {
    val options: WxGetLocationOptions = js("({})")
    options.type = coordinateSystem
    return options
}
