---
translation-of: submodule-plugin-tests
language: en
status: draft
title: plugin tests · contracts, TestKit and declaration checks
---

# Plugin contracts, TestKit and declaration checks

[中文](SPEC-ch.md)

## Responsibility and layers

Contract/unit suites check descriptors, public coordinates, typed extension, dependency classification and pure path/template/verifier rules. TestKit checks real KGP models, compilation, test reports, distributions, configuration cache and root ownership in temporary consumers. Model/task presence is not execution.

`js/verify-consumer-declarations.cjs` uses actual consumer TypeScript packages, configuration and generated declarations to check calls and definition identity. It installs no second dependency set and does not equate language-service output with IDE-click acceptance.

## Dependencies and isolation

Fixtures share the buildscript classpath between KGP and the tested plugin; runtime resolves through public coordinates and test-side composite builds. Production knows no repository path. Temporary projects read no developer-private WeChat configuration or publishing credentials.

Tests assert diagnostics, source preservation, clean behaviour and wiring after relocation, not task names alone. [[module-fixtures]] owns persistent consumers; real-host results remain separate.

## Verification and limits

```shell
./gradlew :miniapp-gradle-plugin:test
./gradlew verifyMiniAppGradlePluginIntegration
```

The first includes contract/TestKit; the second adds SDK guards and persistent fixtures. Neither certifies simulators, devices, backends or all IDEs. DEVELOPMENT records exact manual navigation scope. Historical test configuration does not automatically expand plugin support.
