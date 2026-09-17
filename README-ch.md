# kmp-miniapp-sdk

[English](README-en.md)

一个 Kotlin Multiplatform client runtime，用于跨 Mini App 平台共享客户端行为、宿主能力与 presentation state，同时保持渲染由宿主原生负责。当前微信桥接由 Kotlin/JS 承载。

## 状态

实验性 / pre-alpha。Consumer Bridge、Storage、`wx.login` 客户端认证启动、`wx.request` HTTP transport、App lifecycle、部分微信页面栈导航、运行时能力检测、权限生命周期、微信会话有效性检查、微信剪贴板与震动、微信基础文件系统访问、按需单次微信定位、微信扫码与微信媒体选择已经实现并通过规定的真实宿主验证。隐私授权已实现但仍取决于小程序的后台隐私配置与账号，真实宿主验收尚未完成。位置权限宿主运行覆盖了 `NotRequested`、`Granted` 与 `Denied`。微信订阅消息已实现并具备自动化覆盖，其真实宿主验收尚未完成。微信网络扩展（网络类型查询、网络状态监听、上传与下载）已实现并具备自动化覆盖；上传与下载的真实验收需要一个受控的 HTTPS 测试服务，网络切换需要真机。微信标准支付已实现为「后端参数的类型化转发器」并具备自动化覆盖；它不计算签名、不持有商户密钥，真实宿主验收需要合法商户环境，因此该能力保持 `Partial`。其他能力的准确状态见 [微信能力矩阵](docs/platforms/wechat/WECHAT_CAPABILITIES-ch.md)。

## 本项目是什么

- 承载共享客户端行为与宿主能力的 Kotlin Multiplatform library
- 面向微信小程序宿主的 Kotlin/JS library
- 构建强类型 JavaScript / TypeScript runtime bridge 的基础

## 本项目不是什么

- UI framework 或 Kuikly replacement
- Compose renderer 或 Virtual DOM
- WXML replacement
- 任何形式的 renderer：后端拥有业务事实，Kotlin 拥有客户端行为，宿主拥有渲染。见 [ADR 0011](docs/decisions/0011-presentation-state-shared-rendering-host-native-ch.md)。

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

仓库当前提供 `:kmp-miniapp-sdk` module 及其 CommonJS 与 TypeScript consumer 产物，以及一个 `:miniapp-gradle-plugin` 插件，为 Kotlin Multiplatform 项目提供 `miniappMain` / `miniappTest`、runtime SDK，产出 compiler-managed Mini App distribution 的 `assembleMiniAppBundle` 任务，以及唯一设置是该 bundle 写入位置的 `miniapp { wechat { } }` extension。平台无关契约位于 `commonMain`，微信 interop、adapter、runtime 和 export boundary 位于 `jsMain`。当前只支持能力矩阵明确列出的微信能力；计划中的能力不视为已实现。

`fixtures/miniapp-consumer` 是真实消费者构建，证明外部项目所走的同一条路径：插件按 id、runtime 按公共坐标、bundle 由任务组装。

## 构建

使用仓库内置的 Gradle Wrapper：

```shell
./gradlew projects
./gradlew clean build
./gradlew :kmp-miniapp-sdk:jsNodeTest
./gradlew :miniapp-gradle-plugin:test
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
