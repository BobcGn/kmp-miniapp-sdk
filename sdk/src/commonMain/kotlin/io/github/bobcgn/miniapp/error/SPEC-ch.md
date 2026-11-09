---
id: submodule-sdk-errors
type: submodule-design
status: draft
title: error · 语义错误边界
parent: submodule-sdk-common
tags: [errors]
---

# 语义错误边界

[English](SPEC-en.md)

## 职责与公共表面

`MiniAppException` 是宿主失败进入共享 Kotlin 的语义边界。它区分不可用能力、权限拒绝、隐私前置条件、超时、无效结果、普通宿主失败和 SDK 内部失败；错误不是一份后端业务结论。

`UserCancelled` 表示有证据的用户意图，`HostInteractionInterrupted` 表示宿主交互结束但原因不可归因。协程取消不等于上述任何宿主错误。HTTP 错误 status 是 response，不因 status 被包装为异常。

## 为什么不能直接返回宿主失败对象

调用方需要按可行动的语义处理失败，而不是依赖某个宿主对象形状或文本。拒绝、超时和未知中断不意味着同一种恢复动作；尤其把模糊 cancel 当作用户意图，会让调用方放弃重试实际未能启动的交互。

[MiniAppException.kt](MiniAppException.kt) 及 ADR 0004 是语义边界的依据。保留 scalar diagnostics 有助于诊断，代价是公共错误并不自动满足日志脱敏要求；「共享类型可携带」与「可以直接记录」不是同一许可。

## 数据与依赖边界

允许能力 key、字符串诊断、净化后的字符串 metadata 和 Kotlin cause；禁止 raw JavaScript 对象或宿主 result 直接跨越本层。`HostFailure.hostMessage` 等字段仍可能携带原始诊断字符串，不承诺所有字符串均已脱敏。消费者日志必须另行筛选，不能直接打印全部错误对象。

本层不解析微信错误词，也不提供重试、认证或订单策略。微信文本到错误的分类归 adapter。ADR 0009 的「原始消息不进入公共类型」与现有 scalar `hostMessage` 的口径差异仍未裁定；这项未决差异不表示字段已被移除或错误行为已改变。

## 验证

`MiniAppExceptionTest` 检查错误模型；平台的 `WechatErrorMapperTest` 检查精确分类与未知失败保留。执行 `./gradlew :kmp-miniapp-sdk:jsNodeTest`。fake-host 检查不能证明真机的取消消息或支付事实。
