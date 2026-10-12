# CLAUDE.md

[English](CLAUDE.md)

面向 Claude Code 的本仓库操作指南。

本文件汇总仓库规则，使其在会话开始时自动载入。它不替代规则本身。根目录 `AGENTS.md` 以及嵌套的 `sdk/AGENTS.md`、`examples/AGENTS.md` 仍然具有权威性，本文件不得与其冲突。

## 1. 本项目是什么

`kmp-miniapp-sdk` 是一个 Kotlin Multiplatform client runtime，用于跨 Mini App 平台共享客户端行为、宿主能力与 presentation state，同时保持渲染由宿主原生负责。当前微信桥接由 Kotlin/JS 承载。

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

它是一个 SDK / library。后端拥有业务事实，Kotlin 拥有客户端行为，宿主拥有渲染 —— 见 [ADR 0011](docs/decisions/0011-presentation-state-shared-rendering-host-native-ch.md)。它不是 application、UI framework、Kuikly replacement、Compose renderer、Virtual DOM 或 WXML replacement，并且它从不渲染。

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

源码与文档或不同文档冲突时，按 AGENTS.md 第 2 节的层级处理，向用户报告差异，不悄悄修改文档来掩盖冲突。

## 4. 不可违反的规则

**范围纪律。** 在没有明确任务的情况下，不要添加 Android、iOS、JVM 或 Wasm target；不要添加 Compose、Ktor、serialization 或依赖注入框架；不要添加 npm 或 Maven 发布；不要添加投机性的 Gradle module。

**UI 与渲染边界。** runtime SDK 与未来的 Presentation Core 不得依赖 Compose、其他 UI framework，或 WXML、WXSS 这类宿主 markup。Compose 是客户端 UI consumer。Mini App host integration 只做宿主状态与宿主事件的 binding，从不渲染。`./gradlew :kmp-miniapp-sdk:checkArchitectureBoundaries` 强制其中的依赖部分，并且是 `:kmp-miniapp-sdk:check` 的一部分；见 `AGENTS.md` 第 10 节与 [ADR 0011](docs/decisions/0011-presentation-state-shared-rendering-host-native-ch.md)。

**凭据安全。** 不提交 AppID 密钥、token、证书或其他凭据；示例与测试使用占位值。

**破坏性操作。** 删除文件、重写历史或批量改写前，说明影响范围并取得用户确认。

**最小改动。** 只做当前任务所需的最小改动。不要因为"以后可能有用"而添加基础设施。

**构建权威。** 任何"构建通过"的结论都必须由真实的 Gradle Wrapper 调用支撑。IDE 状态不能作为证据。每项改动至少有一条适合其范围的可执行验证命令，并报告结果及未运行的检查。

**计划不等于事实。** 不要把路线图条目当作已交付的工作来陈述。

**公共 API 纪律。** 项目已启用 `explicitApi()`。不要随意新增 public API。每个 public 声明都需要显式可见性与类型、清晰的 SDK 职责，以及相应的测试。不要仅仅为了让测试更好写而扩大 public 面。

**JS 边界。** `dynamic`、`external` 和 `js()` 绝不能进入 `commonMain`。原始 JavaScript interop 应尽可能收窄，最好放在 `jsMain/.../wechat/interop` 下。

**文档同步。** 面向人的文档使用同步的 `-en.md` / `-ch.md` 配对；权威文件 AGENTS.md 与有意采用不同文件名的 CLAUDE.md / CLAUDE-ch.md 指南配对除外。任一语言发生变化时，必须在同一任务中更新其对应版本，且事实、结构与意图保持一致。本文件同样适用。

**ADR 是历史记录。** `docs/decisions/` 中已接受的决策是历史档案。要改变某个决策，应新增一个取代它的 ADR，而不是改写原记录。

**分支与 PR 工作流。** 修改跟踪文件前检查分支与工作区，切换到任务分支；禁止在默认/主分支上开发或提交。所有仓库修改（含文档和规则）都通过 PR 进入默认分支。保留无关改动，记录分支、提交、PR 和验证；访问受阻时说明具体阻碍，不绕过工作流。见 `AGENTS.md` 第 11 节。

**PR 与 Multica 正文。** 按三个章节依次说明：做了什么或说明了什么；为何该动作来自已确认需求或已观察问题；证据与验证，包括已证明结果、待执行检查、环境和限制。Multica 正文使用中文，并另列真实的 `BOB-123 -> BOB-124` 进度链，区分前置依赖、并行工作、历史链与外部阻塞。已取消或暂缓任务不作为活动关卡。Multica 记录用户确认的工作，不发起需求或开发方向。正文维护使用 `--no-start`，保留元数据与评论，并回读核对。见 `AGENTS.md` 第 12 节。

**不要自动进入下一阶段。** 完成当前任务并汇报，不要自动开始路线图中的下一个条目。

