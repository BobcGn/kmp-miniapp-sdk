# ADR 0005：HTTP 传输能力

[English](0005-http-transport-capability-en.md)

- 状态：Accepted
- 日期：2026-09-14

## 背景

SDK 需要一个最小的平台无关 HTTP 传输，以及对应的微信 adapter。这带来了 ADR 0003 与 ADR 0004 未涵盖的几个边界问题：

- HTTP 传输是真正共享的 capability，还是微信专属的 escape hatch？
- HTTP 错误 status 应当表示为异常还是 response？
- 宿主超时是独立的 SDK 失败，还是 host failure 的一种？
- Headers 是键值数据，但 Kotlin/JS collection 无法跨越 `@JsExport` 边界，因为 JavaScript 调用方无法构造它。
- `wx.request` 是本 SDK 中第一个返回真实 abort handle 的宿主 API。

ADR 0003 确立了「只有真正共享的语义才成为公共 capability」，ADR 0004 确立了错误与取消模型，但没有实现任何 capability。

## 决策

- HTTP 传输是公共 capability `MiniAppHttpTransport`，由 `CapabilityKey("network")` 标识。任何能够执行 HTTP exchange 的宿主都能满足其语义，因此它不是微信专属 escape hatch。
- 该 contract 只承载文本：输入为 URL、method、headers、已编码 body 和可选 timeout；输出为 status code、headers 与解码后的 body。Payload serialization、cookie、redirect 策略、streaming、upload 和 download 均被排除。
- 已完成的 exchange 对包括 `4xx` 与 `5xx` 在内的所有 HTTP status 都算 response。HTTP status 描述的是这次交换，而不是 SDK 能否执行它，因此不抛出异常。只有传输层失败才成为 `MiniAppException`。
- 新增与 `HostFailure` 相互独立的 `MiniAppException.Timeout`，因为超时意味着宿主停止等待，而不是该操作本身存在缺陷。这是对 ADR 0004 中 variant 列表的扩展，而不是对该决策的取代。
- `MiniAppHttpResponse.headers` 只承载单一字符串值。宿主以其他形状上报的 header，例如微信为 `Set-Cookie` 上报的数组，不做有损字符串化，而是不予表示。
- Adapter 请求原始文本响应，并将非文本 body 报告为 `InvalidResponse`。字符串 contract 不得依赖宿主的 JSON 自动解析。
- `wx.request` 会返回 abort handle，因此微信 adapter 向 `awaitHostCallback` 提供了第一个真实 abort hook。取消协程最多中止一次宿主 exchange，微信为已中止 exchange 投递的任何 failure callback 都会被丢弃。这是对 ADR 0004 取消规则的实现，而非修改。
- Export facade 无法跨 `@JsExport` 承载 Kotlin map。因此 headers 以 name 与 value 交替的扁平字符串序列跨越该边界，并由手工维护的 CommonJS wrapper 还原为对象形状。Kotlin API 仍保留 `Map<String, String>`。
- 不引入 Ktor、类 Retrofit API、JSON 序列化框架、缓存或重试框架。

## 后果

- Shared code 可以在不依赖微信声明的前提下执行 HTTP exchange。
- Consumer 可以区分 HTTP 错误 response 与传输失败，也可以区分超时与一般 host failure，而无需自行解析宿主消息。
- 宿主如何表示一次 exchange 由其 adapter 决定，而不是由公共 contract 决定。
- JavaScript 边界需要一个明确的键值数据表示规则，后续导出 map 的 capability 必须遵循。
- JavaScript wrapper 中会包含少量 header 转换。它仅做表示，宿主访问与错误映射仍属于 adapter。
- 宿主以非字符串形状上报的 header 值，在出现具体用例并确定所需表示之前，对 consumer 不可用。
