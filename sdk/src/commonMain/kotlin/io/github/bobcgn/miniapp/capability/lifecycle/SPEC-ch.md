---
id: submodule-sdk-lifecycle
type: submodule-design
status: draft
title: lifecycle · App 可见状态
parent: submodule-sdk-capabilities
tags: [lifecycle]
---

# App 可见状态

[English](SPEC-en.md)

## 职责与入口

`MiniAppLifecycle` 报告 App 是否呈现在用户面前，`LifecycleCapabilityProvider` 提供实现，key 为 `lifecycle`。状态为 FOREGROUND/BACKGROUND；宿主尚未报告时为 BACKGROUND，而不是虚构 App 已显示。

## 设计理由

[MiniAppLifecycle.kt](MiniAppLifecycle.kt) 与 ADR 0006 选择的是可共用的 App 可见性，而不是微信页面词汇：WebView 等 runtime 未必有同一种 Page/页面栈。把页面导航上移会让原本可共享的逻辑被迫认识某一种宿主。

消费者仍拥有宿主注册，所以 SDK 接受钩子转发而不抢占 App/Page 入口；代价是消费者必须完成接线。初始 BACKGROUND 只表示尚无已显示的报告，不替 runtime 编造一次前台事件。

## 事件与边界

`state` 是最近状态，`stateChanges` 向新 collector 发出当前值并继续变化；重复的同一状态不再次发出。取消 collector 停止观察，不需要公共 subscription handle。

宿主只把生命周期交给消费者自己的入口，因此 adapter 接受消费者转发的 hook，不拦截注册。Page route、页面栈、组件生命周期与导航不进入这个公共模型。实现使用的 StateFlow 不是事件日志，不能把它解释为每个中间转场都持久保留。

## 验证与限制

`LifecycleContractChecks` 复用于 `FakeMiniAppLifecycle` 与微信 App 实现，`WechatLifecycleTest` 检查 hook 转发。执行 `./gradlew :kmp-miniapp-sdk:jsNodeTest`。真实后台切换不能由 Node 或开发者工具前台显示证明；剩余证据见 [[submodule-wechat-evidence]]。
