---
id: module-example-kmp
type: module-design
status: draft
title: kmp-demo · 实验性无服务器消费者
parent: module-examples
references: [module-miniapp-gradle-plugin]
tags: [experimental-consumer]
---

# 无服务器客户端组合

[English](SPEC-en.md)

## 职责与根配置

作为 [[module-examples]] 所定义的实验性参考消费者，用独立 build 观察已有共享逻辑同时被移动 UI 与微信消费；不是通用工程模板。根通过 logicProject 选择 `:core`，自身不编译 KMP；通过 includeBuild 使用当前仓库源码，不把尚未发布的根级选择当成发布版契约。

## 为什么把 core 与 UI 分开

Mini App compilation 会继承 common source-set 的依赖；如果 shared 同时拥有 Compose UI/资源，单纯增加一个 target 并不能使那些依赖适合微信。本例通过消费者手工分离保留已有行为和移动 UI，观察 core 是否能单独交付 Mini App 产物；原始问题与验证范围记录在 [README](README-ch.md)。

根 logicProject 接入是当前案例形态，不是所有消费者必须采用的布局。模板稳定性、所有平台运行和自动迁移都不由这个实验推出；两种 demo 的角色及这项取舍由父规格拥有。

## 模块依赖

[[submodule-demo-core]] 拥有 greeting、平台信息、Mini App 编译；[[submodule-demo-ui]] api 消费 core，拥有 Compose/资源。[[submodule-demo-android]] 与 [[submodule-demo-ios]] 消费 UI；[[submodule-demo-miniapp]] 只消费 core bundle，不依赖 UI。core 不反向依赖任何宿主。

这次分离是消费者手工完成的布局，不是插件自动迁移。它证明依赖隔离，不是普遍支持将 Compose Res 编译到 Mini App。

## 验证与限制

在本目录用自己的 Wrapper 运行 `./gradlew :core:miniappTest syncMiniApp assembleMiniApp verifyMiniApp`，移动编译与两个 Node smoke 的完整命令见 [README](README-ch.md)。独立测试／构建记录不等于迁移后微信或人工 IDE 验收；服务器 demo 的结果不能替代本例。
