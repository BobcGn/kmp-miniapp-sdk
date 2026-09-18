# kmp-miniapp-sdk

[English](README-en.md)

一个 Kotlin Multiplatform Mini App 平台：Gradle 插件为普通 Kotlin Multiplatform 项目提供 Mini App
source set、测试 source set 与可分发的宿主 bundle，以及这些源码所编译依赖的 Kotlin runtime。Mini App
是平台，**微信是当前 Host**，渲染由宿主负责。

下列工作流与每一条命令都取自并实际执行于本仓库真实的消费者构建：
[`fixtures/miniapp-consumer`](fixtures/miniapp-consumer)。代码片段只在文中明确说明的位置做删减，
不会引入未经验证的 API 或构建步骤。

## 状态

实验性 / pre-alpha。Consumer Bridge、Storage、`wx.login` 客户端认证启动、`wx.request` HTTP transport、
App lifecycle、微信页面栈导航、运行时能力检测、权限生命周期、微信会话有效性检查、微信剪贴板与震动、
微信基础文件系统访问、按需单次微信定位、微信扫码与微信媒体选择已实现并通过规定的真实宿主验证。隐私授权已实现，
但仍取决于小程序后台隐私配置与账号，因此真实宿主验收尚未完成。微信订阅消息已实现并具备自动化覆盖，其验收需要
已配置的模板。微信网络扩展已实现并具备自动化覆盖；上传与下载的验收需要受控 HTTPS 测试服务，真实网络切换需要
真机。微信标准支付是「后端参数的类型化转发器」，其验收需要合法商户环境，因此保持 `Partial`。每项能力的准确
状态见[微信能力矩阵](docs/platforms/wechat/WECHAT_CAPABILITIES-ch.md)，其中的 `Planned` 条目不是实现事实。

