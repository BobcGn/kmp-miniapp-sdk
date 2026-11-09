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

## 边界的理由与代价

[MiniAppStorage.kt](MiniAppStorage.kt) 明确把契约限制为字符串原语，而不是某种业务对象编码。其直接后果是公共模型不绑定 serializer、凭据策略或缓存寿命；消费者必须自行决定编码与缓存语义，不能让平台 adapter 猜测。

missing 与空值分开、remove 幂等，使共享逻辑可以观察「不存在」而不依赖微信失败文本。这个可共用的语义由共享 contract checks 约束，不从宿主方法名推导，也不等于清空消费者所有数据的授权。

## 语义与边界

不存在的 key 返回 `null`，空字符串是实际值；set 覆盖已有值；删除不存在的 key 也成功。多个 key 相互隔离。没有清空消费者全部数据的 clear，也不提供 object serialization、过期、secure storage 或云存储。

公共层只依赖能力身份与 Kotlin 类型，不携带微信 option bag、原始 JS value 或平台特有配置。宿主数据无法满足字符串契约时由 adapter 报错，不在公共模型中隐式强转。

## 验证

`StorageContractChecks` 同时驱动 `InMemoryStorage` 与微信 adapter，分别由 `MiniAppStorageContractTest`、`WechatStorageContractTest` 执行。命令为 `./gradlew :kmp-miniapp-sdk:jsNodeTest`。真实存储验收另见 [[submodule-wechat-evidence]]，不从内存 fake 的结果推导。
