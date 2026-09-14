# 架构

[English](ARCHITECTURE-en.md)

本文档记录稳定的架构边界，不表示其中描述的所有 integration 都已经实现。当前能力见 [PROJECT_FACTS-ch.md](PROJECT_FACTS-ch.md)。

## 1. 系统边界

```text
commonMain
    ↓
jsMain/export facade
    ↓
Kotlin/JS artifact
    ↓
极薄的 CommonJS export normalization
    ↓
TypeScript / JavaScript
    ↓
WeChat Mini Program runtime
```

## 2. 依赖方向

平台无关的 interfaces、models、errors 和 shared logic 属于 `commonMain`。平台实现属于 `jsMain`，并依赖平台无关 contracts。

```text
jsMain platform implementation
            ↓ depends on
commonMain interfaces and models
```

`commonMain` 不得依赖微信实现细节。微信特定代码不得向上污染 shared layer。

## 3. 宿主边界

微信小程序是 host runtime。SDK 不负责：

- WXML 或 WXSS rendering
- Page rendering
- Component trees
- Application UI

SDK 的范围仅包括 shared logic、runtime integration 和 typed platform bridge。

## 4. JS Interop 边界

`external`、`dynamic` 和 `js()` 等 JavaScript-specific constructs 只能存在于 `jsMain`。原始 platform contracts 限制在 `jsMain/.../host/wechat/interop`。

Business 和 shared layers 不得直接操作 `dynamic` values。将其转换为类型化 Kotlin values 是 adapter boundary 的职责。

微信 interop 层只描述全局 `wx` object、option bags、result shapes 和 callbacks。Plain-object factories 的存在仅因为 external interfaces 没有 Kotlin constructors；它们不调用宿主 API，也不包含错误映射、capability policy 或业务逻辑。

`host/wechat` 是 host boundary 下的平台特定 namespace。该 namespace 为未来宿主保留空间，但当前不引入其他宿主实现或新的 Gradle modules。

## 5. 异步边界

异步平台 API 的架构目标是：

```text
wx callback
    ↓
interop
    ↓
adapter
    ↓
coroutine-friendly Kotlin API
```

这是架构目标，不是当前能力。项目尚未实现 callback-to-coroutine adapter。

## 6. 公共边界

项目区分两套 API surface：

- Kotlin public API：供 Kotlin 代码使用、符合 Kotlin 习惯的 interfaces、models 和 behavior。
- JavaScript / TypeScript export API：根据 JavaScript / TypeScript 易用性和互操作限制设计的 consumer-facing boundary。

Export surface 可以适配 Kotlin API，但不得成为业务实现层。

宿主无关的 export facade 位于 `jsMain/.../export`，可以调用 `commonMain` 中的共享 Kotlin API，但不得包含 `wx` 调用或平台能力实现。当编译器生成的 CommonJS namespace 不适合作为稳定的 consumer ABI 时，可以使用极薄的 JavaScript wrapper，仅做 module 和 export shape normalization；wrapper 不得包含业务逻辑、宿主错误映射或平台行为。

## 7. 非目标

- 重新实现 Kuikly
- 构建 Compose Mini Program renderer
- 替代 WXML
- 构建 Virtual DOM
- 完整镜像 `wx` API
