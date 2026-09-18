# P1 WeChat Issues 落地顺序与 Claude 交接计划

更新日期：2026-09-16

## 1. 使用说明

本文档记录 Multica 项目「KMP小程序SDK」的 P1 微信能力计划，以及新增的 P1 一等 KMP Mini App 消费者集成紧急主线。当前范围包含原有 BOB-58 至 BOB-74 共 17 个 Issue，以及新增 BOB-75 至 BOB-85 共 11 个 Issue。以下 28 个条目与 Multica Issue 一一对应：每个 Issue 恰好对应一个独立条目，不合并、不拆分，也不把一个 Issue 的验收结果代替另一个 Issue。

执行每个 Issue 前，应重新读取该 Issue 在 Multica 中的最新描述。若本文档与 Multica 存在差异，以 Multica 中的 Goal、Scope、Out of Scope、Acceptance Criteria、Dependencies、Verification 和 Documentation Impact 为准。

通用关闭规则：实现、单元测试、契约测试、适用的微信开发者工具或真机验证，以及相关中英文文档同步全部完成后，才能将对应 Issue 标记为 Done。Node 或 Kotlin 测试通过不能代替微信真实宿主验证。

新增紧急主线的执行规则更严格：同一时间只允许一个关键路径子 Issue 进入 Ready 或 In Progress；前序没有完成并形成可核对证据时，后序保持 Blocked。该主线完成前不得自动进入 P2。

## 2. 紧急主线：一等 KMP Mini App 消费者集成

### 总目标：BOB-75 `[P1][URGENT][Release Blocker] Deliver first-class KMP Mini App consumer integration`

BOB-75 是这条紧急主线的父 Issue 和总体验目标，不代替任何子 Issue 的实现或验收。目标是让普通 KMP 消费者只应用 Kotlin Multiplatform 与 `io.github.bobcgn.miniapp` 插件，即可获得稳定的 `miniappMain`、`miniappTest`、正式 SDK runtime 依赖和微信可消费产物，无需理解仓库内部 Kotlin/JS、CommonJS 或手工复制流程。

目标消费形态：

```kotlin
plugins {
    kotlin("multiplatform")
    id("io.github.bobcgn.miniapp")
}
```

BOB-75 只有在 BOB-83、BOB-78、BOB-76、BOB-82、BOB-81、BOB-84、BOB-80、BOB-79、BOB-77 与最终 Gate BOB-85 均满足自身验收标准后才可关闭。现有 BOB-53 `buildMiniAppSdk` 是基础证据，不是这条消费者工作流的替代品。

### 紧急第 A 步：BOB-83 `[P1][URGENT] Validate Kotlin Gradle Plugin model for miniappMain / miniappTest`

先用最小 PoC 比较命名 Kotlin/JS target、自定义 source sets、以及 plugin 管理的 compilation/source-set hierarchy。在 Kotlin 2.4.20 与 Gradle 9.3.1 下验证 Gradle sync、IDEA 识别、`commonMain` 依赖、`miniappTest` 执行、任务命名和后续扩展性。必须记录选型、否决方案、原因和已知 KGP 限制；若决定会长期约束架构，则形成 ADR。不得降低 `miniappMain` / `miniappTest` 的硬性用户体验要求。

**本轮结论（2026-09-17）**：PoC 落于 `poc/kgp-model`（独立 Gradle build，根构建不包含它）。选定「插件管理、以平台命名的 Kotlin/JS target」—— 只有在它之下 `miniappMain` / `miniappTest` 才是由 compilation 拥有的真实 Kotlin source set。被否决：方案 A（消费者 build script 自行声明命名 JS target —— 机制相同，但把 BOB-81 要求隐藏的接线留给消费者）；方案 B（仅创建自定义 source sets —— KGP 报告 `Unused Kotlin Source Sets`，无 compilation 消费、无 `miniappTest` 任务，`miniappMainImplementation` 等 configuration 成为死配置）；方案 C 的隐藏 target 变体（保留内部 `js` target 同时暴露 `miniappMain` —— 公开 API 无法把任意 source set 挂到既有 compilation，`KotlinCompilationSourceSetsContainer` 为 internal）。决策、原因与 KGP 限制记录于 ADR 0010（中英同步），并在 ARCHITECTURE 第 7 节引用。

事实证据（Kotlin 2.4.20 / Gradle 9.3.1）：`model-c` 的 `miniappNodeTest` 实际执行 4 个测试（1 个来自 `commonTest`、3 个来自 `miniappTest`），`checkMiniAppModel` 10/10 通过；`model-b` 6/10 失败、`model-c2` 5/10 失败、`model-a` 因消费者 build script 含 `useCommonJs()` 等手工接线而在 1 项上失败；`poc/kgp-model` 的 `clean check` 通过；`:model-c:check` 成功存储并复用 configuration cache，`--warning-mode all` 未出现弃用警告；commit `6812814` 的 `clean check` 通过（`:kmp-miniapp-sdk:jsNodeTest` 598 个测试，无失败）。IDEA 识别仍属人工验收，本轮未执行，不得声称已通过。

**收尾（2026-09-17）**：BOB-83 已 **Done**，交付 commit `72fdb49 docs(gradle): decide miniapp source set model`。真实 KMP Demo（独立仓库，Kotlin 2.4.20 / Gradle 9.5.1，含 Android/iOS/JVM/Compose）补充验证：`:app:shared` 的 `miniappMain` / `miniappTest` 目录与其他平台 source set 对称，`compileKotlinMiniapp`、`compileTestKotlinMiniapp`、`miniappTest` 通过，`miniappNodeTest` 实际执行而非 SKIPPED。IDEA 人工识别由用户完成 Gradle Sync 后确认。跨模块约束：`app:shared(miniapp)` 依赖 `project(:core)` 时，`:core` 也必须提供 Mini App 变体，否则出现 6 个 variant resolution 错误 —— 该约束保留给 BOB-76、BOB-82、BOB-79 的诊断与测试。

### 紧急第 B 步：BOB-78 `[P1][URGENT] Bootstrap Mini App Gradle Plugin`

建立独立的 `io.github.bobcgn.miniapp` Gradle Plugin 骨架，检测 Kotlin Multiplatform 插件并建立 Mini App 平台集成入口。普通 KMP 项目必须能应用插件并完成 Gradle sync；缺少 KMP 插件时必须给出明确错误。插件只负责构建集成和开发体验，不承载微信 runtime API，也不在本步骤实现多 Host 或发布流程。

**本轮进展（2026-09-17）**：`:miniapp-gradle-plugin` module 已建立，插件 ID `io.github.bobcgn.miniapp`，实现类 `io.github.bobcgn.miniapp.gradle.MiniAppGradlePlugin`。应用 KMP 插件时只做一件事：调用 `kotlin.js("miniapp")` 注册平台 target（ADR 0010 的最小入口）；未应用 KMP 时在 `afterEvaluate` 抛出指明缺失插件与修复写法的错误。`:miniapp-gradle-plugin:test` 6 个测试全部通过（TestKit：可被普通 KMP fixture 应用、缺 KMP 时报错；契约：插件 ID→实现类、`Plugin<Project>`、插件产物不含微信类、WeChat host runtime 不在插件 classpath）。变异探针已执行：注释掉 `kotlin.js(...)` 后 KMP fixture 测试失败（`[commonMain, commonTest]` 缺少 `miniappMain`）。**未实现**：source set 提供（BOB-76）、SDK 依赖接线（BOB-82）、产物组装（BOB-81）、微信 DSL（BOB-84）、发布、多 Host、P2。本轮不提交 Git，交 Codex 审查，状态保持 `in_progress`。

**收尾（2026-09-17）**：BOB-78 已 **Done**，交付 commit `19e807e feat(gradle): bootstrap Mini App Gradle plugin skeleton`。Codex 审查后补齐了中文版 `PROJECT_FACTS-ch.md` 两处遗漏：§3 当前 Gradle Modules 未列出 `:miniapp-gradle-plugin`，§10 仍写着「Gradle plugin」。现中英文 §2 状态、§3 modules、§10 非能力、§11 命令四处一致。验证命令 `./gradlew --no-daemon --console=plain projects clean check :miniapp-gradle-plugin:test` BUILD SUCCESSFUL，插件测试 6 个 0 失败，`git diff --check` 通过。

