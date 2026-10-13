# kmp-miniapp-sdk

[English](README-en.md)

一个 Kotlin Multiplatform Mini App 平台：Gradle 插件为普通 Kotlin Multiplatform 项目提供 Mini App
source set、测试 source set、微信 source host、可分发的宿主 bundle 与独立 distribution，以及这些源码所编译依赖
的 Kotlin runtime。Mini App 是平台，**微信是当前 Host**，渲染由宿主负责。

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

版本 **0.1.1** 是当前最新的公开实验性版本。它增加了 Mini App distribution pipeline：
`initMiniApp` 创建微信 source host，`syncMiniApp` 驱动开发循环，`assembleMiniApp` 与 `verifyMiniApp`
产出并检查一个独立工程。0.1.0 引入的一切 —— source set、runtime 依赖、test run 与 `assembleMiniAppBundle`
—— 均未改变。在线解析方式见专门的[消费者接入指南](docs/CONSUMER_SETUP-ch.md)；以 SDK 为中心的 KMP 接入与依赖
说明见 [runtime SDK README](sdk/README-ch.md)。

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
| **Gradle 插件** `io.github.bobcgn.miniapp` | `miniapp` target 与 `miniappMain` / `miniappTest` source set、runtime 依赖、Node test run、`assembleMiniAppBundle`、`initMiniApp`、`syncMiniApp`、`assembleMiniApp`、`verifyMiniApp`、source host 内的 generated library area、renderer 边界检查、`miniapp { }` extension | 任何 runtime API、任何渲染、任何业务配置 |
| **Runtime SDK** `io.github.bobcgn:kmp-miniapp-sdk` | 你的 `miniappMain` 所编译依赖的 Kotlin API：共享 contract、宿主能力抽象、微信 adapter、错误与异步模型 | 渲染、WXML/WXSS、view tree、业务真相、认证或支付确认 |
| **微信宿主**（你拥有，由 `initMiniApp` 生成骨架） | `project.config.json`、`app.json`、页面、WXML、WXSS、require 并调用 bundle 的薄 JavaScript、`setData` 与事件转发 | 本 SDK 内部的任何东西 |
| **Presentation Runtime** | `UiState`、`Action`、`Store`、`Effect`、state machine、presentation logic —— **未实现，本文档也不展示任何相关 API** | 属于 P2 范围，见[路线图](docs/ROADMAP-ch.md) |

## 快速开始

以下步骤从一个普通 Kotlin Multiplatform 项目得到可用的 Mini App。这正是
[`fixtures/miniapp-consumer`](fixtures/miniapp-consumer) 每次验证所运行的流程。

这些 API 与脚本形态来自 fixture，并已作为 0.1.1 消费者流程发布。

### 0. 前置条件

- Kotlin Gradle Plugin 支持的 JDK（本插件在 JDK 17 上构建与测试）
- 能够访问 Gradle Plugin Portal 与 Maven Central
- 你自己的项目里有 Gradle Wrapper —— 消费者永远不需要本仓库的 wrapper

### 1. `settings.gradle.kts`

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

rootProject.name = "my-miniapp"
```

### 2. `build.gradle.kts`

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

这就是完整的 build script。没有 Kotlin/JS target 要声明、没有 source set 要创建、没有 `dependsOn`
要接线、没有 runtime artifact 要声明、也没有产物要复制。首次 Gradle 导入时，插件会在现有 common
source 目录同级创建 `src/miniappMain/kotlin` 与 `src/miniappTest/kotlin`。`miniappMain`、
`miniappTest`、它们与 `commonMain` / `commonTest` 的边、runtime 依赖与宿主任务全部来自插件。Kotlin 2.4.20 是插件构建与测试
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

### 7. 创建 Mini App source host

```shell
./gradlew initMiniApp
```

这会在你的 build script 旁写出 `miniApp/`：一个最小微信工程。它是**源码**，并且属于你 —— 插件只在文件缺失时
写入，绝不重写已存在的文件，因此第二次执行不会产生任何变化，你的修改是安全的。没有任何环节会隐式创建它：应用插件
不会，Gradle 配置不会，IDE 导入也不会。

```text
miniApp/
├── project.config.json          miniprogramRoot: "miniprogram/"
├── .gitignore                   让 generated library area 不进入版本控制
└── miniprogram/
    ├── app.js、app.json、app.wxss、sitemap.json
    └── pages/index/
        └── index.js、index.json、index.wxml、index.wxss
