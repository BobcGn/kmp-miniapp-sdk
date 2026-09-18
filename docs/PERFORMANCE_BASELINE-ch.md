# Kotlin/JS 包体积基线

[English](PERFORMANCE_BASELINE-en.md)

本文档记录 Mini App SDK 给一个小程序带来多少体积、这些数字如何测量，以及什么情况下测量会让构建失败。它是基线，不是性能保证：一台机器、一对编译器版本、某一个时刻。

## 测量对象

| 测量对象 | 命令 | 为什么 |
| --- | --- | --- |
| SDK 的生产 library distribution：`sdk/build/dist/js/productionLibrary` | `./gradlew verifyMiniAppBundleSize` | 这是 SDK 自己的交付产物：插件重新发布它，它的大小是消费者 bundle 中属于 SDK 的那一部分 |
| 微信宿主分发：`examples/wechat-miniprogram/miniprogram/libs` | `./gradlew verifyMiniAppBundleSize -PminiappSizeDirectory=examples/wechat-miniprogram/miniprogram/libs` | 今天示例宿主实际加载的东西：同一份 library 加上手写 CommonJS 规范化层 |
| 消费者 bundle，例如 `fixtures/miniapp-consumer/build/miniapp/bundle` | 同一条命令加该路径 | 展示 SDK 在应用代码旁边占多少；需要先构建该 fixture |

除此之外不测量任何东西。仓库检出本身不是 bundle：`docs/`、`poc/`、`examples/`、Gradle 脚本与源码树都不属于宿主加载的内容，也不会出现在报告里。

## 分类

每个被测量的文件按文件名分类，因此同一套规则既适用于 SDK 的分发，也适用于消费者 bundle。

| 分类 | 规则 | 含义 |
| --- | --- | --- |
| `sdk` | `kmp-miniapp-sdk-*` | SDK 编译出的 module 与其手写 CommonJS 规范化层 |
| `runtime` | `kotlin-*`、`kotlinx-*`、`kotlin_*` | 该编译解析到的 Kotlin、coroutines、atomicfu 与 DOM 兼容 runtime |
| `consumer` | 其余 `.js` | 消费者编译的应用代码 |
| `development` | `*.map`、`*.d.ts`、`package.json` | assembled distribution 中的辅助 source map、declaration 与 module 元数据；它们不是运行时代码，但这里不假设打包时会排除 |

含 `.wxml`、`.wxss`、`.axml`、`.acss` 或 `.html` 的分发会被拒绝而不是被测量：宿主 markup 属于宿主 UI，把它算作 SDK runtime 体积是错的。

## 当前基线

由 `updateMiniAppSizeBaseline` 记录。机器可读记录为 [`performance-baseline.json`](performance-baseline.json)，任务比较以它为准；下面的表格是它的复述。

被测分发：`sdk/build/dist/js/productionLibrary`。

| 文件 | 分类 | 原始字节 | gzip 字节 |
| --- | --- | ---: | ---: |
| `kmp-miniapp-sdk-kotlin.js` | sdk | 396,978 | 49,023 |
| `kmp-miniapp-sdk-kotlin.d.ts` | development | 13,379 | 1,897 |
| `kmp-miniapp-sdk-kotlin.js.map` | development | 154,020 | 42,220 |
| `kotlin-kotlin-stdlib.js` | runtime | 255,917 | 42,144 |
| `kotlin-kotlin-stdlib.js.map` | development | 154,611 | 20,943 |
| `kotlinx-coroutines-core.js` | runtime | 380,535 | 58,655 |
| `kotlinx-coroutines-core.js.map` | development | 167,944 | 61,065 |
| `kotlinx-atomicfu.js` | runtime | 9,372 | 1,431 |
| `kotlinx-atomicfu.js.map` | development | 4,849 | 1,532 |
| `kotlin_org_jetbrains_kotlin_kotlin_dom_api_compat.js` | runtime | 191 | 168 |
| `kotlin_org_jetbrains_kotlin_kotlin_dom_api_compat.js.map` | development | 151 | 133 |
| `package.json` | development | 311 | 175 |

| 分类 | 文件数 | 原始字节 | gzip 字节 |
| --- | ---: | ---: | ---: |
| sdk | 1 | 396,978 | 49,023 |
| runtime | 4 | 646,015 | 102,398 |
| consumer | 0 | 0 | 0 |
| development | 7 | 495,265 | 127,965 |
| **合计** | **12** | **1,538,258** | **279,386** |

这些数字直白说明了什么：

- SDK 自身代码为 397 KB 原始、49 KB gzip。Kotlin 与 coroutines runtime 为 646 KB 原始、102 KB gzip —— **runtime 约占宿主必须加载字节的 62%**，且仅 coroutines 一项就比 SDK 更大。
- source map 与 declaration 占 495 KB 原始，是该目录的三分之一；它们不是运行时代码，但由于存在于 assembled distribution，仍计入完整包体。
- 同日测量的微信宿主分发共 11 个文件、1,581,342 原始 / 291,071 gzip —— sdk 408,958 原始 / 51,734 gzip（module 加手写 wrapper），runtime 645,824 原始 / 102,237 gzip，development 526,560 原始 / 137,100 gzip。它比 library distribution 更大，因为还带有 wrapper 及其 declaration；而在 runtime 方向更小，因为该路径只复制宿主会加载的文件。

## gzip 规则

gzip 数字是比较指标，不是传输体积。真实宿主用自己的传输、自己的 level、自己的选项来发送这些文件，并且可能先做自己的变换。

