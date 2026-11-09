---
id: submodule-server-demo-miniapp
type: submodule-design
status: draft
title: app/miniApp · 服务器示例微信宿主
parent: module-example-server
depends-on: [submodule-server-demo-core]
---

# 服务器 demo 的微信宿主

[English](SPEC-en.md)

## 职责与入口

原生页面消费 core 的 platformGreeting 与 MiniAppExports.sdkVersion，展示 runtime 环境；它不请求 server，也不将 Compose UI 编译为 WXML。JSDoc/jsconfig 使用当前 bundle 声明，微信仍执行 JS。

## 源码与生成边界

页面和宿主配置由消费者维护，libs 仅由 sync 管理，distribution 为根 build 下独立工程。只提交 touristappid 占位，私有测试配置不入库；初始化补缺不迁移现有页面。

## 验证与限制

示例根 sync 后在此执行 `node scripts/smoke.cjs`，再独立测试 distribution 副本；这是隔离宿主接线。已有 VS Code 声明导航、IDEA 已接受的 JS 目标差异及本地 Kotlin/KDoc 证据见 DEVELOPMENT，不证明微信运行、重开持久性、所有 IDE 或无服务器 demo。实际工具运行按本例 README 单独验收。
