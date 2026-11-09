---
id: submodule-sdk-wechat-runtime
type: submodule-design
status: draft
title: runtime · 门控、宿主信息与生命周期
parent: submodule-sdk-wechat
depends-on: [submodule-sdk-common, submodule-sdk-wechat-adapter]
tags: [runtime, capability-gating]
---

# 门控、宿主信息与生命周期

[English](SPEC-en.md)

## 职责与入口

`WechatCapabilityGate` 依据 catalog、运行时版本和 probe 作答，未知 key 或无法确认的 API 失败关闭为 Unsupported。支持状态描述当前 runtime，不是固定的 SDK 清单；当前微信实现不产出 PermissionDependent。

`WechatRuntimeInfo` 将版本和平台分别延迟读取并缓存，优先使用现代 API、旧基础库有兼容回退；`canIUse` 保持实时。基础库版本已知不等于所有 API 都可用。

## 为什么状态在运行时作答

[WechatCapabilityGate.kt](WechatCapabilityGate.kt)、[WechatRuntimeInfo.kt](WechatRuntimeInfo.kt) 与 ADR 0007 的依据是：同一构建可能运行在不同基础库和设备，构建时清单无法回答当前能力。标量版本／平台延迟读取避免模块加载时就触碰尚未就绪的宿主；缓存它们不应冻结 live probe。

这带来的限制是「代码里有 adapter」不等于「当前可调用」，无法确认时必须保守作答。生命周期又来自消费者自己的注册，runtime 只能接收报告，不因能观察状态而取得页面或导航所有权。

## 生命周期与边界

`WechatAppLifecycle` 接收消费者转发的 launch/show/hide，提供公共 App state；`WechatPageLifecycle` 跟踪显示页面的 route 与页面状态，留在微信逃生口。它们不拦截 App/Page 注册、不拥有页面树或导航框架。

runtime 可使用 adapter 的 inspection port，不要求 adapter 只在 runtime 下游。它不解析业务支付或认证状态，不记录权限决定作为缓存，也不自动弹窗。子模块间依赖由 [[submodule-sdk-wechat]] 统一说明。

## 验证与限制

`WechatCapabilityGateTest` 检查版本边界、未知 key、缺失 probe 和构造不触碰宿主；`WechatLifecycleTest` 与 `WechatPageLifecycleTest` 检查消费者钩子语义。执行 `./gradlew :kmp-miniapp-sdk:jsNodeTest`。

版本／平台 cache 与 live probe 是不同保证。真实最低支持版本、App 后台迁移及平台差异只能按 [[submodule-wechat-evidence]] 的具体环境举证。
