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

## 为什么契约检查独立于宿主

同一保证在参考实现和微信 adapter 上运行，才能区分「公共语义定义有误」与「平台映射有误」；否则每个 fake 和 adapter 可以各自通过一套不相同的断言。现有共享检查及 [TESTING](../../../docs/TESTING-ch.md) 给出了该分层的依据。

参考实现只是测量契约的基准，不因在测试里能存储或授权就成为 production fallback。这个边界让能力实现可替换而不替调用方发明成功结果。

## 边界与依赖

只使用公共代码、internal test-visible 原语、Kotlin test 与 coroutines-test。这里不出现 `wx`、`external`、`dynamic`、`js()`、DOM 或 Node API；不在公共契约里编码某个微信错误词或 callback 形状。

新增跨实现保证放入共享检查；只对微信成立的行为留在平台测试。不得为了测试方便扩大 production public API，也不把 test fake 发布为消费者能力。

## 验证

```shell
./gradlew :kmp-miniapp-sdk:jsNodeTest
```

当前 JS test compilation 同时执行公共测试与平台测试，不存在独立 JVM 测试 target。代表入口为 `HostCallbackAdapterTest`、`MiniAppHostTest` 和各 `MiniApp*ContractTest`；测试文件存在不证明测试已实际执行。

## 证据限制

这些检查证明公共语义及参考实现行为，不接触真实 `wx`、设备权限或后端。测试本身不能关闭 [[submodule-wechat-evidence]] 的宿主验收缺口。
