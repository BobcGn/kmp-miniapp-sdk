---
id: submodule-poc-model-a
type: submodule-design
status: draft
title: model-a · 手工 target 对照
parent: module-kgp-model-poc
---

# 手工命名 target 对照

[English](SPEC-en.md)

## 职责与入口

消费者脚本直接声明 `js("miniapp")`，配置 Node、CommonJS library 与 TypeScript 声明；作为 KGP 派生 miniappMain/miniappTest 的有效对照，不使用实验插件。

## 模型边界

它能建立 compilation 和 source sets，但不满足消费者脚本无需手工 JS 接线的交付要求。它不是当前插件契约，也不因可编译而成为选定方案；与其他对比模型的区别由 [[module-kgp-model-poc]] 汇总。

## 验证

从 PoC 根执行 `../../gradlew :model-a:reportKgpModel :model-a:checkMiniAppModel`。check 中手工接线要求预期失败，其他模型事实需看实际报告。不能删去这条要求来把对照误报为完全验收。
