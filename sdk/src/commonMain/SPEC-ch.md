---
id: submodule-sdk-common
type: submodule-design
status: draft
title: commonMain · 公共 Kotlin 契约
parent: module-sdk
tags: [contracts, platform-neutral]
---

# 公共 Kotlin 契约

[English](SPEC-en.md)

## 职责与入口

承载消费者可以共享的 Host、能力契约、模型、错误和 internal 异步原语。`api/MiniAppSdk.kt` 提供版本 metadata；其值由 Gradle 从 catalog 生成，不维护第二份版本字面量。它不创建具体宿主或实现消费者业务。

## 子模块与依赖

[[submodule-sdk-host]] 定义宿主与版本，[[submodule-sdk-capabilities]] 定义支持模型和各能力领域。两者互用公共类型，属于同一 compilation，不是宣称完全独立的发布包。能力声明使用身份 key；Host 使用支持状态与错误；[[submodule-sdk-errors]] 使用能力 key；[[submodule-sdk-async]] 使用错误与 coroutines。

这些类型只依赖 Kotlin 和 coroutines，不反向依赖微信、export、test fake 或应用 UI。`wx`、DOM、Node、`external`、`dynamic` 与 `js()` 不进入本 source set。新增宿主无关契约必须证明语义确实通用，平台专属选项留给宿主实现。

## 公共表面与验证

公共声明具有显式可见性与类型；internal callback 原语不是消费者 API。行为保证由 [[submodule-sdk-common-tests]] 的参考实现和共享检查验证，再由平台测试复用；当前执行入口为 `./gradlew :kmp-miniapp-sdk:jsNodeTest`。

本层定义契约而非验收状态；已实现但验证不完整的能力不能因此变为 Stable。系统责任由 [[architecture]] 说明。
