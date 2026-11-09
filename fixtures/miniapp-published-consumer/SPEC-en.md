---
translation-of: module-fixture-published
language: en
status: draft
title: miniapp-published-consumer · published artifact identity
---

# Published dependency and source-archive identity

[中文](SPEC-ch.md)

## Responsibility and entry

Its own Wrapper/catalog pins the released Kotlin/Mini App combination, consuming plugin/runtime from public repositories. `PublishedSdkUsage.kt` is the Kotlin entry. verifyPublishedSdkSources checks actual binary components, matching sources JAR, implementation/KDoc, and reports paths/SHA-256.

## Why published identity needs separate proof

Composites can keep resolving correct local source despite missing, mismatched or incomplete public sources JARs. This consumer rejects local substitutions and checks actual resolved components/archive contents against binaries. A pinned version preserves evidence identity, not working-tree freshness.

[build.gradle.kts](build.gradle.kts) explicitly rejects included builds/project replacement and separates archive inspection from manual IDE acceptance. Downloading a source archive alone cannot prove editor auto-association.

## Isolation boundary

No includeBuild, mavenLocal or local project substitution is allowed; verification rejects project replacements rather than accepting working-tree dependencies for convenience. This is neither a WeChat host nor publication task. Pinned release source may differ from the working tree.

## Verification and limits

Here run `./gradlew --no-daemon --no-configuration-cache --console=plain compileKotlinMiniapp verifyPublishedSdkSources`. This proves artifact resolution/source contents, not IDE auto-association, clicks or hover. DEVELOPMENT retains manual IDEA evidence, without certifying Android Studio or VS Code Kotlin/KMP.
