# 在新 KMP 项目中使用 kmp-miniapp-sdk 0.1.0

[English](CONSUMER_SETUP-en.md)

本文面向通过网络解析正式发布的 Gradle 插件与 runtime 的消费者项目。它不需要检出本仓库，不使用
`includeBuild`、内部 project 依赖或手工复制的 SDK 产物。

## 前置条件

- JDK 17 或更高版本
- 使用 Kotlin 2.4.20 的 Kotlin Multiplatform 项目（0.1.0 已测试的兼容基线）
- Gradle 能访问 Maven Central 与 Gradle Plugin Portal
- 原生 Mini App 宿主；微信是本仓库当前完成验证的宿主

## 1. 配置仓库

在 `settings.gradle.kts` 中保留标准公共仓库：

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
```

不要通过 `includeBuild` 添加本 SDK；那种方式只用于仓库开发。

## 2. 应用插件

在需要 Mini App 源码的 KMP module 中添加正式插件版本：

```kotlin
plugins {
    kotlin("multiplatform") version "2.4.20"
    id("io.github.bobcgn.miniapp") version "0.1.0"
}

kotlin {
    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

miniapp {
    wechat {
        bundleDirectory.set(layout.projectDirectory.dir("host/miniprogram/libs"))
    }
}
```

插件会自动把正式 runtime 坐标 `io.github.bobcgn:kmp-miniapp-sdk:0.1.0` 加入 `miniappMain`，不要再次
声明该依赖。

若 `commonMain` 依赖另一个 KMP project，也必须对该 project 应用同一个 Mini App 插件；依赖必须提供
`miniapp` variant。

## 3. 添加源码

Gradle sync 后，应用插件的 module 中会出现：

```text
src/
├── commonMain/kotlin/
├── commonTest/kotlin/
├── miniappMain/kotlin/
└── miniappTest/kotlin/
```

共享行为放在 `commonMain`；宿主 binding 与 JavaScript export 放在 `miniappMain`。渲染仍属于宿主的
WXML/WXSS 与薄 JavaScript 或 TypeScript 层；SDK 不负责渲染。

## 4. 测试与组装

在消费者项目根目录运行：

```shell
./gradlew miniappTest
./gradlew assembleMiniAppBundle
```

配置的 bundle 目录会收到消费者 module、TypeScript 声明、SDK runtime、Kotlin runtime 依赖与
`package.json`。在微信开发者工具中打开宿主项目并验证该 bundle；Node smoke check 有助于检查接线，但不等于
宿主验收。

## 故障排查

- **找不到插件：** 确认 `pluginManagement.repositories` 中存在 `gradlePluginPortal()`，并且插件声明写明
  `0.1.0`。
- **无法解析 runtime：** 确认 `dependencyResolutionManagement.repositories` 中存在 `mavenCentral()`；
  不要添加内部 project 依赖。
- **common 依赖没有 Mini App variant：** 对该 KMP 依赖应用 `io.github.bobcgn.miniapp`，不要把共享代码移出
  `commonMain`。
- **已有 Yarn lock 发生变化：** 执行一次 `./gradlew kotlinUpgradeYarnLock`，审查 lock 变更后重新构建。
- **bundle 携带 Compose 或其他 renderer：** 从 Mini App runtime 依赖图移除该依赖。插件会有意拒绝 renderer。

## 0.1.0 的范围

0.1.0 是第一个公开的实验性版本。能力状态与真实宿主证据记录于
[微信能力矩阵](platforms/wechat/WECHAT_CAPABILITIES-ch.md)。宿主成功回调不代表后端业务事实；标记为
`Partial`、`Experimental` 或 `Planned` 的能力不得视为已完成生产验证。

它同时也是该插件 id 的首次发布，因此 Gradle Plugin Portal 可能把这次上传挂起以待其自身审核，Maven Central 在
发布后也需要一小段时间同步。如果发布后短时间内插件仍无法解析，原因在此，而不是你的构建配置有问题。发布本身属于维护者
事务，记录在 [RELEASING-ch.md](RELEASING-ch.md)。
