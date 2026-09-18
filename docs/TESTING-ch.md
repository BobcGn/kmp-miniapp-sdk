# 测试

[English](TESTING-en.md)

本文档描述两层测试体系、各层可使用的 fake，以及可复现的真实微信小程序运行检查清单。

两层是“自动化”和“真实宿主”的最高层分类。发布证据进一步细分为 Unit、Contract、Node、DeveloperTools、RealDevice 和 BackendRequired。各能力最低需要哪个等级、如何留存证据以及何时回归，见 [微信真实宿主验证矩阵](platforms/wechat/WECHAT_HOST_VERIFICATION-ch.md)。

## 1. 两层，两类不同的结论

| 层次 | 运行环境 | 能证明 | 不能证明 |
| --- | --- | --- | --- |
| 自动化 | Node.js，通过 Gradle Wrapper 与示例的 npm scripts | 共享 Kotlin 逻辑、通过 fake port 驱动的 adapter 行为、模块与类型声明形状、TypeScript 契约 | 任何关于微信小程序 runtime 的事实 |
| 真实宿主 | 微信开发者工具 | 生成的产物能在目标宿主中编译、加载并运行 | 除此之外无其他结论，也不能证明宿主自身行为没有回归 |

**Kotlin 或 Node.js 测试通过，不等于微信小程序集成通过。** `:kmp-miniapp-sdk:jsNodeTest`、`npm run smoke`、`npm run typecheck` 的通过结果与真实宿主结果必须始终分别记录。Node.js 只是本地构建与测试环境，生产宿主是微信小程序 JavaScript runtime。

## 2. 自动化层

### Source set 边界

- `commonTest` 承载宿主无关的预期，不得引用 `wx`、`external` 或 `js()`。
- `jsTest` 承载微信专属预期，可以使用原始 JavaScript 构造宿主结果形状。

### FakeHost 边界 —— `commonTest/.../testing`

供共享代码使用的宿主无关替身：

- `FakeMiniAppHost` —— 完全由 in-memory 实现支撑的 `MiniAppHost`，capability support 可配置。
- `InMemoryStorage` —— 参考 `MiniAppStorage` 实现。
- `RecordingHttpTransport` —— 参考 `MiniAppHttpTransport` 实现，会记录请求并返回一个编排好的 response。

测试共享逻辑时应使用这些替身，这样失败指向的是契约，而不是当前 runtime。

### FakeAdapter 边界 —— `jsTest/.../host/wechat/testing`

供原始微信 callback port 使用的替身，使微信 adapter 无需微信 runtime 即可被驱动：

- `FakeWechatStorageHost`、`FakeWechatAuthHost`、`FakeWechatNetworkHost` —— 各自立即返回一个编排好的结果，并能复现 SDK 契约不允许的宿主行为，例如存储读取返回非字符串值。
- `fakeWxLoginSuccess`、`fakeWxRequestSuccess`、`fakeWxFailure` 等 —— 原始结果对象的构造器，这些对象没有 Kotlin 构造函数。
- `fakeAbortableTask` —— `wx.request` 返回的 task handle，会统计 `abort` 的调用次数。

必须保持静默的 port（例如用于取消场景的 port）应随其场景放置，而不是放进这个共享边界。

### 契约检查

一条契约检查把某个 capability 的一项保证表达为「接收被测实现」的函数。因此同一个检查对象既能跑在宿主无关的参考实现上，也能跑在真实宿主 adapter 上。

- `StorageContractChecks` —— 完整的 storage 契约：key 不存在、写入、覆盖、空值区别于 key 不存在、删除、幂等删除、key 相互独立、跨 key 隔离。
- `HttpTransportContractChecks` —— 对任何 transport 都成立的保证：已完成的 exchange 被原样上报、request 被原样交给实现、HTTP 错误 status 是结果而非失败。

Storage 与 network 的检查各跑两遍：一遍跑在中性实现上（`MiniAppStorageContractTest`、`MiniAppHttpTransportContractTest`），一遍跑在微信 adapter 上（`WechatStorageContractTest`、`WechatNetworkContractTest`）。前者失败说明契约有问题，后者失败说明 adapter 有问题。

微信客户端 login 有意不作为公共 capability，因此 `commonTest` 中没有它的共享检查对象。其契约以 SDK 对外的 adapter 形式写在 `WechatAuthContractTest`，并通过 FakeAdapter 边界驱动。

新增 capability 保证时应加入共享检查对象，而不是某个 adapter 的测试类。仅对单一宿主成立的行为 —— 例如它使用哪个 callback、其错误词汇如何映射、exchange 是否可中止 —— 无法成为共享保证，应留在该 adapter 自己的 suite 中。

### 命令

```shell
./gradlew :kmp-miniapp-sdk:jsNodeTest
```

```shell
cd examples/wechat-miniprogram && npm run smoke && npm run typecheck
```

### 消费者集成夹具

`fixtures/miniapp-consumer` 是真实消费者构建，而不是临时夹具：与本仓库并列的普通 Gradle build，按 id 消费插件、按公共坐标消费 runtime。它覆盖临时目录中的 TestKit fixture 覆盖不到的那一层 —— 一个留在仓库里、像源码一样被评审的消费者形状项目。

