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

## Why publication protection differs from ordinary CI

[ci.yml](workflows/ci.yml) can repeatedly validate working trees; [release.yml](workflows/release.yml) faces immutable coordinates, signing and partially successful external operations. Treating network failure as unpublished or substituting a branch for a pinned tag could publish wrong versions or duplicate submissions.

Maintainer publication consequently needs identity checks, staged visibility and explicit recovery conditions. Isolated-response tests cover guards rather than uploading artifacts to test refusal paths. RELEASING/actual shell guards establish the rationale, not one green CI result.

## Publication safety and evidence

Publication requires signing and GitHub Repository Secrets. Credentials come only from authorized environments, never source, docs, logs or conversations. Visibility checks distinguish 200, 404, partial availability and network failures; runtime visibility precedes plugin publication. Recovery does not re-upload immutable runtime artifacts.

POM visibility, accepted upload, an independent online consumer compiling and WeChat execution are distinct conclusions. Current-branch green checks cannot bypass historical-test blockers in pinned release source. [RELEASING](../docs/RELEASING-en.md) owns recovery conditions.

## Verification and limits

`python3 .github/scripts/test-release-workflow.py` executes actual shell guards with isolated git/curl responses, without publication or secrets. Workflow files and [[module-repository-build]] own Gradle/Node entries; only executed results may be reported passing.

Specs authorize neither publication nor broader CI events. Automation establishes no device, merchant or backend success.
