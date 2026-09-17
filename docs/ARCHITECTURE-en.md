# Architecture

[中文](ARCHITECTURE-ch.md)

This document records stable architectural boundaries. It does not imply that every described integration has been implemented. See [PROJECT_FACTS-en.md](PROJECT_FACTS-en.md) for current capabilities.

## 1. System Boundary

```text
commonMain
    ↓
jsMain/export facade
    ↓
Kotlin/JS artifact
    ↓
thin CommonJS export normalization
    ↓
TypeScript / JavaScript
    ↓
WeChat Mini Program runtime
```

### What the SDK is

A Kotlin Multiplatform client runtime for sharing client behaviour, host capabilities, and presentation state across Mini App platforms, while keeping rendering host-native.

The SDK shares client behaviour and host capabilities today, and presentation state from P2. It never renders. [ADR 0011](decisions/0011-presentation-state-shared-rendering-host-native-en.md) records that boundary and the reasoning behind it.

### The four layers

| Layer | Owns | Must not |
| --- | --- | --- |
| **Backend** | Authentication and server sessions, token lifetime, order and payment final state, inventory, reservations, business rules, server-side authorisation | Be reimplemented on the client |
| **KMP Client Runtime** | Domain model, use cases, repository contracts, error model, async abstraction, host-capability contracts, cache policy, and from P2 `UiState` / `Action` / `Store` / `Effect` / presentation logic | Depend on Compose, any UI framework, or host markup; copy server-authoritative rules; treat its own cache as truth |
| **Host Integration** | WeChat adapters and error mapping, runtime and capability detection, permission, privacy, lifecycle, navigation, JS binding, presentation binding | Define a view, a layout, a view tree, or a renderer |
| **Platform UI** | The final view and layout: WXML and WXSS for the WeChat host, Compose for Android / iOS / desktop clients, AXML and ACSS for a future Alipay host, HTML for a future Telegram host | Live inside the SDK |

Sharing has four levels. Client infrastructure and host-capability abstraction are shared now; presentation state and behaviour are shared from P2; rendering is never shared. **There is no Presentation Core today** — `UiState`, `Action`, `Store` and `Effect` have a defined owner and no implementation, and no placeholder module or type exists for them.

### Responsibility matrix

| Concern | Backend | KMP Client Runtime | Host Integration | Platform UI |
| --- | --- | --- | --- | --- |
| Auth | Verifies the login credential; issues and owns the trusted session and token | Orchestrates host login → backend exchange; holds no trusted identity | `wx.login` credential, `wx.checkSession` state | Sign-in affordances |
| Payment | Creates the order, produces the signed parameters, confirms the final state | Forwards backend-produced parameters; exposes processing, interruption and error state | `wx.requestPayment` invocation only | Payment affordances |
| Network | Serves the API and owns the business response | Transport contract, retry and cancellation policy, error mapping | `wx.request`, upload, download, network status | — |
| Storage | — | Cache policy: what may be cached and for how long | `wx.getStorage`, the file-system sandbox | — |
| Business rule | Authoritative | Presentation-level validation only; never a second source of truth | — | Input affordances |
| UiState | — | Owning layer from P2; **not implemented** | State → `setData` binding, from P3 | — |
| Navigation | — | Navigation intent and effect as presentation behaviour, from P2 | `wx.navigateTo`, `wx.redirectTo`, `wx.navigateBack` | Page stack and transitions |
| Permission | — | Host-neutral permission contract and policy | `wx.getSetting`, `wx.authorize`, `wx.openSetting` | The rationale shown to the user |
| Rendering | — | Never | Binding only, never a renderer | WXML and WXSS for WeChat; Compose for Android / iOS / desktop |
| Cache | The authoritative store | Last-known client state; never truth | Host storage primitives | — |
| Error | Defines business errors | Host-neutral error model and mapping | Host failure text → SDK error | Error presentation |
| Lifecycle | — | App-level lifecycle capability | `App.onLaunch` / `onShow` / `onHide`, page lifecycle | Visual state transitions |

Two consequences follow from the matrix rather than from preference:

- A host success callback resolves an interaction; it never establishes a business fact. An order is paid because the backend says so, a user is authenticated because the backend issued the session.
- No row has the same owner twice in the columns that could disagree. Where the backend and the client could both hold an answer — payment state, authentication, inventory — the client's entry is explicitly the non-authoritative one.

## 2. Dependency Direction

Platform-neutral interfaces, models, errors, and shared logic belong in `commonMain`. Platform implementations belong in `jsMain` and depend on the neutral contracts.

```text
jsMain platform implementation
            ↓ depends on
commonMain interfaces and models
```

`commonMain` must never depend on WeChat implementation details. WeChat-specific code must not leak upward into the shared layer.

## 3. Host Boundary

The WeChat Mini Program is the host runtime. The SDK does not own:

- WXML or WXSS rendering
- Page rendering
- Component trees
- Application UI

The SDK is limited to shared logic, runtime integration, and a typed platform bridge.

`MiniAppHost` is the platform adapter boundary. Shared code identifies semantic capabilities with `CapabilityKey` and observes their current `CapabilitySupport`; it does not select a host with a platform enum. A new host is added as a new implementation rather than as branches in a central `when` statement.

Concrete common capabilities are exposed through small capability-provider facets. The first example is `StorageCapabilityProvider`, which lets `WechatHost` provide `MiniAppStorage` without adding a generic service locator or bypassing the host boundary.

WeChat client login intentionally does not become a common Auth capability. `WechatHost.platform` exposes the WeChat-specific authentication adapter through `WechatPlatformApi`, preserving the meaning of `wx.login` without inventing a misleading cross-host `login(): String` contract.

WeChat is Host Adapter #1. Its platform-specific code is isolated under `jsMain/.../host/wechat`. No Alipay or Telegram implementation currently exists.

### Capability and platform escape hatch

Only semantics that are genuinely common across hosts should become common capabilities. The SDK does not hide the underlying platform or force unrelated APIs into a lowest-common-denominator abstraction. `MiniAppHost.platform` provides a typed `HostPlatformApi` escape hatch for platform-specific APIs; concrete platform APIs will be introduced only with real consumers.

Capability support has four states: `Supported`, `Unsupported`, `VersionDependent`, and `PermissionDependent`. The last two describe what the host would need in order to help, and both mean the capability is not usable now. A host answers from its own runtime rather than from a list, so one SDK build can answer differently on two hosts. Version and platform reads are deferred until each is first needed and cached independently because the export facade builds the host while the module is still loading; `wx.canIUse` remains a live query.

Version comparison is platform-neutral logic and lives in `commonMain` as `HostVersion`. Which base library provides a capability is host data, so it lives in the WeChat catalog, and an entry may leave its minimum version unset and rely on `wx.canIUse` instead — a live answer for the host actually running rather than a figure copied from a table. What the WeChat runtime reports about itself, including the base-library version, is host-specific and reachable only through the escape hatch.

`requireSupported` turns any state other than `Supported` into `MiniAppException.UnsupportedCapability`, and is the only path that produces it. A host that cannot be probed at all fails closed: a capability the SDK cannot confirm is reported `Unsupported` rather than assumed present.

### Runtime families

Mini-app hosts may belong to different runtime families. WeChat-like DSL runtimes and WebView/web runtimes do not share universal assumptions about `window`, DOM, CommonJS, or WXML. CommonJS is the current WeChat distribution strategy, not the permanent ABI of every future host.

### Lifecycle and navigation

App-level lifecycle is a capability. Every mini-app host can say whether the application is in front of the user, so `MiniAppLifecycle` exposes the current state and a stream of changes, and the host adapter republishes the hooks its runtime hands the consumer.

Page-level lifecycle and page-stack navigation are not capabilities. A page, a page route, and a page stack belong to a DSL mini-program runtime, and a WebView host has none of them. `WechatPageLifecycle` and `WechatNavigation` therefore live under `host/wechat` and are reachable only through the platform escape hatch. Describing them as neutral contracts would present WeChat semantics as universal ones.

Lifecycle is forwarded rather than intercepted: WeChat reports lifecycle only to the `App(...)` and `Page(...)` registrations the consumer owns, so the SDK exposes entry points the consumer forwards its hooks into.

