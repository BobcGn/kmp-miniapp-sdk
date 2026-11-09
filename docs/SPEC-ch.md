---
id: module-docs
type: module-design
status: draft
title: docs · 文档、决策与证据
parent: architecture
tags: [documentation, evidence]
---

# 文档、决策与证据

[English](SPEC-en.md)

## 职责

保存源码无法单独解释的责任边界，以及可追溯的能力与验证记录。[[goal-and-requirements]] 拥有产品目标，[[architecture]] 拥有系统约束；就近模块规格拥有局部契约，不在这里复制它们。

目录级规格 [[submodule-architecture-decisions]] 与 [[submodule-wechat-evidence]] 分别细化历史决定和微信证据职责，不重复注册原始 ADR 或矩阵文件。

## 文档边界

- `PROJECT_FACTS-*` 是当前事实索引，源码与可执行配置仍是实现事实的最高依据。
- `decisions/` 保存已接受决定和当时的背景；历史的「未实现」不否定后来的源码。改变决定需要新的 ADR，而不是把旧记录改写成新历史。本图谱引用 ADR 作为来源，不逐个注册决策节点。
- `DEVELOPMENT-*`、`CONSUMER_SETUP-*`、`TESTING-*` 与 `RELEASING-*` 分别承载开发、接入、测试与维护者发布流程。
- 微信能力矩阵拥有状态与最低证据等级，宿主验证矩阵拥有环境、记录与证据限制；Node 结果不能替代宿主、真机或后端结果。
- `ROADMAP-*` 是计划输入，`performance-baseline.json` 是尺寸比较输入；都不能被解读为新能力或新的运行结果。

## 意图、事实与推断

模块规格应回答「负责什么、为什么这条边界有必要、越界的代价是什么」，而非逐文件解释实现。理由优先来自有效 ADR、已有文档或源码中的明确契约；源码可确认依赖与行为，却不自动确认历史动机。缺少依据的解释必须在对应句旁标为「未确认」，不靠 draft 标签或多个新规格的互引把它变成决定。

系统不变量在架构中说明一次，兄弟依赖在父规格说明，叶节点只保留自己的责任、取舍与必要来源。已确认意图可形成职责依据，但不能把意图写成已实现能力或新的验收结果。节点数量、文件覆盖与结构校验不证明这些语义正确。

## 图谱与维护契约

中文 `*-ch.md` 使用唯一 `id`、`type`、`title` 和真实 `parent` 注册节点；英文配对文件只使用 `translation-of` 指向同一 ID，正文事实、结构与意图同步。英文文件不是第二个节点，不使用重复 ID。文件路径可以不是 `SPEC.md`，识别取决于 frontmatter，而不是文件名。

`parent` 表达责任归属；`depends-on` 只表达实际依赖，不代表执行顺序。没有源码依赖的背景材料使用正文链接或 `references`，不把 roadmap/Issue 链变成开发门槛。新导入的规格保持 `draft`，只有用户审阅后才能变为 `active`。

## 验证与限制

`spec_validate` 检查重复 ID、悬空链接和父环；树覆盖、英文不重复注册、译文结构及本地文件链接另行检查。`verifyMiniAppConsumerDocs` 只覆盖 README 的结构、路径与消费者命令，不能证明全部文档语义一致。

已核实的实现／契约差异保留在拥有该边界的规格中，历史 ADR 与原始验收证据不被静默改写。规格索引本身不是新的 SDK API，也不新增发布物或 Gradle module。
