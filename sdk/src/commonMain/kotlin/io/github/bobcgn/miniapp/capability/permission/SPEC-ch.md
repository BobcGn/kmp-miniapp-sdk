---
id: submodule-sdk-permissions
type: submodule-design
status: draft
title: permission · 权限生命周期
parent: submodule-sdk-capabilities
tags: [permission]
---

# 权限生命周期

[English](SPEC-en.md)

## 职责与公共入口

`MiniAppPermissions` 对 `PermissionKey` 提供 stateOf/request/openSettings；`PermissionState` 为 NotRequested、Granted、Denied，provider key 为 `permission`。key 描述权限用途，微信 `scope.*` 不进入公共词汇。

## 状态与交互契约

每次查询以宿主为准，不缓存决定，因为用户可以随时修改宿主设置。请求和打开设置必须由消费者用户手势触发，SDK 不在启动或别的能力调用中自行弹窗。设置页关闭后仍要读取真实状态，不能把返回当作授权。

拒绝是 `PermissionDenied`，宿主无法作答是 HostFailure；两者不能混用。微信 adapter 当前将同一权限的并发请求合并为服务拥有的调用，这不是要求所有宿主共享实现细节。权限状态也不等同于 CapabilitySupport 的 PermissionDependent，更不等同于隐私协议。

## 验证与限制

`PermissionContractChecks`、`MiniAppPermissionsContractTest` 与 `WechatPermissionsTest` 检查三态、不缓存、拒绝、设置返回及并发行为。运行 `./gradlew :kmp-miniapp-sdk:jsNodeTest`。真实手势、弹窗和账号初始状态仍由宿主验收确认，见 [[submodule-wechat-evidence]]。
