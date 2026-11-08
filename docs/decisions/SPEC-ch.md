---
id: submodule-architecture-decisions
type: submodule-design
status: draft
title: decisions · 历史架构决定
parent: module-docs
references: [architecture]
tags: [adr, history]
---

# 历史架构决定

[English](SPEC-en.md)

## 职责与入口

保存已接受决定的上下文、理由和被否决方案。入口为 [决策索引](README-ch.md)，当前系统与模块的可执行边界由 [[architecture]] 及其子树说明。

## 维护边界

ADR 是带时间背景的记录，不是随时覆盖的当前能力清单。0003 的初始状态、0008 的隐私排除、0010 的模型比较等必须放回当时的范围理解，不能据此否认后续实现。

决定确实改变时增加取代它的新 ADR；实现与决定不符时显式记录差异，不先把旧记录改成代码已经满足它。0007 的异常「唯一路径」与当前多个 adapter 直接构造异常的差异由架构规格指出，导入不决定是否重构。

本目录的原始 0001—0011 文档保持原位与原编号，作为来源被引用而非逐一注册为侧栏节点。唯一目录节点承载维护规则，不重新定义这些历史决定。

## 证据限制

ADR 的 `Accepted`、Issue 状态和 PoC 文件存在均不证明当前代码或宿主验收。验证链接需要回到对应源码、测试记录与原始运行环境；中文版与英文版不得对同一决定给出不同含义。
