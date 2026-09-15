package io.github.bobcgn.miniapp.capability.storage

import io.github.bobcgn.miniapp.testing.InMemoryStorage
import io.github.bobcgn.miniapp.testing.StorageContractChecks
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

/**
 * Runs the shared storage contract against the host-neutral reference implementation.
 *
 * The WeChat adapter runs the same checks in `WechatStorageContractTest`, so a
 * failure here identifies the contract and a failure there identifies the adapter.
 */
internal class MiniAppStorageContractTest {
    @Test
    fun absentKeyReadsAsNull() = runTest {
        StorageContractChecks.absentKeyReadsAsNull(InMemoryStorage())
    }

    @Test
    fun storedValueIsReturned() = runTest {
        StorageContractChecks.storedValueIsReturned(InMemoryStorage())
    }

    @Test
    fun writingAgainReplacesTheValue() = runTest {
        StorageContractChecks.writingAgainReplacesTheValue(InMemoryStorage())
    }

    @Test
    fun emptyValueIsStoredDataRatherThanAnAbsentKey() = runTest {
        StorageContractChecks.emptyValueIsStoredDataRatherThanAnAbsentKey(InMemoryStorage())
    }

    @Test
    fun removeMakesTheKeyAbsent() = runTest {
        StorageContractChecks.removeMakesTheKeyAbsent(InMemoryStorage())
    }

    @Test
    fun removingAnAbsentKeySucceeds() = runTest {
        StorageContractChecks.removingAnAbsentKeySucceeds(InMemoryStorage())
    }

    @Test
    fun keysAreIndependent() = runTest {
        StorageContractChecks.keysAreIndependent(InMemoryStorage())
    }

    @Test
    fun writingOneKeyLeavesOtherKeysIntact() = runTest {
        StorageContractChecks.writingOneKeyLeavesOtherKeysIntact(InMemoryStorage())
    }

    @Test
    fun removingOneKeyLeavesOtherKeysIntact() = runTest {
        StorageContractChecks.removingOneKeyLeavesOtherKeysIntact(InMemoryStorage())
    }
}