### 架构纠偏：BOB-86 `[P1][紧急][架构纠偏] Backend 事实、KMP 客户端行为与宿主渲染边界`

**本轮进展（2026-09-17）**：审计结论是**当前不存在层级混合**：仓库内 Compose / `@Composable` / Material 命中 0 处代码（22 处 `compose` 字样全部是文档中的非目标声明），`UiState` / `Action` / `Store` / `Effect` / `Reducer` / `UseCase` 命中 0 处，`Renderer` / `setData` / `WXML` / `Virtual DOM` 在 `sdk/src` 与 `miniapp-gradle-plugin/src` 命中 0 处，`:kmp-miniapp-sdk` 的生产依赖只有 `kotlinx-coroutines-core`，`examples/` 不是 Gradle module 因此结构上不存在 SDK → app 反向依赖。

因此本轮不做代码搬迁，交付的是把边界固化：新增 [ADR 0011](docs/decisions/0011-presentation-state-shared-rendering-host-native-ch.md)（Backend owns business truth / Kotlin owns client behavior / rendering 保持宿主原生 / Compose 是 consumer），ARCHITECTURE 增加四层边界与责任矩阵（Auth、Payment、Network、Storage、Business Rule、UiState、Navigation、Permission、Rendering、Cache、Error、Lifecycle），AGENTS 与 CLAUDE 增加 UI 与渲染硬性约束，PROJECT_FACTS / ROADMAP 记录定位与 P2 规划且明确 Presentation Core **尚未实现**，并新增可执行的架构约束检查 `:kmp-miniapp-sdk:checkArchitectureBoundaries`（接入 `:kmp-miniapp-sdk:check`）。

已执行变异探针两例：向 `sdk/src` 加入 `import androidx.compose.runtime.Composable` → 检查失败并打印文件与行号；向 `:kmp-miniapp-sdk` 声明 `org.jetbrains.compose.runtime:runtime` → 检查失败。第二个探针暴露了初版的 group 前缀匹配缺陷（`org.jetbrains.compose.runtime` 不匹配 `org.jetbrains.compose:`），已修正为按 group 的点分前缀匹配。

### 紧急第 C 步：BOB-76 `[P1][URGENT] Provision miniappMain and miniappTest source sets automatically`

插件自动建立 `commonMain → miniappMain` 与 `commonTest → miniappTest`，消费者不得手写 `sourceSets.create(...)`。验收必须覆盖 IDEA 将两者识别为 Kotlin source sets、代码补全、`miniappMain` 复用 `commonMain`，以及 `miniappTest` 实际执行。当前 Host API 可以由 `miniappMain` 使用，但总体插件架构不得写死 `MiniApp == WeChat`。

**本轮进展（2026-09-17）**：插件在 BOB-78 的 `kotlin.js("miniapp")` 之上补上该 target 的 **Node.js test run** —— 这是让 `miniappTest` 拥有真正执行任务的关键一步；source set、compilation 与层级边仍全部由 Kotlin Gradle Plugin 派生，插件不手工创建任何 source set 或 `dependsOn` 边。Gradle TestKit 由 4 个测试扩展到 **8 个**：新增「插件提供 compilation-owned 的 miniappMain / miniappTest（含 owning compilation 与传递 dependsOn）」「miniappTest 实际执行自身测试并复用 commonTest 测试」「消费者已自行声明 miniapp target 时插件不重复创建且仍补齐 test run」。真实 consumer fixture 的 `miniappNodeTest` 执行了 2 个测试：一个只存在于 `miniappTest`，一个来自 `commonTest`，标识符带 `[miniapp, node]` 后缀。变异探针：移除 `nodejs()` 后 3 个测试失败。跨模块约束仍成立：消费者 `commonMain` 依赖另一个 KMP project 时，该 project 也必须应用插件，插件不提供回退，也不允许把依赖移出 `commonMain` 规避。**未实现**：SDK 依赖接线（BOB-82）、产物组装（BOB-81）、微信 DSL（BOB-84）。IDEA 识别属人工证据，待用户截图确认。

**验收（2026-09-17）**：BOB-76 已 **Done**。自动化：`./gradlew --no-daemon --console=plain projects clean check :miniapp-gradle-plugin:test --rerun-tasks :kmp-miniapp-sdk:checkArchitectureBoundaries` BUILD SUCCESSFUL，插件 8 个测试通过，SDK 598 个测试通过。人工 IDEA + 外部真实 KMP Demo 验收（Gradle 9.5.1 / Kotlin 2.4.20，含 Android、iOS、JVM、Compose）：`:core` 与 `:app:shared` 同时应用正式插件，IDEA 正确识别 `commonMain`、`commonTest`、`miniappMain`、`miniappTest`，`actual getPlatform()` 能识别 `commonMain` 的 expect/API，`miniappTest` 被识别为测试，Gradle Sync BUILD SUCCESSFUL。最终执行 `:core:compileKotlinMiniapp`、`:core:miniappTest`、`:app:shared:compileKotlinMiniapp`、`:app:shared:miniappTest` 全部 BUILD SUCCESSFUL（21s，configuration cache reused）；`:core:miniappTest` 为 NO-SOURCE 属正常结果（`:core` 当前无测试源码）。新增 JS target 后 Yarn lock 经 `kotlinUpgradeYarnLock` 正常更新。

### 紧急第 D 步：BOB-82 `[P1][URGENT] Automate Mini App SDK dependency wiring`

插件自动为 `miniappMain` 接入正式 SDK runtime 依赖，使消费者直接使用受支持公共 API，不必声明内部微信 artifact。验证依赖变体能够在 BOB-83 选定的 KGP 模型下解析，且不会把内部 implementation artifact 泄漏为消费者配置责任。

**本轮进展（2026-09-17）**：插件把运行时接入 `miniappMain` 的 **api configuration**，坐标是公共 module 坐标 `io.github.bobcgn:kmp-miniapp-sdk:<version>` —— 插件运行在消费者构建中，无法访问本仓库的 project path，因此不得也不曾写出 `project(":kmp-miniapp-sdk")`。`miniappTest` 经 source-set hierarchy 继承，不重复声明；`commonMain`、metadata 编译与其他 target 均不获得该依赖。实测依赖报告：`sdkDeclarations=[miniappMainApi]`、`sdkDeclaredCoordinates=[io.github.bobcgn:kmp-miniapp-sdk:0.1.0-SNAPSHOT]`、`metadataCompileClasspath=[]`、`miniappCompileClasspath` 与 `miniappTestCompileClasspath` 均解析到 SDK。

版本单一来源：`gradle/libs.versions.toml` 的 `miniapp` version 同时驱动 `:kmp-miniapp-sdk` 与 `:miniapp-gradle-plugin` 的 version，并由插件构建期生成 `miniapp-plugin-metadata.properties`，插件运行期从该资源读取坐标 —— 插件源码与测试中都不含版本字面量。SDK 尚未发布，因此 fixture 经 **composite build**（`includeBuild` 本仓库）解析该坐标，正式发布后由仓库解析同一坐标。

TestKit 由 13 个测试构成（含契约测试）：依赖接线位置、`commonMain`/metadata 不注入、另一个 target（JVM）不注入、`miniappMain` 编译公共 SDK API、`miniappTest` 运行使用 SDK 的测试、缺失 runtime 时错误指明坐标、插件 JAR 只含自身 Gradle 集成类且 classpath 无 SDK / 微信 runtime 类。变异探针：移除依赖接线后 4 个测试失败。

