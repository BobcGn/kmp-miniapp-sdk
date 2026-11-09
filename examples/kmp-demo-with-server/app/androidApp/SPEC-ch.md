---
id: submodule-server-demo-android
type: submodule-design
status: draft
title: app/androidApp · Android 入口
parent: module-example-server
depends-on: [submodule-server-demo-ui]
---

# 服务器 demo 的 Android 入口

[English](SPEC-en.md)

## 职责与入口

MainActivity 与 Android application 配置承载 shared Compose UI，拥有 manifest、资源打包和 Android 应用生命周期。应用通过 shared 消费 core，不直接复制服务器或 SDK 实现。

## 边界

客户端 renderer、Android API 和本机签名止于应用侧，不进入 Mini App 依赖图。server 模块存在不代表当前 greeting UI 需要后端；新增网络业务不能仅从示例名称推断。

## 验证

在示例根运行 `./gradlew :app:androidApp:assembleDebug`，之后另行安装并运行。已有 APK 构建证据不能被标记为设备 UI、服务器链路或 SDK Android target 验收；本次不提交签名材料。