```

在 `project.config.json` 中填入你自己的 AppID —— 模板里是空值 —— 或使用开发者工具的测试号。

### 8. 开发循环

```shell
./gradlew syncMiniApp
```

`syncMiniApp` 编译你的 Kotlin 并把结果复制进 `miniApp/miniprogram/libs/`，因此循环是：修改 Kotlin →
`./gradlew syncMiniApp` → 在微信开发者工具中重新编译 → 查看模拟器。它是 `Sync` 而不是 `Copy`，因为 generated
library area 属于插件：离开 bundle 的 module 也必须离开你的 source host，否则宿主会继续加载构建已不再产出的代码。
它不会写入 `miniApp/` 中的其他任何位置。

页面以相对路径 require 编译后的 module，与在 distribution 中完全一致：

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

为篇幅做了删减：fixture 的处理函数还会打印每个值，并在失败时渲染错误而不是抛出。`require()` 是静态的，因此该文件名
是你页面的一部分：`initMiniApp` 会把插件的默认 module 名写进去，若构建设置了不同的 `outputModuleName`，改这一行
即可。

### 9. 验收产物

```shell
./gradlew assembleMiniApp
./gradlew verifyMiniApp
```

`assembleMiniApp` 在 `build/miniapp-dist/` 产出完整、独立的微信工程：你的 source host，去掉一切与机器绑定的内容，
再加上被放在 source host 相同相对位置的当前编译产物。它不要求先运行过 `syncMiniApp`，也从不写入 source host，
因此你用来验收的产物并不是你开发所用的工程。

```text
build/miniapp-dist/
├── project.config.json
└── miniprogram/
    ├── app.js、app.json、app.wxss、sitemap.json
    ├── pages/index/...            你手写的页面
    └── libs/                      注入进来的编译产物