**正式公共坐标（2026-09-17 决定）**：runtime 的 artifact id 定为 `io.github.bobcgn:kmp-miniapp-sdk`，不再沿用 Gradle 目录名派生的 `io.github.bobcgn:sdk`。实现方式是保留目录 `sdk/`、把 settings 中的 project 名设为 `:kmp-miniapp-sdk`（`projectDir = file("sdk")`），因此 `outgoingVariants` 报告的 capability、composite build substitution 匹配的坐标与将来正式发布的坐标三者一致，都是 `io.github.bobcgn:kmp-miniapp-sdk`。实测 capability：`io.github.bobcgn:kmp-miniapp-sdk:0.1.0-SNAPSHOT`。

**版本单一来源（2026-09-17）**：`gradle/libs.versions.toml` 的 `miniapp` version 现同时驱动 `:kmp-miniapp-sdk` 与 `:miniapp-gradle-plugin` 的 version；SDK 的公共版本常量由 `:kmp-miniapp-sdk:generateSdkVersionSource` 生成（`GeneratedMiniAppSdkVersion.VALUE`），`MiniAppSdk.VERSION` 引用它，`MiniAppSdkTest` 断言二者一致。两例变异探针：把 catalog 改为 `9.9.9-PROBE` 后生成文件与编译产物 `kmp-miniapp-sdk-kotlin.js` 均变为该值（catalog 单向驱动，不存在可漂移的第二侧）；把 `MiniAppSdk.VERSION` 写死为 `0.0.0-stale` 后 `MiniAppSdkTest` 失败。两者随后均已还原。

**未实现**：微信产物组装（BOB-81）、Gradle DSL（BOB-84）、Maven Central / npm 发布。跨模块约束仍成立：consumer 的 `commonMain` 依赖没有 miniapp variant 的 KMP project 时保留明确的 variant resolution 失败，不提供回退。

### 紧急第 E 步：BOB-81 `[P1][URGENT] Automate WeChat-compatible Kotlin/JS compilation and packaging`

把消费者所需的 Kotlin/JS、CommonJS、TypeScript declarations、runtime dependencies 与微信兼容产物组装隐藏在插件后。提供一个稳定、已文档化的组装任务；消费者不得调用 `useCommonJs()`、`generateTypeScriptDefinitions()` 或手工复制 artifact。不得机械照搬 SDK 仓库自己的 `buildMiniAppSdk`，而应明确真实消费者契约。

**本轮进展（2026-09-17）**：插件把 Mini App target 的 Kotlin/JS 输出配置成宿主可消费形态（`nodejs()`、`useCommonJs()`、`binaries.library()`、`generateTypeScriptDefinitions()`），消费者不再手写其中任何一项；并新增稳定组装任务 **`assembleMiniAppBundle`**，把 Kotlin/JS production library 发布到 **`build/miniapp/bundle/`**。任务是 `Sync`，输入取自 distribution task 的**已声明输出**而非手写路径。

实测产物契约（TestKit fixture）：`<project>-miniapp.js`（消费者入口模块，含其编译代码与 `@JsExport` 导出）、`<project>-miniapp.d.ts`、`kmp-miniapp-sdk-kotlin.js`（runtime SDK 作为**独立模块**，而非内联）、`kotlin-kotlin-stdlib.js`、`kotlinx-coroutines-core.js`、`kotlinx-atomicfu.js`、`kotlin_org_jetbrains_kotlin_kotlin_dom_api_compat.js`、`package.json` 及各 `.js.map`。

两项审计结论：其一，Kotlin/JS library **只导出 `@JsExport` 声明**，其余被消除 —— 消费者的宿主侧表面就是其 `miniappMain` 中的导出，未被导出的代码不会进入 bundle；其二，runtime SDK 因声明了自己的输出模块名而成为独立 JS 模块，因此 bundle 中存在多个宿主需要加载的文件。二者均写入 DEVELOPMENT 与 ARCHITECTURE。

TestKit 由 13 个测试扩展到 **17 个**：新增稳定任务发现与产物契约、重复执行 UP-TO-DATE、编译失败时错误明确（含 `compileKotlinMiniapp` 与未定义符号）、configuration cache 存储与复用。两例变异探针：移除 bundle 任务注册后 4 个测试失败；移除 `generateTypeScriptDefinitions()` 后产物契约测试失败（缺 declaration）。两者均已还原。

**纠偏（2026-09-17，外部 Demo 验证后）**：外部 Demo 的 `assembleMiniAppBundle` 可以执行，但 bundle 约 12 MB，包含 `androidx-compose-*`、`compose-multiplatform-*`、`skiko-kjs.js`、`skiko.wasm`、`kotlinx-browser.js` 与 Compose lifecycle/navigation/resource runtime。根因不是插件复制了文件，而是该 Demo 的 `app/shared/commonMain` 含 Compose UI，而 `miniappMain` 继承 `commonMain`，于是整条 UI stack 进入 Mini App production distribution。

三条处理，均不改 Demo、也不做文件过滤：

1. **新增 `checkMiniAppHostBoundary`**，作为 `assembleMiniAppBundle` 的前置任务。它解析 `miniappRuntimeClasspath`（正是 distribution 据以构建的那一组依赖），在其携带 Compose（`org.jetbrains.compose` / `androidx.compose` / 明确的 `org.jetbrains.androidx` Compose 集成模块）、Skiko（`org.jetbrains.skiko`）、`kotlinx-browser` 或 `kotlinx-html` 时失败，并说明 Compose 属于 client UI、Mini App 宿主使用 WXML/WXSS 等原生 UI。非 Compose 的 lifecycle、saved-state 与 navigation primitives 不受该规则误伤。这是**依赖规则而不是文件规则**：Kotlin/JS distribution 中的文件通过 `require()` 彼此引用，删除「看起来像 renderer」的文件只会把构建失败换成加载失败。
2. **文档措辞纠正**：不再称输出为 host-ready / WeChat-compatible / 可直接交给微信加载；统一改为 compiler-managed Mini App distribution、stable host-integration input、awaiting external host validation。消费者架构前置条件写明：`miniappMain` 可继承共享行为与 state，但不得继承 Compose UI / Skiko / 浏览器 renderer；Compose UI 必须位于不作为 `miniappMain` 父 source set 的客户端 module 或 source set。
3. **测试区分两个结论**：「bundle 不含 `.wxml`/`.wxss`」只证明插件不生成宿主 markup；「distribution 不携带 renderer」是关于依赖图的结论，由 host-boundary 检查断言。二者不再互相冒充。

TestKit 由 17 个测试扩展到 **19 个**：新增 host-boundary 分类器测试（Compose / Skiko / 明确的 `org.jetbrains.androidx` Compose 集成模块 / `kotlinx-browser` / `kotlinx-html-js` 被拒；非 Compose 的 lifecycle / saved-state / navigation primitives 以及 `kotlinx-coroutines-core`、`atomicfu`、`kotlin-stdlib`、`kotlin-test`、`kotlin-dom-api-compat` 不受误伤）与「Compose 泄漏时在产出 bundle 前失败」的集成测试（fixture 在 `commonMain` 声明 `org.jetbrains.compose.runtime:runtime`，断言失败信息含 "client renderer" 与坐标，且 bundle 目录不存在）。

另记录一项消费者侧摩擦（归属 BOB-80 / BOB-77，不在本轮解决）：应用插件会新增一个 Kotlin/JS target，从而改变 npm 依赖集合，已有 yarn lock 的工程需要执行一次 `kotlinUpgradeYarnLock`；Kotlin Gradle Plugin 不会自行覆盖已有锁文件。

**验收收尾（2026-09-17）**：外部真实 KMP Demo 完成正反两条证据。不含 Compose 的 `:core` 执行 `checkMiniAppHostBoundary` 与 `assembleMiniAppBundle` 成功，产出 1.5 MB 的 `core/build/miniapp/bundle`，含消费者 `.js` / `.d.ts`、SDK runtime、stdlib、coroutines、atomicfu 与 `package.json`，不含 Compose、Skiko、browser 或 HTML runtime；含 Compose UI 的 `:app:shared` 则在组装前被 `checkMiniAppHostBoundary` 准确拒绝，列出 Compose-specific AndroidX、Compose、Skiko 与 `kotlinx-browser` 坐标。分类器同时通过契约测试证明不会误伤非 Compose 的 lifecycle、saved-state 与 navigation primitives。BOB-81 的 Gradle 组装与边界验收完成；真实微信宿主加载仍属 BOB-85 Consumer Bridge release gate，不由本 Issue 冒充声称。Mini App Gradle DSL（BOB-84）仍未实现，插件不做上传、发布或宿主部署。

