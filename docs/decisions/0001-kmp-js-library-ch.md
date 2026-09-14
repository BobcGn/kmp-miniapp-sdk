# ADR 0001：Kotlin Multiplatform JavaScript Library

[English](0001-kmp-js-library-en.md)

- 状态：Accepted
- 日期：2026-09-14

## 背景

项目需要让微信小程序消费共享 Kotlin 逻辑。微信小程序是 JavaScript host runtime，而不是常规 browser application。项目是 SDK，必须生成可消费的 library，而不是可执行 Web application。

## 决策

使用官方 Kotlin Multiplatform 和 Kotlin/JS target 构建 SDK。将 JavaScript target 配置为 CommonJS library。Node.js 仅用于本地构建和测试；预期生产宿主是微信小程序 JavaScript runtime。

项目不是 Kotlin/JS browser application，也不使用 browser application 或 webpack executable 模型。

## 后果

- 平台无关代码保留在 `commonMain`。
- JavaScript 和微信特定 integration 保留在 `jsMain`。
- 构建生成 library artifact，而不是 application bundle。
- Browser globals 和 DOM APIs 不是架构依赖。
- Host compatibility 最终必须在微信小程序环境中验证；仅有 Node.js tests 不足以证明兼容性。
