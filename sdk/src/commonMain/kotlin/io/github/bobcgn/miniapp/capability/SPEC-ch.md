---
id: submodule-sdk-capabilities
type: submodule-design
status: draft
title: capability · 能力身份与支持状态
parent: submodule-sdk-common
tags: [capabilities]
---

# 能力身份与支持状态

[English](SPEC-en.md)

## 职责与公共表面

`CapabilityKey` 标识语义能力，`CapabilitySupport` 报告当前宿主的 Supported、Unsupported、VersionDependent 或 PermissionDependent。后两态仍然是当前不可用，而不是准许继续调用；状态不是构建时常量，也不是权限已授予的证明。

公共 key 使用宿主无关语义；微信专属 key 由微信目录命名，不把原始 `wx` 方法当作通用能力。当前微信 gate 不产出 PermissionDependent，不能从模型中存在该状态推断已完成动态权限门控。

## 领域与依赖

[[submodule-sdk-storage]] 拥有字符串存储；[[submodule-sdk-network]] 拥有文本 HTTP 与网络状态；[[submodule-sdk-lifecycle]] 拥有 App 可见性；[[submodule-sdk-permissions]] 与 [[submodule-sdk-privacy]] 分别拥有独立生命周期。各领域使用小 provider facet，不依赖兄弟实现或中央 service locator。

支持模型使用公共 `HostVersion`，Host 又使用 key 与支持状态；它们是同一公共 compilation 的共享类型，不能从目录拆分推断单向、独立发布关系。宿主探测、错误文本与具体前置条件策略不属于本目录的模型声明。

## 验证

`CapabilitySupportContractChecks`、`MiniAppHostTest` 和各领域的共享契约检查经 `./gradlew :kmp-miniapp-sdk:jsNodeTest` 执行。测试模型不替代真实宿主对能力的实时回答。
