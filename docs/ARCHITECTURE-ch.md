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

### 设备能力

设备能力通过 platform escape hatch 到达用户，而不是通过公共 capability——除非其语义确实跨宿主共享。剪贴板与两种震动时长遵循该规则：`WechatClipboard` 与 `WechatHaptics` 位于 `host/wechat` 下，能力目录表以命名空间化的 key 逐项门控，因为宿主可能只提供其中一个剪贴板方向或一种震动时长。

宿主对象不等于 `wx`：`wx.canIUse` 回答的是 `wx` API 与部分组件对象，而不是 SDK 触及的每个对象。因此目录项可以是自带 presence 探测，由 gate 在版本与 schema 检查之外执行。文件管理器是第一个这类情况，这也是为什么只有当方法本身存在时才报告支持，而不是仅凭其 manager 存在。

文件系统以同一方式到达用户：`WechatFileSystem` 位于 `host/wechat` 下，仅操作小程序沙箱内的 UTF-8 文本，并按操作逐项门控。它不代替通用文件 API，也不添加微信未提供的目录、stream、descriptor 或遍历操作。

请求模型本身属于宿主专属的 capability，即使其概念很宽泛，也保留在逃生口之后。定位是典型例子：读取位置每个宿主都能做，但微信要求调用方在 `wgs84` 与 `gcj02` 之间选择，而该选择是任何通用 contract 都不应承载的本地测绘概念。因此整个 capability 位于 `host/wechat` 下，未来的 WebView 宿主会实现自己的版本，而不是声称满足微信的。

Capability 还可能有 SDK 能够强制、而不只是记录的前置条件。定位 adapter 会先查询宿主隐私协议与位置权限：任一条件未满足都在调用 `getLocation` 前失败。两项检查都只查询而不展示界面；隐私接受和权限请求仍必须由消费者通过明确的用户手势分别触发，因此位置读取永远不会成为隐式弹窗。

宿主自带的界面也可能就是这项能力本身。扫描属于这种情况：`WechatScanCode` 位于 `host/wechat` 下，只拥有调用方描述的请求与宿主给出的答案，既不实现界面，也不建模相机，更不解析它拿到的东西。当宿主只通过失败回调表达用户决定、又不提供任何结构化字段时，SDK 只在唯一一处依据宿主文本分类，且只做精确匹配：识别不出的一律保持为失败，因为把真实故障报告成用户的选择，会让调用方停止重试一个从未打开过界面的调用。同样的判断也适用于能力对结果的表达方式：请求用的类别词汇与宿主在结果中报出的格式词汇是两套不同的取值，SDK 不把它们混为一谈。

媒体选择是第二个同类情形，并带来它自己的边界：宿主返回的是临时资源。SDK 只报告拿到的路径、不保留副本，因此既不声称它们的存活时间，也不代替调用方复制任何东西；之后是否需要保存这份媒体由调用方自己决定。请求模型遵循与其他能力相同的规则——微信能精确陈述的部分（例如必填的媒体类型与有文档依据的时长范围）由 SDK 校验，而取决于当前宿主的部分（例如它最多接受多少个文件）留给宿主。

订阅消息请求带来的是另一种边界：宿主的词汇本身无法确立。SDK 只命名自己能够举证的取值，其余一律原样保留，而不是映射到它并未观察到的状态——这与扫码格式和媒体类别采用的是同一种形状，区别只在于这里能命名的内容少得多。这是「SDK 不编码无法引用的事实」这条规则的直接后果：无法证实的词汇表会产生一个很窄的已识别集合与一个很宽的保留集合，而绝不会产生猜测。

能力的要求也可能落在调用方而不是宿主身上。订阅消息请求只有在用户手势之后才有效，而 SDK 无法制造手势，因此它从不自行触发请求，也不会在没有手势的情况下重试一次拒绝；该要求写在能力的文档里，并通过把触发完全留给调用方来落实。

能力的**概念**可以是公共的，而它的**词汇**不是。网络状态就是这种情况：每个宿主都能回答「是否在线、通过什么链路」，因此问题与模型是公共的，但词是宿主的。所以答案同时携带两者——SDK 能识别的名字，以及紧挨着它的宿主原词——而 SDK 从未见过的链路类型会被如实报告，而不是被折进一个 `unknown` 成员里，那等于声称 SDK 知道得比实际更多。同样的拆分也适用于门控：宿主可能能回答该问题却不提供变更事件，因此查询与监听是两个独立的 key，而不是一个「有或没有」的能力。

事件形态的 capability 必须决定谁来拥有宿主注册。生命周期避开了这个问题，因为消费者转发的是它自己拥有的钩子。凡是由 SDK 自行注册 listener 的地方，本 SDK 让每个 collector 拥有一次注册，并在该 collector 结束时移除它：宿主按身份移除 listener，因此在多个 collector 之间共享一次注册就需要引用计数，而那里一旦出错，listener 会泄漏到小程序的整个生命周期。每个 collector 一次注册的代价是每个活跃 collector 一个 listener，换来的是每条终止路径上精确成对的清理。

请求与结果建立在宿主**文件**之上的 capability，即使其外围操作是可移植的，也保留在逃生口之后。上传与下载是 HTTP exchange，这一点公共 transport capability 已经覆盖；它们额外带来的是宿主自身文件系统中的路径，而本 SDK 刻意没有可移植的文件引用，因此把它们提升为公共能力就意味着凭空发明一个。它们的 task handle 按有用之处建模：消费者可以停止传输，取消等待也会停止它，因为一个不再等待某次传输的调用方并没有要求它继续运行。咨询性的进度报告为宿主的最后一个数值，永远不是估算值。

设备调用成功只表示宿主接受并执行了它，仅此而已：尤其是震动，只能由实际握持设备的人确认，因此 SDK 中没有任何部分把震动报告为「已被感知」。

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

`MiniAppException` 是平台无关的语义错误边界。Raw host failure 保留在 interop 和 Host adapter code 中；adapter 在映射为 SDK error 时保留有用的 scalar diagnostics。Raw JavaScript object 永不进入 common error model。只有宿主明确表达用户意图时才使用 `UserCancelled`；宿主结束交互却无法区分用户取消、权限限制或其他原因时使用 `HostInteractionInterrupted`。

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
