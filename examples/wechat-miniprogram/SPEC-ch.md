---
id: module-example-wechat
type: module-design
status: draft
title: wechat-miniprogram · 能力验收宿主
parent: module-examples
depends-on: [module-sdk]
---

# SDK 能力验收宿主

[English](SPEC-en.md)

## 职责与入口

用原生 App/Page、WXML/WXSS 和薄 TypeScript 展示各 SDK 能力检查，转发消费者拥有的生命周期钩子。`miniprogram/libs/kmp-miniapp-sdk.js` 与 `.d.ts` 是手写归一层；其余编译器产物由根 buildMiniAppSdk 刷新，不在页面复制 adapter。

## 消费与安全边界

页面只绑定状态、事件与 capability 调用，不替 SDK 渲染，也不以 host success 确认认证、支付或送达。需要用户手势的能力只从显式动作触发；模板、支付、受控传输未配置时保持不调用宿主的保护。

只报告封闭诊断和结果形状，不显示 login code、坐标、扫码内容、媒体路径、模板或支付凭据。真实 AppID/私有配置不入库。该工程不同于两个 KMP greeting demo，不共享它们的验收结论。

## 验证与限制

先在 SDK 根运行 `./gradlew buildMiniAppSdk`，再在此执行 `npm ci && npm run smoke && npm run typecheck`。smoke 安装 fake wx，typecheck 检查生成声明；真实宿主步骤与未完成的权限／后端证据由 [[submodule-wechat-evidence]] 索引。