### Permission

Permission is a capability because every host that gates device access on the user's decision has the same three answers. `MiniAppPermissions` reports `NotRequested`, `Granted`, or `Denied` for a host-neutral `PermissionKey`, and never keeps an answer: the user can change a permission in the host's own settings at any time, so a remembered state would go stale without notice.

A refusal is `MiniAppException.PermissionDenied` and a host that cannot answer is `HostFailure`. They are not the same instruction to a consumer — one says ask the user to visit settings, the other says try again. Requesting and opening settings need a user gesture, so the SDK never prompts on its own, and a settings visit reports the state the host gives afterwards rather than assuming a grant.

`PermissionKey` names what a permission is for. The `scope.*` string a host uses for it exists only inside the WeChat adapter's mapping, and a key that adapter does not map fails before any host call instead of being forwarded.

`PermissionState` and `CapabilitySupport.PermissionDependent` answer different questions: the first is a permission's own state, the second says a capability is blocked by one. The permission capability reports `Supported` from its host APIs and is never itself permission-dependent.

Permission is not privacy. WeChat's privacy authorization is a separate lifecycle with its own APIs, its own capability, and its own state; sharing either model with the other would misreport one as the other.

### Privacy

Privacy is a capability because every host that collects personal data on the user's behalf has some form of consent condition, and WeChat states it explicitly. `MiniAppPrivacy` reports what the host currently requires for its own privacy contract, asks the host to obtain the user's acceptance, and exposes the precondition point a gated capability calls. It shares no state with `MiniAppPermissions`: the two are prompted, stored, and cleared separately by the host.

Three things are modelled separately, because the host reports them separately: the queryable requirement, the result of one attempt, and SDK errors. A requirement of `NOT_REQUIRED` is a statement about the host, not proof that the user agreed, because WeChat also reports it when the mini program declares no collection at all.

A refusal is an outcome rather than an error. Declining and dismissing enter the host failure callback, but only a recognizable refusal message maps to `Refused`; every unknown failure conservatively remains a `HostFailure`. The SDK does not invent a decline/cancel distinction the host does not provide. A failed privacy precondition is `MiniAppException.PrivacyAuthorizationRequired`, never a `PermissionDenied`, and a host without the privacy APIs reports `UnsupportedCapability`.

The precondition point queries the host and never shows anything: only a consumer can prompt, and only from a user gesture.

### Device capabilities

A device capability reaches the user through the platform escape hatch, not through a common capability, unless its semantics are genuinely shared across hosts. The clipboard and the two vibration lengths follow that rule: `WechatClipboard` and `WechatHaptics` live under `host/wechat`, and the capability catalogue gates them individually under namespaced keys, because a host may expose one clipboard direction or one vibration length without the other.

A host object is not `wx`: `wx.canIUse` answers for `wx` APIs and some component objects, but not for every object the SDK reaches. A capability entry may therefore carry its own presence probe, which the gate runs in addition to the version and schema checks. The file manager is the first such case, and it is why a capability is reported supported only when the method itself is there rather than when its manager is.

The file system reaches the user the same way: `WechatFileSystem` lives under `host/wechat`, works only on UTF-8 text inside the mini program sandbox, and is gated per operation. It does not stand in for a portable file API, and it adds no directory, stream, descriptor, or traversal operation that WeChat does not offer.

A capability whose request model is itself host-specific stays behind the escape hatch even when its idea is broad. Location is the clear case: reading a position is something every host can do, but WeChat makes the caller choose between `wgs84` and `gcj02`, and that choice is a local mapping concept no portable contract should carry. The whole capability therefore lives under `host/wechat`, and a future WebView host would implement its own rather than claim to satisfy WeChat's.

A capability may also have a precondition the SDK can enforce rather than merely document. The location adapter first queries both the host privacy contract and the location permission, and fails before `getLocation` when either is unsatisfied. Both checks are queries that present nothing; accepting privacy and requesting permission remain separate consumer actions that require explicit user gestures, so a location read can never become an implicit prompt.

