# Roadmap

[中文](ROADMAP-ch.md)

Nothing in this document should be treated as an implemented capability unless it is also reflected in source code and PROJECT_FACTS-en.md.

All items below are plans. Completion status must be supported by executable configuration, source code, and [PROJECT_FACTS-en.md](PROJECT_FACTS-en.md).

## V0.1 Bootstrap — DONE

- Establish the single `:sdk` Kotlin Multiplatform module.
- Configure the Kotlin/JS CommonJS library target and Node.js tests.
- Establish minimal source sets, build validation, documentation, and repository rules.

## V0.2 JS Consumer Bridge — PLANNED

- Define the first intentional `@JsExport` boundary.
- Generate and inspect its TypeScript declaration.
- Consume the CommonJS artifact through TypeScript `require()`.
- Validate it in WeChat Developer Tools.

## V0.3 First wx Bridge — PLANNED

Evaluate a deliberately small first bridge. Candidates include `wx.login`, `wx.showToast`, or storage. Candidate status is not a commitment to implement all of them.

## V0.4 Coroutine Adapter — PLANNED

Adapt an appropriate callback-based platform API to a suspend-friendly Kotlin API after the raw interop contract is validated.

## Later — PLANNED

- Network integration
- Navigation integration
- Lifecycle integration
- Build automation
- Distribution strategy

## Future / Exploratory

- Page runtime integration
- Component binding
- Declarative UI research
- Renderer research

Exploratory items have no committed version. Any change to the UI non-goal requires an explicit architecture decision.
