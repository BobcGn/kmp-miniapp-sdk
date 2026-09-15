@file:OptIn(ExperimentalJsExport::class)

package io.github.bobcgn.miniapp.export

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * JavaScript-facing view of one position WeChat reported.
 *
 * The coordinates are in the system the request asked for, named by
 * [coordinateSystem] so a caller never has to guess which one it holds. The two
 * systems are not interchangeable: a coordinate used in the wrong one lands
 * hundreds of metres away.
 *
 * Only the fields WeChat documents as always present are exposed. Altitude,
 * vertical and horizontal accuracy, and speed are deliberately left out; see the
 * capability matrix for why.
 */
@JsExport
public class JsGeoPosition internal constructor(
    /** Degrees, in `-90.0..90.0`; negative is south. */
    public val latitude: Double,
    /** Degrees, in `-180.0..180.0`; negative is west. */
    public val longitude: Double,
    /** Radius of the horizontal uncertainty in metres; never negative. */
    public val accuracyMeters: Double,
    /** `wgs84` or `gcj02`: the system the coordinates are in. */
    public val coordinateSystem: String,
)
