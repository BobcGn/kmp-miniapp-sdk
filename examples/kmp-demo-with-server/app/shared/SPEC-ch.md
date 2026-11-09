---
id: submodule-server-demo-ui
type: submodule-design
status: draft
title: app/shared · Compose 客户端 UI
parent: module-example-server
depends-on: [submodule-server-demo-core]
---

# 服务器 demo 的 Compose 客户端 UI

[English](SPEC-en.md)

## 职责与入口

拥有 Compose App、跨移动端资源与 MainViewController，api 消费 core 的共享逻辑；Shared framework 是 Swift 消费入口。Android/iOS 客户端渲染在这里，不由 SDK 提供。

## 边界

Compose、Material、客户端 lifecycle 集成和资源依赖留在本模块；它不应用 Mini App 插件，不是 core 的父 source set。目录内存在某平台源码并不能证明该 target 已配置或测试实际执行，Gradle 配置才是依据。

## 验证

示例根可执行 `./gradlew :app:shared:testAndroidHostTest :app:shared:iosSimulatorArm64Test`，并单独构建移动应用。测试／framework 编译不能代表 iOS App 启动、真机或微信支持 Compose 资源；实际结果以 README/运行记录为准。
