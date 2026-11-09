---
id: submodule-poc-model-c
type: submodule-design
status: draft
title: model-c · 插件命名 target 选定模型
parent: module-kgp-model-poc
depends-on: [module-poc-plugin]
---

# 插件命名 target 的选定模型

[English](SPEC-en.md)

## 职责与入口

消费者只应用 KMP 和 `io.github.bobcgn.miniapp.poc`，不写 JS DSL；实验插件创建命名 miniapp target，由 KGP 派生 source sets、父边和 compilations。这是 ADR 0010 选定并由生产插件延续的模型。

## 边界

miniappTest 必须实际执行公共与平台测试，而不仅存在同名生命周期任务。选定的是模型归属，不意味着这个 PoC 已是发布插件、宿主 distribution 或 SDK API；没有生产后端或 UI 责任。

## 验证

在 PoC 根运行 `../../gradlew :model-c:miniappTest :model-c:checkMiniAppModel`；十项要求及执行报告才是依据。配置缓存需要独立重复调用举证，Node 模型成功不能替代微信验收。
