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
 * Each entry is gated on its own, because each is a separate host API and a host
 * may offer any subset: the two clipboard directions, the two vibration lengths,
 * and the four file operations the file manager exposes.
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

    /** Reading a file from the mini program file sandbox. */
    val FileSystemRead: CapabilityKey = CapabilityKey("wechat.filesystem-read")

    /** Writing a file to the mini program file sandbox. */
    val FileSystemWrite: CapabilityKey = CapabilityKey("wechat.filesystem-write")

    /** Testing whether a sandbox path exists and is accessible. */
    val FileSystemAccess: CapabilityKey = CapabilityKey("wechat.filesystem-access")

    /** Removing a file from the mini program file sandbox. */
    val FileSystemRemove: CapabilityKey = CapabilityKey("wechat.filesystem-remove")

    /**
     * The sandbox root the four file operations work against.
     *
     * It is a separate host value from the file manager, so a host can expose one
     * without the other and it is gated on its own.
     */
    val FileSystemSandboxPath: CapabilityKey = CapabilityKey("wechat.filesystem-sandbox-path")
}
