# Releasing kmp-miniapp-sdk

[中文](RELEASING-ch.md)

This document is for a repository maintainer. It describes how version 0.1.0 is published, what has to
be true before a tag is pushed, and how to recover a publication that only half finished. It is not
part of the consumer flow; a consumer reads [CONSUMER_SETUP-en.md](CONSUMER_SETUP-en.md).

No secret value appears in this document, in a commit, in a log, or in a screenshot. The release
workflow reads every credential from GitHub Repository Secrets and never writes one to disk.

## 1. Coordinates and the one version source

| What | Coordinate |
| --- | --- |
| Runtime | `io.github.bobcgn:kmp-miniapp-sdk:0.1.0` |
| Gradle plugin | `io.github.bobcgn.miniapp` version `0.1.0` |
| Release tag | `v0.1.0` |

`gradle/libs.versions.toml` holds the single `miniapp` version. The runtime's `MiniAppSdk.VERSION` and
the plugin's runtime coordinate are both generated from it, so a published version and a reported
version cannot drift. A release changes that one entry and nothing else that carries a version.

## 2. Required GitHub Repository Secrets

Configured once, in the repository's own settings:

- `GPG_KEY_CONTENTS`, `SIGNING_KEY_ID`, `SIGNING_PASSWORD` — the signing key Maven Central validates.
- `MAVEN_CENTRAL_USERNAME`, `MAVEN_CENTRAL_PASSWORD` — the Sonatype Central Portal token.
- `GRADLE_PUBLISH_KEY`, `GRADLE_PUBLISH_SECRET` — the Gradle Plugin Portal token.

A maintainer never prints, pastes, or commits these values, and never asks another person to send
one. A local machine has no release credentials, which is why a local `publishPlugins` run ends at
"Missing publishing keys" — that is the expected outcome, not a broken publication model.

## 3. Before the tag

Run all of these from a clean working tree, and keep the output:

```shell
./gradlew projects
./gradlew --no-daemon --console=plain clean check
./gradlew --no-daemon --console=plain :kmp-miniapp-sdk:jsNodeTest --rerun-tasks
./gradlew --no-daemon --console=plain :miniapp-gradle-plugin:test --rerun-tasks
./gradlew --no-daemon --console=plain buildMiniAppSdk
./gradlew --no-daemon --console=plain verifyMiniAppGradlePluginIntegration
./gradlew --no-daemon --console=plain verifyMiniAppConsumerDocs
./gradlew --no-daemon --console=plain verifyMiniAppBundleSize
```

A failure stops the release. It is fixed or reported, never described as environment noise without
evidence.

Then inspect the publications themselves, because a POM that builds is not a POM that is correct:

```shell
./gradlew --no-daemon --console=plain --no-configuration-cache \
  :kmp-miniapp-sdk:generatePomFileForKotlinMultiplatformPublication \
  :kmp-miniapp-sdk:generatePomFileForJsPublication \
  :miniapp-gradle-plugin:generatePomFileForPluginMavenPublication \
  :miniapp-gradle-plugin:generatePomFileForMiniappPluginMarkerMavenPublication
```

Each generated POM must carry Apache-2.0, the project name and description, the project URL, the
developer, the SCM block, and version `0.1.0`.

Finally, prove the consumer shape locally without a signing key — this is the only situation in which
the unsigned property is legitimate, and release CI refuses to run if it is set:

```shell
./gradlew --no-daemon --console=plain --no-configuration-cache \
  clean \
  :kmp-miniapp-sdk:publishToMavenLocal \
  :miniapp-gradle-plugin:publishToMavenLocal \
  -PminiappLocalPublicationWithoutSigning
```

An independent consumer project that uses **only** `mavenLocal()` plus the public repositories, with
no `includeBuild` and no reference to this repository, must then apply
`id("io.github.bobcgn.miniapp") version "0.1.0"`, pass a `miniappTest` asserting
`MiniAppSdk.VERSION == "0.1.0"`, and assemble a bundle. This proves the shape that will be published;
it is not evidence that anything resolves online.

`git diff --check` must be clean, and the working tree must be committed before the tag exists.

## 4. Publishing

Pushing the annotated tag `v0.1.0` on the release commit starts
[`.github/workflows/release.yml`](../.github/workflows/release.yml). The workflow:

1. refuses to run unless `gradle/libs.versions.toml` says `0.1.0` and, for a tag, the tag is exactly
   `v0.1.0`;
2. refuses to run if `miniappLocalPublicationWithoutSigning` is set, because a release publication is
   always signed;
3. checks Maven Central for `kmp-miniapp-sdk/0.1.0` and **skips the runtime upload when it is already
   there**, because a Central release is immutable;
4. checks the Gradle Plugin Portal for `io.github.bobcgn.miniapp:0.1.0` and skips both plugin steps
   when it is already there, because the portal also rejects a version it holds;
5. runs `clean check verifyMiniAppGradlePluginIntegration`;
6. publishes the runtime to Maven Central with `publishAndReleaseToMavenCentral`;
7. validates the plugin publication with `publishPlugins --validate-only`;
8. publishes the plugin to the Gradle Plugin Portal.

The order is deliberate. The runtime goes first, the plugin second, and the plugin steps run only if
everything before them succeeded: a failure leaves a complete, immutable Central release rather than a
plugin whose runtime does not exist yet.

## 5. Recovering a half-finished publication

Maven Central and the Gradle Plugin Portal fail independently, and only one of them can be retried
safely. `workflow_dispatch` exists for exactly that, and requires typing `publish-0.1.0` as
confirmation.

| Situation | Action |
| --- | --- |
| Both succeeded | Nothing. Do not run the workflow again. |
| Central accepted, the Portal upload failed | Run the workflow with `target: plugin`. Maven Central is not touched: the runtime check finds the artifact and the runtime step is skipped. |
| Central failed, nothing published | Fix the cause, then run the workflow with `target: all` from the release commit. Re-running a tag is not necessary. |
| Central is mid-sync (`PENDING`/`VALIDATED` on the Central Portal) | Wait. Do not upload a second time; a second deployment of the same version is rejected or duplicates work a human then has to undo. |
| The Portal holds the plugin for review | Nothing to fix. The upload succeeded; a first publication of a new plugin id may be held for Gradle's own review before it resolves from the portal. Record it as pending approval and do not describe the plugin as available yet. |

**Never re-upload the runtime.** A published Maven Central version cannot be replaced; a correction
is a new version, not a second upload of the same one.

## 6. After the publication

Wait until both coordinates actually resolve, then verify the complete online consumer path — with no
`mavenLocal()`, no `includeBuild`, and no reference to this repository anywhere in the build:

1. A new empty Kotlin Multiplatform project whose `settings.gradle.kts` declares only
   `gradlePluginPortal()`, `mavenCentral()`, and `mavenCentral()`.
2. `id("io.github.bobcgn.miniapp") version "0.1.0"` applied to the KMP module.
3. `miniappMain` reading `MiniAppSdk.VERSION`, and a `miniappTest` asserting it is `0.1.0`.
4. `./gradlew miniappTest` and `./gradlew assembleMiniAppBundle` both pass.

Only that run turns "published" into an accepted online consumption path. Until it passes, "published"
means only that an upload was accepted, and it must not be written up as availability.