**插件与 runtime SDK 目前尚未发布。** 这在实践中意味着什么，见
[发布之前：今天你需要做什么](#发布之前今天你需要做什么)。

## 本项目是什么

- 承载共享客户端行为与宿主能力的 Kotlin Multiplatform library
- 把该 library 构建成 Mini App 宿主可加载 distribution 的 Gradle 插件
- 面向微信小程序 runtime 的类型化 Kotlin/JavaScript 边界

## 本项目不是什么

- UI framework 或 Kuikly replacement
- Compose renderer、Virtual DOM 或 WXML generator
- WXML 或 WXSS 的替代品
- 任何形式的 renderer：后端拥有业务事实，Kotlin 拥有客户端行为，宿主拥有渲染。见
  [ADR 0011](docs/decisions/0011-presentation-state-shared-rendering-host-native-ch.md)。

## 各自负责什么

| 层 | 负责 | 不负责 |
| --- | --- | --- |
| **Gradle 插件** `io.github.bobcgn.miniapp` | `miniapp` target 与 `miniappMain` / `miniappTest` source set、runtime 依赖、Node test run、`assembleMiniAppBundle`、renderer 边界检查、`miniapp { }` extension | 任何 runtime API、任何渲染、任何业务配置 |
| **Runtime SDK** `io.github.bobcgn:kmp-miniapp-sdk` | 你的 `miniappMain` 所编译依赖的 Kotlin API：共享 contract、宿主能力抽象、微信 adapter、错误与异步模型 | 渲染、WXML/WXSS、view tree、业务真相、认证或支付确认 |
| **微信宿主**（你拥有） | `project.config.json`、`app.json`、页面、WXML、WXSS、require 并调用 bundle 的薄 JavaScript、`setData` 与事件转发 | 本 SDK 内部的任何东西 |
| **Presentation Runtime** | `UiState`、`Action`、`Store`、`Effect`、state machine、presentation logic —— **未实现，本文档也不展示任何相关 API** | 属于 P2 范围，见[路线图](docs/ROADMAP-ch.md) |

## 快速开始

以下步骤从一个普通 Kotlin Multiplatform 项目得到可用的 Mini App。这正是
[`fixtures/miniapp-consumer`](fixtures/miniapp-consumer) 每次验证所运行的流程。

这些就是 fixture 自己的文件，只有两处本文档必须做的适配：fixture 位于本仓库内部，因此它的 `includeBuild`
路径是 `../..`、项目名是 `miniapp-consumer`；并且它通过 Gradle 属性选择宿主 bundle 目录，使同一份构建既能
指向默认位置也能指向宿主位置。API 调用、源码与脚本形态都来自 fixture。

### 0. 前置条件

- Kotlin Gradle Plugin 支持的 JDK（本插件在 JDK 17 上构建与测试）
- 在插件与 SDK 发布之前，把本仓库检出到你的项目旁边
- 你自己的项目里有 Gradle Wrapper —— 消费者永远不需要本仓库的 wrapper

### 1. `settings.gradle.kts`

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

    // 发布之前，插件来自本仓库的 composite build。
    includeBuild("../kmp-miniapp-sdk")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

// pluginManagement 的 composite 只解析插件。依赖替换 —— 让 runtime SDK 以
// `io.github.bobcgn:kmp-miniapp-sdk` 到达 —— 还需要在 settings 级别 include 该构建。
includeBuild("../kmp-miniapp-sdk")

rootProject.name = "my-miniapp"
```

### 2. `build.gradle.kts`

```kotlin
plugins {
    kotlin("multiplatform") version "2.4.20"
    id("io.github.bobcgn.miniapp")
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

这就是完整的 build script。没有 Kotlin/JS target 要声明、没有 source set 要创建、没有 `dependsOn`
要接线、没有 runtime artifact 要声明、也没有产物要复制。`miniappMain`、`miniappTest`、它们与
`commonMain` / `commonTest` 的边、runtime 依赖与 bundle 任务全部来自插件。Kotlin 2.4.20 是插件构建与测试
所用的版本；其他 Kotlin Gradle Plugin 版本的兼容性尚未确立。

### 3. `commonMain` 中的共享行为

```kotlin
package consumer

public fun greeting(name: String): String = "Hello, $name, from commonMain"

public class Counter(private var value: Int = 0) {
    public fun increment(): Int {
        value += 1
        return value
    }

    public fun current(): Int = value
}
```

这个文件不知道微信、JavaScript 或渲染的存在。它同样是 Compose 客户端会使用的代码。

### 4. `miniappMain` 中的宿主入口

```kotlin
package consumer

import io.github.bobcgn.miniapp.api.MiniAppSdk
import kotlin.js.JsExport

@JsExport
public fun hostGreeting(name: String): String = greeting(name)

@JsExport
public fun countUpTo(limit: Int): Int {
    val counter = Counter()
    repeat(limit) { counter.increment() }
    return counter.current()
}

@JsExport
public fun sdkVersion(): String = MiniAppSdk.VERSION
```

只有 `@JsExport` 声明会到达宿主。这个文件调用 `commonMain` 与 runtime SDK 的公共 API（之所以能编译，是因为
插件把 SDK 接了进来），除此之外什么都不做 —— 没有 view、没有 layout、没有 `wx` 调用、没有 markup。

### 5. 测试

`commonTest` 承载共享测试：

```kotlin
package consumer

import kotlin.test.Test
import kotlin.test.assertEquals

public class SharedTest {

    @Test
    public fun greetingIsShared() {
        assertEquals("Hello, Ada, from commonMain", greeting("Ada"))
    }
}
```

`miniappTest` 承载需要 Mini App compilation 的测试：

```kotlin
package consumer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

public class MiniAppTest {

    @Test
    public fun miniAppMainReusesTheSharedBehaviour() {
        assertEquals("Hello, Ada, from commonMain", hostGreeting("Ada"))
    }

    @Test
    public fun theRuntimeSdkIsOnTheMiniAppClasspath() {
        assertTrue(sdkVersion().isNotBlank(), "the runtime SDK must report a version")
    }
}
```

### 6. 运行测试

```shell
./gradlew miniappTest
```

`miniappTest` 同时运行两半：位于 `miniappTest` 的测试，以及声明在 `commonTest` 的测试。在 fixture 中即
`consumer.MiniAppTest`（3 个）加 `consumer.SharedTest`（2 个），执行记录位于
`build/test-results/miniappNodeTest/`。

### 7. 组装 bundle

```shell
./gradlew assembleMiniAppBundle
```

bundle 落在第 2 步配置的目录 —— 这里是 `host/miniprogram/libs`，不配置则是 `build/miniapp/bundle`。
它是 compiler-managed distribution，在 fixture 中包含 14 个文件，其中有：

| 文件 | 是什么 |
| --- | --- |
| `miniapp-consumer-miniapp.js` | 你编译后的 module，宿主唯一 require 的东西 |
| `miniapp-consumer-miniapp.d.ts` | 它的 TypeScript declaration，供 TypeScript 宿主使用 |
| `kmp-miniapp-sdk-kotlin.js` | runtime SDK，作为独立 module |
| `kotlin-kotlin-stdlib.js`、`kotlinx-coroutines-core.js`、`kotlinx-atomicfu.js` | 该 compilation 解析到的 Kotlin runtime |
| `package.json` | 把它们连起来的 module 元数据 |

其中没有 WXML 或 WXSS。markup 属于宿主，插件不生成任何 markup。

### 8. 在微信宿主中加载

宿主是一个拥有自己 UI 的普通小程序。[`fixtures/miniapp-consumer/host`](fixtures/miniapp-consumer/host)
是一个最小实例：

```text
host/
├── project.config.json          miniprogramRoot: "miniprogram/"
└── miniprogram/
    ├── app.json                 pages
    └── pages/index/
        ├── index.wxml           用户看到的视图
        └── index.js             薄 JavaScript：require bundle、调用它、setData
```

页面以相对路径 require bundle 并绑定结果：

```javascript
const consumer = require('../../libs/miniapp-consumer-miniapp.js').consumer;

Page({
  onLoad() {
    const greeting = consumer.hostGreeting('WeChat');
    const counted = consumer.countUpTo(5);
    const version = consumer.sdkVersion();

    this.setData({ greeting, counted: String(counted), version });
  }
});
```

为篇幅做了删减：fixture 的处理函数还会打印每个值，并在失败时渲染错误而不是抛出。

把 `miniprogramRoot` 指向你自己的小程序目录，用指向其中某个目录的 `bundleDirectory` 运行
`assembleMiniAppBundle`，然后在微信开发者工具中打开该目录。在 `project.config.json` 中使用你自己的
AppID，或开发者工具的测试号。

### 9. 你应该看到什么

fixture 的宿主页面渲染共享 greeting、计数器与 SDK 版本，其 Console 输出：

```text
fixture.greeting=Hello, WeChat, from commonMain
fixture.counted=5
fixture.sdkVersion=0.1.0-SNAPSHOT
fixture.result=PASS
```

这是真实宿主证据：该 fixture bundle 已在微信开发者工具基础库 **3.17.3** 中加载，并输出上述内容。它是对
*该 bundle 形态在该宿主上*的验收，不代表你的小程序、其他基础库、其他微信能力或其他宿主的结论。

`host/scripts/host-smoke.cjs` 在 Node 上执行同样的 `require()`，因此不必打开开发者工具也能检查模块接线。
那是接线检查，不是宿主验收，两者分开记录。

## 发布之前：今天你需要做什么

插件与 runtime SDK 都尚未发布到 Gradle Plugin Portal、Maven Central 或 npm，因此今天消费者需要本仓库源码。
区别只在于 Gradle 从哪里解析两个坐标：

| | 今天（源码检出） | 发布之后 |
| --- | --- | --- |
| 插件 `io.github.bobcgn.miniapp` | 由 `pluginManagement` 中的 `includeBuild("../kmp-miniapp-sdk")` 按 id 解析 | 由插件仓库按 id 与版本解析 |
| Runtime `io.github.bobcgn:kmp-miniapp-sdk` | 由 settings 级 `includeBuild` 替换 | 由仓库与版本解析 |

你的 `build.gradle.kts`、源码与宿主不受影响：build script 已经按 id 应用插件、从不声明 runtime artifact，
其中没有任何 module 路径、`build/` 目录或生成文件的引用。本文档不提供任何「可用的已发布版本」，因为那样的
版本并不存在。

## 插件刻意不做的事

- 它不渲染，并拒绝 runtime classpath 携带 Compose、Skiko 或浏览器 UI runtime 的构建，因为那样的 bundle 会
  构建成功却仍无法在宿主中运行。
- 它不生成 WXML 或 WXSS。
- 它不向任何仓库发布任何东西。
- 它不把业务配置 —— AppID、密钥、商户材料、模板 id —— 放进 Gradle。那些属于你的后端与宿主后台，而不是
  build script。
- 它不提供 Presentation Runtime。`UiState`、`Action`、`Store` 与 `Effect` 在本 SDK 中尚不存在；已有的以
  [PROJECT_FACTS-ch.md](docs/PROJECT_FACTS-ch.md) 为准，计划的见
  [ROADMAP-ch.md](docs/ROADMAP-ch.md)。

## 仓库结构

本仓库构建快速开始所消费的 SDK 与插件，并验证它们：

```shell
./gradlew projects
./gradlew clean check
./gradlew :kmp-miniapp-sdk:jsNodeTest
./gradlew :miniapp-gradle-plugin:test
./gradlew verifyMiniAppGradlePluginIntegration
./gradlew verifyMiniAppBundleSize
```

`buildMiniAppSdk` 是仓库内部任务，把 SDK 重新发布到 `examples/wechat-miniprogram`；该示例早于插件存在，
不是消费者工作流。维护者命令、消费者 fixture，以及为什么它的验证不属于 `check`，见
[开发指南](docs/DEVELOPMENT-ch.md)。

## 文档

- [当前项目事实](docs/PROJECT_FACTS-ch.md)
- [架构](docs/ARCHITECTURE-ch.md)
- [开发指南](docs/DEVELOPMENT-ch.md)
- [测试与微信宿主验收](docs/TESTING-ch.md)
- [微信能力矩阵](docs/platforms/wechat/WECHAT_CAPABILITIES-ch.md)
- [路线图](docs/ROADMAP-ch.md)
- [架构决策](docs/decisions/README-ch.md)

若文档过期，以可执行 Gradle 配置和源码为准。

## 许可证

待定。