| 检查 | 证据 |
| --- | --- |
| 插件按 id 解析，runtime 按发布坐标解析 | 夹具的 `plugins { }` 块与 `settings.gradle.kts` |
| `miniappMain` / `miniappTest` 存在，且测试 compilation 继承 `commonTest` | `build/test-results/miniappNodeTest/` 下实际执行出的报告，同时包含 `consumer.SharedTest` 与 `consumer.MiniAppTest` |
| runtime SDK 无需消费者声明即可解析 | `sdkVersion()`，它调用 `MiniAppSdk.VERSION` |
| bundle 满足其契约 | 夹具自身的 `verifyConsumerContract` 任务 |
| DSL 改变真实输出 | 同一任务，分别在带与不带 `-PminiappBundleDirectory` 时运行 |
| 宿主的消费路径可用 | `host/scripts/host-smoke.cjs`，以与页面相同的方式加载 bundle |

最后一项运行在 Node 上，只证明模块接线。**它不是微信宿主验收**，两者分别记录。单独完成的开发者工具验收使用基础库 3.17.3，页面渲染共享 greeting、计数与 SDK 版本，Console 报告 `fixture.result=PASS`。

### Gradle 插件套件

`:miniapp-gradle-plugin:test` 通过 Gradle TestKit 在临时消费者工程中驱动插件。它覆盖插件应用、缺少 Kotlin Multiplatform 时的失败、source set 提供与 compilation 归属、测试执行、runtime 依赖接线、`assembleMiniAppBundle` 契约、为该契约把关的 renderer 拒绝、重复执行的增量行为，以及 configuration cache 兼容性。它还覆盖 `miniapp { }` extension：规范的 `miniapp { wechat { ... } }` 块能编译并执行；配置的 bundle 目录确实是 bundle 的写入位置；项目之外的目录会被拒绝并给出可操作的错误信息；重复应用插件不会创建第二个 extension；以及微信是宿主配置而不是平台 extension 本身。

错误路径断言的是失败信息本身，而不是「构建失败了」：未应用 Kotlin Multiplatform 的项目；已把 `miniapp` 用于其他平台的同名 target（在插件应用阶段抛出 Kotlin 自身的诊断，而不是被静默替换）；未提供 Mini App variant 的共享 Kotlin Multiplatform 依赖；无法解析的 runtime 坐标；项目之外的 bundle 目录；以及携带 Compose 的 runtime classpath。每一条都指明消费者必须修改什么。

fixture 经本仓库的 composite build 解析 runtime SDK，因为目前尚未发布。它们断言真实输出 —— 实际执行出的测试报告，以及 bundle 中真实存在的文件 —— 而不是只断言任务名：任务存在不能证明它产出了任何东西。bundle 测试刻意把两个结论分开：插件不生成宿主 markup；以及 distribution 不携带 renderer。前者是关于生成文件的陈述，后者是关于依赖图的陈述，由 host-boundary 检查断言。

这些测试证明的是「产出了 distribution 以及它包含什么」，不证明任何宿主能够加载它；那需要真实宿主运行。

#### 该套件拒绝猜测的部分

`checkMiniAppHostBoundary` 读取的是已解析的依赖图，而 Gradle 的解析结果只列出解析成功的依赖。因此未能完整解析的 classpath 会以错误的理由通过检查 —— 不是「没有 renderer」而是「从未看过」—— 所以检查会拒绝它，并列出无法解析的坐标。藏在不可解析坐标背后的 renderer 无论如何都会让构建失败；区别在于失败信息说明了发生了什么。

#### 测试分层

| 层 | 证明什么 | 位置 |
| --- | --- | --- |
| Contract / unit | 插件 descriptor 到实现类、公共 runtime 坐标、extension 的平台/宿主层级形状、renderer 分类器的允许与拒绝清单、bundle 目录规则 | `miniapp-gradle-plugin/src/test/.../MiniAppPluginContractTest.kt` |
| TestKit | 插件应用、缺少 Kotlin Multiplatform 时的失败、source set 与其 compilation 归属、`miniappTest` 执行、runtime 接线、任务注册、DSL、下文列出的全部错误路径、bundle 契约、configuration cache 兼容性 | `miniapp-gradle-plugin/src/test/.../MiniAppGradlePluginTest.kt` |
| 持久消费者 fixture | 留在仓库中的普通消费者构建：clean 构建、`commonMain` 复用、两种 bundle 目录、bundle 内容、宿主的 CommonJS 消费路径 | `fixtures/miniapp-consumer` |
| 真实宿主 | 微信开发者工具能加载并运行该 bundle | 人工，记录于第 3 节 |

前三层是自动化且隔离的：TestKit fixture 位于临时目录，持久 fixture 从 clean 运行，二者都不读取开发者本机工程或绝对路径。第四层不自动化，Node 输出永远不会被当作宿主结果汇报。

### Gradle 插件集成套件入口

```shell
./gradlew verifyMiniAppGradlePluginIntegration
```

一个入口即可运行插件的 contract 与 TestKit 套件、保护 SDK 不引入 renderer 的架构边界检查，以及从 clean 状态运行的消费者 fixture。这是 CI 应当调用的命令。

README 是消费者的入口，因此由 `verifyMiniAppConsumerDocs` 守护：当两种语言记录的章节数或代码块序列不一致、当它引用的某个 `fixtures/miniapp-consumer/...` 路径已不存在、或当消费者要执行的两条命令 —— `./gradlew miniappTest` 与 `./gradlew assembleMiniAppBundle` —— 之一不再被记录时，它会失败。它读取结构与路径而非正文，且开销极低、无需网络，因此 `check` 依赖它。

