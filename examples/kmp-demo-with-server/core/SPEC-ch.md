---
id: submodule-server-demo-core
type: submodule-design
status: draft
title: core · 移动、JVM 与 Mini App 逻辑
parent: module-example-server
depends-on: [module-sdk]
---

# 服务器 demo 的共享逻辑

[English](SPEC-en.md)

## 职责与入口

commonMain 提供 sayHello；Android/iOS/JVM 消费同一逻辑。Mini App 的 platformGreeting 从 SDK runtime 信息组合环境文字，缺失时显示未知，再调用 sayHello；不调用 server。

## 构建与依赖边界

本模块应用 Mini App 插件并启用 rootHost，将源码宿主配置到 app/miniApp，compilation/bundle 仍属于 core。SDK runtime 只在 Mini App variant，Compose 与资源只在客户端 UI。模块关系见 [[module-example-server]]，不得把 UI 或 Ktor 服务器依赖反向带进 core。

## 验证与限制

示例根 `./gradlew :core:miniappTest :core:checkMiniAppHostBoundary` 检查 Mini App 逻辑与依赖；其他平台需要各自编译／测试。README 中 jvmTest 的 NO-SOURCE 不证明 JVM suite 通过；有 JVM target 也不说明有业务后端链路。
