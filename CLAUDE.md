# CLAUDE.md

[中文](CLAUDE-ch.md)

Operating guide for Claude Code in this repository.

This file summarizes the repository rules so they load automatically at the start of a session. It does not replace them. `AGENTS.md` and the nested `sdk/AGENTS.md` and `examples/AGENTS.md` remain authoritative, and this file must not contradict them.

## 1. What this project is

`kmp-miniapp-sdk` is a Kotlin Multiplatform client runtime for sharing client behaviour, host capabilities, and presentation state across Mini App platforms, while keeping rendering host-native. Kotlin/JS carries the current WeChat bridge.

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

It is an SDK / library. The backend owns business truth, Kotlin owns client behaviour, and the host owns rendering — see [ADR 0011](docs/decisions/0011-presentation-state-shared-rendering-host-native-en.md). It is not an application, a UI framework, a Kuikly replacement, a Compose renderer, a Virtual DOM, or a WXML replacement, and it never renders.

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

For source/document or document/document conflicts, follow AGENTS.md Section 2, report the discrepancy, and do not silently change documents to hide it.

## 4. Non-negotiable rules

**Scope discipline.** Without an explicit task, do not add Android, iOS, JVM, or Wasm targets; Compose, Ktor, serialization, or dependency injection frameworks; npm or Maven publication; or speculative Gradle modules.

**UI and rendering boundary.** The runtime SDK and any future Presentation Core must not depend on Compose, another UI framework, or host markup such as WXML or WXSS. Compose is a client UI consumer. Mini App host integration binds host state and host events and never renders. `./gradlew :kmp-miniapp-sdk:checkArchitectureBoundaries` enforces the dependency half and runs as part of `:kmp-miniapp-sdk:check`; see `AGENTS.md` §10 and [ADR 0011](docs/decisions/0011-presentation-state-shared-rendering-host-native-en.md).

**Credential safety.** Never commit AppID secrets, tokens, certificates, or other credentials; examples and tests use placeholders.

**Destructive actions.** Explain the affected scope and obtain user confirmation before file deletion, history rewriting, or bulk rewrites.

**Minimal change.** Make the smallest change the current task requires. Do not add infrastructure because it might be useful later.

**Build authority.** Any claim that the build passes must be backed by an actual Gradle Wrapper invocation. IDE state proves nothing. Every change needs at least one executable validation command appropriate to its scope; report results and checks not run.

**Plans are not facts.** Do not present a roadmap item as shipped work.

**Public API discipline.** `explicitApi()` is enabled. Do not add public API casually. Every public declaration needs explicit visibility and types, a clear SDK responsibility, and appropriate tests. Do not widen the public surface just to make tests easier.

**JS boundary.** `dynamic`, `external`, and `js()` must never enter `commonMain`. Keep raw JavaScript interop as narrow as possible, preferably under `jsMain/.../wechat/interop`.

**Documentation sync.** Human-facing documentation uses synchronized `-en.md` / `-ch.md` pairs, except the authoritative AGENTS.md and the intentional CLAUDE.md / CLAUDE-ch.md guide pair. When either language changes, update its counterpart in the same task with the same facts, structure, and intent. This applies to this file too. A change to a project fact or boundary — capability, supported scope, supported tool or IDE, workflow, verified result, or known limit — updates every fact source that states it (PROJECT_FACTS, ARCHITECTURE, README, DEVELOPMENT, TESTING, platform matrices) in the same PR; see `AGENTS.md` §6.

**ADRs are historical.** Accepted decisions in `docs/decisions/` are records. To change one, add a new ADR that supersedes it; do not rewrite the original.

**Branch and PR workflow.** Before editing tracked files, check the branch/worktree and move to a task branch named `<type>/<short-description>` (for example `docs/refine-agent-rules`, `fix/bob-123-ide-navigation`; never a tool prefix such as `codex/`); never develop or commit on the default/main branch. All repository changes, including documentation and rules, reach the default branch through a PR. Preserve unrelated changes, record branch/commit/PR and validation, and report access blockers without bypassing the workflow. See `AGENTS.md` §11.

**PR and Multica bodies.** Use three ordered sections: what the change does or the record explains; why the action follows from a confirmed need or observed problem; evidence and validation with proved results, pending checks, environments, and limits. Multica prose is Chinese and additionally lists real Issue chains as `BOB-123 -> BOB-124`, distinguishing prerequisites, parallel work, history, and external blockers. Cancelled/deferred Issues do not become active gates. Multica records user-confirmed work rather than initiating requirements or direction. Description maintenance uses `--no-start`, preserves metadata/comments, and is read back. See `AGENTS.md` §12.

