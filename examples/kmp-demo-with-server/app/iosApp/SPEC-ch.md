---
id: submodule-server-demo-ios
type: submodule-design
status: draft
title: app/iosApp · iOS 入口
parent: module-example-server
depends-on: [submodule-server-demo-ui]
---

# 服务器 demo 的 iOS 入口

[English](SPEC-en.md)

## 职责与入口

SwiftUI App 与 Xcode 工程拥有 iOS 启动，ContentView 通过 UIKit bridge 调用 Shared 的 MainViewController。Kotlin UI/资源仍由 app/shared 编译，不在 Swift 复制共享行为。

## 边界

iOS 平台视图与签名留在消费者应用，不能进入 runtime SDK 或 core 的 Mini App compilation。它不是 SDK 的 iOS target，也不是可由 Node 检验的宿主；本机 Team/Provisioning 信息不提交。

## 验证

用示例 Wrapper 检查 `:app:shared:iosSimulatorArm64Test`，再用 Xcode 打开 `iosApp.xcodeproj` 验证链接与运行。Kotlin 测试、Android 构建或微信导航记录都不能证明本 App 已运行；未执行项保持未验收。
