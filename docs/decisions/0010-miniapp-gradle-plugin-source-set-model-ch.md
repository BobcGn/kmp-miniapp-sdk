# ADR 0010：Mini App Gradle 插件 source set 模型

[English](0010-miniapp-gradle-plugin-source-set-model-en.md)

- 状态：Accepted
- 日期：2026-09-17

## 背景

P1 消费者集成主线要求：普通 Kotlin Multiplatform 项目只需应用 `io.github.bobcgn.miniapp`，即可获得稳定的 `miniappMain` 与 `miniappTest` source set，且无需了解 SDK 内部的 Kotlin/JS target、CommonJS 产物、TypeScript 声明或产物复制流程。

在任何插件代码存在之前，BOB-83 要求在 Kotlin 2.4.20 与 Gradle 9.3.1 下用最小可执行 PoC 在三种 Kotlin Gradle Plugin 模型中做出选择：

- **方案 A** — 由消费者 build script 声明命名 Kotlin/JS target。
- **方案 B** — 只创建自定义 source set，背后没有 target 或 compilation。
- **方案 C** — 由插件拥有 Kotlin/JS compilation，并向消费者暴露 `miniappMain` / `miniappTest`。

PoC 位于 [`poc/kgp-model`](../../poc/kgp-model)，是一个独立的 Gradle build，SDK 构建不包含它。它的四个模块与两个校验任务（`reportKgpModel`、`checkMiniAppModel`）是本决策的可复现证据；它们是实验性工具，不是生产代码。

PoC 通过实际执行确认的事实：

- 命名 Kotlin/JS target 后，Kotlin Gradle Plugin 会依据 target 名称派生真实 source set。消费者看到 `miniappMain` 还是 `jsMain`，只由 target 名称决定。
- 没有 target 的自定义 source set 会被 Kotlin Gradle Plugin 报告为 `Unused Kotlin Source Sets`，且不被任何 compilation 消费。它们的 configuration 存在但是死配置：消费者可以向 `miniappMainImplementation` 添加依赖，而没有任何东西会编译它。
- Kotlin Gradle Plugin 没有受支持的公开 API 能把任意 source set 挂到既有 compilation 上。`KotlinCompilation.kotlinSourceSets` 与 `allKotlinSourceSets` 是公开但只读的，而 compilation 赖以构建的 source-set 容器是 `internal`。唯一公开的杠杆是 target 名称。
- 把某个 Kotlin 源码目录加到既有 compilation 上是可行的（`KotlinSourceSet.kotlin.srcDir`），但这不会产生名为 `miniappMain` 的 source set，因此无法满足命名要求。

## 决策

**插件创建一个以平台命名（`miniapp`）的 Kotlin/JS target，并让 Kotlin Gradle Plugin 依据该名称派生面向消费者的 source set。**

- 插件调用 `kotlin.js("miniapp")`。Kotlin 2.4.20 随即创建名为 `miniappMain` 与 `miniappTest` 的 `KotlinSourceSet`，二者都由真实 compilation 拥有。
- 插件把该 target 的 Kotlin/JS 输出配置作为当前微信宿主的实现细节。消费者不写 `js { }`、`nodejs()`、`useCommonJs()`、`binaries.library()`、`generateTypeScriptDefinitions()` 或 `sourceSets.create(...)` 中的任何一项。
- `commonMain → miniappMain` 与 `commonTest → miniappTest` 由 Kotlin Gradle Plugin 产生，而不是由插件代码产生。PoC 中它们在命名 target 模型下成立，在仅 source set 模型下不存在。
- 插件拥有 target 名称。以后改名会重命名消费者的 source set，因此 target 名称属于插件的兼容契约，尽管它不是模型中最友好的名字。

### 稳定的消费者表面

消费者可以依赖的一切都由平台名 `miniapp` 派生：

- Source set：`miniappMain`、`miniappTest`，与 `commonMain`、`commonTest` 并列。
- 生命周期任务：`miniappTest`。
- 执行任务：`miniappNodeTest`，在 Node.js 上运行 `miniappTest` compilation 的测试。
- 编译任务：`miniappMain` 对应 `compileKotlinMiniapp`，`miniappTest` 对应 `compileTestKotlinMiniapp`。
- 依赖 configuration：`miniappMainApi`、`miniappMainImplementation`、`miniappMainCompileOnly`、`miniappMainRuntimeOnly`，以及对应的 `miniappTest*` 集合。
- 产出：`miniappMainClasses`、`miniappJar`、`miniappSourcesJar`，以及该 target 的发布 element（`miniappApiElements`、`miniappRuntimeElements`）。

### 内部实现细节

- compilation 名称保持为 `main` 与 `test`。消费者永远不会看到带 `miniapp` 前缀的 compilation 名称，插件也不应自行发明一个。
- Kotlin/JS 产物流水线（`miniappNodeProductionLibraryDistribution`、`miniappNodeDevelopmentLibraryDistribution`、`miniappPublicPackageJson`、`miniappProductionLibraryValidateGeneratedByCompilerTypeScript`）是插件在 BOB-81 中的组装职责，不是消费者契约。
- Kotlin/JS target 的编译器设置（`useCommonJs`、library binary、TypeScript 声明生成）是宿主分发细节，不是平台语义。未来的宿主可能需要不同设置，因此插件必须把它们留在宿主边界内，而不能当作 Mini App 自身的属性。
- `miniappNodeTest` 的命名确实暴露了本地测试运行环境是 Node.js。这一点被接受：Node 是本地 Kotlin/JS 测试环境而非部署宿主，而在缺少 source-set 挂载 API 的情况下也没有更好的公开名称可用。