**Do not start the next phase.** Finish the current task and report. Do not automatically begin the next roadmap item.

## 5. Layout

```text
AGENTS.md                  repository rules
CLAUDE.md / CLAUDE-ch.md  this operating guide
README-en.md / README-ch.md
docs/                      PROJECT_FACTS, ARCHITECTURE, DEVELOPMENT, TESTING, ROADMAP, decisions/ (ADRs)
gradle/libs.versions.toml  single source of dependency and plugin versions
settings.gradle.kts        includes :kmp-miniapp-sdk and :miniapp-gradle-plugin
sdk/                       the Kotlin/JS runtime SDK module
miniapp-gradle-plugin/     the io.github.bobcgn.miniapp Gradle plugin skeleton
examples/                  integration host; not a Gradle module
```

Inside `sdk/src`:

- `commonMain` — platform-neutral APIs, models, errors, shared logic. No `wx`, no DOM or Node APIs, no `dynamic` / `external` / `js()`.
- `commonTest` — shared tests.
- `jsMain/kotlin/io/github/bobcgn/miniapp/host/wechat/` — `interop`, `adapter`, `runtime`, and `export`, plus the `WechatHost` implementation.
- `jsTest` — JavaScript tests.

Layer responsibilities in `jsMain/.../host/wechat`: `interop` holds raw JavaScript and `wx` contracts and no business logic; `adapter` owns type conversion, error mapping, and async adaptation and no UI responsibilities; `runtime` owns host runtime and lifecycle integration only; `export` owns the Kotlin-to-JavaScript and TypeScript boundary and must not become the business implementation layer.

Dependency direction: `commonMain` never depends on WeChat code; `interop` never depends on business implementation logic; platform code may implement contracts defined by platform-neutral code, never the reverse.

## 6. Verified state and commands

Verified on 2026-09-14:

| Command | Result |
| --- | --- |
| `./gradlew projects` | VERIFIED |
| `./gradlew clean build` | VERIFIED |
| `./gradlew :kmp-miniapp-sdk:jsNodeTest` | VERIFIED |

```bash
./gradlew clean build
```

Toolchain: Gradle 9.3.1 from the checked-in Wrapper, project Kotlin 2.4.20 from `gradle/libs.versions.toml`, `kotlinx-coroutines-core` 1.11.0, verified on JDK 25.0.2. No JDK toolchain is pinned. Always use `./gradlew` (Unix) or `gradlew.bat` (Windows), never a system Gradle.

Current state, as of 2026-09-15:

- Status is experimental / pre-alpha. Bootstrap is complete, and every implemented capability is verified in WeChat Developer Tools and, where applicable, on a real device. The one gap is the permission `NotRequested` state, which the account used could not reproduce and which automated tests cover instead.
- Module list is `:kmp-miniapp-sdk` and `:miniapp-gradle-plugin`. `examples/` is an integration host directory, not a Gradle module. The plugin registers the `miniapp` target and that target's Node.js test run, provisioning `miniappMain` / `miniappTest` as compilation-owned source sets and making `miniappTest` execute, it adds the runtime SDK to `miniappMain` as the public coordinate `io.github.bobcgn:kmp-miniapp-sdk`, `assembleMiniAppBundle` republishes the host bundle at `build/miniapp/bundle/`, and a `miniapp { wechat { } }` extension configures that location; no other build setting is exposed.
- The JavaScript target is configured with `nodejs()`, `useCommonJs()`, `binaries.library()`, and `generateTypeScriptDefinitions()`.
- `sdk/src/commonMain/.../api/MiniAppSdk.kt` declares `MiniAppSdk.VERSION = "0.1.0-SNAPSHOT"`; `commonTest` asserts it.
- `commonMain` contains `MiniAppHost` / `HostPlatformApi`, `HostVersion`, `CapabilityKey` / `CapabilitySupport` with its `Supported` / `Unsupported` / `VersionDependent` / `PermissionDependent` states and the `requireSupported` guard, `MiniAppStorage` with `StorageCapabilityProvider`, `MiniAppHttpTransport` with `NetworkCapabilityProvider`, `MiniAppLifecycle` with `LifecycleCapabilityProvider`, `MiniAppPermissions` with `PermissionCapabilityProvider` and its `PermissionKey` / `PermissionState` model, `MiniAppException`, and the internal `awaitHostCallback` primitive.
- WeChat interop covers typed `login`, `showToast`, Storage, `request`, the three page-stack navigation contracts, the runtime-inspection members with their presence guards, and the three permission methods with their raw authorization-map reader. Storage, WeChat client login, the HTTP transport, navigation, and the permission lifecycle have adapters; `showToast` remains interop-only. `WechatAppLifecycle`, `WechatPageLifecycle`, `WechatRuntimeInfo`, and the capability catalog and gate live in the WeChat `runtime` package, while `WechatPermissions` and `WechatPermissionScopes` live in its `adapter` package. Only the microphone permission is mapped.
- Only the app-level lifecycle is a common capability. Page-level lifecycle, navigation, and the runtime description are WeChat-specific and are reachable only through `WechatPlatformApi`. Capability support is answered from the running host, so it is not constant for a build.
- The compiler-generated TypeScript declaration contains the version, Storage, WeChat login, HTTP transport, lifecycle, navigation, capability-support, and permission exports, and the hand-maintained CommonJS wrapper exposes them as flat functions.

