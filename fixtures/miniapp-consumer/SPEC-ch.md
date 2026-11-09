---
id: module-fixture-consumer
type: module-design
status: draft
title: miniapp-consumer · 单项目源码消费
parent: module-fixtures
depends-on: [module-sdk, module-miniapp-gradle-plugin]
---

# 单项目源码消费者

[English](SPEC-en.md)

## 职责与入口

普通独立 KMP build 按 id 应用插件，公共 runtime 坐标通过 composite 替换到当前源码。`commonMain` 的 greeting/counter 被 `miniappMain` 导出，版本来自 SDK；消费者不手工声明 target、source-set 层级或 runtime 依赖。

## 验证意图与取舍

本 fixture 是最小的普通消费者路径：如果它仍要手工命名 JS target、声明 runtime artifact 或复制生成物，就没有验证插件真正消除了接线。公共源码调用和实际测试报告比任务名更能证明这条路径；根 README 的接入步骤因此可以引用同一个可执行工程，而不是另一套无法运行的片段。

`settings.gradle.kts` 为 plugin 与 dependency substitution 分别 includeBuild，这是当前源码测试的机制，不是要求公开消费者检出 SDK。公开依赖身份的证据归 [[module-fixture-published]]，不在此模拟。

## 模块与产物边界

Kotlin compilation、Mini App 测试与 bundle 属于本项目；[[submodule-fixture-consumer-host]] 消费该 bundle。公共插件负责分发任务，不把 fixture 路径写进 production。source host、generated libs 与独立 distribution 保持不同归属。

`verifyConsumerContract` 检查实际 bundle；`verifyConsumerSourceHost` 检查已提交的宿主。它们不是微信 runtime 验收，增加源文件也不能假装测试已实际执行。

## 验证

在本目录使用仓库 Wrapper：`../../gradlew --no-daemon --console=plain clean miniappTest assembleMiniAppBundle verifyConsumerContract`；根聚合入口为 `./gradlew verifyMiniAppConsumerFixture`。根命令只能从 SDK 根目录运行。微信工具的既有结果由 TESTING 保留，不能代替当前产物的独立运行。
