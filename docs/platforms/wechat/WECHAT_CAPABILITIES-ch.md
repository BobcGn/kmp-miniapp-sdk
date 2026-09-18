# 微信能力矩阵

[English](WECHAT_CAPABILITIES-en.md)

本文档是微信宿主能力状态的事实索引，不是路线图。源码和可执行 Gradle 配置优先于本文档。只有具备实现、自动测试和规定层级的宿主验证证据时，能力才能标记为 `Stable`。各等级的环境、步骤、证据格式和回归频率见 [微信真实宿主验证矩阵](WECHAT_HOST_VERIFICATION-ch.md)。

## 状态与验证等级

| 状态 | 含义 |
| --- | --- |
| `Stable` | 当前范围已实现，并完成相应自动测试和微信开发者工具验证。 |
| `Experimental` | 已有可运行实现或概念验证，但 API 或行为尚不稳定。 |
| `Partial` | 仅实现了列出的部分 API、语义或验证层级。 |
| `Planned` | 尚未实现；Issue 或计划不构成实现证据。 |
| `Unsupported` | 当前 SDK 明确不提供。 |
| `P3-Presentation` | 原生视图或展现层，不属于 P1 Host Capability。 |

| 验证等级 | 含义 |
| --- | --- |
| `Unit` | Kotlin/JS 单元测试或聚焦的 adapter 测试。 |
| `Contract` | 同一语义契约同时验证参考实现和微信 adapter。 |
| `Node` | CommonJS smoke 或 TypeScript 检查；不能证明微信运行时兼容。 |
| `DeveloperTools` | 已在微信开发者工具中编译、加载并运行。 |
| `RealDevice` | 必须或已经在微信真机环境验证。 |
| `BackendRequired` | 完整验收需要消费者后端或受控测试服务。 |

`Minimum Host Version` 记录提供该能力的最低基础库，共有三种取值：

- `Resolved at runtime` —— SDK 对该能力做了门控，通过 `wx.canIUse` 询问宿主，而不是与记录下来的数字比较。当前 Storage API、`wx.request`、`wx.getLocation`、`wx.scanCode`、`wx.chooseMedia`、`wx.requestSubscribeMessage` 与各网络扩展 API 的官方入口页没有标注 API 本身的引入版本，因此不从开发机表现反推数字。这是针对当前真正运行的宿主的实时答案，而不是缺失值。
- 版本号 —— 微信有文档记载的边界。仓库只在能够引用来源时才记录。
- `Not established` —— 尚未确立最低版本，原因或是该能力尚未实现，或是 SDK 未对其做门控。

SDK 从 `wx.getAppBaseInfo` 读取基础库版本，并在早于它的基础库上回退到已停止维护的 `wx.getSystemInfoSync`。

## 证据来源

本矩阵于 2026-09-15 对照以下事实来源建立：

