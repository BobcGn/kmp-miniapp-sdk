# ADR 0004：错误与异步适配

[English](0004-error-and-async-adaptation-en.md)

- 状态：Accepted
- 日期：2026-09-14

## 背景

Callback-based Host API 会报告平台特定的 result objects、messages、codes 和 cancellation behavior。将这些值直接传给 shared consumer 会把 Host contract 泄漏到 `commonMain`。把 coroutine cancellation 直接当作 Host operation cancellation 也不正确，因为许多 Host API 不提供 abort mechanism。

Host callback 可能迟到或互相冲突。Adapter 必须能够处理 success 后 complete、fail 后 complete、coroutine cancellation 后 callback，以及缺陷 Host 多次调用 terminal callback 的情况。

## 决策

- `MiniAppException` 是公共语义错误边界。最小 variants 为 `UnsupportedCapability`、`PermissionDenied`、`UserCancelled`、`HostFailure`、`InvalidResponse` 和 `InternalFailure`。
- Raw host result objects 保留在 Host-specific interop 与 adapter layers。Host adapter 将强类型 scalar diagnostics 映射为 `HostFailure`，其中可保留 host、可选 code、message、可选 cause 和经过净化的字符串 metadata。
- `awaitHostCallback` 是 internal callback-to-coroutine primitive。它接收已经映射为 `MiniAppException` 的失败，并且只允许一个 terminal result。
- 取消 coroutine 一定会阻止其继续消费后续 callback，但不代表底层 Host operation 已经停止。
- 如果 Host operation 提供真实 abort hook，取消时最多调用一次。若没有 hook，Host operation 可以继续执行，最终 callback 会被忽略。
- Callback 注册期间抛出的 exception 映射为 `InternalFailure`。
- 在真实 Host integration 提出需求前，不提前泛化 Promise 或 event adapter。

## 后果

- Common consumer 接收稳定 SDK 语义，而不是 raw JavaScript values。
- Host-specific error classification 仍由各 Host adapter 负责。
- 对可 abort 和不可 abort API，取消语义均保持真实。
- Capability adapter 可以复用该 primitive，但本决策不实现 Storage、Auth、Network 或其他 Capability。
