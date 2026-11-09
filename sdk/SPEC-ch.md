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

这是 SDK 自动化入口，不证明微信、真机或后端成功。精确能力状态、未实现范围与既有验收由 [[submodule-wechat-evidence]] 索引；本规格导入不运行或补造这些验收。
