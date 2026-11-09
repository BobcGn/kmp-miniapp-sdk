---
id: submodule-plugin-tests
type: submodule-design
status: draft
title: plugin tests · 契约、TestKit 与声明检查
parent: module-miniapp-gradle-plugin
depends-on: [module-miniapp-gradle-plugin]
tags: [testkit, testing]
---

# 插件契约、TestKit 与声明检查

[English](SPEC-en.md)

## 职责与分层

contract/unit suite 检查 descriptor、公共坐标、typed extension、依赖分类与纯路径／模板／verifier 规则；TestKit 在临时消费者中检查真实 KGP model、编译、测试报告、distribution、配置缓存及 root 所有权。模型或任务名存在不等于执行成功。

`js/verify-consumer-declarations.cjs` 使用消费者实际 TypeScript 包、配置与生成声明检查类型调用和定义身份；不安装第二套依赖，不把语言服务结果记为 IDE 点击验收。

## 依赖与隔离边界

fixture 让 KGP 与被测插件共享 buildscript classpath，runtime 通过公共坐标和测试侧 composite build 解析，生产插件不知道仓库路径。临时工程不会读取开发者私有微信配置或发布凭据。

测试断言错误信息、源码保留、clean 行为、搬离仓库后的接线等结果，不只断言任务名。真正持久消费者由 [[module-fixtures]] 覆盖，真实宿主结果仍另外记录。

## 验证与限制

```shell
./gradlew :miniapp-gradle-plugin:test
./gradlew verifyMiniAppGradlePluginIntegration
```

前者包含 contract/TestKit，后者还加 SDK guard 和持久 fixture。套件不是微信模拟器、真机、后端或所有 IDE 的认证；精确人工导航范围见 DEVELOPMENT。测试所保留的历史配置不自动扩大插件支持范围。