### 紧急第 F 步：BOB-84 `[P1][URGENT] Add Mini App Gradle extension and WeChat host configuration`

提供最小且可扩展的 Gradle DSL，候选形态为 `miniapp { wechat { ... } }`。DSL 只承载真正属于构建的配置，不把业务配置整体迁入 Gradle。架构必须表达 Mini App Platform → 当前 Host WeChat，并允许未来新增 Host，但本步骤不实现支付宝、Telegram 或多 Host source-set hierarchy。

**本轮进展（2026-09-17）**：插件注册 `miniapp` extension，唯一宿主为 `wechat`。类型层级为 `MiniAppExtension`（平台）→ `WeChatHostConfiguration`（宿主）→ 继承 `MiniAppHostConfiguration`，因此新增宿主是「新增子类型 + 新增访问器」，而不是把微信类型加宽，也不是根级 `wechatXxx` 属性。

DSL 只承载一个真正属于构建的字段：`wechat.bundleDirectory`（`DirectoryProperty`，默认 `build/miniapp/bundle`，即 BOB-81 之前的默认路径），`assembleMiniAppBundle` 读取该 Provider 作为 `Sync` 目标。`checkMiniAppHostBoundary` 与 Kotlin/JS 输出形态不受 DSL 影响 —— 前者是平台级保证，后者由插件决定而非消费者决定。

新增一条校验并附理由：`bundleDirectory` 必须是项目内目录。理由是 `Sync` 会删除目标中 bundle 不含的文件，指向项目之外（或项目目录本身）会删除而非组装。失败信息给出字段、两个路径与期望值。写入项目内的小程序目录仍被允许。

兼容性决定：宿主块与字段全部可选，未配置 DSL 时行为与 BOB-81 完全一致（默认目录），不要求消费者显式声明 `wechat { }`。理由：当前只有微信一个宿主，要求显式声明只增加噪音，且没有可选的第二宿主可供选择。

TestKit 由 19 个测试扩展到 **25 个**：DSL 配置实际改变 bundle 写入位置（并断言默认目录未被写入）、规范的 `miniapp { wechat { ... } }` 在 Groovy fixture 中编译并执行、项目外目录被拒绝且错误可操作、重复应用插件不产生第二个 extension、以及微信是宿主配置而非平台 extension 的类型断言；另有目录校验的单元测试。变异探针：让 bundle task 忽略自定义目录后，只有 DSL 接线测试失败。

**验收收尾（2026-09-17）**：`:miniapp-gradle-plugin:test` 的 25 个测试全部通过，`:kmp-miniapp-sdk:check` 与 `buildMiniAppSdk` 通过。外部真实 KMP Demo 的 Compose-free `:core` 显式配置 `miniapp { wechat { bundleDirectory.set(layout.buildDirectory.dir("verified-miniapp/bundle")) } }` 后，`:core:assembleMiniAppBundle` 构建成功，产物实际写入自定义目录且包含消费者模块、声明文件、SDK runtime 与 Kotlin/协程运行时。该证据证明 DSL 在独立消费者构建中生效，而不只是 TestKit fixture 内可编译。BOB-84 的最小宿主 DSL、兼容默认值和安全目录约束验收完成。

**未完成**：真实微信宿主加载（BOB-85）。业务与后台配置不进 Gradle，属设计而非缺失。

### 紧急第 G 步：BOB-80 `[P1][URGENT] Create real consumer KMP integration fixture`

建立真正独立、普通的 KMP consumer fixture，而不是 SDK 内部 demo。Fixture 只应用插件便可编译 `miniappMain`、复用 `commonMain`、运行 `miniappTest`、自动解析 runtime 依赖，并让微信 Host 消费最终产物。Android/iOS 仅在证明与普通 KMP target 共存确有必要时加入。

**本轮进展（2026-09-17）**：夹具位于 `fixtures/miniapp-consumer/`，是与本仓库并列的**普通 KMP 构建**，不是 SDK 内部 demo 或 `include(":...")` 子模块：它有自己的 `settings.gradle.kts` 与 `gradle.properties`，`build.gradle.kts` 只声明 `plugins { kotlin("multiplatform"); id("io.github.bobcgn.miniapp") }`、`commonTest` 的 `kotlin("test")` 以及 `miniapp { wechat { bundleDirectory.set(...) } }`。夹具中**没有** `sourceSets.create`、没有 target / compilation 配置、没有 `useCommonJs` / `moduleKind` / webpack、没有 `project(":kmp-miniapp-sdk")` 或任何内部 artifact 声明、没有绝对路径。插件与 runtime 目前尚未发布，因此复合构建（`pluginManagement { includeBuild("../..") }` 加 settings 级 `includeBuild("../..")`）是本仓库内唯一的接入手段；settings 级 `includeBuild` 负责把公共坐标 `io.github.bobcgn:kmp-miniapp-sdk` 替换为本仓库 project，这正是夹具不依赖本仓库结构、只依赖公共坐标的证明方式。

已执行的自动化证据：

| 命令 | 结果 |
| --- | --- |
| `./gradlew -p fixtures/miniapp-consumer clean miniappTest assembleMiniAppBundle verifyConsumerContract`（由根任务 `verifyMiniAppConsumer` 驱动） | 从 clean 通过；`miniappTest` 实际执行 **5** 个测试（`consumer.SharedTest` 2 个来自 `commonTest`、`consumer.MiniAppTest` 3 个只存在于 `miniappTest`），标识符后缀均为 `[miniapp, node]`，0 失败 |
| 同上，默认 bundle 目录 | `fixtures/miniapp-consumer/build/miniapp/bundle`，**14** 个文件；`consumerContract.verified=true` |
| `-PminiappBundleDirectory=host/miniprogram/libs` | bundle 实际写入 `fixtures/miniapp-consumer/host/miniprogram/libs`，**14** 个文件；DSL 的自定义目录确实生效，而不是仅可编译 |
| `verifyConsumerContract` 断言 | 恰好一个 `.d.ts`（`miniapp-consumer-miniapp.d.ts`）、`miniapp-consumer-miniapp.js`、`kmp-miniapp-sdk-kotlin.js`、`kotlin-kotlin-stdlib.js`、`kotlinx-coroutines-core.js`、`package.json`；无 `.wxml` / `.wxss`；无 Compose / Skiko / `kotlinx-browser` 文件；消费者模块 JS 含 `hostGreeting`、`sdkVersion`、`Hello,` 与版本号 |
| `node scripts/host-smoke.cjs`（由 `verifyMiniAppConsumerHostSmoke` 驱动） | 以宿主相同的方式 `require()` bundle：`fixture.greeting=Hello, WeChat, from commonMain`、`fixture.counted=5`、`fixture.sdkVersion=0.1.0-SNAPSHOT`、`fixture.result=PASS` |
| 夹具配置缓存 | 首次 `Configuration cache entry stored`，再次运行 `Reusing configuration cache` |

微信 Host 位于 `fixtures/miniapp-consumer/host/`：`project.config.json`（`miniprogramRoot: miniprogram/`）、`miniprogram/` 下的 `app.json` / `app.js` / `pages/index/*`，页面通过 `require` 加载 `libs/` 下的 bundle 并调用 `hostGreeting` / `countUpTo` / `sdkVersion`。**宿主侧 UI 只有 WXML 与 WXSS，且全部留在宿主内**，不进入 SDK、插件或消费者 Kotlin。

