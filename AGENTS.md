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
- **WXML and WXSS belong to the WeChat host UI** — `examples/wechat-miniprogram` and the consumer fixture's `host/`. They do not enter SDK source.
- **The backend owns business truth.** A host success callback, a local cache, or a `UiState` is never the authoritative answer for authentication, payment, inventory, or any other business rule.
- **`app/` and host UI consume the SDK; the SDK never depends on `app/`.**

## 11. Branch and Pull Request Workflow

- Never develop, edit tracked project files, or commit directly on the repository's default/main branch (including `main` and `master`). Check the current branch and worktree before changing files, then create or switch to a task branch. Use the `codex/` prefix for Codex-created branches.
- All repository changes, including documentation and rules, must reach the default branch through a pull request. Do not push commits directly to the default branch or merge locally into it. Opening a PR does not authorize merging it.
- Preserve unrelated working-tree changes. Stage and commit only the files owned by the current task.
- Record the branch, commit, PR link, and actual validation results. If pushing or opening a PR is blocked, report the completed local change and the concrete blocker; do not bypass this workflow.

## 12. First-Principles PR and Issue Bodies

Every drafted PR body and Multica Issue body must explain, in this order:

1. **What this change does / what this record explains.** State the concrete behavior, decision, problem, scope, and current state. Separate implemented code, pending verification, deferred work, cancelled work, and historical records.
2. **Why.** Connect the action to a user-confirmed need or an observed problem and the project's actual constraints. A roadmap entry, dependency chain, abstract best practice, or a proposed solution is not by itself evidence that work is necessary. Do not invent historical motives.
3. **Evidence and validation.** Cite relevant source paths, decisions, PRs/commits, commands, environments, and observed results. Separate existing evidence from acceptance checks still to run. Explain what the evidence proves and its limits. Never report planned tests, file presence, Node results, or a status label as real-device/backend success.

Use these three sections for PRs and Issues. Keep acceptance criteria, exclusions, blockers, and documentation impact inside the relevant section rather than dropping them during a rewrite. Translate explanatory prose in Multica Issues into Chinese; retain code identifiers, commands, and source links.

Multica additionally requires a **Progress chain** section:

- Write actual prerequisite relationships as `BOB-123 -> BOB-124`, using the real Issue IDs. Show converging prerequisites explicitly, such as `(BOB-123 + BOB-124) -> BOB-125`; do not imply that independent tasks must run in sequence.
- Distinguish hard prerequisites, parallel work, historical delivery chains, and external conditions. Include the relevant Issues' current states and the evidence/condition that permits the next action.
- A parent/stage relationship or an old description does not prove an active dependency. Cancelled or deferred tasks must not silently remain execution gates; preserve an obsolete chain only when clearly labelled historical.
- Multica records work independently confirmed by the user, progress, decisions, and acceptance evidence. Agents must not use it to discover requirements, choose the development direction, generate a new roadmap, or repack cancelled tasks as new Issues.
- Description-only maintenance must preserve Issue identity, status, priority, ownership, parent/stage, and comments. Use `--no-start`, read back each changed body and the whole project, and do not launch an Agent run.

## 13. Final Report

After completing a task, report:

- Changed
- Validation
- Known issues
- Documentation impact
- Next step

Do not automatically start the next phase.
