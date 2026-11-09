---
id: module-fixtures
type: module-design
status: draft
title: fixtures · 独立消费者验证
parent: architecture
tags: [integration, consumers]
---

# 独立消费者验证

[English](SPEC-en.md)

## 职责与模块

保存可评审、可重复运行的外部消费者形状，不把消费者合并成 SDK 根构建模块。[[module-fixture-consumer]] 验证单项目源码消费；[[module-fixture-root]] 验证根选择逻辑模块及宿主所有权；[[module-fixture-published]] 专门验证公开依赖与源码归档。

前两者通过 composite 解析当前工作树的插件和 runtime，第三者刻意拒绝本地替换。三者的证据不可互换：发布归档存在、模块接线和微信运行分别作答。

## 依赖与证据边界

fixture 只消费公共 API/插件，不引用 SDK internal 包；宿主拥有 WXML、WXSS 和薄 JS，生成库不作为手写源码。测试号、私有配置与凭据不入库。

根 `verifyMiniAppConsumerFixture` 只聚合其明确列出的单项目场景，不自动运行所有 fixture 或 demo。各子规格给出自己的验证入口；Node smoke 不替代真实宿主运行，历史验收保留原环境。
