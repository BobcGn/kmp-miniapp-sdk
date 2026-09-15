package io.github.bobcgn.miniapp.testing

import io.github.bobcgn.miniapp.capability.storage.MiniAppStorage
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * The [MiniAppStorage] contract, expressed once for every implementation.
 *
 * Each check takes the storage under test as a parameter, so the same suite runs
 * against the host-neutral reference implementation and against a real host
 * adapter. A new storage guarantee belongs here, not in one adapter's test class.
 */
internal object StorageContractChecks {
    suspend fun absentKeyReadsAsNull(storage: MiniAppStorage) {
        assertNull(storage.get("absent"))
    }

    suspend fun storedValueIsReturned(storage: MiniAppStorage) {
        storage.set("key", "value")

        assertEquals("value", storage.get("key"))
    }

    suspend fun writingAgainReplacesTheValue(storage: MiniAppStorage) {
        storage.set("key", "first")
        storage.set("key", "second")

        assertEquals("second", storage.get("key"))
    }

    suspend fun emptyValueIsStoredDataRatherThanAnAbsentKey(storage: MiniAppStorage) {
        storage.set("key", "")

        // Only an absent key reads as null; an empty string is a value the host stored.
        assertEquals("", storage.get("key"))
    }

    suspend fun removeMakesTheKeyAbsent(storage: MiniAppStorage) {
        storage.set("key", "value")
        storage.remove("key")

        assertNull(storage.get("key"))
    }

    suspend fun removingAnAbsentKeySucceeds(storage: MiniAppStorage) {
        storage.remove("absent")
        storage.remove("absent")

        assertNull(storage.get("absent"))
    }

    suspend fun keysAreIndependent(storage: MiniAppStorage) {
        storage.set("first", "1")
        storage.set("second", "2")

        assertEquals("1", storage.get("first"))
        assertEquals("2", storage.get("second"))
    }

    suspend fun writingOneKeyLeavesOtherKeysIntact(storage: MiniAppStorage) {
        storage.set("first", "1")
        storage.set("second", "2")
        storage.set("first", "updated")

        assertEquals("updated", storage.get("first"))
        assertEquals("2", storage.get("second"))
    }

    suspend fun removingOneKeyLeavesOtherKeysIntact(storage: MiniAppStorage) {
        storage.set("first", "1")
        storage.set("second", "2")
        storage.remove("first")

        assertNull(storage.get("first"))
        assertEquals("2", storage.get("second"))
    }
}
