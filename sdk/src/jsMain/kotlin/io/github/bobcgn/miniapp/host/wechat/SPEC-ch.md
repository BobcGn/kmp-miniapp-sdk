---
id: submodule-sdk-wechat
type: submodule-design
status: draft
title: wechat · 微信宿主组合
parent: module-sdk
depends-on: [submodule-sdk-common]
tags: [wechat, host]
---

# 微信宿主组合

[English](SPEC-en.md)

## 职责与入口

`WechatHost` 组合公共能力的 provider facets 与微信专属模型；`WechatPlatformApi` 承载登录、页面栈、设备、文件、传输、支付与实验性 BLE 逃生口。二者均为 internal；消费者当前经 [[submodule-sdk-export]] 使用具体宿主，不存在公开的 Host 构造／注入入口。

Host 为 location 复用与权限、隐私 capability 相同的服务实例，避免另一套状态；其构造不会预先读取 `wx`。公共契约的存在不意味着某个平台专属概念应向公共层搬移。

## 子模块依赖与边界

[[submodule-sdk-wechat-interop]] 提供原始契约、presence 与解码；[[submodule-sdk-wechat-adapter]] 使用这些契约、公共模型与 internal 异步机制；[[submodule-sdk-wechat-runtime]] 使用公共模型和 adapter inspection port，负责门控与 lifecycle。Host 组合后三者，不把它们解释为严格的目录串联。

平台专属请求／结果模型保留在本 namespace，不借宿主成功回调确定后端身份或订单。宿主的原生界面是 capability 的交互对象，不是 SDK 的 renderer。完整系统约束由 [[architecture]] 拥有。

## 验证与限制

`WechatHostTest` 验证组合与 provider 关系；runtime、adapter 与 interop 各有 fake-port suite，统一经 `./gradlew :kmp-miniapp-sdk:jsNodeTest` 执行。测试构造器可注入 fake ports，不等于公开消费者 API。

当前只有微信实现。toast 仅有 interop，虚拟支付、WebSocket 与 Presentation Core 没有实现；BLE 保持 Experimental。各能力真实宿主／后端证据不足的部分以 [[submodule-wechat-evidence]] 为索引。
