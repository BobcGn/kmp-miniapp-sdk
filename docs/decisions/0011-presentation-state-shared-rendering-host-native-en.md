# ADR 0011: Presentation State Is Shared; Rendering Remains Host-Native

[中文](0011-presentation-state-shared-rendering-host-native-ch.md)

- Status: Accepted
- Date: 2026-09-17

## Context

ADR 0010 established how a consumer gets a Mini App platform target. It left a different question unanswered, and the consumer-integration work made it urgent: which parts of an application may this SDK share, and where does sharing stop?

Two failure modes are equally easy to fall into. One is a "share everything" SDK that absorbs business rules the backend owns and ends up with a second, weaker source of truth. The other is a Kotlin wrapper over `wx.*` that shares almost nothing and leaves every consumer reimplementing the same client behaviour. The same ambiguity applies to UI: nothing in the current repository stops a future contributor from putting a renderer behind `miniappMain`, or from making the presentation layer depend on Compose so that only Compose clients can use it.

This matters because the SDK is consumed by more than one kind of client. An Android, iOS or desktop client renders with Compose. The current WeChat host renders with WXML and WXSS. A future Alipay host renders with AXML and ACSS, and a Telegram host with HTML. If presentation state were expressed in terms of any one of those, the others could not use it, and the SDK would have quietly become a Compose library with a WeChat adapter.

A naming trap is worth recording explicitly: an external demonstration project has a Gradle module called `app/shared`. It is a Compose client application that consumes the SDK. A module named `shared` is not automatically SDK code, and this repository was audited against that assumption rather than trusting the name.

## Decision

**The SDK shares client behaviour, host capabilities and presentation state. It never renders. The platform host owns the final view.**

The scaffold uses `miniappMain` and `miniappTest` for host integration. Host integration is not rendering.

Three principles, in order of authority:

1. **Backend owns business truth.** Identity and server sessions, token lifetime, order and payment final state, inventory, reservations, business rules, and every server-side authorisation fact. A host success callback, a local cache, and a `UiState` are client observations, never business truth.
2. **Kotlin owns client behaviour.** API clients, session handling on the client, caching policy, error and async models, retry and cancellation policy, host-capability abstraction, flow orchestration, and — from P2 — presentation state and presentation logic.
3. **Host owns rendering.** The final view, layout, native components and every rendering decision.

### The four layers

| Layer | Owns | Must not |
| --- | --- | --- |
| **Backend** | Authentication and server session, token lifetime, order and payment final state, inventory, reservations, business rules, server-side authorisation and trusted facts | Be reimplemented on the client |
| **KMP Client Runtime** | Domain model, use cases, repository contracts, error model, async and coroutine abstraction, host-capability contracts, cache policy, and from P2 `UiState` / `Action` / `Store` / `Effect` / state machine / presentation logic | Depend on Compose, any UI framework, or host markup; copy server-authoritative rules; treat its own cache as truth |
| **Host Integration** | WeChat today — adapters and error mapping for host APIs, runtime and capability detection, permission, privacy, lifecycle, navigation, JS binding, presentation binding | Define the view, a layout, a view tree, or a renderer |
| **Platform UI** | The final view and layout: Compose for Android, iOS and desktop clients; WXML and WXSS for the WeChat host; AXML and ACSS for a future Alipay host; HTML for a future Telegram host | Live inside the SDK |

### The four levels of sharing

1. **Shared client infrastructure** — API client, session, error, cache, async. Shared.
2. **Shared host-capability abstraction** — storage, location, scanner, haptics, network, and the rest of the current capability set. Shared.
3. **Shared presentation** — `UiState`, `Action`, `Store`, `Effect`, state machine. Planned for P2. **Not implemented today.**
4. **Shared rendering** — view, layout, renderer, component tree. Explicitly out of scope, permanently.

Level 3 is the boundary this ADR exists to fix: presentation *state and behaviour* are portable, presentation *rendering* is not. A `UiState` is a value; a `Column` is a layout instruction for one specific renderer.

### Hard boundaries

