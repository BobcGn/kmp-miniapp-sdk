---
id: submodule-demo-ios
type: submodule-design
status: draft
title: iosApp · SwiftUI 应用入口
parent: module-example-kmp
depends-on: [submodule-demo-ui]
---

# 无服务器 demo 的 iOS 入口

[English](SPEC-en.md)

## 职责与入口

Xcode 工程与 SwiftUI App 持有 iOS 生命周期；ContentView 通过 UIViewControllerRepresentable 嵌入 Shared framework 的 `MainViewController()`。Swift 只是客户端桥接，不重新实现 core greeting。

## 边界

UIKit/SwiftUI 和签名设置留在此应用与 shared UI，不进入 SDK 或 Mini App。TEAM_ID 与机器本地配置不能被规格维护补成真实凭据；这是 Xcode host，不是新增 Gradle project。

## 验证

从示例根用 `./gradlew :shared:compileKotlinIosSimulatorArm64` 检查 Kotlin 部分，再用 Xcode 打开 `iosApp.xcodeproj` 验证应用。Kotlin framework 编译不能代替 Xcode 链接、模拟器或真机运行。