The host's own interface can also be the capability itself. Scanning is that case: `WechatScanCode` lives under `host/wechat` and owns only the request the caller describes and the answer the host gives — it does not implement the interface, model a camera, or parse what it receives. When a host expresses the user's decision only through its failure callback and publishes no structured field for it, the SDK classifies on host text in exactly one place and only by exact match: anything unrecognized stays a failure, because reporting a real fault as the user's choice would tell a caller to stop retrying a call that never opened an interface. The same care applies to how a capability expresses its answer: the categories a request may ask for and the formats the host reports back are two different vocabularies, and the SDK does not conflate them.

Media selection is the second such case, and it adds one boundary of its own: what the host returns are temporary resources. The SDK reports the paths it was given and keeps no copy, so it makes no claim about how long they live and copies nothing on the caller's behalf; a caller that needs the media later owns that decision. The request model follows the same rule as every other capability — the SDK validates what WeChat states exactly, such as the required media type and the documented duration range, and leaves what depends on the running host, such as how many files it will accept, to the host.

Subscription requests add a different kind of boundary: one where the host's own vocabulary could not be established. The SDK names the statuses it can evidence and preserves every other one verbatim rather than mapping it onto a status it did not observe, which is the same shape the scan-format and media-kind vocabularies take — the difference here is only how little could be named. That is a deliberate consequence of the rule that the SDK does not encode facts it cannot cite: an unverified vocabulary produces a narrow recognized set and a wide preserved one, never a guess.

A capability can also require something of the caller rather than of the host. A subscription request is only valid after a user gesture, and the SDK cannot manufacture one, so it never triggers the request itself and never retries a refusal that followed none; the requirement is documented on the capability and enforced by leaving the trigger entirely to the caller.

A capability's *concept* can be common while its *vocabulary* is not. Network status is the case: every host can say whether it is online and over what kind of link, so the question and the model are common, but the words are the host's. The answer therefore carries both — a name this SDK recognizes and the host's own word beside it — and a link kind the SDK has never seen is reported rather than folded into an `unknown` member that would claim more knowledge than the SDK has. The same split applies to gating: a host may answer the question without offering change events, so the query and the listener are separate keys rather than one capability that is either present or absent.

An event-shaped capability has to decide who owns the host registration. Lifecycle avoids the question because the consumer forwards the hooks it owns. Where the SDK registers a listener itself, this SDK registers one per collector and removes it when that collector ends: the host removes listeners by identity, so sharing one registration between collectors would need reference counting, and a mistake there leaks a listener for the lifetime of the mini program. One registration per collector costs a listener per active collector and makes the pairing exact on every termination path.

A capability whose request and result are built on a host *file* stays behind the escape hatch even when the surrounding operation is portable. An upload and a download are HTTP exchanges, which the common transport capability already covers; what they add is a path in the host's own file system, and this SDK deliberately has no portable file reference, so promoting them would mean inventing one. Their task handle is modelled where it is useful: a consumer can stop the transfer, and stopping the wait stops it too, because a caller that stopped waiting for a transfer did not ask for it to keep running. Advisory progress is reported as the host's last figure and never as an estimate.

A device call resolving means the host accepted and performed it. It never means more than that: a vibration in particular can only be confirmed by someone holding the device, so nothing in the SDK reports one as having been felt.

Payment is the extreme case of that rule. The SDK's part is a typed forwarder: it carries the five parameters a trusted backend produced to WeChat's own interface and reports the one thing the host said — that the payment interaction completed — instead of anything about an order. That is architectural rather than stylistic. The authoritative order state lives in a backend that holds the merchant key and receives WeChat Pay's own notification, so a client able to decide an order was paid would be a second source of truth for money; this SDK has no signature, no key, and no order model for that reason. Naming a result `paid` or `confirmed` later would change what the SDK claims rather than add a field, which is why the exported type carries `interactionCompleted` and nothing else.

### Planned virtual-payment boundary (not implemented)

Virtual payment is represented by a different WeChat API name from standard payment. This project therefore plans it as a separate capability rather than a mode of the existing one; its eligibility and backend workflow remain unverified. Nothing in this section is implemented, registered in the capability catalog, or exported; the section exists so that a later implementation does not begin by widening the standard-payment model, and so that a reader can tell a boundary decision from a roadmap wish.

