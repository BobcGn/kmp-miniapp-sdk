package io.github.bobcgn.miniapp.host.wechat

/**
 * Coordinate system a WeChat position is reported in.
 *
 * WeChat is not free to answer in one system: `wgs84` is the raw satellite fix,
 * while `gcj02` is the offset system that Chinese map data uses. They are not
 * interchangeable, and a coordinate used in the wrong one lands hundreds of
 * metres away, so the SDK asks for one explicitly rather than accepting whichever
 * the host happens to default to.
 *
 * The distinction is specific to this host and its mapping regulation; no
 * portable location concept carries it, which is part of why location is modelled
 * as a WeChat capability rather than a host-neutral one.
 */
internal enum class WeChatCoordinateSystem(
    /** The value [io.github.bobcgn.miniapp.host.wechat.interop.wx] expects. */
    internal val hostValue: String,
) {
    /** Raw satellite coordinates, as a GPS receiver reports them. */
    WGS84("wgs84"),

    /**
     * Coordinates offset for Chinese map data, which is what WeChat's own map
     * views and `wx.openLocation` accept.
     */
    GCJ02("gcj02"),
}

/**
 * One position the host reported.
 *
 * Only the fields WeChat documents as always present are modelled. The host also
 * reports altitude, vertical and horizontal accuracy, and speed, but those are
 * deliberately left out: Android reports `0` for vertical accuracy when it cannot
 * obtain one, which is indistinguishable from a real zero, and the SDK has no
 * consumer that needs them. Adding them without that care would hand a caller a
 * number it cannot trust.
 *
 * The coordinates are in the system the request asked for, not in a fixed one.
 *
 * @property latitude degrees, in `-90.0..90.0`, negative is south
 * @property longitude degrees, in `-180.0..180.0`, negative is west
 * @property accuracyMeters radius of the horizontal uncertainty, never negative
 */
internal class WeChatGeoPosition(
    internal val latitude: Double,
    internal val longitude: Double,
    internal val accuracyMeters: Double,
    internal val coordinateSystem: WeChatCoordinateSystem,
)
