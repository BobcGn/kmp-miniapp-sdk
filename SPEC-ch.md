---
id: module-repository-build
type: module-design
status: draft
title: 根构建 · 产物与验证接线
parent: architecture
tags: [build, repository]
---

# 根构建的产物与验证接线

[English](SPEC-en.md)

## 职责与入口

根 `settings.gradle.kts` 纳入 runtime 与消费者插件两个 project，并 include 仓库专用 build-logic；独立示例、夹具和 PoC 不自动成为此构建的模块。根脚本编排仓库验证，不代替消费者插件。

`buildMiniAppSdk` 依赖 SDK production library distribution，将编译器模块、声明和 source maps 同步到微信能力示例，保留手写 CommonJS wrapper；它不是消费者的初始化或分发入口。

## 为什么仓库编排不成为消费者契约

[build.gradle.kts](build.gradle.kts) 要验证的是这个仓库交付出的 SDK/插件，而消费者构建不应认识仓库目录或复制清单。内部 buildMiniAppSdk 可以维护能力示例，但把它作为接入步骤会让共享逻辑必须依附 SDK checkout，违背独立消费者主线。

根任务把持久 fixture、文档与尺寸检查变成可重复入口，避免只留下「有人曾打开过示例」的证据。嵌套 clean 和公共 build 目录需要顺序归属，因此编排也负责避免并发相互覆盖，而不接管被测插件实现。

## 验证任务与所有权

`verifyMiniAppConsumerDocs` 检查根 README 章节／代码块结构、fixture 路径和消费者命令，接入 check；`verifyMiniAppGradlePluginIntegration` 聚合插件 suite、SDK guard 与真实消费者 fixture，不接入 check 以免嵌套构建重入。

各 fixture 运行按顺序避免 clean 相互删除和并发写同一产物；fixture 仍以普通独立构建消费公共坐标。`verifyMiniAppBundleSize` 来自 [[module-build-logic]]，工具版本和坐标来自 [[submodule-gradle-toolchain]]；CI/发布职责见 [[module-ci-release]]。

## 边界与验证

根接线不引入 renderer、业务配置、消费者源码迁移或宿主部署。不得从 `check` 推断独立 demo、真机或后端链路已运行。

```shell
./gradlew projects
./gradlew --no-daemon --console=plain verifyMiniAppConsumerDocs
./gradlew verifyMiniAppGradlePluginIntegration
```

这些命令覆盖不同范围；只运行文档任务不代表完整构建通过。Gradle 日志与实际任务结果才是证据，既有历史结果不能代替新的实际执行。
