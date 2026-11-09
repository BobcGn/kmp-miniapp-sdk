---
id: module-sdk
type: module-design
status: draft
title: sdk · Mini App 运行时
parent: architecture
tags: [runtime, kotlin-js]
---

# Mini App 运行时

[English](SPEC-en.md)

## 职责与公共表面

本目录对应根构建的 `:kmp-miniapp-sdk`，发布坐标为 `io.github.bobcgn:kmp-miniapp-sdk`。它提供消费者共享逻辑所需的 Kotlin 契约和当前微信能力实现，不拥有消费者构建或最终视图。系统级职责见 [[architecture]]。

公共 Kotlin API 与 JS/TS ABI 是两种消费表面：前者使用接口、模型、错误与协程；后者经 `MiniAppExports` 和编译器声明消费。`WechatHost` / `WechatPlatformApi` 本身为 internal，不存在公开的微信 Host 构造或任意宿主注入入口。

## 为什么 runtime 与构建、宿主 UI 分开

已有 KMP 逻辑需要的是可调用、可测试的宿主行为，而不是 Gradle project 或微信页面对象。runtime 因而拥有契约与适配，插件负责让消费者得到它，Host UI 负责展示；修改 bundle 路径不应改变 storage 语义，修改页面也不应要求重写 adapter。这条职责分割来自 [[architecture]] 的既有决定，并服务于 [[goal-and-requirements]] 的接入主线。

两种消费表面是语言边界适配，不是复制一套行为：Kotlin 保留契约，JS/TS 获得可构造的导出形状。当前具体 Host 的公开入口动机仍未确认，不能从 internal 实现推导永久不开放的政策；该未确认项归 [[submodule-sdk-wechat]]。

## 模块结构与依赖

- [[submodule-sdk-common]] 拥有公共契约、版本 metadata、错误和 internal 异步原语，进一步分出 Host 与各能力领域。
- [[submodule-sdk-wechat]] 实现公共契约并保留微信专属模型与逃生口，其子树说明 interop、adapter 和 runtime。
- [[submodule-sdk-export]] 依赖公共契约与微信实现，适配宿主可消费形状而不接管宿主操作。
- [[submodule-sdk-common-tests]] 只依赖公共逻辑和参考实现；[[submodule-sdk-js-tests]] 复用共享检查并驱动微信实现与导出会话。

这些是同一 SDK 产物中的 source-set／package 边界，不是新建独立 Gradle modules。公共代码不向上依赖平台实现，生产代码也不依赖 test source sets、示例或消费者插件。

## 构建边界

`build.gradle.kts` 配置唯一 JS library target、CommonJS 和 TypeScript declaration；Node 是本地测试环境，不是生产宿主。`explicitApi()` 约束公开声明，版本值由 catalog 生成，不在源码中维护第二份字面量。

生产依赖是 Kotlin 与 coroutines。UI framework、宿主 markup 和业务事实的禁区由架构定义；`checkArchitectureBoundaries` 只检查已声明的坐标和 Kotlin namespace 分类清单，不是任意 renderer 检测。

## 验证与限制

```shell
./gradlew :kmp-miniapp-sdk:checkArchitectureBoundaries
./gradlew :kmp-miniapp-sdk:jsNodeTest
```

这是 SDK 自动化入口，不证明微信、真机或后端成功。精确能力状态、未实现范围与既有验收由 [[submodule-wechat-evidence]] 索引；规格存在本身不构成这些验收的执行证据。
