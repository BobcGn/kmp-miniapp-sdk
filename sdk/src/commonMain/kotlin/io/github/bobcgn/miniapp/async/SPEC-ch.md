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

## 终态与取消契约

第一个 success/failure 才能结算 continuation，后续矛盾或迟到回调被忽略。取消后不再交付结果；若宿主提供 abort，最多调用一次，包括注册函数同步触发取消、尚未返回 aborter 的竞态。

没有 hook 时只停止等待，不声称底层操作已停止。注册过程抛出的异常在未终结时映射为 `InternalFailure`；本层不解释微信错误词，也不自行生成权限拒绝或用户取消。

## 边界与验证

只依赖公共错误模型与 coroutines，不依赖平台 interop、业务代码、Promise DTO 或 UI。新增宿主 adapter 复用单终态机制，而不是复制 continuation 状态机。

`HostCallbackAdapterTest` 覆盖重复完成、取消、注册异常、可选 abort 与同步取消路径。执行 `./gradlew :kmp-miniapp-sdk:jsNodeTest`；真实宿主是否可中止仍由具体 adapter 与宿主契约决定。
