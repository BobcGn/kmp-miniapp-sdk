---
translation-of: submodule-architecture-decisions
language: en
status: draft
title: decisions · historical architecture decisions
---

# Historical architecture decisions

[中文](SPEC-ch.md)

## Responsibility and entry

Preserve the context, rationale and rejected alternatives of accepted decisions. The [decision index](README-en.md) is the entry; [[architecture]] and its subtree describe current system and module boundaries.

## Maintenance boundary

An ADR is a contextual historical record, not an overwritable current-capability list. The initial states in 0003, privacy exclusion in 0008 and model comparison in 0010 belong to their original scopes and must not negate later implementation.

A genuine decision change adds a superseding ADR. An implementation/decision mismatch is recorded explicitly rather than rewriting the old record to claim the code satisfies it. The architecture spec identifies the difference between 0007's single exception path and adapters currently constructing that exception directly; importing specs does not decide whether to refactor.

Original 0001–0011 documents retain their paths and numbering. They are referenced sources, not individually registered sidebar nodes. This sole directory node owns maintenance rules without redefining historical decisions.

## Evidence limits

An ADR's `Accepted` label, Issue state or PoC file presence proves neither current implementation nor host acceptance. Verification references must lead back to source, test records and original environments; Chinese and English must not give a decision different meanings.
