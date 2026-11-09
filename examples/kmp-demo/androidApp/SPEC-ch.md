---
id: submodule-demo-android
type: submodule-design
status: draft
title: androidApp · Android 应用入口
parent: module-example-kmp
depends-on: [submodule-demo-ui]
---

# 无服务器 demo 的 Android 入口

[English](SPEC-en.md)

## 职责与入口

Android application 模块拥有 MainActivity、manifest、应用配置与打包，通过 shared UI 显示 core 的共享行为。它不成为 SDK 的 Android target。

## 边界

Android/Compose UI 依赖止于客户端，不进入 Mini App runtime classpath。入口不复制 SDK adapter，不将生成微信库或 WXML 当作 Android 资源；业务逻辑归 core，显示归客户端。

## 验证

在示例根执行 `./gradlew :androidApp:assembleDebug`。APK 可生成只证明构建；安装、启动、UI 和设备行为需要独立运行。签名与本机配置不由规格导入创建或提交。
