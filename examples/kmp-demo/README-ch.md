# KMP 消费者示例

[English](README-en.md)

这是独立消费者构建，通过 `includeBuild("../..")` 使用当前仓库的 SDK 与 Gradle 插件。它不是 SDK 根构建的模块，也不代表已发布插件版本的消费结果。

## 目录

- `androidApp/` 与 `iosApp/`：原生应用入口；Compose UI 位于 `shared` 的 Android/iOS source sets。
- `shared/src/commonMain` 与 `commonTest`：共享 greeting 逻辑与测试。
- `shared/src/miniappMain` 与 `miniappTest`：Mini App 逻辑、SDK 运行时信息读取与测试；不包含 renderer。
- `miniApp/`：根级微信宿主源码，由 `initMiniApp` 创建后作为源码维护。`miniprogram/libs/` 是被忽略的生成物。
- `build/miniapp-dist/`：被 Git 忽略的独立分发构建产物。

## 当前状态与限制

消费者启用了 `miniapp { wechat { rootHost.set(true) } }`，根级宿主初始化已通过。Mini App Kotlin 入口导出 `platformGreeting()`，使用共享 greeting 逻辑与 SDK 读取的平台、微信基础库版本，不需要后端。

按照用户决定，Compose 资源依赖仍保留在 `commonMain`。它目前将 renderer 依赖引入 Mini App 依赖图，生成库同步被 SDK 宿主边界检查阻塞。本示例尚未通过完整 Mini App 构建和运行验收。微信页面仍是列出模块导出的初始化模板，尚未展示预期的 greeting 首屏。提交这些文件不构成 Android/iOS 构建或运行验收。BOB-122 仍待推进；BOB-126 的运行证据来自独立根级宿主 fixture。

`project.config.json` 只包含 `touristappid`。在微信开发者工具中配置本地测试环境，私有工具配置和凭据不要提交。iOS 的 `TEAM_ID` 为空，签名时需要在本地提供。

## 命令

在本目录使用自身 Gradle Wrapper：

```bash
./gradlew --no-daemon --no-configuration-cache initMiniApp
./gradlew --no-daemon --no-configuration-cache syncMiniApp assembleMiniApp verifyMiniApp
./gradlew :shared:miniappTest
./gradlew :androidApp:assembleDebug
./gradlew :shared:testAndroidHostTest
./gradlew :shared:iosSimulatorArm64Test
```

第一条命令补齐宿主缺失文件，不覆盖已有文件。后两条 Mini App 命令仍受上述资源依赖问题阻塞。成功同步后，再将 `miniApp/` 导入微信开发者工具。iOS 使用 Xcode 打开 `iosApp/iosApp.xcodeproj`。这些命令描述工作流，不表示每条命令都已通过。

## 版本管理边界

提交 Kotlin、宿主 UI、平台资源、Gradle 配置、Wrapper 文件、Xcode 工程文件及 `kotlin-js-store/yarn.lock`。忽略构建产物、宿主生成库、本机 SDK 路径、IDE/缓存文件、Xcode 用户状态和微信私有配置。忽略规则保留源码与依赖锁文件。
