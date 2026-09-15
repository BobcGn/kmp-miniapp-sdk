# 项目事实

[English](PROJECT_FACTS-en.md)

本文件是 kmp-miniapp-sdk 当前状态的人类可读事实源。

如果本文件与可执行配置或源码冲突，以可执行配置和源码为准。

微信各能力的实现状态、宿主 API、权限前置条件和验证等级见 [微信能力矩阵](platforms/wechat/WECHAT_CAPABILITIES-ch.md)。矩阵中的 `Planned`、`Partial`、`Experimental`、`Unsupported` 与 `P3-Presentation` 均不得解释为已实现能力。

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
- Host 与 Capability 边界 contract 已实现
- 公共错误模型与 internal callback-to-coroutine primitive 已实现
- Storage capability 已实现并通过微信开发者工具验证
- 微信客户端 authentication bootstrap 已实现并通过微信开发者工具验证
- HTTP transport capability 已实现并通过微信开发者工具验证
- App 级 lifecycle capability 已实现，并通过微信开发者工具验证前台状态与页面 route
- 微信页面栈导航桥接已实现，并通过微信开发者工具验证
- 运行时能力检测与版本门控已实现，并通过微信开发者工具与真机验证
- 权限生命周期已实现，并通过微信开发者工具与真机验证

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

`export` 边界包含版本 facade、Promise-based Storage functions、微信专属 login bootstrap、Promise-based HTTP transport function、供消费者转发的 App 与 Page lifecycle 入口、微信导航函数、capability support 与 runtime inspection 查询，以及权限生命周期函数。Interop 边界包含 `login`、`showToast`、Storage、`request`、三个页面栈导航方法，以及带 presence guard 的 runtime inspection 成员的 internal 契约；production adapters 通过 `WechatHost` 调用 login、Storage、HTTP transport、导航、runtime inspection 与权限生命周期，`showToast` 仍仅为 interop contract。`runtime` 边界包含实现公共 lifecycle capability 的 `WechatAppLifecycle`、跟踪 runtime 报告为已显示页面的 `WechatPageLifecycle`，以及依据 runtime 报告判定支持状态的能力目录表与 gate。`adapter` 边界另外包含 `WechatPermissionScopes`，这是微信 scope 字符串唯一存在的地方。

## 9. 当前能力

- Kotlin Multiplatform 项目可完成配置和构建。
- `:sdk` JavaScript library target 可编译。
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
- `WechatPermissionScopes` 是微信 scope 字符串唯一存在的地方。当前映射的权限是麦克风；adapter 未映射的 key 会在任何宿主调用之前失败。
- 不做任何缓存：每次查询与每次设置页返回都询问宿主，因为用户随时可以在宿主自身的设置中改变权限。
- 拒绝是 `MiniAppException.PermissionDenied`，宿主无法作答是 `HostFailure`；只有报告授权被拒绝的失败消息才会成为拒绝。
- 同一权限的并发请求共享由服务拥有的单次宿主调用；已有决定的权限由宿主状态作答，不会再次弹窗。
- JavaScript / TypeScript facade 暴露 `permissionState`、`requestPermission` 与 `openPermissionSettings`，使用稳定的字符串状态 union，不依赖 `any`。
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
- 微信隐私授权（`getPrivacySetting`）及其约束的设备能力
- Request 或 response body 序列化、cookie 处理、redirect 策略、streaming、upload 或 download
- 服务端 code exchange、已认证用户/session 管理和 token refresh
- 公共通用 callback-to-coroutine API
- Compose integration、UI DSL、renderer 或 Virtual DOM
- npm publication
- Maven publication
- Gradle plugin
- 支付宝或 Telegram Host implementations

## 11. 已验证命令

验证日期：2026-09-15。

| 命令 | 结果 |
| --- | --- |
| `./gradlew projects` | VERIFIED |
| `./gradlew clean build` | VERIFIED |
| `./gradlew :sdk:jsNodeTest` | VERIFIED |
| `./gradlew :sdk:jsNodeProductionLibraryDistribution` | VERIFIED |
| `./gradlew buildMiniAppSdk` | VERIFIED — 重复执行可复用 configuration cache，且任务为 up to date |
| 在 `examples/wechat-miniprogram` 执行 `npm run smoke` | VERIFIED |
| 在 `examples/wechat-miniprogram` 执行 `npm run typecheck` | VERIFIED |
| 微信开发者工具加载、console、页面渲染、lifecycle 与导航 | VERIFIED — 用户确认前台状态、页面 route 与三种导航操作验收通过 |
| 微信真机调试（Android） | VERIFIED — OnePlus PLQ110 / Android 36 / 微信 8.0.76；Runtime Detection、Storage、Authentication Bootstrap、HTTP Request 与三项导航操作均 PASS，`platform=android` |
| 微信开发者工具中的 Permission | VERIFIED — 基础库 3.17.2；卡片显示 `Granted` 与 `Denied`，设置页返回的是宿主的决定 |
| 真机中的 Permission | VERIFIED — 完整走通 `Granted` → `Denied` → `DENIED` → `Granted`，拒绝后未出现第二次弹窗 |

早期微信开发者工具证据覆盖版本、Storage 与 authentication 检查；network 检查于 2026-09-14 单独完成验收。用户于 2026-09-15 确认 lifecycle 前台状态、页面 route，以及 `navigateTo`、`redirectTo`、`navigateBack` 的真实宿主验收通过。后台状态迁移不属于开发者工具模拟器可验证范围。权限生命周期定义了宿主无关的三态模型 —— `NotRequested`、`Granted`、`Denied` —— 由命名权限「为了什么」的 `PermissionKey` 标识，并通过微信 adapter 适配 `wx.getSetting`、`wx.authorize` 与 `wx.openSetting`。不做任何缓存；拒绝是 `MiniAppException.PermissionDenied` 而不是宿主失败；请求权限与打开设置绝不在缺少用户手势时执行。Kotlin/JS、fake-host、CommonJS 与 TypeScript 自动检查均已通过。微信开发者工具（基础库 3.17.2）与 Android 真机（OnePlus PLQ110、Android 36、微信 8.0.76）验证了：宿主能够报告 `Granted` 与 `Denied`；设置页返回的是宿主的决定而不是被假定为已授权；对已拒绝权限再次请求会报告 `DENIED` 且不出现第二次弹窗。`NotRequested` 无法在所用账号上产出，因为该账号已对所映射权限持有决定；该状态改由自动化测试覆盖。

运行时能力检测已于 2026-09-15 完成开发者工具与真机验收。开发者工具在基础库 3.17.2 下报告 `baseLibrary=3.17.2, platform=devtools, runtime-detection=Supported, storage=Supported, ungated=Unsupported`；Android 真机（OnePlus PLQ110、Android 36、微信 8.0.76）报告同样的状态而 `platform=android`。开发者工具当前可选的最低调试基础库为 2.21.4，无法在该环境中构造低于 2.20.1 的宿主，因此 `VersionDependent` 没有真实宿主截图；该边界由 `HostVersion` 单元测试、Fake Host 契约检查、边界测试与已执行的变异探针覆盖。3.17.2 是当前主要兼容验证版本，不是已验证的最低支持版本。权限生命周期已于 2026-09-15 完成开发者工具（基础库 3.17.2）与 Android 真机验收，覆盖 `Granted`、`Denied`、设置页返回以及拒绝后的再次请求。`NotRequested` 无法在所用账号上复现，改由自动化测试覆盖。当前只映射麦克风权限。

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
