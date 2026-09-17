# Roadmap

[中文](ROADMAP-ch.md)

Nothing in this document should be treated as an implemented capability unless it is also reflected in source code and PROJECT_FACTS-en.md.

All items below are plans. Completion status must be supported by executable configuration, source code, and [PROJECT_FACTS-en.md](PROJECT_FACTS-en.md).

## V0.1 Bootstrap — DONE

- Establish the single `:sdk` Kotlin Multiplatform module.
- Configure the Kotlin/JS CommonJS library target and Node.js tests.
- Establish minimal source sets, build validation, documentation, and repository rules.

## P1 Closeout: First-class KMP Mini App Consumer Integration — URGENT / RELEASE BLOCKER

BOB-75 raises the P1 completion bar from “this repository can generate Kotlin/JS for the WeChat example” to “an ordinary KMP project can use the Mini App SDK without understanding its internal Kotlin/JS wiring.” The existing `buildMiniAppSdk` task and WeChat example remain foundation evidence, but they do not replace a Gradle Plugin, `miniappMain` / `miniappTest`, automatic dependency wiring, and a genuine consumer fixture.

The target consumer experience is:

```kotlin
plugins {
    kotlin("multiplatform")
    id("io.github.bobcgn.miniapp")
}
```

Applying the plugin must provide stable `miniappMain` and `miniappTest` entry points, the formal SDK API dependency, and an artifact consumable by WeChat Developer Tools. A PoC must first choose between a named Kotlin/JS target, custom source sets, or a plugin-managed compilation hierarchy; delivery pressure must not weaken the user-facing source-set requirement.

### Strict critical path

Only one critical-path stage advances at a time. A later issue must not become Ready or In Progress before its predecessor is complete:

| Order | Issue | Deliverable | Gate to the next stage |
| --- | --- | --- | --- |
| A | BOB-83 | Minimal Kotlin 2.4.20 / Gradle 9.3.1 PoC comparing a named JS target, custom source sets, and a custom compilation/hierarchy | Record the chosen and rejected approaches, reasons, and KGP limitations; create an ADR if long-lived; retain the hard `miniappMain` / `miniappTest` requirement |
| B | BOB-78 | Independent `io.github.bobcgn.miniapp` Gradle Plugin skeleton | An ordinary KMP project applies the plugin and syncs; a missing KMP plugin produces a clear error |
| C | BOB-76 | Automatic `commonMain → miniappMain` and `commonTest → miniappTest` wiring | Consumers create no source sets manually; IDEA recognition and completion pass; `miniappTest` runs |
| D | BOB-82 | Automatic formal SDK runtime dependency wiring | `miniappMain` directly uses the public API; consumers declare no internal WeChat artifact |
| E | BOB-81 | Hidden CommonJS, TypeScript declaration, runtime dependency, and WeChat artifact assembly | One stable task emits the WeChat-consumable artifact; consumers call no `useCommonJs()` and copy no artifact manually |
| F | BOB-84 | Minimal `miniapp { wechat { ... } }` Gradle DSL | Carry only genuine build configuration and preserve the Mini App Platform / current WeChat Host boundary |
| G | BOB-80 | Independent ordinary KMP consumer fixture | Applying only the plugin compiles `miniappMain`, runs `miniappTest`, reuses `commonMain`, and produces the artifact consumed by the WeChat host |
| H | BOB-79 | Gradle Plugin integration test suite | Automate plugin-apply, missing-KMP, source-set wiring, test execution, dependency wiring, task registration, and consumer-build coverage |
| I | BOB-77 | First-class consumer workflow documentation | Synchronize README, PROJECT_FACTS, ARCHITECTURE, and DEVELOPMENT in English and Chinese; a new user builds Hello World from the README alone |
| Release Gate | BOB-85 | Complete P1 Consumer Integration acceptance | After A–I have reviewable evidence, accept Gradle, IDEA, fixture, artifact, WeChat Developer Tools, and documentation; only then unblock the BOB-51 release gate |

Stage A is decided. The PoC in `poc/kgp-model` selects a plugin-managed Kotlin/JS target named after the platform, because that is the only model in which `miniappMain` and `miniappTest` are real Kotlin source sets owned by a compilation. [ADR 0010](decisions/0010-miniapp-gradle-plugin-source-set-model-en.md) records the decision, the rejected alternatives, and the Kotlin Gradle Plugin limits. The hard `miniappMain` / `miniappTest` requirement is unchanged.

Stage B is done and stage C is in progress. `:miniapp-gradle-plugin` publishes `io.github.bobcgn.miniapp`; applying it registers the `miniapp` target and that target's Node.js test run, so a consumer gets `miniappMain` and `miniappTest` as compilation-owned source sets with `commonMain` and `commonTest` as their parents, plus a `miniappTest` task that actually executes. SDK dependency wiring (BOB-82), artifact assembly (BOB-81) and the WeChat DSL (BOB-84) are not implemented, so those stages remain gated.

