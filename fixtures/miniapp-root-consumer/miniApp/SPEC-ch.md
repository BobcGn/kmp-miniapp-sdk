---
id: submodule-fixture-root-host
type: submodule-design
status: draft
title: miniApp · 根级夹具宿主
parent: module-fixture-root
depends-on: [submodule-fixture-root-logic]
---

# 根级夹具宿主

[English](SPEC-en.md)

## 职责与入口

根级微信工程拥有原生页面与薄 JS，消费 shared module 的 greeting 与 SDK 信息；页面不定义 SDK 能力行为。它证明宿主位于根而 compilation 仍在逻辑子模块的布局。

## 文件与证据边界

源码由消费者维护，libs 为 sync 生成区；build/miniapp-dist 是独立副本，不回指源项目。只提交占位 AppID，私有测试配置不属于规格或源码。

## 验证

同步后在此目录执行 `node scripts/smoke.cjs`，再对 distribution 内副本独立执行。Node 仅是接线；TESTING 中既有基础库与工具版本的开发者工具运行记录只覆盖本夹具，不推广到手机、后端或其他示例。
