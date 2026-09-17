# 项目事实

[English](PROJECT_FACTS-en.md)

本文件是 kmp-miniapp-sdk 当前状态的人类可读事实源。

如果本文件与可执行配置或源码冲突，以可执行配置和源码为准。

微信各能力的实现状态、宿主 API、权限前置条件和验证等级见 [微信能力矩阵](platforms/wechat/WECHAT_CAPABILITIES-ch.md)。矩阵中的 `Planned`、`Partial`、`Experimental`、`Unsupported` 与 `P3-Presentation` 均不得解释为已实现能力。

## 1. 项目标识

- 项目：`kmp-miniapp-sdk`
- 目的：一个 Kotlin Multiplatform client runtime，用于跨 Mini App 平台共享客户端行为、宿主能力与 presentation state，同时保持渲染由宿主原生负责。它使用官方 Kotlin Multiplatform 和 Kotlin/JS，让共享 Kotlin 逻辑可由微信小程序 JavaScript 或 TypeScript runtime 消费。
- 类型：SDK / library。

后端拥有业务事实，Kotlin 拥有客户端行为，宿主拥有渲染。[ADR 0011](decisions/0011-presentation-state-shared-rendering-host-native-ch.md) 固定了四层 —— backend、KMP client runtime、host integration、platform UI —— 及其责任矩阵；矩阵内容见 [ARCHITECTURE-ch.md](ARCHITECTURE-ch.md)。

本项目不是微信小程序 UI framework、Kuikly replacement、Compose renderer、Virtual DOM 或 WXML replacement。

## 2. 当前状态

- Experimental
- Pre-alpha
- Bootstrap completed
- Consumer Bridge 已完成并通过微信开发者工具验证
- 微信强类型 interop 基础已实现
- Host 与 Capability 边界 contract 已实现
- 公共错误模型与 internal callback-to-coroutine primitive 已实现
- Storage capability 已实现并通过微信开发者工具验证
- 微信客户端 authentication bootstrap 已实现并通过微信开发者工具验证
- HTTP transport capability 已实现并通过微信开发者工具验证
- App 级 lifecycle capability 已实现，并通过微信开发者工具验证前台状态与页面 route
- 微信页面栈导航桥接已实现，并通过微信开发者工具验证
- 运行时能力检测与版本门控已实现，并通过微信开发者工具与真机验证
- 权限生命周期已实现，并通过微信开发者工具与真机验证
- 隐私授权已实现，并通过自动化检查覆盖
- 微信会话有效性检查已实现，并通过自动化检查与 Android 真机验证
- 微信剪贴板读写与短/长震动已实现，并通过自动化检查、开发者工具与 Android 真机验证
- 微信文件系统读取、写入、检查与删除已实现，并通过自动化检查、开发者工具与 Android 真机验证
- 微信按需单次定位已实现，并通过自动化检查、微信开发者工具与 Android 真机验证
- 微信扫码已实现，并通过自动化检查、微信开发者工具与 Android 真机验证
- 微信媒体选择已实现，并通过自动化检查、开发者工具与 Android 真机运行验证
- 微信订阅消息请求已实现并通过自动化检查；Android 真机已验证运行时支持与零模板保护，弹窗结果仍受阻于当前 AppID 下的有效模板
- 微信标准支付已实现，并作为「后端参数的类型化转发器」通过自动化检查；Android 真机已验证运行时支持与「未配置参数」保护，真实支付验收仍受阻塞于合法商户环境与可信后端
- 虚拟支付未实现：不存在请求、结果、capability 条目或导出，且所带开发者工具基础库中没有可发现的虚拟支付契约。其边界记录于 ARCHITECTURE，状态在能力矩阵中为 `Planned`
- `io.github.bobcgn.miniapp` Gradle 插件已实现并有自动化覆盖。把它应用到 Kotlin Multiplatform 项目会提供 `miniappMain` 与 `miniappTest`，二者是由 compilation 拥有、以 `commonMain` / `commonTest` 为父边的真实 source set；绑定 Node.js test run 使 `miniappTest` 真正执行测试；把 runtime SDK 接入 `miniappMain`，使消费者无需声明 artifact 即可针对公开 API 编译；并提供 `assembleMiniAppBundle`，把 Kotlin/JS production library —— 消费者模块与其声明，以及它所需的 runtime 模块 —— 重新发布到 `build/miniapp/bundle`，作为 compiler-managed Mini App distribution。它会对未应用 Kotlin Multiplatform 插件的项目报错，也会在 `miniappRuntimeClasspath` 携带客户端 renderer 时报错。它通过 `miniapp { wechat { } }` extension 配置，其中唯一的设置是当前宿主的 bundle 目录，默认为 `build/miniapp/bundle`
- 若消费者的 `commonMain` 依赖另一个 Kotlin Multiplatform project，则必须对该 project 同样应用 Mini App 插件：该依赖经由 Mini App variant 解析，不提供该 variant 的 project 会产生 variant 解析错误，且不存在自动回退。把依赖移出 `commonMain` 以规避该问题是不可接受的变通
- 客户端 / runtime 架构边界已记录并被强制：后端拥有业务事实，本 SDK 拥有客户端行为与宿主能力，宿主拥有渲染。当 runtime SDK 导入或声明 UI framework namespace 或 artifact 时，`:kmp-miniapp-sdk:checkArchitectureBoundaries` 会让构建失败，且它是 `:kmp-miniapp-sdk:check` 的一部分

构建基线与第一条端到端消费链路均已通过验证。Common layer 包含最小 Host identity、Capability support 与强类型 platform escape-hatch contracts。Production code 通过 Host 与 adapter boundaries 调用 typed 微信 Storage contracts，完整 Storage 链路已通过真实宿主验证。

首个 Storage capability implementation 已通过 Kotlin/JS、fake-host、CommonJS 和 TypeScript 自动检查。用户提供的微信开发者工具证据也验证了真实 runtime 中的读取、覆盖、删除与 key 不存在语义。

微信专属 authentication adapter 调用 typed `wx.login`、返回 `WeChatLoginResult`，并通过公共错误模型映射宿主失败。它不会建立已认证用户或 session。Kotlin/JS、CommonJS 与 TypeScript 自动检查均已通过；用户提供的微信开发者工具证据确认真实 runtime 能取得非空 login code，且没有暴露该 code。

HTTP transport capability 定义了宿主无关的 request 与 response 契约，通过微信 adapter 适配 typed `wx.request`，并将传输失败与超时映射到公共错误模型。`wx.request` 还会返回 abort handle，因此这是第一个在协程取消时会同时中止底层宿主操作的 capability。Kotlin/JS、fake-host、CommonJS 与 TypeScript 自动检查均已通过。用户提供的微信开发者工具验收确认 index 页面的 network 检查可在真实 runtime 中运行。

App 级 lifecycle capability 建模应用是否呈现在用户面前。它是唯一被建模为公共概念的生命周期；Page 级生命周期与页面栈导航属于微信语义，保留在 platform escape hatch 之后。微信 adapter 转发消费者传入的 App 钩子。自动化检查已通过；用户于 2026-09-15 确认微信开发者工具中的前台状态与页面 route 验收通过。后台切换仍无法从开发者工具模拟器触发。

