# ADR 0011：Presentation state 共享，渲染保持宿主原生

[English](0011-presentation-state-shared-rendering-host-native-en.md)

- 状态：Accepted
- 日期：2026-09-17

## 背景

ADR 0010 解决了消费者如何获得 Mini App 平台 target。它没有回答另一个问题，而消费者集成工作让这个问题变得紧迫：本 SDK 允许共享应用的哪些部分，共享在哪里停止？

有两种同样容易落入的失败形态。一种是「共享一切」的 SDK，它吸收本属于后端权威的业务规则，最终形成第二个、更弱的真相源。另一种是只包裹 `wx.*` 的 Kotlin wrapper，几乎什么都不共享，让每个消费者重复实现同样的客户端行为。UI 上也存在同样的模糊：当前仓库中没有任何东西阻止未来的贡献者把一个 renderer 塞进 `miniappMain` 背后，或者让 presentation 层依赖 Compose，从而使只有 Compose 客户端能使用它。

这件事重要，是因为 SDK 会被不止一种客户端消费。Android、iOS 与桌面客户端用 Compose 渲染；当前微信宿主用 WXML 与 WXSS 渲染；未来的支付宝宿主用 AXML 与 ACSS，Telegram 宿主用 HTML。如果 presentation state 用其中任何一种渲染器的词汇表达，其余客户端就无法使用，SDK 也就悄悄变成了一个带微信适配器的 Compose 库。

值得明确记录一个命名陷阱：外部演示项目有一个名为 `app/shared` 的 Gradle module。它是消费 SDK 的 Compose 客户端应用。名为 `shared` 的 module 不自动等于 SDK 代码，本次审计是按这一前提去核对的，而不是相信名字。

## 决策

**SDK 共享客户端行为、宿主能力与 presentation state；它从不渲染。最终视图属于平台宿主。**

脚手架的 `miniappMain` 与 `miniappTest` 用于 host integration。host integration 不是渲染。

三条原则，按权威顺序排列：

1. **Backend owns business truth。** 身份与服务端 session、token 生命周期、订单与支付的最终状态、库存、预约、业务规则，以及一切服务端授权事实。宿主成功回调、本地缓存与 `UiState` 都是客户端观测，永远不是业务事实。
2. **Kotlin owns client behavior。** API client、客户端 session 处理、缓存策略、error 与 async 模型、retry 与 cancellation 策略、宿主能力抽象、流程编排，以及从 P2 起的 presentation state 与 presentation logic。
3. **Host owns rendering。** 最终视图、布局、原生组件，以及一切渲染决策。

### 四层职责

| 层 | 负责 | 不得 |
| --- | --- | --- |
| **Backend** | 认证与服务端 session、token 生命周期、订单与支付最终状态、库存、预约、业务规则、服务端授权与可信事实 | 在客户端被重新实现 |
| **KMP Client Runtime** | Domain model、UseCase、Repository contract、error model、async 与 coroutine 抽象、宿主能力 contract、缓存策略，以及从 P2 起的 `UiState` / `Action` / `Store` / `Effect` / state machine / presentation logic | 依赖 Compose、任何 UI framework 或宿主 markup；复制服务端权威规则；把自己的缓存当作事实 |
| **Host Integration** | 当前是微信 —— 宿主 API 的 adapter 与错误映射、runtime 与能力检测、permission、privacy、lifecycle、navigation、JS binding、presentation binding | 定义 view、layout、view tree 或 renderer |
| **Platform UI** | 最终视图与布局：Android / iOS / 桌面客户端用 Compose；微信宿主用 WXML 与 WXSS；未来支付宝宿主用 AXML 与 ACSS；未来 Telegram 宿主用 HTML | 出现在 SDK 内部 |

### 四个共享层级

1. **Shared client infrastructure** —— API client、session、error、cache、async。共享。
2. **Shared host capability abstraction** —— storage、location、scanner、haptics、network 以及当前能力集合的其余部分。共享。
3. **Shared presentation** —— `UiState`、`Action`、`Store`、`Effect`、state machine。规划于 P2。**当前未实现。**
4. **Shared rendering** —— view、layout、renderer、component tree。明确且永久地不在范围内。

第 3 层正是本 ADR 要固定的边界：presentation 的**状态与行为**可移植，presentation 的**渲染**不可移植。`UiState` 是一个值；`Column` 是给某一具体渲染器的布局指令。

