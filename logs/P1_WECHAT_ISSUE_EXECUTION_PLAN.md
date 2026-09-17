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

### 紧急第 G 步：BOB-80 `[P1][URGENT] Create real consumer KMP integration fixture`

建立真正独立、普通的 KMP consumer fixture，而不是 SDK 内部 demo。Fixture 只应用插件便可编译 `miniappMain`、复用 `commonMain`、运行 `miniappTest`、自动解析 runtime 依赖，并让微信 Host 消费最终产物。Android/iOS 仅在证明与普通 KMP target 共存确有必要时加入。

### 紧急第 H 步：BOB-79 `[P1][URGENT] Define Gradle Plugin integration test suite`

使用 Gradle TestKit 或与已选插件架构相符的可靠方案，自动覆盖：插件应用、缺少 KMP 的错误、`miniappMain` / `miniappTest` 创建、`miniappTest` 执行、依赖接线、构建任务注册与 consumer build。测试应以 BOB-80 的真实 fixture 和已接受模型为依据，不能只靠人工 IDEA 观察防回归。

### 紧急第 I 步：BOB-77 `[P1][URGENT] Document first-class KMP Mini App consumer workflow`

在工作流被真实证明后，同步更新 README、PROJECT_FACTS、ARCHITECTURE 与 DEVELOPMENT 的中英文配对。新用户必须能只依靠 README 建立 `miniappMain` Hello World；文档要说明当前 Host 是 WeChat，同时保持 Mini App 平台边界，并移除已经被插件隐藏的 `jsMain`、CommonJS 接线和手工复制指导。

### 紧急 Release Gate：BOB-85 `[P1][URGENT][Release Gate] Accept KMP Mini App platform integration`

对 A–I 进行总验收。必须同时具备：普通 KMP fixture clean build、`miniappTest` 实际执行、source-set 与 dependsOn 自动断言、SDK 依赖自动解析、稳定产物组装、IDEA 识别证据、微信开发者工具 Consumer Bridge 验证、插件集成测试、真实 consumer fixture，以及同步的中英文文档。任何缺项都不能用“实现完成”替代。通过后才解除 BOB-51 的 Consumer Integration 发布阻塞；不得自动进入 P2 Presentation Runtime。

### 紧急主线顺序表

| 顺序 | Multica Issue | 当前状态（快照 2026-09-16，A/B 更新至 2026-09-17） | 进入下一步的门禁 |
| --- | --- | --- | --- |
| 总目标 | BOB-75 | Todo | A–I 与 Release Gate 全部完成 |
| A | BOB-83 | Done（2026-09-17） | PoC 选型、否决项、KGP 限制和必要 ADR 可核对 |
| B | BOB-78 | Done（2026-09-17，commit `19e807e`） | 插件可应用并 sync；缺少 KMP 时明确失败 |
| B+ | BOB-86 | In Progress（2026-09-17） | 架构边界已记录并被构建强制；无 Compose / Renderer 泄漏 |
| C | BOB-76 | Done（2026-09-17） | source sets 自动创建、IDEA 识别、`miniappTest` 可执行 |
| D | BOB-82 | In Progress（2026-09-17） | 公共 SDK 依赖自动接入且不泄漏内部 artifact |
| E | BOB-81 | Done（2026-09-17） | 稳定任务生成完整 Mini App distribution，且依赖边界由正反外部 Demo 验收 |
| F | BOB-84 | Blocked by B；在 E 后执行 | 最小 DSL 通过架构审查且不承载业务配置 |
| G | BOB-80 | Blocked by C、D、E、F | 普通 KMP fixture 完成编译、测试和微信消费闭环 |
| H | BOB-79 | Blocked by B、C；在 G 后执行 | 插件关键路径由自动化 integration tests 覆盖 |
| I | BOB-77 | Blocked by G、H | 中英文文档只描述已验证消费者工作流 |
| Release Gate | BOB-85 | Blocked by A–I | 完整 Consumer Integration 验收通过并可解除 BOB-51 阻塞 |

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
