---
id: submodule-sdk-wechat-adapter
type: submodule-design
status: draft
title: adapter · 宿主行为与错误适配
parent: submodule-sdk-wechat
depends-on: [submodule-sdk-common, submodule-sdk-wechat-interop]
tags: [adapters, wechat]
---

# 宿主行为与错误适配

[English](SPEC-en.md)

## 职责与内部入口

将 typed callback ports 转为 Kotlin 能力或微信专属结果，拥有类型映射、语义校验、错误分类和资源归属。生产 `Wx*Host` 实现调用 interop，测试注入 fake port；adapter 不承载 UI 或消费者业务。

## 为什么适配不是逐函数转发

宿主的 callback 成功不一定包含满足 SDK 契约的数据，失败文本也不一定能归因于用户取消。因此 adapter 必须验证、保留未知信息并只在有证据时分类，而不能靠强转、补零或模糊文本匹配伪造答案。否则共享逻辑得到的是更容易使用、但含义错误的值。

[WechatErrorMapper.kt](WechatErrorMapper.kt) 集中错误解释，其他 ports 允许测试驱动真实 adapter 而无需 wx。统一的是映射和资源机制，不是把平台专属 API 全部提升为公共能力。每个领域仍以自身契约为准，跨层方向由 [[submodule-sdk-wechat]] 统一说明。

## 行为领域

- Storage 与 HTTP 满足各自公共契约；HTTP 请求原始文本，真实 task abort 接入取消。网络状态 query/listener 分别探测，逐 collector 配对监听；实现有 conflate，不承诺无丢失事件。
- Auth 只取得短期 code 或检查微信登录态，不交换后端 session。权限 scope 在 `WechatPermissionScopes` 唯一映射，权限与隐私分别查询；定位在调用前只检查二者，不自行弹窗。
- 文件系统只适配宿主沙箱 UTF-8 文本和存在／删除；当前没有 SDK 路径规范化或防穿越保证。剪贴板不缓存内容，震动成功不证明人实际感知。
- 扫码与媒体只启动宿主自带交互；精确的模糊 cancel 信号映射为不可归因中断，不推断用户意图。媒体临时资源不归 SDK 长期管理，未知宿主词保留原名。
- 订阅按模板关联结果，不发送消息；标准支付只转发后端参数并报告 interactionCompleted，不签名、不确认订单，也不为虚拟支付建立降级路径。
- 上传／下载保留宿主文件路径与 task，abort 最多一次，进度取宿主读数，终态清理 listener；非 2xx 仍是完成的 exchange。

## 事件资源与已知限制

BLE 是 Experimental：adapter/discovery/connection 用 collector 持有 listener，终止时尝试用相同身份移除；宿主拒绝移除时保留计数，不能保证零残留。channel 有界且丢弃旧值，但按设备去重的 `seen` 集合没有容量上限，不宣称全部状态均有界。

错误分类集中在 `WechatErrorMapper`，未知失败保持 HostFailure，诊断字符串仍可能进入公共错误。某些 adapter 直接构造 UnsupportedCapability，与统一 guard 路径的历史要求有差异，见 [[architecture]]；该差异仍为未决项，不能当成所有调用已统一门控的保证。

## 验证边界

`Wechat*Test`、共享 contract suites 与原始 port fakes 经 `./gradlew :kmp-miniapp-sdk:jsNodeTest` 执行，覆盖迟到／双回调、畸形结果、abort、进度与 listener 身份。真实取消文本、硬件、模板、支付和后端结果仍需 [[submodule-wechat-evidence]] 的独立证据。
