# 开发指南

[English](DEVELOPMENT-en.md)

## 环境要求

- 与仓库内置 Gradle 9.3.1 Wrapper 兼容的 JDK。项目没有固定 JDK toolchain；当前验证环境为 JDK 25.0.2。
- 仓库内置的 Gradle Wrapper。无需安装系统 Gradle，系统 Gradle 也不是构建事实源。
- Kotlin 2.4.20，由 Gradle 从 version catalog 解析。

在类 Unix 系统上始终使用 `./gradlew`，在 Windows 上使用 `gradlew.bat`。

## IDE

推荐使用 IntelliJ IDEA 开发 Kotlin Multiplatform、Gradle Kotlin DSL 和 Kotlin/JS。

VS Code 可用于处理 `examples/wechat-miniprogram` 下的文件。Consumer Bridge 的真实宿主验证必须使用微信开发者工具。

开始或关闭微信能力工作前，必须核对 [微信能力矩阵](platforms/wechat/WECHAT_CAPABILITIES-ch.md)。该矩阵区分已实现、部分实现、计划中、不支持和 P3 展现层能力，并记录每项能力所需的验证等级。

需要运行微信宿主验收时，使用 [微信真实宿主验证矩阵](platforms/wechat/WECHAT_HOST_VERIFICATION-ch.md) 选择最低环境并填写证据模板。不得用 DeveloperTools 结果代替真机、权限、隐私或后端链路证据。

## 命令

```shell
./gradlew projects
./gradlew clean build
./gradlew :kmp-miniapp-sdk:jsNodeTest
./gradlew :kmp-miniapp-sdk:jsTest
./gradlew :kmp-miniapp-sdk:checkArchitectureBoundaries
./gradlew :miniapp-gradle-plugin:test
./gradlew buildMiniAppSdk
./gradlew verifyMiniAppBundleSize
```

这些命令有效，并已于 2026-09-15 完成验证；其中 `:miniapp-gradle-plugin:test` 于 2026-09-17 完成验证。

发布流程 —— 唯一的版本来源、tag 规则、所读取的 GitHub Repository Secrets，以及发布只完成一半时如何恢复 —— 见
[RELEASING-ch.md](RELEASING-ch.md)。它属于维护者事务，从不属于消费者构建。

`:kmp-miniapp-sdk:jsTest` suite 覆盖 Host boundary、error model、callback adaptation、cancellation、double completion、可选 abort、微信 error mapping、包含取消时宿主中止的 HTTP transport adaptation、生命周期状态迁移、导航适配（含仅限 tabBar 的 `switchTab` 与其空白 route 拒绝）、capability support 状态与版本门控、包含并发请求合并的权限生命周期行为、包含拒绝分类的隐私授权、包含过期分类的微信会话检查、包含逐项门控的微信剪贴板与震动 adapter、包含沙箱与文本边界的微信文件系统 adapter、包含坐标校验与隐私前置条件的微信定位 adapter、包含不可归因中断分类与结果校验的微信扫码 adapter、包含请求边界、结果校验与中断分类的微信媒体 adapter、包含模板校验与逐模板结果策略的微信订阅消息 adapter、包含逐 collector 监听配对的网络状态 adapter、包含 abort、进度与超时行为的微信上传与下载 adapter、包含参数转发、空白参数拒绝、精确中断分类与单次终态行为的微信支付 adapter，以及 interop object construction。这些测试使用 fake callbacks，不代表能够访问真实 `wx` runtime。

## 架构边界

runtime SDK 共享客户端行为、宿主能力与 presentation state，从不渲染。[ADR 0011](decisions/0011-presentation-state-shared-rendering-host-native-ch.md) 固定了四层职责，责任矩阵见 [ARCHITECTURE-ch.md](ARCHITECTURE-ch.md)。

```shell
./gradlew :kmp-miniapp-sdk:checkArchitectureBoundaries
```

当 `sdk/**` 下任何内容导入 UI framework namespace 或声明 UI framework 依赖时，该任务会失败，并且它是 `:kmp-miniapp-sdk:check` 的一部分。它被有意设计为构建失败而不是评审提醒，因为「SDK 不依赖 Compose」这条不变量必须能在没有读过 ADR 的贡献者手中存活。

依赖匹配分两种：当某个 group 只为 UI framework 服务时按 group 匹配；当该 group 同时承载本 SDK 正当使用的依赖时，按精确 module coordinate 匹配。`org.jetbrains.kotlinx` 承载 `kotlinx-coroutines-core`，因此不能封禁整个 group；改为封禁 `kotlinx-html` 及其 `-js` / `-jvm` 变体这些 module。在把分类器用于真实模型之前，任务会先拿一张固定的期望结论表跑一遍，任何坐标被分类成与预期不符即失败，因此后续改动无法悄悄放宽或收紧规则。

该检查不做的事：它无法发现不导入任何 UI framework 却写出来的 renderer。一个 WXML generator，或用 SDK 自身类型拼装的 view tree，都能通过它。这类错误仍由评审与 `sdk/AGENTS.md` 规则拦截，而不是由构建拦截。

## Mini App Gradle 插件

`:miniapp-gradle-plugin` 由实现类 `io.github.bobcgn.miniapp.gradle.MiniAppGradlePlugin` 发布 `io.github.bobcgn.miniapp` 插件。把它应用到 Kotlin Multiplatform 项目会做四件事：

- 注册一个名为 `miniapp` 的 Kotlin/JS target —— 正是这次注册让 `miniappMain` 与 `miniappTest` 成为由 compilation 拥有、以 `commonMain` / `commonTest` 为父边的真实 source set；
- 在 common source 目录同级创建 `src/miniappMain/kotlin` 与 `src/miniappTest/kotlin`，使项目向导未生成它们时，Gradle 导入仍能显示可写目录；
- 注册该 target 的 Node.js test run —— 正是它让 `miniappTest` 拥有一个真正执行的任务；
- 把 runtime SDK 接入 `miniappMain` 的 api configuration —— 正是它让消费者代码无需声明 artifact 即可针对公开 API 编译。

runtime 是**公共 module 坐标** `io.github.bobcgn:kmp-miniapp-sdk:<version>`，因此插件从不写出本仓库的 project path —— 消费者构建无法访问它们，`project(":kmp-miniapp-sdk")` 对它不可用。runtime 的目录是 `sdk/`，project 名是 `kmp-miniapp-sdk`，因为该名字正是 composite build substitution 匹配、也是正式发布将要使用的 artifact id。`miniappTest` 通过 source-set hierarchy 继承该 runtime；其他 source set 与其他 target 都不会获得它。

