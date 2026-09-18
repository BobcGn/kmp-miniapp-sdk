# kmp-miniapp-sdk runtime

[English](README-en.md)

`kmp-miniapp-sdk` 的 runtime 模块提供共享 Mini App 客户端行为、宿主能力以及当前微信 adapter
所需的 Kotlin API。它是 Kotlin Multiplatform library，但当前发布的平台实现面向 Kotlin/JS Mini App
宿主。

版本 `0.1.0` 已发布至 Maven Central：

```kotlin
io.github.bobcgn:kmp-miniapp-sdk:0.1.0
```

这是实验性版本。在生产环境依赖具体能力前，请检查
[微信能力矩阵](../docs/platforms/wechat/WECHAT_CAPABILITIES-ch.md)。

## 推荐的 KMP 接入方式

预期的消费者体验使用配套 Gradle 插件。它会创建 `miniapp` target，添加 `miniappMain` 与
`miniappTest`，接入本 runtime 依赖，配置 Node 测试并提供 `assembleMiniAppBundle`。

> **当前发布状态：**上述 runtime artifact 已在 Maven Central 上线。首次提交的
> `io.github.bobcgn.miniapp:0.1.0` 插件仍在等待 Gradle Plugin Portal 审核。下列配置是最终支持的
> 使用形式，但只有审核完成后才能在线解析插件。在此之前，请使用
> [源码检出工作流](../docs/CONSUMER_SETUP-ch.md#当前源码检出工作流)，或使用下方仅接入 runtime
> 的方式。

### 1. 配置仓库

在 `settings.gradle.kts` 中：

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

### 2. 应用插件

在 KMP 模块的 `build.gradle.kts` 中：

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

不要手工创建 `miniappMain`、配置 CommonJS 或添加 runtime 依赖；这些构建细节由插件负责。

### 3. 将代码放入正确的 source set

```text
src/
├── commonMain/kotlin/     可移植行为；不含宿主 API 或渲染
├── commonTest/kotlin/     可移植测试
├── miniappMain/kotlin/    SDK 调用、JS export 与宿主绑定
└── miniappTest/kotlin/    Mini App / Node 测试
```

例如，共享行为可以保留在 `commonMain`：

```kotlin
package example

public fun greeting(name: String): String = "Hello, $name"
```

Mini App 边界放在 `miniappMain`：

```kotlin
package example

import io.github.bobcgn.miniapp.api.MiniAppSdk
import kotlin.js.JsExport

@JsExport
public fun hostGreeting(name: String): String = greeting(name)

@JsExport
public fun sdkVersion(): String = MiniAppSdk.VERSION
```

只有在此边界导出的声明对宿主可见。WXML、WXSS 与薄宿主 JavaScript 仍留在 Mini App 工程中；
SDK 不负责渲染 UI。

### 4. 测试并组装

```bash
./gradlew miniappTest
./gradlew assembleMiniAppBundle
```

使用微信开发者工具打开所配置的宿主目录，并检查宿主控制台和页面。Node 测试只证明 Kotlin/JS
行为，不能替代真实宿主或真机验证。

## 已有 Kotlin/JS target 的仅 runtime 接入方式

如果工程已经拥有 Kotlin/JS target，并且不需要由插件管理 Mini App target，只把 runtime 加入该
target 的 main source set。不要把这个仅 JS 的 artifact 加入多平台 `commonMain`，因为 Android 与
iOS 无法解析匹配的 runtime variant。

```kotlin
kotlin {
    js {
        nodejs()
        useCommonJs()
        binaries.library()
        generateTypeScriptDefinitions()
    }

    sourceSets {
        jsMain.dependencies {
            implementation("io.github.bobcgn:kmp-miniapp-sdk:0.1.0")
        }
    }
}
```

这种方式只提供 runtime API。target 配置、测试、bundle 组装、宿主依赖复制以及渲染边界均由消费者
自行负责。

## 架构边界

- `commonMain` 负责可移植 contract、model、error 与行为。
- `miniappMain` 或 `jsMain` 负责 SDK 调用、JavaScript export 与宿主绑定。
- 宿主负责页面、布局、WXML/WXSS、渲染与原生组件。
- 后端负责认证、支付、库存以及其他业务事实。
- runtime SDK 不依赖 Compose，也不是 renderer、Virtual DOM 或 WXML generator。

## 延伸文档

- [完整消费者接入指南](../docs/CONSUMER_SETUP-ch.md)
- [架构](../docs/ARCHITECTURE-ch.md)
- [项目事实](../docs/PROJECT_FACTS-ch.md)
- [测试](../docs/TESTING-ch.md)
- [微信能力矩阵](../docs/platforms/wechat/WECHAT_CAPABILITIES-ch.md)
- [宿主验证矩阵](../docs/platforms/wechat/WECHAT_HOST_VERIFICATION-ch.md)

## 许可证

采用 [Apache License 2.0](../LICENSE) 许可。