| 规则 | 取值 |
| --- | --- |
| 算法 | `java.util.zip.GZIPOutputStream` |
| Level | `Deflater.DEFAULT_COMPRESSION`（`-1`，zlib 默认值），以 `gzipLevel` 记入基线 |
| 时间戳 | 无：JDK 的 gzip header 不携带修改时间，因此同样的字节总是压缩出同样的长度 |
| 汇总方式 | 每个文件单独压缩后求和；绝不把多个文件合并压缩 |
| 排序 | 按相对路径排序 |
| 路径 | 相对于被测目录；报告中不出现任何绝对路径 |

因此同样的字节总是产生同样的数字，单元测试对此作出断言。**不稳定的是编译器自身的输出**，见下文限制。

## 阈值

`verifyMiniAppBundleSize` 会把新的测量与已提交的基线比较，并且只在**同时**超过以下两项时失败 —— 对每个分类和完整 assembled distribution 分别检查：

| 阈值 | 取值 | 依据 |
| --- | --- | --- |
| 绝对下限 | 4,096 字节 | 在小程序这种以 MB 计包体上限的场景里，几百字节不是任何人能据以决策的量；4 KB 约为微信单包 2 MB 上限的 0.2% |
| 相对比例 | 5% | 最小的交付分类（atomicfu，9 KB）翻倍对宿主也无所谓，因此只看比例会被噪声触发；最大的分类（coroutines，380 KB）在比例触发前可吸收约 19 KB |

两者必须同时超过，因此小文件大幅增长、大文件小幅增长都会被报告但都不会失败。只有增长才计数：包体变小永远不会失败。辅助 `development` 文件因存在于 assembled distribution 而参与门禁；总量还会单独检查，避免增长分散在多个分类后绕过门禁。

该规则刻意保持宽松。单机单次测量不是性能保证，而在任何优化尚未发生之前就取得基线，也无法诚实声称 4 KB 是「可接受」与「不可接受」的分界。

**接受有意的增长。** 当增长已被理解且确实需要时，用产生该增长的测量重写基线，并像评审其他改动一样评审该 diff：

```shell
./gradlew updateMiniAppSizeBaseline
```

基线记录 commit、日期、Gradle / Kotlin / SDK 版本与 gzip level，因此后来的读者能知道它描述的是什么。用其他 gzip level 记录的基线、或针对其他目录的基线不会被比较 —— 任务会直接说明，而不是产出一个无意义的 diff。

## 重现报告

```shell
./gradlew verifyMiniAppBundleSize
```

它会写出 `build/reports/miniapp-size/bundle-size.json`，并打印每个文件、每个分类合计以及比较结果。该测量不触碰产物、不需要网络，并可复用 configuration cache。

## 首次加载观察

本仓库无法测量一个小程序启动需要多久，本文档中任何内容都不应被读成它可以。以下是人工用微信开发者工具自带数据做一次观察的方法。

1. 构建示例分发：`./gradlew buildMiniAppSdk`。
2. 在微信开发者工具中打开 `examples/wechat-miniprogram` 并编译。
3. 记录工具中显示的调试基础库版本。
4. 打开 **详情 → 性能**，读取冷启动的 **Launch Time**（每次测量之间关闭并重新打开模拟器，使每次都是冷加载）。
5. 至少重复三次，记录每一次的数值与中位数。
6. 若工具提供包分析，按其报告的各文件大小截图。
7. 把机器信息与基础库版本一并记录在该数字旁。

### 2026-09-18 验收观察

- 微信开发者工具：`2.01.2510290 darwin-arm64`
- 调试基础库：`3.17.2`
- SDK：`0.1.0-SNAPSHOT`
- 方法：关闭自动热重载；每轮清除数据/文件缓存并重新编译，期间未修改代码或重新运行 Gradle
- 三次 Launch Time：`784 ms`、`851 ms`、`807 ms`
- 中位数：`807 ms`；平均值：约 `814 ms`；极差：`67 ms`
- 结果：三轮均完成 SDK 版本输出与 runtime detection `PASS`；清缓存后的 session `Invalid` 属预期状态

这是一组开发者工具中的宿主冷启动观察，不是 SDK 单独耗时，也不是发布环境的性能保证。

**该数字的含义。** 微信的 Launch Time 覆盖整个小程序：微信 runtime 自身、页面自身初始化、示例的启动调用（storage、login、一次网络请求、能力查询）、其 WXML 渲染，最后才是 SDK 的那部分。它不是 SDK 耗时；用另一个项目的数字去相减也得不到 SDK 耗时。上面的尺寸报告才是 SDK 的成本，Launch Time 是背景信息。

## 已知限制

- **编译器输出不是逐字节稳定的。** 重新构建 `jsNodeProductionLibraryDistribution` 会重新生成 `kotlinx-coroutines-core.js` 与 `kotlin-kotlin-stdlib.js`，并产生几字节的差异：连续四次重建中观察到的浮动为总计 gzip 7 字节、单文件最多 11 字节，原始大小浮动不超过 1 字节。测量对于它所读取的文件是确定性的 —— 同一目录总是产生同一份报告 —— 但一次重建会让输入略微移动。阈值规则中的下限部分正是为此存在。
- 这些数字描述的是记录了 Kotlin 与 Gradle 版本的某一台机器。换一个编译器版本可能带来超过上述浮动的变化。
- gzip 数值是比较指标，不是网络传输体积。
- 基线覆盖 SDK 的分发，宿主示例的分发与它一同测量。消费者 bundle 可以用同一条命令测量，但仓库中没有提交任何消费者 bundle，因此没有为它建立基线。
- 没有历史趋势：基线是单条记录，更新时原地替换。
- 本文档不对运行时性能、内存或启动成本作任何声明。