### `miniapp { }` extension

插件注册一个名为 `miniapp` 的 extension。它配置的是当前宿主，而宿主目前唯一的设置是 bundle 的写入位置：

```kotlin
plugins {
    kotlin("multiplatform")
    id("io.github.bobcgn.miniapp")
}

miniapp {
    wechat {
        bundleDirectory.set(layout.buildDirectory.dir("miniapp/bundle"))
    }
}
```

| 字段 | 默认值 | 改变什么 |
| --- | --- | --- |
| `wechat.bundleDirectory` | `build/miniapp/bundle` | `assembleMiniAppBundle` 写入宿主 bundle 的位置 |

宿主块与字段都是可选的。`bundleDirectory` 默认为 `build/miniapp/bundle`，因此不做任何配置的消费者得到的行为与引入该 extension 之前完全一致。

**为什么需要 `wechat { }`。** Mini App 平台不是宿主：平台是源码与 runtime SDK 所针对的对象，宿主是加载 bundle 的 runtime。`MiniAppExtension` 是平台，`WeChatHostConfiguration` 是宿主。新增宿主意味着新增一个 `MiniAppHostConfiguration` 子类型并在 extension 上新增一个访问器 —— 既不是把 WeChat 那个加宽，也不是添加根级 `wechatXxx` 属性把平台写死成微信的形状。

**为什么可配置项这么少。** Gradle DSL 里的每个字段都是消费者必须做出的决定，因此这里只承载构建确实需要与宿主达成一致的内容。Kotlin/JS 输出形态、CommonJS、TypeScript declaration 与 renderer 边界都不是消费者的选择 —— 它们正是插件存在的理由，暴露它们等于把接线又放回消费者的 build script。该 extension 既不改变 `checkMiniAppHostBoundary`（平台级保证），也不改变宿主输出形态。

**有意不放在这里的内容。** AppID 与密钥、商户与支付材料、订单或签名数据、API token、用户身份、订阅消息模板 ID、权限状态、presentation state、页面 route、markup、View 定义，以及其他任何业务或后台配置。它们不是构建输入。Gradle extension 不是宿主控制台的副本；SDK 在构建期不为它们建模，理由与它在运行期不为它们建模相同。

**唯一的一条校验。** `bundleDirectory` 必须是项目内的目录。`assembleMiniAppBundle` 使用 `Sync`，会删除目标目录中 bundle 不包含的文件，因此项目之外的目录、或项目目录本身，会被删除而不是被组装进去。失败信息会指出字段、两个路径与期望值。写入项目内的小程序目录是允许的；放宽该规则需要有真实的外部用例。

### 宿主 bundle

插件把 Mini App target 的 Kotlin/JS 输出配置成宿主可消费的形态：在 Node.js test run 之上，再加 CommonJS library binary 与 TypeScript declaration。消费者不需要写 `useCommonJs()`、`binaries.library()`、`generateTypeScriptDefinitions()` 或 `nodejs()` 中的任何一项。

```shell
./gradlew assembleMiniAppBundle
```

该任务把 Kotlin/JS production library 重新发布到 `build/miniapp/bundle/`。它使用 `Sync`，因此一个从 compilation 中消失的 runtime 模块也会从 bundle 中消失；它复制的是 distribution task 的**已声明输出**而不是手写路径，因此 Kotlin Gradle Plugin 的目录布局变化会让 bundle 跟着移动，而不是悄悄变空。它只读写 Mini App target 的范围，不触碰其他任何东西。

对于一个 `miniappMain` 使用 runtime SDK 的项目，bundle 形如：

```text
build/miniapp/bundle/
  <project>-miniapp.js        消费者入口模块：其编译后的代码与 @JsExport 声明，
                              并 re-export runtime 的 namespace、require 下面的 runtime 模块
  <project>-miniapp.d.ts      薄宿主代码据以编译的 declaration
  kmp-miniapp-sdk-kotlin.js   runtime SDK，作为独立模块
  kotlin-kotlin-stdlib.js     该 compilation 解析出的 Kotlin runtime
  kotlinx-coroutines-core.js
  kotlinx-atomicfu.js
  kotlin_org_jetbrains_kotlin_kotlin_dom_api_compat.js
  package.json
  *.js.map                    外部 source map，供宿主侧本地调试
```

契约承诺的内容：消费者入口模块、其 TypeScript declaration、runtime SDK 模块、该 compilation 解析出的 Kotlin runtime 模块，以及 `package.json`。契约**有意不承诺**固定的 runtime 模块列表：那一组取决于解析结果，SDK 自身依赖变化时它也会变化；宿主加载入口模块以及该模块 require 的一切。

有两点值得明说，因为它们与人直觉不符：

- **Kotlin/JS library 只导出 `@JsExport` 声明，其余一律被消除。** 因此消费者的宿主侧表面就是其 `miniappMain` 中自己的 `@JsExport` 声明；没有被导出的代码永远不会进入 bundle。测试 fixture 之所以给被断言的对象加上导出，正是这个原因。
- **runtime SDK 是独立模块，而不是被内联进消费者模块**，因为 runtime 声明了自己的 Kotlin/JS 输出模块名。因此 bundle 中有多个 JavaScript 文件需要宿主加载，入口模块会 require 其余部分。

### Mini App source set 的架构前置条件

`miniappMain` 继承 `commonMain`，因此 common source set 中的内容都会进入 Mini App compilation。它可以继承共享行为、model 与 state，但**不得**继承 Compose UI、Skiko 或浏览器 renderer：Compose UI 必须位于不作为 `miniappMain` 父 source set 的客户端 module 或 source set 中，例如 Compose 客户端应用自身的 UI source set。

插件把这一点作为约束强制，而不只是写在文档里：

```shell
./gradlew checkMiniAppHostBoundary
```

该检查解析 `miniappRuntimeClasspath` —— 正是 distribution 据以构建的那一组依赖 —— 并在其携带 Compose、Skiko、Compose 专用的 AndroidX 集成 artifact、`kotlinx-browser` 或 `kotlinx-html` 时失败。非 Compose 的 lifecycle、saved-state 与 navigation primitives 仍被允许。它在 `assembleMiniAppBundle` 之前运行，因此不会产出任何宿主都无法运行的 distribution。它是**依赖**规则而不是文件规则：Kotlin/JS distribution 中的文件通过 `require()` 彼此引用，删除看起来像 renderer 的文件只会把构建失败变成加载失败。