微信导航桥接适配 `wx.navigateTo`、`wx.redirectTo` 与 `wx.navigateBack`，并将页面栈已满、route 未知、以及在首页执行返回映射为 `MiniAppException.HostFailure`，而不是报告成功。自动化检查已通过；用户于 2026-09-15 确认三种导航操作均在微信开发者工具中验收通过。

Capability support 由正在运行的宿主回答，而不是依据硬编码清单。`MiniAppHost.capabilitySupport` 报告 `Supported`、`Unsupported`、`VersionDependent` 或 `PermissionDependent`，`requireSupported` 会把除 `Supported` 外的每个状态转换为 `MiniAppException.UnsupportedCapability`。微信 gate 读取 `wx.canIUse` 与基础库版本，优先使用 `wx.getAppBaseInfo`，并以已停止维护的 `wx.getSystemInfoSync` 作为回退，因此同一份构建在两个宿主上可以给出不同答案。版本与平台读取推迟到各自首次使用并分别缓存；`wx.canIUse` 保持实时查询。Kotlin/JS、fake-host、CommonJS 与 TypeScript 自动检查均已通过。开发者工具（基础库 3.17.2）与 Android 真机（OnePlus PLQ110、Android 36、微信 8.0.76）均已验证 `Supported` 与 `Unsupported` 两种状态。

## 3. 当前 Gradle Modules

通过 `./gradlew projects` 验证：

```text
Root project 'kmp-miniapp-sdk'
+--- Project ':miniapp-gradle-plugin'
\--- Project ':kmp-miniapp-sdk'
```

`:kmp-miniapp-sdk` 是本文档其余部分描述的 Kotlin/JS runtime SDK；其源码位于 `sdk/`，而其 Gradle project 名（也就是发布的 artifact id）是 `kmp-miniapp-sdk`，这正是 composite build substitution 匹配的名字。`:miniapp-gradle-plugin` 是 `io.github.bobcgn.miniapp` Gradle 插件，只负责构建集成与开发体验：它检测 Kotlin Multiplatform 插件，注册 [ADR 0010](decisions/0010-miniapp-gradle-plugin-source-set-model-ch.md) 选定的 `miniapp` 平台 target 及其 Node.js test run，把 runtime SDK 接入 `miniappMain`，并在缺少 Kotlin Multiplatform 插件时给出指明原因的报错。注册 target 才使 `miniappMain` 与 `miniappTest` 成为由 compilation 拥有、并带有 `commonMain` / `commonTest` 父边的真实 source set；test run 才使 `miniappTest` 拥有一个真正执行的任务；该依赖才使消费者能够直接针对公开 API 编写 `miniappMain` 代码而不必声明任何 artifact。它还提供 `assembleMiniAppBundle`，把 Kotlin/JS production library 重新发布到一个宿主集成可以依赖的稳定路径。该插件不暴露 Mini App DSL，也不包含微信 runtime 代码。

插件接入的 runtime 坐标为 `io.github.bobcgn:kmp-miniapp-sdk:<version>`。`gradle/libs.versions.toml` 是该版本的唯一来源：插件侧的坐标由它生成成资源，SDK 自身的 `MiniAppSdk.VERSION` 也由同一处生成，因此二者都不可能偏离实际发布的版本。插件只把它加入 `miniappMain`，`miniappTest` 通过 source-set hierarchy 继承。SDK 目前尚未发布到任何仓库，因此该坐标当前经 composite build 解析；正式发布后由仓库解析同一坐标。

`examples/` 是 integration host 目录，不是 Gradle module。

`poc/kgp-model` 是 BOB-83 的 Kotlin Gradle Plugin 模型 PoC。它是一个独立 Gradle build，根构建不包含它，因此不为 SDK 贡献任何 module、target 或依赖。其决策记录于 [ADR 0010](decisions/0010-miniapp-gradle-plugin-source-set-model-ch.md)。

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
- 稳定的编译器模块名 `kmp-miniapp-sdk-kotlin`
- 在 `jsMain/.../export` 下提供最小 `@JsExport` facade
- 为消费者提供极薄的 CommonJS export normalization wrapper
- 用于 consumer distribution 的根级 `buildMiniAppSdk` 任务

编译器生成的 TypeScript declaration 已包含版本、Promise-based Storage 与 Promise-based HTTP transport exports。编译器的 CommonJS export shape 带有命名空间，因此 consumer-facing wrapper 在不包含业务逻辑的前提下提供稳定的扁平函数。`buildMiniAppSdk` 会将编译器模块、类型声明、runtime dependencies 与外部 source maps 同步到微信示例，同时保留该 wrapper。

## 6. 当前依赖

| 用途 | 依赖 | 版本来源 | 版本 |
| --- | --- | --- | --- |
| 构建插件和 Kotlin libraries | Kotlin Multiplatform / Kotlin | `gradle/libs.versions.toml` | 2.4.20 |
| 生产代码 | `org.jetbrains.kotlinx:kotlinx-coroutines-core` | `gradle/libs.versions.toml` | 1.11.0 |
| 测试 | `kotlin-test` | Kotlin plugin version | 2.4.20 |
| 测试 | `org.jetbrains.kotlinx:kotlinx-coroutines-test` | `gradle/libs.versions.toml` | 1.11.0 |

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

`export` 边界包含版本 facade、Promise-based Storage functions、微信专属 login bootstrap、Promise-based HTTP transport function、供消费者转发的 App 与 Page lifecycle 入口、微信导航函数、capability support 与 runtime inspection 查询、权限生命周期函数、微信专属的会话检查、四项微信剪贴板与震动函数、五项微信文件系统函数、微信定位函数、微信扫码函数、微信媒体选择函数、微信订阅消息请求函数、网络状态函数，以及两个微信传输函数。Interop 边界包含 `login`、`showToast`、Storage、`request`、三个页面栈导航方法，以及带 presence guard 的 runtime inspection 成员的 internal 契约；production adapters 通过 `WechatHost` 调用 login、Storage、HTTP transport、导航、runtime inspection 与权限生命周期，`showToast` 仍仅为 interop contract。`runtime` 边界包含实现公共 lifecycle capability 的 `WechatAppLifecycle`、跟踪 runtime 报告为已显示页面的 `WechatPageLifecycle`，以及依据 runtime 报告判定支持状态的能力目录表与 gate。`adapter` 边界另外包含 `WechatPermissionScopes`，这是微信 scope 字符串唯一存在的地方。

## 9. 当前能力