它**刻意不接入 `check`**：该 fixture 会驱动一个编译 Kotlin/JS 并安装 npm 依赖的嵌套 Gradle 构建，因此让 `check` 依赖它会给每一次普通构建增加数分钟与一个网络依赖，并让 `check` 重新进入 Gradle。插件自身的套件已经通过该工程的 `check` 任务属于 `check`；这个入口增加的是 fixture 与 `check` 不会运行的 SDK 架构检查。

## 3. 真实宿主层

### 可复现检查清单

以下步骤是当前已实现能力的 DeveloperTools 清单。它不替代验证矩阵要求的 RealDevice 或 BackendRequired 检查。执行后应使用验证矩阵中的证据模板记录 commit、工具版本、基础库版本、结果和证据引用。

1. 在仓库根目录准备产物。

   ```shell
   ./gradlew buildMiniAppSdk
   ```

2. 安装示例的本地工具链。

   ```shell
   cd examples/wechat-miniprogram && npm install
   ```

3. 将 `examples/wechat-miniprogram` 作为小程序项目导入微信开发者工具并编译。
4. 打开 index 页面。network 检查会发起真实 HTTPS 请求，因此目标 host 必须已列入 request domain 白名单，或项目在关闭域名校验的情况下运行。
5. 确认页面与 console。

| 检查项 | 页面 | Console |
| --- | --- | --- |
| 版本 | `0.1.0-SNAPSHOT` | `[kmp-miniapp-sdk] sdkVersion: 0.1.0-SNAPSHOT` |
| Runtime lifecycle | `Runtime Lifecycle` 卡片显示 `FOREGROUND` 与页面 route | 无专门 console 行；卡片本身即证据 |
| Runtime detection | `Runtime Detection and Version Gate` 卡片显示 `PASS`、基础库版本与各支持状态 | `[kmp-miniapp-sdk] runtime detection: PASS baseLibrary=…, platform=…, runtime-detection=…, storage=Supported, ungated=Unsupported` |
| Permission | `Permission Lifecycle` 卡片在下述步骤后显示权限名与状态 | `[kmp-miniapp-sdk] permission query: PASS permission=microphone, state=…` |
| Privacy | `Privacy Authorization` 卡片显示宿主的要求与其协议名 | `[kmp-miniapp-sdk] privacy query: PASS requirement=…, contract=…` |
| Session check | `WeChat Session Check` 卡片显示 `VALID`、`INVALID` 或 `FAIL` | `[kmp-miniapp-sdk] session check: PASS check #N, state=Valid\|Invalid` |
| File system | `File System` 卡片的写入、检查、读取、删除四项均为 `PASS` | `[kmp-miniapp-sdk] filesystem write: PASS`、`filesystem access: PASS exists=true`、`filesystem read: PASS matched=true`、`filesystem remove: PASS`、`filesystem access: PASS exists=false` |
| Location | `Location` 卡片显示能力判定、权限、隐私要求，以及定位读取的 `PASS` | `[kmp-miniapp-sdk] location capability: PASS wechat.location=…`、`location permission query: PASS state=…`、`location privacy query: PASS requirement=…`、`location: PASS coordinatesValid=true, accuracyValid=true` |
| Scanner | `Scanner` 卡片显示能力判定，以及扫码的 `PASS`、`INTERRUPTED` 或 `FAIL` | `[kmp-miniapp-sdk] scanner capability: PASS wechat.scan-code=…`、`scan: PASS resultPresent=true, typeRecognized=true`、`scan: INTERRUPTED cause=indeterminate` |
| Media | `Media` 卡片显示能力判定，以及选择的 `PASS`、`INTERRUPTED` 或 `FAIL` | `[kmp-miniapp-sdk] media capability: PASS wechat.choose-media=…`、`media choose: PASS count=1, typesValid=true, metadataValid=true`、`media choose: INTERRUPTED cause=indeterminate` |
| Subscription Message | `Subscription Message` 卡片显示能力判定，以及请求的 `PASS`、`NOT CONFIGURED` 或 `FAIL` | `[kmp-miniapp-sdk] subscription capability: PASS wechat.request-subscribe-message=…`、`subscription request: PASS accepted=N, otherStatuses=N, signals=…`、`subscription request: FAIL reason=HostFailure, signal=…` |
| Network extensions | `Network Extensions` 卡片显示每项能力判定、当前网络，以及观察、上传、下载状态 | `[kmp-miniapp-sdk] network capabilities: PASS query=…, listener=…, upload=…, download=…`、`network type: PASS connected=true, type=WIFI`、`network observation: PASS events=N, last=…`、`upload check: PASS\|NOT CONFIGURED`、`download check: PASS\|NOT CONFIGURED` |
| Clipboard 与震动 | `Clipboard and Haptics` 卡片的写入、读取、短震动、长震动四项均为 `PASS` | `[kmp-miniapp-sdk] clipboard write: PASS`、`clipboard read: PASS matched=true`、`haptics short: PASS`、`haptics long: PASS` |
| Standard payment | `Standard Payment` 卡片显示能力答案，以及请求的 `PASS`、`NOT CONFIGURED`、`CANCELLED` 或 `FAIL` | `[kmp-miniapp-sdk] payment capability: PASS wechat.request-payment=…`、`payment request: NOT CONFIGURED`、`payment request: PASS interactionCompleted=true`、`payment request: CANCELLED cause=indeterminate`、`payment request: FAIL reason=<封闭标签>` |
| Storage | `Storage verification: PASS` | `[kmp-miniapp-sdk] storage: PASS first=first, overwritten=second, missing=null` |
| Client login code | `Client login code: PASS` | `[kmp-miniapp-sdk] auth bootstrap: PASS codeReceived=true, length=<正整数>` |
| Network | `Network verification: PASS` | `[kmp-miniapp-sdk] network: PASS status=200, bytes=<正整数>` |

