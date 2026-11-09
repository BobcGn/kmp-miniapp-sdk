---
id: submodule-demo-miniapp
type: submodule-design
status: draft
title: miniApp · 无服务器微信宿主
parent: module-example-kmp
depends-on: [submodule-demo-core]
---

# 无服务器 demo 的微信宿主

[English](SPEC-en.md)

## 职责与入口

原生页面 require core 生成模块，调用 platformGreeting 与 SDK 版本，绑定到 WXML/WXSS。jsconfig 与 JSDoc 指向同一 bundle 的声明；UI 不由 Kotlin 生成，也不访问后端。

## 文件与依赖边界

消费者维护页面和项目配置，sync 管理忽略的 libs；独立 distribution 位于示例根 build。页面必须引用真实 core module，不保留旧 shared 编译物；init 不重写手工页面。不提交测试账号或私有配置。

## 验证与限制

从示例根 sync 后在此执行 `node scripts/smoke.cjs`，并在独立 distribution 执行副本。mock devtools/Android/无宿主的结果只是 Node 接线。微信运行、重新打开及本例人工 IDE 导航仍需自己的记录，服务器 demo 截图不能替代。
