# AGENTS.md

These rules apply to the entire repository. A nested `AGENTS.md` may add more specific restrictions for its directory, but it must not conflict with these root rules.

`CLAUDE-en.md` and `CLAUDE-ch.md` are the Claude Code operating guide. They summarize these rules and must stay consistent with them; where they differ, this file is authoritative.

## 1. Read Before Changing

Before modifying the project, read:

- `docs/PROJECT_FACTS-en.md` or `docs/PROJECT_FACTS-ch.md`
- `docs/ARCHITECTURE-en.md` or `docs/ARCHITECTURE-ch.md`
- `docs/DEVELOPMENT-en.md` or `docs/DEVELOPMENT-ch.md`

If the task concerns future scope or sequencing, also read `docs/ROADMAP-en.md` or `docs/ROADMAP-ch.md`.

## 2. Fact Hierarchy

Use this order of authority:

```text
source code and executable Gradle configuration
> docs/PROJECT_FACTS-en.md and docs/PROJECT_FACTS-ch.md
> docs/ARCHITECTURE-en.md and docs/ARCHITECTURE-ch.md
> README-en.md and README-ch.md
> docs/ROADMAP-en.md and docs/ROADMAP-ch.md
```

Never infer an implemented capability from the roadmap.

## 3. Scope Discipline

This project is an SDK / library, not an application, UI framework, or renderer.

Without an explicit task, do not add Android, iOS, JVM, or Wasm targets; Compose, Ktor, serialization, or dependency injection frameworks; npm or Maven publication; or speculative Gradle modules.

## 4. Minimal Change Principle

Make the smallest change required by the current task. Do not add infrastructure because it might be useful later.

## 5. Build Authority

Any claim that the build passes must be supported by an actual Gradle Wrapper invocation. Do not infer build success from IDE state.

## 6. Documentation Synchronization

All human-facing project documentation uses synchronized `-en.md` and `-ch.md` pairs. When either language changes, update its counterpart in the same task with the same facts, structure, and intent.

When a change affects modules, dependencies, Kotlin or Gradle versions, targets, public architecture, or current capabilities, check and update both `docs/PROJECT_FACTS-en.md` and `docs/PROJECT_FACTS-ch.md` when necessary.

## 7. Plans Are Not Facts

Roadmap entries, TODOs, issues, and comments are not evidence that a feature exists.

## 8. Public API Discipline

Do not add public API casually. Every public declaration must have explicit visibility and types, a clear SDK responsibility, and appropriate tests. Do not expand the public surface only to simplify tests.

## 9. JS Boundary

`dynamic`, `external`, and `js()` must not enter `commonMain`. Keep raw JavaScript interop as narrow as possible, preferably under `jsMain/.../wechat/interop`.

## 10. UI and Rendering Boundary

The SDK shares client behaviour, host capabilities, and presentation state. It never renders. [ADR 0011](docs/decisions/0011-presentation-state-shared-rendering-host-native-en.md) records the boundary.

- **The runtime SDK, and any Presentation Core, must not depend on Compose** — not on `androidx.compose.*`, `org.jetbrains.compose.*`, Material or the Compose compiler plugin, not on Android views, UIKit or SwiftUI, and not on host markup such as WXML, WXSS, AXML or HTML. `./gradlew :kmp-miniapp-sdk:checkArchitectureBoundaries` enforces this and runs as part of `:kmp-miniapp-sdk:check`.
- **Compose is a client UI consumer, not SDK infrastructure.** A client application may use Compose; the SDK must not know it exists.
- **Mini App host integration binds, it does not render.** Code under `sdk/src/jsMain` and a consumer's `miniappMain` may bind state to the host's own view mechanism and turn host events into actions. It must not contain a renderer, virtual DOM, view tree, layout engine, Kotlin UI DSL or WXML generator.
- **WXML and WXSS belong to the WeChat host UI**, which today means `examples/wechat-miniprogram`. They do not enter SDK source.
- **The backend owns business truth.** A host success callback, a local cache, or a `UiState` is never the authoritative answer for authentication, payment, inventory, or any other business rule.
- **`app/` and host UI consume the SDK; the SDK never depends on `app/`.**

## 11. Final Report

After completing a task, report:

- Changed
- Validation
- Known issues
- Documentation impact
- Next step

Do not automatically start the next phase.
