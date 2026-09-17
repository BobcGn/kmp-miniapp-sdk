# 路线图

[English](ROADMAP-en.md)

除非某项能力同时体现在源码和 PROJECT_FACTS-ch.md 中，否则不得将本文档中的任何内容视为已实现能力。

以下内容均为计划。完成状态必须有可执行配置、源码和 [PROJECT_FACTS-ch.md](PROJECT_FACTS-ch.md) 支持。

## V0.1 Bootstrap — DONE

- 建立单一 `:sdk` Kotlin Multiplatform module。
- 配置 Kotlin/JS CommonJS library target 和 Node.js tests。
- 建立最小 source sets、构建验证、文档和仓库规则。

## P1 收尾：一等 KMP Mini App 消费者集成 — URGENT / RELEASE BLOCKER

BOB-75 将 P1 的完成标准从“仓库内部能够生成 Kotlin/JS 并由微信示例消费”提升为“普通 KMP 项目无需理解内部 Kotlin/JS 接线即可使用 Mini App SDK”。现有 `buildMiniAppSdk` 与微信示例仍是基础证据，但不能替代 Gradle Plugin、`miniappMain` / `miniappTest`、自动依赖接线和真实消费者夹具。

目标消费体验为：

```kotlin
plugins {
    kotlin("multiplatform")
    id("io.github.bobcgn.miniapp")
}
```

应用插件后，消费者必须自动获得稳定的 `miniappMain` 与 `miniappTest` 入口、正式 SDK API 依赖以及可供微信开发者工具消费的产物。内部实现采用命名 Kotlin/JS target、自定义 source sets 或 plugin 管理的 compilation hierarchy，必须先由 PoC 决定；不得为了赶进度降低这项用户体验要求。

### 严格关键路径

同一时间只推进一个关键路径阶段。前序未完成时不得把后序 Issue 标记为 Ready 或 In Progress：

| 顺序 | Issue | 交付物 | 进入下一步的门禁 |
| --- | --- | --- | --- |
| A | BOB-83 | 对 Kotlin 2.4.20 / Gradle 9.3.1 比较命名 JS target、自定义 source sets 与自定义 compilation/hierarchy 的最小 PoC | 记录选型、否决项、原因、KGP 限制；必要时形成 ADR；`miniappMain` / `miniappTest` 硬要求不变 |
| B | BOB-78 | 独立 `io.github.bobcgn.miniapp` Gradle Plugin 骨架 | 普通 KMP 项目可应用插件并完成 sync；缺少 KMP 时给出明确错误 |
| C | BOB-76 | 自动建立 `commonMain → miniappMain`、`commonTest → miniappTest` | 无需消费者手写 source set；IDEA 识别与代码补全通过；`miniappTest` 可执行 |
| D | BOB-82 | 自动接入正式 SDK runtime 依赖 | `miniappMain` 可直接使用公共 API；不得要求消费者声明内部微信 artifact |
| E | BOB-81 | 隐藏 CommonJS、TypeScript declaration、runtime dependency 与微信产物组装 | 一个稳定任务生成微信可消费产物；消费者不调用 `useCommonJs()`、不手工复制 artifact |
| F | BOB-84 | 最小 `miniapp { wechat { ... } }` Gradle DSL | 只承载真正属于构建的配置；保持 Mini App Platform 与当前 WeChat Host 的边界 |
| G | BOB-80 | 独立、普通的 KMP consumer fixture | fixture 仅应用插件即可编译 `miniappMain`、运行 `miniappTest`、复用 `commonMain` 并由微信 Host 消费最终产物 |
| H | BOB-79 | Gradle Plugin integration test suite | 自动覆盖插件应用、缺少 KMP、source-set 接线、测试执行、依赖接线、任务注册与 consumer build |
| I | BOB-77 | 一等消费者工作流文档 | README、PROJECT_FACTS、ARCHITECTURE、DEVELOPMENT 中英文同步；新用户只读 README 即可建立 Hello World |
| Release Gate | BOB-85 | P1 Consumer Integration 总验收 | A–I 全部有可核对证据后，完成 Gradle、IDEA、fixture、产物、微信开发者工具与文档验收；通过后才解除 BOB-51 的发布阻塞 |

第 A 步已有结论。`poc/kgp-model` 中的 PoC 选定「由插件管理、以平台命名的 Kotlin/JS target」，因为只有在它之下 `miniappMain` 与 `miniappTest` 才是由 compilation 拥有的真实 Kotlin source set。[ADR 0010](decisions/0010-miniapp-gradle-plugin-source-set-model-ch.md) 记录了该决策、被否决的替代方案与 Kotlin Gradle Plugin 的限制。`miniappMain` / `miniappTest` 的硬性要求不变。

第 B 步进行中。`:miniapp-gradle-plugin` 发布 `io.github.bobcgn.miniapp`，在 Kotlin Multiplatform 项目中注册 `miniapp` target，并对未应用 Kotlin Multiplatform 插件的项目报错。自动 source set 提供（BOB-76）、SDK 依赖接线（BOB-82）、产物组装（BOB-81）与微信 DSL（BOB-84）尚未实现，因此后续阶段仍处于门禁之下。

### P1 完成证据

