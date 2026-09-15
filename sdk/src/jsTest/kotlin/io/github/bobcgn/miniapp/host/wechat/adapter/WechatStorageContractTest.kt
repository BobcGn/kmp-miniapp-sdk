package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.capability.storage.MiniAppStorage
import io.github.bobcgn.miniapp.host.wechat.testing.FakeWechatStorageHost
import io.github.bobcgn.miniapp.testing.StorageContractChecks
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

/**
 * Runs the shared storage contract against the WeChat adapter.
 *
 * These are the same checks `MiniAppStorageContractTest` runs against the
 * host-neutral reference implementation. Passing here means `WechatStorage`
 * honours the contract through its callback port; it is not real-host evidence,
 * because the port is a fake rather than `wx`.
 */
internal class WechatStorageContractTest {
    private fun storage(): MiniAppStorage = WechatStorage(FakeWechatStorageHost())

    @Test
    fun absentKeyReadsAsNull() = runTest {
        StorageContractChecks.absentKeyReadsAsNull(storage())
    }

    @Test
    fun storedValueIsReturned() = runTest {
        StorageContractChecks.storedValueIsReturned(storage())
    }

    @Test
    fun writingAgainReplacesTheValue() = runTest {
        StorageContractChecks.writingAgainReplacesTheValue(storage())
    }

    @Test
    fun emptyValueIsStoredDataRatherThanAnAbsentKey() = runTest {
        StorageContractChecks.emptyValueIsStoredDataRatherThanAnAbsentKey(storage())
    }

    @Test
    fun removeMakesTheKeyAbsent() = runTest {
        StorageContractChecks.removeMakesTheKeyAbsent(storage())
    }

    @Test
    fun removingAnAbsentKeySucceeds() = runTest {
        StorageContractChecks.removingAnAbsentKeySucceeds(storage())
    }

    @Test
    fun keysAreIndependent() = runTest {
        StorageContractChecks.keysAreIndependent(storage())
    }

    @Test
    fun writingOneKeyLeavesOtherKeysIntact() = runTest {
        StorageContractChecks.writingOneKeyLeavesOtherKeysIntact(storage())
    }

    @Test
    fun removingOneKeyLeavesOtherKeysIntact() = runTest {
        StorageContractChecks.removingOneKeyLeavesOtherKeysIntact(storage())
    }
}