**验收收尾（2026-09-17）**：用户先执行 `verifyMiniAppConsumerHostBundle`，得到自定义宿主目录、14 个文件与 `consumerContract.verified=true`，随后在微信开发者工具基础库 3.17.3 中导入 `fixtures/miniapp-consumer/host`。页面实际显示 `Hello, WeChat, from commonMain`、`countUpTo(5): 5` 与 `sdkVersion: 0.1.0-SNAPSHOT`；Console 输出相同 greeting、count 与版本，并以 `fixture.result=PASS` 收尾。截图与日志共同证明最终产物由真实微信小程序 runtime 加载，而不是用 Node smoke 代替宿主验收。BOB-80 的独立消费者、自动化和微信宿主标准全部满足。

### 紧急第 H 步：BOB-79 `[P1][URGENT] Define Gradle Plugin integration test suite`

使用 Gradle TestKit 或与已选插件架构相符的可靠方案，自动覆盖：插件应用、缺少 KMP 的错误、`miniappMain` / `miniappTest` 创建、`miniappTest` 执行、依赖接线、构建任务注册与 consumer build。测试应以 BOB-80 的真实 fixture 和已接受模型为依据，不能只靠人工 IDEA 观察防回归。

**本轮进展（2026-09-18）**：先审计后补测，没有重复既有覆盖。`MiniAppGradlePluginTest` 由 16 个扩充到 **20** 个，`MiniAppPluginContractTest` 保持 9 个，`:miniapp-gradle-plugin:test` 共 **29** 个测试、0 失败；BOB-80 的持久 fixture 保持不变，其 `miniappNodeTest` 仍执行 **5** 个测试（`consumer.SharedTest` 2 个来自 `commonTest`、`consumer.MiniAppTest` 3 个）。

审计形成的覆盖矩阵（19 项）中，15 项在 BOB-78/76/82/81/84/80 已有有效证据，本轮不重复；新增的 4 个测试补齐真实缺口：同名但不兼容的 target、跨模块 variant 约束、未解析 classpath 的静默通过、共享 runtime 不被误伤。

1. `a target already using the platform name for another platform fails` —— 消费者已把 `miniapp` 用于 `jvm()` 时，在插件应用阶段以 Kotlin Gradle Plugin 自身的诊断失败（`The target 'miniapp' already exists, but it was not created with the 'js' preset`），而不是被静默覆盖。插件刻意不预先拦截该冲突：KGP 的信息已指明原因与修复方式，插件不重复实现它。
2. `a shared dependency without a Mini App variant fails variant resolution` —— BOB-83 记录、BOB-76 明确留给本 Issue 的跨模块约束：消费者 `commonMain` 依赖一个只提供 `jvm()` 的 KMP project 时，`miniappCompileClasspath` 以 `No matching variant of project :shared was found` 失败，并列出缺失的 `org.jetbrains.kotlin.platform.type` 属性；没有静默替换，也没有空 classpath。
3. `the boundary check refuses a classpath it could not resolve` —— 本轮发现并修正的最小缺陷（见下）。
4. `a classpath carrying only shared runtime passes the boundary check` —— 断言检查通过，并在同一 fixture 中打印 `miniappRuntimeClasspath` 的真实解析结果，因此该通过不是空断言。

**发现并修正的最小缺陷**：`checkMiniAppHostBoundary` 只读取 `ResolutionResult.allComponents`，而 Gradle 的解析结果只列出解析成功的依赖 —— 未解析的依赖直接缺席。探针证明：声明一个变体不匹配的坐标（如 `org.jetbrains.kotlinx:kotlinx-html-js:0.11.0`）后，runtime classpath 上并没有它，检查却**成功**，即把「从未看过这张依赖图」报告成「没有 renderer」—— 正是 BOB-81 要消除的那类「看起来成功但不可用」。最小修正：classpath 未完整解析时拒绝作出报告并列出无法解析的坐标（`MiniAppHostBoundary.UNRESOLVED_HEADER` / `describeUnresolved`），判定数据由新的 `MiniAppRuntimeClasspath` 承载。该修正不新增限制：未解析的 classpath 本来也无法产出 bundle，区别只在于失败信息说明了原因。

**统一入口**：根构建新增 `verifyMiniAppGradlePluginIntegration`，覆盖 `:miniapp-gradle-plugin:test`、`:kmp-miniapp-sdk:checkArchitectureBoundaries` 与 `verifyMiniAppConsumerFixture`。**不接入 `check`**：该 fixture 会驱动一个编译 Kotlin/JS 并安装 npm 依赖的嵌套 Gradle 构建，接入会让每次普通 `check` 增加数分钟与一个网络依赖；插件自身的套件已通过该工程的 `check` 属于 `check`。fixture 的三个 `Exec` 任务加了 `mustRunAfter`，避免嵌套构建与仓库自身的 plugin/SDK 任务同时写同一批构建目录。

**已执行验证**：`:miniapp-gradle-plugin:test --rerun-tasks`（29/0）、`:kmp-miniapp-sdk:check`、`verifyMiniAppConsumerFixture`（默认目录 + 宿主目录 + Node smoke，两次 bundle 均 14 文件且 `consumerContract.verified=true`）、`verifyMiniAppGradlePluginIntegration` 连续两次 BUILD SUCCESSFUL 且夹具侧复用 configuration cache、`git diff --check` clean。

**验收收尾（2026-09-18）**：Codex 复核生产修正、测试分层和任务顺序后，实际执行 `verifyMiniAppGradlePluginIntegration`，结果 `BUILD SUCCESSFUL`（3m19s）且复用 configuration cache。XML 再确认 TestKit 20 个、contract 9 个，均 0 failures / 0 errors；持久 fixture 的默认与宿主 bundle 各 14 个文件并输出 `consumerContract.verified=true`，Node smoke 输出 `fixture.result=PASS`。未发现需要进一步纠偏的代码或测试缺口，BOB-79 的集成测试套件验收完成。

**边界**：真实微信宿主验收不属于本 Issue，也不被 Node smoke 冒充；该宿主证据已由 BOB-80 单独完成。IDEA 人工识别仍属前序人工证据。

### 紧急第 I 步：BOB-77 `[P1][URGENT] Document first-class KMP Mini App consumer workflow`

在工作流被真实证明后，同步更新 README、PROJECT_FACTS、ARCHITECTURE 与 DEVELOPMENT 的中英文配对。新用户必须能只依靠 README 建立 `miniappMain` Hello World；文档要说明当前 Host 是 WeChat，同时保持 Mini App 平台边界，并移除已经被插件隐藏的 `jsMain`、CommonJS 接线和手工复制指导。

**本轮进展（2026-09-18）**：README 由「仓库内部说明」改写为消费者工作流文档：21 个章节、12 个代码块，结构与顺序在中英文之间完全一致。快速开始逐步给出前置条件、`settings.gradle.kts`、`build.gradle.kts`、`commonMain` 共享行为、`miniappMain` 宿主导出、`commonTest`/`miniappTest` 测试、`./gradlew miniappTest`、`./gradlew assembleMiniAppBundle`、微信宿主接入与预期输出。所有片段取自 `fixtures/miniapp-consumer` 的真实源码，未引入任何未验证 API。

**发布状态诚实处理**：新增「发布之前：今天你需要做什么」一节，明确插件与 SDK 均未发布到 Gradle Plugin Portal / Maven Central / npm，今天需要本仓库源码；并给出对照表说明发布后只有两个坐标的解析来源改变（插件按 id、runtime 按公共坐标），`build.gradle.kts`、源码与宿主不受影响。文档不提供任何尚不存在的已发布版本。

**职责边界**：新增「各自负责什么」表，区分 Gradle 插件、Runtime SDK、微信宿主（Host UI）与 **尚未实现** 的 Presentation Runtime；`UiState` / `Action` / `Store` / `Effect` 明确标注未实现且不展示任何 API。微信明确为当前 Host，Mini App 为平台抽象。

**移出消费者主流程**：README 不再以 `jsMain`、CommonJS、Kotlin/JS 或 SDK `build/`、`dist/` 内部路径作为消费者步骤；`buildMiniAppSdk` 与 `examples/wechat-miniprogram` 被标注为仓库内部/历史路径，不是消费者工作流。README 中不再要求消费者理解 `useCommonJs()`、手工复制产物或手工创建 source set。