### P1 completion evidence

BOB-85 requires all of the following evidence. An implementation report cannot substitute for a missing acceptance result:

- An actual Gradle clean build and `miniappTest` execution for an ordinary KMP fixture.
- Automated assertions that the plugin creates `miniappMain` / `miniappTest` and both dependsOn relationships.
- Automatic SDK dependency resolution, with no internal WeChat artifact or manual Kotlin/JS module-kind configuration in the consumer build.
- A stable assembly task producing the complete WeChat consumer artifact, including declarations and runtime dependencies, with no manual copy step.
- IDEA recognition of both source sets as Kotlin source sets; this needs a screenshot or equivalent reviewable IDE evidence and cannot be replaced by Gradle tests alone.
- The final artifact loading in WeChat Developer Tools and passing the prescribed Consumer Bridge smoke. Real-device, permission, privacy, and backend capabilities remain separately governed by the host-verification matrix.
- Both the Gradle Plugin integration suite and the genuine consumer fixture passing, preventing an SDK-repository-only false green.
- English and Chinese README, PROJECT_FACTS, ARCHITECTURE, and DEVELOPMENT matching the proven workflow.

### Architecture boundaries and non-goals

- The Gradle Plugin owns platform/build integration and developer experience; the Runtime SDK continues to own APIs and Host capabilities.
- Mini App Platform is the abstraction and WeChat is the current Host; the plugin must not encode `MiniApp == WeChat` as the overall architecture.
- This stage does not implement Alipay, Telegram, a multi-Host source-set hierarchy, npm/Maven Central publication, or multiple Mini App targets.
- This stage does not enter the P2 Presentation Runtime or introduce Compose, a renderer, Virtual DOM, or WXML generation.
- BOB-53 `buildMiniAppSdk` is foundation evidence, not the consumer workflow required by BOB-75/85.

## V0.2 JS Consumer Bridge — DONE

- Defined the intentional `@JsExport` boundary.
- Generated and inspected its TypeScript declaration.
- Consumed the CommonJS artifact through TypeScript `require()`.
- Validated it in WeChat Developer Tools.

## V0.3 First wx Bridge — DONE

Storage was selected as the first minimal bridge and now has typed interop, an adapter, automated coverage, and WeChat Developer Tools evidence. That choice does not imply every earlier candidate is implemented.

## V0.4 Coroutine Adapter — DONE

An internal callback-to-coroutine primitive now adapts suitable callback APIs into suspend-friendly Kotlin APIs, while preserving the distinction between operations with a real abort handle and operations where cancellation only stops waiting.

## Remaining P1 Host Capabilities — IN PROGRESS

- Network, Navigation, Lifecycle, and multiple WeChat Host capabilities are implemented; PROJECT_FACTS and the WeChat capability matrix remain authoritative for exact status.
- BOB-60 real-host privacy acceptance remains blocked by account/backend conditions; BOB-73 subscription-message real-host acceptance is in progress.
- Payment, Upload/Download/Network Status, BLE, and Virtual Payment continue under their own issues and are not made implemented by the consumer-integration track.
- The virtual-payment boundary is decided in BOB-74 and recorded in [ARCHITECTURE-en.md](ARCHITECTURE-en.md): a capability separate from Standard Payment, with its own key, request, outcome and backend workflow, and no fallback between the two. P1 implements none of it — the capability is `Planned` in the [WeChat capability matrix](platforms/wechat/WECHAT_CAPABILITIES-en.md) and no code, catalog entry or export exists. Standard payment (BOB-59) is implemented and automated-tested, and its real-host acceptance is blocked on a legal merchant environment, which is an environment block rather than an implementation gap.
- Build automation and consumer distribution close through the urgent BOB-75 to BOB-85 track above.

## After P1: Client Presentation and Additional Hosts

The layering is fixed by [ADR 0011](decisions/0011-presentation-state-shared-rendering-host-native-en.md): the backend owns business truth, the SDK shares client behaviour, host capabilities and presentation state, and the host renders. Everything below is a plan; none of it is implemented.

- **P2 — Client Presentation Core.** `UiState`, `Action`, `Store`, `Effect`, state machine and presentation logic. It must not depend on Compose or any renderer. It does not exist today, and no placeholder module or type has been created for it.
- **P3 — WeChat presentation binding.** `StateFlow` → JavaScript → `setData`, and host event → `Action`. Binding only: no view tree, layout engine, Kotlin UI DSL or WXML generator.
- **P4 — Backend and presentation orchestration maturity.** API client, session, cache, offline behaviour, retry, and the authentication and payment flows.
- **P5 — Additional hosts.** Alipay and Telegram. Adding a host must not require rewriting a Presentation Core.
- **P6 — Optional Compose UI adapter** and richer client integrations. Compose remains a client UI consumer.

Rendering is not on this path. A renderer, virtual DOM, view tree, layout engine or WXML generator remains an explicit non-goal, and changing that requires a new architecture decision rather than a roadmap entry.
