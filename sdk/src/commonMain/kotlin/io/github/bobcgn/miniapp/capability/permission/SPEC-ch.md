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

## 为什么报告宿主决定而非管理授权真相

[MiniAppPermissions.kt](MiniAppPermissions.kt) 与 ADR 0008 将状态留给宿主：用户在设置中修改决定不经过 SDK，缓存就会把旧答案伪装成当前授权。Denied 与宿主故障分开，让调用方能区分「前往设置」和「稍后重试」，而不是盲目再弹一次窗。

权限用途与 `scope.*` 分离，使共享代码不依赖微信命名；代价是 adapter 只支持已明确映射的 key。是否已授权、是否支持某能力、是否满足隐私要求是不同问题，不能为了减少类型而合并。

## 状态与交互契约

每次查询以宿主为准，不缓存决定，因为用户可以随时修改宿主设置。请求和打开设置必须由消费者用户手势触发，SDK 不在启动或别的能力调用中自行弹窗。设置页关闭后仍要读取真实状态，不能把返回当作授权。

拒绝是 `PermissionDenied`，宿主无法作答是 HostFailure；两者不能混用。微信 adapter 当前将同一权限的并发请求合并为服务拥有的调用，这不是要求所有宿主共享实现细节。权限状态也不等同于 CapabilitySupport 的 PermissionDependent，更不等同于隐私协议。

## 验证与限制

`PermissionContractChecks`、`MiniAppPermissionsContractTest` 与 `WechatPermissionsTest` 检查三态、不缓存、拒绝、设置返回及并发行为。运行 `./gradlew :kmp-miniapp-sdk:jsNodeTest`。真实手势、弹窗和账号初始状态仍由宿主验收确认，见 [[submodule-wechat-evidence]]。
