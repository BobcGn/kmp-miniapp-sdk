---
id: submodule-sdk-storage
type: submodule-design
status: draft
title: storage · 字符串键值契约
parent: submodule-sdk-capabilities
tags: [storage]
---

# 字符串键值存储契约

[English](SPEC-en.md)

## 职责与入口

`MiniAppStorage` 提供字符串 get/set/remove，`StorageCapabilityProvider` 将实现暴露给消费者，key 为 `storage`。它定义客户端存储原语，不决定缓存寿命或后端业务事实。

## 语义与边界

不存在的 key 返回 `null`，空字符串是实际值；set 覆盖已有值；删除不存在的 key 也成功。多个 key 相互隔离。没有清空消费者全部数据的 clear，也不提供 object serialization、过期、secure storage 或云存储。

公共层只依赖能力身份与 Kotlin 类型，不携带微信 option bag、原始 JS value 或平台特有配置。宿主数据无法满足字符串契约时由 adapter 报错，不在公共模型中隐式强转。

## 验证

`StorageContractChecks` 同时驱动 `InMemoryStorage` 与微信 adapter，分别由 `MiniAppStorageContractTest`、`WechatStorageContractTest` 执行。命令为 `./gradlew :kmp-miniapp-sdk:jsNodeTest`。真实存储验收另见 [[submodule-wechat-evidence]]，不从内存 fake 的结果推导。