- Kotlin Multiplatform 项目可完成配置和构建。
- `:kmp-miniapp-sdk` JavaScript library target 可编译。
- 可生成生产用 CommonJS library 文件。
- 已配置 TypeScript definition generation 和 validation。
- `./gradlew buildMiniAppSdk` 无需手工复制即可准备微信 integration host 所需的全部 compiler-managed 产物。
- 编译器模块使用稳定名称 `kmp-miniapp-sdk-kotlin`；外部 source maps 会复制用于本地调试，并被 Git 忽略。
- `MiniAppExports.sdkVersion()` 可导出共享的 `MiniAppSdk.VERSION` 值。
- `examples/wechat-miniprogram/miniprogram/libs` 下的 consumer-facing CommonJS wrapper 暴露 `sdkVersion(): string`。
- 严格 TypeScript consumer 与 Node/CommonJS smoke test 均通过，consumer contract 不依赖 `any`。
- `examples/wechat-miniprogram` 已包含加载同一消费产物并显示版本值的最小 index 页面。
- 微信开发者工具能够加载该产物，在 console 输出 `0.1.0-SNAPSHOT`，并在 index 页面显示相同值。
- 一个 common test 可在 Node.js Kotlin/JS test environment 中运行。
- 已启用 Explicit API mode。
- `commonMain` 已定义 `MiniAppHost`、`HostPlatformApi`、`HostVersion`、`CapabilityKey` 以及 `Supported` / `Unsupported` / `VersionDependent` / `PermissionDependent` capability states，以及把任何非支持状态转换为 `MiniAppException.UnsupportedCapability` 的 `requireSupported` guard。
- `jsMain/.../host/wechat/interop` 已定义 internal 全局 `wx` 契约、强类型 callback results，以及首批 toast 和 storage 方法的强类型 option bags。
- 这些 option bags 的 plain-object factories 已在 Kotlin/JS Node test environment 中通过测试，不需要真实 `wx` runtime。
- `commonMain` 已定义不含 raw JavaScript value 的语义化 `MiniAppException` hierarchy。
- Internal `awaitHostCallback` primitive 保证只有一个 terminal callback 生效、忽略 cancellation 后 callback，并最多调用一次可选 Host abort hook。
- 微信 adapter boundary 已将强类型 raw callback failure 映射为 `MiniAppException.HostFailure`，同时保留 scalar diagnostics。
- `MiniAppStorage` 已定义字符串 get/set/remove 语义，包括覆盖、幂等删除以及 key 不存在时返回 `null`。
- `WechatHost` 通过 `StorageCapabilityProvider` 提供 `MiniAppStorage`；`WechatStorage` 将其连接到 typed 微信 storage interop 和 coroutine adaptation。
- JavaScript / TypeScript facade 已暴露 Promise-based `storageGet`、`storageSet` 和 `storageRemove` functions。
- 自动测试覆盖 Storage 语义、微信 adaptation、invalid response、Host error、cancellation 和 CommonJS consumption。
- 微信开发者工具已验证 Storage 的读取、覆盖、删除与 key 不存在语义；页面显示 `PASS`，详情为 `first=first, overwritten=second, missing=null`。
- Typed `wx.login` interop 覆盖 option bag、成功结果 `code`/`errMsg` 与失败结果 `errMsg`/`errno` shapes。
- `WechatAuth` 将 callback 适配为 coroutine result、拒绝空 code，并在不暴露 raw JavaScript values 的前提下映射宿主失败。
- TypeScript facade 暴露不依赖 `any` 的 `wechatLogin(): Promise<WeChatLoginResult>`；示例不会记录或渲染 raw code。
- `MiniAppHttpTransport` 定义了宿主无关的 request 与 response 契约，仅承载文本 body 与字符串值 headers，不包含 payload 序列化或重试策略。
- 完成的 exchange 对包括 `4xx` 与 `5xx` 在内的所有 HTTP status 都返回 response；只有传输层失败才会成为 `MiniAppException`。
- `MiniAppException.Timeout` 与 `MiniAppException.HostFailure` 相互独立，仅当宿主表明 exchange 超出 timeout 时上报。
- Adapter 请求原始文本响应（`dataType: 'text'`），避免宿主返回字符串契约无法表示的已解析对象；非文本 body 会映射为 `InvalidResponse`。
- `WechatHost` 通过 `NetworkCapabilityProvider` 提供 `MiniAppHttpTransport`；`WechatNetwork` 将其连接到 typed `wx.request` interop 和 coroutine adaptation。
- `wx.request` 会返回 abort handle，因此这是第一个在协程取消时同时中止底层宿主操作的 capability，且最多调用一次。
- Typed `wx.request` interop 覆盖 option bag、含 `statusCode`/`header`/`data` 的成功结果、含 `errMsg`/`errno` 的失败结果，以及返回的 task handle。
- JavaScript / TypeScript facade 暴露不依赖 `any` 的 `networkRequest(url, init): Promise<NetworkResponse>`。
- 自动测试覆盖 request 转发、response 映射、header 转换、invalid response、timeout 映射、host failure 映射，以及取消时的宿主中止。
- `MiniAppLifecycle` 定义 App 级 lifecycle capability：当前 `MiniAppLifecycleState` 与后续变化的 `Flow`。它是唯一事件形态的 capability，也是唯一被建模为公共概念的生命周期。
- `WechatAppLifecycle` 由 `App.onLaunch` 与 `App.onShow` 上报前台、`App.onHide` 上报后台来实现该契约。
- `WechatPageLifecycle` 跟踪 runtime 报告为已显示的页面 route。Page 级生命周期不是公共 capability，仅通过 `WechatPlatformApi` 可达。
- `WechatNavigation` 适配 `wx.navigateTo`、`wx.redirectTo` 与 `wx.navigateBack`，仅通过 `WechatPlatformApi` 可达，并将微信的 failure callback 映射为 `MiniAppException.HostFailure`。
- JavaScript / TypeScript facade 暴露 App 与 Page lifecycle 入口、lifecycle 状态与页面 route，以及三个导航函数。
- `WechatCapabilityGate` 依据 `wx.canIUse` 与基础库版本判定支持状态，优先使用 `wx.getAppBaseInfo`，并在早于它的基础库上回退到已停止维护的 `wx.getSystemInfoSync`。每一次 runtime inspection 读取都有 guard，因此 `wx` 缺失时得到的是 `Unsupported` 而不是崩溃。
- `WechatRuntimeInfo` 报告基础库版本、runtime platform，以及该 runtime 是否为开发者工具。版本与平台分别在首次使用时取值并保留；`canIUse` 保持实时查询，因此构造 SDK 的过程绝不触碰 `wx`，支持答案也不会因标量缓存而冻结。
- 微信能力目录表记录每个纳入 gate 的能力所需条件。Storage 与 network 声明 `wx.canIUse` 必须确认的 schema，且不设最低版本；App lifecycle 没有可探测的宿主 API；runtime detection 记录有文档依据的 `2.20.1` 边界，这正是版本依赖答案可复现的原因。
- JavaScript / TypeScript facade 暴露不依赖 `any` 的 `capabilitySupport`、`requireCapability`、`wechatRuntimeInfo` 与 `wechatCanIUse`。
- `MiniAppPermissions` 定义宿主无关的权限生命周期：`PermissionKey` 命名权限「为了什么」，`PermissionState` 为 `NotRequested`、`Granted` 或 `Denied`，契约提供查询、请求与打开设置。`WechatHost` 通过 `PermissionCapabilityProvider` 提供它。
- `WechatPermissionScopes` 是微信 scope 字符串唯一存在的地方。当前映射麦克风与位置权限；adapter 未映射的 key 会在任何宿主调用之前失败。
- 不做任何缓存：每次查询与每次设置页返回都询问宿主，因为用户随时可以在宿主自身的设置中改变权限。
- 拒绝是 `MiniAppException.PermissionDenied`，宿主无法作答是 `HostFailure`；只有报告授权被拒绝的失败消息才会成为拒绝。
- 同一权限的并发请求共享由服务拥有的单次宿主调用；已有决定的权限由宿主状态作答，不会再次弹窗。
- JavaScript / TypeScript facade 暴露 `permissionState`、`requestPermission` 与 `openPermissionSettings`，使用稳定的字符串状态 union，不依赖 `any`。
- `WeChatSessionState` 针对 `wx.checkSession` 报告 `VALID` 或 `INVALID`。它属于微信专属并带命名空间（`wechat.check-session`），而不是宿主无关的认证 capability，因为没有其他宿主已知共享这些语义。
- `wx.checkSession` 的 success/fail callback 本身分别映射为 `VALID`/`INVALID`，不解析可能随语言或基础库变化的 `errMsg`。
- 会话检查只是查询：`INVALID` 结果不会获取 code、不交换 session、不刷新 token、不做任何重试。有效结果只说明微信客户端登录态完好，不代表用户已认证或后端 session 有效。
- JavaScript / TypeScript facade 暴露 `wechatCheckSession`，使用 `Valid` / `Invalid` union，不依赖 `any`。
- `WeChatDeviceCapabilities` 命名四项微信专属设备能力：`wechat.clipboard-read`、`wechat.clipboard-write`、`wechat.vibrate-short` 与 `wechat.vibrate-long`。每项独立门控，因为宿主可能只提供其中一个剪贴板方向或一种震动时长。
- `WechatClipboard` 读写系统剪贴板文本。空剪贴板读取为空字符串；缺失或非字符串的答案映射为 `InvalidResponse`；两个方向都不保存读取到的内容。
- `WechatHaptics` 把短震动与长震动实现为两次独立调用。二者都不报告强度，也都不声称设备发生了震动。
- 剪贴板 API 以基础库 1.1.0 为最低版本、震动 API 以 1.2.0 为最低版本，且各自都通过 `wx.canIUse` 探测。`wx.vibrateShort` 的可选 `type` 字段（heavy / medium / light，基础库 2.13.0）有意未建模。
- JavaScript / TypeScript facade 暴露不依赖 `any` 的 `wechatGetClipboardText`、`wechatSetClipboardText`、`wechatVibrateShort` 与 `wechatVibrateLong`。
- `WechatFileSystem` 在小程序文件沙箱中读写 UTF-8 文本，并检查与删除沙箱路径。它不提供目录、stream、descriptor、数据库或 secure storage 操作。
- 五个文件系统 key（`wechat.filesystem-read`、`-write`、`-access`、`-remove`、`-sandbox-path`）分别门控。manager 是宿主对象，因此只有当对应方法存在时才报告该项支持。
- 文件系统 API 以基础库 1.9.9 为最低版本；能力目录表为 `wx.canIUse` 无法回答的宿主对象成员提供了专用 presence 探测。
- `access` 仅在微信为「路径不存在」提供文档依据的失败文本上返回 `false`；其他失败一律抛出，因此权限错误永远不会被报告为文件不存在。
- JavaScript / TypeScript facade 暴露不依赖 `any` 的 `wechatUserDataPath`、`wechatReadTextFile`、`wechatWriteTextFile`、`wechatFileExists` 与 `wechatRemoveFile`。
- `WechatLocation` 只读取一次位置；不监听、不缓存、不上传。仅通过 `WechatPlatformApi` 可达。
- `WeChatCoordinateSystem` 命名微信可作答的两种坐标系统。SDK 始终显式设置其一，默认 `gcj02`，因为那是微信自身地图视图接受的形式；绝不静默采用宿主默认值。
- `PermissionKey.Location` 是宿主无关的权限，在唯一的 adapter 映射点内映射为 `scope.userLocation`。
- Adapter 在调用 `getLocation` 之前查询并强制宿主隐私协议与位置权限；两项检查都不展示界面，也不代替用户接受协议或请求权限。
- 定位 API 未记录最低基础库版本，因为微信页面未标注；以 `wx.canIUse` 为准。
- JavaScript / TypeScript facade 暴露 `wechatGetCurrentLocation`，使用 `GeoPosition` 与 `'wgs84' | 'gcj02'` union，不依赖 `any`。
- `WechatScanCode` 只把一次用户手势变成一次宿主扫码；不重试、不解析内容、不缓存、不上传。仅通过 `WechatPlatformApi` 可达。
- `WeChatScanCategory` 命名请求可用的四个粗粒度类别（`barCode`、`qrCode`、`datamatrix`、`pdf417`），`WeChatScanFormat` 命名宿主在结果中报出的具体格式。两者词汇不同，类型名称刻意不混用；空类别集合表示「不限」，而不是「都不要」。
- 真机证明微信对用户关闭扫码界面和系统相机权限阻止界面启动返回相同信号；两种精确的宿主消息因此映射为 `MiniAppException.HostInteractionInterrupted`，不再推断用户取消。其他消息保留为 `HostFailure`。
- 扫码结果的 `result` 必须存在且为字符串，否则映射为 `InvalidResponse`；描述性字段缺失时保持缺失而不是填空字符串，宿主给出的类型错误也不会被丢弃。
- 扫码不请求任何权限：`wx.scanCode` 驱动的是微信自身界面，其权限前置条件无法从宿主契约中得到依据，因此 SDK 既不为它弹窗，也不把它映射到 `scope.camera`。
- `WechatChooseMedia` 只把一次用户手势变成一次微信自身界面的选择；不重试、不解析、不存储、不上传所选内容，仅通过 `WechatPlatformApi` 可达。
- 请求只校验 SDK 能精确陈述的部分，其余一律拒绝：`mediaType` 必填，因为微信自己的 schema 把它标为 required；`count` 必须至少为 1；`maxDurationSeconds` 必须落在微信文档给出的 3 至 60 之内。无效输入会被拒绝而不是被悄悄修正，而宿主的数量上限留给宿主，因为它取决于宿主的基础库。
- `WeChatMediaType`（`image`、`video`、`mix`）、`WeChatMediaSource`（`album`、`camera`）、`WeChatMediaSizeType` 与 `WeChatCameraPosition` 命名请求词汇，`WeChatMediaFileType` 命名宿主报回的类别。`sizeType` 文档说明仅对图片有效，`camera` 仅在请求了相机来源时生效；两者都原样转发，由宿主决定其效果。
- 选择结果的 `tempFilePath` 必须是非空字符串，`size` 必须是完整的非负字节数，否则整个答案是 `InvalidResponse`。视频的 `duration`、`width`、`height` 与 `thumbTempFilePath` 在宿主未报告时保持缺失，因为伪造 0 等于声称图片时长为零且没有像素。
- 宿主报出本 SDK 不认识的类别不是失败，且会同时保留宿主的原名。
- 媒体选择不查询也不请求任何权限：微信自身的选择界面不需要权限，宿主的 scope 列表中也没有读取媒体库的 scope，因此 SDK 即使想做映射也没有对象。
- 选择结果返回的临时路径属于宿主与产生它的那次会话。SDK 不保留副本、不声称其生命周期，也不代替调用方把媒体复制到任何地方。
- `WechatRequestSubscribeMessage` 只把一次用户手势变成一次针对模板列表的请求；它从不发送消息、不管理模板，仅通过 `WechatPlatformApi` 可达。它刻意不是通用推送通知能力：订阅微信模板是微信概念，没有任何其他宿主被证明共享它。
- 用户手势由调用方负责。微信要求如此，因此 adapter 从不在加载时请求、不代替用户提供手势，也不会在没有手势的情况下重试一次拒绝。
- 模板 ID 必须非空白；重复项会折叠并保留调用方顺序。不施加数量上限，因为该 API 的可离线来源都没有给出上限，而本仓库不会编码一个无法引用的数字。
- 答案按模板 ID 读取而不是按位置读取，且宿主自身的 `errMsg` 状态行会先被排除，之后才轮到把其他内容当作模板：把它当成模板会凭空造出一个调用方从未请求过的模板。
- 合法结果按调用方顺序为每个请求模板各给一条。缺少、多出、空白或非文本条目均为 `InvalidResponse`，因为接受它们会破坏请求与答案的关联。
- `WeChatSubscriptionStatus` 只命名 `accept`，并刻意不命名更多：该 API 的可离线来源完全没有给出状态词汇。整个已安装基础库中唯一观察到的状态字符串是模拟器订阅弹窗预览载荷里的 `accept`，那属于弹窗渲染数据而不是 API 结果。其他状态一律原样保留在 `hostStatus` 中而 `status` 为 `null`，因此调用方依据宿主实际所说行动，真机运行可以凭证据扩展该集合。
- 「同意」是订阅状态而不是送达。SDK 中没有任何部分把消息报告为已发送；端到端送达需要微信后台模板与可信后端，属于本能力之外的 `BackendRequired` 事项。
- 订阅请求不查询也不请求任何权限：该 API 的可离线来源没有给出任何 scope。这是关于证据的结论，SDK 既不假定 BOB-60 尚未完成的隐私工作适用于它，也不假定不适用。
- 网络状态是公共 capability，因为每个宿主都能回答「是否在线、通过什么链路」这种问题，且该模型不需要任何宿主专属部分。词汇不是公共的，因此 `NetworkType` 命名 SDK 能识别的取值，`hostNetworkType` 始终携带宿主自己的词；SDK 从未见过的类型会被如实报告，而不是映射成 `UNKNOWN`。
- 查询与监听是两个独立的 capability key（`network-status-query`、`network-status-listener`），因为宿主可能能回答该问题却不提供变更事件。
- `MiniAppNetworkStatus.changes` 只转发宿主推送的内容：不做轮询，不在开始收集时合成任何读数，每个 collector 注册自己的宿主 listener 并在结束时恰好移除一次，结束之后到达的事件不会再被投递。
- 上传与下载保留在微信逃生口之后，key 为 `wechat.upload-file` 与 `wechat.download-file`。它们可移植的部分——method、URL、headers、status、文本 body——已经属于公共 HTTP transport capability；不可移植的是文件，也就是宿主路径，而本 SDK 刻意没有可移植的文件引用。两个方向各自按 API 门控。
- 传输报告宿主自己的 task：`abort()` 停止它（最多一次，并会说明是否真的触发了宿主 abort），`lastProgress()` 报告宿主给出的最后一个数值而不是估算值，取消等待也会停止宿主操作。进度 listener 最多注册一次，并在包括 abort 在内的每条终止路径上被移除。
- 已完成的传输就是一次已完成的 exchange：非 2xx 状态会 resolve，与 HTTP transport capability 完全一致，只有传输层失败才 reject。超时是宿主的确切消息 `uploadFile:fail timeout` 或 `downloadFile:fail timeout`（由该基础库自身的错误映射产出）；仅仅提到 timeout 的消息仍然是 `HostFailure`。
- 网络扩展不查询也不请求任何权限。访问某个 host 需要它位于消费者的 request domain 列表中，那是对小程序的配置约束而不是 SDK 能索取的权限；可离线来源也没有把任何隐私条件与这些 API 绑定。
- `wechatRequestPayment()` 只把可信后端产出的五个参数 —— `timeStamp`、`nonceStr`、`package`、`signType` 与 `paySign` —— 转发给微信自身的支付界面，并且只报告宿主说「交互已完成」。SDK 不计算签名、不持有商户密钥、不获取预支付标识，也没有订单模型。空白字段在调用宿主之前就被拒绝，`signType` 是基础库所接受的两个取值构成的封闭集合。
- 支付相关的任何内容都不会被 SDK 记录、缓存或持久化：`nonceStr`、`package` 与 `paySign` 是一次交互的凭据。导出结果只带 `interactionCompleted` 一个字段，因此一次成功调用不可能被误读为订单已支付，SDK 也从不用 `paid`、`settled` 或 `confirmed` 命名任何结果。
- 支付不请求任何权限：可离线来源没有为该 API 给出任何 scope 或隐私条件。商户是否已配置、订单是否存在、参数是否可接受，都是宿主被调用时才给出的答案，因此其中任何一项都不会被报告为「宿主不支持」。
- 确切消息 `requestPayment:cancel` —— 即所带基础库自身支付流程产出的形式 —— 映射为 `HostInteractionInterrupted`，且 SDK 不声称用户取消。`requestPayment:fail cancel` 及其他所有消息在合法商户环境提供真实关闭证据之前，保持 `HostFailure`。
- 虚拟支付未实现：不存在对应的请求、结果、capability 条目或导出。对同一离线基础库的探测结果是 `requestPayment` 出现 7 次（含其 `canIUse` 元数据条目），而 `requestVirtualPayment` 在整个文件中出现 0 次，因此该来源无法提供正向能力证据；本次探测没有执行运行时能力检查。这些是关于单一离线来源的事实，不是关于真实微信客户端的事实：参数表、最低基础库版本、平台可用性以及账号/类目资格均未确立，该能力在能力矩阵中保持 `Planned`，SDK 中没有任何注册。[ARCHITECTURE-ch.md](ARCHITECTURE-ch.md) 固定了未来实现必须遵守的边界 —— 独立 key、独立请求与结果模型、两种支付产品之间不存在降级路径，以及没有客户端签名、密钥、会话或订单。

