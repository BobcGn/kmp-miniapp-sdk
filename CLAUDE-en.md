# CLAUDE.md

[中文](CLAUDE-ch.md)

Operating guide for Claude Code in this repository.

This file summarizes the repository rules so they load automatically at the start of a session. It does not replace them. `AGENTS.md` and the nested `sdk/AGENTS.md` and `examples/AGENTS.md` remain authoritative, and this file must not contradict them.

## 1. What this project is

`kmp-miniapp-sdk` is a Kotlin Multiplatform SDK that bridges shared Kotlin logic into WeChat Mini Programs through Kotlin/JS.

```text
commonMain
    ↓
jsMain
    ↓
Kotlin/JS
    ↓
TypeScript / JavaScript
    ↓
WeChat Mini Program
```

It is an SDK / library. It is not an application, a UI framework, a Kuikly replacement, a Compose renderer, a Virtual DOM, or a WXML replacement.

## 2. Read before changing

Read these before modifying the project:

1. `docs/PROJECT_FACTS-en.md` or `docs/PROJECT_FACTS-ch.md`
2. `docs/ARCHITECTURE-en.md` or `docs/ARCHITECTURE-ch.md`
3. `docs/DEVELOPMENT-en.md` or `docs/DEVELOPMENT-ch.md`
4. `docs/ROADMAP-en.md` or `docs/ROADMAP-ch.md` — only when the task concerns future scope or sequencing

Then, for the affected area, read the nested rules: `sdk/AGENTS.md` for `sdk/**`, `examples/AGENTS.md` for `examples/**`.

## 3. Fact hierarchy

Use this order of authority:

```text
source code and executable Gradle configuration
> docs/PROJECT_FACTS-en.md and docs/PROJECT_FACTS-ch.md
> docs/ARCHITECTURE-en.md and docs/ARCHITECTURE-ch.md
> README-en.md and README-ch.md
> docs/ROADMAP-en.md and docs/ROADMAP-ch.md
```

Never infer an implemented capability from the roadmap. Roadmap entries, TODOs, issues, and comments are plans, not evidence that a feature exists.

## 4. Non-negotiable rules

**Scope discipline.** Without an explicit task, do not add Android, iOS, JVM, or Wasm targets; Compose, Ktor, serialization, or dependency injection frameworks; npm or Maven publication; or speculative Gradle modules.

**Minimal change.** Make the smallest change the current task requires. Do not add infrastructure because it might be useful later.

**Build authority.** Any claim that the build passes must be backed by an actual Gradle Wrapper invocation. IDE state proves nothing.

**Plans are not facts.** Do not present a roadmap item as shipped work.

**Public API discipline.** `explicitApi()` is enabled. Do not add public API casually. Every public declaration needs explicit visibility and types, a clear SDK responsibility, and appropriate tests. Do not widen the public surface just to make tests easier.

**JS boundary.** `dynamic`, `external`, and `js()` must never enter `commonMain`. Keep raw JavaScript interop as narrow as possible, preferably under `jsMain/.../wechat/interop`.

**Documentation sync.** Human-facing documentation uses synchronized `-en.md` / `-ch.md` pairs. When either language changes, update its counterpart in the same task with the same facts, structure, and intent. This applies to this file too.

**ADRs are historical.** Accepted decisions in `docs/decisions/` are records. To change one, add a new ADR that supersedes it; do not rewrite the original.

**Do not start the next phase.** Finish the current task and report. Do not automatically begin the next roadmap item.

## 5. Layout

```text
AGENTS.md                  repository rules
CLAUDE-en.md / CLAUDE-ch.md  this operating guide
README-en.md / README-ch.md
docs/                      PROJECT_FACTS, ARCHITECTURE, DEVELOPMENT, ROADMAP, decisions/ (ADRs)
gradle/libs.versions.toml  single source of dependency and plugin versions
settings.gradle.kts        includes only :sdk
sdk/                       the only Gradle module
examples/                  integration host; not a Gradle module
```

Inside `sdk/src`:

- `commonMain` — platform-neutral APIs, models, errors, shared logic. No `wx`, no DOM or Node APIs, no `dynamic` / `external` / `js()`.
- `commonTest` — shared tests.
- `jsMain/kotlin/io/github/bobcgn/miniapp/wechat/` — `interop`, `adapter`, `runtime`, `export`. These directories currently establish boundaries only and contain no implementation.
- `jsTest` — JavaScript tests.

Layer responsibilities in `jsMain/wechat`: `interop` holds raw JavaScript and `wx` contracts and no business logic; `adapter` owns type conversion, error mapping, and async adaptation and no UI responsibilities; `runtime` owns host runtime and lifecycle integration only; `export` owns the Kotlin-to-JavaScript and TypeScript boundary and must not become the business implementation layer.

Dependency direction: `commonMain` never depends on WeChat code; `interop` never depends on business implementation logic; platform code may implement contracts defined by platform-neutral code, never the reverse.

## 6. Verified state and commands

Verified on 2026-09-14:

| Command | Result |
| --- | --- |
| `./gradlew projects` | VERIFIED |
| `./gradlew clean build` | VERIFIED |
| `./gradlew :sdk:jsNodeTest` | VERIFIED |

```bash
./gradlew clean build
```

Toolchain: Gradle 9.3.1 from the checked-in Wrapper, project Kotlin 2.4.20 from `gradle/libs.versions.toml`, `kotlinx-coroutines-core` 1.11.0, verified on JDK 25.0.2. No JDK toolchain is pinned. Always use `./gradlew` (Unix) or `gradlew.bat` (Windows), never a system Gradle.

Current state, as of 2026-09-14:

- Status is experimental / pre-alpha. Bootstrap is complete.
- Module list is `:sdk` only. `examples/` is an integration host directory, not a Gradle module.
- The JavaScript target is configured with `nodejs()`, `useCommonJs()`, `binaries.library()`, and `generateTypeScriptDefinitions()`.
- `sdk/src/commonMain/.../api/MiniAppSdk.kt` declares `MiniAppSdk.VERSION = "0.1.0-SNAPSHOT"`; `commonTest` asserts it. That is the entire current public surface.
- The generated TypeScript declaration contains no SDK export API because no `@JsExport` boundary exists yet.
- No `wx` wrapper, lifecycle adapter, network/storage/auth adapter, callback-to-coroutine bridge, or consumer-facing JS/TS export API is implemented.

Node.js is only the local Kotlin/JS build and test environment. The intended production host is the WeChat Mini Program JavaScript runtime, and a passing Node.js test does not establish WeChat Mini Program integration. Report those two results separately.

There is no `.git` directory in this working copy, so no git history is available to consult.

## 7. Workflow

1. Read the documents listed in section 2 for the area you are touching.
2. Confirm the current state in source code and Gradle configuration rather than assuming it from documentation.
3. Make the smallest change that satisfies the task.
4. Validate with the Gradle Wrapper.
5. Update the `-en` / `-ch` documentation pairs affected by the change, including `docs/PROJECT_FACTS-en.md` and `docs/PROJECT_FACTS-ch.md` when modules, dependencies, Kotlin or Gradle versions, targets, public architecture, or current capabilities changed.
6. Report using the format below.

## 8. Final report format

Report:

- Changed
- Validation
- Known issues
- Documentation impact
- Next step

Then stop.
