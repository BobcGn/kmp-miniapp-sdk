# 开发指南

[English](DEVELOPMENT-en.md)

## 环境要求

- 与仓库内置 Gradle 9.3.1 Wrapper 兼容的 JDK。项目没有固定 JDK toolchain；当前验证环境为 JDK 25.0.2。
- 仓库内置的 Gradle Wrapper。无需安装系统 Gradle，系统 Gradle 也不是构建事实源。
- Kotlin 2.4.20，由 Gradle 从 version catalog 解析。

在类 Unix 系统上始终使用 `./gradlew`，在 Windows 上使用 `gradlew.bat`。

## IDE

推荐使用 IntelliJ IDEA 开发 Kotlin Multiplatform、Gradle Kotlin DSL 和 Kotlin/JS。

VS Code 可用于处理 `examples/wechat-miniprogram` 下的文件。Consumer Bridge 的真实宿主验证必须使用微信开发者工具。

## 命令

```shell
./gradlew projects
./gradlew clean build
./gradlew :sdk:jsNodeTest
```

这些命令有效，并已于 2026-09-14 完成验证。

## Consumer Bridge

生成 Kotlin/JS production library：

```shell
./gradlew :sdk:jsNodeProductionLibraryDistribution
```

编译器输出位于：

```text
sdk/build/dist/js/productionLibrary/kmp-miniapp-sdk-sdk.js
sdk/build/dist/js/productionLibrary/kmp-miniapp-sdk-sdk.d.ts
sdk/build/dist/js/productionLibrary/kotlin-kotlin-stdlib.js
```

示例使用独立的 consumer-facing 副本：

```text
examples/wechat-miniprogram/miniprogram/libs/
```

当前复制流程有意保持手工操作。自动复制产物属于后续 tooling Issue。刷新时，将编译器模块复制为 `kmp-miniapp-sdk-kotlin.js`，类型声明复制为 `kmp-miniapp-sdk-kotlin.d.ts`，并复制所需的标准库模块；不得改写极薄的 `kmp-miniapp-sdk.js` / `kmp-miniapp-sdk.d.ts` normalization wrapper。

执行本地消费端检查：

```shell
cd examples/wechat-miniprogram
npm install
npm run smoke
npm run typecheck
```

smoke test 加载 consumer-facing CommonJS 模块并调用 `sdkVersion()`。TypeScript 使用 strict mode，并将同一函数解析为返回 `string`。

进行真实宿主验证时，在微信开发者工具中导入 `examples/wechat-miniprogram`，编译项目并打开 index 页面。页面必须显示 `0.1.0-SNAPSHOT`，console 必须包含 `[kmp-miniapp-sdk] sdkVersion: 0.1.0-SNAPSHOT`。

该结果已于 2026-09-14 在微信开发者工具 Stable 2.01.2510290 中通过用户提供的视觉证据完成验证。

Node/CommonJS smoke test 或 TypeScript check 通过，并不能证明微信小程序集成通过。必须分别记录两层结果。

## 开发原则

IntelliJ IDEA 提供代码 intelligence。Gradle Wrapper 是构建事实源。IDE 显示成功并不能证明项目构建或测试成功。

## 添加依赖

依赖和插件版本应声明在 `gradle/libs.versions.toml`。没有明确并记录的原因时，不得在多个 `build.gradle.kts` 文件中重复硬编码相同版本。

只在已实现的变更确实需要时添加依赖，不得添加推测性基础设施。

## 完成定义

修改 SDK 后：

- Gradle build 通过。
- 相关 tests 通过。
- `docs/PROJECT_FACTS-en.md` 和 `docs/PROJECT_FACTS-ch.md` 保持准确和同步。
- JavaScript、WeChat、DOM 或 Node-specific implementation 不得泄漏到 `commonMain`。