- **SDK Core must not depend on Compose.** Not on `androidx.compose.*`, not on `org.jetbrains.compose.*`, not on Material or the Compose compiler plugin, and not on Android views, UIKit or SwiftUI.
- **A Presentation Core, when it exists, must not depend on Compose either.** It may carry `UiState` and `Action`; it may not carry a composable.
- **Compose is a consumer, not SDK infrastructure.** A Compose client collects state, dispatches actions and renders. The SDK does not know it exists.
- **`miniappMain` is host integration and binding only.** It may convert host events into actions and state into `setData`. It must not contain a renderer, a virtual DOM, a view tree, a layout engine, a Kotlin UI DSL or a WXML generator.
- **WXML, WXSS and host markup never enter the SDK Core**, for the same reason Compose does not: they are one renderer's vocabulary.
- **`app/` and any host UI consume the SDK; the SDK never depends on `app/`.** This is structural rather than aspirational: examples and consumer applications are not Gradle modules of this repository.
- **The client is untrusted.** Reusing Kotlin code and sharing presentation state does not move the trust boundary. A host login produces a credential for the backend to verify; a backend signs the session. A host payment call produces an interaction result; the backend holds the order and confirms the final state.
- **Server domain model, client DTO and presentation model are separate.** A wire model is not a presentation model, and neither is assumed to be exposed as-is.
- **Deleting every Compose dependency from a client must leave SDK Core and a future Presentation Core architecturally intact and buildable.** This is the test the layering has to pass, and it is enforced in the build rather than asserted in prose.

### Enforcement

`./gradlew :kmp-miniapp-sdk:checkArchitectureBoundaries` fails when the runtime SDK imports or declares a dependency from a UI framework namespace, including `androidx.compose.*`, `org.jetbrains.compose.*`, `android.view.*`, `android.widget.*`, `androidx.activity.*`, `kotlinx.html.*` and `com.android.*`. A group that exists only to serve a UI framework is forbidden whole; a library whose group also carries a dependency this SDK legitimately uses is forbidden by exact module coordinate, so `org.jetbrains.kotlinx:kotlinx-html` is rejected without also rejecting `org.jetbrains.kotlinx:kotlinx-coroutines-core`. The task checks its own classifier against a fixed table of expected verdicts before it applies it to the model, so the rule cannot drift unnoticed. It is wired into `:kmp-miniapp-sdk:check`, so the constraint is a build failure rather than a review comment. The same check is the one to apply to a `presentation` module when one exists.

The same rule reaches consumers through the Gradle plugin. `checkMiniAppHostBoundary` fails a Mini App build whose runtime classpath carries Compose, Skiko, a Compose-specific AndroidX integration artifact, `kotlinx-browser` or `kotlinx-html`, and it runs before the host distribution is assembled. Non-Compose lifecycle, saved-state and navigation primitives remain valid shared behaviour and are not rejected. Without it, a consumer whose common source set contains Compose UI would build a Mini App artifact that no host can run — the build would succeed and the artifact would simply be unusable. It is a dependency rule rather than a file rule, because the files in a Kotlin/JS distribution reach one another through `require()`.

What the check cannot do is decide meaning. A renderer assembled from the SDK's own types imports no UI framework and declares no UI framework dependency, so it would pass; that class of mistake remains a review responsibility, and the check does not claim to cover it.

## Consequences

- A client on a platform without Compose can use everything the SDK shares, because nothing it shares is expressed in a renderer's terms.
- The SDK cannot become a Compose library by accident: the build rejects it.
- Adding a host means writing an adapter and a renderer for that host, not rewriting shared presentation logic. The explicit requirement is that adding Alipay or Telegram must not require rewriting a Presentation Core.
- Replacing the typeScript/Kotlin split in the WeChat host, or replacing the host's own UI runtime, leaves the SDK's layers untouched, because the SDK never derived anything from them.
- `UiState`, `Action`, `Store` and `Effect` are **ownerless-but-unimplemented** today: their home is defined, and no code exists. No empty module or placeholder type was created to satisfy the vocabulary. BOB-85 requires this to stay true — the ownership may be stated, the implementation may not be claimed.
- The repository keeps its current module layout. The long-term split into `client-core` / `presentation` / `capability` / `host-wechat` / `host-alipay` / `host-telegram` / `testing` is a plan, not a fact, and applies only when a module boundary is justified by public API, dependency lifecycle, independent testing or release, or a distinct change rate. Registering these boundaries in documentation now is what makes that split mechanical later rather than a redesign.
