---
id: module-miniapp-gradle-plugin
type: module-design
status: draft
title: miniapp-gradle-plugin · 消费者构建集成
parent: architecture
references: [module-sdk]
tags: [gradle-plugin]
---

# 消费者构建集成

[English](SPEC-en.md)

## 职责与公共表面

`io.github.bobcgn.miniapp` 由 `MiniAppGradlePlugin` 实现。公开 extension 为 `MiniAppExtension` 及 typed `wechat` 配置，任务契约为 miniappTest、assembleMiniAppBundle、initMiniApp、syncMiniApp、assembleMiniApp、verifyMiniApp 与 checkMiniAppHostBoundary。它不链接 runtime 实现，只向消费者注入公共 SDK 坐标。

## 平台与配置所有权

命名 `miniapp` 的 Kotlin/JS target 由 KGP 派生真实 source sets、compilations 和 common 父边；插件不手工拼 dependsOn 模型。配置时只实体化标准 Kotlin 源码目录，不初始化微信宿主。Node、CommonJS library 和声明生成属于当前宿主构建形态。

模块模式由编译项目配置；rootHost 将宿主生命周期放在根工程而保留模块 compilation/bundle。非 KMP 根可用 logicProject 在求值前明确选择已有 KMP 子项目：不猜名称、不建模块、不迁移源码、不剔除依赖，也不自动配置该模块的依赖项目。

## 分发与路径边界

init 只显式补缺，sync 只管理 generated area；assemble 从 source host 和当前 KGP 输出建立独立 distribution，不要求先 sync 或 assembleMiniAppBundle；verify 只读取该 distribution。bundle、source、generated、distribution 有不同归属，不能互换。

canonical 路径检查限制 source/distribution 不重叠、generated 严格在 source 内；根级模式另要求 source 在根 build 外、distribution 在根 build 内、唯一 producer 且不覆盖已有任务。模块模式不全面禁止 source 配到 build 内；bundle 检查仅要求在编译项目内。手写文件保护是启发式，不是任意配置防误删的证明。

## 依赖与禁止伸展

KGP 是 compileOnly，版本/坐标资源由 catalog 生成。checkMiniAppHostBoundary 在组装前检查完整解析的 runtime classpath，拒绝已知 renderer 坐标与未解析依赖，不通过删 JS 文件规避依赖问题；不声称检测任意手写 renderer。

不承载 AppID、密钥、商户、模板、业务状态、markup 或部署。公开构建路径与选择逻辑不是 runtime API，不能把构建成功描述为宿主验收。

## 验证与限制

[[submodule-plugin-tests]] 拥有 contract、TestKit 和声明检查；`./gradlew :miniapp-gradle-plugin:test` 与根 `verifyMiniAppGradlePluginIntegration` 覆盖不同范围。独立消费者还需提供兼容 Mini App variants；既有其他命名 JS target 的全兼容性未由当前代表性检查确立。