```text
Standard Payment
  -> physical/general commerce boundary
  -> wx.requestPayment
  -> independent request/outcome model

Virtual Payment
  -> virtual goods/services boundary
  -> wx.requestVirtualPayment
  -> independent eligibility, request, outcome and backend workflow
```

The separation is a rule, not a preference:

- **Separate capability key.** Standard payment is `wechat.request-payment`. Virtual payment needs its own key — `wechat.request-virtual-payment` is the candidate, to be settled against the existing naming rules when it is implemented — because one key answering for both would make `Supported` ambiguous.
- **Separate request and outcome models.** No virtual-payment field enters `WeChatPaymentRequest`, and no model carries both sets with nullable halves: such a DTO would make every consumer ask which half it holds, and would let a virtual-payment value reach `wx.requestPayment`. Reusing `JsPaymentOutcome` is not planned either — the standard-payment outcome is tied to one specific host call, and nothing available offline establishes that the virtual-payment success callback means the same thing. A similar name is not proof of the same meaning.
- **No fallback between them.** A failed or unavailable standard payment is never retried as a virtual payment, or the reverse. Their distinct API names are enough for this project to keep the boundaries separate; neither the unavailable official contract nor this repository is evidence that they are interchangeable.
- **No client-side credentials.** As with standard payment, the SDK would compute no signature and hold no key, session, payment token or order. If the official contract requires a client-visible credential, it is a value a trusted backend produced and this SDK forwards, never one this SDK generates.
- **API presence does not establish eligibility.** Until the official eligibility rules are retrieved, a future implementation must not equate "the API exists" with "this account may use it". Availability must be evidenced separately for every platform and account scope the SDK claims; a result on Android says nothing about iOS, HarmonyOS, or the Developer Tools simulator.

Planned shape, to be confirmed against the official contract before any of it is written:

- `VirtualPaymentRequest` — only the fields the official parameter table proves, each required one non-blank; no nullable placeholder for an unknown, and no field shared with the standard-payment request.
- `VirtualPaymentOutcome` — naming only what the host's success callback actually establishes, which offline evidence cannot yet state. As with standard payment, the final transaction state would belong to a trusted backend, and no result would be named `paid`, `settled` or `confirmed`.
- `VirtualPaymentFailure` semantics — reuse of the existing error model where it fits (the same `HostFailure`, `HostInteractionInterrupted` and `UnsupportedCapability` distinctions) rather than a parallel hierarchy for one capability; whether an ended interaction has a stable official signal is unknown, so no cancellation classification is planned from a guess.
- Sensitive values — anything the official contract marks as a signature, token, session or order credential is neither logged, displayed nor persisted, exactly as the standard-payment request already requires.

Open items, all recorded as unknown rather than guessed: the parameter table and result shape, the minimum base library version, whether the surface is a mini program or a mini game, per-platform availability, the account and category eligibility rules, whether any permission or backend interface permission applies, and whether the success callback carries any transaction fact at all. These are the questions a future implementation must answer from official sources first; until then the capability stays `Planned` in the [capability matrix](platforms/wechat/WECHAT_CAPABILITIES-en.md) with no code behind it.

## 4. JS Interop Boundary

JavaScript-specific constructs such as `external`, `dynamic`, and `js()` may exist only in `jsMain`. Raw platform contracts are restricted to `jsMain/.../host/wechat/interop`.

Business and shared layers must not manipulate `dynamic` values directly. Conversion to typed Kotlin values belongs at the adapter boundary.

The WeChat interop layer models the global `wx` object, option bags, result shapes, and callbacks only. Its plain-object factories exist solely because external interfaces have no Kotlin constructors; they do not invoke host APIs or contain error mapping, capability policy, or business logic.

`host/wechat` is the platform-specific namespace beneath the host boundary. This namespace preserves room for future hosts without introducing additional host implementations or Gradle modules now.

## 5. Async Boundary

The architectural target for asynchronous platform APIs is:

