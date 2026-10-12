# kmp-miniapp-sdk 发布流程

[English](RELEASING-en.md)

本文档面向仓库维护者，说明 0.1.1 如何发布、打 tag 之前必须满足什么条件，以及发布只完成一半时如何恢复。它不属于
消费者主流程；消费者请阅读 [CONSUMER_SETUP-ch.md](CONSUMER_SETUP-ch.md)。

本文档、提交、日志与截图中都不出现任何 Secret 值。发布 workflow 只从 GitHub Repository Secrets 读取凭据，从不
把凭据写入磁盘。

## 1. 坐标与唯一的版本来源

| 内容 | 坐标 |
| --- | --- |
| Runtime | `io.github.bobcgn:kmp-miniapp-sdk:0.1.1` |
| Gradle 插件 | `io.github.bobcgn.miniapp` 版本 `0.1.1` |
| 发布 tag | `v0.1.1` |

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
./gradlew --no-daemon --console=plain clean check :build-logic:test
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

每个生成的 POM 都必须带有 Apache-2.0、项目名与描述、项目 URL、developer、SCM 与版本 `0.1.1`。

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
应用 `id("io.github.bobcgn.miniapp") version "0.1.1"`，通过断言 `MiniAppSdk.VERSION == "0.1.1"` 的
`miniappTest`，并组装出 bundle。这证明的是「即将被发布的形状」，不是「在线可解析」。

`git diff --check` 必须干净，且在 tag 存在之前工作树必须已提交。

## 4. 发布

该 workflow 专用于已有的 `v0.1.1` 发布。它检出固定发布提交
`024abd672224700a78349f9b94bab90f135f3736`，获取 tags，并校验 `HEAD` 与 `v0.1.1^{commit}`
均等于该 SHA。版本目录必须仍为 `0.1.1`；tag push 的事件 ref 与 SHA 也必须匹配。
手动恢复即使从 tag 发起，也必须输入 `publish-0.1.1`；确认输入通过环境变量进入 shell，绝不作为脚本文本插入。
新版本需要明确评审版本、tag 与提交的更新，绝不移动已有 tag。

[`.github/workflows/release.yml`](../.github/workflows/release.yml) 随后：

1. 拒绝无签名发布的环境属性，包括空值；
2. 查询 Maven Central 上 runtime 根 POM 与 JS POM：两个 HTTP 200 表示存在，两个 404 表示缺失，
   部分可见或其他响应都停止运行；
3. 若 `target: plugin`，在任何可能发布的操作之前要求 runtime 已存在；
4. 查询 Plugin Portal marker POM，区分 200 与 404，查询异常时停止；
5. 在固定发布源码上运行 `clean check verifyMiniAppGradlePluginIntegration`；
6. 仅当 runtime 缺失且目标为 `all`（或 tag push）时发布 runtime；
7. 在任何插件上传前轮询两个 runtime POM，最多 30 次，间隔 10 秒，curl 有连接与总时长限制；
   仅 404 会重试，网络与其他 HTTP 异常立即停止；
8. 仅当插件 marker 缺失且之前每一步均成功时，校验并发布插件。

这些 POM 检查证明仓库元数据可见，不等于完整在线消费者构建成功。公开仓库的 404 不能判定 Portal 审核或
Central 部署是否仍在进行；手动恢复前必须核对对应 portal 状态，禁止重复提交仍在处理的部署。

## 5. 发布只完成一半时的恢复

PR 合并后，从默认分支运行更新后的 workflow；无论 dispatch 选择哪个分支，它都检出固定的发布 SHA。
选择旧 tag 会使用该 tag 上的旧 workflow 定义，不包含这些新保护。输入 `publish-0.1.1` 作为确认。

| 情况 | 处理方式 |
| --- | --- |
| 两者都成功 | 什么都不做，不要再次运行 workflow。 |
| 两个 runtime POM 可解析，Portal 上传失败 | 使用 `target: plugin`，绝不重复上传 runtime。 |
| Central 失败且没有待处理的部署 | 修复原因后使用 `target: all`，workflow 仍构建固定发布提交。 |
| Central 正在同步（`PENDING`/`VALIDATED`），或仅一个 runtime POM 可解析 | 等待，不要重复上传。 |
| 仓库查询失败 | 停止并解决查询故障，不能将其当成发布缺失的证据。 |
| Portal 将插件挂起以待审核 | 等待审核；公开 marker 返回 404 不授权重复上传。 |

固定的 0.1.1 源码含有一条历史缺失 runtime 测试，其前提是 Central 上不存在该 runtime。如今该版本已可获取，
旧测试可能阻断恢复验证。当前分支修复了测试，但无法追溯修改不可变的发布提交。workflow 不绕过该测试、不移动
tag；若它失败，恢复仍保持阻断。修改该历史验证策略需要单独评审的决定。

**绝不重复上传 runtime。** 已发布版本无法替换，纠正方式是发布新版本。

## 6. 发布之后

等待两个坐标真正可解析后，再验证完整的在线消费者链路 —— 不含 `mavenLocal()`、不含 `includeBuild`，且构建中任何
位置都不引用本仓库：

1. 新建一个空的 Kotlin Multiplatform 工程，其 `settings.gradle.kts` 只声明 `gradlePluginPortal()`、
   `mavenCentral()` 与 `mavenCentral()`。
2. 在 KMP module 上应用 `id("io.github.bobcgn.miniapp") version "0.1.1"`。
3. `miniappMain` 读取 `MiniAppSdk.VERSION`，`miniappTest` 断言其为 `0.1.1`。
4. `./gradlew miniappTest`、`./gradlew initMiniApp`、`./gradlew syncMiniApp`、`./gradlew assembleMiniApp`
   与 `./gradlew verifyMiniApp` 均通过，且组装出的 distribution 能在微信开发者工具中打开。

第 4 步的最后一句是其他任何东西都无法替代的：`verifyMiniApp` 是结构与 Node 层面的检查，只有真实宿主中的一次运行，
才能把一次发布变成一条被接受的消费路径。

只有那次运行才能把「已发布」变成「在线消费链路已验收」。在该验证通过之前，「已发布」只意味着上传被接受，不得写成
「已可用」。
