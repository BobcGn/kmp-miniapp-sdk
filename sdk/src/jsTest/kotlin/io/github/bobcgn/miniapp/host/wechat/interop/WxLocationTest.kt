package io.github.bobcgn.miniapp.host.wechat.interop

import io.github.bobcgn.miniapp.host.wechat.testing.fakeGetLocationSuccess
import kotlin.js.jsTypeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class WxLocationTest {
    @Test
    fun theOptionsAlwaysNameACoordinateSystem() {
        val options = wxGetLocationOptions("gcj02")

        // The two systems are not interchangeable, so the SDK never lets the host
        // pick one by default.
        assertEquals("gcj02", options.type)
        assertEquals("undefined", jsTypeOf(options.success))
        assertEquals("undefined", jsTypeOf(options.fail))
        assertEquals("undefined", jsTypeOf(options.complete))
    }

    @Test
    fun theOptionsAcceptCallbacks() {
        val options = wxGetLocationOptions("wgs84")
        options.success = { result -> result.latitude; result.errMsg }
        options.fail = { result -> result.errMsg.length }

        assertNotNull(options.success)
        assertNotNull(options.fail)
    }

    @Test
    fun thePresenceGuardAnswersFalseWithoutAWxGlobal() {
        assertFalse(hasWxGetLocation())
    }

    @Test
    fun aUsablePositionIsRead() {
        val result = fakeGetLocationSuccess(latitude = 31.5, longitude = 121.5, accuracy = 15.0)

        val sample = wxLocationSample(result) as WxLocationSample.Present
        assertEquals(31.5, sample.latitude)
        assertEquals(121.5, sample.longitude)
        assertEquals(15.0, sample.accuracyMeters)
    }

    @Test
    fun theCoordinateBoundsAreAccepted() {
        val extremes = listOf(
            Triple(-90.0, -180.0, 0.0),
            Triple(90.0, 180.0, 1_000_000.0),
            Triple(0.0, 0.0, 0.0),
        )

        extremes.forEach { (latitude, longitude, accuracy) ->
            val sample = wxLocationSample(
                fakeGetLocationSuccess(latitude, longitude, accuracy),
            )
            assertEquals(
                WxLocationSample.Present(latitude, longitude, accuracy),
                sample,
                "extremes ($latitude, $longitude, $accuracy)",
            )
        }
    }

    @Test
    fun aMissingFieldIsUnreadable() {
        assertEquals(
            WxLocationSample.Unreadable,
            wxLocationSample(fakeGetLocationSuccess(null, 121.0, 12.0)),
        )
        assertEquals(
            WxLocationSample.Unreadable,
            wxLocationSample(fakeGetLocationSuccess(31.0, null, 12.0)),
        )
        assertEquals(
            WxLocationSample.Unreadable,
            wxLocationSample(fakeGetLocationSuccess(31.0, 121.0, null)),
        )
    }

    @Test
    fun aFieldThatIsNotANumberIsUnreadable() {
        assertEquals(
            WxLocationSample.Unreadable,
            wxLocationSample(fakeGetLocationSuccess("31.0", 121.0, 12.0)),
        )
        assertEquals(
            WxLocationSample.Unreadable,
            wxLocationSample(fakeGetLocationSuccess(31.0, 121.0, js("({})"))),
        )
    }

    @Test
    fun nonFiniteNumbersAreUnreadable() {
        // NaN and the infinities are numbers in JavaScript, so a type check alone
        // would let them through and produce a coordinate that is neither valid nor
        // invalid.
        assertEquals(
            WxLocationSample.Unreadable,
            wxLocationSample(fakeGetLocationSuccess(Double.NaN, 121.0, 12.0)),
        )
        assertEquals(
            WxLocationSample.Unreadable,
            wxLocationSample(fakeGetLocationSuccess(31.0, Double.POSITIVE_INFINITY, 12.0)),
        )
        assertEquals(
            WxLocationSample.Unreadable,
            wxLocationSample(fakeGetLocationSuccess(31.0, 121.0, Double.NEGATIVE_INFINITY)),
        )
    }

    @Test
    fun aCoordinateOutsideItsRangeIsUnreadable() {
        assertEquals(
            WxLocationSample.Unreadable,
            wxLocationSample(fakeGetLocationSuccess(90.1, 121.0, 12.0)),
        )
        assertEquals(
            WxLocationSample.Unreadable,
            wxLocationSample(fakeGetLocationSuccess(-90.1, 121.0, 12.0)),
        )
        assertEquals(
            WxLocationSample.Unreadable,
            wxLocationSample(fakeGetLocationSuccess(31.0, 180.1, 12.0)),
        )
        assertEquals(
            WxLocationSample.Unreadable,
            wxLocationSample(fakeGetLocationSuccess(31.0, -180.1, 12.0)),
        )
    }

    @Test
    fun aNegativeAccuracyIsUnreadable() {
        // A negative radius is not a distance, so the host has reported something
        // the contract cannot carry.
        assertEquals(
            WxLocationSample.Unreadable,
            wxLocationSample(fakeGetLocationSuccess(31.0, 121.0, -1.0)),
        )
    }
}
