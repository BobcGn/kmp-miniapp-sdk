---
id: submodule-sdk-network
type: submodule-design
status: draft
title: network · 文本传输与网络状态
parent: submodule-sdk-capabilities
tags: [network]
---

# 文本 HTTP 与网络状态

[English](SPEC-en.md)

## 职责与公共入口

`MiniAppHttpTransport` 执行一次文本 HTTP exchange，key 为 `network`；`MiniAppNetworkStatus` 查询或观察连接状态，query/listener 各有独立 key。它们回答不同问题，不是带认证、重试、缓存的完整 HTTP client。

## 传输契约

请求承载 method、URL、字符串 headers、已编码文本和可选宿主 timeout；完成的 4xx/5xx 同样返回 response，只有传输失败才抛错。adapter 可通过真实 task abort 响应协程取消，但公共接口不虚构所有宿主均可中止。

没有 payload serialization、cookie、redirect policy、streaming 或文件引用。微信专属上传下载留在平台逃生口，不向本契约塞入宿主文件路径。

## 连接状态与未决差异

状态同时保留 SDK 可识别的类型和宿主原词，未知词使 `networkType` 为 null，而不是丢弃原信息。查询与监听分别门控；观察不轮询、不合成起始读数，每个 collector 持有自己的 listener。

公共 KDoc 写明发出每次宿主状态，但微信实现使用 `conflate()`，慢消费者可能跳过中间状态。此为已核实但尚未裁定的契约差异，不承诺无丢失事件，不通过文档导入改变 API 或实现。

## 验证

`HttpTransportContractChecks` 对参考实现与微信 adapter 复用；`WechatNetworkStatusTest` 验证查询、监听配对及错误。运行 `./gradlew :kmp-miniapp-sdk:jsNodeTest`。真实网络切换和受控 HTTPS 传输仍需独立环境，见 [[submodule-wechat-evidence]]。