**新增防漂移检查**：根任务 `verifyMiniAppConsumerDocs`（已接入 `check`）断言两份 README 的章节数一致、代码块语言序列一致、引用的每个 `fixtures/miniapp-consumer/...` 路径存在，且两条消费者命令仍被记录。它读取结构与路径而非正文，开销极低且无需网络。变异探针三例均已执行：从 README-ch 删除一个章节 → 失败（`README-en.md has 21, README-ch.md has 20`）；把 README-en 的路径改成不存在 → 失败（`refers to 'fixtures/miniapp-consumer/host-typo', which does not exist`）；改掉 README-ch 的 `assembleMiniAppBundle` 命令 → 失败。

**已执行验证**：README 中列出的每条命令都实际运行 —— 根构建 `projects`、`clean check`、`:kmp-miniapp-sdk:jsNodeTest`、`:kmp-miniapp-sdk:check`、`:miniapp-gradle-plugin:test`、`verifyMiniAppGradlePluginIntegration`；fixture 侧 `clean miniappTest`、`assembleMiniAppBundle`（默认目录 14 个文件）、`clean assembleMiniAppBundle -PminiappBundleDirectory=host/miniprogram/libs`（宿主目录 14 个文件）、`node scripts/host-smoke.cjs`（`fixture.result=PASS`）。全部 BUILD SUCCESSFUL。

**验收收尾（2026-09-18）**：Codex 复核确认 `verifyMiniAppConsumerDocs` 实际位于根 `check` 的任务图，并纠正三处过强或失真的 README 表述：代码片段允许明确删减而不声称逐字复制、Kotlin Gradle Plugin 仅声明已验证的 2.4.20 而不承诺其他版本兼容、隐私/订阅/网络能力分别保留其真实验收前置条件。用户随后执行 `./gradlew --no-daemon --console=plain verifyMiniAppConsumerDocs`，得到 2 份 README、21 个章节、12 个代码块、`consumerDocs.verified=true` 与 `BUILD SUCCESSFUL`。中英文标题结构和 `git diff --check` 均通过，BOB-77 验收完成。

**边界**：本轮不新增任何 API、发布配置或 P2 内容；未修改已验收的消费者 API 或插件行为。BOB-85 Release Gate 是下一步。

### 紧急 Release Gate：BOB-85 `[P1][URGENT][Release Gate] Accept KMP Mini App platform integration`

对 A–I 进行总验收。必须同时具备：普通 KMP fixture clean build、`miniappTest` 实际执行、source-set 与 dependsOn 自动断言、SDK 依赖自动解析、稳定产物组装、IDEA 识别证据、微信开发者工具 Consumer Bridge 验证、插件集成测试、真实 consumer fixture，以及同步的中英文文档。任何缺项都不能用“实现完成”替代。通过后才解除 BOB-51 的 Consumer Integration 发布阻塞；不得自动进入 P2 Presentation Runtime。

**验收结果（2026-09-18）：通过。**

- 验收基线 commit：`ae65a41`（BOB-77 交付）。本轮在该基线上执行全部验证，未改动任何生产代码。
- 环境：Gradle 9.3.1（仓库内置 Wrapper）、Kotlin 2.4.20、构建 JVM OpenJDK 25.0.2（插件字节码目标 JVM 17）、SDK 版本 `0.1.0-SNAPSHOT`、微信开发者工具基础库 3.17.3、IntelliJ IDEA 2026.2.2（Build `IU-262.10315.125`，Runtime 25.0.4+1-b508.27 aarch64）。

**自动化结果（全部实际执行，非推断）**

| 命令 | 结果 |
| --- | --- |
| `./gradlew projects` | BUILD SUCCESSFUL |
| `./gradlew clean check` | BUILD SUCCESSFUL（4m34s，含真实执行的插件测试套件与 `verifyMiniAppConsumerDocs`） |
| `./gradlew :kmp-miniapp-sdk:checkArchitectureBoundaries` | BUILD SUCCESSFUL |
| `./gradlew :kmp-miniapp-sdk:jsNodeTest --rerun-tasks` | BUILD SUCCESSFUL，**598** 个测试 / 0 失败 / 0 错误 |
| `./gradlew :miniapp-gradle-plugin:test --rerun-tasks` | BUILD SUCCESSFUL，**29** 个测试 / 0 失败（TestKit 20 + contract 9） |
| `./gradlew verifyMiniAppGradlePluginIntegration` | BUILD SUCCESSFUL |
| `./gradlew verifyMiniAppConsumerDocs` | BUILD SUCCESSFUL（`sections=21`、`codeBlocks=12`、两语言一致） |
| `./gradlew verifyMiniAppConsumerFixture` | BUILD SUCCESSFUL |
| `git diff --check` | 通过（无空白错误） |

**consumer fixture 结果**

- `clean miniappTest`：**5** 个测试真实执行、0 失败 —— `consumer.SharedTest` 2 个（只存在于 `commonTest`，经 source-set 层级被 `miniappTest` 复用）+ `consumer.MiniAppTest` 3 个（只存在于 `miniappTest`）。
- 默认 bundle 目录：`build/miniapp/bundle`，**14** 个文件，`consumerContract.verified=true`。
- 自定义宿主目录：`-PminiappBundleDirectory=host/miniprogram/libs`，**14** 个文件，`consumerContract.verified=true`；契约同时断言默认目录未被写入。
- bundle 文件集合：`miniapp-consumer-miniapp.js` / `.d.ts` / `.js.map`、`kmp-miniapp-sdk-kotlin.js`（+map）、`kotlin-kotlin-stdlib.js`（+map）、`kotlinx-coroutines-core.js`（+map）、`kotlinx-atomicfu.js`（+map）、`kotlin_org_jetbrains_kotlin_kotlin_dom_api_compat.js`（+map）、`package.json`；无 `.wxml` / `.wxss`，无 Compose / Skiko / `kotlinx-browser` 文件。
- Node host smoke：`fixture.greeting=Hello, WeChat, from commonMain`、`fixture.counted=5`、`fixture.sdkVersion=0.1.0-SNAPSHOT`、`fixture.result=PASS`。**这是接线检查，不是微信宿主验收。**
- configuration cache：夹具侧 `Reusing configuration cache.` / `Configuration cache entry reused.`。

**IDEA 证据**

用户已在当前 HEAD `ffc7bae` 对外部真实 KMP Demo 重新执行 Gradle Sync，`prepareKotlinIdeaImport` 与相关项目模型任务完成，最终 `BUILD SUCCESSFUL in 25s`。IntelliJ IDEA 2026.2.2（Build `IU-262.10315.125`）将 `commonMain`、`commonTest`、`miniappMain`、`miniappTest` 识别为 Kotlin source set；截图同时证明 `Platform.miniapp.kt` 的 `actual getPlatform()` 与 SDK `MiniAppSdk.VERSION` 正常解析、无错误标记，`SharedLogicMiniAppTest` 的测试类与测试方法均出现运行入口。同步日志中的 Android SDK XML 版本警告来自 Android Studio/命令行工具版本差异，与 Mini App source-set 模型无关，不阻塞本闸门。

**微信开发者工具证据**

来源为 BOB-80 在基础库 3.17.3 的验收：页面渲染 `Hello, WeChat, from commonMain`、`countUpTo(5): 5`、`sdkVersion: 0.1.0-SNAPSHOT`，Console 输出同样的三行并以 `fixture.result=PASS` 收尾。与本基线的一致性依据：`git diff 3952f2e..HEAD -- fixtures/ sdk/src/` 为空 —— 消费者源码、宿主工程与 SDK 源码与验收时完全相同；`configureMiniAppOutput`（`nodejs()` / `useCommonJs()` / `binaries.library()` / `generateTypeScriptDefinitions()`）与 bundle 任务自 `3952f2e` 起未改（`MiniAppPlatformSupport.kt` 的差异只在边界检查的判定与消息）。因此该证据仍然适用；未做字节级比对，因为验收时的产物未归档。

**架构检查清单（静态审计，SDK `sdk/src` + 插件 `miniapp-gradle-plugin/src`）**

