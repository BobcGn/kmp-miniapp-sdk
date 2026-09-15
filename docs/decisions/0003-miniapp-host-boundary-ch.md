# ADR 0003：小程序宿主边界

[English](0003-miniapp-host-boundary-en.md)

- 状态：Accepted
- 日期：2026-09-14

## 背景

微信小程序是 SDK 的第一个真实宿主，但微信不是 SDK core。未来宿主可能提供不同能力，也可能属于不同 runtime family。微信和支付宝接近 DSL mini-program runtime，而 Telegram Mini Apps 一类宿主使用 WebView/web runtime。因此，Host abstraction 不能假定 `window` 一定存在、DOM 一定不存在、CommonJS 一定存在或 WXML 一定存在。

如果强行把每个宿主 API 放进同一个公共 surface，会形成 lowest-common-denominator API。通过不断增长的平台枚举和中央 `when` 选择行为，也会导致每增加宿主都必须修改既有 core code。

## 决策

- Host 是由 `MiniAppHost` 表达的平台适配边界，不是平台枚举值。
- 微信是 Host Adapter #1，其实现代码保留在 `jsMain/.../host/wechat`。
- Common layer 定义宿主无关 contract，且永不依赖具体 Host。
- `CapabilityKey` 标识 SDK 语义能力，`CapabilitySupport` 首期只表达 `Supported` 或 `Unsupported`。
- 只有语义真正一致时才建立公共 Capability abstraction。平台专属 API 通过强类型 `HostPlatformApi` escape hatch 保持可访问。
- 新 Host 通过新增 implementation 扩展。核心架构不使用中央 `Platform` enum 和 `when` branches 分发。
- CommonJS 是当前微信 distribution strategy，不是所有未来 runtime family 共用的永久 ABI。
- 本决策不引入 Host registry、DI framework、新 Gradle module 或推测性的支付宝/Telegram 实现。

未来可在具体用例证明所需数据后，为 capability support 增加版本或权限要求状态；本轮不提前加入。

## 后果

- Shared code 可以判断语义能力是否可用，同时不导入微信声明。
- Consumer 可以访问平台专属能力，而无需伪装其可跨平台。
- 未来 Host 可以使用适合自身 runtime family 的 distribution format。
- Capability interfaces、具体 Host implementations 和更丰富的 availability states 仍属于未来工作，必须由真实用例驱动。
