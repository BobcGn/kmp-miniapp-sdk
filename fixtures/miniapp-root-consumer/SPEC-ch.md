---
id: module-fixture-root
type: module-design
status: draft
title: miniapp-root-consumer · 根级逻辑选择
parent: module-fixtures
depends-on: [module-miniapp-gradle-plugin]
---

# 根级逻辑选择消费者

[English](SPEC-en.md)

## 职责与入口

非 KMP 根通过 logicProject 明确选择已有 `:shared`；子模块拥有 compilation/bundle，根拥有宿主任务与 source/distribution 配置。插件和 runtime 经 composite 消费当前源码，不代表公开版本含有此入口。

## 模块依赖与边界

[[submodule-fixture-root-logic]] 提供共享 greeting、Mini App 导出及 SDK 调用；[[submodule-fixture-root-host]] 只消费它的生成模块。这里没有 Compose UI，不能据此宣称共享 CMP 资源已兼容微信。

根级选择不自动建立 core、不迁移代码，也不配置任意依赖项目。多个 producer、路径重叠或配置覆盖应由插件检查拒绝，而不是夹具绕过。

## 验证

本目录运行 `../../gradlew initMiniApp syncMiniApp assembleMiniApp verifyMiniApp`，分别在 source 与 distribution 运行 `node scripts/smoke.cjs`。已有开发者工具记录只覆盖该纯逻辑夹具，不能替代两个 demo、真机或后端验收。