若消费者在应用插件前已存在 Kotlin/JS target，可能需要执行一次 `./gradlew kotlinUpgradeYarnLock`。应用插件会新增一个 target，从而改变 npm 依赖集合，而 Kotlin Gradle Plugin 不会自行覆盖已有的 yarn lock。

**以上任何一项本身都不代表**宿主能够加载该 distribution；任务成功与边界检查都不是宿主证据。该证据单独记录：消费者夹具 distribution 已在微信开发者工具基础库 3.17.3 中加载并报告 `fixture.result=PASS`。

### 版本来源

`gradle/libs.versions.toml` 保存 runtime SDK 与本插件共享的唯一版本。插件构建据此生成 `miniapp-plugin-metadata.properties`，插件运行期从该资源读取坐标，因此插件源码与测试中都不含版本字面量。

公开的 0.1.0 消费路径从 Gradle Plugin Portal 解析插件，并从 Maven Central 解析 runtime 坐标。仓库 fixture 有意保留 composite substitution，以便测试当前工作树。无法解析 runtime 时消费者会看到 Gradle 指明该坐标的依赖解析错误；插件有意不回退到其他版本、其他 artifact 或 project path。

插件自身不创建任何 source-set 模型与 `dependsOn` 边：手工搭建的正是 [ADR 0010](decisions/0010-miniapp-gradle-plugin-source-set-model-ch.md) 否决的「未使用 source set」模型。它只在磁盘上创建两个标准 Kotlin 目录；重复应用会保留现有文件。把它应用到未应用 Kotlin Multiplatform 插件的项目会失败，并给出指明缺失插件的错误信息。

```shell
./gradlew :miniapp-gradle-plugin:test
```