- 自动测试覆盖版本比较与解析、四种支持状态、版本边界本身、版本不可读时的回退、完全无法探测的宿主、runtime 只读取一次，以及 `UnsupportedCapability` guard。
- 微信开发者工具已验证前台 lifecycle 状态、页面 route，以及 `navigateTo` → `redirectTo` → `navigateBack` 的完整页面栈路径。
- [TESTING-ch.md](TESTING-ch.md) 记录了两层测试体系、各层可用的 fake，以及可复现的真实宿主检查清单。
- `commonTest` 定义了宿主无关的 FakeHost 边界：`FakeMiniAppHost`、`InMemoryStorage`、`RecordingHttpTransport` 与 `FakeMiniAppLifecycle`。
- `jsTest` 定义了面向原始微信 callback port 的 FakeAdapter 边界，使微信 adapter 无需微信 runtime、也无需 `wx` 即可被驱动。
- Storage、transport 与 App lifecycle 契约各自只声明一次为共享检查，并分别跑在宿主无关参考实现与微信 adapter 上。
- 没有任何自动化测试声称能够访问真实 `wx` runtime，也没有任何自动化检查可以替代真实宿主验证。

## 10. 明确尚不具备的能力

项目当前没有：

- Router 或导航栈框架
- UI 组件生命周期抽象
- 地图 SDK、地图 UI、后台或持续定位、`startLocationUpdate`、`onLocationChange`、地理围栏、轨迹记录、逆地理编码、第三方地图服务、原生 `map` 组件、位置缓存、位置上传播，以及 `chooseLocation` 与 `openLocation`
- 超出小程序沙箱内 UTF-8 文本的文件系统能力：没有目录、目录遍历、递归删除、stream、file descriptor、随机访问、文件监听、数据库、secure storage，也没有二进制或 base64 文件内容
- Node `fs` 或任何 POSIX 文件抽象
- 受隐私授权约束、但本 SDK 未实现的设备能力，例如 Media 与 Bluetooth
- 相机原生组件、连续视觉识别、自建二维码或条码解析器、通用相机界面，以及扫码结果的业务解析、持久化或上传
- 所选媒体的播放器、编辑器、压缩或转码器；图片识别；上传、下载或后端存储；对宿主临时文件的长期管理；微信媒体 API 的完整封装；以及宿主选择界面本身
- 通用推送通知抽象；由后端发送订阅消息；模板管理；静默或页面加载时的订阅请求；以及任何「已同意订阅即表示消息已发送或将要送达」的说法
- 客户端签名、商户密钥或证书、订单创建或任何订单模型、退款、对账，以及 `requestVirtualPayment` 边界；也包括任何「客户端支付交互完成即代表订单已支付」的说法
- 带重试、缓存或认证的通用 HTTP 客户端；断点续传、下载缓存或下载管理器；读取、移动、解压或解析下载内容；WebSocket 客户端；以及任何「下载结果的路径会超出本次会话」的说法
- Request 或 response body 序列化、cookie 处理、redirect 策略、streaming、upload 或 download
- 服务端 code exchange、已认证用户/session 管理和 token refresh
- 公共通用 callback-to-coroutine API
- Compose integration、UI DSL、renderer 或 Virtual DOM
- Presentation Core：`UiState`、`Action`、`Store`、`Effect` 与 state machine 已有明确定义归属（未来的 `presentation` module，P2），但不存在任何实现、module 或占位类型
- Mini App renderer、view tree、layout engine、Kotlin view DSL 或 WXML generator；`miniappMain` 与 `jsMain` 只做宿主状态与宿主事件的 binding，从不渲染
- 可以携带客户端 renderer 的 Mini App 宿主 bundle：当 `miniappRuntimeClasspath` 携带 Compose、Skiko、Compose 专用的 AndroidX 集成 artifact、`kotlinx-browser` 或 `kotlinx-html` 时，`checkMiniAppHostBoundary` 会让构建失败。非 Compose 的 lifecycle、saved-state 与 navigation primitives 仍允许作为共享行为。消费者的 `miniappMain` 可以继承共享行为与 state，但 Compose UI 必须位于不作为 `miniappMain` 父 source set 的客户端 module 或 source set 中
- npm publication
- Maven publication
- 承载宿主业务配置的 Gradle DSL。`miniapp { }` extension 已存在，且只承载一个构建设置 —— 微信宿主的 bundle 目录 —— 并有意不含其他内容：没有 AppID 或密钥、没有商户或支付材料、没有订单或签名数据、没有 API token、没有模板 ID、没有 presentation state、没有 markup 或 View 定义。插件会组装 compiler-managed Mini App distribution，但不执行上传、发布或宿主部署，且没有任何小程序宿主加载过它：其宿主兼容性尚未验证
- 第二个 Mini App 宿主。当前只有微信；extension 的形状使得新增宿主是「新增一个 `MiniAppHostConfiguration` 子类型与一个访问器」，而不是把微信那个类型加宽
- 支付宝或 Telegram Host implementations

