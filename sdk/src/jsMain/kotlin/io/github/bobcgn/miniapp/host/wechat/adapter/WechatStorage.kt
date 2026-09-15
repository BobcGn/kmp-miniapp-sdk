package io.github.bobcgn.miniapp.host.wechat.adapter

import io.github.bobcgn.miniapp.async.awaitHostCallback
import io.github.bobcgn.miniapp.capability.storage.MiniAppStorage
import io.github.bobcgn.miniapp.error.MiniAppException

/** WeChat implementation of the platform-neutral string storage capability. */
internal class WechatStorage(
    private val host: WechatStorageHost = WxStorageHost,
) : MiniAppStorage {
    override suspend fun get(key: String): String? = awaitHostCallback { success, failure ->
        host.get(
            key = key,
            success = value@{ value ->
                if (value is String) {
                    success(value)
                } else {
                    failure(
                        MiniAppException.InvalidResponse(
                            "WeChat storage returned a non-string value for key '$key'",
                        ),
                    )
                }
            },
            failure = { result ->
                if (result.errMsg == MISSING_KEY_ERROR) {
                    success(null)
                } else {
                    failure(mapWechatHostFailure(operation = "getStorage", result = result))
                }
            },
        )
        null
    }

    override suspend fun set(key: String, value: String): Unit =
        awaitHostCallback { success, failure ->
            host.set(
                key = key,
                value = value,
                success = { success(Unit) },
                failure = { result ->
                    failure(mapWechatHostFailure(operation = "setStorage", result = result))
                },
            )
            null
        }

    override suspend fun remove(key: String): Unit = awaitHostCallback { success, failure ->
        host.remove(
            key = key,
            success = { success(Unit) },
            failure = { result ->
                failure(mapWechatHostFailure(operation = "removeStorage", result = result))
            },
        )
        null
    }

    private companion object {
        private const val MISSING_KEY_ERROR: String = "getStorage:fail data not found"
    }
}
