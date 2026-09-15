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

Capability support 当前只有 `Supported` 和 `Unsupported` 两种状态。Version-dependent 和 permission-dependent 状态是明确的演进方向，但只会在具体 Capability 证明其所需数据与行为后引入。

### Runtime families

Mini App Host 可能属于不同 runtime family。微信一类 DSL runtime 与 WebView/web runtime 对 `window`、DOM、CommonJS 或 WXML 没有统一假设。CommonJS 是当前微信 distribution strategy，而不是所有未来 Host 的永久 ABI。

### 生命周期与导航

应用级生命周期是 capability。每个小程序宿主都能表达应用是否呈现在用户面前，因此 `MiniAppLifecycle` 暴露当前状态与状态变化流，由宿主 adapter 转发其 runtime 交给消费者的钩子。

Page 级生命周期与页面栈导航不是 capability。页面、页面 route 与页面栈属于 DSL 小程序 runtime，WebView 宿主一个都没有。因此 `WechatPageLifecycle` 与 `WechatNavigation` 位于 `host/wechat` 下，仅通过 platform escape hatch 可达。把它们描述为中性的 contract，等于把微信语义当成通用语义。

生命周期是转发而非拦截：微信只向消费者拥有的 `App(...)` 与 `Page(...)` 注册投递生命周期，因此 SDK 提供由消费者转发钩子的入口。

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