## 11. 已验证命令

验证日期：2026-09-15。

| 命令 | 结果 |
| --- | --- |
| `./gradlew projects` | VERIFIED |
| `./gradlew clean build` | VERIFIED |
| `./gradlew :kmp-miniapp-sdk:jsNodeTest` | VERIFIED |
| `./gradlew :kmp-miniapp-sdk:jsNodeProductionLibraryDistribution` | VERIFIED |
| `./gradlew buildMiniAppSdk` | VERIFIED — 重复执行可复用 configuration cache，且任务为 up to date |
| 在 `examples/wechat-miniprogram` 执行 `npm run smoke` | VERIFIED |
| 在 `examples/wechat-miniprogram` 执行 `npm run typecheck` | VERIFIED |
| 微信开发者工具加载、console、页面渲染、lifecycle 与导航 | VERIFIED — 用户确认前台状态、页面 route 与三种导航操作验收通过 |
| 微信真机调试（Android） | VERIFIED — OnePlus PLQ110 / Android 36 / 微信 8.0.76 / 基础库 3.17.3 [1641]；Runtime Detection、Storage、Authentication Bootstrap、HTTP Request、Clipboard 读写与短/长震动均 PASS，`platform=android` |
| 微信开发者工具中的 Clipboard | VERIFIED — 基础库 3.17.2；写入 PASS，读回 `matched=true` |
| 微信开发者工具中的 Permission | VERIFIED — 基础库 3.17.2；卡片显示 `Granted` 与 `Denied`，设置页返回的是宿主的决定 |
| 真机中的 Permission | VERIFIED — 完整走通 `Granted` → `Denied` → `DENIED` → `Granted`，拒绝后未出现第二次弹窗 |

