---
id: submodule-sdk-common-tests
type: submodule-design
status: draft
title: commonTest · 参考实现与共享契约测试
parent: module-sdk
depends-on: [submodule-sdk-common]
tags: [testing, contracts]
---

# 参考实现与共享契约测试

[English](SPEC-en.md)

## 职责与入口

验证公共 Kotlin 语义，不验证微信行为。测试范围包括 Host 与版本比较、错误模型、callback 单终态、生成版本关系，以及 storage、HTTP、lifecycle、permission、privacy 的公共契约。

`testing/` 中的 `FakeMiniAppHost` 和内存／记录式能力实现是测试参考实现；`*ContractChecks` 接收被测实现，使相同保证也能在 [[submodule-sdk-js-tests]] 的微信 adapter 上运行。参考实现不能成为 SDK 的 production fallback。

## 边界与依赖

只使用公共代码、internal test-visible 原语、Kotlin test 与 coroutines-test。这里不出现 `wx`、`external`、`dynamic`、`js()`、DOM 或 Node API；不在公共契约里编码某个微信错误词或 callback 形状。

新增跨实现保证放入共享检查；只对微信成立的行为留在平台测试。不得为了测试方便扩大 production public API，也不把 test fake 发布为消费者能力。

## 验证

```shell
./gradlew :kmp-miniapp-sdk:jsNodeTest
```

当前 JS test compilation 同时执行公共测试与平台测试，不存在独立 JVM 测试 target。代表入口为 `HostCallbackAdapterTest`、`MiniAppHostTest` 和各 `MiniApp*ContractTest`；测试文件存在不代表本次已执行。

## 证据限制

这些检查证明公共语义及参考实现行为，不接触真实 `wx`、设备权限或后端。测试本身不能关闭 [[submodule-wechat-evidence]] 的宿主验收缺口。