### 硬性边界

- **SDK Core 不得依赖 Compose。** 不依赖 `androidx.compose.*`，不依赖 `org.jetbrains.compose.*`，不依赖 Material 或 Compose 编译器插件，也不依赖 Android View、UIKit 或 SwiftUI。
- **Presentation Core 一旦存在，同样不得依赖 Compose。** 它可以承载 `UiState` 与 `Action`，不得承载 composable。
- **Compose 是消费者，不是 SDK 基础设施。** Compose 客户端收集 state、派发 action 并渲染；SDK 不知道它存在。
- **`miniappMain` 只做 host integration 与 binding。** 它可以把宿主事件转换为 action、把 state 转换为 `setData`。它不得包含 renderer、virtual DOM、view tree、layout engine、Kotlin UI DSL 或 WXML generator。
- **WXML、WXSS 与宿主 markup 永不进入 SDK Core**，理由与 Compose 相同：它们是某一个渲染器的词汇。
- **`app/` 与任何宿主 UI 消费 SDK；SDK 永不依赖 `app/`。** 这是结构性的而不是愿望性的：examples 与消费者应用都不是本仓库的 Gradle module。
- **客户端不可信。** 复用 Kotlin 代码、共享 presentation state 都不移动信任边界。宿主登录产出的是交给后端校验的凭证；session 由后端签发。宿主支付调用产出的是交互结果；订单与最终状态由后端持有并确认。
- **Server domain model、client DTO 与 presentation model 三者分离。** 线上模型不是 presentation model，也不假设任何一方被原样暴露。
- **删除客户端的全部 Compose 依赖后，SDK Core 与未来的 Presentation Core 必须在架构上成立且可构建。** 这是分层必须通过的测试，并且它在构建中被强制，而不是只在文档中声明。

### 强制手段

当 runtime SDK 导入或声明来自 UI framework namespace 的依赖时，`./gradlew :kmp-miniapp-sdk:checkArchitectureBoundaries` 会失败，覆盖 `androidx.compose.*`、`org.jetbrains.compose.*`、`android.view.*`、`android.widget.*`、`androidx.activity.*`、`kotlinx.html.*` 与 `com.android.*`。只为 UI framework 服务的 group 会被整体封禁；若某个 group 同时承载本 SDK 正当使用的依赖，则按精确 module coordinate 封禁，因此 `org.jetbrains.kotlinx:kotlinx-html` 会被拒绝，而 `org.jetbrains.kotlinx:kotlinx-coroutines-core` 不会。任务在把分类器用于模型之前，先拿一张固定的期望结论表校验它，因此规则不会在无人察觉的情况下漂移。该任务接入 `:kmp-miniapp-sdk:check`，因此这条约束是构建失败而不是评审意见。将来出现 `presentation` module 时，同样应用这一检查。

该检查无法做的是判断语义。一个用 SDK 自身类型拼装出来的 renderer 既不导入 UI framework，也不声明 UI framework 依赖，因此可以通过；这类错误仍属评审职责，检查并不声称覆盖它。

## 后果

- 没有 Compose 的平台上的客户端可以使用 SDK 共享的一切，因为共享的内容没有一处是用某个渲染器的词汇表达的。
- SDK 不会意外变成 Compose 库：构建会拒绝它。
- 新增宿主意味着为该宿主编写 adapter 与渲染器，而不是重写共享 presentation logic。明确的硬性要求是：新增支付宝或 Telegram 时不得重写 Presentation Core。
- 替换微信宿主的 TypeScript / Kotlin 分工，或替换宿主自身的 UI runtime，都不会触及 SDK 的分层，因为 SDK 从未从它们那里派生任何东西。
- `UiState`、`Action`、`Store` 与 `Effect` 当前处于**归属已定义但未实现**的状态：它们的归属已明确，且不存在任何代码。没有为了满足术语而创建空 module 或占位类型。BOB-85 要求这一点保持成立 —— 可以陈述归属，不得声称已实现。
- 仓库保持当前的 module 布局。长期拆分出的 `client-core` / `presentation` / `capability` / `host-wechat` / `host-alipay` / `host-telegram` / `testing` 是计划而非事实，只有在 module 边界被 public API、依赖生命周期、独立测试或发布、或明显不同的变更频率证明必要时才适用。现在把这些边界登记到文档中，是为了让将来的拆分是机械的，而不是一次重新设计。
