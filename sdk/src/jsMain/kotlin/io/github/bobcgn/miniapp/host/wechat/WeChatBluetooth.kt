package io.github.bobcgn.miniapp.host.wechat

/**
 * Marks the WeChat BLE proof of concept.
 *
 * BLE is here to test the SDK's event-driven resource model — paired `on`/`off`
 * registration, flow cancellation, and cleanup — not to offer a stable Bluetooth
 * API. The models and functions marked with this annotation may change or
 * disappear, and only adapter open/close, discovery, and the connection state
 * stream are implemented. There is no service or characteristic access, no MTU
 * handling, no reconnection, and no background scanning.
 */
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "The WeChat BLE proof of concept is experimental and may change.",
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.TYPEALIAS,
)
public annotation class ExperimentalMiniAppBleApi

/**
 * One device a discovery session reported.
 *
 * Only the fields the proof of concept needs are modelled. WeChat also reports
 * the advertised service UUIDs and the raw advertisement payload; neither is
 * carried here, because nothing in this SDK consumes them yet and a field kept
 * "for completeness" is a field that has to be kept correct.
 *
 * @property deviceId the host's own identifier for the device. It is a host value:
 *   on some platforms it is a MAC address, so a consumer must treat it as
 *   identifying and must not print it.
 * @property name the device's advertised name, or `null` when it advertised none.
 *   A name is not verified: it is what the device claims to be called.
 * @property rssi the signal strength the host reported, or `null` when it reported
 *   none. WeChat declares this field as required, but a host that omits it is
 *   reported as unknown rather than as a made-up zero.
 */
@ExperimentalMiniAppBleApi
public data class WeChatBleDevice(
    public val deviceId: String,
    public val name: String?,
    public val rssi: Int?,
)

/**
 * What the host reports about its Bluetooth adapter.
 *
 * @property available whether this host has a Bluetooth adapter at all.
 * @property discovering whether the adapter is currently scanning. The query
 *   reports it; an adapter-state change event does not, and a state built from an
 *   event therefore leaves it `null` rather than carrying a stale answer.
 * @property powered whether the adapter is switched on, or `null` when the source
 *   of the state does not report it. "Powered off" and "not reported" are
 *   different facts and are not flattened into one boolean.
 */
@ExperimentalMiniAppBleApi
public data class WeChatBleAdapterState(
    public val available: Boolean,
    public val discovering: Boolean?,
    public val powered: Boolean?,
)

/**
 * One connection-state change the host reported.
 *
 * The stream is not per device: WeChat reports a change for any device, and the
 * event names the device it belongs to.
 *
 * @property deviceId the host's identifier for the device this change is about.
 * @property connected whether that device is now connected.
 */
@ExperimentalMiniAppBleApi
public data class WeChatBleConnectionState(
    public val deviceId: String,
    public val connected: Boolean,
)
