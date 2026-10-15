# 无服务器的 KMP 消费者示例

[English](README-en.md)

这是通过 `includeBuild("../..")` 使用当前 SDK/插件源码的独立消费者。根级产物模块选择不属于已发布的 0.1.1，不需要后端。

## 目录与依赖边界

- `core/`：`Greeting`、`sayHello`、平台信息与实现、Mini App 导出和逻辑测试。应用 Kotlin Multiplatform，不包含 Compose 依赖。
- `shared/`：Android/iOS Compose UI、Compose 资源与 iOS controller。通过 `api(project(":core"))` 消费 core，core 不反向依赖 shared。
- `androidApp/` 与 `iosApp/`：移动端应用入口。
- `miniApp/`：根级微信宿主源码，用原生 WXML/WXSS 展示 greeting；生成库 `miniprogram/libs/` 被忽略。
- `core/build/miniapp/bundle/` 与根级 `build/miniapp-dist/`：生成的构建产物。

## 根工程明确选择

```kotlin
// Root build.gradle.kts
plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    id("io.github.bobcgn.miniapp")
}
miniapp {
    logicProject.set(project(":core"))
}
```

所选 core 照常应用 KMP，由根工程在其中接入 Mini App。模块名是消费者决定，不是插件要求。本仓库将原混合 shared 模块手工迁移为 core 与 shared UI；插件不猜测名称、不创建模块、不移动源码、不排除依赖。所选模块不能再次覆盖根工程管理的宿主路径。已有模块内接入继续支持，带服务器示例保留该方式。

迁移保留移动端 UI/资源，将已有 greeting 与 expect/actual 平台逻辑迁移但不改包名。`platformGreeting()` 与移动端 UI 调用同一 `Greeting().greet()`；微信平台/基础库信息由 SDK 提供，缺失时显示未知。页面 require 从旧 shared 模块改为实际 core 模块；初始化不重写手工页面。

## 构建与检查

在本目录运行：

```bash
./gradlew --no-daemon --no-configuration-cache --console=plain \
  :core:miniappTest syncMiniApp assembleMiniApp verifyMiniApp \
  :androidApp:assembleDebug :shared:compileKotlinIosSimulatorArm64
node miniApp/scripts/smoke.cjs
node build/miniapp-dist/scripts/smoke.cjs
```

iOS 应用使用 Xcode 打开 `iosApp/iosApp.xcodeproj`。这些命令定义验收步骤，实际结果与限制记录如下。Node smoke 检查隔离的 mock devtools、Android 和无宿主场景，不代表真实微信运行。

## 验证与限制

原 shared/commonMain 的资源依赖曾导致 renderer 边界拒绝。现在资源只属于 shared UI 模块，Mini App 编译 core。这是依赖隔离，不是 Mini App 支持共享 Compose Res。2026-10-14，上述 Wrapper 命令通过：3 项 Mini App 测试、同步/组装/校验（24 files、verified=true）、Android assembleDebug 与 iOS Simulator Kotlin 编译。Source host 与 distribution 的 Node smoke 均通过 3 个模拟宿主场景。Android 保留 Compose 资源，Mini App 无 Compose/Skiko 模块，旧 shared 模块陈旧产物已移除。这些是构建/接线证据，不是设备运行验收。

成功同步后使用本地测试配置导入 `miniApp/`，预期首屏为 `Hello, WeChat · devtools · 基础库 <实际基础库版本>!`，Console `demo.result=PASS`；分别检查重新编译/打开和独立 distribution。迁移后的无服务器示例需用户独立验收，带服务器示例的截图不能替代它。不会自动上传、发布或合并。

## 版本管理

保留源码、平台资源、Wrapper、工程配置和依赖锁文件；忽略生成库/build、微信私有配置与本机设置。工程配置只保留 touristappid；iOS TEAM_ID 仍为空。

## JS/TS 声明导航

宿主包含 `miniprogram/jsconfig.json` 和 bundle 引用的 JSDoc 类型注解。`platformGreeting()` 与 `MiniAppExports.sdkVersion()` 已有自动化声明检查，但本无服务器 demo 仍需自己的人工 IDE 验收。服务器 demo 的 VS Code 结果及 2026-10-15 已接受的 IDEA 交互差异不能证明本 demo 的行为。SDK 接入保证声明／运行产物身份对应，不保证各编辑器默认点击目标一致。检查与限制见 [DEVELOPMENT](../../docs/DEVELOPMENT-ch.md#jsts-消费者声明与导航当前源码尚未发布)。页面另外记录 `demo.sdkVersion`，不声称本 demo 的 Kotlin 导航已人工通过。
