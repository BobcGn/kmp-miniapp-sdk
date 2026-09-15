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
./gradlew :sdk:jsNodeTest
./gradlew :sdk:jsTest
./gradlew buildMiniAppSdk
```

这些命令有效，并已于 2026-09-15 完成验证。

`:sdk:jsTest` suite 覆盖 Host boundary、error model、callback adaptation、cancellation、double completion、可选 abort、微信 error mapping、包含取消时宿主中止的 HTTP transport adaptation、生命周期状态迁移、导航适配、capability support 状态与版本门控、包含并发请求合并的权限生命周期行为，以及 interop object construction。这些测试使用 fake callbacks，不代表能够访问真实 `wx` runtime。

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

smoke test 加载 consumer-facing CommonJS 模块、调用 `sdkVersion()`，并安装 fake global `wx` 验证 Storage、微信 login bootstrap、HTTP transport、lifecycle 转发、三种导航调用、运行时能力检测与权限生命周期。它还会断言 HTTP transport 交给 fake host 的内容，包括原始文本响应模式、导航收到的绝对页面路径，以及每个纳入门控的能力所报告的支持状态。该测试验证 module 与 adapter behavior，但不构成真实 Host 证据。TypeScript 使用 strict mode，并在不依赖 `any` 的情况下验证 Promise-based Storage、typed `WeChatLoginResult`、HTTP transport、lifecycle、导航、capability support 与权限 exports。

真实宿主验证按 [TESTING-ch.md](TESTING-ch.md) 中的检查清单执行，该清单是页面取值、console 输出与准备步骤的权威来源。只有在完成该运行后，某项 capability 才会在 [PROJECT_FACTS-ch.md](PROJECT_FACTS-ch.md) 中被记录为已通过宿主验证。

Consumer Bridge 与 Storage capability 已于 2026-09-14 在微信开发者工具 Stable 2.01.2510290 中通过用户提供的视觉证据完成验证。页面显示 `0.1.0-SNAPSHOT`，Storage 状态为 `PASS`，详情为 `first=first, overwritten=second, missing=null`。

Authentication 的真实宿主验收已于 2026-09-14 通过用户提供的微信开发者工具证据完成。页面显示 `Client login code: PASS`、`codeReceived=true` 和 code 长度 32，且没有显示凭证本身。示例不会记录或渲染 raw credential。`wx.login` code 是短期凭证，必须发送到可信的消费者后端与微信交换；取得 code 不代表用户已认证，不会创建 SDK session，也不能授权请求。

HTTP transport capability 已于 2026-09-14 通过用户提供的微信开发者工具确认完成真实宿主验收。其 adapter、error mapping 与 cancellation 行为仍由 Kotlin/JS、fake-host、CommonJS 与 TypeScript 自动检查覆盖；真实宿主运行确认 index 页面上的 network 卡片可成功到达 `wx.request`。

Lifecycle 与导航桥接已于 2026-09-15 通过用户确认完成微信开发者工具验收。运行结果覆盖前台 lifecycle 状态、当前页面 route，以及 `navigateTo` 打开第二页、`redirectTo` 替换为第三页、`navigateBack` 直接返回首页的完整页面栈路径。后台状态迁移仍需要真机将小程序切入后台，开发者工具模拟器不覆盖该行为。

Node/CommonJS smoke test 或 TypeScript check 通过，并不能证明微信小程序集成通过。必须分别记录两层结果。

运行时能力检测已于 2026-09-15 完成开发者工具（基础库 3.17.2）与 Android 真机（OnePlus PLQ110、Android 36、微信 8.0.76）验收，两种环境均报告 `runtime-detection=Supported`、`storage=Supported`、`ungated=Unsupported`。开发者工具当前可选的最低调试基础库为 2.21.4，无法构造低于 2.20.1 的宿主，因此 `VersionDependent` 由自动化测试覆盖而不是真实宿主截图；各情形的构造方式见 [TESTING-ch.md](TESTING-ch.md) 中的检查清单。

权限生命周期已于 2026-09-15 完成开发者工具（基础库 3.17.2）与 Android 真机验收：宿主报告了 `Granted` 与 `Denied`，设置页返回的是宿主的决定，对已拒绝权限再次请求报告 `DENIED` 且不出现第二次弹窗。`NotRequested` 无法在所用账号上产出，因为该账号已对所映射权限持有决定，因此该状态由自动化测试覆盖。自动化套件仍然从不请求权限，因为真实弹窗需要用户手势。当前只映射麦克风权限。

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
