---
id: submodule-demo-core
type: submodule-design
status: draft
title: core · 共享逻辑与 Mini App 产物
parent: module-example-kmp
depends-on: [module-sdk]
---

# 无服务器 demo 的共享逻辑

[English](SPEC-en.md)

## 职责与入口

拥有 `Greeting`、sayHello、平台 expect/actual 与逻辑测试；miniappMain 的 `platformGreeting()` 导出同一 `Greeting().greet()`，微信实现从 SDK 取得 runtime 信息，缺失时保留未知。Android/iOS 与微信共享行为，不共享视图。

## 边界

只应用 KMP/平台 library 配置，根 logicProject 在这里接入 Mini App。common 逻辑不依赖 Compose 或 shared UI；SDK runtime 只进入 Mini App source set。导出不创建 view、调用服务器或决定业务事实。模块关系由 [[module-example-kmp]] 统一说明。

## 验证

在示例根执行 `./gradlew :core:miniappTest`；common 测试与 Mini App 平台测试分别覆盖 greeting 和宿主信息形状。Node smoke 使用真实产物加隔离宿主，不是手机或微信工具证据。资源隔离不意味着通用 Compose Res 支持。