## 5. 目录结构

```text
AGENTS.md                    仓库规则
CLAUDE.md / CLAUDE-ch.md  本操作指南
README-en.md / README-ch.md
docs/                        PROJECT_FACTS、ARCHITECTURE、DEVELOPMENT、TESTING、ROADMAP、decisions/（ADR）
gradle/libs.versions.toml    依赖与插件版本的唯一来源
settings.gradle.kts          include :kmp-miniapp-sdk 与 :miniapp-gradle-plugin
sdk/                         Kotlin/JS runtime SDK module
miniapp-gradle-plugin/       io.github.bobcgn.miniapp Gradle 插件骨架
examples/                    集成宿主；不是 Gradle module
```

`sdk/src` 内部：

- `commonMain` —— 平台无关的 API、model、error 与共享逻辑。不得出现 `wx`，不得出现 DOM 或 Node API，不得出现 `dynamic` / `external` / `js()`。
- `commonTest` —— 共享测试。
- `jsMain/kotlin/io/github/bobcgn/miniapp/host/wechat/` —— `interop`、`adapter`、`runtime`、`export`，以及 `WechatHost` 实现。
- `jsTest` —— JavaScript 测试。

`jsMain/.../host/wechat` 各层职责：`interop` 只描述底层 JavaScript 与 `wx` 契约，不放置业务逻辑；`adapter` 负责类型转换、错误映射与异步适配，不承担 UI 职责；`runtime` 只负责宿主 runtime 与生命周期集成；`export` 负责 Kotlin 到 JavaScript 与 TypeScript 的边界，不得演变为业务实现层。

依赖方向：`commonMain` 绝不依赖微信相关代码；`interop` 绝不依赖业务实现逻辑；平台代码可以实现平台无关代码定义的契约，反向依赖被禁止。

## 6. 已验证状态与命令

验证日期 2026-09-14：

| 命令 | 结果 |
| --- | --- |
| `./gradlew projects` | VERIFIED |
| `./gradlew clean build` | VERIFIED |
| `./gradlew :kmp-miniapp-sdk:jsNodeTest` | VERIFIED |

```bash
./gradlew clean build
```

工具链：Gradle 9.3.1（来自仓库内置 Wrapper），项目 Kotlin 2.4.20（来自 `gradle/libs.versions.toml`），`kotlinx-coroutines-core` 1.11.0，验证环境为 JDK 25.0.2。项目未 pin JDK toolchain。始终使用 `./gradlew`（Unix）或 `gradlew.bat`（Windows），不要使用系统安装的 Gradle。

截至 2026-09-15 的当前状态：

- 状态为实验性 / pre-alpha。Bootstrap 已完成，所有已实现的 capability 均已通过微信开发者工具验证，并在适用处通过真机验证。唯一缺口是权限的 `NotRequested` 状态：所用账号无法复现，改由自动化测试覆盖。
- module 列表为 `:kmp-miniapp-sdk` 与 `:miniapp-gradle-plugin`。`examples/` 是集成宿主目录，不是 Gradle module。插件注册 `miniapp` target 及其 Node.js test run，由此提供由 compilation 拥有的 `miniappMain` / `miniappTest` 并让 `miniappTest` 真正执行，以公共坐标 `io.github.bobcgn:kmp-miniapp-sdk` 把 runtime SDK 接入 `miniappMain`，由 `assembleMiniAppBundle` 把宿主 bundle 重新发布到 `build/miniapp/bundle/`，并由 `miniapp { wechat { } }` extension 配置该位置；不暴露其他构建设置。
- JavaScript target 配置为 `nodejs()`、`useCommonJs()`、`binaries.library()` 与 `generateTypeScriptDefinitions()`。
- `sdk/src/commonMain/.../api/MiniAppSdk.kt` 声明 `MiniAppSdk.VERSION = "0.1.0-SNAPSHOT"`，`commonTest` 对其断言。
- `commonMain` 包含 `MiniAppHost` / `HostPlatformApi`、`HostVersion`、`CapabilityKey` / `CapabilitySupport`（含 `Supported` / `Unsupported` / `VersionDependent` / `PermissionDependent` 四态与 `requireSupported` guard）、带 `StorageCapabilityProvider` 的 `MiniAppStorage`、带 `NetworkCapabilityProvider` 的 `MiniAppHttpTransport`、带 `LifecycleCapabilityProvider` 的 `MiniAppLifecycle`、带 `PermissionCapabilityProvider` 的 `MiniAppPermissions` 及其 `PermissionKey` / `PermissionState` 模型、`MiniAppException`，以及 internal 的 `awaitHostCallback` primitive。
- 微信 interop 覆盖 typed `login`、`showToast`、Storage、`request`、三个页面栈导航契约、带 presence guard 的 runtime inspection 成员，以及三个权限方法与原始授权 map reader。Storage、微信客户端 login、HTTP transport、导航与权限生命周期均有 adapter；`showToast` 仍仅为 interop contract。`WechatAppLifecycle`、`WechatPageLifecycle`、`WechatRuntimeInfo` 与能力目录表及 gate 位于微信 `runtime` package，`WechatPermissions` 与 `WechatPermissionScopes` 位于其 `adapter` package；`WechatPermissionScopes` 是微信 scope 字符串唯一存在的地方，当前只映射麦克风权限。
- 只有 App 级生命周期是公共 capability。Page 级生命周期、导航与 runtime 自身描述属于微信专属，仅通过 `WechatPlatformApi` 可达。Capability support 由正在运行的宿主回答，因此对某次构建并不恒定。
- 编译器生成的 TypeScript declaration 包含版本、Storage、微信 login、HTTP transport、生命周期、导航、capability support 与权限 exports，手工维护的 CommonJS wrapper 将其暴露为扁平函数。

