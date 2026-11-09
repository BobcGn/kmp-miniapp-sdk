---
id: submodule-fixture-root-logic
type: submodule-design
status: draft
title: shared · 根级夹具逻辑产物
parent: module-fixture-root
depends-on: [module-sdk]
---

# 根级夹具逻辑产物

[English](SPEC-en.md)

## 职责与入口

作为根明确选择的 KMP 项目，commonMain 拥有 greeting，miniappMain 导出宿主入口并读取 SDK runtime 信息。插件提供 Mini App 编译和公共 SDK 依赖，本模块拥有其生成 bundle。

## 边界与依赖

只包含共享行为与宿主 binding，不依赖宿主 UI 或 renderer；不因根级入口而改变 Kotlin compilation 的归属。宿主路径由根唯一管理，模块不能静默覆盖。与宿主的依赖方向见 [[module-fixture-root]]。

## 验证

父项目的 assembleMiniApp/verifyMiniApp 编译并消费此模块，Node smoke 读取实际生成函数。构建及接线证据不确认所有 runtime probe 在真机可用，也不表示新增 SDK targets。
