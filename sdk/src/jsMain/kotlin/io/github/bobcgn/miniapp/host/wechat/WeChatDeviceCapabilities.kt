package io.github.bobcgn.miniapp.host.wechat

import io.github.bobcgn.miniapp.capability.CapabilityKey

/**
 * The WeChat device capabilities this SDK gates.
 *
 * These are WeChat-specific rather than host-neutral. Clipboard and vibration are
 * device capabilities WeChat exposes through its own APIs, and no other host has
 * been shown to share their semantics, so they are named with a namespace instead
 * of being presented as capabilities every host can be asked for.
 *
 * Each of the four is gated on its own: the two clipboard directions and the two
 * vibration lengths are separate host APIs, and a host may offer any subset.
 */
internal object WeChatDeviceCapabilities {
    /** Reading system clipboard text. */
    val ClipboardRead: CapabilityKey = CapabilityKey("wechat.clipboard-read")

    /** Writing system clipboard text. */
    val ClipboardWrite: CapabilityKey = CapabilityKey("wechat.clipboard-write")

    /** The short vibration. */
    val VibrateShort: CapabilityKey = CapabilityKey("wechat.vibrate-short")

    /** The long vibration. */
    val VibrateLong: CapabilityKey = CapabilityKey("wechat.vibrate-long")
}
