---
id: submodule-gradle-toolchain
type: submodule-design
status: draft
title: gradle · Wrapper、版本与依赖目录
parent: module-repository-build
tags: [toolchain]
---

# Wrapper、版本与依赖目录

[English](SPEC-en.md)

## 职责与输入表面

`wrapper/gradle-wrapper.properties` 与仓库 Wrapper 固定根构建的 Gradle 分发；`libs.versions.toml` 统一 Kotlin、coroutines、发布插件以及共享 miniapp 版本。它们是可执行构建输入，不由 IDE 状态或路线图替代。

SDK 版本常量与插件携带的 runtime 坐标从 catalog 生成，避免重复字面量。消费者插件使用 KGP compileOnly，由消费者提供运行时；不把构建工具变成 SDK 的 runtime 依赖。

## 为什么版本输入只有一个来源

[libs.versions.toml](libs.versions.toml) 同时驱动发布版本、runtime 的生成常量和插件携带的 runtime 坐标。若在三处分别维护字面量，构建可能编译成功却让消费者拿到版本不匹配的依赖或报告；生成关系比人工同步更能约束这种漂移。

Wrapper 固定的是构建入口，不替消费者决定工程版本。独立消费者为了重现实验或已发布依赖可以保留自己的输入；不能为了根目录数值整齐就统一升级它们并破坏证据身份。

## 边界与版本范围

根构建没有 pin JDK toolchain；文档中的 JDK 验证环境不是新增的硬性 toolchain。只使用 `./gradlew` 或 `gradlew.bat`，不把系统 Gradle 作为构建事实源。

独立 demo 与发布版消费者有自己的 Wrapper/catalog；它们为复现实例固定的版本不随根 catalog 自动升级。版本相同也不能把本地 composite 和公开依赖的证据混用。

## 验证

`./gradlew --version` 确认实际 Wrapper；`./gradlew projects` 确认根模型。版本变更还需相关编译、测试与发布形状检查，不能仅靠目录或声明存在判定兼容。
