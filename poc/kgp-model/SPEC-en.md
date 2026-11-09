---
translation-of: module-kgp-model-poc
language: en
status: draft
title: kgp-model · platform-model comparison experiment
---

# KGP platform-model comparison

[中文](SPEC-ch.md)

## Responsibility and modules

Isolate how Mini App source sets acquire real compilations/test tasks, outside SDK root build and publications. After evaluation the root snapshots targets, source sets, parents/tasks and provides reportKgpModel plus ten checkMiniAppModel assertions.

[[submodule-poc-model-a]] is the manual named-target control; [[submodule-poc-model-b]] reproduces unused source sets. [[submodule-poc-model-c]] is the chosen plugin-owned named target; [[submodule-poc-model-c2]] reproduces hidden-js-target failure. The latter two consume [[module-poc-plugin]]; models do not depend on each other.

## Why counterexamples accompany the successful model

Directories/source-set presence can make wrong models look complete; actual compilation ownership/test execution distinguishes them. Isolated controls/counterexamples expose failures under shared requirements, avoiding confusion between one green task and a valid whole model.

[ADR 0010](../../docs/decisions/0010-miniapp-gradle-plugin-source-set-model-en.md) records selection/rejections; root [build.gradle.kts](build.gradle.kts) supplies current snapshots/checks. These are reviewable evidence, not four product directions. Tasks must not remove requirements simply because counterexamples fail them.

## Boundary and decision

Experiments evidence ADR 0010 without expanding public APIs. Rejected models remain reproducible failure cases: failing assertions are evidence, not reasons to weaken checks until every model is green. Historical Issue chains are not current execution gates.

## Verification and limits

Here use `../../gradlew :model-c:miniappTest :model-c:checkMiniAppModel`; other models can run their reportKgpModel. Ordinary clean check success does not mean every explicit model-acceptance check passes. Node tests, IDE models and WeChat loading are separate evidence.