6. 通过点击验证页面栈桥接。导航无法只在单个页面上验证，因为每次操作都会改变屏幕上停留的页面。

| 步骤 | 点击 | 预期 |
| --- | --- | --- |
| 1 | index 页面的 `Open second page (wx.navigateTo)` | 出现第二页。Console：`[kmp-miniapp-sdk] second page: SHOWN pages/second/index`，随后 `[kmp-miniapp-sdk] navigation: PASS wx.navigateTo /pages/second/index` |
| 2 | 第二页的 `Replace with third page (wx.redirectTo)` | 出现第三页。Console：`[kmp-miniapp-sdk] second page: UNLOADED pages/second/index`，随后 `[kmp-miniapp-sdk] navigation: PASS wx.redirectTo /pages/third/index` |
| 3 | 第三页的 `Go back (wx.navigateBack)` | 重新出现 index 页面，`FOREGROUND` 与 `pages/index/index`。Console：`[kmp-miniapp-sdk] navigation: PASS wx.navigateBack` |

第 2 步正是让 `redirectTo` 可观测的关键：第二页是被替换而不是被覆盖，因此第 3 步露出的是 index 页面而不是第二页。

7. 核对版本门控的状态。Runtime Detection 卡片的期望状态来自宿主，因此不需要改动代码即可观察：

| 情形 | 如何构造 | 预期 |
| --- | --- | --- |
| `Supported` | 在当前受支持的基础库版本打开页面 | `storage=Supported`、`runtime-detection=Supported` |
| `Unsupported` | 在任一基础库版本打开同一页面 | 未登记能力显示 `ungated=Unsupported` |
| `VersionDependent` | 开发者工具无法构造 | 见下方说明 |

`VersionDependent` 无法在开发者工具中复现：其可选的最低调试基础库为 2.21.4，高于该能力记录的 2.20.1 边界，因此不存在可构造的更低宿主环境。不要为迎合该限制而修改边界，不要伪造旧版本证据，也不要下载不受支持的旧版开发者工具。该状态由以下自动化覆盖承担：

- `HostVersionTest` 验证版本解析与逐段数值比较。
- `WechatCapabilityGateTest` 覆盖四种状态、`2.20.1` 边界本身、版本不可读时的回退，以及完全无法探测的宿主。
- `CapabilitySupportContractChecks` 在 Fake Host 与真实 `WechatHost` 两处运行同一组契约断言。
- 变异探针已实测并回滚：把版本比较方向取反后，方向项与边界项共 3 项失败。

卡片中的基础库版本必须与所选调试基础库一致，请记录下来：它是验证矩阵所要求的证据的一部分。该能力已完成开发者工具与真机验收，记录见 [微信真实宿主验证矩阵](../platforms/wechat/WECHAT_HOST_VERIFICATION-ch.md)。

8. 验证权限生命周期。页面加载期间不会请求任何权限，因此以下每一步都由点击触发。卡片初始显示宿主已有的状态：全新安装或清除小程序授权数据后为 `NotRequested`。

| 步骤 | 点击 | 预期 |
| --- | --- | --- |
| 1 | `Refresh permission state` | 从未询问过的宿主上为 `[kmp-miniapp-sdk] permission query: PASS permission=microphone, state=NotRequested` |
| 2 | `Request permission` 并允许 | `[kmp-miniapp-sdk] permission request: PASS permission=microphone, state=Granted` |
| 3 | `Refresh permission state` | 状态仍为 `Granted`：它来自宿主，而不是 SDK 的记忆 |
| 4 | 在宿主自身的设置中关闭该权限，然后 `Refresh permission state` | `state=Denied` |
| 5 | 再次 `Request permission` | `[kmp-miniapp-sdk] permission request: DENIED permission=microphone, state=Denied`。拒绝不是宿主失败，且不会出现第二次弹窗 |
| 6 | `Open settings` 并重新允许该权限 | 页面关闭后为 `[kmp-miniapp-sdk] permission settings: PASS permission=microphone, state=Granted` |

弹窗只允许由点击触发。如果 smoke test 或页面加载产生弹窗，那是缺陷，不是配置问题。权限状态属于用户，因此开发者工具运行不能替代验证矩阵中的真机运行。

该流程已在微信开发者工具（基础库 3.17.2）与 Android 真机（OnePlus PLQ110、Android 36、微信 8.0.76）上执行，包含完整走通 `Granted` → `Denied` → `DENIED` → `Granted`，且拒绝后没有第二次弹窗。第 1 步是例外：所用账号已持有决定，因此无法产出 `NotRequested`。该状态改由自动化测试覆盖 —— Fake Host 契约检查、针对缺失授权 entry 与 `true`/`false`/缺失值转换的 adapter 测试，以及首次弹窗的成功与拒绝路径；要在宿主上复现它需要更换账号或设备，或清除小程序的授权历史。

9. 验证隐私授权流程。它与上面的权限是不同条件，二者分别记录。调试基础库必须为 2.32.3 或更高，且小程序必须在 MP 后台隐私指引中声明收集类型，否则宿主没有可授权的内容。