```text
wx callback
    ↓
interop
    ↓
adapter
    ↓
coroutine-friendly Kotlin API
```

`MiniAppException` is the platform-neutral semantic error boundary. Raw host failures stay in interop and host adapter code; adapters preserve useful scalar diagnostics while mapping them to SDK errors. Raw JavaScript objects never enter the common error model. `UserCancelled` is used only when the host identifies user intent; when the host ends an interaction without distinguishing dismissal, permission restriction, or another cause, the adapter uses `HostInteractionInterrupted`.

The internal `awaitHostCallback` primitive implements the callback-to-coroutine mechanics without implementing a capability. It accepts one success or failure terminal result and ignores every later callback, including callbacks received after cancellation.

Coroutine cancellation and host cancellation are distinct:

- Without an abort hook, cancellation stops result consumption but the host operation may continue.
- With a real abort hook, cancellation invokes that hook at most once.

Promise and event adaptation remain future, use-case-driven work, except for the one event-shaped capability that has a use case: app lifecycle state. `MiniAppLifecycle` publishes the current state and a `Flow` of later changes, which is the only stream abstraction the SDK introduces.

The first concrete capability follows this path:

```text
MiniAppStorage
    ↓ provided by
WechatHost
    ↓
WechatStorage adapter
    ↓
awaitHostCallback + WeChat error mapping
    ↓
typed storage interop
    ↓
wx.getStorage / wx.setStorage / wx.removeStorage
```

The common contract intentionally stores strings only. Object serialization, secure storage, cloud storage, and host-specific options are outside this capability.

The WeChat-specific authentication bootstrap uses a separate path:

```text
WeChatLoginResult
    ↑ returned by
WechatAuth adapter
    ↓
awaitHostCallback + WeChat error mapping
    ↓
typed login interop
    ↓
wx.login
```

WeChat also exposes `wx.checkSession`, which reports whether the client login state WeChat issued is still within the lifetime WeChat defines. It is modelled as a WeChat-specific result rather than a common authentication capability, because no other host has been shown to share those semantics and a generic `isAuthenticated()` would promise a guarantee the API cannot give. It is a query: an invalid answer acquires no code, exchanges no session, and refreshes no token, and a consumer decides for itself whether to call the existing login flow again.

A valid session check is not identity. It does not mean a user is authenticated, that a consumer backend session is valid, that a `session_key` is still accepted by a consumer backend, or that an access token is valid. Only the login-code path through the consumer's backend establishes a trusted identity, exactly as it did before this capability existed.

The login code is a short-lived client credential, not a trusted user identity, SDK session, or access token. Exchanging it with WeChat and establishing a trusted application session are consumer-backend responsibilities. The SDK does not log, persist, exchange, or refresh the code.

The first common capability that sends data to the host follows the shared path:

```text
MiniAppHttpTransport
    ↓ provided by
WechatHost
    ↓
WechatNetwork adapter
    ↓
awaitHostCallback + WeChat error mapping
    ↓
typed request interop
    ↓
wx.request
```

An exchange that completes is a response for every status, including `4xx` and `5xx`; an HTTP status describes the exchange rather than the SDK's ability to perform it. Only a transport-level failure becomes an exception: a host timeout maps to `MiniAppException.Timeout`, and every other transport failure maps to `MiniAppException.HostFailure`. `wx.request` returns an abort handle, so this is the first capability where coroutine cancellation also aborts the host operation instead of merely stopping consumption of its callbacks.

The contract carries text only. The adapter therefore requests raw response text and reports a non-text body as `MiniAppException.InvalidResponse` rather than letting the host hand back a parsed object the contract cannot represent. Response headers cross as single string values; a host header reported in any other shape is not represented. Payload serialization, cookies, redirects, streaming, and retry policy remain outside the capability.

## 6. Public Boundary

The project distinguishes two API surfaces:

- Kotlin public API: Kotlin-idiomatic interfaces, models, and behavior used by Kotlin code.
- JavaScript / TypeScript export API: a consumer-facing boundary designed for JavaScript and TypeScript ergonomics and interoperability constraints.

The export surface may adapt the Kotlin API; it must not become the business implementation layer.

