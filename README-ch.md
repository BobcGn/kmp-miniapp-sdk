# kmp-miniapp-sdk

[English](README-en.md)

一个通过 Kotlin/JS 将共享 Kotlin 逻辑桥接到微信小程序的 Kotlin Multiplatform SDK。

## 状态

实验性 / pre-alpha。项目 Bootstrap 已可构建并通过测试，但尚未实现任何 `wx` API bridge。

## 本项目是什么

- 承载共享 Kotlin 逻辑的 Kotlin Multiplatform library
- 面向微信小程序宿主的 Kotlin/JS library
- 构建强类型 JavaScript / TypeScript runtime bridge 的基础

## 本项目不是什么

- UI framework 或 Kuikly replacement
- Compose renderer 或 Virtual DOM
- WXML replacement

## 架构

```text
commonMain
    ↓
jsMain
    ↓
Kotlin/JS
    ↓
TypeScript / JavaScript
    ↓
WeChat Mini Program
```

## 当前范围

仓库当前只有一个 `:sdk` module，已配置 JavaScript library target、CommonJS 输出、TypeScript definition generation 和基于 Node.js 的 Kotlin/JS tests。项目尚未暴露 JavaScript consumer API，也未实现微信平台 API。

## 构建

使用仓库内置的 Gradle Wrapper：

```shell
./gradlew projects
./gradlew clean build
./gradlew :sdk:jsNodeTest
```

## 文档

- [当前项目事实](docs/PROJECT_FACTS-ch.md)
- [架构](docs/ARCHITECTURE-ch.md)
- [开发指南](docs/DEVELOPMENT-ch.md)
- [路线图](docs/ROADMAP-ch.md)
- [架构决策](docs/decisions/README-ch.md)

## 项目事实

当前项目状态以 [docs/PROJECT_FACTS-ch.md](docs/PROJECT_FACTS-ch.md) 为人类可读事实源。若文档过期，以可执行 Gradle 配置和源码为准。

## 许可证

待定。
