# kmp-miniapp-sdk

[English](README-en.md)

一个通过 Kotlin/JS 将共享 Kotlin 逻辑桥接到微信小程序的 Kotlin Multiplatform SDK。

## 状态

实验性 / pre-alpha。Consumer Bridge、Storage、`wx.login` 客户端认证启动、`wx.request` HTTP transport、App lifecycle 以及部分微信页面栈导航已经实现并通过规定的微信开发者工具验证。其他能力的准确状态见 [微信能力矩阵](docs/platforms/wechat/WECHAT_CAPABILITIES-ch.md)。

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

仓库当前只有一个 `:sdk` module，提供 CommonJS 与 TypeScript consumer 产物。平台无关契约位于 `commonMain`，微信 interop、adapter、runtime 和 export boundary 位于 `jsMain`。当前只支持能力矩阵明确列出的微信能力；计划中的能力不视为已实现。

## 构建

使用仓库内置的 Gradle Wrapper：

```shell
./gradlew projects
./gradlew clean build
./gradlew :sdk:jsNodeTest
./gradlew buildMiniAppSdk
```

## 文档

- [当前项目事实](docs/PROJECT_FACTS-ch.md)
- [架构](docs/ARCHITECTURE-ch.md)
- [开发指南](docs/DEVELOPMENT-ch.md)
- [微信能力矩阵](docs/platforms/wechat/WECHAT_CAPABILITIES-ch.md)
- [测试与微信宿主验收](docs/TESTING-ch.md)
- [路线图](docs/ROADMAP-ch.md)
- [架构决策](docs/decisions/README-ch.md)

## 项目事实

当前项目状态以 [docs/PROJECT_FACTS-ch.md](docs/PROJECT_FACTS-ch.md) 为人类可读事实源。若文档过期，以可执行 Gradle 配置和源码为准。

## 许可证

待定。
