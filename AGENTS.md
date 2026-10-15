# AGENTS.md

These rules apply to the entire repository. A nested `AGENTS.md` may add more specific restrictions for its directory, but it must not conflict with these root rules.

`CLAUDE.md` and `CLAUDE-ch.md` are the Claude Code operating guide. They summarize these rules and must stay consistent with them; where they differ, this file is authoritative.

## 1. Read Before Changing

Before modifying the project, read:

- `docs/PROJECT_FACTS-en.md` or `docs/PROJECT_FACTS-ch.md`
- `docs/ARCHITECTURE-en.md` or `docs/ARCHITECTURE-ch.md`
- `docs/DEVELOPMENT-en.md` or `docs/DEVELOPMENT-ch.md`

For new requirements, follow Section 14 before implementation; clarification stays read-only.

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

When source and documentation, or documents, conflict, follow the hierarchy above and report the discrepancy to the user. Do not silently rewrite documentation to hide it.

## 3. Scope Discipline

This project is an SDK / library, not an application, UI framework, or renderer.

Without an explicit task, do not add Android, iOS, JVM, or Wasm targets; Compose, Ktor, serialization, or dependency injection frameworks; npm or Maven publication; or speculative Gradle modules.

Never commit AppID secrets, tokens, certificates, or other credentials. Use placeholders in examples and tests.

## 4. Minimal Change Principle

Make the smallest change required by the current task. Do not add infrastructure because it might be useful later.

Before destructive actions such as deleting files, rewriting history, or bulk rewrites, explain the affected scope and obtain user confirmation.

## 5. Build Authority

Any claim that the build passes must be supported by an actual Gradle Wrapper invocation. Do not infer build success from IDE state.

Every change must have at least one executable validation command appropriate to its scope; report the command, result, and any checks not run.

## 6. Documentation Synchronization

AGENTS.md is authoritative; CLAUDE.md and CLAUDE-ch.md form the synchronized English/Chinese operating-guide pair with intentionally different filenames.

All other human-facing project documentation uses synchronized `-en.md` and `-ch.md` pairs. When either language changes, update its counterpart in the same task with the same facts, structure, and intent.

When a change affects modules, dependencies, Kotlin or Gradle versions, targets, public architecture, or current capabilities, check and update both `docs/PROJECT_FACTS-en.md` and `docs/PROJECT_FACTS-ch.md` when necessary.

When a change alters a project fact or boundary — a capability, supported scope, supported tool or IDE, workflow, verified result, or known limit — update every fact source that states it in the same task and PR: `docs/PROJECT_FACTS-*`, `docs/ARCHITECTURE-*`, `README-*`, `docs/DEVELOPMENT-*`, `docs/TESTING-*`, and the platform capability and verification matrices, in both languages. Check each of these sources; leave unaffected ones unchanged and state in the PR or report which sources were checked and found unaffected. Do not leave one fact source contradicting another.

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

- Never develop, edit tracked project files, or commit directly on the repository's default/main branch (including `main` and `master`). Check the current branch and worktree before changing files, then create or switch to a task branch. Name task branches `<type>/<short-description>`, where `<type>` states the kind of change — `feat`, `fix`, `docs`, `refactor`, `test`, `build`, or `chore` — and `<short-description>` is lowercase English kebab-case, optionally starting with the Multica Issue key, for example `docs/refine-agent-rules` or `fix/bob-123-ide-navigation`. Do not name branches after the tool or agent that created them, such as `codex/` or `claude/`.
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

## 14. Requirement Alignment

### Applicability

- Use the full flow for new capabilities, public API changes, multi-file or multi-module changes, ambiguous requirements, or multiple viable approaches.
- For wording, typos, or a clearly described bug, state the understood goal and scope in one or two sentences and wait for user agreement before editing.
- If the user says "directly do it" / "skip alignment", publish a boundary card from available information and execute without another confirmation. "Enough questions" only ends clarification; it does not authorize execution.

### Stage 1: Clarify

- Stay read-only: do not modify version-controlled files or create a branch. Ask why the change is needed until the fundamental goal is clear.
- Separate facts verified under AGENTS.md Section 2 from unverified assumptions. Challenge whether a simpler action, or no change, would satisfy the goal; check Section 3 scope discipline.
- Ask only 1–3 questions at a time. For a user decision, offer 2–3 options with trade-offs and a recommendation tied to this goal, not convention.
- Roadmaps, TODOs, and comments are not requirement evidence. State risks or unreasonable requests directly.
- When the user says "enough questions", stop questioning and produce the boundary card from current information.

### Stage 2: Boundary Card

Use the same three sections as Section 12 so the card can become the PR body:

1. **What.** State the fundamental goal in one sentence, verified facts, assumptions and how each will be checked or handled before verification, the minimum solution, and explicit exclusions.
2. **Why.** State the user-confirmed need and why this is the smallest action that satisfies it.
3. **Evidence and validation.** Define testable acceptance criteria and intended Gradle Wrapper commands. For documentation-only work, specify executable document checks and state that Gradle is not run when it is not applicable.

Wait for the user's "confirm" before Stage 3, except for the explicit skip-alignment path.

### Stage 3: Execute

- Follow Section 11 to create or switch to a task branch, then implement only the agreed boundary card. Do not add features or perform incidental refactoring.
- If an assumption fails, the card must change, or source and documentation conflict, stop and ask the user before proceeding.
- Finish with the Section 13 Final Report. If the user says "back to Stage 1", invalidate the card and resume clarification.

### Communication and Records

- Reply in Chinese; keep code identifiers and commands in English. Explain a technical term in one sentence on first use, and advance one small step at a time.
- Keep the agreed card in the PR body. Record architecture decisions in docs/decisions when relevant; do not create another document unless the user requests it.