| 步骤 | 点击 | 预期 |
| --- | --- | --- |
| 1 | 页面加载后读取卡片 | `[kmp-miniapp-sdk] privacy query: PASS requirement=REQUIRED, contract=…`。查询无副作用，因此没有弹出任何东西 |
| 2 | `Request privacy authorization` 并同意 | `[kmp-miniapp-sdk] privacy request: PASS result=Authorized`，随后卡片显示 `NOT_REQUIRED` |
| 3 | `Refresh privacy status` | 仍为 `NOT_REQUIRED`，来自宿主而非记忆 |
| 4 | 在开发者工具缓存中清除该账号的同意记录并重新加载 | 重新变为 `REQUIRED` |
| 5 | `Request privacy authorization` 并拒绝或关闭 | `[kmp-miniapp-sdk] privacy request: REFUSED result=Refused`，且要求仍为 `REQUIRED` |

第 5 步是一个状态而不是两个：微信没有提供区分「拒绝协议」与「关闭弹窗」的字段，SDK 只把可识别的拒绝报告为 `Refused`；未知失败显示为 `FAIL` 并保留为 `HostFailure`。`NOT_REQUIRED` 从不表示用户已同意，因为小程序未声明任何收集类型时宿主也会返回它。

10. 验证微信会话检查。它与上面的 login bootstrap 分别记录，因为有效会话不是身份，只有 login code 路径才能建立身份。该检查刻意在页面加载时、bootstrap 之前执行：获取 code 会刷新客户端登录态，从而掩盖已过期的会话。

| 步骤 | 操作 | 预期 |
| --- | --- | --- |
| 1 | 清除小程序登录态后打开页面 | 卡片在第 1 次检查显示 `INVALID`，且此时 bootstrap 尚未取得 code。Console：`[kmp-miniapp-sdk] session check: PASS check #1, state=Invalid` |
| 2 | 再次点击 `Check WeChat session` | 在仍未取得新 code 前保持 `INVALID` |
| 3 | 让 auth bootstrap 执行，或在 Auth 卡片调用 `wechatLogin()` | `codeReceived=true` 且长度为正数；code 本身从不显示或记录 |
| 4 | 再次点击 `Check WeChat session` | `VALID`。Console：`… check #N, state=Valid` |

`wx.checkSession` 的 fail callback 按宿主契约就是登录态失效，因此页面报告 `INVALID`，并且不依赖 `errMsg` 的语言或具体文本。只有 API 不存在或调用无法注册时才会拒绝 Promise，而不会伪装成 `VALID`。

11. 验证剪贴板与震动。页面加载期间不会触碰剪贴板或震动器，因此以下每一步都由点击触发。剪贴板步骤只与页面自己写入的固定测试字符串比较；页面从不显示或记录剪贴板里可能存在的其他内容，那属于用户。

| 步骤 | 点击 | 预期 |
| --- | --- | --- |
| 1 | `Write test text` | `[kmp-miniapp-sdk] clipboard write: PASS`，卡片显示 `Clipboard write: PASS` |
| 2 | `Read clipboard` | `[kmp-miniapp-sdk] clipboard read: PASS matched=true`，卡片显示 `Clipboard read: PASS`。不匹配时打印 `matched=false`，且不暴露任何一侧的内容 |
| 3 | `Short vibration` | `[kmp-miniapp-sdk] haptics short: PASS` |
| 4 | `Long vibration` | `[kmp-miniapp-sdk] haptics long: PASS` |

第 3、4 步必须在真机上执行。Console 行只记录微信接受了该调用；震动是否真的被感知，要由握持设备的人确认，任何自动化检查都无法证明。`getClipboardData` 不属于 `app.json.requiredPrivateInfos` 允许的字段，不应在该数组中声明。

12. 验证文件系统。页面加载期间不会触碰文件系统，因此以下每一步都由点击触发。页面只向固定文件名写入固定的非敏感字符串，从不显示或记录沙箱根或其他文件的内容。

| 步骤 | 点击 | 预期 |
| --- | --- | --- |
| 1 | `Write test file` | `[kmp-miniapp-sdk] filesystem write: PASS` |
| 2 | `Check file exists` | `[kmp-miniapp-sdk] filesystem access: PASS exists=true` |
| 3 | `Read test file` | `[kmp-miniapp-sdk] filesystem read: PASS matched=true` |
| 4 | `Remove test file` | `[kmp-miniapp-sdk] filesystem remove: PASS` |
| 5 | `Check removed file` | `[kmp-miniapp-sdk] filesystem access: PASS exists=false` |

顺序很重要：只有当第 4 步删除了文件，第 5 步才会读到 `exists=false`。删除不存在的文件会失败，遵循微信 `unlink` 契约，因此第 4、5 步不应连续执行两次。

13. 验证定位。它由三个彼此独立的条件把关，因此以下步骤依次走过能力判定、隐私协议与权限。页面加载期间不会读取位置，因此每一步都由点击触发。

