---
id: submodule-poc-model-b
type: submodule-design
status: draft
title: model-b · 未使用 source set 反例
parent: module-kgp-model-poc
---

# 未使用 source set 反例

[English](SPEC-en.md)

## 职责与入口

保留默认 js target，同时手工 creating miniappMain/miniappTest 并连接 common 父边，用来复现目录／source set 存在却没有 compilation 消费的失败形状。

## 模型边界

source-set 名字与 dependsOn 本身不能产生平台 compilation 或真实 Mini App test run。这里的失败是保留的反例，不为 production 提供隐藏的回退方案，不把 IDE 中看见目录当成可编译证据。

## 验证

PoC 根 `../../gradlew :model-b:reportKgpModel :model-b:checkMiniAppModel` 显示模型与失败要求；显式 check 预期不满足选定模型要求。根普通 build 成功也不能改写这个结论。
