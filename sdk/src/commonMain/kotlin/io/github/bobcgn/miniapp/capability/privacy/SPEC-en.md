---
translation-of: submodule-sdk-privacy
language: en
status: draft
title: privacy · privacy requirements and outcomes
---

# Privacy requirements and outcomes

[中文](SPEC-ch.md)

## Responsibility and public entries

`MiniAppPrivacy` supplies status, requestAuthorization and query-only requireSatisfied under key `privacy`. `PrivacyStatus` describes the host's current requirement/name; `PrivacyAuthorizationOutcome` describes Authorized/Refused for one request. Requirements, outcomes and errors are separate concepts.

## Semantics and interaction boundary

NOT_REQUIRED says only that the host requires no authorization now, not proof of consent: it may also mean no declared collection. Every call asks the host rather than treating old state as permanent truth; the host supplies the contract name.

Refusal is an outcome, not a permission-denial exception; unknown host failures remain HostFailure. When authorization is needed, requireSatisfied throws PrivacyAuthorizationRequired without prompting. Only a consumer gesture can request the host's consent UI.

This model shares no permission state and supplies no privacy-policy generation, compliance judgement or legal advice. Missing host APIs do not mean authorization is unnecessary.

## Verification and limits

`PrivacyContractChecks`, `MiniAppPrivacyContractTest` and `WechatPrivacyTest` check requirements, outcomes, preconditions and error distinctions. Run `./gradlew :kmp-miniapp-sdk:jsNodeTest`; real accounts and backend declarations determine prompt acceptance. [[submodule-wechat-evidence]] records outstanding evidence.
