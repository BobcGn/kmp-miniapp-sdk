---
translation-of: module-fixtures
language: en
status: draft
title: fixtures · independent consumer verification
---

# Independent consumer verification

[中文](SPEC-ch.md)

## Responsibility and modules

Keep reviewable, repeatable external-consumer shapes, not extra SDK root-build modules. [[module-fixture-consumer]] checks single-project source consumption; [[module-fixture-root]] checks root logic selection/host ownership; [[module-fixture-published]] checks public dependencies/source archives.

The first two composite-resolve the working-tree plugin/runtime; the third deliberately rejects local substitution. Their evidence is not interchangeable: published archives, module wiring and WeChat execution answer separately.

## Why independent consumers supplement repository-internal tests

Tests always inside SDK builds/internal classpaths could bypass user-facing plugin resolution, public coordinates and source-set wiring. Fixtures isolate single-project flows, root ownership and published-archive identity so internal paths cannot hide different failure classes. They are not three product templates.

Source composites validate current changes but not published binary/source archives; released consumers cannot replace current-plugin regressions. Settings resolution, verifyPublishedSdkSources refusal conditions and TESTING records support the separation.

## Dependency and evidence boundary

Fixtures consume public APIs/plugins, not SDK internal packages. Hosts own WXML/WXSS/thin JS; generated libraries are not handwritten source. Test-account data, private configuration and credentials stay untracked.

Root `verifyMiniAppConsumerFixture` aggregates only explicitly listed single-project scenarios, not every fixture/demo. Child specs own their verification entries. Node smoke never replaces real-host execution; historical acceptance retains its environment.
