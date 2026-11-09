---
translation-of: module-docs
language: en
status: draft
title: docs · documentation, decisions and evidence
---

# Documentation, decisions and evidence

[中文](SPEC-ch.md)

## Responsibility

Preserve responsibilities that source alone cannot explain, together with traceable capability and verification records. [[goal-and-requirements]] owns product goals, [[architecture]] owns system constraints, and nearby module specs own local contracts; this document does not copy them.

Directory specs [[submodule-architecture-decisions]] and [[submodule-wechat-evidence]] refine historical-decision and WeChat-evidence responsibilities without separately registering original ADR or matrix files.

## Documentation boundaries

- `PROJECT_FACTS-*` indexes current facts; source and executable configuration remain the highest authority for implementation facts.
- `decisions/` preserves accepted decisions and their context. A historical “not implemented” does not negate later source. Changing a decision requires a new ADR rather than rewriting the old record into new history. This import references ADRs without registering them individually as graph nodes.
- `DEVELOPMENT-*`, `CONSUMER_SETUP-*`, `TESTING-*` and `RELEASING-*` own development, integration, testing and maintainer-publication workflows respectively.
- The WeChat capability matrix owns status and minimum evidence levels; the host-verification matrix owns environments, records and evidence limits. Node results cannot substitute for host, device or backend results.
- `ROADMAP-*` is planning input and `performance-baseline.json` is size-comparison input; neither establishes a new capability or a result from this run.

## Graph and maintenance contract

Chinese `*-ch.md` files register nodes with unique `id`, `type`, `title` and real `parent` fields. English pairs use only `translation-of` to identify that node, with matching body facts, structure and intent. English files are not second nodes and use no duplicate ID. A path need not be named `SPEC.md`: frontmatter, not the filename, determines discovery.

`parent` expresses responsibility ownership; `depends-on` expresses only real dependencies, not execution order. Background material without a source dependency uses body links or `references`, never turns roadmap/Issue chains into development gates. Imported specs remain `draft`; only user review permits `active`.

## Verification and limits

`spec_validate` checks duplicate IDs, dangling links and parent cycles. Tree coverage, non-indexed English translations, translated structure and local file links are checked separately. `verifyMiniAppConsumerDocs` covers only README structure, paths and consumer commands, not semantic consistency of all documentation.

Implementation/contract gaps verified during import remain explicit in the owning spec; historical ADRs and original acceptance evidence are not silently rewritten. The spec index is not a new SDK API, publication or Gradle module.
