---
id: submodule-fixture-consumer-host
type: submodule-design
status: draft
title: miniApp · 单项目微信宿主
parent: module-fixture-consumer
depends-on: [module-fixture-consumer]
---

# 单项目微信宿主

[English](SPEC-en.md)

## 职责与消费入口

保存 init 后由消费者维护的微信工程。页面 require 生成的 consumer module，展示共享 greeting、计数与 SDK 版本；WXML/WXSS 与 setData 属于此宿主，不复制 SDK 行为。

## 源码与生成边界

`miniprogram/libs/` 由 syncMiniApp 管理并被忽略，不手工放入 UI 或长期保存旧 module。独立验收工程在父构建的 build/miniapp-dist；测试号配置与私有设置不入库，clean 的默认保留性质不扩展到任意自定义路径。

## 验证与限制

先从父项目运行 syncMiniApp，再在本目录执行 `node scripts/host-smoke.cjs`；assembled distribution 内可执行同一脚本。它证明相对 require 接线，不证明微信运行。开发者工具必须导入实际工程并单独记录输出与环境。