## 被否决的方案

- **方案 A — 消费者 build script 中声明的命名 Kotlin/JS target。** 否决。它产生的模型与选定方案相同，但要求消费者编写 BOB-81 规定必须隐藏的 Kotlin/JS 接线，并且只通过 10 项模型校验中的 9 项。机制是对的，把它留在消费者 build script 中是错的。它的价值在于作为对照实验，证明选定机制在没有任何插件时也能成立。
- **方案 B — 只创建自定义 source set。** 否决，而且它是错误的模型，而非不完整的模型。Kotlin Gradle Plugin 把 `miniappMain` 与 `miniappTest` 报告为未使用 source set，没有任何 compilation 消费它们，不存在 `miniappTest` 任务，唯一执行的测试任务是 `jsNodeTest`。它在 10 项模型校验中失败 6 项。这就是「IDE 看得到、Gradle 不编译」的假平台：`src/miniappMain/kotlin` 中的源码被静默忽略，`miniappMainImplementation` 中的依赖被静默废弃。
- **方案 C 的隐藏 target 变体 — 插件保留内部 `js` target，同时仍然暴露 `miniappMain` / `miniappTest`。** 以「受支持 API 无法实现」为由否决。模块 `model-c2` 编译并运行了它：source set 仍被报告为未使用，`jsMain` / `jsTest` 仍对消费者可见，尽管消费者 build script 本身是干净的，它仍在 10 项模型校验中失败 5 项。要让它成立，必须依赖 Kotlin Gradle Plugin 内部的 source-set 容器，而本项目不会依赖它。

## 后果

- `miniappMain` 与 `miniappTest` 是拥有真实 compilation 的普通 Kotlin source set，因此 IntelliJ IDEA 无需任何插件专属 IDE 支持即可识别它们，其中的代码补全走标准 Kotlin Multiplatform 机制。这仍需人工 IDEA 验收；PoC 证明的是 Gradle 模型，不是 IDE 模型。
- 消费者的 `commonMain` 参与 `miniapp` compilation，因此 BOB-76 的 `commonMain → miniappMain` 要求由 Kotlin Gradle Plugin 满足，而不是由插件必须维护的接线满足。
- Kotlin/JS 能力范围受 Kotlin Gradle Plugin 对 JS target 提供的功能约束。不存在独立的 Mini App 平台类型、独立编译器，也无法为 `miniappMain` 单独建立 compilation；插件是一个 Kotlin/JS 项目集成器，不得暗示自己超出这个范围。
- 消费者项目中新增第二个 JavaScript target 未被本 PoC 覆盖。同一项目中的两个 Kotlin/JS target 受 Kotlin Gradle Plugin 自身的 target 消歧规则约束，因此插件不得在已存在 target 时静默再创建一个。
- 插件继承且无法完全隐藏的已知 Kotlin Gradle Plugin 行为：
  - 默认层级模板会在 `commonMain` / `commonTest` 与 target 自身 source set 之间插入分组 source set `webMain` 与 `webTest`。这不是 `miniapp` target 特有的：现有 `:sdk` 模块在 `jsMain` / `jsTest` 旁边本来就有 `webMain` 与 `webTest`。希望去掉它们的消费者可以设置 `kotlin.mpp.applyDefaultHierarchyTemplate=false`，PoC 确认这会移除它们并让 `miniappMain` 直接依赖 `commonMain`；插件无法替消费者设置这个项目级属性，也不应尝试。
  - Kotlin/JS 构建会在消费者项目根目录创建 `kotlin-js-store/` 与 `package.json`。插件文档必须预期它们存在，而不是把它们当成生成垃圾。
- 该模型与 configuration cache 兼容。`:model-c:check` 与 PoC 的模型校验任务成功存储并复用了 configuration cache 条目，且在 `--warning-mode all` 下，`useCommonJs()`、`binaries.library()`、`generateTypeScriptDefinitions()` 在 Kotlin 2.4.20 上未产生任何弃用警告。在 task action 内部从 `Project` 读取 Kotlin 模型是不兼容 configuration cache 的；PoC 改在 `gradle.projectsEvaluated` 中计算模型并只捕获纯值，这是 BOB-79 测试套件应采用的模式。
- 本决策对消费者集成主线的约束：
  - **BOB-78** 负责创建 target，不创建 compilation 或 source set。
  - **BOB-76** 不得手工搭建层级。`commonMain` 与 `commonTest` 的边由 Kotlin Gradle Plugin 产生，应当被断言而不是被构造。
  - **BOB-82** 把 SDK 接入 `miniappMain`，即真实 compilation 的 source set。它不得把 SDK 接入孤立的 configuration —— 那正是方案 B 产生的失败形态。
  - **BOB-81** 挂接 `miniapp` target 的 Kotlin/JS 产物流水线。其输出任务必须以平台命名，消费者绝不能调用 `useCommonJs()` 或 `generateTypeScriptDefinitions()`。
  - **BOB-84** 配置当前微信宿主。Kotlin/JS 输出形态属于该宿主配置，而不属于平台身份，因此后续宿主可以在不改名 `miniappMain` / `miniappTest` 的前提下改变它。
  - **BOB-79** 必须断言每个 source set 的所属 compilation，而不能只断言名称存在与 `dependsOn`，因为在被否决的模型中「存在性」断言同样会通过。