Node.js 只是本地 Kotlin/JS 构建与测试环境。预期生产宿主是微信小程序 JavaScript runtime，Node.js 测试通过并不代表微信小程序集成成功。这两类结果必须分别汇报。

当前工作副本中存在 Git 仓库。改动可能尚未提交，因此在假设文件与最后一次提交一致之前，请先检查 `git status`。

## 7. 工作流程

1. 按 AGENTS.md 第 14 节采用完整、轻量或明确跳过的需求对齐流程，并阅读第 2 节中的相关文档。
2. 在源码与 Gradle 配置中确认当前状态，不要仅凭文档假设。
3. 修改前检查工作区并切换到任务分支，然后做出满足任务的最小改动。
4. 执行适合本次改动的验证命令；构建与测试验证使用 Gradle Wrapper。仅改文档时执行文档检查，不适用 Gradle 时如实写“未运行”。
5. 更新受改动影响的 `-en` / `-ch` 文档配对；当 module、依赖、Kotlin 或 Gradle 版本、target、公共架构或当前能力发生变化时，一并更新 `docs/PROJECT_FACTS-en.md` 与 `docs/PROJECT_FACTS-ch.md`。
6. 通过 PR 提交仓库修改，正文使用三个证据章节，再按下述格式汇报；未经授权不合并。

## 8. 最终汇报格式

汇报内容包括：

- Changed
- Validation
- Known issues
- Documentation impact
- Next step

然后停止。

## 9. 需求对齐

### 适用范围

- 新能力、公共 API 变更、多文件或多模块改动、需求模糊或存在多种可行方案时，采用完整流程。
- 改文案、错别字或描述明确的 bug，采用轻量流程：用一两句话说明理解的目标与范围，取得用户同意后再修改。
- 用户说“直接做”／“skip alignment”时，根据现有信息先输出边界卡，再执行，无需再次确认。“问题够了”只结束澄清，不代表允许执行。

### 阶段一：澄清

- 保持只读：不修改受版本控制的文件，不创建分支。追问为什么需要这项改动，直到根本目标明确。
- 区分按 AGENTS.md 第 2 节核实的事实与未验证假设。质疑更简单的动作或不修改是否已能满足目标，并按 AGENTS.md 第 3 节检查范围纪律。
- 每次只问 1～3 个问题。需要用户决策时提供 2～3 个选项，说明优缺点和推荐理由；理由必须适合当前目标，不能只凭惯例。
- Roadmap、TODO、注释不构成需求证据。直接指出风险或不合理之处。
- 用户说“问题够了”时停止追问，根据现有信息输出边界卡。

### 阶段二：边界卡

采用 AGENTS.md 第 12 节的三个章节，使边界卡可复用为 PR 正文：

1. **做什么。** 用一句话说明根本目标，列出已核实事实、假设及每条假设的验证或验证前处理方式，给出最小方案和明确不做的范围。
2. **为什么。** 说明用户确认的需求，以及为什么该方案是满足目标的最小动作。
3. **证据与验证。** 列出可检验的验收标准和拟运行的 Gradle Wrapper 命令。仅改文档时列出可执行的文档检查；不适用 Gradle 时明确写“未运行”。

除用户明确跳过对齐外，收到用户“确认”后才进入阶段三。

### 阶段三：执行

- 先按 AGENTS.md 第 11 节创建或切换到任务分支，再严格按确认的边界卡实现。不加功能，不顺手重构。
- 假设不成立、需要修改边界卡或源码与文档冲突时，停止并询问用户，再继续。
- 结束时按 AGENTS.md 第 13 节输出 Final Report。用户说“回到阶段一”时，边界卡作废，重新澄清。

### 沟通与记录

- 对用户使用中文，代码标识符和命令保留英文。技术术语首次出现时用一句话解释，一次只推进一小步。
- 确认的边界卡保存在 PR 正文中；涉及架构决策时记录到 docs/decisions。除非用户要求，不另建文档。
