package io.github.bobcgn.miniapp.host.wechat.testing

import io.github.bobcgn.miniapp.host.wechat.WechatHost
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatAuthHost
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatNavigationHost
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatNetworkHost
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatPermissionHost
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatRuntimeInfoHost
import io.github.bobcgn.miniapp.host.wechat.adapter.WechatStorageHost

/**
 * Builds a [WechatHost] wired entirely to the FakeAdapter boundary.
 *
 * Nothing here touches the `wx` global, so tests that use this fixture exercise
 * the real host implementation without a WeChat runtime.
 */
internal fun fakeWechatHost(
    runtimeInfoHost: WechatRuntimeInfoHost = FakeWechatRuntimeInfoHost(),
    storageHost: WechatStorageHost = FakeWechatStorageHost(),
    authHost: WechatAuthHost = FakeWechatAuthHost(),
    networkHost: WechatNetworkHost = FakeWechatNetworkHost(),
    navigationHost: WechatNavigationHost = FakeWechatNavigationHost(),
    permissionHost: WechatPermissionHost = FakeWechatPermissionHost(),
): WechatHost = WechatHost(
    storageHost = storageHost,
    authHost = authHost,
    networkHost = networkHost,
    navigationHost = navigationHost,
    runtimeInfoHost = runtimeInfoHost,
    permissionHost = permissionHost,
)
