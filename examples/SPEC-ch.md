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

## 为什么保留不同案例

两个 KMP demo 的已确认角色是实验性参考消费者，不是通用模板。它们让现有共享逻辑、客户端 UI 和微信接入在真实工程形态中被观察；带服务器案例还提供 JVM 消费者，但不因此引入 SDK 的后端职责。SDK 尚有待验证与待推进的内容，案例可运行不能把这些缺口变成已支持。

已确认的是它们不承担稳定工程布局、全部平台兼容或通用模板承诺；源码中的两个接入方式是当前实验事实，不推导必须永久保留两套方式的决定。准确接线与运行记录由各案例维护，目录组织不等于公开 SDK API。这里的「非通用模板」只限定两个 KMP demo，不撤销插件随附的最小微信 source-host 初始化模板契约。

## 依赖与职责边界

能力宿主消费 SDK 与薄 wrapper；两个 demo 的 UI 消费各自 core，微信也只消费对应 core 的 Mini App 产物。服务器示例的 server 消费 core 的 JVM variant，不被 SDK 或微信 greeting 依赖。

Compose 与资源留在客户端 UI，WXML/WXSS 留在微信工程。不能为示例方便将 UI 依赖推入 SDK，或把手工拆分 core/UI 描述为插件自动迁移。系统边界见 [[architecture]]。

## 验证

各示例的 README 记录准确命令和既有结果；SDK 根 check 不运行所有独立消费者。编译、Node smoke、IDE 导航、微信工具与真机验收分开记录，一个 demo 的截图不能证明另一个。只保留占位配置，不提交密钥或本机私有设置。
