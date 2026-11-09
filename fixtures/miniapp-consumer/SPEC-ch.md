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

## 模块与产物边界

Kotlin compilation、Mini App 测试与 bundle 属于本项目；[[submodule-fixture-consumer-host]] 消费该 bundle。公共插件负责分发任务，不把 fixture 路径写进 production。source host、generated libs 与独立 distribution 保持不同归属。

`verifyConsumerContract` 检查实际 bundle；`verifyConsumerSourceHost` 检查已提交的宿主。它们不是微信 runtime 验收，增加源文件也不能假装测试已实际执行。

## 验证

在本目录使用仓库 Wrapper：`../../gradlew --no-daemon --console=plain clean miniappTest assembleMiniAppBundle verifyConsumerContract`；根聚合入口为 `./gradlew verifyMiniAppConsumerFixture`。根命令只能从 SDK 根目录运行。微信工具的既有结果由 TESTING 保留，不在本次重新宣称通过。
