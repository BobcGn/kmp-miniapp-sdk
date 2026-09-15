# kmp-miniapp-sdk

[English](README-en.md)

一个通过 Kotlin/JS 将共享 Kotlin 逻辑桥接到微信小程序的 Kotlin Multiplatform SDK。

## 状态

实验性 / pre-alpha。Consumer Bridge、Storage、`wx.login` 客户端认证启动、`wx.request` HTTP transport、App lifecycle、部分微信页面栈导航、运行时能力检测、权限生命周期、微信会话有效性检查、微信剪贴板与震动，以及微信基础文件系统访问已经实现并通过规定的真实宿主验证。隐私授权已实现但仍取决于小程序的后台隐私配置与账号，真实宿主验收尚未完成。权限运行所用账号已对唯一映射的权限持有决定，因此无法产出 `NotRequested`；该状态由自动化测试覆盖。其他能力的准确状态见 [微信能力矩阵](docs/platforms/wechat/WECHAT_CAPABILITIES-ch.md)。

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