The host-neutral export facade belongs under `jsMain/.../export` and may call shared Kotlin APIs from `commonMain`. It must not contain `wx` calls or platform capability implementations. When the compiler-generated CommonJS namespace is unsuitable as the stable consumer ABI, a thin JavaScript wrapper may normalize module and export shape only; it must not contain business logic, host error mapping, or platform behavior.

Kotlin collections cannot cross the `@JsExport` boundary, because a JavaScript caller has no way to construct a Kotlin map. An export that needs key-value data therefore carries it in a primitively representable shape and lets the wrapper restore an object shape; the Kotlin API keeps its idiomatic collection types. The HTTP transport export follows this rule for headers, representing them as a flat sequence of alternating name and value strings.

## 7. Gradle Build Integration Boundary

The Gradle Plugin owns platform and build integration; the Runtime SDK keeps owning APIs and Host capabilities. The plugin is a Kotlin/JS project integrator, not a second platform: it gives the consumer `miniappMain` and `miniappTest` by creating one Kotlin/JS target named after the platform, so the Kotlin Gradle Plugin — not the plugin — derives those source sets, the compilations that own them, and their `commonMain` / `commonTest` edges. [ADR 0010](decisions/0010-miniapp-gradle-plugin-source-set-model-en.md) records that decision and why the alternatives were rejected.

```text
miniappMain (consumer sources)      miniappTest (consumer tests)
      ↓ derived by the Kotlin Gradle Plugin from one target named `miniapp`
Kotlin/JS compilation
      ↓ configured behind the boundary
current WeChat Host distribution (CommonJS library, TypeScript declarations)
```

Two rules follow from that shape:

- **The plugin chooses the platform's build shape; the Host chooses its distribution shape.** CommonJS output, library binaries, and TypeScript declaration generation describe how the current WeChat Host consumes the artifact. They belong to the Host configuration rather than to the platform identity, so a later Host may change them without renaming a source set.
- **Mini App is the platform and WeChat is the current Host.** Nothing in the build-integration boundary may encode the two as the same thing.

The plugin hides build wiring, never meaning. It does not hand-build compilations or source sets, does not attach a source set to a compilation through an unsupported API, and does not present an artifact-assembly task as a consumer contract. `buildMiniAppSdk`, which distributes this repository's own SDK into the WeChat example, is foundation evidence for the assembly step rather than the consumer workflow.

The `:miniapp-gradle-plugin` module is this plugin. It registers the `miniapp` target together with that target's Node.js test run, adds the runtime SDK to `miniappMain`, and rejects a project that does not apply the Kotlin Multiplatform plugin. Those registrations are the whole of its provisioning: `miniappMain`, `miniappTest`, the compilations that own them and their `commonMain` / `commonTest` edges all come from the Kotlin Gradle Plugin, and the plugin never hand-builds a source set or a hierarchy edge, because hand-built ones are the unused-source-set model [ADR 0010](decisions/0010-miniapp-gradle-plugin-source-set-model-en.md) rejected.

The runtime reaches the consumer as a **public module coordinate** — `io.github.bobcgn:kmp-miniapp-sdk` — not as a project path. A consumer build has no access to this repository's structure, so the plugin cannot and must not name `project(":kmp-miniapp-sdk")`; the same coordinate resolves from a composite build today and from a repository after publication. It is added to `miniappMain` alone, so a project that also targets Android, iOS or the JVM does not acquire a Mini App runtime on those platforms, and `miniappTest` inherits it through the source-set hierarchy rather than declaring it again.

WeChat artifact assembly and the host Gradle DSL remain later work.

A consumer whose `commonMain` depends on another Kotlin Multiplatform project must apply this plugin to that project as well. The dependency resolves through a Mini App variant, so a project that offers none fails variant resolution; there is no fallback, and moving the dependency out of `commonMain` is not a workaround.

## 8. Non-goals

- Reimplementing Kuikly
- Building a Compose Mini Program renderer
- Replacing WXML
- Building a Virtual DOM
- Mirroring the complete `wx` API
- Selecting hosts through a central platform enum
- Hiding platform-specific APIs behind misleading common abstractions
