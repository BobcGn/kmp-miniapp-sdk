---
id: module-poc-plugin
type: module-design
status: draft
title: miniapp-poc-plugin · 实验模型提供者
parent: module-kgp-model-poc
---

# 实验模型提供者

[English](SPEC-en.md)

## 职责与内部入口

独立 included build 提供 `io.github.bobcgn.miniapp.poc` 与 `.poc.hidden` 两个实验插件，分别驱动 model-c 与 model-c2。它让消费者脚本不手写 JS 接线，以比较 KGP 真实模型。

## 边界

它不是生产 `io.github.bobcgn.miniapp`，不为 SDK 提供模块、发布、能力或 distribution pipeline。命名 target 与隐藏 target 的差异只用于实验，不把被否决的挂接方式作为 production fallback。

## 验证

由 PoC settings includeBuild 解析，运行父构建的模型报告及 checkMiniAppModel。被测 compilation、source-set 归属和测试实际执行比插件 descriptor 存在更重要；模型层面的结果不证明宿主可运行。
