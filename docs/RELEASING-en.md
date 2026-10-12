# Releasing kmp-miniapp-sdk

[中文](RELEASING-ch.md)

This document is for a repository maintainer. It describes how version 0.1.1 is published, what has to
be true before a tag is pushed, and how to recover a publication that only half finished. It is not
part of the consumer flow; a consumer reads [CONSUMER_SETUP-en.md](CONSUMER_SETUP-en.md).

No secret value appears in this document, in a commit, in a log, or in a screenshot. The release
workflow reads every credential from GitHub Repository Secrets and never writes one to disk.

## 1. Coordinates and the one version source

| What | Coordinate |
| --- | --- |
| Runtime | `io.github.bobcgn:kmp-miniapp-sdk:0.1.1` |
| Gradle plugin | `io.github.bobcgn.miniapp` version `0.1.1` |
| Release tag | `v0.1.1` |

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
./gradlew --no-daemon --console=plain clean check :build-logic:test
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
developer, the SCM block, and version `0.1.1`.

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
`id("io.github.bobcgn.miniapp") version "0.1.1"`, pass a `miniappTest` asserting
`MiniAppSdk.VERSION == "0.1.1"`, and assemble a bundle. This proves the shape that will be published;
it is not evidence that anything resolves online.

`git diff --check` must be clean, and the working tree must be committed before the tag exists.

## 4. Publishing

The workflow is specific to the existing `v0.1.1` release. It checks out the fixed release commit
`024abd672224700a78349f9b94bab90f135f3736`, with tags fetched, and verifies that both `HEAD` and
`v0.1.1^{commit}` equal that SHA. The version catalog must still say `0.1.1`. A tag push must also
carry the matching event ref and SHA. Manual recovery requires `publish-0.1.1` even when dispatched
from a tag; the confirmation enters the shell as an environment variable, never script text.
A new release needs an explicitly reviewed version/tag/commit update; never move the existing tag.

[`.github/workflows/release.yml`](../.github/workflows/release.yml) then:

1. refuses the unsigned-publication environment property, including an empty value;
2. probes both the runtime root POM and JS POM on Maven Central: two HTTP 200 responses mean
   present, two 404 responses mean missing, and partial visibility or any other response stops the run;
3. for `target: plugin`, requires the runtime to be present before doing anything that could publish;
4. probes the Plugin Portal marker POM, distinguishing 200 from 404 and stopping on query errors;
5. runs the pinned release source's `clean check verifyMiniAppGradlePluginIntegration`;
6. publishes the runtime only when absent and the target is `all` (or a tag push);
7. before any plugin upload, polls both runtime POMs for up to 30 attempts, with 10 seconds between
   attempts and bounded curl timeouts; only 404 is retried, while network and other HTTP errors stop;
8. validates and publishes the plugin only if its marker was missing and every prior step succeeded.

These POM checks establish repository metadata visibility, not a complete online consumer build.
Portal review and Central deployments still in progress cannot be inferred from a public 404:
inspect their portal state before manual recovery and do not resubmit a pending deployment.

## 5. Recovering a half-finished publication

Run the updated workflow from the default branch after its PR is merged; it always checks out the
fixed release SHA regardless of the branch selected for dispatch. Selecting the old tag would use
that tag's old workflow definition, without these new guards. Type `publish-0.1.1` as confirmation.

| Situation | Action |
| --- | --- |
| Both succeeded | Nothing. Do not run the workflow again. |
| Both runtime POMs resolve, Portal upload failed | Use `target: plugin`; the runtime is never re-uploaded. |
| Central failed and no deployment is pending | Fix the cause, then use `target: all`; the workflow still builds the fixed release commit. |
| Central is mid-sync (`PENDING`/`VALIDATED`), or only one runtime POM resolves | Wait. Do not upload a second time. |
| A repository query fails | Stop and resolve the query failure; it is not evidence that a publication is missing. |
| Portal holds the plugin for review | Wait for review. A public marker 404 does not authorize re-uploading it. |

The fixed 0.1.1 source contains a historical missing-runtime test that assumes the runtime is absent
from Central. Since the release is now available, that old test can block recovery verification.
The current branch fixes the test, but cannot retroactively change the immutable release commit.
This workflow does not bypass the test or move the tag; recovery remains blocked if it fails.
Any change to that historical verification policy requires a separate reviewed decision.

**Never re-upload the runtime.** A published version cannot be replaced; a correction is a new version.

## 6. After the publication

Wait until both coordinates actually resolve, then verify the complete online consumer path — with no
`mavenLocal()`, no `includeBuild`, and no reference to this repository anywhere in the build:

1. A new empty Kotlin Multiplatform project whose `settings.gradle.kts` declares only
   `gradlePluginPortal()`, `mavenCentral()`, and `mavenCentral()`.
2. `id("io.github.bobcgn.miniapp") version "0.1.1"` applied to the KMP module.
3. `miniappMain` reading `MiniAppSdk.VERSION`, and a `miniappTest` asserting it is `0.1.1`.
4. `./gradlew miniappTest`, `./gradlew initMiniApp`, `./gradlew syncMiniApp`,
   `./gradlew assembleMiniApp` and `./gradlew verifyMiniApp` all pass, and the assembled
   distribution opens in WeChat Developer Tools.

Step 4's last clause is the one nothing else can substitute for: `verifyMiniApp` is structural
checking on Node-level evidence, and only a run in the real host turns a publication into an accepted
one.

Only that run turns "published" into an accepted online consumption path. Until it passes, "published"
means only that an upload was accepted, and it must not be written up as availability.
