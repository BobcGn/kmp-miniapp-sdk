# CLAUDE.md

[English](CLAUDE-en.md)

面向 Claude Code 的本仓库操作指南。

本文件汇总仓库规则，使其在会话开始时自动载入。它不替代规则本身。根目录 `AGENTS.md` 以及嵌套的 `sdk/AGENTS.md`、`examples/AGENTS.md` 仍然具有权威性，本文件不得与其冲突。

## 1. 本项目是什么

`kmp-miniapp-sdk` 是一个 Kotlin Multiplatform SDK，通过 Kotlin/JS 将共享 Kotlin 逻辑桥接到微信小程序。

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

它是一个 SDK / library，不是 application、UI framework、Kuikly replacement、Compose renderer、Virtual DOM 或 WXML replacement。

## 2. 修改前必读

修改项目前，请先阅读：

1. `docs/PROJECT_FACTS-en.md` 或 `docs/PROJECT_FACTS-ch.md`
2. `docs/ARCHITECTURE-en.md` 或 `docs/ARCHITECTURE-ch.md`
3. `docs/DEVELOPMENT-en.md` 或 `docs/DEVELOPMENT-ch.md`
4. `docs/ROADMAP-en.md` 或 `docs/ROADMAP-ch.md` —— 仅当任务涉及未来范围或排期时

随后，针对受影响的区域阅读嵌套规则：`sdk/**` 读 `sdk/AGENTS.md`，`examples/**` 读 `examples/AGENTS.md`。

## 3. 事实层级

按以下顺序确定权威性：

```text
源码与可执行 Gradle 配置
> docs/PROJECT_FACTS-en.md 与 docs/PROJECT_FACTS-ch.md
> docs/ARCHITECTURE-en.md 与 docs/ARCHITECTURE-ch.md
> README-en.md 与 README-ch.md
> docs/ROADMAP-en.md 与 docs/ROADMAP-ch.md
```

绝不可从路线图推断某项能力已经实现。路线图条目、TODO、issue 和注释都是计划，不能作为功能已存在的证据。

## 4. 不可违反的规则

**范围纪律。** 在没有明确任务的情况下，不要添加 Android、iOS、JVM 或 Wasm target；不要添加 Compose、Ktor、serialization 或依赖注入框架；不要添加 npm 或 Maven 发布；不要添加投机性的 Gradle module。

**最小改动。** 只做当前任务所需的最小改动。不要因为"以后可能有用"而添加基础设施。

**构建权威。** 任何"构建通过"的结论都必须由真实的 Gradle Wrapper 调用支撑。IDE 状态不能作为证据。

**计划不等于事实。** 不要把路线图条目当作已交付的工作来陈述。

**公共 API 纪律。** 项目已启用 `explicitApi()`。不要随意新增 public API。每个 public 声明都需要显式可见性与类型、清晰的 SDK 职责，以及相应的测试。不要仅仅为了让测试更好写而扩大 public 面。

**JS 边界。** `dynamic`、`external` 和 `js()` 绝不能进入 `commonMain`。原始 JavaScript interop 应尽可能收窄，最好放在 `jsMain/.../wechat/interop` 下。

**文档同步。** 面向人的文档使用同步的 `-en.md` / `-ch.md` 配对。任一语言发生变化时，必须在同一任务中更新其对应版本，且事实、结构与意图保持一致。本文件同样适用。

**ADR 是历史记录。** `docs/decisions/` 中已接受的决策是历史档案。要改变某个决策，应新增一个取代它的 ADR，而不是改写原记录。

**不要自动进入下一阶段。** 完成当前任务并汇报，不要自动开始路线图中的下一个条目。

## 5. 目录结构

```text
AGENTS.md                    仓库规则
CLAUDE-en.md / CLAUDE-ch.md  本操作指南
README-en.md / README-ch.md
docs/                        PROJECT_FACTS、ARCHITECTURE、DEVELOPMENT、ROADMAP、decisions/（ADR）
gradle/libs.versions.toml    依赖与插件版本的唯一来源
settings.gradle.kts          仅 include :sdk
sdk/                         唯一的 Gradle module
examples/                    集成宿主；不是 Gradle module
```