2026-09-17 针对 BOB-83 的 Kotlin Gradle Plugin 模型 PoC 验证，均在 `poc/kgp-model` 下执行：

| 命令 | 结果 |
| --- | --- |
| `../../gradlew --no-daemon --console=plain clean check` | VERIFIED — 四个 PoC 模块全部 BUILD SUCCESSFUL |
| `../../gradlew --no-daemon --console=plain :model-c:miniappTest` | VERIFIED — `miniappNodeTest` 实际执行 4 个测试，无失败 |
| `../../gradlew --no-daemon --console=plain :model-c:checkMiniAppModel` | VERIFIED — 10 项模型要求全部通过 |
| `../../gradlew --no-daemon --console=plain --configuration-cache :model-c:check` | VERIFIED — configuration cache 条目成功存储并复用 |
| 在 commit `6812814` 上执行 `./gradlew --no-daemon --console=plain clean check` | VERIFIED — `:kmp-miniapp-sdk:jsNodeTest` 执行 598 个测试，无失败 |

2026-09-17 针对 Mini App Gradle 插件骨架的验证：

| 命令 | 结果 |
| --- | --- |
| `./gradlew --no-daemon --console=plain clean check` | VERIFIED — `:kmp-miniapp-sdk` 与 `:miniapp-gradle-plugin` 均 BUILD SUCCESSFUL |
| `./gradlew --no-daemon --console=plain :miniapp-gradle-plugin:test` | VERIFIED — 13 个测试，无失败 |
| `./gradlew --no-daemon --console=plain :kmp-miniapp-sdk:checkArchitectureBoundaries` | VERIFIED — SDK 未导入也未声明任何 UI framework |

