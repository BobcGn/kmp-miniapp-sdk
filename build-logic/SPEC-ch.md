---
id: module-build-logic
type: module-design
status: draft
title: build-logic · 包体积测量与门禁
parent: architecture
tags: [build-logic, measurement]
---

# 包体积测量与门禁

[English](SPEC-en.md)

## 职责与入口

独立 included build 提供仓库专用 `io.github.bobcgn.miniapp.size` 插件，注册 verifyMiniAppBundleSize 与显式 updateMiniAppSizeBaseline；它不发布、不被消费者应用，也不把实现链接进 runtime。

默认测量 SDK production distribution，也可通过 miniappSizeDirectory 指向已构建的其他 bundle。按文件名分 sdk/runtime/consumer/development，逐文件测量原始与 gzip 字节；source maps、声明与 manifest 参与完整 distribution 分类，markup 被拒绝而不是记作 SDK 成本。

## 为什么测量产物而不测量源码目录

消费者承担的是编译 module、runtime 与辅助元数据的成本，源码行数或仓库 checkout 大小无法替代它。任务因而读取实际 distribution，并分别报告分类与总量；把 markup 混进 SDK 体积会把宿主 UI 成本归给错误的所有者。

[PERFORMANCE_BASELINE](../docs/PERFORMANCE_BASELINE-ch.md) 解释了确定性 gzip 和双阈值的依据：小幅编译器漂移不应触发误报，已理解的实质增长又应通过评审接受，而不是自动改基线。这个软门禁不是 SDK 启动或生产可靠性保证。

## 确定性与阈值

相同文件按相对路径稳定排序，使用相同 gzip 规则得到相同测量；重编译后输入可能有少量差异，不能承诺编译器逐字节可重现。机器可读基线为 `docs/performance-baseline.json`，保留版本、日期、commit 与 gzip context。

门禁比较原始字节增长：每类与总量同时达到或超过 4,096 字节、5% 才触发；gzip 是报告指标而非独立门禁。减少不失败，development 也参与。更新基线是显式维护者决定，不为让检查通过而自动接受增长。

## 边界与验证

不测 SDK 独立启动时间、真实网络传输量、内存或微信性能；开发者工具 Launch Time 是整个宿主的人工观察。尺寸任务不接入普通 check，避免每次验证强制生产 JS distribution。

`./gradlew :build-logic:test` 检查分类、测量、基线与阈值；`./gradlew verifyMiniAppBundleSize` 测量真实产物。历史测量数字的上下文见 PERFORMANCE_BASELINE 文档，不能代替对当前产物的实际测量。
