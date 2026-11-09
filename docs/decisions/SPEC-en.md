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

## Why decision records differ from current specs

A record can accurately describe its original choice without remaining a current-capability list. Keeping context/rejected alternatives lets agents understand boundaries they must not accidentally repeat. Current specs carry applicable decisions/verified gaps rather than rewriting history to pretend code always complied. The [decision index](README-en.md) establishes this maintenance convention.

Accepted consequently does not mean current implementation satisfies a decision; not-implemented claims retain their historical scope. Original records supply rationale, source supplies implementation facts, and neither impersonates the other.

## Maintenance boundary

An ADR is a contextual historical record, not an overwritable current-capability list. The initial states in 0003, privacy exclusion in 0008 and model comparison in 0010 belong to their original scopes and must not negate later implementation.

A genuine decision change adds a superseding ADR. An implementation/decision mismatch is recorded explicitly rather than rewriting the old record to claim the code satisfies it. The architecture spec identifies the difference between 0007's single exception path and adapters currently constructing that exception directly; recording that unresolved gap does not establish implementation compliance.

Original 0001–0011 documents retain their paths and numbering. They are referenced sources, not individually registered sidebar nodes. This sole directory node owns maintenance rules without redefining historical decisions.

## Evidence limits

An ADR's `Accepted` label, Issue state or PoC file presence proves neither current implementation nor host acceptance. Verification references must lead back to source, test records and original environments; Chinese and English must not give a decision different meanings.
