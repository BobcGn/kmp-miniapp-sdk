---
id: submodule-poc-model-c2
type: submodule-design
status: draft
title: model-c2 · 隐藏 js target 反例
parent: module-kgp-model-poc
depends-on: [module-poc-plugin]
---

# 隐藏 js target 反例

[English](SPEC-en.md)

## 职责与入口

应用 `.poc.hidden` 实验插件，保留内部 target 名 js，却尝试将 miniappMain/miniappTest 暴露为消费者入口。它复现「隐藏原 target 名但未建立真实 Mini App compilation 归属」的被否决方案。

## 边界

不能用手工 source-set 目录、别名或看似存在的测试任务掩盖 compilation 不匹配；此模块不是生产插件的兼容路径。对比的通过/失败条件由父实验根定义，不为此例单独放宽。

## 验证

PoC 根执行 `../../gradlew :model-c2:reportKgpModel :model-c2:checkMiniAppModel`。显式模型检查预期失败，普通编译结果不等价于十项验收，更不代表隐藏 target 方案受到支持。