BOB-85 必须同时具备以下证据，缺一项都不得用“实现完成”代替验收：

- 普通 KMP fixture 的 Gradle clean build 与 `miniappTest` 实际执行结果。
- 插件自动创建 `miniappMain` / `miniappTest` 以及两条 dependsOn 关系的自动化断言。
- SDK 依赖自动解析，且消费者构建文件中没有内部微信 artifact 或手工 Kotlin/JS module-kind 配置。
- 稳定组装任务生成完整微信消费产物，包含所需声明与 runtime dependencies，无手工复制步骤。
- IDEA 将两个 source sets 识别为 Kotlin source sets；该项需要 IDE 截图或等价的可核对证据，不能仅由 Gradle 测试替代。
- 最终产物在微信开发者工具中加载并完成规定的 Consumer Bridge smoke；真机、权限、隐私或后端能力仍按微信真实宿主验证矩阵分别验收。
- Gradle Plugin integration tests 与真实 consumer fixture 均通过，避免仅在 SDK 自身仓库内形成假绿。
- README、PROJECT_FACTS、ARCHITECTURE、DEVELOPMENT 的中英文内容与实际工作流一致。

### 架构边界与非目标

- Gradle Plugin 只负责平台/构建集成与开发者体验；Runtime SDK 继续负责 API 与 Host capability。
- Mini App Platform 是抽象，WeChat 是当前 Host；插件不得把 `MiniApp == WeChat` 固化为总体架构。
- 本阶段不实现 Alipay、Telegram、多 Host source-set hierarchy、npm/Maven Central 发布或多个 Mini App targets。
- 本阶段不进入 P2 Presentation Runtime，不引入 Compose、renderer、Virtual DOM 或 WXML generation。
- BOB-53 的 `buildMiniAppSdk` 是基础证据，不是 BOB-75/85 所要求的消费者工作流。

## V0.2 JS Consumer Bridge — DONE

- 已定义有明确意图的 `@JsExport` boundary。
- 已生成并检查 TypeScript declaration。
- 已通过 TypeScript `require()` 消费 CommonJS artifact。
- 已在微信开发者工具中完成验证。

## V0.3 First wx Bridge — DONE

已选择 Storage 作为首个最小 bridge，并完成强类型 interop、adapter、自动化测试与微信开发者工具验证。该选择不表示候选 API 均已实现。

## V0.4 Coroutine Adapter — DONE

已建立 internal callback-to-coroutine primitive，并将适合的 callback-based platform API 适配为 suspend-friendly Kotlin API；带真实 abort handle 的操作与仅停止等待的操作保持不同语义。

## 其余 P1 Host Capability — IN PROGRESS

- Network、Navigation、Lifecycle 与多项微信 Host capability 已落地；精确状态以 PROJECT_FACTS 与微信能力矩阵为准。
- BOB-60 隐私真实宿主验收仍被账号后台条件阻塞；BOB-73 订阅消息正在真实宿主验收。
- Payment、Upload/Download/Network Status、BLE 与 Virtual Payment 仍按各自 Issue 推进，不因消费者集成主线而被视为已实现。
- 虚拟支付边界已在 BOB-74 中确定并记录于 [ARCHITECTURE-ch.md](ARCHITECTURE-ch.md)：它是与标准支付相互独立的 capability，拥有自己的 key、请求、结果与后端流程，两者之间不存在降级路径。P1 不实现其中任何部分 —— 该能力在[微信能力矩阵](platforms/wechat/WECHAT_CAPABILITIES-ch.md)中为 `Planned`，且不存在任何代码、catalog 注册或导出。标准支付（BOB-59）已实现并具备自动化测试，其真实宿主验收被合法商户环境阻塞，属于环境阻塞而不是实现缺口。
- Build automation 与 consumer distribution 由上面的 BOB-75 至 BOB-85 紧急主线收口。

## P1 之后：Client Presentation 与新增 Host

分层已由 [ADR 0011](decisions/0011-presentation-state-shared-rendering-host-native-ch.md) 固定：后端拥有业务事实，SDK 共享客户端行为、宿主能力与 presentation state，宿主负责渲染。以下全部是计划，没有任何一项已实现。

- **P2 —— Client Presentation Core。** `UiState`、`Action`、`Store`、`Effect`、state machine 与 presentation logic。它不得依赖 Compose 或任何 renderer。当前不存在，也没有为它创建占位 module 或占位类型。
- **P3 —— 微信 presentation binding。** `StateFlow` → JavaScript → `setData`，以及宿主事件 → `Action`。仅做 binding：没有 view tree、layout engine、Kotlin UI DSL 或 WXML generator。
- **P4 —— Backend 与 presentation 编排成熟度。** API client、session、cache、offline 行为、retry，以及认证与支付流程。
- **P5 —— 新增 Host。** 支付宝与 Telegram。新增 Host 时不得重写 Presentation Core。
- **P6 —— 可选的 Compose UI adapter** 与更丰富的客户端集成。Compose 始终是客户端 UI consumer。

渲染不在这条路径上。renderer、virtual DOM、view tree、layout engine 或 WXML generator 仍是明确的非目标；改变这一点需要一个新的 architecture decision，而不是一条 roadmap 条目。
