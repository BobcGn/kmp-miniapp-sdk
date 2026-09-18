# kmp-miniapp-sdk 发布流程

[English](RELEASING-en.md)

本文档面向仓库维护者，说明 0.1.0 如何发布、打 tag 之前必须满足什么条件，以及发布只完成一半时如何恢复。它不属于
消费者主流程；消费者请阅读 [CONSUMER_SETUP-ch.md](CONSUMER_SETUP-ch.md)。

本文档、提交、日志与截图中都不出现任何 Secret 值。发布 workflow 只从 GitHub Repository Secrets 读取凭据，从不
把凭据写入磁盘。

## 1. 坐标与唯一的版本来源

| 内容 | 坐标 |
| --- | --- |
| Runtime | `io.github.bobcgn:kmp-miniapp-sdk:0.1.0` |
| Gradle 插件 | `io.github.bobcgn.miniapp` 版本 `0.1.0` |
| 发布 tag | `v0.1.0` |

`gradle/libs.versions.toml` 中的 `miniapp` 是唯一的版本来源。runtime 的 `MiniAppSdk.VERSION` 与插件使用的
runtime 坐标都由它生成，因此发布出去的版本与代码里上报的版本不可能漂移。一次发布只改这一处，其他任何携带版本号
的地方都不手工修改。

## 2. 必需的 GitHub Repository Secrets

在仓库设置中配置一次：

- `GPG_KEY_CONTENTS`、`SIGNING_KEY_ID`、`SIGNING_PASSWORD` —— Maven Central 用于校验的签名密钥。
- `MAVEN_CENTRAL_USERNAME`、`MAVEN_CENTRAL_PASSWORD` —— Sonatype Central Portal 令牌。
- `GRADLE_PUBLISH_KEY`、`GRADLE_PUBLISH_SECRET` —— Gradle Plugin Portal 令牌。

维护者不得打印、粘贴或提交这些值，也不得要求他人把其中任何一个发到终端或对话中。本地机器没有发布凭据，因此本地
执行 `publishPlugins` 会停在 "Missing publishing keys" —— 这是预期结果，不是发布模型故障。

## 3. 打 tag 之前

在干净工作树上依次执行以下命令，并保留输出：

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

任何一项失败都会中止发布：要么修复，要么如实报告，不得在没有确切证据时把失败描述为环境噪声。

随后检查 publication 本身，因为「能构建出 POM」不等于「POM 是正确的」：

```shell
./gradlew --no-daemon --console=plain --no-configuration-cache \
  :kmp-miniapp-sdk:generatePomFileForKotlinMultiplatformPublication \
  :kmp-miniapp-sdk:generatePomFileForJsPublication \
  :miniapp-gradle-plugin:generatePomFileForPluginMavenPublication \
  :miniapp-gradle-plugin:generatePomFileForMiniappPluginMarkerMavenPublication
```

每个生成的 POM 都必须带有 Apache-2.0、项目名与描述、项目 URL、developer、SCM 与版本 `0.1.0`。

最后，在没有签名密钥的情况下本地验证消费者形态 —— 这是唯一允许使用无签名开关的场景，而 release CI 在该属性被
设置时会拒绝运行：

```shell
./gradlew --no-daemon --console=plain --no-configuration-cache \
  clean \
  :kmp-miniapp-sdk:publishToMavenLocal \
  :miniapp-gradle-plugin:publishToMavenLocal \
  -PminiappLocalPublicationWithoutSigning
```

随后，一个**只**使用 `mavenLocal()` 加公共仓库、不含 `includeBuild`、也不引用本仓库的独立消费者工程，必须能够
应用 `id("io.github.bobcgn.miniapp") version "0.1.0"`，通过断言 `MiniAppSdk.VERSION == "0.1.0"` 的
`miniappTest`，并组装出 bundle。这证明的是「即将被发布的形状」，不是「在线可解析」。

`git diff --check` 必须干净，且在 tag 存在之前工作树必须已提交。

## 4. 发布

在 release commit 上推送 annotated tag `v0.1.0` 会触发
[`.github/workflows/release.yml`](../.github/workflows/release.yml)。该 workflow：

1. 若 `gradle/libs.versions.toml` 不是 `0.1.0`，或 tag 触发时 tag 不是精确的 `v0.1.0`，则拒绝运行；
2. 若 `miniappLocalPublicationWithoutSigning` 被设置，则拒绝运行，因为正式发布必须签名；
3. 查询 Maven Central 是否已有 `kmp-miniapp-sdk/0.1.0`，**已存在时跳过 runtime 上传**，因为 Central 的发布不可覆盖；
4. 查询 Gradle Plugin Portal 是否已有 `io.github.bobcgn.miniapp:0.1.0`，已存在时跳过两个插件步骤，因为 portal 同样拒绝其已持有的版本；
5. 运行 `clean check verifyMiniAppGradlePluginIntegration`；
6. 用 `publishAndReleaseToMavenCentral` 把 runtime 发布到 Maven Central；
7. 用 `publishPlugins --validate-only` 校验插件发布；
8. 把插件发布到 Gradle Plugin Portal。

这个顺序是刻意的：runtime 先发布，插件后发布，且插件的步骤只在其之前的步骤全部成功后才执行 —— 失败时留下的是一个
完整的、不可变的 Central 发布，而不是一个「runtime 还不存在」的插件。

## 5. 发布只完成一半时的恢复

Maven Central 与 Gradle Plugin Portal 会各自独立失败，而其中只有一侧可以安全重试。`workflow_dispatch` 正是为此
存在，并要求手工输入 `publish-0.1.0` 作为确认。

| 情况 | 处理方式 |
| --- | --- |
| 两者都成功 | 什么都不做。不要再次运行该 workflow。 |
| Central 已接受，Portal 上传失败 | 以 `target: plugin` 运行该 workflow。Maven Central 不会被触碰：runtime 检查会发现产物已存在并跳过。 |
| Central 失败，尚未发布任何内容 | 修复原因后，在 release commit 上以 `target: all` 运行。不必重新推送 tag。 |
| Central 正在同步中（Central Portal 显示 `PENDING`/`VALIDATED`） | 等待。不要重复上传：同一版本的第二次部署会被拒绝，或制造出需要人工撤销的重复工作。 |
| Portal 将插件挂起以待审核 | 无需修复。上传已经成功；某个新插件 id 的首次发布可能被 Gradle 自己的审核流程挂起，之后才可从 portal 解析。记录为 pending approval，且不得把该插件描述为已经可用。 |

**绝不重复上传 runtime。** 已发布的 Maven Central 版本无法替换；纠正方式是发布新版本，而不是把同一版本再传一次。

## 6. 发布之后

等待两个坐标真正可解析后，再验证完整的在线消费者链路 —— 不含 `mavenLocal()`、不含 `includeBuild`，且构建中任何
位置都不引用本仓库：

1. 新建一个空的 Kotlin Multiplatform 工程，其 `settings.gradle.kts` 只声明 `gradlePluginPortal()`、
   `mavenCentral()` 与 `mavenCentral()`。
2. 在 KMP module 上应用 `id("io.github.bobcgn.miniapp") version "0.1.0"`。
3. `miniappMain` 读取 `MiniAppSdk.VERSION`，`miniappTest` 断言其为 `0.1.0`。
4. `./gradlew miniappTest` 与 `./gradlew assembleMiniAppBundle` 均通过。

只有那次运行才能把「已发布」变成「在线消费链路已验收」。在该验证通过之前，「已发布」只意味着上传被接受，不得写成
「已可用」。