`sdk/src` 内部：

- `commonMain` —— 平台无关的 API、model、error 与共享逻辑。不得出现 `wx`，不得出现 DOM 或 Node API，不得出现 `dynamic` / `external` / `js()`。
- `commonTest` —— 共享测试。
- `jsMain/kotlin/io/github/bobcgn/miniapp/wechat/` —— `interop`、`adapter`、`runtime`、`export`。这些目录目前仅确立边界，不含任何实现。
- `jsTest` —— JavaScript 测试。

`jsMain/wechat` 各层职责：`interop` 只描述底层 JavaScript 与 `wx` 契约，不放置业务逻辑；`adapter` 负责类型转换、错误映射与异步适配，不承担 UI 职责；`runtime` 只负责宿主 runtime 与生命周期集成；`export` 负责 Kotlin 到 JavaScript 与 TypeScript 的边界，不得演变为业务实现层。

依赖方向：`commonMain` 绝不依赖微信相关代码；`interop` 绝不依赖业务实现逻辑；平台代码可以实现平台无关代码定义的契约，反向依赖被禁止。

## 6. 已验证状态与命令

验证日期 2026-09-14：

| 命令 | 结果 |
| --- | --- |
| `./gradlew projects` | VERIFIED |
| `./gradlew clean build` | VERIFIED |
| `./gradlew :sdk:jsNodeTest` | VERIFIED |

```bash
./gradlew clean build
```

工具链：Gradle 9.3.1（来自仓库内置 Wrapper），项目 Kotlin 2.4.20（来自 `gradle/libs.versions.toml`），`kotlinx-coroutines-core` 1.11.0，验证环境为 JDK 25.0.2。项目未 pin JDK toolchain。始终使用 `./gradlew`（Unix）或 `gradlew.bat`（Windows），不要使用系统安装的 Gradle。

截至 2026-09-14 的当前状态：

- 状态为实验性 / pre-alpha。Bootstrap 已完成。
- module 列表仅有 `:sdk`。`examples/` 是集成宿主目录，不是 Gradle module。
- JavaScript target 配置为 `nodejs()`、`useCommonJs()`、`binaries.library()` 与 `generateTypeScriptDefinitions()`。
- `sdk/src/commonMain/.../api/MiniAppSdk.kt` 声明 `MiniAppSdk.VERSION = "0.1.0-SNAPSHOT"`，`commonTest` 对其断言。这就是当前全部 public 面。
- 生成的 TypeScript declaration 中没有任何正式 SDK export API，因为尚未实现 `@JsExport` 边界。
- 尚未实现任何 `wx` wrapper、生命周期 adapter、network/storage/auth adapter、callback 到 coroutine 的桥接，以及面向消费者的 JS/TS export API。

Node.js 只是本地 Kotlin/JS 构建与测试环境。预期生产宿主是微信小程序 JavaScript runtime，Node.js 测试通过并不代表微信小程序集成成功。这两类结果必须分别汇报。

当前工作副本中没有 `.git` 目录，因此没有可查阅的 git 历史。

## 7. 工作流程

1. 阅读第 2 节中与所改区域相关的文档。
2. 在源码与 Gradle 配置中确认当前状态，不要仅凭文档假设。
3. 做出满足任务的最小改动。
4. 使用 Gradle Wrapper 进行验证。
5. 更新受改动影响的 `-en` / `-ch` 文档配对；当 module、依赖、Kotlin 或 Gradle 版本、target、公共架构或当前能力发生变化时，一并更新 `docs/PROJECT_FACTS-en.md` 与 `docs/PROJECT_FACTS-ch.md`。
6. 按下述格式汇报。

## 8. 最终汇报格式

汇报内容包括：

- Changed
- Validation
- Known issues
- Documentation impact
- Next step

然后停止。
