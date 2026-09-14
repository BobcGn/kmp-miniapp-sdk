# ADR 0002：不构建 UI Framework

[English](0002-no-ui-framework-en.md)

- 状态：Accepted
- 日期：2026-09-14

## 背景

当前产品边界是共享 Kotlin 逻辑以及面向微信小程序 runtime 的 typed bridge。在基本 SDK bridge 得到验证之前构建 rendering system，会引入另一个产品、架构和维护范围。

## 决策

本项目不构建 UI framework、Virtual DOM、Compose renderer 或 WXML replacement，也不尝试复制或替代 Kuikly。

## 后果

- WXML、WXSS、page rendering 和 component trees 由 host application 负责。
- SDK 工作聚焦于 shared logic、runtime integration 和 typed platform bridges。
- UI experiments 不属于当前 SDK implementation scope。
- 如果未来改变本决策，必须添加新的 ADR 取代本决策，不得静默改写这份历史记录。
