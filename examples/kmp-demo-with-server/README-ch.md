# 带服务器的 KMP 消费者示例

[English](README-en.md)

这是包含 Android/iOS 客户端和 Ktor 服务器的独立 Gradle 构建。它通过 `includeBuild("../..")` 使用当前仓库的 SDK 插件与 runtime，包括尚未发布的根级宿主支持；不代表已发布插件版本的验证结果。

## 目录

- `core/`：Android、iOS 与 JVM 共享逻辑。Mini App 插件在这里增加 `core/src/miniappMain/kotlin` 和 `core/src/miniappTest/kotlin`。SDK runtime 只接入 Mini App source set。
- `app/shared/`：Compose UI 与资源；依赖 `core`，不应用 Mini App 插件。
- `app/androidApp/` 与 `app/iosApp/`：移动端应用入口。
- `app/miniApp/`：显式初始化后由消费者维护的微信宿主源码。生成库放在 `miniprogram/libs/`，被 Git 忽略。
- `server/`：依赖 `core` 的 Ktor JVM 应用；Mini App 初始化无需启动服务器。
- `core/build/miniapp/bundle/`：生成的 Kotlin/JS bundle；`build/miniapp-dist/`：生成的独立微信分发工程。二者都是构建产物，不是宿主源码。

## 命令

在本目录使用自身 Gradle Wrapper：

```bash
./gradlew --no-daemon --no-configuration-cache --console=plain initMiniApp :core:checkMiniAppHostBoundary
./gradlew syncMiniApp assembleMiniApp verifyMiniApp
./gradlew :core:miniappTest
node app/miniApp/scripts/smoke.cjs
node build/miniapp-dist/scripts/smoke.cjs
./gradlew :app:androidApp:assembleDebug
./gradlew :app:shared:testAndroidHostTest
./gradlew :app:shared:iosSimulatorArm64Test
./gradlew :server:run
./gradlew :server:test
```

iOS 使用 Xcode 打开 `app/iosApp/iosApp.xcodeproj`。成功同步生成库后，将 `app/miniApp/` 导入微信开发者工具。

## 当前验证与限制

2026-10-14，`initMiniApp :core:checkMiniAppHostBoundary` 得到 BUILD SUCCESSFUL：宿主创建于 `app/miniApp/`，解析后的 Mini App 依赖图通过 renderer 边界检查。初始化补齐缺失文件，不覆盖已有文件。

Mini App 已导出 `org.example.kmp_demo_with_server.platformGreeting()`，与移动端共同使用 `core/commonMain` 中的 `sayHello()`。微信原生页面只展示 greeting，通过 SDK 读取平台与基础库版本，无需服务器或网络请求；宿主信息缺失时显示未知。Compose UI 与资源继续留在 `app/shared`。

2026-10-14，Wrapper 命令 `:core:miniappTest :core:jvmTest syncMiniApp assembleMiniApp verifyMiniApp` 通过：2 项 Mini App 测试通过，distribution 报告 `verified=true`（24 个文件）。`jvmTest` 为 NO-SOURCE，不记为 JVM 测试套件通过。Node smoke 在隔离的模拟 devtools、Android 与无宿主环境中检查真实编译模块和页面，不代表微信运行证据。Android `:app:androidApp:assembleDebug` 通过，APK 保留 Compose 资源。`core` 的 `miniappRuntimeClasspath` 与生成库没有 Compose/Skiko renderer 或 Compose 资源。这证明本示例的依赖隔离生效，不代表 Mini App 已普遍支持共享 `Res`。

开发者工具运行验收仍待完成：用户已确认后续使用测试账号手工导入并验收。本轮 CLI 服务端口关闭，没有修改安全设置。不宣称 iOS、服务器或真机运行验收通过。初始化仍保持幂等并保留已有宿主文件。

## 开发者工具验收

成功同步后，用本地测试配置导入 `app/miniApp/`。首屏应显示 `Hello, WeChat · devtools · 基础库 <实际基础库版本>!`，与工具选择的基础库一致；Console 应报告 `demo.result=PASS`，无应用异常。重新编译或重新打开后检查重复加载。截图和运行结果必须与 Node/构建结果分别记录；本次示例验收不上传或发布。

入库配置必须只使用 `touristappid`；本地私有配置和凭据不得纳入版本管理。宿主源码位于 build 外，clean 不删除它。此次没有给 SDK 增加 renderer 或服务器依赖。

## JS/TS 声明导航

宿主包含 `miniprogram/jsconfig.json` 和 bundle 引用的 JSDoc 类型注解。2026-10-14 用户在 VS Code 1.141.0（Universal）验证 `platformGreeting()` 和 `MiniAppExports.sdkVersion()` 均进入生成 `.d.ts`。IDEA 2026.2.3 的两个悬停签名指向该声明；`sdkVersion()` 提供声明候选，`platformGreeting()` 则打开对应生成 JS。2026-10-15 用户接受该交互差异，它不再是阻塞项，也不代表导航完全一致。可选项目级 library 实验不是接入必需步骤或已验证修复。页面另外记录 `demo.sdkVersion`。

本 demo 的本地 `includeBuild` 模式已在 IDEA 2026.2.3 通过 Kotlin 消费源码导航与 KDoc 验收；发布版 SDK 0.1.1 由 `fixtures/miniapp-published-consumer` 单独验收，不能据此将本 demo 说成发布插件夹具。命令、证据与工具限制见 [DEVELOPMENT](../../docs/DEVELOPMENT-ch.md#jsts-消费者声明与导航当前源码尚未发布)。Node／类型检查不能替代人工 IDE 验收，不宣称 Android Studio 或 VS Code Kotlin/KMP 已通过。
