---
id: submodule-sdk-async
type: submodule-design
status: draft
title: async · 单终态 callback 适配
parent: submodule-sdk-common
tags: [coroutines, cancellation]
---

# 单终态 callback 适配

[English](SPEC-en.md)

## 职责与内部入口

`awaitHostCallback` 将一次 callback 操作转为 cancellable suspension；`HostOperationAborter` 表示宿主真实提供的可选 abort hook。两者都是 internal，不是公共通用异步框架。

## 为什么抽取机制而不统一业务结果

宿主可能回调两次、在取消后才回答，或在注册返回前同步完成。若各 adapter 自行恢复 continuation，同一种资源错误会被复制到每项能力；[HostCallbackAdapter.kt](HostCallbackAdapter.kt) 因而共享终态机制，而把结果校验和错误含义留给 adapter。ADR 0004 记录该分工。

取消等待不总能取消宿主操作，因此 hook 是可选的，不能为 API 形状整齐而假造 abort。internal 可见性限制的是这项机制的公共承诺；它不引入通用 Promise/event 框架，也不保证任意多线程宿主的调度模型。

## 终态与取消契约

第一个 success/failure 才能结算 continuation，后续矛盾或迟到回调被忽略。取消后不再交付结果；若宿主提供 abort，最多调用一次，包括注册函数同步触发取消、尚未返回 aborter 的竞态。

没有 hook 时只停止等待，不声称底层操作已停止。注册过程抛出的异常在未终结时映射为 `InternalFailure`；本层不解释微信错误词，也不自行生成权限拒绝或用户取消。

## 边界与验证

只依赖公共错误模型与 coroutines，不依赖平台 interop、业务代码、Promise DTO 或 UI。新增宿主 adapter 复用单终态机制，而不是复制 continuation 状态机。

`HostCallbackAdapterTest` 覆盖重复完成、取消、注册异常、可选 abort 与同步取消路径。执行 `./gradlew :kmp-miniapp-sdk:jsNodeTest`；真实宿主是否可中止仍由具体 adapter 与宿主契约决定。
