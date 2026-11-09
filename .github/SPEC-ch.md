---
id: module-ci-release
type: module-design
status: draft
title: .github · CI 与维护者发布
parent: module-repository-build
depends-on: [module-repository-build]
tags: [ci, release]
---

# CI 与维护者发布

[English](SPEC-en.md)

## 职责与入口

`workflows/ci.yml` 在配置的 PR、push 与手动事件中调用根验证入口；两个 job 分别覆盖快速检查／尺寸／示例类型和插件消费者集成。它们使用独立 CI 环境，不推断独立 demo 的移动端或真实微信结果。

`workflows/release.yml` 是维护者发布保护流程，不属于消费者插件。目前固定历史发布 tag、版本与 commit；不能把它当作当前分支的通用自动发布器，也不移动旧 tag 来包含新代码。

## 发布安全与证据

发布需要签名与 GitHub Repository Secrets；凭据只从授权环境读取，不进入源码、文档、日志或对话。仓库可见性检查区分 200、404、部分可见和网络故障；runtime 必须先可见，才允许插件发布。恢复不重复上传不可变 runtime。

POM 可见、上传接受、独立在线消费者可编译，以及微信运行是不同结论。固定发布源码中的历史测试阻塞不能被当前分支的绿色结果绕过；具体恢复条件见 [RELEASING](../docs/RELEASING-ch.md)。

## 验证与限制

`python3 .github/scripts/test-release-workflow.py` 用隔离 git/curl 响应执行真实 shell 保护代码，不调用发布、不使用 secret。CI 的 Gradle 与 Node 命令见 workflow 及 [[module-repository-build]]，只有实际运行结果才可报告通过。

本次规格维护不触发发布、不扩大 CI 事件范围，也不把自动化当成真机、商户或后端成功。
