# 架构

[English](ARCHITECTURE-en.md)

本文档记录稳定的架构边界，不表示其中描述的所有 integration 都已经实现。当前能力见 [PROJECT_FACTS-ch.md](PROJECT_FACTS-ch.md)。

## 1. 系统边界

```text
commonMain
    ↓
jsMain/export facade
    ↓
Kotlin/JS artifact
    ↓
极薄的 CommonJS export normalization
    ↓
TypeScript / JavaScript
    ↓
WeChat Mini Program runtime
```

## 2. 依赖方向

平台无关的 interfaces、models、errors 和 shared logic 属于 `commonMain`。平台实现属于 `jsMain`，并依赖平台无关 contracts。

```text
jsMain platform implementation
            ↓ depends on
commonMain interfaces and models
```

`commonMain` 不得依赖微信实现细节。微信特定代码不得向上污染 shared layer。

## 3. 宿主边界

微信小程序是 host runtime。SDK 不负责：

- WXML 或 WXSS rendering
- Page rendering
- Component trees
- Application UI

SDK 的范围仅包括 shared logic、runtime integration 和 typed platform bridge。

`MiniAppHost` 是 platform adapter boundary。Shared code 使用 `CapabilityKey` 标识语义能力并读取当前 `CapabilitySupport`，不通过平台枚举选择 Host。新增 Host 应增加 implementation，而不是在中央 `when` statement 中增加 branches。

具体公共 Capability 通过小型 capability-provider facet 暴露。首个实例是 `StorageCapabilityProvider`：它让 `WechatHost` 提供 `MiniAppStorage`，同时不增加通用 service locator，也不绕过 Host boundary。

微信客户端登录不会被刻意包装为公共 Auth capability。`WechatHost.platform` 通过 `WechatPlatformApi` 暴露微信专属 authentication adapter，在保留 `wx.login` 语义的同时，避免创造误导性的跨 Host `login(): String` contract。

微信是 Host Adapter #1，其平台特定代码隔离在 `jsMain/.../host/wechat`。当前不存在支付宝或 Telegram implementation。

### Capability 与平台逃生口

只有不同 Host 间语义真正一致的内容才应成为公共 Capability。SDK 不隐藏底层平台，也不把无关 API 强行塞入 lowest-common-denominator abstraction。`MiniAppHost.platform` 提供强类型 `HostPlatformApi` escape hatch；具体平台 API 只在出现真实 consumer 时引入。

Capability support 有四种状态：`Supported`、`Unsupported`、`VersionDependent` 和 `PermissionDependent`。后两者描述的是宿主需要具备什么才能提供该能力，二者都表示当前不可用。宿主依据自身 runtime 作答，而不是依据清单，因此同一份 SDK 构建在两个宿主上可以给出不同答案。版本与平台读取推迟到各自首次需要时执行并分别缓存，因为导出 facade 在模块仍处于加载阶段时就构造了宿主；`wx.canIUse` 则保持实时查询。

版本比较属于平台无关逻辑，以 `HostVersion` 落在 `commonMain`。哪个基础库提供某项能力属于宿主数据，因此位于微信目录表；目录项可以不设最低版本，转而依靠 `wx.canIUse`——它回答的是当前真正运行的宿主，而不是从表格抄来的数字。微信 runtime 关于自身的报告（含基础库版本）属于宿主专属信息，仅通过逃生口可达。

`requireSupported` 会把除 `Supported` 外的任何状态转换为 `MiniAppException.UnsupportedCapability`，并且是产出该异常的唯一路径。完全无法探测的宿主按失败关闭处理：SDK 无法确认的能力一律报告为 `Unsupported`，而不是假定可用。

### Runtime families

Mini App Host 可能属于不同 runtime family。微信一类 DSL runtime 与 WebView/web runtime 对 `window`、DOM、CommonJS 或 WXML 没有统一假设。CommonJS 是当前微信 distribution strategy，而不是所有未来 Host 的永久 ABI。

### 生命周期与导航

应用级生命周期是 capability。每个小程序宿主都能表达应用是否呈现在用户面前，因此 `MiniAppLifecycle` 暴露当前状态与状态变化流，由宿主 adapter 转发其 runtime 交给消费者的钩子。

Page 级生命周期与页面栈导航不是 capability。页面、页面 route 与页面栈属于 DSL 小程序 runtime，WebView 宿主一个都没有。因此 `WechatPageLifecycle` 与 `WechatNavigation` 位于 `host/wechat` 下，仅通过 platform escape hatch 可达。把它们描述为中性的 contract，等于把微信语义当成通用语义。

