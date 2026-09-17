# Example Host Agent Rules

These rules apply to `examples/**` and extend the repository root `AGENTS.md`.

Examples are integration hosts, not SDK source. Their purpose is to prove that generated SDK artifacts run in WeChat Developer Tools and, when applicable, a real-device WeChat Mini Program runtime.

An example may contain:

- WXML
- WXSS
- TypeScript or JavaScript
- WeChat-specific project configuration

Do not copy SDK implementation logic into an example. If an integration host requires a workaround for an SDK defect, label the workaround clearly and link it to the defect or explanation.

A passing Node.js test does not establish successful WeChat Mini Program integration. Report these results separately.

## UI and Rendering

An example is Host UI. WXML, WXSS and thin TypeScript are expected here, and this is the correct place for them — they must not be pushed down into the SDK.

- The example consumes SDK artifacts. It must not render on the SDK's behalf, and it must not require the SDK to grow a renderer, virtual DOM, view tree, layout engine, Kotlin UI DSL or WXML generator.
- Binding host state into the page and turning host events into SDK calls is the example's job. Defining how the SDK behaves is not.
- The backend owns business truth. An example must not treat a host success callback as the authoritative answer for authentication, payment, inventory, or any other business rule.