插件测试是分层的：contract 测试覆盖 descriptor、公共坐标、extension 形状与 renderer 分类器；TestKit fixture 覆盖只有在真实构建中才存在的行为；消费者 fixture 覆盖外部项目所走的路径。运行全部内容只有一个入口，见 [TESTING](TESTING-ch.md#gradle-插件集成套件入口)。

```shell
./gradlew --no-daemon --console=plain verifyMiniAppGradlePluginIntegration
```

若消费者的 `commonMain` 依赖另一个 Kotlin Multiplatform project，则必须对该 project 同样应用本插件。该依赖经由 Mini App variant 解析，不提供该 variant 的 project 会解析失败；插件不提供回退，把依赖移出 `commonMain` 也不是可接受的变通。

测试由 Gradle TestKit fixture 与契约测试组成。fixture 有意把 Kotlin Gradle Plugin 与被测插件放在同一个 buildscript classpath 上：`withPluginClasspath()` 只把被测插件注入 plugin-resolution classpath，因此单独解析第二个插件的 fixture 无法复现真实消费者构建给插件的 classpath。其中一个 fixture 带有 `commonMain`、`miniappMain`、`commonTest` 与 `miniappTest` 源码，并断言实际执行出的测试报告，因此 source set 接线与测试执行都是由运行测试证明的，而不是靠读模型。fixture 经 composite build（`includeBuild` 本仓库）消费 runtime，以便测试当前源码而不是已经发布的旧 binary；插件自身永远看不到该路径。

extension 只承载一个设置 —— 宿主 bundle 的写入位置。业务与宿主配置按设计留在 Gradle 之外，理由见上文 `miniapp { }` extension 一节。

## 包体积

`build-logic/` 是一个 included build，存放本仓库自己的构建逻辑：包体积任务。它只被 include，从不发布，消费者也不会应用它。

```shell
./gradlew verifyMiniAppBundleSize
./gradlew updateMiniAppSizeBaseline
```

`verifyMiniAppBundleSize` 测量 SDK 生产分发 —— 逐文件、逐分类与合计的原始与 gzip 字节 —— 写出 `build/reports/miniapp-size/bundle-size.json`，并与提交在 `docs/performance-baseline.json` 的基线比较。它只在某个交付分类的增长同时超过 4 KB 下限与 5% 比例时才失败，因此编译器以几字节差异重新生成某个文件不会打断构建。`updateMiniAppSizeBaseline` 重写该基线；它是一次维护者操作，其 diff 按普通改动评审。

用同一任务测量其他分发 —— 微信宿主的，或某个消费者 bundle —— 只需指向它：

```shell
./gradlew verifyMiniAppBundleSize -PminiappSizeDirectory=examples/wechat-miniprogram/miniprogram/libs
```

测量对象、分类、gzip 规则、阈值及其依据、以及编译器自身的不稳定性，记录在 [PERFORMANCE_BASELINE-ch.md](PERFORMANCE_BASELINE-ch.md)。

它刻意不接入 `check`：尺寸任务依赖分发任务，接入后每次普通构建都会重新编译 Kotlin/JS 并重新测量，而一个针对有意体积变化的阈值会在那时阻断无关工作。CI 显式调用它，与调用 `verifyMiniAppGradlePluginIntegration` 的方式一致。

## Mini App Gradle 插件模型 PoC

`poc/kgp-model` 是一个独立 Gradle build，用于对比 `miniappMain` / `miniappTest` 的 Kotlin Gradle Plugin 模型。根构建不包含它，因此它不会作为 SDK 构建的一部分运行。请在它自己的目录下使用仓库根目录内置的 Wrapper 执行：

```shell
cd poc/kgp-model
../../gradlew --no-daemon --console=plain clean check
../../gradlew --no-daemon --console=plain :model-c:miniappTest
../../gradlew --no-daemon --console=plain :model-c:checkMiniAppModel
../../gradlew --no-daemon --console=plain :model-a:reportKgpModel
```

各模块：

| 模块 | 说明 |
| --- | --- |
| `model-a` | 由消费者 build script 声明的命名 Kotlin/JS target。对照实验；作为交付模型被否决。 |
| `model-b` | 背后没有任何东西的自定义 source set。复现「未使用 source set」失败形态。 |
| `model-c` | 拥有 Kotlin/JS target 的 PoC 插件（`io.github.bobcgn.miniapp.poc`）。选定模型。 |
| `model-c2` | 保留内部 `js` target 却仍试图暴露 `miniappMain` 的 PoC 插件。复现被否决的隐藏 target 变体。 |

`reportKgpModel` 打印模块的模型形状；`checkMiniAppModel` 断言十项模型要求，任一项不满足即失败。`model-b` 与 `model-c2` 预期会在该检查中失败 —— 它们是被否决方案的实际执行证据，而不是通过模块。`model-a` 不满足的唯一一项要求是「消费者 build script 不包含手工 Kotlin/JS 接线」。

该决策、被否决的替代方案以及 Kotlin Gradle Plugin 的限制记录于 [ADR 0010](decisions/0010-miniapp-gradle-plugin-source-set-model-ch.md)。PoC 是实验性工具：它不随 SDK 发布，也不定义任何 SDK 公共 API。

## 消费者集成夹具

`fixtures/miniapp-consumer` 是与本仓库并列的普通 Gradle build，而不是本仓库的一个 module。它的存在是为了按外部项目的方式证明消费者路径：插件经 `pluginManagement { includeBuild }` 按 id 解析，runtime 以公共坐标 `io.github.bobcgn:kmp-miniapp-sdk` 到达，其中没有任何一处引用内部实现。

```shell
cd fixtures/miniapp-consumer
../../gradlew --no-daemon --console=plain clean miniappTest assembleMiniAppBundle verifyConsumerContract
```

它通过实际运行证明：

| 证据 | 来源 |
| --- | --- |
| 插件按 id 解析且无需版本号 | 在一个从未见过本仓库的构建里写 `plugins { id("io.github.bobcgn.miniapp") }` |
| `miniappMain` 与 `miniappTest` 存在并可编译 | `src/miniappMain`、`src/miniappTest` |
| `miniappMain` 复用 `commonMain` | `hostGreeting()` 调用 `src/commonMain` 中的 `greeting()` |
| `miniappTest` 运行自身测试**以及**共享测试 | `build/test-results/miniappNodeTest/` 下有 `consumer.SharedTest`（2 个）与 `consumer.MiniAppTest`（3 个） |
| runtime SDK 自动解析 | `sdkVersion()` 调用 `MiniAppSdk.VERSION` |
| bundle 满足其契约 | `verifyConsumerContract` 任务 |
| DSL 真实生效 | 同一任务断言配置的目录，宿主运行则传入 `-PminiappBundleDirectory=host/miniprogram/libs` |

消费者的 build script 中没有 source set 声明、没有 `dependsOn`、没有 `useCommonJs()`、没有 `generateTypeScriptDefinitions()`、没有 runtime artifact 坐标、也没有复制步骤。 新用户的入口是 README，其快速开始取自本 fixture；当两种语言发生漂移、README 引用的路径消失、或被记录的消费者命令缺失时，`verifyMiniAppConsumerDocs` 会失败。除两个插件外它只声明了一件事：`commonTest` 的 `kotlin-test`，这是每个 Kotlin Multiplatform 项目都会做的。

### 从仓库运行

```shell
./gradlew --no-daemon --console=plain verifyMiniAppConsumerFixture
```

它会以三种方式运行该夹具：默认 bundle 目录、宿主目录，以及宿主自身的消费路径。这些任务未接入 `check`，因为它们会编译 Kotlin/JS 并安装 npm 依赖。

### 微信宿主

`fixtures/miniapp-consumer/host` 是一个最小微信小程序：WXML、WXSS 与调用组装产物的薄 JavaScript。它属于 Host UI，因此 markup 留在这里，永不进入 SDK 或夹具的共享 Kotlin。

其 `miniprogram/libs/` 是构建产物并被 Git 忽略；在微信开发者工具中打开前请先运行 `verifyMiniAppConsumerHostBundle`。该夹具已完成基础库 3.17.3 的开发者工具验收：页面渲染 `Hello, WeChat, from commonMain`、`5` 与 `0.1.0-SNAPSHOT`，Console 报告 `fixture.result=PASS`。宿主另有一个 Node smoke 检查，以与页面完全相同的方式加载该 bundle：

```shell
cd fixtures/miniapp-consumer/host
node scripts/host-smoke.cjs
```

那是在 Node 上的模块接线检查，不是宿主验收；两者分别记录。

## Consumer Bridge

构建并准备微信 integration host 所需的全部产物：

```shell
./gradlew buildMiniAppSdk
```

该任务会构建 Kotlin/JS production library 与 TypeScript declaration，再将所需模块同步到示例。稳定的编译器输出名称为：

```text
sdk/build/dist/js/productionLibrary/kmp-miniapp-sdk-kotlin.js
sdk/build/dist/js/productionLibrary/kmp-miniapp-sdk-kotlin.d.ts
sdk/build/dist/js/productionLibrary/kotlin-kotlin-stdlib.js
sdk/build/dist/js/productionLibrary/kotlinx-coroutines-core.js
sdk/build/dist/js/productionLibrary/kotlinx-atomicfu.js
```

示例使用独立的 consumer-facing 副本：

```text
examples/wechat-miniprogram/miniprogram/libs/
```

该目录包含两个手工维护的 normalization 文件：`kmp-miniapp-sdk.js` 与 `kmp-miniapp-sdk.d.ts`。`buildMiniAppSdk` 会保留这两个文件，并同步编译器模块、类型声明和三个 runtime modules。目录内其余文件均视为任务管理的产物，不要放置无关文件。

外部 `.js.map` 文件会随每个 JavaScript module 生成并复制，用于本地微信调试。它们包含嵌入的源码、可重复生成，并被 Git 忽略。JavaScript 与 `.d.ts` 分发文件继续纳入版本控制，使示例可直接打开；`buildMiniAppSdk` 是刷新产物的唯一事实路径。

执行本地消费端检查：

```shell
cd examples/wechat-miniprogram
npm install
npm run smoke
npm run typecheck
```

smoke test 加载 consumer-facing CommonJS 模块、调用 `sdkVersion()`，并安装 fake global `wx` 验证 Storage、微信 login bootstrap、HTTP transport、lifecycle 转发、四种导航调用、运行时能力检测、权限生命周期、隐私授权、微信会话检查、剪贴板与震动能力、文件系统、定位、扫码、媒体选择、订阅消息请求、网络扩展（网络状态、上传与下载）与标准支付。它还会断言 HTTP transport 交给 fake host 的内容，包括原始文本响应模式、导航收到的绝对页面路径、`switchTab` 交给宿主的完整 option bag，以及每个纳入门控的能力所报告的支持状态。该测试还会断言扫码、媒体选择、订阅消息请求与每个网络扩展都不查询、不请求任何权限，覆盖交互中断与相似失败文本的分类，确认宿主自身的状态行永远不会被当作模板 ID，并验证支付只以宿主接受的五个字段到达宿主、其 resolve 值不携带任何订单字段，验证网络状态观察会注册一个 listener 后将其移除、以及中止传输会只停止一次宿主 task 并清理其进度 listener。该测试验证 module 与 adapter behavior，但不构成真实 Host 证据。TypeScript 使用 strict mode，并在不依赖 `any` 的情况下验证 Promise-based Storage、typed `WeChatLoginResult`、HTTP transport、lifecycle、导航、capability support、权限、隐私、会话检查、剪贴板、震动、文件系统、定位、扫码、媒体选择、订阅消息请求、网络状态、传输与支付 exports。

真实宿主验证按 [TESTING-ch.md](TESTING-ch.md) 中的检查清单执行，该清单是页面取值、console 输出与准备步骤的权威来源。只有在完成该运行后，某项 capability 才会在 [PROJECT_FACTS-ch.md](PROJECT_FACTS-ch.md) 中被记录为已通过宿主验证。

Consumer Bridge 与 Storage capability 已于 2026-09-14 在微信开发者工具 Stable 2.01.2510290 中通过用户提供的视觉证据完成验证。页面显示 `0.1.0-SNAPSHOT`，Storage 状态为 `PASS`，详情为 `first=first, overwritten=second, missing=null`。

Authentication 的真实宿主验收已于 2026-09-14 通过用户提供的微信开发者工具证据完成。页面显示 `Client login code: PASS`、`codeReceived=true` 和 code 长度 32，且没有显示凭证本身。示例不会记录或渲染 raw credential。`wx.login` code 是短期凭证，必须发送到可信的消费者后端与微信交换；取得 code 不代表用户已认证，不会创建 SDK session，也不能授权请求。

HTTP transport capability 已于 2026-09-14 通过用户提供的微信开发者工具确认完成真实宿主验收。其 adapter、error mapping 与 cancellation 行为仍由 Kotlin/JS、fake-host、CommonJS 与 TypeScript 自动检查覆盖；真实宿主运行确认 index 页面上的 network 卡片可成功到达 `wx.request`。

Lifecycle 与导航桥接已完成规定的宿主验收。2026-09-15 的开发者工具运行覆盖前台 lifecycle 状态、当前页面 route，以及 `navigateTo` 打开第二页、`redirectTo` 替换为第三页、`navigateBack` 直接返回首页的完整页面栈路径。2026-09-18 的开发者工具与 Android 真机进一步覆盖 `switchTab`：目标 tab 报告 `SHOWN`，切走时报告 `HIDDEN` 而不是 `UNLOADED`，没有对应 tab 的 route 只输出封闭的 `HostFailure` 分类。后台状态迁移仍需要真机将小程序切入后台；这是 App lifecycle 证据的剩余限制，不影响已完成的导航验收。

Node/CommonJS smoke test 或 TypeScript check 通过，并不能证明微信小程序集成通过。必须分别记录两层结果。

运行时能力检测已于 2026-09-15 完成开发者工具（基础库 3.17.2）与 Android 真机（OnePlus PLQ110、Android 36、微信 8.0.76）验收，两种环境均报告 `runtime-detection=Supported`、`storage=Supported`、`ungated=Unsupported`。开发者工具当前可选的最低调试基础库为 2.21.4，无法构造低于 2.20.1 的宿主，因此 `VersionDependent` 由自动化测试覆盖而不是真实宿主截图；各情形的构造方式见 [TESTING-ch.md](TESTING-ch.md) 中的检查清单。

### 微信隐私配置

微信针对小程序已声明的个人信息接口把关其自身的隐私协议，与系统权限分开。有两件事属于小程序后台，本 SDK 既不能代做也无法替代：

- 收集类型必须在 MP 后台「设置 → 服务内容声明 → 用户隐私保护指引」中声明。未声明任何类型的小程序会被报告为无需授权，因此读到 `NOT_REQUIRED` 并不证明用户曾经同意过。
- 自基础库 2.32.3 起宿主才会拦截隐私相关调用。低于该版本宿主根本不拦截，因此本 capability 报告 `UnsupportedCapability`，而不是假装该条件不存在。

SDK 只做告知与把关；它不生成隐私政策、不判断业务是否合规，也不提供法律意见。

要在微信开发者工具中观察隐私状态，打开 index 页面并使用 Privacy Authorization 卡片：`Refresh privacy status` 只查询宿主、无任何副作用，只有 `Request privacy authorization` 按钮会启动弹窗。调试基础库需为 2.32.3 或更高，卡片才能到达宿主。

已记录的答案属于账号、设备、后台配置与该账号在小程序中的历史，因此同一份构建在不同账号上可能报告 `REQUIRED` 或 `NOT_REQUIRED`。要再次看到 `REQUIRED`，请在开发者工具缓存中清除该账号的同意记录，或更换账号；不要为了制造状态而破坏性地重置设备或账号。

隐私与权限的验收在验证矩阵中分别记录，因为宿主对二者分开跟踪，其中一项的结果不能说明另一项。

权限生命周期已于 2026-09-15 完成麦克风权限以及位置权限的开发者工具与 Android 真机验收。麦克风运行覆盖 `Granted`、`Denied`、设置页返回以及拒绝后的再次请求；位置运行额外覆盖宿主中的 `NotRequested`、显式授权后的 `Granted` 与拒绝后的 `Denied`。自动化套件仍然从不请求权限，因为真实弹窗需要用户手势。

微信会话检查已于 2026-09-15 通过 Android 真机验收：OnePlus PLQ110、Android 36、微信 8.0.76、基础库 3.17.3 [1641]。Console 在 login bootstrap 取得新 code 前报告 `check #1, state=Invalid`，`wx.login` 成功后连续报告 `state=Valid`。该结果仍不是身份的证明，会话检查与 login bootstrap 继续分别记录。

微信剪贴板与震动已于 2026-09-15 完成真实宿主验收。开发者工具（基础库 3.17.2）与 Android 真机均验证剪贴板写入、读回与 `matched=true`；OnePlus PLQ110、Android 36、微信 8.0.76、基础库 3.17.3 [1641] 验证短震动与长震动调用均 PASS，且测试者确认实际感知两种震动。`getClipboardData` 不属于 `app.json.requiredPrivateInfos` 允许的字段，示例不声明它。

### 微信定位配置

`wx.getLocation` 比本项目任何其他 capability 都需要更多的后台准备，且这些都无法由 SDK 代做或替代：

- 小程序类目必须是微信允许定位的类目，且需在「开发 → 开发管理 → 接口设置」中开通该接口。
- 2022-07-14 之后发布的版本，`app.json` 必须在 `requiredPrivateInfos` 中列出 `getLocation`，并在 `permission.scope.userLocation.desc` 中说明用途；示例两者都已配置。
- 用户必须授予 `scope.userLocation`。

微信自带的模拟器通过 IP 定位且仅支持 `gcj02`，因此模拟器结果不能作为真机真实位置的证据。其调用频率规则在不同版本类型间也有差异，因此同一会话中的第二次调用可能返回第一次的位置而非新位置。

页面不得自行读取位置：示例的 Location 卡片只在点击后调用，且从不显示或记录坐标。

微信定位已于 2026-09-15 完成开发者工具与 Android 真机验收。运行覆盖能力为 `Supported`、权限由 `NotRequested` 到 `Granted`、拒绝后在触及 `getLocation` 前报告 `DENIED`，以及恢复权限后再次定位成功；成功日志只报告坐标与精度是否有效，不记录实际经纬度。当前仅实现按需单次 `getLocation`，不包含 `chooseLocation` 或 `openLocation`。

### 微信扫码的不可归因中断

`wx.scanCode` 打开的是微信自身的扫码界面，因此 SDK 不实现界面，也不建模相机。示例的 Scanner 卡片只在点击后调用，且从不显示或记录扫码内容、`rawData`、字符集或图片路径。

真机验证证明主动关闭扫码界面与系统相机权限阻止界面启动都会产出宿主的 cancel 信号。因此 SDK 不再把该信号解释为用户意图或权限拒绝，而是以 `HostInteractionInterrupted` 报告“交互没有结果且原因不可判定”。只精确匹配 `scanCode:cancel` 与 `scanCode:fail cancel`；相似但不同的消息保持为 `HostFailure`。

开发者工具不是真机：其扫码实现是让用户选一张图片再解码，因此相机路径、`onlyFromCamera` 的实际行为与真机取消文本都必须在真机上确认。

该能力不查询也不请求任何权限。`scope.camera` 确实存在于该宿主，但没有任何可引用来源把它与 `wx.scanCode` 绑定，因此 SDK 既不为它弹窗，也不把它登记为权限前置条件，示例也不向 `app.json.requiredPrivateInfos` 添加任何字段。

页面不得自行读取扫码内容：示例只报告内容是否存在、宿主报出的格式是否被 SDK 识别，以及取消与失败的区别。

微信扫码已在开发者工具与 Android 真机覆盖 `Supported` 和成功扫描；真机还证明主动取消与相机受限产生相同的不可归因中断，因此该能力不声称能区分二者。它在能力矩阵中记为 `Stable`：实现、自动化测试与规定的宿主验证证据三者齐备，而宿主的不可区分性由错误模型显式保留，而不是被描述成宿主并不做出的区分。

### 微信媒体选择

`wx.chooseMedia` 打开的是微信自身的选择界面，因此 SDK 不实现界面，也不建模相机。示例的 Media 卡片只在点击后调用，且从不显示或记录所选内容或媒体所在的临时路径。

SDK 只校验微信能精确陈述的部分，其余一律拒绝：`mediaType` 必填，因为微信自己的 schema 把它标为 required；`maxDurationSeconds` 必须落在微信文档给出的 3 至 60 秒内；字节数必须是完整的非负数；临时路径必须是非空字符串。宿主的 `count` 上限刻意不在 SDK 里复制一份，因为它取决于基础库，因此 SDK 只按调用方要求发送，并如实报告返回结果。

已安装的开发者工具基础库在模拟选择器关闭时报告 `chooseMedia:cancel`，Android 真机在主动关闭时报告 `chooseMedia:fail cancel`；两者都没有结构化原因。因此这两条精确信号映射为 `HostInteractionInterrupted`，但不推断用户意图；其他失败保持为 `HostFailure`。在已验证的 Android 宿主上，限制媒体访问与主动关闭产生相同的不可归因中断，因此 SDK 不虚构宿主无法支持的权限分类。

该能力不查询也不请求任何权限：微信自身的选择界面不需要权限，宿主的 scope 列表里也没有读取媒体库的 scope。写入相册是另一项能力，有它自己的 API（`saveImageToPhotosAlbum` 与 `saveVideoToPhotosAlbum`）；本轮未实现它，因为本任务没有该需求，也没有可引用来源把 `scope.writePhotosAlbum` 与它绑定。

选择返回的路径是宿主临时资源。SDK 不保留副本、不声称其生命周期，因此之后需要这份媒体的调用方必须自行复制到自己拥有的存储中。

微信媒体选择已于 2026-09-16 完成真实宿主验收。自动化检查、开发者工具与 Android 真机覆盖了 `wechat.choose-media=Supported`、图片、视频、混合、拍摄、主动关闭与媒体访问受限路径，且未记录媒体内容或完整临时路径。该宿主上的主动关闭与受限访问产生相同的不可归因中断；由于这项歧义被显式保留，该能力为 `Stable`。

### 微信订阅消息请求

`wx.requestSubscribeMessage` 把微信自身的弹窗放到用户面前，因此 SDK 不实现弹窗、也不发送消息。示例的 Subscription Message 卡片只在点击按钮时请求宿主，并且只有在本地配置了测试模板时才会请求；未配置时报告 `NOT CONFIGURED`，完全不调用宿主。模板 ID 是归属于小程序账号的宿主标识，因此仓库里不写入任何真实值：卡片中的列表在仓库中为空，页面也从不显示或记录 ID。

**该能力是本项目证据最薄的一处，代码形态正源于此。** 已安装开发者工具所带的基础库在元数据表中声明了该 API——选项 `tmplIds`，以及以模板 ID 为键、旁边带 `errMsg` 的 success 结果——但其中没有该 API 的实现，也没有文档 schema。因此可离线来源没有给出该 API 的状态词汇，也没有给出它的关闭消息。整个 bundle 中唯一观察到的状态字符串是模拟器订阅弹窗预览载荷里的 `accept`，那属于弹窗渲染数据而不是 API 结果。SDK 只识别这一个状态，并原样保留其他非空白状态。目前不分类任何关闭信号：在该 API 产出真实宿主证据之前，类似 cancel 的失败仍为 `HostFailure`。示例只记录封闭的诊断标签，从不记录原始消息或模板 ID。

SDK 校验模板与响应关联，而不猜测答案词汇：ID 必须非空白，重复项折叠并保留调用方顺序，不施加数量上限，因为可离线来源没有给出上限。响应必须精确包含所请求的模板键，且每项都是非空白文本状态；缺少、多出、空白或非文本条目均为 `InvalidResponse`。

用户手势由调用方负责，而且这是硬性要求：微信在没有手势时会拒绝该请求。SDK 与示例都不会在加载时触发它，也不会重试一次拒绝。

该能力不查询也不请求任何权限，因为没有可离线来源把某个 scope 或隐私条件与该 API 绑定。这是关于证据的结论，不等于认定其不存在，因此人工运行需要观察是否出现弹窗而不是预先假设任一方向；BOB-60 的隐私工作尚未完成，既不表示这里一定有条件，也不表示一定没有。也没有添加任何 `app.json` 声明。

「同意」是订阅状态，永远不是送达。端到端送达需要微信后台模板与可信后端，属于 `BackendRequired`，也超出任何 adapter 测试所能证明的范围。

微信订阅消息请求已有自动化覆盖与部分真机证据。2026-09-16 的 Android 运行验证了运行时支持与 `templateCount=0` 保护。该保护有意不调用宿主，因此没有出现订阅同意弹窗是预期结果，不是权限失败。同意、拒绝、关闭、多模板关联与消息送达仍**阻塞**于为同一 AppID 配置的有效测试模板。该能力为 `Partial`。

### 微信网络扩展

这里适配五个宿主 API：网络类型查询、网络状态变更监听，以及文件上传与下载。契约取自已安装开发者工具所带的基础库：它枚举了查询与事件各自的连接类型、两个传输的选项与结果字段、两种 task 的成员，以及它自身错误映射产出的超时消息。

网络状态成为公共 capability，而两个传输没有，区别在模型而不在名字。宿主总能回答「是否在线、通过什么链路」，而回答它不需要任何宿主专属内容；上传与下载建立在宿主自身文件系统的路径之上，而本 SDK 刻意没有可移植的文件引用，因此把它们提升为公共能力，等于为唯一一个实现凭空发明一个。传输中可移植的那一半——method、URL、headers、status、文本 body——已经属于 transport capability。

查询与监听分开门控，因为宿主可能能回答该问题却不提供事件；监听还要求两半都在：只能注册而无法移除会让 listener 泄漏到小程序的整个生命周期，因此只有 `on` 的宿主被报告为不支持。`changes` 的每个 collector 注册自己的宿主 listener，并在结束时移除，这使得每条终止路径上的配对都是精确的——包括一个畸形事件，它会以 `InvalidResponse` 结束该 collector，而不是让这条流对宿主的实际情况撒谎。这里不做轮询，也不在开始收集时合成任何读数；需要起点的消费者去调用查询。

传输 adapter 拥有三项单纯的 suspend 调用无法提供的东西：abort、进度与清理。`abort()` 最多停止宿主 task 一次，并报告是否真的触发了宿主 abort，因此没有返回 task 的宿主永远不会被描述成「已停止」。取消正在等待传输的协程同样会停止宿主 task，因为一个不再等待的调用方并没有要求它继续运行。进度 listener 最多注册一次——当 task 无法报告进度时完全不注册，因为一次永远配不到移除的注册就是泄漏——并在成功、失败与 abort 时一律被移除。进度是咨询性的：SDK 读不懂的数值会被忽略，而不会让一个正常工作的传输失败；没有报告进度的传输没有进度值，而不是 0%。

已完成的传输就是一次已完成的 exchange，因此非 2xx 状态会 resolve，与 transport capability 完全一致。超时是宿主确切的消息 `<api>:fail timeout`；仅仅提到 timeout 的消息仍然是 `HostFailure`。

这些 API 都不索取权限。访问某个 host 需要消费者把它列入 request domain，那是对小程序的配置约束而不是 SDK 能请求的东西；可离线来源也没有把任何隐私条件与它们中的任何一个绑定。

网络扩展目前仅有自动化覆盖。真实宿主验收尚未完成，且其中两半无法由自动化产出：真实传输需要一个位于 request domain 列表中的受控 HTTPS 服务（属 `BackendRequired`，也是示例在未提供之前报告 `NOT CONFIGURED` 的原因），而真实的网络切换需要一台能够真正切换连接的真机，任何模拟器都无法替代。WebSocket 未实现，在能力矩阵中保持 `Planned`。


### 微信标准支付

支付被实现为类型化转发器，而不是 SDK 能够自行推理的能力。契约读取自已安装开发者工具所带的基础库：`requestPayment` 恰好接收 `timeStamp`、`nonceStr`、`package`、`signType` 与 `paySign`，把 `signType` 限制为 `MD5` 与 `HMAC-SHA256`，并声明 success 形状为空；该库自身的支付流程把交互结束报告为 `requestPayment:cancel`。

交给宿主的一切都来自消费者的可信后端，本 SDK 不产出其中任何一项。没有签名、没有商户密钥、没有预支付查询，也没有订单模型——这不是尚未实现的功能，而是边界本身：订单就是资金，其状态属于持有密钥并接收微信支付通知的后端。因此导出结果只说一件事 `interactionCompleted`，任何结果都不会被命名为 `paid`、`settled` 或 `confirmed`。支付相关的任何内容都不会被记录、缓存或持久化；请求模型在调用宿主之前拒绝空白字段，因为空白参数无法让宿主校验任何东西。

失败词表被刻意收窄。只有确切消息 `requestPayment:cancel` 会成为 `HostInteractionInterrupted`；`requestPayment:fail cancel` 及其他一切都保持 `HostFailure`。宿主其他接口使用 `:fail cancel` 形式，但该 API 没有证据覆盖，在此处猜测等于判定「支付被主动关闭」还是「支付被破坏」。出于同样的原因，该中断也不声称用户取消。

真实宿主验收尚未完成，且属于 `BackendRequired`：真实支付需要绑定本小程序 AppID 的合法商户号、真实订单、完成签名的可信后端与真机。模拟器的支付流程不是商户，因此它产出的任何内容都不能被记为支付结果。示例在本地提供参数之前报告 `NOT CONFIGURED` 且不调用宿主，该能力在这次运行发生前保持 `Partial`。

微信文件系统已于 2026-09-15 完成真实宿主验收。微信开发者工具与 Android 真机均在基础库 3.17.2 上验证了固定测试文件的写入、读取匹配、存在检查、删除与删除后不存在；沙箱根和文件内容均未被显示或记录。自动化 Kotlin/JS、fake-host、CommonJS 与 TypeScript 检查也已通过。

隐私授权目前仅有自动化覆盖。其真实宿主验收尚未完成，且取决于两件本仓库无法安排的事：小程序在 MP 后台声明的收集类型，以及该账号对宿主弹窗的作答。验证矩阵要求的证据是：`REQUIRED` 读数与宿主返回的协议名；成功后宿主报告 `NOT_REQUIRED`；以及拒绝被报告为 `REFUSED` 且要求仍然存在。

### 计划中的虚拟支付（未实现）

当前不存在任何虚拟支付代码，P1 也不计划实现：本轮的交付物是边界定义，见 [ARCHITECTURE-ch.md](ARCHITECTURE-ch.md)。以下内容是未来实现必须先确立的事项，记录在此是为了让这项工作不要从标准支付的文件开始。

在写下任何代码前，必须先从官方来源回答以下问题，因为每一项都会改变设计：

- `wx.requestVirtualPayment` 的参数表与结果形状；
- 它的最低基础库版本，或是否只能依赖 `wx.canIUse`；
- 其载体是小程序还是小游戏，以及哪些平台暴露该 API（Android、iOS、HarmonyOS 与开发者工具模拟器）；
- 账号、主体与类目资格规则，以及是否存在灰度；
- 是否需要权限或后台接口权限；
- 交互结束是否产生稳定、确切、区别于其他失败的消息；
- success callback 确立了哪些内容，以及由哪一方确认最终交易。

未来实现会遵循与其他微信能力相同的路径 —— 强类型 `interop` 声明与 presence guard、带生产实现的 adapter 端口、经 `canIUse` 门控的 catalog 条目、强类型导出 —— 并额外增加两点支付专属要求：独立 capability key，以及绝不与 `WeChatPaymentRequest` 合并的请求模型。

若实现，独立测试计划如下：

| 层级 | 必须覆盖的内容 |
| --- | --- |
| Interop | option bag 的确切字段，且不包含官方参数表未证明的字段；callback 初始化与赋值；API 或 `wx` 不存在时 presence guard 返回 false；没有任何 raw JS 对象离开 interop 包。 |
| Adapter | 完整的合法请求原样到达宿主；每个必填字段为空时被拒绝；成功、交互结束与失败各自只结算一次；重复与迟到回调不改变第一个终态；账号或平台不具备资格与 API 缺失被区分开；宿主是否提供 task 或 abort handle 由真实契约决定，而不是假设。 |
| Capability | key 与 `wechat.request-payment` 不同，且同一个 key 绝不同时回答两者；API 存在与账号/平台资格被区分；通过 `canIUse` 版本门控；缺少该 API 的宿主报告 `UnsupportedCapability`。 |
| Consumer | 示例默认 `NOT CONFIGURED`；未配置任何参数时不调用宿主；不打印也不显示敏感参数、凭据或宿主原始消息；一次成功调用显示为「交互结果」而不是「交易完成」；最终状态单独由后端验证。 |
| Real host | 分平台运行（Android、iOS、HarmonyOS、开发者工具），一个平台的结果不得记为另一个平台的证据；具备合法资格的账号；真实但可处置的商品与订单；交互结束实际产生的信号；失败场景；服务端最终交易确认；截图与 Console 记录不含敏感数据。 |

自动化无法替代资格与后端这两半：它们需要合格账号与真实订单，与标准支付完全相同。

## Bluetooth（实验性）

BLE 是对 SDK **事件驱动资源模型**的概念验证，不是 Bluetooth API，其全部内容均为 `Experimental`。它存在的原因是：SDK 其他能力要么回答问题，要么转发消费者本就拥有的界面；BLE 是第一个真实的 `on`/`off` 事件源。

覆盖范围：adapter 的 open / close / state；discovery 及其设备流；connection 及其连接状态流。刻意不做：service、characteristic、notify、MTU、信号强度查询、配对、自动重连、后台扫描与 `getBluetoothDevices`。

```shell
./gradlew :kmp-miniapp-sdk:jsNodeTest --tests "*Bluetooth*" --tests "*Ble*"
```

概念验证所依据、并由其测试断言的规则：

- **每个 collector 一个注册，并在所有终止路径上移除。** 每条流只注册一个宿主 listener，并把同一个值交给移除调用，因此无论 collector 正常结束、失败还是被取消都不会留下残留。宿主拒绝的移除只会被计数而不会抛出 —— 在清理阶段抛出会覆盖调用方自己的结果或取消。
- **重复事件策略：按 `deviceId` 逐 collector 一次事件**，以首次出现为准。discovery 同时以 `allowDuplicatesKey = false` 启动，因此宿主也会过滤；但流的形状不依赖宿主是否遵守该标志。
- **背压：有界缓冲并丢弃最旧事件**，因此宿主回调永不阻塞，也没有 collector 能让 SDK 无界地持有事件。由于 discovery 同时要求宿主不要重复上报，被丢弃的事件可能永久丢失；这是尽力而为的观察流，不是完整扫描结果。
- **幂等的状态变更。** 打开已开启的 adapter、关闭已关闭的 adapter、停止未运行的扫描，都不会调用宿主。
- **不建模任何权限或隐私前置条件**，因为无法从宿主契约确认。蓝牙未开启以普通的 `openBluetoothAdapter` 失败到达，而 adapter 的唯一端口是 Bluetooth host，因此它无法查询二者。

已安装的开发者工具基础库在 macOS 上拒绝 `createBLEConnection` 与 `closeBLEConnection`（`createBLEConnection:fail API_NOT_SUPPORT`），因此模拟器可以验证 adapter 与 discovery，但无法验证连接。这是宿主限制，连接验收因此需要真机。

adapter 与 discovery 已于 2026-09-18 在 Android 真机上执行并通过：三个能力键均为 `Supported`，adapter 开启与关闭均成功，discovery 正常启动与停止，共上报 32 个设备，listener 计数由 2 → 1 → 0。连接与断开未执行，因为当时没有可连接的外设。因此能力键在 [WECHAT_CAPABILITIES-ch.md](platforms/wechat/WECHAT_CAPABILITIES-ch.md) 中保持 `Experimental`：不完整的真机记录不构成完成的验收。

## 开发原则

IntelliJ IDEA 提供代码 intelligence。Gradle Wrapper 是构建事实源。IDE 显示成功并不能证明项目构建或测试成功。

## 添加依赖

依赖和插件版本应声明在 `gradle/libs.versions.toml`。没有明确并记录的原因时，不得在多个 `build.gradle.kts` 文件中重复硬编码相同版本。

只在已实现的变更确实需要时添加依赖，不得添加推测性基础设施。

## 完成定义

修改 SDK 后：

- Gradle build 通过。
- 相关 tests 通过。
- `docs/PROJECT_FACTS-en.md` 和 `docs/PROJECT_FACTS-ch.md` 保持准确和同步。
- JavaScript、WeChat、DOM 或 Node-specific implementation 不得泄漏到 `commonMain`。