| 步骤 | 点击 | 预期 |
| --- | --- | --- |
| 1 | `Check location capability` | `[kmp-miniapp-sdk] location capability: PASS wechat.location=Supported`，卡片显示同一状态。宿主没有该 API 时 `Unsupported` 也是正确结果 |
| 2 | `Refresh privacy status` | `[kmp-miniapp-sdk] location privacy query: PASS requirement=REQUIRED`。查询无副作用，因此没有弹出任何东西 |
| 3 | 在未接受协议前点击 `Get current location` | `[kmp-miniapp-sdk] location: PRIVACY_REQUIRED`。调用在触及宿主之前即被拒绝，且不出现弹窗 |
| 4 | `Request privacy authorization` 并同意 | `[kmp-miniapp-sdk] location privacy request: result=Authorized`，随后卡片显示 `NOT_REQUIRED` |
| 5 | `Refresh location permission` | 从未询问过的宿主上为 `[kmp-miniapp-sdk] location permission query: PASS state=NotRequested` |
| 6 | 在未授予权限前点击 `Get current location` | `[kmp-miniapp-sdk] location: DENIED permissionState=NotRequested`；该能力不会代替消费者弹出权限弹窗 |
| 7 | `Request location permission` 并允许 | `[kmp-miniapp-sdk] location permission request: PASS state=Granted` |
| 8 | `Get current location` | `[kmp-miniapp-sdk] location: PASS coordinatesValid=true, accuracyValid=true` |
| 9 | 在宿主设置中关闭该权限后再次点击 `Get current location` | `[kmp-miniapp-sdk] location: DENIED permissionState=Denied`，与第 3 步的隐私前置失败可区分 |

页面只校验返回值的形状 —— 纬度在 `-90..90` 内且有限、经度在 `-180..180` 内且有限、精度不为负 —— 从不渲染或记录经纬度，因此第 8 步那行是它打印的唯一证据。第 3、6 步不应被意外触发：如果页面加载或 smoke test 产生了弹窗或位置读取，那是缺陷而不是配置问题。

第 1、8 步的结论仅限于其字面含义。能力受支持只说宿主暴露了该 API，不代表读到了任何位置；读取成功只说宿主给出的位置能被契约承载，不代表该位置准确或当前有效。模拟器的结果由 IP 推导而非来自设备定位模块，因此开发者工具运行可以走通整个流程，但无法证明真实定位；只有真机可以。在真机上，操作系统还可能独立于 `scope.userLocation` 限制微信自身对定位的访问，因此即便 scope 已授予，在该项放行之前仍可能失败。

有三项配置决定它能否工作，且都在 SDK 之外：`getLocation` 必须列入 `app.json.requiredPrivateInfos`，`scope.userLocation` 必须在 `app.json.permission` 中声明并给出说明，且小程序必须在 MP 后台的接口设置中声明定位接口。缺少最后一项时，宿主会在用户看到任何东西之前就拒绝，这属于配置失败而不是 SDK 缺陷。

本能力同样执行并回滚了一次变异探针：把坐标范围判断取反后，interop、adapter 与契约三个层面共 9 项测试失败，说明形状校验确实被断言，而不是碰巧通过。

14. 验证扫码。它打开的是微信自身的扫码界面，因此页面加载期间不会打开任何界面，以下每一步都由点击触发。页面只报告内容是否存在、宿主报出的格式是否被识别，以及结果是成功、原因不可判定的中断还是其他失败；**从不显示或记录扫码内容、`rawData`、字符集或图片路径**。

| 步骤 | 点击 | 预期 |
| --- | --- | --- |
| 1 | `Check scanner capability` | `[kmp-miniapp-sdk] scanner capability: PASS wechat.scan-code=Supported`，卡片显示同一状态。宿主没有该 API 时 `Unsupported` 也是正确结果 |
| 2 | 页面加载后确认 Console 与页面 | 没有出现扫码界面，也没有任何 `scan:` 行；卡片保持 `NOT RUN` |
| 3 | `Scan code` 并扫描一个普通测试二维码 | `[kmp-miniapp-sdk] scan: PASS resultPresent=true, typeRecognized=true`，且 Console 中没有出现扫码内容 |
| 4 | `Scan code` 后主动取消 | `[kmp-miniapp-sdk] scan: INTERRUPTED cause=indeterminate` |
| 5 | 在系统设置中限制微信的相机权限，然后点击 `Scan from camera only` | 同样为 `scan: INTERRUPTED cause=indeterminate`；这证明宿主没有提供足够信息区分取消与相机限制 |

第 3 步只证明 SDK 交回了契约能承载的结果，不证明内容被业务解析过——本能力不做任何业务解析。第 4、5 步共同证明宿主把用户取消与相机限制压成同一信号；SDK 必须保留这种不确定性，不能将其报告为 `UserCancelled` 或 `PermissionDenied`。

中断分类只精确匹配两种宿主消息（`scanCode:cancel` 与 `scanCode:fail cancel`）；其他失败保持为 `HostFailure`。该结论来自真实宿主行为而不是对错误文本含义的猜测。

开发者工具不是真机：其扫码实现是让用户选一张图片再解码，因此相机路径与 `onlyFromCamera` 的实际行为只能在真机上判断。`Scan from camera only` 只是把 `onlyFromCamera=true` 交给微信，用于防止相册回退；它不查询、不请求也不假定 `scope.camera`。示例没有向 `app.json.requiredPrivateInfos` 添加任何扫码字段。系统或微信自身可能在扫码界面中处理相机访问，这不等于 SDK 建立了权限前置条件。

15. 验证媒体选择。它打开的是微信自身的选择界面，因此页面加载期间不会打开任何界面，以下每一步都由点击触发。页面只报告返回了几个文件、宿主报出的每个类别是否被 SDK 识别、元数据是否可用；**从不显示或记录所选媒体、base64、文件名或完整临时路径**。

