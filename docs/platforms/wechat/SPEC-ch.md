---
id: submodule-wechat-evidence
type: submodule-design
status: draft
title: wechat · 能力状态与验收证据
parent: module-docs
tags: [wechat, verification]
---

# 微信能力状态与验收证据

[English](SPEC-en.md)

## 职责与入口

用 [能力矩阵](WECHAT_CAPABILITIES-ch.md) 索引当前实现与状态，用 [宿主验证矩阵](WECHAT_HOST_VERIFICATION-ch.md) 规定最低环境、复测与证据格式。二者不定义新的 runtime API，也不把计划升级为能力。

## 为什么能力状态与运行证据分别拥有

实现存在只能说明 SDK 写了什么，fake/Node 只能说明构造条件下的行为；权限、真实硬件、账号资格与后端结果还受 SDK 无法控制的环境影响。因此能力矩阵回答当前状态，验证矩阵说明什么证据足以支撑它，而不是用一张绿色清单代替全部环境。

既有两份矩阵及 TESTING 是规则来源。代价是完成一段代码不一定能关闭验收缺口；缺少合法模板、受控服务或商户环境时，应保留限制，而不是为了规格完整而伪造成功。

## 状态边界

`Stable` 需要实现、自动化和规定宿主等级的证据。`Partial` 明确保留语义或验证缺口；`Experimental` 不承诺稳定。`Planned`、`Unsupported` 与 presentation 条目不是已有接口。

最低版本只能来自可引用契约；没有版本依据但实现有 probe 时记为 runtime-resolved，而不是由开发机反推数字。API 存在、权限允许、隐私条件和账号资格是不同答案。

## 证据边界

Unit、Contract、Node、DeveloperTools、RealDevice、BackendRequired 各自只证明所运行的范围。原始记录保留 commit、设备、微信和基础库版本，以及未执行项；旧记录里的版本不改成当前版本。

权限、隐私、App 后台切换、订阅模板、受控传输服务、真实支付和 BLE 连接的未完成验收不得因导入规格而关闭。listener 清理计数归零是一次运行的观察，不是宿主拒绝移除时仍成立的保证。

## 维护与验证

能力或导出改变时同步两种语言的矩阵，并交叉核对 `PROJECT_FACTS-*`、`ARCHITECTURE-*`、`TESTING-*` 与源码。矩阵条目和历史 Issue 链只提供索引，不选择新的开发方向。