早期微信开发者工具证据覆盖版本、Storage 与 authentication 检查；network 检查于 2026-09-14 单独完成验收。用户于 2026-09-15 确认 lifecycle 前台状态、页面 route，以及 `navigateTo`、`redirectTo`、`navigateBack` 的真实宿主验收通过。后台状态迁移不属于开发者工具模拟器可验证范围。权限生命周期定义了宿主无关的三态模型 —— `NotRequested`、`Granted`、`Denied` —— 由命名权限「为了什么」的 `PermissionKey` 标识，并通过微信 adapter 适配 `wx.getSetting`、`wx.authorize` 与 `wx.openSetting`。不做任何缓存；拒绝是 `MiniAppException.PermissionDenied` 而不是宿主失败；请求权限与打开设置绝不在缺少用户手势时执行。Kotlin/JS、fake-host、CommonJS 与 TypeScript 自动检查均已通过。微信开发者工具（基础库 3.17.2）与 Android 真机（OnePlus PLQ110、Android 36、微信 8.0.76）验证了：宿主能够报告 `Granted` 与 `Denied`；设置页返回的是宿主的决定而不是被假定为已授权；对已拒绝权限再次请求会报告 `DENIED` 且不出现第二次弹窗。`NotRequested` 无法在所用账号上产出，因为该账号已对所映射权限持有决定；该状态改由自动化测试覆盖。

隐私授权 capability 通过微信 adapter 适配 typed `wx.getPrivacySetting` 与 `wx.requirePrivacyAuthorize`，并报告宿主对其自身隐私协议的要求。它与权限有意分离：二者不共享任何状态，隐私拒绝是 `MiniAppException.PrivacyAuthorizationRequired` 而不是 `PermissionDenied`。可查询的要求、一次尝试的结果与 SDK 错误分别建模。Kotlin/JS、fake-host、CommonJS 与 TypeScript 自动检查均已通过。真实宿主隐私验收尚未进行：该要求取决于小程序的后台隐私配置与用户，只有开发者工具与真机运行才能确认。无论哪种情况都拿不到「用户已同意」的证明，因为宿主在用户已同意与小程序未声明任何收集类型时都会报告无需授权。

微信会话检查通过认证链路适配 typed `wx.checkSession`，报告 `WeChatSessionState.VALID` 或 `INVALID`。它有意保持微信专属，而不是一个宿主无关的认证概念；它只是查询：结果为失效时不会获取 code、不交换 session、不刷新 token。其结果只说明微信自身的客户端登录态仍然完好——不代表用户已认证、不代表消费者后端 session 有效、也不代表任何凭证仍被接受。success/fail callback 按微信 contract 分别映射为 `VALID`/`INVALID`，不解析 raw `errMsg`。自动化检查已通过；Android 真机已验证取得新 code 前为 `INVALID`、`wx.login` 成功后为 `VALID`。

微信剪贴板与震动 capability 适配 typed `wx.getClipboardData`、`wx.setClipboardData`、`wx.vibrateShort` 与 `wx.vibrateLong`。它们是微信专属设备能力，而不是宿主无关能力，因此各自以命名空间化的 key 独立门控（`wechat.clipboard-read`、`wechat.clipboard-write`、`wechat.vibrate-short`、`wechat.vibrate-long`），并通过 platform escape hatch 可达。剪贴板读取原样返回文本（包括空字符串），缺失或非字符串的答案报告为 `InvalidResponse` 而不是被强制转换。震动调用成功只表示微信接受了该调用；真机震动由握持设备的测试者确认。Kotlin/JS、fake-host、CommonJS 与 TypeScript 自动检查均已通过。开发者工具与 Android 真机已验证剪贴板读写，Android 真机已验证短震动与长震动。

微信文件系统 capability 适配 typed `wx.getFileSystemManager` 及其 `readFile`、`writeFile`、`access` 与 `unlink` 方法，仅操作微信小程序文件沙箱中的 UTF-8 文本。它属于微信专属能力，因此每项操作以命名空间化的 key 独立门控（`wechat.filesystem-read`、`-write`、`-access`、`-remove`），并与调用方据以构造路径的沙箱根（`wechat.filesystem-sandbox-path`）一起。manager 是宿主对象，因此只有当对应方法存在时才报告支持，而不是仅凭 manager 存在。空文件读取为空字符串；二进制内容映射为 `InvalidResponse`；宿主报告路径不存在时 `exists` 返回 `false`，其他失败一律抛出，因此权限错误永远不会被报告为文件不存在。删除不存在的文件会失败，因为微信的 `unlink` 就是这样。Kotlin/JS、fake-host、CommonJS 与 TypeScript 自动检查均已通过。2026-09-15 的微信开发者工具与 Android 真机证据在基础库 3.17.2 上验证了写入、读取匹配、存在、删除和删除后不存在的完整链路。

微信定位 capability 适配 typed `wx.getLocation`，按调用方要求的坐标系统返回 `latitude`、`longitude` 与 `accuracyMeters`。它属于微信专属能力，因为坐标系统（`wgs84` 或 `gcj02`）是本地测绘概念，因此位于 platform escape hatch 之后，key 为 `wechat.location`，导出为 `wechatGetCurrentLocation`。当前只实现 `getLocation`；`chooseLocation` 与 `openLocation` 未实现。宿主还会返回海拔、垂直与水平精度以及速度，这些有意未建模：Android 在无法取得垂直精度时返回 `0`，与真实的零无法区分，且 SDK 中没有任何部分需要它们。字段缺失、非数字、非有限值或超出合法坐标范围的答案映射为 `InvalidResponse`，而不是被填成零——零是一个真实但错误的位置。定位 API 可用性、`scope.userLocation` 权限与宿主隐私协议是三个不同问题：gate 只回答第一个，权限生命周期回答第二个；adapter 在调用宿主之前查询并强制后两项，但从不自行触发权限或隐私弹窗。Kotlin/JS、fake-host、CommonJS 与 TypeScript 自动检查均已通过。2026-09-15 的微信开发者工具与 Android 真机验收覆盖 `Supported`、`NotRequested`、显式授权后的 `Granted`、`Denied` 阻断，以及恢复权限后的成功定位；日志只报告坐标和精度是否有效，不显示实际坐标。