| 步骤 | 点击 | 预期 |
| --- | --- | --- |
| 1 | `Check media capability` | `[kmp-miniapp-sdk] media capability: PASS wechat.choose-media=Supported`，卡片显示同一状态。宿主没有该 API 时 `Unsupported` 也是正确结果 |
| 2 | 页面加载后确认 Console 与页面 | 没有出现选择界面，也没有任何 `media choose:` 行；卡片保持 `NOT RUN` |
| 3 | `Choose image` 并选择一张测试图片 | `[kmp-miniapp-sdk] media choose: PASS count=1, typesValid=true, metadataValid=true` |
| 4 | `Choose video` 并选择一个测试视频 | 同一行且 `count=1`；在真机上记录 `durationSeconds`、`width`、`height` 是被报告了还是缺失 |
| 5 | `Choose image or video` | 无论返回哪一类，`typesValid` 都应为 `true` |
| 6 | `Choose from camera only` 并拍摄一张照片或一段视频 | 选择界面打开的是相机而不是相册 |
| 7 | `Choose image` 后主动关闭选择界面 | `[kmp-miniapp-sdk] media choose: INTERRUPTED cause=indeterminate` |
| 8 | 限制微信访问照片后 `Choose image` | 失败，或得到同一 `INTERRUPTED` 行 —— **记录是哪一种**，因为这一步的观察决定该宿主能否区分「受限」与「主动关闭」 |

开发者工具把模拟关闭报告为 `chooseMedia:cancel`，已验收的 Android 真机把主动关闭报告为 `chooseMedia:fail cancel`；这两条精确信号都分类为中断。已验收的受限访问运行在该 Android 宿主上产生同样的不可归因中断，因此 SDK 不声称能够区分原因；在宿主证据支持修改分类前，其他消息均保持为宿主失败。

第 3 步只证明 SDK 交回了契约能承载的文件，不证明有任何东西解码或上传过它们——本能力两者都不做。成功回调若不含任何所选文件属于无效宿主响应，不会产生空集意义下的 `count=0` PASS。

页面不自行读取媒体，示例也不把临时文件复制到任何地方。宿主返回的路径属于产生它的那次会话，请按短期资源对待：如果媒体需要在本次运行之后仍然存在，请自行复制到你拥有的存储中。

16. 验证订阅消息请求。微信要求用户手势，因此页面加载期间不会询问宿主，以下每一步都由点击触发。页面只打印计数，从不打印模板 ID：用户订阅了哪些模板，是用户与小程序之间的事，截图不得携带它。

| 步骤 | 点击 | 预期 |
| --- | --- | --- |
| 1 | `Check subscription capability` | `[kmp-miniapp-sdk] subscription capability: PASS wechat.request-subscribe-message=Supported` |
| 2 | 页面加载后确认 Console 与页面 | 没有出现弹窗，也没有任何 `subscription request:` 行；卡片保持 `NOT RUN` |
| 3 | 本地未配置测试模板时点击 `Request subscription` | `[kmp-miniapp-sdk] subscription request: NOT CONFIGURED templateCount=0`，且不出现弹窗：页面完全不调用宿主 |
| 4 | 在本地配置一个测试模板 ID（不要提交它），点击 `Request subscription` 并同意 | 若宿主返回 `accept`，为 `[kmp-miniapp-sdk] subscription request: PASS accepted=1, otherStatuses=0, signals=accept` |
| 5 | 点击 `Request subscription` 并拒绝 | 同一摘要但计数不同。**记录宿主用于拒绝的状态字符串**，因为本 SDK 没有它的证据，只能原样保留 |
| 6 | 点击 `Request subscription` 后主动关闭弹窗 | `subscription request: FAIL reason=HostFailure, signal=…`；反馈封闭的 `signal` 标签（`cancel`、`fail-cancel`、`cancel-like` 或 `other`），仅在有证据后增加精确映射 |
| 7 | 配置两个测试模板，同意其中一个并拒绝另一个，然后点击 `Request subscription` | `PASS`，计数之和为 2，且按给定顺序为每个请求的模板各给一条 |

`templateCount=0` 不是宿主拒绝，也没有执行 `wx.requestSubscribeMessage`，因此第 3 步不可能出现订阅同意弹窗。要执行第 4–7 步，必须先在本次测试所用的同一 AppID 下添加订阅消息模板，再仅把该模板 ID 放入示例本地的 `subscriptionTemplateIds` fixture。不得编造 ID、复用其他 AppID 的 ID 或提交该值。如果账号无法提供有效模板，将第 4–7 步记录为 `NOT RUN — blocked by AppID template configuration`。

第 5、6 步承载的正是本仓库目前没有的信息。该能力的状态词汇无法从可离线来源核实，因此 SDK 只识别 `accept`，其余非空白状态一律原样保留。第 6 步有意保持 `HostFailure`；其安全标签用于判断是否有依据增加精确的中断映射，同时不暴露原始宿主数据。请把两者都报告出来，而不是直接把该步记为通过。

第 4 步只证明 SDK 交回了宿主的逐模板答案，不证明存在任何消息。同意订阅是一种订阅状态；消息是否被发送或送达属于 `BackendRequired` 问题，背后需要微信后台模板与可信后端。

Adapter 要求精确关联：缺少或多出的模板键、空白或非文本状态均为 `InvalidResponse`，绝不会被当成成功的“沉默”答案。

17. 验证网络扩展。页面加载期间不会查询、注册监听、上传或下载，因此每一步都由点击触发。页面从不打印 URL、header、文件路径或响应正文；它报告状态、字节数、事件数与是否触发了 abort。

