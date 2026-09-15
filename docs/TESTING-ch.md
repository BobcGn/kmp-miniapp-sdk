# 测试

[English](TESTING-en.md)

本文档描述两层测试体系、各层可使用的 fake，以及可复现的真实微信小程序运行检查清单。

两层是“自动化”和“真实宿主”的最高层分类。发布证据进一步细分为 Unit、Contract、Node、DeveloperTools、RealDevice 和 BackendRequired。各能力最低需要哪个等级、如何留存证据以及何时回归，见 [微信真实宿主验证矩阵](platforms/wechat/WECHAT_HOST_VERIFICATION-ch.md)。

## 1. 两层，两类不同的结论

| 层次 | 运行环境 | 能证明 | 不能证明 |
| --- | --- | --- | --- |
| 自动化 | Node.js，通过 Gradle Wrapper 与示例的 npm scripts | 共享 Kotlin 逻辑、通过 fake port 驱动的 adapter 行为、模块与类型声明形状、TypeScript 契约 | 任何关于微信小程序 runtime 的事实 |
| 真实宿主 | 微信开发者工具 | 生成的产物能在目标宿主中编译、加载并运行 | 除此之外无其他结论，也不能证明宿主自身行为没有回归 |

**Kotlin 或 Node.js 测试通过，不等于微信小程序集成通过。** `:sdk:jsNodeTest`、`npm run smoke`、`npm run typecheck` 的通过结果与真实宿主结果必须始终分别记录。Node.js 只是本地构建与测试环境，生产宿主是微信小程序 JavaScript runtime。

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
./gradlew :sdk:jsNodeTest
```

```shell
cd examples/wechat-miniprogram && npm run smoke && npm run typecheck
```

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
| Clipboard 与震动 | `Clipboard and Haptics` 卡片的写入、读取、短震动、长震动四项均为 `PASS` | `[kmp-miniapp-sdk] clipboard write: PASS`、`clipboard read: PASS matched=true`、`haptics short: PASS`、`haptics long: PASS` |
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

13. 记录你观察到了哪些检查项、哪些没有观察到。只有在某项 capability 完成真实宿主运行后，才会在 [PROJECT_FACTS-ch.md](PROJECT_FACTS-ch.md) 中被记录为已通过宿主验证。

Storage 检查会写入专用测试 key、验证覆盖、删除该 key，并确认 missing key 读取为 `null`。Network 检查会向 `https://example.com/` 发起 `GET`，并报告 status code 与 body 长度；验证其他 host 时请修改示例中的 `networkUrl` 常量。

App 级生命周期的 `BACKGROUND` 状态无法从开发者工具模拟器触发。要观察它，需要在真机上让小程序进入后台；因此模拟器运行可以确认前台状态与页面 route，但无法确认后台迁移。

login code 是短期凭证，不会被记录或渲染。`wx.login` code 必须发送到可信的消费者后端与微信交换；取得 code 不代表用户已认证，也不能授权请求。

### 不在范围内

自动驱动微信开发者工具、完整端到端自动化，以及支付测试，均不属于本层。本仓库中没有任何自动化测试声称能够访问真实 `wx` runtime。
