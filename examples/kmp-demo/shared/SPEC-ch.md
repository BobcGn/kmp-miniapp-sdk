---
id: submodule-demo-ui
type: submodule-design
status: draft
title: shared · Compose UI 与资源
parent: module-example-kmp
depends-on: [submodule-demo-core]
---

# 无服务器 demo 的客户端 UI

[English](SPEC-en.md)

## 职责与入口

拥有 Android/iOS Compose App、资源和供 Swift 调用的 MainViewController；api 消费 core，使 UI 使用已有共享 greeting。iOS 输出 Shared framework，Android 保留资源配置。

## 边界

这里是客户端 renderer 的合法位置，不是 runtime SDK。Compose 依赖和资源不能通过 common 父边进入 core 的 Mini App compilation；本模块不应用 Mini App 插件，不产生微信 bundle，也不把 UI 反向依赖推给 core。

## 验证

示例根 `./gradlew :shared:compileKotlinIosSimulatorArm64 :androidApp:assembleDebug` 检查代表性编译，其他 host tests 见源码和 README。framework 或 APK 生成不证明 iOS/Android 实际运行，更不能证明微信能加载资源。