```

复制过程中被排除的：`project.private.config.json`、`node_modules/`、`build/`、`.gradle/`、`.idea/`、`.git/`、
`*.iml`、`.DS_Store`，以及 distribution 自身的 `.gitignore`。被保留的：微信会打包的一切 —— 工程配置、页面、
WXML 与 WXSS、资源、编译后的 module、runtime SDK、Kotlin runtime、`package.json` 与 `.d.ts`。

`verifyMiniApp` 只读取组装好的 distribution，不读其他任何东西。它检查工程与 app 配置、解析 `miniprogramRoot`、
检查每个声明的页面都有入口文件、检查 library 中恰好有一个 module、一个 declaration 及其 npm manifest、解析
distribution 中的每一个 `require()`，并拒绝绝对路径、`node_modules` 以及回指原工程的引用。失败时会明确指出缺失的
文件或损坏的引用。

然后在微信开发者工具中打开 `build/miniapp-dist/`。

### 10. 你应该看到什么

fixture 的宿主页面渲染共享 greeting、计数器与 SDK 版本，其 Console 输出：

```text
fixture.greeting=Hello, WeChat, from commonMain
fixture.counted=5
fixture.sdkVersion=0.1.0-SNAPSHOT
fixture.result=PASS
```

这是真实宿主证据：该 fixture bundle 已在微信开发者工具基础库 **3.17.3** 中加载，并输出上述内容。版本行是当时的
历史值 —— `0.1.0-SNAPSHOT` 是那次运行时的版本目录取值，从当前工作树构建会打印 `0.1.1`；其余三行不变。它是对
*该 bundle 形态在该宿主上*的验收，不代表你的小程序、其他基础库、其他微信能力或其他宿主的结论。

`miniApp/scripts/host-smoke.cjs` 在 Node 上执行同样的 `require()`，因此不必打开开发者工具也能检查模块接线；
`assembleMiniApp` 会把它一并复制进 distribution，因此同一检查也能在那里运行。那是 Node 上的模块接线，不是宿主
验收，两者分开记录。

## 什么是源码，什么是产物

涉及四个目录，把它们中的任意两个混为一谈，正是这套布局要防止的错误：

| 路径 | 性质 | 由谁拥有 | `./gradlew clean` |
| --- | --- | --- | --- |
| `miniApp/` | 源码 | 你 | 不会删除 |
| `miniApp/miniprogram/libs/` | 生成物 | `syncMiniApp` | 不会删除；下次 sync 会重新填充 |
| `build/miniapp/bundle/` | 构建产物 | `assembleMiniAppBundle` | 删除 |
| `build/miniapp-dist/` | 构建产物 | `assembleMiniApp` | 删除 |

每个路径都可配置，且都有上述默认值：

```kotlin
miniapp {
    wechat {
        bundleDirectory.set(layout.buildDirectory.dir("miniapp/bundle"))
        sourceDirectory.set(layout.projectDirectory.dir("miniApp"))
        generatedLibraryDirectory.set(sourceDirectory.dir("miniprogram/libs"))
        distributionDirectory.set(layout.buildDirectory.dir("miniapp-dist"))
    }
}
```

它们在被写入前都会经过校验。`Sync` 会删除目标目录中不存在于来源的文件，因此 source host 必须留在你的工程内、
generated library area 必须严格位于 source host 内部、distribution 必须留在你的工程内。被拒绝的配置会在任何
复制或删除发生之前失败。

```shell
./gradlew assembleMiniAppBundle
```

它自 0.1.0 起未变，仍然把 compiler-managed bundle 写入 `build/miniapp/bundle`。它是 `syncMiniApp` 与
`assembleMiniApp` 所依赖的底层契约：两者重新发布的都恰好是 Kotlin/JS production library —— 编译器声明为输出的
那些 module —— 而不是写在插件里的路径，因此 Kotlin Gradle Plugin 的布局变化会带着 bundle 一起移动。

## 宿主与插件之间的 module 契约

- **CommonJS。** 编译产物是 CommonJS（`useCommonJs()`）。你的页面以相对路径 require module，module 以同样方式
  require 它的 runtime 兄弟模块，`package.json` 指定入口文件。`verifyMiniApp` 会解析 distribution 中的每一个
  `require()`，并拒绝任何非相对路径或指向外部的引用，因为 distribution 不携带 `node_modules`。
- **TypeScript 是宿主的选择，且微信开发者工具是唯一的 TypeScript 编译器。** 生成的工程是纯 JavaScript；需要
  TypeScript 的宿主自行添加 `.ts` 页面并打开开发者工具的 `typescript` 编译插件。插件不编译 TypeScript，因此只有
  一个编译器，不存在第二个需要同步的构建步骤。
- **`.d.ts`。** distribution 恰好携带一个 declaration —— 你的 module 的 —— 且它是自包含的：runtime SDK 导出的
  类型被内联其中。它是 TypeScript 宿主页面做类型检查的依据，`verifyMiniApp` 要求它存在并与 module 匹配。
- **Source map。** `*.js.map` 是编译器声明输出的一部分，generated area 与 distribution 都会携带它们，因为 `.js`
  文件通过 `sourceMappingURL` 引用它们。它们是调试元数据：其 `sources` 以相对路径指向原始 Kotlin 文件，因此在你
  的工程之外无法解析。`verifyMiniApp` 拒绝其中的绝对路径 —— 那会把产物钉死在构建它的机器上 —— 但接受相对源码
  路径，它们在运行时不起作用。
- **Node smoke 不是宿主验收。** 在 Node 上运行 bundle 只证明 module graph 可以解析；它运行在与加载你小程序的
  运行时不同的环境上。只有微信开发者工具，以及在能力需要时再加真机，才是宿主验收。

## 多模块根级微信工程（当前源码，尚未发布）

多模块消费者可在唯一的最终产物模块（例如 `shared`）配置根级宿主：

```kotlin
miniapp {
    wechat {
        rootHost.set(true)
    }
}
```

这保留模块中的 Kotlin 源码和 bundle，在消费者根目录提供 `miniApp/`，并将独立验收工程放到根级 `build/miniapp-dist/`。根级入口委托给选中的模块：

```shell
./gradlew initMiniApp
./gradlew syncMiniApp
./gradlew assembleMiniApp verifyMiniApp
```

初始化只显式运行且只补缺失文件；同步仅管理 `miniApp/miniprogram/libs/`；`clean` 保留手写宿主。此配置尚未包含在公开的 0.1.1 中。仓库的 [多模块消费者](fixtures/miniapp-root-consumer) 使用当前源码；完整路径保护和兼容说明见 [开发指南](docs/DEVELOPMENT-ch.md)。renderer 检查继续适用，根级目录不解决 CMP 资源兼容性。

## 在线解析

Gradle 从 Gradle Plugin Portal 解析插件 `io.github.bobcgn.miniapp:0.1.1`。插件随后添加
runtime `io.github.bobcgn:kmp-miniapp-sdk:0.1.1`，Gradle 从 Maven Central 解析它。消费者不需要本仓库源码、
`includeBuild`、内部 module 路径或手工复制的 artifact。完整独立项目说明见
[CONSUMER_SETUP-ch.md](docs/CONSUMER_SETUP-ch.md)。

## 插件刻意不做的事

- 它不渲染，并拒绝 runtime classpath 携带 Compose、Skiko 或浏览器 UI runtime 的构建，因为那样的 bundle 会
  构建成功却仍无法在宿主中运行。
- 它不生成 WXML 或 WXSS。
- 它不编译 TypeScript，不启动微信开发者工具，也不上传小程序。
- 它不发布消费者项目或宿主产物；仓库 release automation 属于维护者职责，不是插件行为。
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
