---
id: submodule-sdk-privacy
type: submodule-design
status: draft
title: privacy · 隐私要求与授权结果
parent: submodule-sdk-capabilities
tags: [privacy]
---

# 隐私要求与授权结果

[English](SPEC-en.md)

## 职责与公共入口

`MiniAppPrivacy` 提供 status、requestAuthorization 和只查询的 requireSatisfied，key 为 `privacy`。`PrivacyStatus` 描述宿主当前要求与协议名，`PrivacyAuthorizationOutcome` 描述一次请求的 Authorized/Refused；要求、结果与错误是独立概念。

## 为什么不是权限的另一个状态

[MiniAppPrivacy.kt](MiniAppPrivacy.kt) 与 ADR 0009 的依据是宿主将权限和隐私分别提示、保存、清除；共享一个模型会报告错误的下一步动作。要求是宿主现在要什么，outcome 是一次交互的回答，错误则是无法满足前置条件或无法作答，三者不能互相代替。

SDK 无法从 NOT_REQUIRED 证明同意，因此只提供查询与显式请求，不替调用方证明合规。这个较窄的保证避免把账号配置或宿主回答变成客户端拥有的法律／业务事实。

## 语义与交互边界

NOT_REQUIRED 只表示宿主当前不要求授权，不证明用户同意，因为未声明收集类型时也可能如此。每次询问宿主，不把旧状态作为永久事实；协议名来自宿主，不由 SDK 生成。

拒绝是结果，不是权限拒绝异常；未知宿主失败仍是 HostFailure。requireSatisfied 在仍需授权时抛出 PrivacyAuthorizationRequired，绝不自动弹窗；只有消费者可以从手势请求宿主展示协议。

它不与权限生命周期共享状态，不提供隐私政策生成、合规判断或法律意见。宿主缺少 API 不能被解释为无需授权。

## 验证与限制

`PrivacyContractChecks`、`MiniAppPrivacyContractTest`、`WechatPrivacyTest` 验证要求、结果、前置检查与错误区分。执行 `./gradlew :kmp-miniapp-sdk:jsNodeTest`；真实账号与后台声明决定弹窗是否可验收，未完成证据见 [[submodule-wechat-evidence]]。