Node.js is only the local Kotlin/JS build and test environment. The intended production host is the WeChat Mini Program JavaScript runtime, and a passing Node.js test does not establish WeChat Mini Program integration. Report those two results separately.

A Git repository is present in this working copy. A change may be uncommitted, so check `git status` before assuming a file matches the last commit.

## 7. Workflow

1. Apply requirement alignment under AGENTS.md Section 14 (full, lightweight, or explicit skip), and read the documents listed in section 2.
2. Confirm the current state in source code and Gradle configuration rather than assuming it from documentation.
3. Check the worktree and switch to a task branch before editing; make the smallest change that satisfies the task.
4. Run executable checks appropriate to the change; use the Gradle Wrapper for build/test validation. For documentation-only work, run document checks and report Gradle as not run when it is not applicable.
5. Update the `-en` / `-ch` documentation pairs affected by the change, including `docs/PROJECT_FACTS-en.md` and `docs/PROJECT_FACTS-ch.md` when modules, dependencies, Kotlin or Gradle versions, targets, public architecture, or current capabilities changed.
6. Submit repository changes through a PR with the three-section evidence body, then report using the format below; do not merge without authorization.

## 8. Final report format

Report:

- Changed
- Validation
- Known issues
- Documentation impact
- Next step

Then stop.

## 9. Requirement Alignment

### Applicability

- Use the full flow for new capabilities, public API changes, multi-file or multi-module changes, ambiguous requirements, or multiple viable approaches.
- For wording, typos, or a clearly described bug, state the understood goal and scope in one or two sentences and wait for user agreement before editing.
- If the user says "directly do it" / "skip alignment", publish a boundary card from available information and execute without another confirmation. "Enough questions" only ends clarification; it does not authorize execution.

### Stage 1: Clarify

- Stay read-only: do not modify version-controlled files or create a branch. Ask why the change is needed until the fundamental goal is clear.
- Separate facts verified under AGENTS.md Section 2 from unverified assumptions. Challenge whether a simpler action, or no change, would satisfy the goal; check AGENTS.md Section 3 scope discipline.
- Ask only 1–3 questions at a time. For a user decision, offer 2–3 options with trade-offs and a recommendation tied to this goal, not convention.
- Roadmaps, TODOs, and comments are not requirement evidence. State risks or unreasonable requests directly.
- When the user says "enough questions", stop questioning and produce the boundary card from current information.

### Stage 2: Boundary Card

Use the same three sections as AGENTS.md Section 12 so the card can become the PR body:

1. **What.** State the fundamental goal in one sentence, verified facts, assumptions and how each will be checked or handled before verification, the minimum solution, and explicit exclusions.
2. **Why.** State the user-confirmed need and why this is the smallest action that satisfies it.
3. **Evidence and validation.** Define testable acceptance criteria and intended Gradle Wrapper commands. For documentation-only work, specify executable document checks and state that Gradle is not run when it is not applicable.

Wait for the user's "confirm" before Stage 3, except for the explicit skip-alignment path.

### Stage 3: Execute

- Follow AGENTS.md Section 11 to create or switch to a task branch, then implement only the agreed boundary card. Do not add features or perform incidental refactoring.
- If an assumption fails, the card must change, or source and documentation conflict, stop and ask the user before proceeding.
- Finish with the AGENTS.md Section 13 Final Report. If the user says "back to Stage 1", invalidate the card and resume clarification.

### Communication and Records

- Reply in Chinese; keep code identifiers and commands in English. Explain a technical term in one sentence on first use, and advance one small step at a time.
- Keep the agreed card in the PR body. Record architecture decisions in docs/decisions when relevant; do not create another document unless the user requests it.
