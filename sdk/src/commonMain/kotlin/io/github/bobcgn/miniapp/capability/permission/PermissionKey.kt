package io.github.bobcgn.miniapp.capability.permission

/**
 * Host-neutral identity of a permission the SDK manages.
 *
 * The value names what the permission is *for*, never the string a particular
 * host uses for it. A host adapter maps it into its own vocabulary, and a key the
 * adapter does not know is refused there rather than being handed to a host API.
 * That is what keeps raw host scope names out of the shared layer.
 *
 * The set is deliberately open: a permission is added when a capability needs it,
 * not by mirroring a host's full list.
 *
 * @property value stable, host-neutral identifier
 */
public data class PermissionKey(public val value: String) {
    public companion object {
        /** Permission to capture audio through the host. */
        public val Microphone: PermissionKey = PermissionKey("microphone")
    }
}
