# 路线图

[English](ROADMAP-en.md)

除非某项能力同时体现在源码和 PROJECT_FACTS-ch.md 中，否则不得将本文档中的任何内容视为已实现能力。

以下内容均为计划。完成状态必须有可执行配置、源码和 [PROJECT_FACTS-ch.md](PROJECT_FACTS-ch.md) 支持。

## V0.1 Bootstrap — DONE

- 建立单一 `:sdk` Kotlin Multiplatform module。
- 配置 Kotlin/JS CommonJS library target 和 Node.js tests。
- 建立最小 source sets、构建验证、文档和仓库规则。

## V0.2 JS Consumer Bridge — PLANNED

- 定义第一个有明确意图的 `@JsExport` boundary。
- 生成并检查其 TypeScript declaration。
- 通过 TypeScript `require()` 消费 CommonJS artifact。
- 在微信开发者工具中完成验证。

## V0.3 First wx Bridge — PLANNED

评估一个刻意保持最小的首个 bridge。候选包括 `wx.login`、`wx.showToast` 或 storage。候选状态不表示承诺实现全部内容。

## V0.4 Coroutine Adapter — PLANNED

在原始 interop contract 完成验证后，将适合的 callback-based platform API 适配为 suspend-friendly Kotlin API。

## 后续计划 — PLANNED

- Network integration
- Navigation integration
- Lifecycle integration
- Build automation
- Distribution strategy

## 未来 / 探索性内容

- Page runtime integration
- Component binding
- Declarative UI research
- Renderer research

探索性内容没有承诺版本。任何对 UI 非目标的变更都需要明确的 architecture decision。
