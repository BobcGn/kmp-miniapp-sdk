package io.github.bobcgn.miniapp.capability.storage

import io.github.bobcgn.miniapp.capability.CapabilityKey

/**
 * Platform-neutral string key-value storage capability.
 *
 * This deliberately small contract does not define object serialization, secure
 * storage, cloud storage, expiration, or host-specific storage options.
 */
public interface MiniAppStorage {
    /** Returns the stored value, or `null` when [key] does not exist. */
    public suspend fun get(key: String): String?

    /** Stores [value] at [key], replacing any previous value. */
    public suspend fun set(key: String, value: String): Unit

    /** Removes [key]. Removing an absent key succeeds without an error. */
    public suspend fun remove(key: String): Unit

    public companion object {
        /** Stable identity used for host capability-support queries. */
        public val Key: CapabilityKey = CapabilityKey("storage")
    }
}

/** Host facet that provides the common [MiniAppStorage] capability. */
public interface StorageCapabilityProvider {
    /** Storage implementation supplied by the active host. */
    public val storage: MiniAppStorage
}