| 检查 | 结果 |
| --- | --- |
| `@Composable` / MaterialTheme / LazyColumn / Modifier / Dp | 0 |
| `androidx.compose` / `org.jetbrains.compose` | 仅出现在插件的**禁止坐标清单**与对应测试字符串，无依赖、无类型 |
| `Column` / `Row` / `Button` / `Canvas` | 0 |
| 整词 `Text` | 11 处，全部为 KDoc 文案与微信订阅消息 interop 的 `WxSubscriptionEntry.Text` 数据取值，非 UI 类型 |
| Virtual DOM / Applier / View Tree / Layout Engine / setData / WXML generator | 0（`setData` 在 SDK 中不存在） |
| `WXML` / `WXSS` | 仅在插件的边界检查**说明文本**中出现；宿主 markup 只存在于 `examples/wechat-miniprogram` 与 `fixtures/miniapp-consumer/host` |
| `UiState` / `Store` / `Effect` / `Reducer` / `Presentation` | 0；`Action` 的 2 处均为 `org.gradle.api.Action`（Gradle DSL） |
| SDK 生产依赖 | 仅 `kotlinx-coroutines-core` |
| Gradle modules | 仅 `:kmp-miniapp-sdk` 与 `:miniapp-gradle-plugin`，不存在 `app` module，SDK 无法依赖 app |
| `dynamic` / `external` / `js()` | 限于 `jsMain`（`commonMain` 为 0） |

**闸门发现并修正的两处问题（均非生产代码）**

1. **测试依赖外部仓库状态（已修正）。** `the boundary check refuses a classpath it could not resolve` 使用已发布的 `org.jetbrains.kotlinx:kotlinx-html-js:0.11.0` 作为「不可解析」的替身。BOB-79 时该坐标因缓存未命中而无法解析，测试因此通过；本轮缓存过期后该坐标可解析，检查随即按**精确模块规则**将其判定为 renderer，断言随之失败。这是测试的缺陷而非插件缺陷：插件两次行为都正确。修正为使用不可能存在的坐标 `org.example.nowhere:not-a-real-module:1.0`，使断言只依赖插件行为，并记录「不得用第三方坐标的解析状态构造断言」。本轮两次全量执行（`clean check` 4m34s、`test --rerun-tasks` 3m33s）均为 29/0。
2. **AGENTS.md 宿主 UI 位置陈述过期（已修正）。** §10 仍写「WXML 与 WXSS 属于微信宿主 UI，今天即 `examples/wechat-miniprogram`」，而 BOB-80 之后消费者 fixture 的 `host/` 同样是微信宿主 UI。改为同时列出两处，规则不变。

**剩余限制（真实存在，不阻塞本闸门）**

- 插件与 runtime SDK 未发布到任何仓库；消费者当前需源码检出 + composite build，README 已明写。
- 微信宿主验收只覆盖该 bundle 形态与基础库 3.17.3 的页面路径；其他基础库、真机、其他微信能力按各自矩阵分别验收。
- Node smoke 仅证明模块接线，不等于宿主验收。
- 隐私授权、订阅消息、网络扩展、标准支付的真实宿主验收仍按 PROJECT_FACTS 与能力矩阵记录的状态各自待办 —— 与本闸门无关，不得据此声称已完成。

**结论**：BOB-85 的 18 项验收标准全部有证据支持，P1 Mini App 平台集成闸门通过；BOB-51 中的 Consumer Integration 发布阻塞可以解除。不自动进入 P2 Presentation Runtime。

### 紧急主线顺序表

| 顺序 | Multica Issue | 当前状态（快照 2026-09-16，A/B 更新至 2026-09-17） | 进入下一步的门禁 |
| --- | --- | --- | --- |
| 总目标 | BOB-75 | Done（2026-09-18） | A–I 与 Release Gate 全部完成；一等 KMP Mini App 消费者集成总目标验收通过 |
| A | BOB-83 | Done（2026-09-17） | PoC 选型、否决项、KGP 限制和必要 ADR 可核对 |
| B | BOB-78 | Done（2026-09-17，commit `19e807e`） | 插件可应用并 sync；缺少 KMP 时明确失败 |
| B+ | BOB-86 | Done（2026-09-17） | 架构边界已记录并被构建强制；无 Compose / Renderer 泄漏 |
| C | BOB-76 | Done（2026-09-17） | source sets 自动创建、IDEA 识别、`miniappTest` 可执行 |
| D | BOB-82 | Done（2026-09-17，commit `b4bfa38`） | 公共 SDK 依赖自动接入且不泄漏内部 artifact |
| E | BOB-81 | Done（2026-09-17） | 稳定任务生成完整 Mini App distribution，且依赖边界由正反外部 Demo 验收 |
| F | BOB-84 | Done（2026-09-17） | 最小 DSL 通过架构审查且不承载业务配置；外部 Demo 自定义目录构建通过 |
| G | BOB-80 | Done（2026-09-17） | clean build、5 个测试、runtime 自动解析、两个 bundle 目录、Node smoke 与微信开发者工具 3.17.3 验收均通过 |
| H | BOB-79 | Done（2026-09-18） | 29 个自动化测试覆盖插件关键路径；统一入口 `verifyMiniAppGradlePluginIntegration` 复核通过 |
| I | BOB-77 | Done（2026-09-18） | README 消费者快速开始、发布状态诚实边界与 `verifyMiniAppConsumerDocs` 防漂移检查均验收通过 |
| Release Gate | BOB-85 | Done（2026-09-18） | 18 项验收标准逐条有证据；完整 Consumer Integration 验收通过，BOB-51 的发布阻塞可解除 |

## 3. 原微信能力落地顺序

### 第 1 步：BOB-67 `[文档][P1][发布阻塞] 建立微信能力矩阵`

先建立中英文能力矩阵骨架，登记能力状态、API、权限、最低宿主版本、测试等级和备注。本步骤先建立结构，后续每个 Issue 完成时持续更新；只有所有条目与真实代码及验证证据一致后才能关闭 BOB-67。

### 第 2 步：BOB-71 `[测试][P1][发布阻塞] 建立微信真实宿主验证矩阵`

建立 Unit、Contract、DeveloperTools、RealDevice、BackendRequired 五级验证体系，并规定前置条件、执行步骤、预期结果、设备与基础库版本、证据和验证日期。先建立模板，后续随各能力持续补证，最后统一关闭。

### 第 3 步：BOB-70 `[微信][P1][发布阻塞] 添加运行时能力检测与版本门控`

实现 `wx.canIUse`、基础库版本读取和宿主能力状态。至少能够表达支持、不支持、版本依赖和权限依赖；不支持的 API 应映射为明确错误，而不是在运行时意外失败。该 Issue 是后续所有新增微信能力的首要技术依赖。

### 第 4 步：BOB-64 `[微信][P1][发布阻塞] 建立权限生命周期抽象`

接入 `getSetting`、`authorize`、`openSetting`，建立未请求、已授权、已拒绝三态模型。权限拒绝必须与用户取消、宿主失败区分，定位、相机、相册、麦克风和蓝牙等能力不得重复建立各自的权限体系。

### 第 5 步：BOB-60 `[微信][P1][发布阻塞] 接入微信隐私授权`

接入 `getPrivacySetting`，建立与普通权限相互独立的隐私授权状态和流程。完成后执行基础 Gate：确认 Runtime Detection、Permission、Privacy 职责分离，并可被后续设备能力统一复用。

### 第 6 步：BOB-58 `[微信][P1] 添加会话有效性检查能力`

在既有 `wx.login` bootstrap 上增加强类型 `wx.checkSession`。继续保持 `login code -> Backend -> verified identity` 的安全边界，不在客户端 SDK 中建立可信用户、服务端 session 或 token refresh 系统。

### 第 7 步：BOB-65 `[微信][P1] 添加剪贴板与震动能力`

先落地剪贴板读写以及短震动、长震动，验证标准能力扩展路径。剪贴板需要契约与开发者工具验证，震动必须补充真机证据。

### 第 8 步：BOB-72 `[微信][P1] 添加基础文件系统能力`

