package io.github.bobcgn.miniapp.testing

import io.github.bobcgn.miniapp.capability.storage.MiniAppStorage

/**
 * Host-neutral in-memory [MiniAppStorage].
 *
 * This is the reference implementation the shared storage contract is written
 * against. It exists so the contract can be stated once and then re-run against
 * a real host adapter.
 */
internal class InMemoryStorage : MiniAppStorage {
    private val values = mutableMapOf<String, String>()

    override suspend fun get(key: String): String? = values[key]

    override suspend fun set(key: String, value: String): Unit {
        values[key] = value
    }

    override suspend fun remove(key: String): Unit {
        values.remove(key)
    }
}
