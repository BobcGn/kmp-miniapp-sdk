---
id: module-fixture-published
type: module-design
status: draft
title: miniapp-published-consumer · 发布产物身份
parent: module-fixtures
references: [module-sdk, module-miniapp-gradle-plugin]
---

# 发布依赖与源码归档身份

[English](SPEC-en.md)

## 职责与入口

使用自己的 Wrapper/catalog 固定发布的 Kotlin/Mini App 组合，从公共仓库消费插件和 runtime。`PublishedSdkUsage.kt` 是 Kotlin 调用入口，verifyPublishedSdkSources 检查实际 binary 组件、同版本 sources JAR、源码实现和 KDoc，并生成带路径与 SHA-256 的报告。

## 为什么发布身份需要单独证明

composite build 可以一直解析到本地正确源码，即使公开 sources JAR 缺失、版本不匹配或内容不全。这个消费者因此拒绝本地替换，以实际 resolved component 和归档内容检查 binary/source 对应关系；固定版本的价值是证据身份，不是追随工作树版本。

[build.gradle.kts](build.gradle.kts) 明确拒绝 included builds 与 project 替换，并分开报告归档内容和人工 IDE 验收。这也说明为何仅下载一个源码包不能证明编辑器自动关联了它。

## 隔离边界

没有 includeBuild、mavenLocal 或本地 project substitution；校验器拒绝 project 替换，不能为了本地通过而改用工作树。它不是微信宿主，也不是发布任务；固定版本与当前工作树源码可能不同。

## 验证与限制

在本目录执行 `./gradlew --no-daemon --no-configuration-cache --console=plain compileKotlinMiniapp verifyPublishedSdkSources`。这证明可解析产物与源码内容，不证明 IDE 自动关联、Cmd 点击或悬停；IDEA 已有人工证据保留在 DEVELOPMENT，不据此认证 Android Studio 或 VS Code Kotlin/KMP。