生命周期是转发而非拦截：微信只向消费者拥有的 `App(...)` 与 `Page(...)` 注册投递生命周期，因此 SDK 提供由消费者转发钩子的入口。

### 权限

权限之所以是 capability，是因为每个依据用户决定来把关设备访问的宿主都会给出同样的三种答案。`MiniAppPermissions` 针对宿主无关的 `PermissionKey` 报告 `NotRequested`、`Granted` 或 `Denied`，且从不保留答案：用户随时可以在宿主自身的设置里改变权限，被记住的状态会在无人察觉时过期。

拒绝是 `MiniAppException.PermissionDenied`，宿主无法作答是 `HostFailure`。对消费者而言这是两条不同的指令——前者是请用户前往设置页，后者是可以稍后重试。请求权限与打开设置都需要用户手势，因此 SDK 绝不自行弹窗；设置页返回后报告的是宿主此后给出的状态，而不是假定为已授权。

`PermissionKey` 命名的是权限「为了什么」。宿主用于它的 `scope.*` 字符串只存在于微信 adapter 的映射边界内，adapter 未映射的 key 会在任何宿主调用之前失败，而不是被转发出去。

`PermissionState` 与 `CapabilitySupport.PermissionDependent` 回答的是不同问题：前者是权限自身的状态，后者表示某个 capability 正被权限阻塞。权限 capability 自身由其宿主 API 判定为 `Supported`，永远不会依赖权限。

权限不是隐私。微信的隐私授权是独立生命周期、独立 API、独立 capability 与独立状态；二者共享模型会互相误报。

### 隐私

隐私之所以是 capability，是因为每个代替用户收集个人数据的宿主都有某种同意条件，而微信把它明确表述出来。`MiniAppPrivacy` 报告宿主当前对其自身隐私协议的要求、请求宿主取得用户同意，并暴露受门控 capability 所调用的前置条件判定点。它与 `MiniAppPermissions` 不共享任何状态：宿主对二者的弹窗、存储与清除都是分开的。

三者分开建模，因为宿主也是分开报告的：可查询的要求、一次尝试的结果与 SDK 错误。`NOT_REQUIRED` 是关于宿主的陈述，不是用户已同意的证明，因为微信在小程序未声明任何收集类型时也会返回它。

拒绝是结果而不是错误；拒绝与关闭弹窗会进入宿主的失败回调，但只有可识别的拒绝消息才映射为 `Refused`，其他未知失败保守地保留为 `HostFailure`。SDK 不凭空造出宿主没有的拒绝/取消区分。隐私前置条件失败是 `MiniAppException.PrivacyAuthorizationRequired`，绝不是 `PermissionDenied`；没有隐私 API 的宿主报告 `UnsupportedCapability`。

前置条件判定点只查询宿主，从不展示任何界面：只有消费者能弹窗，且必须在用户手势中。

## 4. JS Interop 边界

`external`、`dynamic` 和 `js()` 等 JavaScript-specific constructs 只能存在于 `jsMain`。原始 platform contracts 限制在 `jsMain/.../host/wechat/interop`。

Business 和 shared layers 不得直接操作 `dynamic` values。将其转换为类型化 Kotlin values 是 adapter boundary 的职责。

微信 interop 层只描述全局 `wx` object、option bags、result shapes 和 callbacks。Plain-object factories 的存在仅因为 external interfaces 没有 Kotlin constructors；它们不调用宿主 API，也不包含错误映射、capability policy 或业务逻辑。

`host/wechat` 是 host boundary 下的平台特定 namespace。该 namespace 为未来宿主保留空间，但当前不引入其他宿主实现或新的 Gradle modules。

## 5. 异步边界

异步平台 API 的架构目标是：

```text
wx callback
    ↓
interop
    ↓
adapter
    ↓
coroutine-friendly Kotlin API
```

`MiniAppException` 是平台无关的语义错误边界。Raw host failure 保留在 interop 和 Host adapter code 中；adapter 在映射为 SDK error 时保留有用的 scalar diagnostics。Raw JavaScript object 永不进入 common error model。

Internal `awaitHostCallback` primitive 已实现 callback-to-coroutine mechanics，但没有实现任何 Capability。它只接收一个 success 或 failure terminal result，并忽略所有后续 callback，包括 cancellation 后到达的 callback。

Coroutine cancellation 与 Host cancellation 是不同概念：

- 没有 abort hook 时，cancellation 只停止消费结果，Host operation 可以继续执行。
- 存在真实 abort hook 时，cancellation 最多调用该 hook 一次。