微信扫码 capability 适配 typed `wx.scanCode`，把一次用户手势变成一次微信自身界面的扫码。它属于微信专属能力，因此位于 platform escape hatch 之后，key 为 `wechat.scan-code`，导出为 `wechatScanCode`。请求只建模两个确有必要的选项：是否仅从相机扫码，以及允许的类别集合；类别使用请求词汇 `barCode`、`qrCode`、`datamatrix`、`pdf417`，而结果中的 `scanType` 使用宿主自己的格式词汇（例如 `QR_CODE`、`EAN_13`、`WX_CODE`），两者是不同的枚举。结果模型保留解码内容、宿主报告的格式名、字符集、原始数据与图片路径；无效字段映射为 `InvalidResponse`，未知格式保留宿主原名。Android 真机表明主动关闭界面和系统相机权限阻止界面启动都落入相同的 cancel 信号，因此这两种精确消息映射为 `HostInteractionInterrupted`，而非 `UserCancelled` 或 `PermissionDenied`；其他消息仍为 `HostFailure`。扫码不查询也不请求任何 SDK 权限，因为没有可靠契约把 `scope.camera` 与 `wx.scanCode` 绑定。自动化验证通过；真实宿主已覆盖 Supported、成功与不可归因的中断，该能力在矩阵中记为 `Stable`，因为矩阵要求实现、自动化测试与规定的宿主验证证据三者齐备，而它三者都有；宿主的不可区分性在错误模型中被显式保留，而不是被描述成宿主并不做出的区分。

微信媒体选择 capability 适配 typed `wx.chooseMedia`，把一次用户手势变成一次微信自身界面的选择。它属于微信专属能力，因此位于 platform escape hatch 之后，key 为 `wechat.choose-media`，导出为 `wechatChooseMedia`。请求只建模确有必要的选项——媒体类型、数量、来源、最长拍摄时长、压缩与摄像头——并只校验能够精确陈述的部分：微信自己的 schema 把 `mediaType` 标为 required，并把 `maxDurationSeconds` 文档化为 3 至 60 秒，因此空媒体类型列表与越界时长都不会被发送；宿主的数量上限则交给宿主，因为它取决于宿主的基础库。结果报告每个文件的临时路径、字节数、宿主必填类别，以及宿主报告的视频元数据；本 SDK 不认识的类别会保留宿主原名而不是让选择失败，宿主未报告的描述性字段保持缺失而不是变成 0，而空成功结果、缺少必填字段或字节数不是非负 JavaScript 安全整数都会映射为 `InvalidResponse`。开发者工具模拟关闭报告 `chooseMedia:cancel`，Android 真机主动关闭报告 `chooseMedia:fail cancel`；这两条精确信号都映射为 `HostInteractionInterrupted`，但不推断用户意图，其他失败保持为 `HostFailure`。媒体选择不查询也不请求 SDK 权限。选择返回的路径是宿主临时资源：SDK 不保留副本、不声称其生命周期，也不代替调用方把媒体复制到任何地方。自动化检查通过，真实宿主已覆盖能力支持、图片、视频、混合、拍摄、主动关闭与媒体访问受限路径。在已验证的 Android 宿主上，主动关闭与受限访问产生相同的不可归因中断，因此 SDK 显式保留这项宿主歧义。该能力为 `Stable`。

微信订阅消息 capability 适配 typed `wx.requestSubscribeMessage`，把一次用户手势变成一次针对模板列表的请求。它属于微信专属能力，因此位于 platform escape hatch 之后，key 为 `wechat.request-subscribe-message`，导出为 `wechatRequestSubscribeMessage`。**它是本项目证据最薄的一处。** 已安装开发者工具所带的基础库在其元数据表中声明了该 API——选项 `tmplIds`，以及一个以模板 ID 为键、旁边带 `errMsg` 的 success 结果——但其中没有该 API 的任何实现，也没有文档 schema，因此可离线来源没有给出该 API 的状态词汇，也没有给出它的关闭消息。整个 bundle 中唯一观察到的状态字符串是 `accept`，来自模拟器订阅弹窗预览载荷，那是弹窗渲染数据而不是 API 结果。因此 SDK 只识别这一个状态，并原样保留其他非空白字符串。在该 API 自身产出可观察证据之前，不分类任何关闭信号；惯例性的 cancel 形式仍为 `HostFailure`。合法结果必须精确包含所请求的模板键，且每项都是非空白文本状态，否则为 `InvalidResponse`。「同意」是订阅状态，永远不是送达，端到端送达属于 `BackendRequired`。不映射任何权限，也不添加任何 `app.json` 声明，因为没有可引用来源把某个 scope 或隐私条件与该 API 绑定——这是关于证据的结论，不等于认定其不存在。自动化 Kotlin/JS、fake-host、CommonJS 与 TypeScript 检查均已通过。2026-09-16 的 Android 真机运行（OnePlus PLQ110、Android 36、微信 8.0.76、运行时基础库 3.17.2）验证了 `wechat.request-subscribe-message=Supported` 与 `NOT CONFIGURED templateCount=0`；此路径有意不调用宿主，因此不会出现订阅同意弹窗。弹窗同意、拒绝、关闭、多模板关联与消息送达仍未验证，并受阻于为同一 AppID 配置的有效订阅模板，因此该能力仍为 `Partial`。

微信网络扩展适配五个宿主 API。其中成为公共 capability 的是网络状态，因为它回答的是一个普适问题，且模型里没有任何宿主专属部分；两个传输能力保留为微信专属，因为它们的请求与结果建立在宿主文件路径之上，而本 SDK 刻意没有可移植的文件引用——HTTP exchange 中可移植的那一半已经属于 transport capability。契约读取自已安装开发者工具所带的基础库：`getNetworkType` 枚举 `wifi`、`2g`、`3g`、`4g`、`5g`、`unknown` 与 `none`；变更事件枚举同一集合但不含 `5g`；`uploadFile` 接收 `url`、`filePath`、`name`、`header`、`formData` 与 `timeout`，并返回 `data` 与 `statusCode`；`downloadFile` 接收 `url`、`header`、`timeout` 与 `filePath`，并返回 `tempFilePath`、`filePath` 与 `statusCode`；两个 task 都带 `abort`、`onProgressUpdate` 与 `offProgressUpdate`（`RequestTask` 没有）；同一基础库的错误映射产出 `<api>:fail timeout`。它们都没有标注引入基础库版本，因此各自依靠 `wx.canIUse`。四个能力按 API 逐项门控，监听能力要求两半都在，以保证每次注册都能配到一次移除。自动化 Kotlin/JS、fake-host、CommonJS 与 TypeScript 检查均已通过。开发者工具与真机验收尚未完成：真实的传输需要位于 request domain 列表中的受控 HTTPS 服务（属 `BackendRequired`），而真实的网络切换需要一台能够真正切换连接的真机，任何模拟器都无法替代。WebSocket 未实现，在能力矩阵中保持 `Planned`。

运行时能力检测已于 2026-09-15 完成开发者工具与真机验收。开发者工具在基础库 3.17.2 下报告 `baseLibrary=3.17.2, platform=devtools, runtime-detection=Supported, storage=Supported, ungated=Unsupported`；Android 真机（OnePlus PLQ110、Android 36、微信 8.0.76）报告同样的状态而 `platform=android`。开发者工具当前可选的最低调试基础库为 2.21.4，无法在该环境中构造低于 2.20.1 的宿主，因此 `VersionDependent` 没有真实宿主截图；该边界由 `HostVersion` 单元测试、Fake Host 契约检查、边界测试与已执行的变异探针覆盖。3.17.2 是当前主要兼容验证版本，不是已验证的最低支持版本。权限生命周期已于 2026-09-15 完成麦克风与位置权限的开发者工具和 Android 真机验收；位置运行补充覆盖了 `NotRequested`、`Granted` 与 `Denied`。

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
