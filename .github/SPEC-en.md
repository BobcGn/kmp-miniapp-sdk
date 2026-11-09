---
translation-of: module-ci-release
language: en
status: draft
title: .github · CI and maintainer publication
---

# CI and maintainer publication

[中文](SPEC-ch.md)

## Responsibility and entries

`workflows/ci.yml` invokes root verification for configured PR/push/manual events. Its two jobs cover quick checks/size/example typing and plugin-consumer integration. Independent CI environments establish no mobile-demo or real-WeChat results.

`workflows/release.yml` protects maintainer publication, not consumer-plugin behaviour. It currently pins a historical release tag/version/commit, not a generic publisher of the current branch. Old tags are never moved to include new code.

## Publication safety and evidence

Publication requires signing and GitHub Repository Secrets. Credentials come only from authorized environments, never source, docs, logs or conversations. Visibility checks distinguish 200, 404, partial availability and network failures; runtime visibility precedes plugin publication. Recovery does not re-upload immutable runtime artifacts.

POM visibility, accepted upload, an independent online consumer compiling and WeChat execution are distinct conclusions. Current-branch green checks cannot bypass historical-test blockers in pinned release source. [RELEASING](../docs/RELEASING-en.md) owns recovery conditions.

## Verification and limits

`python3 .github/scripts/test-release-workflow.py` executes actual shell guards with isolated git/curl responses, without publication or secrets. Workflow files and [[module-repository-build]] own Gradle/Node entries; only executed results may be reported passing.

This spec import triggers no publication or broader CI events and equates no automation with device, merchant or backend success.
