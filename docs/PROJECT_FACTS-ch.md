# 项目事实

[English](PROJECT_FACTS-en.md)

本文件是 kmp-miniapp-sdk 当前状态的人类可读事实源。

如果本文件与可执行配置或源码冲突，以可执行配置和源码为准。

## 1. 项目标识

- 项目：`kmp-miniapp-sdk`
- 目的：使用官方 Kotlin Multiplatform 和 Kotlin/JS，让共享 Kotlin 逻辑可由微信小程序 JavaScript 或 TypeScript runtime 消费。
- 类型：SDK / library。

本项目不是微信小程序 UI framework、Kuikly replacement、Compose renderer、Virtual DOM 或 WXML replacement。

## 2. 当前状态

- Experimental
- Pre-alpha
- Bootstrap completed
- Consumer Bridge 已完成并通过微信开发者工具验证
- 微信强类型 interop 基础已实现

构建基线与第一条端到端消费链路均已通过验证。第一批原始 callback-style 微信契约现已通过编译和 option 构造测试；production code 尚未调用任何平台 API。

## 3. 当前 Gradle Modules

通过 `./gradlew projects` 验证：

```text
Root project 'kmp-miniapp-sdk'
└── Project ':sdk'
```

`examples/` 是 integration host 目录，不是 Gradle module。

## 4. 当前 Targets

```text
Kotlin Multiplatform
└── JavaScript
```

Node.js 仅配置为本地 Kotlin/JS 构建和测试环境，不是目标宿主。预期生产宿主是微信小程序 JavaScript runtime。

项目没有配置 Android、JVM、iOS、Wasm 或 browser application target。

## 5. 当前 JS 策略

- 通过 Kotlin 2.4.20 进行 Kotlin/JS IR compilation
- 通过 `useCommonJs()` 生成 CommonJS module output
- 通过 `binaries.library()` 生成 library output
- 通过 `generateTypeScriptDefinitions()` 生成 TypeScript definitions
- 在 `jsMain/.../export` 下提供最小 `@JsExport` facade
- 为消费者提供极薄的 CommonJS export normalization wrapper

编译器生成的 TypeScript declaration 已包含 `MiniAppExports.sdkVersion(): string`。编译器的 CommonJS export shape 带有命名空间，因此 consumer-facing wrapper 在不包含业务逻辑的前提下提供稳定的 `sdkVersion(): string` 入口。

## 6. 当前依赖

| 用途 | 依赖 | 版本来源 | 版本 |
| --- | --- | --- | --- |
| 构建插件和 Kotlin libraries | Kotlin Multiplatform / Kotlin | `gradle/libs.versions.toml` | 2.4.20 |
| 生产代码 | `org.jetbrains.kotlinx:kotlinx-coroutines-core` | `gradle/libs.versions.toml` | 1.11.0 |
| 测试 | `kotlin-test` | Kotlin plugin version | 2.4.20 |

Kotlin/JS platform variants 及其传递依赖由 Gradle 在依赖解析期间选择。

## 7. 当前 Package Namespace

源码 namespace 是 `io.github.bobcgn.miniapp`。共享 metadata API 位于 `io.github.bobcgn.miniapp.api`，JavaScript export facade 位于 `io.github.bobcgn.miniapp.export`，原始微信声明位于 `io.github.bobcgn.miniapp.host.wechat.interop`。

## 8. Source Set 职责

### `commonMain`

允许的职责：

- 平台无关 API 和 interfaces
- Models 和 errors
- Shared logic

禁止的依赖和构造：

- `wx`
- `window` 或 `document`
- DOM APIs
- `dynamic`、`external` 或 `js()`
- Node-specific APIs

### `jsMain`

`jsMain` 负责 JavaScript 和微信小程序 integration。内部预期边界为：

- `host/wechat/interop`：原始 JavaScript 和 `wx` declarations
- `host/wechat/adapter`：platform API 到 Kotlin API 的 adaptation
- `host/wechat/runtime`：宿主 lifecycle 和 runtime integration
- `export`：宿主无关的 Kotlin 到 JavaScript / TypeScript public boundary

`export` 边界目前只包含版本 facade。Interop 边界包含 `showToast`、`getStorage`、`setStorage` 和 `removeStorage` 的 internal 契约；项目尚无调用这些契约的微信平台 API 实现。

## 9. 当前能力

- Kotlin Multiplatform 项目可完成配置和构建。
- `:sdk` JavaScript library target 可编译。
- 可生成生产用 CommonJS library 文件。
- 已配置 TypeScript definition generation 和 validation。
- `MiniAppExports.sdkVersion()` 可导出共享的 `MiniAppSdk.VERSION` 值。
- `examples/wechat-miniprogram/miniprogram/libs` 下的 consumer-facing CommonJS wrapper 暴露 `sdkVersion(): string`。
- 严格 TypeScript consumer 与 Node/CommonJS smoke test 均通过，consumer contract 不依赖 `any`。
- `examples/wechat-miniprogram` 已包含加载同一消费产物并显示版本值的最小 index 页面。
- 微信开发者工具能够加载该产物，在 console 输出 `0.1.0-SNAPSHOT`，并在 index 页面显示相同值。
- 一个 common test 可在 Node.js Kotlin/JS test environment 中运行。
- 已启用 Explicit API mode。
- `jsMain/.../host/wechat/interop` 已定义 internal 全局 `wx` 契约、强类型 callback results，以及首批 toast 和 storage 方法的强类型 option bags。
- 这些 option bags 的 plain-object factories 已在 Kotlin/JS Node test environment 中通过测试，不需要真实 `wx` runtime。

## 10. 明确尚不具备的能力

项目当前没有：

- `wx` API wrappers
- 对已声明 `wx` 契约的 runtime 调用
- Mini Program lifecycle adapter
- Network、storage 或 authentication adapter
- Callback-to-coroutine bridge
- Compose integration、UI DSL、renderer 或 Virtual DOM
- npm publication
- Maven publication
- Gradle plugin

## 11. 已验证命令

验证日期：2026-09-14。

| 命令 | 结果 |
| --- | --- |
| `./gradlew projects` | VERIFIED |
| `./gradlew clean build` | VERIFIED |
| `./gradlew :sdk:jsNodeTest` | VERIFIED |
| `./gradlew :sdk:jsNodeProductionLibraryDistribution` | VERIFIED |
| 在 `examples/wechat-miniprogram` 执行 `npm run smoke` | VERIFIED |
| 在 `examples/wechat-miniprogram` 执行 `npm run typecheck` | VERIFIED |
| 微信开发者工具加载、console 与页面渲染 | VERIFIED — 用户提供的截图同时显示 index 页面与一致的 console 输出 |

验证环境使用仓库内置的 Gradle 9.3.1 Wrapper 和 JDK 25.0.2。当前构建没有固定 JDK toolchain。

## 12. 当前 Consumer Bridge 状态

已验证的端到端链路为：

```text
Kotlin @JsExport
→ TypeScript declaration
→ CommonJS
→ TypeScript require
→ Node smoke test
→ WeChat Mini Program runtime
```

同一 consumer-facing artifact 已在微信开发者工具中完成验证。console 与页面均显示 `0.1.0-SNAPSHOT`。Consumer Bridge 里程碑已经完成。
