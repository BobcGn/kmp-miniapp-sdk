# 在新 KMP 项目中使用 kmp-miniapp-sdk 0.1.1

[English](CONSUMER_SETUP-en.md)

本文描述 0.1.1 通过 release gate 并发布后的在线消费路径。在此之前，0.1.0 仍是最新公开版本，0.1.1
工作流通过本仓库的 composite-build fixture 验证。正式发布路径不需要检出本仓库，不使用 `includeBuild`、
内部 project 依赖或手工复制的 SDK 产物。

## 前置条件

- JDK 17 或更高版本
- 使用 Kotlin 2.4.20 的 Kotlin Multiplatform 项目（0.1.1 已测试的兼容基线）
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
    id("io.github.bobcgn.miniapp") version "0.1.1"
}

kotlin {
    sourceSets {
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
```

插件会自动把正式 runtime 坐标 `io.github.bobcgn:kmp-miniapp-sdk:0.1.1` 加入 `miniappMain`，不要再次
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

## 4. 创建 source host 并进入开发循环

```shell
./gradlew miniappTest
./gradlew initMiniApp
./gradlew syncMiniApp
```

`initMiniApp` 在 `miniApp/` 写出一个最小微信工程，并且可以安全重复执行：它只创建缺失的文件，绝不重写你编辑过
的文件。`syncMiniApp` 把编译后的 module 复制进 `miniApp/miniprogram/libs/`，那是插件在你项目内的区域；
`miniApp/` 的其余部分属于你。在微信开发者工具中打开 `miniApp/`，在 `project.config.json` 中填入你自己的
AppID；此后每次修改 Kotlin，运行 `syncMiniApp` 并在那里重新编译。

## 5. 组装并校验 distribution

```shell
./gradlew assembleMiniApp
./gradlew verifyMiniApp
```

`assembleMiniApp` 把完整、独立的微信工程写入 `build/miniapp-dist/`：你的 source host 加上当前编译出的
module，并排除与本机和 IDE 绑定的文件。它不要求先运行 `syncMiniApp`，也从不修改 source host。随后
`verifyMiniApp` 单独读取该目录，若它不是一个完整的宿主工程，就会明确指出缺失的文件或无法解析的 `require()`。
在微信开发者工具中打开 `build/miniapp-dist/` 以验收该产物；Node smoke check 有助于检查接线，但不等于宿主验收。

## 故障排查

- **找不到插件：** 确认 `pluginManagement.repositories` 中存在 `gradlePluginPortal()`，并且插件声明写明
  `0.1.1`。
- **无法解析 runtime：** 确认 `dependencyResolutionManagement.repositories` 中存在 `mavenCentral()`；
  不要添加内部 project 依赖。
- **common 依赖没有 Mini App variant：** 对该 KMP 依赖应用 `io.github.bobcgn.miniapp`，不要把共享代码移出
  `commonMain`。
- **已有 Yarn lock 发生变化：** 执行一次 `./gradlew kotlinUpgradeYarnLock`，审查 lock 变更后重新构建。
- **bundle 携带 Compose 或其他 renderer：** 从 Mini App runtime 依赖图移除该依赖。插件会有意拒绝 renderer。
- **`assembleMiniApp` 报 source host 不存在：** 先执行一次 `./gradlew initMiniApp`，或用
  `miniapp { wechat { sourceDirectory } }` 指向你自己的小程序工程。
- **配置的路径被拒绝：** 插件拒绝工程之外的 source host、source host 之外的 generated library area，以及工程之外的
  distribution，因为它们都会被 `Sync` 写入。错误会指出该路径以及原本会被删除的内容。

## 0.1.1 的范围

0.1.0 是第一个公开的实验性版本；0.1.1 增加了上文所述的 distribution pipeline，且未改变 0.1.0 引入的任何东西。
能力状态与真实宿主证据记录于
[微信能力矩阵](platforms/wechat/WECHAT_CAPABILITIES-ch.md)。宿主成功回调不代表后端业务事实；标记为
`Partial`、`Experimental` 或 `Planned` 的能力不得视为已完成生产验证。

0.1.0 同时也是该插件 id 的首次发布，因此 Gradle Plugin Portal 对那次上传做过自身审核，Maven Central 在发布后也
需要一小段时间同步。后续版本不再有首次上传的延迟，但如果发布后短时间内插件仍无法解析，原因在此，而不是你的构建配置有问题。
发布本身属于维护者事务，记录在 [RELEASING-ch.md](RELEASING-ch.md)。