Promise 与 event adaptation 仍属于由真实用例驱动的未来工作，但有一类事件形态的 capability 已经出现用例：应用生命周期状态。`MiniAppLifecycle` 发布当前状态与后续变化的 `Flow`，这也是 SDK 引入的唯一流抽象。

首个具体 Capability 使用以下路径：

```text
MiniAppStorage
    ↓ 由其提供
WechatHost
    ↓
WechatStorage adapter
    ↓
awaitHostCallback + 微信错误映射
    ↓
typed storage interop
    ↓
wx.getStorage / wx.setStorage / wx.removeStorage
```

公共 contract 有意只存储字符串。Object serialization、secure storage、cloud storage 和 Host-specific options 均不属于该 Capability。

微信专属 authentication bootstrap 使用独立链路：

```text
WeChatLoginResult
    ↑ 返回自
WechatAuth adapter
    ↓
awaitHostCallback + 微信错误映射
    ↓
typed login interop
    ↓
wx.login
```

微信还提供 `wx.checkSession`，用于报告微信自身发放的客户端登录态是否仍在微信定义的时效内。它被建模为微信专属结果，而不是通用认证 capability，因为没有任何其他宿主被证明共享这些语义，而通用的 `isAuthenticated()` 会承诺该 API 无法给出的保证。它只是查询：失效的答案不会获取 code、不交换 session、不刷新 token，是否再次走既有登录流程由消费者自行决定。

会话检查有效不等于身份。它不表示用户已认证、不表示消费者后端 session 有效、不表示 `session_key` 仍被消费者后端接受，也不表示 access token 有效。只有经由消费者后端的 login code 路径才能建立可信身份，与本 capability 出现之前完全一致。

登录 code 是短期客户端凭证，不是可信用户身份、SDK session 或 access token。向微信交换 code 并建立可信应用 session 属于消费者后端职责。SDK 不记录、不持久化、不交换也不刷新该 code。

首个向宿主发送数据的公共 Capability 使用上述共享路径：

```text
MiniAppHttpTransport
    ↓ 由其提供
WechatHost
    ↓
WechatNetwork adapter
    ↓
awaitHostCallback + 微信错误映射
    ↓
typed request interop
    ↓
wx.request
```

已完成的 exchange 对包括 `4xx` 与 `5xx` 在内的所有 status 都算 response；HTTP status 描述的是这次交换，而不是 SDK 能否执行它。只有传输层失败才成为异常：宿主超时映射为 `MiniAppException.Timeout`，其余传输失败映射为 `MiniAppException.HostFailure`。`wx.request` 会返回 abort handle，因此这是第一个在协程取消时同时中止宿主操作的 Capability，而不只是停止消费其 callback。

该 contract 只承载文本。因此 adapter 请求原始文本响应，并将非文本 body 报告为 `MiniAppException.InvalidResponse`，而不是让宿主返回 contract 无法表示的已解析对象。Response headers 以单一字符串值传递；宿主以其他形状上报的 header 不会被表示。Payload serialization、cookie、redirect、streaming 与 retry 策略仍不在该 Capability 范围内。

## 6. 公共边界

项目区分两套 API surface：

- Kotlin public API：供 Kotlin 代码使用、符合 Kotlin 习惯的 interfaces、models 和 behavior。
- JavaScript / TypeScript export API：根据 JavaScript / TypeScript 易用性和互操作限制设计的 consumer-facing boundary。

Export surface 可以适配 Kotlin API，但不得成为业务实现层。

宿主无关的 export facade 位于 `jsMain/.../export`，可以调用 `commonMain` 中的共享 Kotlin API，但不得包含 `wx` 调用或平台能力实现。当编译器生成的 CommonJS namespace 不适合作为稳定的 consumer ABI 时，可以使用极薄的 JavaScript wrapper，仅做 module 和 export shape normalization；wrapper 不得包含业务逻辑、宿主错误映射或平台行为。

Kotlin collection 无法跨越 `@JsExport` 边界，因为 JavaScript 调用方无法构造 Kotlin map。需要键值数据的 export 因此改用可原生表示的形状承载，并由 wrapper 还原为对象形状；Kotlin API 仍保留符合习惯的 collection 类型。HTTP transport export 对 headers 采用该规则，将其表示为 name 与 value 交替的扁平字符串序列。

## 7. 非目标

- 重新实现 Kuikly
- 构建 Compose Mini Program renderer
- 替代 WXML
- 构建 Virtual DOM
- 完整镜像 `wx` API
- 通过中央平台枚举选择 Host
- 用误导性的公共抽象隐藏平台专属 API
