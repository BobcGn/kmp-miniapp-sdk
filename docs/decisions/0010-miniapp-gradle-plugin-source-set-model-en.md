# ADR 0010: Mini App Gradle Plugin Source-Set Model

[中文](0010-miniapp-gradle-plugin-source-set-model-ch.md)

- Status: Accepted
- Date: 2026-09-17

## Context

The P1 consumer-integration track requires an ordinary Kotlin Multiplatform project to obtain stable `miniappMain` and `miniappTest` source sets by applying `io.github.bobcgn.miniapp`, without knowing anything about the SDK's internal Kotlin/JS target, CommonJS output, TypeScript declarations, or artifact copying.

Before any plugin code exists, BOB-83 required a minimal executable PoC to choose between three Kotlin Gradle Plugin models on Kotlin 2.4.20 and Gradle 9.3.1:

- **Model A** — a named Kotlin/JS target declared in the consumer build script.
- **Model B** — custom source sets only, with no target or compilation behind them.
- **Model C** — the plugin owns a Kotlin/JS compilation and exposes `miniappMain` / `miniappTest`.

The PoC lives in [`poc/kgp-model`](../../poc/kgp-model) and is a standalone Gradle build that the SDK build does not include. Its four modules and its two verification tasks (`reportKgpModel`, `checkMiniAppModel`) are the reproducible evidence for this decision; they are experimental tooling, not production code.

What the PoC established, by execution:

- Naming a Kotlin/JS target makes the Kotlin Gradle Plugin derive real source sets from the target name. Only the target name decides whether the consumer sees `miniappMain` or `jsMain`.
- Custom source sets without a target are reported by the Kotlin Gradle Plugin as `Unused Kotlin Source Sets` and are consumed by no compilation. Their configurations exist and are dead: a consumer could add a dependency to `miniappMainImplementation` and nothing would ever compile it.
- No supported Kotlin Gradle Plugin API attaches an arbitrary source set to an existing compilation. `KotlinCompilation.kotlinSourceSets` and `allKotlinSourceSets` are public but read-only, and the source-set container that a compilation is built from is `internal`. The only public lever is the target name.
- Assigning a Kotlin source set directory to an existing compilation is possible (`KotlinSourceSet.kotlin.srcDir`), but it does not create a source set named `miniappMain`, so it cannot satisfy the naming requirement.

## Decision

**The plugin creates one Kotlin/JS target named after the platform, and lets the Kotlin Gradle Plugin derive the consumer-facing source sets from that name.**

- The plugin applies `kotlin.js("miniapp")`. Kotlin 2.4.20 then creates `KotlinSourceSet`s named `miniappMain` and `miniappTest`, each owned by a real compilation.
- The plugin configures that target's Kotlin/JS output as an implementation detail of the current WeChat host. The consumer writes none of `js { }`, `nodejs()`, `useCommonJs()`, `binaries.library()`, `generateTypeScriptDefinitions()`, or `sourceSets.create(...)`.
- `commonMain → miniappMain` and `commonTest → miniappTest` are produced by the Kotlin Gradle Plugin, not by plugin code. In the PoC they hold in the named-target model and are absent in the source-set-only model.
- The plugin owns the target name. Changing it later would rename the consumer's source sets, so the target name is part of the plugin's compatibility contract even though it is not the friendliest name in the model.

### Stable consumer surface

Everything a consumer may depend on is derived from the platform name `miniapp`:

- Source sets: `miniappMain`, `miniappTest`, beside `commonMain` and `commonTest`.
- Lifecycle task: `miniappTest`.
- Execution task: `miniappNodeTest`, which runs the `miniappTest` compilation's tests on Node.js.
- Compilation tasks: `compileKotlinMiniapp` for `miniappMain` and `compileTestKotlinMiniapp` for `miniappTest`.
- Dependency configurations: `miniappMainApi`, `miniappMainImplementation`, `miniappMainCompileOnly`, `miniappMainRuntimeOnly` and the corresponding `miniappTest*` set.
- Produced artifact: `miniappMainClasses`, `miniappJar`, `miniappSourcesJar`, and the target's publication elements (`miniappApiElements`, `miniappRuntimeElements`).

### Internal implementation detail

- The compilation names stay `main` and `test`. The consumer never sees a `miniapp`-qualified compilation name, and the plugin should not invent one.
- The Kotlin/JS artifact pipeline (`miniappNodeProductionLibraryDistribution`, `miniappNodeDevelopmentLibraryDistribution`, `miniappPublicPackageJson`, `miniappProductionLibraryValidateGeneratedByCompilerTypeScript`) is the plugin's assembly concern for BOB-81, not a consumer contract.
- The Kotlin/JS target's compiler settings (`useCommonJs`, library binaries, TypeScript declaration generation) are host-distribution detail, not platform semantics. A future host may need different ones, so the plugin must keep them behind the host boundary rather than treating them as properties of a Mini App.
- `miniappNodeTest` naming does reveal that the local test runner is Node.js. That is accepted: Node is the local Kotlin/JS test environment, not the deployment host, and no better public name is available without the source-set attachment API that does not exist.

