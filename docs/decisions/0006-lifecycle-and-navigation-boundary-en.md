# ADR 0006: Lifecycle and Navigation Boundary

[中文](0006-lifecycle-and-navigation-boundary-ch.md)

- Status: Accepted
- Date: 2026-09-14

## Context

The SDK needed runtime lifecycle integration and navigation for WeChat. That raised questions ADR 0003 and ADR 0004 did not settle:

- Which lifecycle concepts are genuinely shared across mini-app hosts, and which are WeChat semantics wearing a neutral name?
- Is navigation a common capability or a host-specific bridge?
- ADR 0004 recorded that promise and event adapters are not generalized until a concrete host integration requires them. Lifecycle state is the first such need.
- WeChat reports lifecycle only to the `App(...)` and `Page(...)` registrations owned by the consumer, so an SDK cannot observe it on its own.

## Decision

- **App-level lifecycle is a common capability.** `MiniAppLifecycle` models whether the application is in front of the user, as `MiniAppLifecycleState.FOREGROUND` or `BACKGROUND`, identified by `CapabilityKey("lifecycle")`. WeChat and Alipay report it through `onShow` and `onHide`; a WebView host reports it through visibility. It is the only lifecycle concept every mini-app host can honestly express.
- **State changes are published as a `Flow`.** The contract exposes the current `state` and a `stateChanges` stream. This is the concrete host integration ADR 0004 awaited. It generalizes state observation only; no promise adapter and no generic event adapter is introduced.
- **Page lifecycle is not a common capability.** A page, a page route, and a page stack are DSL mini-program concepts, and a WebView host has none of them. `WechatPageLifecycle` therefore lives in the WeChat runtime and is reachable only through the escape hatch. It tracks the page the runtime reports as shown; it is not a navigation stack, and the SDK takes no action on page hooks.
- **Navigation is not a common capability.** `navigateTo`, `redirectTo`, and `navigateBack` operate on a page stack, which only a DSL runtime has. `WechatNavigation` therefore lives in the WeChat adapter and is reachable only through `WechatPlatformApi`.
- **Lifecycle is forwarded, not intercepted.** WeChat reports lifecycle only to the registrations the consumer owns, so the SDK exposes entry points the consumer forwards its hooks into instead of pretending to observe the runtime directly.
- **`onLoad` is not a separate entry point.** A page becomes the one the user is on when it is shown, and `onShow` reports that same route, so a separate `onLoad` entry point would add public API with no observable effect. `onLaunch` is still forwarded because it is a legitimate independent entry point for a consumer that forwards only that hook; WeChat always follows it with `onShow`, so both report the foreground state.
- **Navigation failures are host failures.** A bounded page stack, an unknown route, and going back from the first page are reported by WeChat through its failure callback, and map to `MiniAppException.HostFailure` rather than succeeding silently.
- No router framework, navigation stack framework, UI component lifecycle abstraction, Compose navigation, or renderer is introduced.

## Consequences

- Shared Kotlin code can react to foreground and background without importing WeChat declarations.
- Consumers can tell an app-level state the SDK models apart from a page-level state the host defines.
- Lifecycle only works if the consumer forwards its hooks. A consumer that forwards nothing observes the initial background state and nothing else, which is a truthful outcome rather than a silent failure.
- Page route tracking reflects the page the runtime reports as shown. Returning from a deeper page is correct because the unloading page clears its own route before the revealed page reports its own.
- A future WebView host implements `MiniAppLifecycle` from visibility events and is under no obligation to invent pages or a stack.
