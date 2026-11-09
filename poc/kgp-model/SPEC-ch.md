---
id: module-kgp-model-poc
type: module-design
status: draft
title: kgp-model · 平台模型对比实验
parent: architecture
tags: [poc, model]
---

# KGP 平台模型对比实验

[English](SPEC-en.md)

## 职责与模块

隔离比较 Mini App source set 如何获得真实 compilation 与测试任务，不属于 SDK 根构建或消费者发布物。根脚本在模型求值后快照 targets、source sets、父边和任务，提供 reportKgpModel 与十项 checkMiniAppModel。

[[submodule-poc-model-a]] 是手工命名 target 对照，[[submodule-poc-model-b]] 是未使用 source set 反例；[[submodule-poc-model-c]] 是插件拥有命名 target 的选定模型，[[submodule-poc-model-c2]] 是隐藏 js target 的反例。后二者消费 [[module-poc-plugin]]，模型之间不互相依赖。

## 边界与决定

实验解释 ADR 0010 的依据，不扩大当前公开 API。被否决模型仍保留用于复现失败形状，其断言失败是设计证据，不能改弱检查让所有模型变绿，也不能把旧 Issue 链当作当前开发门槛。

## 验证与限制

在本目录用 `../../gradlew :model-c:miniappTest :model-c:checkMiniAppModel` 验证选定模型，其他模型可各自 reportKgpModel。普通 clean check 成功不表示每个显式模型验收都通过。PoC 的 Node 测试、IDE model 与微信加载是不同证据。
