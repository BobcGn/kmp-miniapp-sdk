---
id: module-example-server
type: module-design
status: draft
title: kmp-demo-with-server · 实验性多客户端消费者
parent: module-examples
references: [module-miniapp-gradle-plugin]
tags: [experimental-consumer]
---

# 多客户端与服务器组合

[English](SPEC-en.md)

## 职责与根配置

作为 [[module-examples]] 所定义的实验性参考消费者，包含移动端、微信和 Ktor server，通过 includeBuild 使用当前 SDK 源码；不是通用工程模板。与无服务器 demo 不同，本例由 core 模块应用插件并启用 rootHost，sourceDirectory 指向 app/miniApp，不由根 logicProject 迁移模块。

## 为什么服务器与客户端只是不同消费者

core 的 JVM variant 可以被 Ktor 消费，Mini App variant 可以被微信消费；这观察的是共享逻辑与平台依赖的隔离，不是让微信必须先有服务器，或让 SDK 获得后端职责。当前 greeting 不发起网络调用，server 只是另一种消费环境，具体实现与结果在 [README](README-ch.md) 和各模块源码中。

core 内 rootHost 接入与另一 demo 的根 logicProject 是现有两种实验路径，不构成双模板维护承诺。移动 UI/资源与 server 仍不进入 Mini App compilation；若案例尚有未验收部分，不能被其他案例的成功填补。

## 模块依赖

[[submodule-server-demo-core]] 提供共享逻辑及各平台 variant；[[submodule-server-demo-ui]] 消费 core 并拥有 Compose/资源；[[submodule-server-demo-android]] 与 [[submodule-server-demo-ios]] 消费 UI。[[submodule-server-demo-miniapp]] 只消费 core Mini App bundle；[[submodule-server-demo-backend]] 消费其 JVM variant。

微信 greeting 不请求 server，初始化不要求 server 已运行；服务器存在也不赋予 SDK 后端身份或支付能力。core 不依赖 UI，Mini App 不携带 Compose。

## 验证与限制

本目录 `./gradlew :core:miniappTest syncMiniApp assembleMiniApp verifyMiniApp` 检查 Mini App；移动／server 命令及历史结果见 [README](README-ch.md)。NO-SOURCE 不记作测试 suite 通过；IDEA/VS Code 导航证据不等于微信、iOS、服务器或真机运行验收。
