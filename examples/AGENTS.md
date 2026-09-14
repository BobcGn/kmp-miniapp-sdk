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