- [PROJECT_FACTS-ch.md](../../PROJECT_FACTS-ch.md)：当前实现与宿主验证事实。
- [ARCHITECTURE-ch.md](../../ARCHITECTURE-ch.md)：Host、Capability、平台逃生口、异步与错误边界。
- [TESTING-ch.md](../../TESTING-ch.md)：Unit、Contract、Node 与 DeveloperTools 证据和限制。
- [`commonMain` capability contracts](../../../sdk/src/commonMain/kotlin/io/github/bobcgn/miniapp/capability)：Storage、HTTP transport 与 App lifecycle 公共契约。
- [`jsMain` WeChat host](../../../sdk/src/jsMain/kotlin/io/github/bobcgn/miniapp/host/wechat)：微信 interop、adapter、runtime 与 platform API。
- [`jsMain` export facade](../../../sdk/src/jsMain/kotlin/io/github/bobcgn/miniapp/export)：JavaScript / TypeScript consumer surface。
- [`commonTest`](../../../sdk/src/commonTest) 与 [`jsTest`](../../../sdk/src/jsTest)：共享契约和微信 adapter 测试。
- [ADR-0006](../../decisions/0006-lifecycle-and-navigation-boundary-ch.md)：App、Page 与导航边界。
- [ADR-0008](../../decisions/0008-permission-lifecycle-boundary-ch.md)：权限生命周期、其用户手势规则，以及它与隐私保持分离的原因。
- [ADR-0009](../../decisions/0009-privacy-authorization-boundary-ch.md)：隐私授权边界，包括拒绝为何是结果、以及为何不存在 `Cancelled` 状态。
- [微信 `wx.getPrivacySetting` 文档](https://developers.weixin.qq.com/miniprogram/dev/api/open-api/privacy/wx.getPrivacySetting.html) 与 [`wx.requirePrivacyAuthorize` 文档](https://developers.weixin.qq.com/miniprogram/dev/api/open-api/privacy/wx.requirePrivacyAuthorize.html)：基础库 2.32.3 起支持；低于该版本宿主不会拦截隐私相关调用。
- [微信 `wx.canIUse` 文档](https://developers.weixin.qq.com/miniprogram/dev/api/base/wx.canIUse.html)：基础库 1.1.1 起支持。
- [微信 `wx.getAppBaseInfo` 文档](https://developers.weixin.qq.com/miniprogram/dev/api/base/system/wx.getAppBaseInfo.html) 与 [`wx.getDeviceInfo` 文档](https://developers.weixin.qq.com/miniprogram/dev/api/base/system/wx.getDeviceInfo.html)：基础库 2.20.1 起支持。
- [微信 `wx.getSystemInfoSync` 文档](https://developers.weixin.qq.com/miniprogram/dev/api/base/system/wx.getSystemInfoSync.html)：从基础库 2.20.1 起停止维护，是旧基础库读取版本与平台的回退路径。
- [微信 `RequestTask.abort` 文档](https://developers.weixin.qq.com/miniprogram/dev/api/network/request/RequestTask.abort.html)：基础库 1.4.0 起支持。
- [微信 `wx.getSetting` 文档](https://developers.weixin.qq.com/miniprogram/dev/api/open-api/setting/wx.getSetting.html) 与 [`wx.authorize` 文档](https://developers.weixin.qq.com/miniprogram/dev/api/open-api/authorize/wx.authorize.html)：基础库 1.2.0 起支持。
- [微信 `wx.openSetting` 文档](https://developers.weixin.qq.com/miniprogram/dev/api/open-api/setting/wx.openSetting.html)：基础库 1.1.0 起支持，且自基础库 2.3.0 起只能由用户手势调用。
- [微信 `wx.getFileSystemManager` 文档](https://developers.weixin.qq.com/miniprogram/dev/api/file/wx.getFileSystemManager.html) 以及 [`FileSystemManager.readFile`](https://developers.weixin.qq.com/miniprogram/dev/api/file/FileSystemManager.readFile.html)、[`writeFile`](https://developers.weixin.qq.com/miniprogram/dev/api/file/FileSystemManager.writeFile.html)、[`access`](https://developers.weixin.qq.com/miniprogram/dev/api/file/FileSystemManager.access.html)、[`unlink`](https://developers.weixin.qq.com/miniprogram/dev/api/file/FileSystemManager.unlink.html)：基础库 1.9.9 起支持。`access` 与 `unlink` 页面把 `no such file or directory` 记录为路径不存在的失败文本，`wechatFileExists` 即据此分类。
- [微信 `wx.getClipboardData` 文档](https://developers.weixin.qq.com/miniprogram/dev/api/device/clipboard/wx.getClipboardData.html) 与 [`wx.setClipboardData` 文档](https://developers.weixin.qq.com/miniprogram/dev/api/device/clipboard/wx.setClipboardData.html)：基础库 1.1.0 起支持。
- [微信 `wx.vibrateShort` 文档](https://developers.weixin.qq.com/miniprogram/dev/api/device/vibrate/wx.vibrateShort.html) 与 [`wx.vibrateLong` 文档](https://developers.weixin.qq.com/miniprogram/dev/api/device/vibrate/wx.vibrateLong.html)：基础库 1.2.0 起支持。`wx.vibrateShort` 文档中的 `type` 字段（heavy / medium / light）自基础库 2.13.0 起支持，有意未建模。
- [微信 `wx.checkSession` 文档](https://developers.weixin.qq.com/miniprogram/dev/api/open-api/login/wx.checkSession.html)：未标注最低基础库，因此该项依靠 `wx.canIUse` 而不是记录下来的数字。该文档把 success/fail 分别定义为登录态有效/过期，因此 adapter 使用 callback 本身，不解析 `errMsg`。
- 微信开发者工具所带基础库的实测契约（`package.nw/core.wxvpkg`）：`wx.scanCode` 的请求选项 `onlyFromCamera` 与四个请求类别（`barCode`、`qrCode`、`datamatrix`、`pdf417`）；其 success 字段 `result`、`scanType`、`charSet`、`path` 与 `rawData`；其结果格式（`QR_CODE`、`AZTEC`、`CODABAR`、`CODE_39`、`CODE_93`、`CODE_128`、`DATA_MATRIX`、`EAN_8`、`EAN_13`、`ITF`、`MAXICODE`、`PDF_417`、`RSS_14`、`RSS_EXPANDED`、`UPC_A`、`UPC_E`、`UPC_EAN_EXTENSION`、`WX_CODE`、`CODE_25`）；以及其模拟路径与「用户关闭选图弹窗」都会产出的取消消息 `scanCode:cancel`。这属于宿主契约证据而不是文档页面证据，因此本条把取消分类记为限制而不是有文档保证的行为。同一基础库的授权 scope 列表中也存在 `scope.camera`，但其中没有任何内容把它与 `wx.scanCode` 绑定，因此 SDK 不为该能力映射任何权限。
- 同一基础库中网络扩展的实测契约：`getNetworkType` 枚举 `wifi`、`2g`、`3g`、`4g`、`5g`、`unknown`、`none`，而 `onNetworkStatusChange` 在 `isConnected` 旁携带同一集合但不含 `5g`；`uploadFile` 接收 `url`、`filePath`、`name`、`header`、`formData`、`timeout`，并返回 `statusCode` 与 `data`；`downloadFile` 接收 `url`、`header`、`timeout`、`filePath`，并返回 `tempFilePath`、`filePath` 与 `statusCode`；两种 task 类型都列出 `abort`、`onProgressUpdate` 与 `offProgressUpdate`（`RequestTask` 没有）；该库自身的传输错误映射产出 `<api>:fail timeout`，正是本 SDK 映射为超时的那条确切消息。该库的传输记账把 `progress` 作为整数百分比与按方向的字节数一并发出，也就是进度读取器所接受的形状。
- 同一基础库中 `wx.requestSubscribeMessage` 的实测契约：其元数据表声明了选项 `tmplIds`，以及以模板 ID 为键、旁边带 `errMsg` 的 success 结果（`"success":{"errMsg":1,"TEMPLATE_ID":1}`）。该 bundle 中**没有该 API 的实现，也没有参数 schema**，因此与上面的 `wx.scanCode`、`wx.chooseMedia` 不同，它没有给出任何逐模板状态取值，也没有给出关闭消息。整个 bundle 中唯一观察到的状态字符串是模拟器订阅弹窗预览载荷里的 `accept`（`WxaSubscribeStatusString:"accept"`），那属于弹窗渲染数据而不是 API 结果。因此只识别这一状态；在真机运行提供证据之前不分类任何关闭形式。
- 同一基础库中 `wx.chooseMedia` 的实测契约：其参数 schema（`IChooseMediaOption`）把 `mediaType` 标为 required，取值为 `image`、`mix`、`video`；`sourceType` 为 `album`、`camera`；`camera` 为 `back`、`front`；`maxDuration` 为「3s 至 60s，不限制相册」；`count` 未标注上限；`sizeType` 是字符串数组且自身没有 enum。其模拟实现为每个 `tempFiles` 条目给出 `tempFilePath`、`size` 与 `fileType`，仅对视频补充 `duration`、`width`、`height` 与 `thumbTempFilePath`，并把主动关闭报告为 `chooseMedia:cancel`。`scope.writePhotosAlbum` 存在于该基础库的 scope 列表，但其中没有任何内容把它与 `chooseMedia` 绑定；相册保存 API 在本轮未实现，因此该能力不映射任何权限。
- 同一基础库中 `wx.requestPayment` 的实测契约：其元数据恰好声明五个请求字段 —— `timeStamp`、`nonceStr`、`package`、`signType` 与 `paySign`；把 `signType` 限制为 `MD5` 与 `HMAC-SHA256`；并记录 success 形状为空，因此 callback 本身就是完整的完成信号，本 SDK 不从其载荷读取任何字段。该库自身的支付流程把「交互结束」报告为 `requestPayment:cancel`，这是本 SDK 唯一分类为交互中断的消息；其模拟成功路径返回支付后端的 `payment_ret`，并把 `err_msg` 前缀改写为 API 名称；普通失败为 `requestPayment:fail <reason>`。该库中没有任何内容把签名 scope 或权限与该 API 绑定，因此 SDK 不映射任何权限。真实关闭信号与真实订单结果需要具备合法商户账号的真机，属于 `BackendRequired`。
- 虚拟支付在同一 bundle 中做了探测，结论是**其中不存在可发现的契约**。该 18,152,387 字节的基础库中 `requestPayment` 出现 7 次 —— 包括其实现注册与 `canIUse` 元数据表条目 `"requestPayment":{"object":{"timeStamp":1,"nonceStr":1,"package":1,"signType":["MD5","HMAC-SHA256"],"paySign":1},"success":{}}` —— 而 `requestVirtualPayment`、`virtualPayment`、`VirtualPayment` 在整个文件中出现 **0** 次（元数据表与其余位置皆无）。相邻的支付名称确实存在，但只出现在工具自身的内部 mock 列表里（`requestOrderPayment` 1 次、`requestApplePayment` 2 次），那不是 API 契约；「虚拟」二字只出现在与该能力无关的工具界面字符串中。**这证明了什么：** 该开发者工具 runtime 中没有可发现的虚拟支付 API 契约，因此模拟器无法为该能力提供正向证据。本次探测没有实际执行 `wx.canIUse('requestVirtualPayment')`，所以不声称其运行时返回值。**这不能证明什么：** 任何真实微信客户端的行为、参数表、结果形状、最低基础库版本、分平台可用性与资格规则 —— 桌面 bundle 中缺少条目，不构成关于手机 runtime 的证据。本环境无法抓取官方页面（在此访问 `developers.weixin.qq.com` 被阻断，搜索只返回链接列表）：限定官方域名的搜索列出标题为 `# wx.requestVirtualPayment(Object object)` 的页面，URL 为 `https://developers.weixin.qq.com/miniprogram/dev/api/payment/wx.requestVirtualPayment.html`，查询日期 2026-09-16。这只确认该 API 由微信文档记载并给出名称；搜索摘要不是契约证据，因此不从中记录任何字段列表、版本或平台矩阵。Virtual Payment 行所需的全部细节都属于**待确认**，必须在任何实现之前从该官方页面取得。
- [微信 `wx.getLocation` 文档](https://developers.weixin.qq.com/miniprogram/dev/api/location/wx.getLocation.html)：`type` 的取值 `wgs84` 与 `gcj02`，SDK 只转发而不解释；以及宿主作答前必须具备的声明 —— `app.json.requiredPrivateInfos`、`permission.scope.userLocation`，以及 MP 后台开启的接口权限。该文档未标注 API 的引入基础库版本，因此该项依靠 `wx.canIUse` 而不是记录下来的数字。[`wx.chooseLocation`](https://developers.weixin.qq.com/miniprogram/dev/api/location/wx.chooseLocation.html) 有意未实现：其当前参数表没有 `cancel` callback，取消会以 `fail` 到达，从而无法与权限拒绝或宿主失败区分。

标为 `Planned` 的条目在上述 production source 和 export surface 中没有实现；对应 Tracking 只表示缺口已进入 Multica，不表示能力存在。

## 核心与微信生态能力

| Capability | Status | API | Permission / Preconditions | Minimum Host Version | Test Level | Evidence / Notes | Tracking |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Consumer Bridge | `Stable` | Kotlin/JS CommonJS artifact | 无 | 不适用 | Unit, Node, DeveloperTools | `buildMiniAppSdk`、`.d.ts`、smoke 与微信示例已验证。 | BOB-45 Done |
| Runtime Detection | `Stable` | `canIUse`, `getAppBaseInfo`，旧版本回退 `getSystemInfoSync` | 无 | 2.20.1 | Unit、Contract、Node、DeveloperTools、RealDevice | 2.20.1 是现代 `getAppBaseInfo` 路径的官方边界；旧路径仍可读取版本并报告 `VersionDependent`。`requireSupported` 产出 `UnsupportedCapability`。开发者工具（基础库 3.17.2）与 Android 真机均已验证 `Supported` 与 `Unsupported`；`VersionDependent` 无法在开发者工具构造（其最低可选调试基础库 2.21.4 高于该边界），由自动化测试覆盖。3.17.2 是当前主要验证版本，不是已验证的最低支持版本。 | BOB-70 Done |
| Permission | `Stable` | `getSetting`, `authorize`, `openSetting` | 请求与打开设置需用户手势；宿主在拒绝后不会再次弹窗 | 1.2.0 | Unit、Contract、Node、DeveloperTools、RealDevice | 三态生命周期且不做缓存；拒绝是独立的 SDK 错误而不是宿主失败。`openSetting` 有文档记载自基础库 1.1.0，`getSetting` 与 `authorize` 自 1.2.0，因此 1.2.0 是同时提供三者的最低基础库；capability gate 仍在运行时探测三者。当前映射麦克风与位置权限；真实宿主证据覆盖两者，且位置运行产出了 `NotRequested`、`Granted` 与 `Denied`。3.17.2 是当前验证版本，不是已验证的最低支持版本。 | BOB-64 Done；BOB-66 Done |
| Privacy | `Partial` | `getPrivacySetting`, `requirePrivacyAuthorize` | 小程序需在 MP 后台隐私指引中声明收集类型；弹窗由宿主展示 | 2.32.3 | Unit、Contract、Node | 不设三态：授权要求、一次尝试的结果与错误分别建模，且与权限不共享任何状态。微信没有提供可区分拒绝与关闭弹窗的字段，因此不存在 `Cancelled` 结果；可识别的拒绝映射为 `Refused`，未知失败保持 `HostFailure`。目前仅有自动化覆盖，仍需 DeveloperTools 与 RealDevice 证据。 | BOB-60 |
| Storage | `Stable` | `getStorage`, `setStorage`, `removeStorage` | 无 | Resolved at runtime | Unit, Contract, Node, DeveloperTools | 支持字符串读取、写入、覆盖、删除、missing key 和幂等删除。 | BOB-50 Done |
| Storage Clear | `Unsupported` | `clearStorage` | 无 | Not established | 尚无 | 公共 contract 有意不清空消费者全部数据。 | 矩阵记录 |
| HTTP Request | `Stable` | `request` | 合法 HTTPS request domain | Resolved at runtime | Unit, Contract, Node, DeveloperTools | 支持 method、URL、headers、文本 body、status、timeout、failure 和取消时 abort。`RequestTask.abort` 需要基础库 1.4.0；低于该版本请求仍可完成，但取消无法中止它。 | BOB-48 Done |
| Upload | `Partial` | `uploadFile` | 位于小程序沙箱内的文件，以及消费者列入 request domain 的 endpoint；无权限 | Resolved at runtime | Unit、Contract、Node | 上传沙箱文件并报告宿主的 HTTP 状态与响应文本；非 2xx 状态是结果，与 HTTP transport 一致。返回宿主的 task，因此消费者可以中止它（最多一次，并报告是否真的触发了宿主 abort）并读取宿主报告的最后一个进度值；进度 listener 最多注册一次，并在包括 abort 在内的每条终止路径上被移除。宿主确切的 `<api>:fail timeout` 映射为超时。由于请求指名宿主文件路径，该能力属于微信专属。开发者工具与真机证据仍待补齐，真实传输属 `BackendRequired`。 | BOB-68 |
| Download | `Partial` | `downloadFile` | 消费者列入 request domain 的 endpoint，以及小程序文件沙箱；无权限 | Resolved at runtime | Unit、Contract、Node | 将 URL 取到宿主文件系统中，并报告宿主的 HTTP 状态与其文件引用，可选由调用方指定目标路径。SDK 不读取内容、不移动任何东西，也不声称该路径会超出本次会话；后续文件操作属于文件系统能力。abort、进度、超时与清理行为与上传完全一致。 | BOB-68 |
| Network Status | `Partial` | `getNetworkType`、`onNetworkStatusChange`、`offNetworkStatusChange` | 无 | Resolved at runtime | Unit、Contract、Node | 公共 capability，因为每个宿主都能回答「是否在线、通过什么链路」：查询与监听分开门控，且监听要求两半都在，以保证每次注册都能配到一次移除。每个 collector 注册自己的宿主 listener 并在结束时移除；不做轮询，不在开始收集时合成读数，读不懂的事件会结束该 collector 而不是被丢弃。宿主自己的连接类型词始终与 SDK 识别的名字并列携带。 | BOB-68 |
| WebSocket | `Planned` | WebSocket APIs | 消费者列入的 socket domain | Not established | 尚无 | 未实现：不存在 interop、adapter 或 export，且本 Issue 的可达范围不包含它。该行是状态记录，不是承诺。 | 矩阵记录 |
| Authentication Bootstrap | `Partial` | `login` | Backend 负责 code exchange | Not established | Unit, Node, DeveloperTools | `wechatLogin()` 返回短期 code，不是身份、SDK session 或 access token。 | BOB-47 Done |
| Check Session | `Stable` | `checkSession` | 无 | Resolved at runtime | Unit、Contract、Node、RealDevice | 报告微信自身客户端登录态是否仍然完好。属微信专属并带命名空间；有效不代表用户已认证、不代表后端 session 或 token。success/fail callback 分别映射为 `Valid`/`Invalid`，不解析 raw `errMsg`。微信该 API 页面未标注最低基础库，因此由 capability gate 探测。Android 真机已验证 `Invalid → wx.login → Valid`。 | BOB-58 |
| Standard Payment | `Partial` | `requestPayment` | 合法商户、可信 Backend 下单与签名、真机；未建立权限前置 | Resolved at runtime | Unit, Contract, Node | 只把可信后端产出的五个参数转发给微信自身的支付界面，并且只报告宿主说「交互已完成」。SDK 不计算签名、不持有商户密钥、没有订单模型；不记录、不缓存、不持久化，且调用成功绝不是订单事实。确切消息 `requestPayment:cancel`（即所带基础库自身支付流程产出的形式）映射为 `HostInteractionInterrupted`，且不声称用户取消；`requestPayment:fail cancel` 及其他所有消息因缺乏证据仍是 `HostFailure`。真实主动关闭是否也以 `requestPayment:fail cancel` 到达，属于必须由合法商户环境建立的待办，即 `BackendRequired`。 | BOB-59 |
| Virtual Payment | `Planned` | `requestVirtualPayment` | 潜在资格条件可能涉及账号主体、类目、平台或灰度，另需可信 Backend；实际条件均未确立，也不声称存在本 SDK 可索取的权限 | Not established | 尚无（若实现：Unit、Contract、Node、RealDevice、BackendRequired） | **仅有边界：没有实现、没有注册进 capability catalog、也没有导出。** 虚拟支付与标准支付保持独立 —— 独立 key（候选 `wechat.request-virtual-payment`，实现时最终确定）、独立的请求与结果模型、独立的资格答案、独立的后端流程。它绝不作为标准支付失败后的降级路径；不与 `WeChatPaymentRequest` 共享任何字段；不复用 `JsPaymentOutcome`。API 存在不能确立资格，SDK 声称的每个平台与账号范围都必须独立举证。所带开发者工具基础库中没有可发现的虚拟支付契约，这与能力保持 `Planned` 而非 `Partial` 一致；本次探测没有执行运行时能力检查。详见 [ARCHITECTURE-ch.md](../../ARCHITECTURE-ch.md) 的计划设计一节。 | BOB-74 |
| Subscription Message | `Partial` | `requestSubscribeMessage` | 由调用方提供的用户手势；同一 AppID 后台中真实存在的模板；端到端送达还需要可信后端，属于 `BackendRequired`。没有可确立的权限 | Resolved at runtime | Unit、Contract、Node、RealDevice（部分） | 请宿主把消息模板呈现给用户，并按模板逐条作答。**这是本项目证据最薄的一处：** 已安装基础库声明了选项（`tmplIds`）以及「结果以模板 ID 为键、旁边带 `errMsg`」，但没有该 API 的实现也没有 schema，因此无法核实任何状态词汇或关闭消息。SDK 只识别 `accept`（观察自模拟器弹窗预览载荷，属于渲染数据而不是 API 结果），其余状态一律原样保留。同意是订阅状态，永远不是送达。自动化检查通过；Android 真机已验证 `Supported` 与零模板不调用宿主的保护。缺少有效配置模板时，弹窗结果仍为阻塞。 | BOB-73 |
| Navigation | `Stable` | `navigateTo`, `redirectTo`, `navigateBack`、`switchTab` | 有效页面 route；`switchTab` 需要小程序自身 `tabBar` 中声明的 route | Not established | Unit, Node, DeveloperTools, RealDevice | 四项操作均具备规定的宿主证据。`switchTab` 对空白 route 在调用宿主前拒绝；开发者工具与 Android 真机覆盖了成功切换的 `SHOWN`/`HIDDEN` 语义，以及没有对应 tab 的 route 所产生的封闭 `HostFailure`。 | BOB-49 Done |
| App Lifecycle | `Partial` | consumer-forwarded launch/show/hide | 消费者转发宿主钩子 | Resolved at runtime | Unit, Contract, Node, DeveloperTools | 前台已验证；后台迁移缺 RealDevice 证据。 | BOB-49 |
| Page Lifecycle | `Partial` | consumer-forwarded show/hide/unload | 消费者转发宿主钩子 | Not applicable | Unit, Node, DeveloperTools | 微信 escape hatch。Page `onLoad` 作为公共入口是**有意 `Unsupported` 而非遗漏**：依 ADR-0006，页面在被显示时才成为用户所在的页面，而 `onShow` 上报同一条 route，因此单独的入口只会增加没有可观测效果的公共 API。 | BOB-49 |
| Platform Escape Hatch | `Stable` | `WechatPlatformApi` | 仅微信宿主 | 不适用 | Unit, Node | 微信 Auth、Navigation、Page Lifecycle 未伪装为通用能力。 | BOB-46 Done |
| Toast | `Partial` | `showToast` | 无 | Not established | Unit | 只有 typed interop；没有 production adapter 或 export。 | 矩阵记录 |

## 设备与系统能力

| Capability | Status | API | Permission / Preconditions | Minimum Host Version | Test Level | Evidence / Notes | Tracking |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Location | `Stable` | `getLocation` | Location permission（需用户手势）；宿主隐私协议已被接受；`app.json` 声明与 MP 后台接口权限已就位 | Resolved at runtime | Unit、Contract、Node、DeveloperTools、RealDevice | 按需读取一次位置，可在 `wgs84` 与 `gcj02` 之间选择。adapter 在调用宿主前查询隐私和权限前置条件，任一未满足都不会触及 `getLocation`，也不会自行弹窗。真实宿主覆盖 `Supported`、`NotRequested`、`Granted`、`Denied` 阻断与恢复后的成功读取，且不记录经纬度。`chooseLocation` 与 `openLocation` 未实现，因此本条不声称位置选择或取消分类能力。 | BOB-66 Done |
| Scanner | `Stable` | `scanCode` | 未确立：该 API 驱动微信自身界面，没有任何可确认的权限前置条件 | Resolved at runtime | Unit、Contract、Node、DeveloperTools、RealDevice | 通过微信自身界面扫码并校验结果。真机证明主动取消与系统相机权限阻止界面启动产生相同信号，因此两种精确消息映射为 `HostInteractionInterrupted`，不推断用户意图；其他失败仍为 `HostFailure`。真实宿主已覆盖 Supported、成功与不可归因的中断。 | BOB-61 Done；显式保留宿主歧义 |
| Clipboard Read | `Stable` | `getClipboardData` | 用户手势；微信开发者工具需有剪贴板访问权限 | 1.1.0 | Unit、Contract、Node、DeveloperTools、RealDevice | 原样返回剪贴板文本（含空字符串）；缺失或非字符串的答案映射为 `InvalidResponse`。SDK 不保存也不记录读取到的内容。`getClipboardData` 不属于 `requiredPrivateInfos` 允许的字段。 | BOB-65 |
| Clipboard Write | `Stable` | `setClipboardData` | 用户手势；微信开发者工具需有剪贴板访问权限 | 1.1.0 | Unit、Contract、Node、DeveloperTools、RealDevice | 与读取分别门控，因为宿主可能只提供其中一个方向。真实宿主已验证写入后读回匹配。 | BOB-65 |
| Short Vibration | `Stable` | `vibrateShort` | 用户手势；具备震动硬件的设备 | 1.2.0 | Unit、Contract、Node、RealDevice | Android 真机调用 PASS，且测试者确认感知短震动。可选 `type` 字段（2.13.0）未建模。 | BOB-65 |
| Long Vibration | `Stable` | `vibrateLong` | 用户手势；具备震动硬件的设备 | 1.2.0 | Unit、Contract、Node、RealDevice | 与短震动是独立宿主 API。Android 真机调用 PASS，且测试者确认感知长震动。 | BOB-65 |
| Media | `Stable` | `chooseMedia` | 未确立：微信自身的选择界面不需要 SDK 权限 | Resolved at runtime | Unit、Contract、Node、DeveloperTools、RealDevice | 选择图片或视频并校验必填字段与安全整数字节数。未知类别保留宿主原名；未报告的视频元数据保持缺失。开发者工具与 Android 的主动关闭信号按精确匹配映射为 `HostInteractionInterrupted`；其他失败保持 `HostFailure`。真实宿主已覆盖 Supported、图片、视频、混合选择、拍摄、关闭与媒体访问受限。已验证的 Android 宿主上，关闭与受限访问产生相同的不可归因中断。相册保存是另一项未实现能力。 | BOB-63 |
| File System Read | `Stable` | `getFileSystemManager().readFile` | 小程序文件沙箱；`encoding` 始终为 UTF-8 | 1.9.9 | Unit、Contract、Node、DeveloperTools、RealDevice | 读取文本；空文件为空字符串，二进制内容映射为 `InvalidResponse`。基础库 3.17.2 的开发者工具与 Android 真机均验证读回内容匹配。 | BOB-72 |
| File System Write | `Stable` | `getFileSystemManager().writeFile` | 小程序文件沙箱；父目录必须已存在 | 1.9.9 | Unit、Contract、Node、DeveloperTools、RealDevice | 写入 UTF-8 文本并替换原有内容；基础库 3.17.2 的两个真实宿主环境均验证成功。 | BOB-72 |
| File System Access | `Stable` | `getFileSystemManager().access` | 小程序文件沙箱 | 1.9.9 | Unit、Contract、Node、DeveloperTools、RealDevice | 仅在微信为「路径不存在」提供文档依据的失败文本上返回 `false`；其他失败一律抛出。真实宿主已验证删除前 `true`、删除后 `false`。 | BOB-72 |
| File System Remove | `Stable` | `getFileSystemManager().unlink` | 小程序文件沙箱 | 1.9.9 | Unit、Contract、Node、DeveloperTools、RealDevice | 删除不存在的文件会失败，遵循微信契约。沙箱根以 `wechat.filesystem-sandbox-path` 门控；真实宿主已验证删除闭环。 | BOB-72 |
| Bluetooth / BLE | `Experimental` | `openBluetoothAdapter`、`closeBluetoothAdapter`、`getBluetoothAdapterState`、`onBluetoothAdapterStateChange`、`startBluetoothDevicesDiscovery`、`stopBluetoothDevicesDiscovery`、`onBluetoothDeviceFound`、`createBLEConnection`、`closeBLEConnection`、`onBLEConnectionStateChange` | 未确立：SDK 不请求任何权限、也不请求隐私授权，因为无法从宿主契约确认该 capability 的前置条件 | Not established | Unit、Node，以及 adapter 与 discovery 的 RealDevice | 这是对 SDK **事件驱动资源模型**的概念验证，不是 Bluetooth API。每条流只注册一个宿主 listener，并在**所有终止路径**（正常结束、异常、取消）上用同一个值移除；按 `deviceId` 逐 collector 去重；使用有界缓冲、丢弃最旧事件；`open`/`close`/`start`/`stop` 保持幂等。Discovery 是尽力而为的观察而非完整结果：缓冲区丢弃的事件可能不会被宿主再次上报。已安装的开发者工具基础库在 macOS 上直接拒绝 `createBLEConnection` 与 `closeBLEConnection`（`API_NOT_SUPPORT`），因此**连接只能在真机上验收**；开发者工具只能覆盖 adapter 与 discovery。权限与隐私前置条件被有意不建模：微信把「蓝牙未开启」报告为普通宿主失败，且 BOB-60 的隐私验收仍被阻塞。`getBluetoothDevices`、service、characteristic、notify、MTU、RSSI 查询、配对、自动重连与后台扫描均未实现。adapter 与 discovery 已于 2026-09-18 在 Android 真机上执行：三个能力键均为 `Supported`，开启与关闭均成功，discovery 正常启动与停止，共上报 32 个设备，listener 计数由 2 → 1 → 0。连接与断开**未执行**，因为当时没有可连接的外设，因此该能力尚未完成验收。 | BOB-69 In Progress |
| Sensors | `Planned` | accelerometer, gyroscope, compass, beacon 等 | 依具体 API 和真机 | Not established | 尚无 | P1 不批量实现，只记录状态。 | 矩阵记录 |

## 原生界面与展现层

| Capability | Status | Component | Permission / Preconditions | Minimum Host Version | Test Level | Evidence / Notes | Tracking |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Camera UI | `P3-Presentation` | `camera` | Camera permission + Privacy | 不属于 P1 | 尚无 | 原生视图，不是 P1 Host Capability。 | P3 |
| Map UI | `P3-Presentation` | `map` | Location permission + Privacy | 不属于 P1 | 尚无 | Location 属于 P1，地图视图属于展现层。 | P3 |
| Video UI | `P3-Presentation` | `video`, `live-player` | 依组件而定 | 不属于 P1 | 尚无 | 不在当前 SDK renderer 范围。 | P3 |
| Canvas | `P3-Presentation` | `canvas` | 依组件而定 | 不属于 P1 | 尚无 | 不启动 Renderer、Virtual DOM 或 WXML generator。 | P3 |
| Web View / Editor / Other Native UI | `P3-Presentation` | `web-view`, `editor` 等 | 依组件而定 | 不属于 P1 | 尚无 | 留待 Presentation 阶段评估。 | P3 |

## 维护规则

1. 新增或关闭微信能力 Issue 时，在同一变更中核对本矩阵的中英文版本。
2. `Stable` 必须同时列出实现和规定层级的验证证据；仅有 class、interface、路线图或 Issue 时只能标为 `Planned`。
3. DeveloperTools 证据不能代替 RealDevice 或 BackendRequired 证据。
4. `Minimum Host Version` 只能填写有微信可引用来源的值，或在 SDK 通过询问宿主做门控时填 `Resolved at runtime`。不得依据开发机表现推断版本，也不得留下没有来源的版本号。
5. Native UI 保持 `P3-Presentation`，不得借矩阵启动 Renderer、Compose、Virtual DOM 或 WXML 工作。