## Rejected models

- **Model A — a named Kotlin/JS target declared in the consumer build script.** Rejected. It produces the same model as the chosen approach, but it requires the consumer to write the Kotlin/JS wiring that BOB-81 says must be hidden, and it passes only 9 of the 10 model checks. The mechanism is right; leaving it in the consumer's build script is not. Its value is as the control experiment that proves the chosen mechanism works without any plugin.
- **Model B — custom source sets only.** Rejected, and it is the wrong model rather than an incomplete one. The Kotlin Gradle Plugin reports `miniappMain` and `miniappTest` as unused source sets, no compilation consumes either, no `miniappTest` task exists, and the only executed test task is `jsNodeTest`. It fails 6 of the 10 model checks. It is the "IDE sees it, Gradle does not compile it" false platform: sources in `src/miniappMain/kotlin` are silently ignored and dependencies in `miniappMainImplementation` are silently dead.
- **Model C, hidden-target variant — the plugin keeps an internal `js` target and still exposes `miniappMain` / `miniappTest`.** Rejected as unachievable with supported API. The module `model-c2` compiles and runs it: the source sets are still reported unused, `jsMain` / `jsTest` are still visible to the consumer, and it fails 5 of the 10 model checks even though the consumer build script itself is clean. Making it work would require the Kotlin Gradle Plugin's internal source-set container, which this project will not depend on.

## Consequences

- `miniappMain` and `miniappTest` are ordinary Kotlin source sets with ordinary compilations behind them, so IntelliJ IDEA recognises them without any plugin-specific IDE support and code completion inside them works through the normal Kotlin Multiplatform mechanism. This still requires manual IDE acceptance; the PoC proves the Gradle model, not the IDE model.
- The consumer's `commonMain` participates in the `miniapp` compilation, so BOB-76's `commonMain → miniappMain` requirement is satisfied by the Kotlin Gradle Plugin rather than by wiring the plugin must maintain.
- The Kotlin/JS feature set is constrained by what the Kotlin Gradle Plugin offers for a JS target. There is no separate Mini App platform type, no separate compiler, and no way to give `miniappMain` a compilation of its own; the plugin is a Kotlin/JS project integrator, and must not pretend otherwise.
- Adding a second JavaScript target to a consumer project is untested by this PoC. Two Kotlin/JS targets in one project are subject to the Kotlin Gradle Plugin's own target disambiguation rules, which is why the plugin must not silently create a target when one is already present.
- Known Kotlin Gradle Plugin behaviour that the plugin inherits and cannot fully hide:
  - The default hierarchy template inserts the grouping source sets `webMain` and `webTest` between `commonMain` / `commonTest` and the target's own source sets. This is not specific to the `miniapp` target: the existing `:sdk` module already has `webMain` and `webTest` beside `jsMain` and `jsTest`. A consumer that wants them gone can set `kotlin.mpp.applyDefaultHierarchyTemplate=false`, which the PoC confirmed removes them and leaves `miniappMain` depending on `commonMain` directly; the plugin cannot set that project-wide property on the consumer's behalf and must not try.
  - The Kotlin/JS build creates `kotlin-js-store/` and a `package.json` at the consumer project's root. The plugin's documentation must expect them rather than treat them as generated clutter.
- The model is compatible with the configuration cache. `:model-c:check` and the PoC's model checks stored and reused a configuration cache entry, and `--warning-mode all` produced no deprecation warning from `useCommonJs()`, `binaries.library()`, or `generateTypeScriptDefinitions()` on Kotlin 2.4.20. A verification task that reads the Kotlin model from `Project` inside its own action is not configuration-cache safe; the PoC computes the model in `gradle.projectsEvaluated` and captures plain values instead, which is the pattern BOB-79's suite should follow.
- Constraints this decision places on the consumer-integration track:
  - **BOB-78** creates the target; it does not create compilations or source sets.
  - **BOB-76** must not hand-build a hierarchy. The `commonMain` and `commonTest` edges come from the Kotlin Gradle Plugin and should be asserted, not constructed.
  - **BOB-82** wires the SDK into `miniappMain`, which is a real compilation source set. It must not wire the SDK into an orphan configuration, which is exactly the failure mode model B produces.
  - **BOB-81** hooks the Kotlin/JS artifact pipeline of the `miniapp` target. Its output task must be named for the platform, and consumers must never call `useCommonJs()` or `generateTypeScriptDefinitions()`.
  - **BOB-84** configures the current WeChat host. The Kotlin/JS output shape belongs to that host configuration, not to the platform identity, so a later host can change it without renaming `miniappMain` / `miniappTest`.
  - **BOB-79** must assert the owning compilation of each source set, not only the presence and `dependsOn` of the names, because the presence assertions pass in the rejected models too.
