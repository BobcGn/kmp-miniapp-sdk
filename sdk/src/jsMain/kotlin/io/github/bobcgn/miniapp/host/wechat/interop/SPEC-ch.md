---
id: submodule-sdk-wechat-interop
type: submodule-design
status: draft
title: interop · wx 原始契约与安全解码
parent: submodule-sdk-wechat
tags: [interop, wechat]
---

# wx 原始契约与安全解码

[English](SPEC-en.md)

## 职责与内部表面

声明全局 `wx`、宿主 option bags、callbacks、task handles 与原始结果形状。plain-object factories 补足 external interface 没有 Kotlin constructor 的限制；presence helpers 在宿主对象或方法缺失时安全返回不可用。

原始值 reader 可以校验类型、有限数值与范围，并区分缺失／错误形状；这里不生成业务结果或决定错误分类。`WxLocation.kt` 等解码器的检查不是把平台策略上移到公共层。

## 边界

`dynamic`、`external`、`js()` 收窄在此原始边界；不得让 raw JS 对象进入 common API。factory 不调用宿主，具体 callback 调用由 adapter port 承载。interop 不依赖业务、export 或宿主 UI，也不添加 Kotlin renderer。

契约有无需要来源支撑。`showToast` 声明存在不代表有 production adapter／export；没有可发现的虚拟支付契约时不预建请求与能力注册。

## 验证

`WxOptionsTest`、`WxRuntimeInfoTest` 及各 `Wx*Test` 在 Node 构造并检查原始形状、缺失方法与无效值。运行 `./gradlew :kmp-miniapp-sdk:jsNodeTest`。fake object 不证明真实基础库的可用性或权限行为。
