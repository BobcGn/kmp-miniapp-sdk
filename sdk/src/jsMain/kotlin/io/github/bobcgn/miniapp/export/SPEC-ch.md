---
id: submodule-sdk-export
type: submodule-design
status: draft
title: export · JS/TS ABI 与观察会话
parent: module-sdk
depends-on: [submodule-sdk-common, submodule-sdk-wechat]
tags: [exports, typescript]
---

# JS/TS ABI 与观察会话

[English](SPEC-en.md)

## 职责与公开入口

`@JsExport MiniAppExports` 与同目录导出 DTO 是 JavaScript / TypeScript 消费表面，编译器生成声明；suspend 操作通过 Promise 暴露。它适配 Kotlin collection、enum 与对象到宿主可构造的形状，不承担宿主行为实现。

namespace 在 `host/wechat` 之外，但当前 facade 持有 `WechatHost`，不是公开支持任意宿主注入的中性实现。它调用公共契约与微信逃生口，不直接调用 `wx`。示例中的手写 CommonJS wrapper 只归一 namespace、参数与返回值形状。

## 为什么有单独的导出边界

调用方不能因为使用 JS 就被迫构造 Kotlin Map 或收集 Kotlin Flow；同样，也不应得到第二套微信行为实现。因此本层只把 Kotlin 行为换成宿主可使用的参数、Promise、DTO 与显式观察入口，真正的语义仍由原能力／adapter 提供。[MiniAppExports.kt](MiniAppExports.kt) 与会话类是实现依据，headers 形状的取舍已记录在 ADR 0005。

代价是 Kotlin 与 JS/TS 表面不逐签名相同，类型声明和归一 wrapper 必须与同一产物对应。namespace 独立不意味着 Host 已可任意替换；internal 入口的未确认动机归 [[submodule-sdk-wechat]]，不是本层额外制定的禁令。

## 会话与资源边界

JS 不能收集 Flow 或取消 Promise，因此 `NetworkStatusObservation`、`BleObservationSession` 通过显式 start/stop 持有 collector；关闭 BLE adapter 还终止 discovery/connection 观察。会话快照各保留最近 32 项，不是完整日志或全部扫描结果。

`JsNetworkTransfer` 提供 Promise result、abort 与最后进度；资源清理依赖被委托的 adapter，而不是另一套宿主实现。bounded 快照不使 adapter 的设备去重集合变成有界。

## 公共契约限制

宿主成功不变成身份、订单或送达事实；未知词保留宿主信息，日志不得打印凭据。TypeScript declaration、Kotlin 源码／KDoc 和 IDE 导航是不同链路，不承诺所有编辑器默认点击目标一致。

## 验证

`NetworkStatusObservationTest`、`BleObservationSessionTest` 经 `./gradlew :kmp-miniapp-sdk:jsNodeTest` 检查会话归属与停止；CommonJS smoke 与 strict TypeScript 在刷新示例产物后另行运行。入口与状态见 [[submodule-sdk-js-tests]]、[[module-examples]]，不以 Node 成功代替宿主验收。