基于 `getFileSystemManager` 实现 read、write、access、remove 的最小契约，明确沙箱路径、编码和错误语义。该能力为后续下载结果和媒体临时文件处理提供基础，但不得扩张为完整 FileSystemManager 封装。

### 第 9 步：BOB-66 `[微信][P1] 添加定位能力`

至少实现 `getLocation`，并根据真实需求决定是否加入 choose/open location。必须完整验证 Runtime Detection、Permission、Privacy、Host Adapter、Error Mapping 和 RealDevice 链路。

### 第 10 步：BOB-61 `[微信][P1] 添加扫码能力`

实现强类型 `scanCode`，验证用户取消、权限与隐私前置条件、扫码结果校验和真机差异。相机原生组件及通用相机界面不属于该 Issue。

### 第 11 步：BOB-63 `[微信][P1] 添加媒体能力`

以 `chooseMedia` 为最小范围，处理媒体类型、临时路径、基础元数据、用户取消以及相册相关权限和隐私要求。不得进入 Camera、Video、播放器或编辑器等原生展现层。

### 第 12 步：BOB-73 `[微信][P1] 添加订阅消息能力`

实现微信专属 `requestSubscribeMessage`，保持用户主动触发语义，并区分同意、拒绝、取消和宿主失败。该能力通过微信平台专属入口暴露，不能抽象为通用推送通知。

### 第 13 步：BOB-68 `[微信][P1] 补齐网络上传、下载与网络状态能力`

按 `getNetworkType`、`uploadFile`、`downloadFile`、`onNetworkStatusChange`、`offNetworkStatusChange` 的内部顺序推进。重点验证可取消 Task 与 `abort()` 的关系、on/off 监听成对清理、上传下载的真机与测试后端条件，以及下载结果与 BOB-72 文件系统边界。WebSocket 只在能力矩阵中记录状态，不在本 Issue 实现。

### 第 14 步：BOB-59 `[微信][P1][发布阻塞] 实现微信标准支付能力`

先定义强类型 PaymentRequest 和 PaymentResult，再接入 `wx.requestPayment`，最后使用合法商户和后端环境完成真机验证。支付签名只能由 Backend 生成；用户取消必须独立表达；客户端支付成功不能作为最终订单事实，最终状态必须由 Backend 校验。

### 第 15 步：BOB-74 `[微信][P1] 定义虚拟支付能力边界`

在标准支付模型稳定后，独立定义 `requestVirtualPayment` 的 capability、请求与结果语义、平台限制和后端责任。P1 不要求生产实现，但必须在能力矩阵中给出明确状态，不得与标准支付合并为充满可空字段的 DTO。

**本轮结论（2026-09-16）**：边界已定义，记录于 ARCHITECTURE 的「计划中的虚拟支付边界（未实现）」与 DEVELOPMENT 的「计划中的虚拟支付（未实现）」两节 —— 独立 capability key（候选 `wechat.request-virtual-payment`）、独立请求与结果模型、与标准支付之间不存在降级路径、资格不等于 API 存在、分平台分账号、无客户端签名或凭据。能力矩阵状态为 `Planned`：不注册 catalog、不导出、无任何代码。

事实证据：所带开发者工具基础库（18,152,387 字节）中 `requestVirtualPayment` 出现 **0** 次，而 `requestPayment` 出现 7 次（含 `canIUse` 元数据表条目）。官方页面在本环境无法抓取，因此参数表、最低基础库版本、平台可用性与账号/类目资格均记为**待确认**，不得由路线图、社区文章或标准支付行为推断。

### 第 16 步：BOB-69 `[微信][P1][实验性] 验证 BLE 事件驱动能力模型`

只进行最小实验：打开适配器、开始发现、设备发现事件、停止发现、连接和断开。重点验证 Flow、取消、连接生命周期、背压、重复事件，以及 `onXXX/offXXX` 清理。完成后仍标记为 Experimental，不得宣传为完整或稳定 BLE SDK。

### 第 17 步：BOB-62 `[性能][P1] 建立 Kotlin/JS 包体积基线`

在能力实现基本稳定后，记录 SDK 自身、Kotlin 标准库、Coroutines、AtomicFU 和包装层的原始及 gzip 尺寸，提供可重复命令、首次加载观察方法和回归告警阈值。单次机器测量不能被描述为绝对性能保证。

## 4. 阶段 Gate

### Gate A：基础前置条件

BOB-70、BOB-64、BOB-60 完成后，确认运行时检测、权限和隐私是三个清晰边界，设备能力能够直接复用，错误分类没有退化为通用 HostFailure。

### Gate B：代表性能力

BOB-58、BOB-65、BOB-72、BOB-66、BOB-61、BOB-63、BOB-73 完成后，确认新增能力均遵循相同的 interop、adapter、async、error、export、testing 和 documentation 路径。

### Gate C：复杂异步与安全

BOB-68、BOB-59、BOB-74、BOB-69 完成后，确认 abort、listener cleanup、用户取消、权限拒绝、支付后端责任和事件资源生命周期模型成立。BOB-74 以边界定义与文档审查计入本 Gate，本轮不实现虚拟支付，能力保持 `Planned`。

### Gate D：收尾

BOB-62 完成后，回到 BOB-67 和 BOB-71 补齐全部状态与证据。原 17 个微信能力 Issue 逐项满足自身 Acceptance Criteria，并且新增 BOB-75 至 BOB-85 紧急主线通过 BOB-85 Consumer Integration Release Gate 后，才进入项目现有的 BOB-51 P1 Release Gate 审查。任一被账号、模板、商户、后端、设备或 IDE 条件阻塞的验收必须保持 Blocked，不得用自动化结果代替。

## 5. 原微信能力一一对应核对表

| 顺序 | Multica Issue | 中文标题 | 计划阶段 |
| --- | --- | --- | --- |
| 1 | BOB-67 | 建立微信能力矩阵 | 验收框架 |
| 2 | BOB-71 | 建立微信真实宿主验证矩阵 | 验收框架 |
| 3 | BOB-70 | 添加运行时能力检测与版本门控 | 公共前置条件 |
| 4 | BOB-64 | 建立权限生命周期抽象 | 公共前置条件 |
| 5 | BOB-60 | 接入微信隐私授权 | 公共前置条件 |
| 6 | BOB-58 | 添加会话有效性检查能力 | 基础能力 |
| 7 | BOB-65 | 添加剪贴板与震动能力 | 基础能力 |
| 8 | BOB-72 | 添加基础文件系统能力 | 基础能力 |
| 9 | BOB-66 | 添加定位能力 | 设备能力 |
| 10 | BOB-61 | 添加扫码能力 | 设备能力 |
| 11 | BOB-63 | 添加媒体能力 | 设备能力 |
| 12 | BOB-73 | 添加订阅消息能力 | 微信专属能力 |
| 13 | BOB-68 | 补齐网络上传、下载与网络状态能力 | 复杂异步 |
| 14 | BOB-59 | 实现微信标准支付能力 | 支付安全 |
| 15 | BOB-74 | 定义虚拟支付能力边界 | 支付设计 |
| 16 | BOB-69 | 验证 BLE 事件驱动能力模型 | 实验性架构验证 |
| 17 | BOB-62 | 建立 Kotlin/JS 包体积基线 | 发布收尾 |

## 6. 全量范围核对

- 原微信能力主线：BOB-58 至 BOB-74，共 17 个 Issue，均在第 3 节和第 5 节各有唯一执行条目与索引。
- 紧急消费者集成主线：BOB-75 至 BOB-85，共 11 个 Issue；BOB-75 是总目标，BOB-83、78、76、82、81、84、80、79、77 是 A–I，BOB-85 是最终 Release Gate，均在第 2 节各有唯一条目。
- 当前文档合计覆盖 28 个 Multica Issue，没有把父 Issue、子 Issue 或 Release Gate 合并为同一个验收结果。
- 执行时必须重新读取 Multica 最新内容；本文件中的状态快照只表示 2026-09-16，不是持续同步的事实源。