| 步骤 | 点击 | 预期 |
| --- | --- | --- |
| 1 | `Check network capabilities` | `[kmp-miniapp-sdk] network capabilities: PASS query=Supported, listener=Supported, upload=Supported, download=Supported`，宿主缺少哪个 API 就显示 `Unsupported` |
| 2 | `Get current network type` | `[kmp-miniapp-sdk] network type: PASS connected=true, type=<已识别类型或 unrecognized>` |
| 3 | `Start network status observation`，切换设备连接（Wi-Fi 与蜂窝互切，或断开再恢复），然后 `Stop network status observation` | `[kmp-miniapp-sdk] network observation: PASS events=<大于 0 的计数>, last=<你切换到的类型>`。**这一步需要真机**：模拟器没有可切换的连接 |
| 4 | 未启动观察时再次点击 `Stop network status observation` | `[kmp-miniapp-sdk] network observation: PASS events=0, last=none` |
| 5 | 未配置 endpoint 时点击 `Run upload check` | `[kmp-miniapp-sdk] upload check: NOT CONFIGURED`，且设备不发出任何请求 |
| 6 | 未配置 endpoint 时点击 `Run download check` | `[kmp-miniapp-sdk] download check: NOT CONFIGURED` |
| 7 | 在本地配置一个受控 HTTPS endpoint（不要提交它），将其加入 request domain 白名单，然后 `Run upload check` | `[kmp-miniapp-sdk] upload check: PASS status=<2xx 或服务返回的状态>, bytes=<长度>, progressSeen=<true\|false>` |
| 8 | 对同一服务点击 `Run download check` | `[kmp-miniapp-sdk] download check: PASS status=…, fileReported=true, progressSeen=…` |
| 9 | 对一个较大或较慢的 endpoint 发起检查，并在进行中点击 `Cancel upload`（或 `Cancel download`） | `[kmp-miniapp-sdk] upload cancel: PASS abortInvoked=true`，卡片显示 `CANCELLED`；该传输不会给出结果 |

第 3、7、8、9 步是自动化无法产出的部分。第 3 步需要一台能够真正切换连接的真机；第 7 至 9 步需要一个受控的 HTTPS 服务，属 `BackendRequired`——公共 endpoint 不能作为证据，`https://example.com/` 也不是上传服务。

第 5 步是示例默认状态诚实性的保障：未配置时页面完全不调用宿主，因此不会有任何上传被误当成通过的检查。第 7 或 8 步出现 `progressSeen=false` 并不代表失败：小传输可能在宿主报告任何进度之前就已完成。

18. 验证标准支付。页面加载期间不会触碰支付界面，因此每一步都由点击触发。页面的日志行中不会出现任何支付参数、签名、商户标识或宿主自身的失败文本；失败会被归约为一个封闭标签。

| 步骤 | 点击 | 期望结果 |
| --- | --- | --- |
| 1 | `Check payment capability` | `[kmp-miniapp-sdk] payment capability: PASS wechat.request-payment=Supported`，在缺少该 API 的宿主上为 `Unsupported` |
| 2 | 未配置任何参数时点击 `Request payment` | `[kmp-miniapp-sdk] payment request: NOT CONFIGURED`，且完全不调用宿主：页面在调用 SDK 之前先检查本地占位配置 |
| 3 | 在具备合法商户环境时，把后端参数填入页面的本地占位配置（绝不提交），然后点击 `Request payment` 并完成支付 | `[kmp-miniapp-sdk] payment request: PASS interactionCompleted=true`。**这不是订单事实**，必须用可信后端确认，后端从微信支付服务端 API、异步通知或订单查询获知真相，并以该服务端结果作为证据 |
| 4 | 点击 `Request payment` 并主动关闭支付界面 | `[kmp-miniapp-sdk] payment request: CANCELLED cause=indeterminate`，且卡片不声称用户取消。真实主动关闭究竟产生 `requestPayment:cancel` 还是 `requestPayment:fail cancel`，正是这次运行要确定的待办项 |
| 5 | 用宿主会拒绝的参数点击 `Request payment`（例如已过期或已使用的预支付包） | `[kmp-miniapp-sdk] payment request: FAIL reason=host-failure`，页面与 Console 中都没有宿主文本 |

步骤 3 至 5 在此处完全无法产出：它们需要绑定本小程序 AppID 的合法微信支付商户号、真实订单，以及完成签名的可信后端，即 `BackendRequired`。开发者工具的模拟器不是商户，它显示的任何内容都不能被记为支付结果。步骤 2 是「已提交的默认状态是诚实的」这一保证，也是这些步骤中唯一不需要该环境的一项。

19. 记录你观察到了哪些检查项、哪些没有观察到。只有在某项 capability 完成真实宿主运行后，才会在 [PROJECT_FACTS-ch.md](PROJECT_FACTS-ch.md) 中被记录为已通过宿主验证。

Storage 检查会写入专用测试 key、验证覆盖、删除该 key，并确认 missing key 读取为 `null`。Network 检查会向 `https://example.com/` 发起 `GET`，并报告 status code 与 body 长度；验证其他 host 时请修改示例中的 `networkUrl` 常量。

App 级生命周期的 `BACKGROUND` 状态无法从开发者工具模拟器触发。要观察它，需要在真机上让小程序进入后台；因此模拟器运行可以确认前台状态与页面 route，但无法确认后台迁移。

login code 是短期凭证，不会被记录或渲染。`wx.login` code 必须发送到可信的消费者后端与微信交换；取得 code 不代表用户已认证，也不能授权请求。

### 不在范围内

自动驱动微信开发者工具、完整端到端自动化，以及支付测试，均不属于本层。本仓库中没有任何自动化测试声称能够访问真实 `wx` runtime。
