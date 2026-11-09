---
id: submodule-server-demo-backend
type: submodule-design
status: draft
title: server · Ktor JVM 示例服务
parent: module-example-server
depends-on: [submodule-server-demo-core]
---

# Ktor JVM 示例服务

[English](SPEC-en.md)

## 职责与入口

`ApplicationKt.main` 启动 Netty；Application.module 的 GET `/` 返回 core 的 sayHello("Ktor")。这是 JVM 消费共享逻辑的最小例子，不是 SDK 后端、认证服务或支付系统。

## 依赖与运行边界

server 依赖 core JVM variant 和 Ktor/日志；core、runtime SDK 与 Mini App greeting 都不依赖 server。源码当前绑定 0.0.0.0:8080，启动会监听网络；不能把运行服务伪装成只读编译检查，本次不自动启动。

## 验证

在示例根执行 `./gradlew :server:test` 检查测试；`./gradlew :server:run` 是单独的运行步骤。源码／测试存在或共享 core 编译成功不等于 server、真实后端业务或客户端端到端验收。
