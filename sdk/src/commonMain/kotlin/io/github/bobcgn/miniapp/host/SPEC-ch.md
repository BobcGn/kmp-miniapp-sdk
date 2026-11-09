---
id: submodule-sdk-host
type: submodule-design
status: draft
title: host · 宿主身份、版本与能力 guard
parent: submodule-sdk-common
tags: [host, capability]
---

# 宿主、版本与能力 guard

[English](SPEC-en.md)

## 职责与公共入口

`MiniAppHost<P>` 暴露运行时 `capabilitySupport` 与强类型 `platform` 逃生口；`HostPlatformApi` 是平台专属 API 的标记。新增宿主增加实现，不向中央平台枚举增加分支。

`HostVersion` 拥有公共版本解析与比较，而某能力所需的微信版本是宿主数据，不在这里硬编码。`requireSupported` 只接受 `Supported`，其余支持状态统一抛出 `UnsupportedCapability`；调用方需要原因时直接查询状态。

## 取舍与实现依据

[MiniAppHost.kt](MiniAppHost.kt) 把「能否执行」与「如何处理宿主选项」分开：消费者先按能力作答，不需要识别平台枚举。guard 只保留失败的 key，代价是异常本身不携带版本／权限原因；需要解释原因的调用方应查询完整 support，而不是解析错误文本。

[HostVersion.kt](HostVersion.kt) 的解析比较不需要宿主，因而可共享和独立测试；版本从哪里读取、某 API 需要哪个版本则不属于本模块。这样不会把微信版本表编进所有公共模型。

## 边界与已知差异

本层不探测 `wx`，不管理权限状态，也不提供通用 service locator。能力 provider facets 属于各自领域，不把所有服务塞入 Host 基类。

guard 是公开的检查入口，但不是当前所有异常的唯一路径：adapter 也直接抛出同类异常，且部分调用未预先门控。它与 ADR 0007 的单一路径要求仍有差异，见 [[architecture]]；统一门控仍为未决项，不能被描述为已实现的不变量。

## 验证

`HostVersionTest` 覆盖解析、比较和边界；`MiniAppHostTest` 与 `CapabilitySupportContractChecks` 覆盖状态与 guard。执行 `./gradlew :kmp-miniapp-sdk:jsNodeTest`。这些是公共语义检查，不证明真实宿主可用。
