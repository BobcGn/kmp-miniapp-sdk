---
id: module-examples
type: module-design
status: draft
title: examples · 宿主与客户端示例
parent: architecture
tags: [examples, consumers]
---

# 宿主与客户端示例

[English](SPEC-en.md)

## 职责与模块

[[module-example-wechat]] 是 SDK 能力验收用的独立 npm/微信工程；[[module-example-kmp]] 与 [[module-example-server]] 是各有 Wrapper、模块和移动客户端的独立 KMP 消费者。examples 不是一个应用，也不是 SDK 根构建的 Gradle module。

## 依赖与职责边界

能力宿主消费 SDK 与薄 wrapper；两个 demo 的 UI 消费各自 core，微信也只消费对应 core 的 Mini App 产物。服务器示例的 server 消费 core 的 JVM variant，不被 SDK 或微信 greeting 依赖。

Compose 与资源留在客户端 UI，WXML/WXSS 留在微信工程。不能为示例方便将 UI 依赖推入 SDK，或把手工拆分 core/UI 描述为插件自动迁移。系统边界见 [[architecture]]。

## 验证

各示例的 README 记录准确命令和既有结果；SDK 根 check 不运行所有独立消费者。编译、Node smoke、IDE 导航、微信工具与真机验收分开记录，一个 demo 的截图不能证明另一个。只保留占位配置，不提交密钥或本机私有设置。
