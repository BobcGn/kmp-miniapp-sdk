---
id: submodule-sdk-js-tests
type: submodule-design
status: draft
title: jsTest · 微信适配与导出测试
parent: module-sdk
depends-on: [submodule-sdk-common, submodule-sdk-wechat, submodule-sdk-common-tests, submodule-sdk-export]
tags: [testing, wechat]
---

# 微信适配与导出测试

[English](SPEC-en.md)

## 职责与测试表面

在 Kotlin/JS 中用可控制的原始 callback port 验证真实 adapter、interop 对象、runtime gate 和导出 observation session。`host/wechat/testing` 提供 fake raw results 与 ports；它们不替换被测 adapter 的行为。

Storage、HTTP、lifecycle、permission、privacy 等测试复用公共检查，验证同一语义在参考实现与微信实现上成立；另有微信专属失败分类、结果解码、版本边界、abort 和 listener 身份配对测试。

## 边界与依赖

依赖生产 JS 实现、[[submodule-sdk-common-tests]] 的共享检查、Kotlin test 和 coroutines-test。允许用 raw JavaScript 构造畸形宿主结果，但不调用真实 `wx`、弹出权限界面或读取开发者账号凭据。

局部静默 port 留在其场景内，不把测试特殊行为加到 production public API。导出层测试检查显式 start/stop、资源归属和快照窗口；CommonJS 与 TypeScript 宿主消费另由示例 smoke/typecheck 覆盖，不混为同一测试。

## 验证

```shell
./gradlew :kmp-miniapp-sdk:jsNodeTest
./gradlew :kmp-miniapp-sdk:jsNodeTest --tests "*Bluetooth*" --tests "*Ble*"
```

代表 suite 为 `WechatCapabilityGateTest`、`WechatNetworkTest`、`WechatBluetoothTest`、`NetworkStatusObservationTest` 与 `BleObservationSessionTest`。聚焦过滤只覆盖命中的测试，不代表完整 suite 已运行。

## 证据限制

fake host 可复现迟到、双回调、缺失方法和宿主拒绝清理；成功不证明真实微信会给出同一答案。Node 模块接线、微信工具、真机与后端证据由各自入口分别报告，见 [[submodule-wechat-evidence]]。
