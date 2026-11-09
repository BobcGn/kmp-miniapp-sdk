---
id: module-example-server
type: module-design
status: draft
title: kmp-demo-with-server · 多客户端与服务器
parent: module-examples
references: [module-miniapp-gradle-plugin]
---

# 多客户端与服务器组合

[English](SPEC-en.md)

## 职责与根配置

独立 KMP 消费者包含移动端、微信和 Ktor server，通过 includeBuild 使用当前 SDK 源码。与无服务器 demo 不同，本例由 core 模块应用插件并启用 rootHost，sourceDirectory 指向 app/miniApp，不由根 logicProject 迁移模块。

## 模块依赖

[[submodule-server-demo-core]] 提供共享逻辑及各平台 variant；[[submodule-server-demo-ui]] 消费 core 并拥有 Compose/资源；[[submodule-server-demo-android]] 与 [[submodule-server-demo-ios]] 消费 UI。[[submodule-server-demo-miniapp]] 只消费 core Mini App bundle；[[submodule-server-demo-backend]] 消费其 JVM variant。

微信 greeting 不请求 server，初始化不要求 server 已运行；服务器存在也不赋予 SDK 后端身份或支付能力。core 不依赖 UI，Mini App 不携带 Compose。

## 验证与限制

本目录 `./gradlew :core:miniappTest syncMiniApp assembleMiniApp verifyMiniApp` 检查 Mini App；移动／server 命令及历史结果见 [README](README-ch.md)。NO-SOURCE 不记作测试 suite 通过；IDEA/VS Code 导航证据不等于微信、iOS、服务器或真机运行验收。
