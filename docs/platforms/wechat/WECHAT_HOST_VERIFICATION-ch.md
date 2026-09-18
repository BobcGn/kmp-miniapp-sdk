# 微信真实宿主验证矩阵

[English](WECHAT_HOST_VERIFICATION-en.md)

本文档定义微信能力的真实宿主验收环境、证据格式、复测频率和关闭条件。能力实现状态见 [微信能力矩阵](WECHAT_CAPABILITIES-ch.md)。

## 1. 结论边界

| 等级 | 环境 | 能证明 | 不能证明 |
| --- | --- | --- | --- |
| `Unit` | Kotlin/JS 测试 | 单个类型、映射器或 adapter 行为 | 微信 runtime 可用 |
| `Contract` | 参考实现与微信 adapter | 公共语义在两个实现上一致 | 微信 API、权限或设备行为 |
| `Node` | CommonJS smoke、TypeScript | 分发模块和 consumer 类型可用 | 微信小程序兼容 |
| `DeveloperTools` | 微信开发者工具 | 产物能在指定基础库配置中编译、加载和运行 | 真机硬件、系统权限、后台行为或支付结果 |
| `RealDevice` | 微信客户端中的真实小程序 | 指定设备、系统、微信和基础库组合下的宿主行为 | 其他设备或版本全部兼容 |
| `BackendRequired` | 真实设备加受控后端 | 签名、code exchange、上传下载或支付等完整链路 | 未记录环境下的生产可靠性 |

**Unit、Contract 或 Node PASS 不等于 DeveloperTools PASS；DeveloperTools PASS 也不等于 RealDevice 或 BackendRequired PASS。**

## 2. 统一准备步骤

1. 记录待验证 Git commit，并确认工作区状态。
2. 在仓库根目录运行：

   ```shell
   ./gradlew clean build
   ./gradlew buildMiniAppSdk
   ```

3. 在 `examples/wechat-miniprogram` 运行：

   ```shell
   npm install
   npm run smoke
   npm run typecheck
   ```

4. 将 `examples/wechat-miniprogram` 导入微信开发者工具，记录工具版本、调试基础库版本、AppID 类型和域名校验设置。
5. 真机验证时额外记录设备型号、操作系统版本、微信版本、网络类型，以及隐私与权限初始状态。
6. BackendRequired 验证必须记录测试后端环境、非敏感订单或请求标识和最终服务端结果；凭证、登录 code、支付签名和个人数据不得进入截图或日志。

## 3. 当前能力验证矩阵

| Capability | Required Environment | Preconditions | Reproducible Steps | Expected Result | Current Evidence | Regression Frequency |
| --- | --- | --- | --- | --- | --- | --- |
| Consumer Bridge | Node + DeveloperTools | 已运行 `buildMiniAppSdk` | 运行 smoke/typecheck；导入示例并打开 index | Node 输出版本；页面与 Console 均显示 `0.1.0-SNAPSHOT` | `WDT-2026-09-14-A`，见 PROJECT_FACTS | 每次 export、Gradle 或 distribution 变更 |
| Storage | Unit + Contract + Node + DeveloperTools | 使用示例专用 key | 运行测试；打开 index 等待 Storage 卡片 | `PASS`；详情为 `first=first, overwritten=second, missing=null`；测试 key 最终删除 | `WDT-2026-09-14-A` | 每次 Storage contract、adapter 或 export 变更 |
| HTTP Request | Unit + Contract + Node + DeveloperTools | 合法 HTTPS request domain，或明确关闭本地域名校验 | 运行测试；打开 index 等待 Network 卡片 | 请求成功；页面显示 PASS；Console 含 status 和正数 bytes | `WDT-2026-09-14-C` | 每次 transport、error、timeout、abort 或 wrapper 变更 |
| Authentication Bootstrap | Unit + Node + DeveloperTools | 可调用 `wx.login` 的测试 AppID | 打开 index 等待 Login 卡片 | `codeReceived=true` 且 length 为正数；不得显示或记录 code | `WDT-2026-09-14-B` | 每次 auth interop、adapter、error 或 export 变更 |
| App Lifecycle — Foreground | Unit + Contract + Node + DeveloperTools | 消费者正确转发 App hooks | 编译并打开 index | 卡片显示 `FOREGROUND` | `WDT-2026-09-15-A` | 每次 lifecycle state 或 hook 变更 |
| App Lifecycle — Background | RealDevice | 真机打开示例；消费者转发 App hooks | 将小程序切到后台，再返回前台 | 状态按 BACKGROUND → FOREGROUND 迁移，不重复或丢失终态 | Pending | 每次 lifecycle 变更；每个候选发布至少一次 |
| Page Lifecycle | Unit + Node + DeveloperTools | 消费者正确转发 Page hooks | 依次进入第二页、第三页并返回 | route、show/hide/unload 与可见页面一致 | `WDT-2026-09-15-A` | 每次 Page hook 或 navigation 变更 |
| Navigation | Unit + Node + DeveloperTools，`switchTab` 另需 RealDevice | 示例包含 index、second、third、tabtarget 四个页面，以及含两项的 `tabBar` | index navigateTo second；second redirectTo third；third navigateBack；switchTab 切到 tabtarget tab；switchTab 请求没有对应 tab 的 route | 第二页被第三页替换；返回后显示 index；Console 顺序与文档一致；出现 tab target 页面，index 页面被隐藏而非卸载；没有 tab 的 route 保持当前 tab 并只输出封闭分类 | `WDT-2026-09-15-A` 覆盖三项页面栈操作；`WDT-2026-09-18-A` 与 `DEVICE-2026-09-18-A` 覆盖 `switchTab` | 每次 navigation interop、adapter 或页面配置变更 |
| Runtime Detection | Unit + Contract + Node + DeveloperTools + RealDevice | 已完成 `buildMiniAppSdk`；记录开发者工具调试基础库版本；具备一台真机 | 运行测试；打开 index 读取 Runtime Detection 卡片；在真机上重复一次 | 卡片显示基础库版本与 platform；`storage` 为 `Supported`；`wechat.runtime-detection` 为 `Supported`；未纳入门控的能力为 `Unsupported` | `WDT-2026-09-15-C`；`DEVICE-2026-09-15-A`；见 PROJECT_FACTS | 每次 catalog、gate 或 export 变更 |
| Permission | Unit + Contract + Node + DeveloperTools + RealDevice | 已完成 `buildMiniAppSdk`；具备一台可手动改变权限决定的宿主 | 运行测试；打开 index，使用 Permission Lifecycle 卡片：刷新 → 请求 → 在宿主自身设置中关闭 → 刷新 → 再次请求 → 打开设置并重新允许 | 允许后为 `Granted`；关闭后为 `Denied`；再次请求报告 `DENIED` 且不出现第二次弹窗；设置页返回后重新为 `Granted` | `WDT-2026-09-15-D`；`DEVICE-2026-09-15-B`；见 PROJECT_FACTS | 每次权限 interop、adapter、scope 映射或 export 变更 |
| Privacy | Unit + Contract + Node + DeveloperTools + RealDevice | 已完成 `buildMiniAppSdk`；小程序已在 MP 后台隐私指引中声明收集类型 | 运行测试；打开 index，使用 Privacy Authorization 卡片：刷新后请求；清除该账号的同意记录后再重复一次 | 卡片先显示 `REQUIRED` 与宿主返回的协议名，同意后显示 `NOT_REQUIRED`；拒绝会报告为 `REFUSED` 且要求保持存在 | Pending | 每次隐私 interop、adapter 或 export 变更 |
| Check Session | Unit + Contract + Node + DeveloperTools + RealDevice | 已完成 `buildMiniAppSdk`；具备一台可清除登录态的宿主 | 运行测试；打开 index 读取 WeChat Session Check 卡片；清除登录态后重新加载以看到 `Invalid`；取得新的 login code 后再次检查得到 `Valid` | 卡片在取得 code 前报告 `INVALID`、之后报告 `VALID`，且 Console 行一致 | Verified 2026-09-15 — Android、OnePlus PLQ110、微信 8.0.76、基础库 3.17.3 [1641] | 每次会话检查 interop、adapter 或 export 变更 |
| Clipboard | Unit + Contract + Node + DeveloperTools + RealDevice | 已完成 `buildMiniAppSdk`；具备剪贴板访问权限 | 运行测试；打开 index，使用 Clipboard and Haptics 卡片：先写入测试文本，再读回 | `clipboard write: PASS` 与 `clipboard read: PASS matched=true`；页面两项均为 `PASS` | Verified 2026-09-15 — DeveloperTools 3.17.2；Android、OnePlus PLQ110、微信 8.0.76、基础库 3.17.3 [1641] | 每次剪贴板 interop、adapter 或 export 变更 |
| Haptics | Unit + Contract + Node + RealDevice | 具备震动硬件的设备 | 运行测试；打开 index，依次点击 `Short vibration` 与 `Long vibration` | Console 出现 `haptics short: PASS` 与 `haptics long: PASS`，且测试人员实际感受到两种震动 | Verified 2026-09-15 — Android、OnePlus PLQ110、微信 8.0.76、基础库 3.17.3 [1641]；短/长震动均感知 | 每次震动 interop、adapter 或 export 变更 |
| File System | Unit + Contract + Node + DeveloperTools + RealDevice | 已完成 `buildMiniAppSdk`；小程序文件沙箱可用 | 运行测试；打开 index，按顺序使用 File System 卡片：写入、检查存在、读取、删除、再次检查 | `filesystem write: PASS`、`filesystem access: PASS exists=true`、`filesystem read: PASS matched=true`、`filesystem remove: PASS`、`filesystem access: PASS exists=false` | `WDT-2026-09-15-E`；`DEVICE-2026-09-15-C` | 每次文件系统 interop、adapter 或 export 变更 |
| Location | Unit + Contract + Node + DeveloperTools + RealDevice | 已完成 `buildMiniAppSdk`；MP 后台已开启定位接口；宿主权限决定可手工更改 | 运行测试；打开 index，检查能力与隐私，依次覆盖 `NotRequested`、显式授权、定位成功、设置中拒绝、拒绝时读取、恢复后再次读取 | `wechat.location=Supported`；权限依次报告 `NotRequested`、`Granted`、`Denied`；拒绝时为 `location: DENIED permissionState=Denied`；恢复后为 `PASS coordinatesValid=true, accuracyValid=true`；不显示也不记录任何经纬度 | `WDT-2026-09-15-F`；`DEVICE-2026-09-15-D` | 每次定位 interop、adapter、catalog 或 export 变更 |
| Scanner | Unit + Contract + Node + DeveloperTools + RealDevice | 已完成 `buildMiniAppSdk`；可用的测试二维码；可限制相机访问的设备 | 检查能力、扫描测试码、主动取消；限制相机后分别运行普通与 camera-only 扫码 | `Supported`；成功为 `PASS resultPresent=true, typeRecognized=true`；取消与相机受限均为 `INTERRUPTED cause=indeterminate`；不出现扫码内容 | 已于 2026-09-15 验证 —— Android、OnePlus PLQ110、微信 8.0.76、运行时基础库 3.17.2；宿主歧义以 `HostInteractionInterrupted` 保留 | 每次扫码 interop、adapter、catalog 或 export 变更 |
| Media | Unit + Contract + Node + DeveloperTools + RealDevice | 已完成 `buildMiniAppSdk`；设备上有测试图片与测试视频；可限制照片访问的设备 | 运行测试；打开 index，使用 Media 卡片：检查能力、选择图片、选择视频、选择任一类型、从相机拍摄、主动关闭选择界面，最后限制照片访问后再选择一次 | `wechat.choose-media=Supported`；图片选择报告 `media choose: PASS count=1, typesValid=true, metadataValid=true`；视频选择报告其时长与尺寸，若宿主未发送则报告为缺失；主动关闭与受限访问均报告 `media choose: INTERRUPTED cause=indeterminate`；页面与 Console 中都不出现媒体内容与完整临时路径 | 已于 2026-09-16 验证 —— 开发者工具与 Android OnePlus PLQ110、微信 8.0.76、基础库 3.17.2 已覆盖支持、图片、视频、混合、拍摄、主动关闭与受限访问；宿主歧义以 `HostInteractionInterrupted` 保留 | 每次媒体 interop、adapter、catalog 或 export 变更 |
| Bluetooth / BLE（Experimental） | adapter 与 discovery 用 DeveloperTools；连接必须用 RealDevice | `buildMiniAppSdk` 已完成；macOS 上的开发者工具会拒绝一切连接调用；连接另需真机与可达的外设 | 运行测试；打开 index，使用 Bluetooth 卡片：check capability、open adapter、start discovery、观察设备计数、stop discovery、close adapter。在真机上连接一个外设后再断开 | Console：`ble capability: PASS wechat.bluetooth-adapter=…, wechat.bluetooth-discovery=…, wechat.bluetooth-connection=…`；`ble adapter: PASS opened=true`；`ble discovery: STARTED`；`ble device: EVENT count=N duplicatePolicy=deviceId-dedup`；`ble discovery: STOPPED`；`ble cleanup: PASS listeners=0`；真机上另有 `ble connection: PASS connected=true device=<已脱敏>`。任何位置都不出现设备标识、MAC 地址、广播数据或宿主原文。 | 已于 2026-09-18 在 Android 真机上验证，且仅覆盖 adapter 与 discovery：三个能力键均为 `Supported`，adapter 开启与关闭均成功，discovery 正常启动与停止，共上报 32 个设备，session 的 listener 计数在 stop discovery 与 close adapter 两步之间由 2 → 1 → 0。**连接与断开未执行**：当时没有可连接的外设，因此二者仍未验证，本条记录不构成完整的 BLE 验收。 | 每次 BLE interop、adapter 或 listener 变更 |
| Platform Escape Hatch | Unit + Node + capability consumer 的宿主等级 | 使用 `WechatPlatformApi` | 运行相关测试，并由具体微信专属能力执行宿主验证 | 微信 API 可达且未被描述为通用 capability | 由 Auth、Page Lifecycle、Navigation 间接覆盖 | 每次 platform API surface 变更 |
| Subscription Message | Unit + Contract + Node + DeveloperTools + RealDevice | 已完成 `buildMiniAppSdk`；在同一 AppID 后台真实存在、仅在本地配置且从不提交的测试模板；任何送达结论都需要可信后端及其模板条件 | 运行测试；打开 index，使用 Subscription Message 卡片：检查能力、未配置模板时请求、在本地配置一个后请求并分别同意与拒绝、主动关闭弹窗、最后用两个模板重试 | `wechat.request-subscribe-message=Supported`；未配置时报告 `NOT CONFIGURED` 且不弹窗；合法响应对每个请求模板精确给出一个非空白文本状态；`accept` 被识别，**其他非空白状态原样保留**；在精确宿主消息得到证据之前，主动关闭保持 `HostFailure` 且只报告封闭的 signal 标签；页面与 Console 中都不出现模板 ID 或原始失败 | `DEVICE-2026-09-16-A` 已部分验证：能力支持与零模板不调用宿主的保护通过。当前 AppID 获得有效模板前，弹窗结果为**阻塞**而非失败。接受订阅只表示订阅状态；送达属于 `BackendRequired`，此处不声称 | 每次订阅消息 interop、adapter、catalog 或 export 变更 |
| Network Status | Unit + Contract + Node + DeveloperTools + RealDevice | 已完成 `buildMiniAppSdk`；一台能够真正切换连接的真机 | 运行测试；打开 index，检查能力、读取当前网络，然后启动观察、切换连接（Wi-Fi 与蜂窝互切，或断开再恢复），再停止观察 | `network-status-query=Supported` 且 `network-status-listener=Supported`；查询报告连接类型与 `connected=true`；停止时报告 `network observation: PASS events=<大于 0>, last=<切换到的类型>`；只有 `on` 的宿主报告监听不支持 | 待补 —— 模拟器没有可切换的连接，因此需要真机 | 每次网络状态 interop、adapter、catalog 或 export 变更 |
| Upload | Unit + Contract + Node + DeveloperTools + RealDevice + BackendRequired | 已完成 `buildMiniAppSdk`；一个列入 request domain 的受控 HTTPS endpoint，以及沙箱中的文件；未提供时示例报告 `NOT CONFIGURED` | 运行测试；打开 index，使用 Network Extensions 卡片：检查能力、执行上传检查，然后对一个较慢的 endpoint 重试并在进行中取消 | `wechat.upload-file=Supported`；`upload check: PASS status=…, bytes=…, progressSeen=<true\|false>`；取消时报告 `upload cancel: PASS abortInvoked=true` 且卡片显示 `CANCELLED`；不出现 URL、header、路径或响应正文 | 待补 —— 属 `BackendRequired`：公共 endpoint 不构成证据，`https://example.com/` 也不是上传服务 | 每次上传 interop、adapter、catalog 或 export 变更 |
| Download | Unit + Contract + Node + DeveloperTools + RealDevice + BackendRequired | 已完成 `buildMiniAppSdk`；一个列入 request domain 的受控 HTTPS endpoint | 运行测试；打开 index 使用卡片：检查能力、执行下载检查，然后对一个较慢的 endpoint 重试并在进行中取消 | `wechat.download-file=Supported`；`download check: PASS status=…, fileReported=true, progressSeen=<true\|false>`；取消时报告 `download cancel: PASS abortInvoked=true`；不出现路径或正文 | 待补 —— 与上传相同，属 `BackendRequired` | 每次下载 interop、adapter、catalog 或 export 变更 |
| Standard Payment | Unit + Contract + Node + DeveloperTools + RealDevice + BackendRequired | 已完成 `buildMiniAppSdk`；绑定本小程序 AppID 的合法微信支付商户号；负责创建订单并生成签名的可信后端；装有微信客户端的真机 | 运行测试；打开 index，使用 Standard Payment 卡片：检查能力、在未配置状态下请求、在本地粘贴来自可信后端的参数后再请求，分别完成支付、主动关闭界面、复现一次支付被拒 | `wechat.request-payment=Supported`；未配置时报告 `NOT CONFIGURED` 且完全不调用宿主；完成时报告 `payment request: PASS interactionCompleted=true`，这不是订单事实；交互结束时报告 `payment request: CANCELLED cause=indeterminate`，保留不确定性而不归因于用户；失败时报告 `payment request: FAIL reason=<封闭标签>` 且不带宿主文本；页面与 Console 中都不出现任何支付参数、签名或商户标识；最终订单结果以可信后端为准，而不是客户端 | Pending —— `BackendRequired`：自动化部分已通过，真实宿主部分需要合法商户环境 | 每次支付 interop、adapter、catalog、错误映射或 export 变更 |

`VersionDependent` 无法在开发者工具中产出：其可选的最低调试基础库为 2.21.4，高于 Runtime Detection 记录的 2.20.1 边界。该状态仅由自动化测试覆盖。这属于验证环境限制，不是未实现功能。

麦克风运行所用账号无法产出 `NotRequested`，但后续位置运行已在宿主中产出该状态，并完成 `NotRequested` → `Granted` → `Denied` → 恢复后成功读取的闭环。尚未查询过的页面会显示 `UNKNOWN`，那是示例页面的占位值，刻意不属于公共 `PermissionState`。

上传与下载检查同样有一半属于 `BackendRequired`：真实传输需要一个消费者列入 request domain 的受控 HTTPS 服务，因此任何已提交的 endpoint 都无法替代。网络切换则不同——它不需要服务，但需要一台能够真正切换连接的真机，模拟器无法提供。

订阅消息验收有一半属于 `BackendRequired`：客户端只能确立订阅状态，消息是否真正发送或送达取决于微信后台模板与可信后端。该部分单独记录，且绝不从一次成功的客户端请求推断出来。

标准支付的 `BackendRequired` 部分比其他能力更大，因为答案的绝大部分不在客户端。参数必须来自持有商户密钥并完成签名的可信后端，订单结果必须来自同一后端、微信支付的异步通知或订单查询。因此真实宿主验收需要绑定本小程序 AppID 的合法商户号、真实订单与受控后端。缺少这些条件时自动化契约依然成立，能力保持 `Partial`：客户端 callback 永不被记录为支付或订单事实，也不会从模拟器推断任何结果 —— 模拟器的支付流程不是商户。

真实主动关闭信号是其中具体的待办项。所带基础库自身支付流程把交互结束报告为 `requestPayment:cancel`，只有这条确切消息被分类为交互中断；其其他接口使用的 `requestPayment:fail cancel` 形式在合法商户环境给出真实关闭产物之前，被有意保留为 `HostFailure`。两种情况下 SDK 都不声称用户取消。

虚拟支付仍有标准支付无法回答的资格问题，正是这一点让该能力保持 `Planned` 而不是进入实现：一个实现可能通过全部自动化检查，却对某个具体账号不可用。未来运行需要：按届时官方规则确认具备资格的账号与类目、真实但可处置的虚拟商品与订单、每个所声称平台的独立通过记录、交互结束实际产生的确切信号，以及服务端最终交易确认。这些都没有任何离线依据：所带开发者工具基础库没有定义虚拟支付 API，因此该模拟器无法提供正向能力证据，官方页面在本环境也无法抓取。在这次运行出现之前，不记录任何平台、账号或版本结论，该能力也不在 SDK 中注册。

详细页面和 Console 断言继续以 [TESTING-ch.md](../../TESTING-ch.md) 为准。本表负责选择环境和管理证据，不复制完整页面操作说明。

## 4. 发布阻塞项关闭条件

| Issue | Capability | Required Evidence Before Done |
| --- | --- | --- |
| BOB-70 | Runtime Detection | Unit；DeveloperTools 验证 supported/unsupported/version-dependent；至少一项 RealDevice 版本抽查；记录基础库版本。 |
| BOB-64 | Permission | Unit + Contract；RealDevice 覆盖 NotRequested、Granted、Denied 和 openSetting 返回；不得只用模拟器。 |
| BOB-60 | Privacy | Unit + Contract；RealDevice 覆盖需要隐私授权、已授权、拒绝或取消；证明 Privacy 与 Permission 分离。 |
| BOB-59 | Standard Payment | Unit + Contract + RealDevice + BackendRequired；覆盖成功、取消、失败；服务端最终订单结果必须入证。 |
| BOB-67 | Capability Matrix | 中英文条目结构一致；每个状态与源码、测试和宿主证据交叉核对。 |
| BOB-71 | Host Verification Matrix | 本文档、证据模板和入口完成；独立执行者按当前稳定能力清单复跑并提交记录。 |

## 5. 后续能力环境要求

| Tracking | Capability | Minimum Verification |
| --- | --- | --- |
| BOB-74 | Virtual Payment Boundary | 架构与文档审查 —— 即本 Issue 的交付物，其结果是该能力保持 `Planned`。若未来实现：分平台 RealDevice 运行（Android、iOS、HarmonyOS、开发者工具），一个平台的结果不得带到另一个平台；具备合法资格的账号与类目；真实但可处置的虚拟商品与订单；交互结束实际产生的信号；以及服务端最终交易的 `BackendRequired` 证据。 |
| BOB-69 | BLE PoC | Unit + Contract + RealDevice；覆盖发现、连接、取消、停止与 listener cleanup；保持 Experimental。 |
| BOB-62 | Bundle Baseline | 自动尺寸报告 + DeveloperTools 启动观察；记录 commit 和构建模式。 |

## 6. 证据记录模板

每次验证复制以下模板。一个记录可以覆盖多项能力，但每项必须有独立结果；失败或未执行不得省略。

```markdown
# WeChat Host Verification Record

- Record ID: WDT|DEVICE|BACKEND-YYYY-MM-DD-序号
- Git commit:
- SDK version:
- Executor:
- Date and timezone:
- Environment: DeveloperTools | RealDevice | BackendRequired
- Developer Tools version:
- Debug base-library version:
- AppID class: test | development | production-like
- Domain checking: enabled | disabled
- Device model / OS version: N/A for DeveloperTools
- WeChat version: N/A for DeveloperTools
- Network type:
- Backend environment and non-sensitive correlation ID: N/A when not required
- Initial permission/privacy state:

| Capability | Steps Performed | Expected | Actual | Result | Evidence Reference |
| --- | --- | --- | --- | --- | --- |
| Example | ... | ... | ... | PASS / FAIL / NOT RUN | screenshot/log/video path |

## Redaction Check

- Login code absent from artifacts: YES / NO / N/A
- Payment signature absent from artifacts: YES / NO / N/A
- Personal data absent or redacted: YES / NO / N/A

## Deviations and Known Limitations

- ...
```

## 7. 已有证据登记

| Record ID | Date | Environment | Covered Capabilities | Evidence Source | Limitations |
| --- | --- | --- | --- | --- | --- |
| `WDT-2026-09-14-A` | 2026-09-14 | WeChat Developer Tools Stable 2.01.2510290 | Consumer Bridge, Storage | 用户提供的视觉证据；PROJECT_FACTS/DEVELOPMENT 已记录 | 不包含真机、权限或后台行为。 |
| `WDT-2026-09-14-B` | 2026-09-14 | WeChat Developer Tools | Authentication Bootstrap | 用户提供的视觉证据；页面只显示 received 和 length | 未验证 Backend code exchange；原始 code 不在证据中。 |
| `WDT-2026-09-14-C` | 2026-09-14 | WeChat Developer Tools | HTTP Request | 用户确认真实 runtime 的 Network 卡片通过 | 域名校验设置和原始截图未纳入仓库记录。 |
| `WDT-2026-09-15-A` | 2026-09-15 | WeChat Developer Tools | App foreground, Page Lifecycle, Navigation | 用户确认前台状态、route 与三种导航通过 | 不包含 App background 真机迁移；`switchTab` 未实现。 |
| `WDT-2026-09-15-B` | 2026-09-15 | WeChat Developer Tools，调试基础库 3.17.3 | Consumer Bridge、Storage、Authentication Bootstrap、HTTP Request、App foreground、Page Lifecycle、Navigation | 独立用户复跑截图：页面显示全部基础检查 PASS；Console 记录 `navigateTo`、第二页卸载、第三页显示、`redirectTo` 和 `navigateBack` PASS | 对应文档基线 commit `59c7db6`；工具完整版本沿用现有 Stable 2.01.2510290 项目记录；不包含 App background 真机迁移或 `switchTab`。 |
| `WDT-2026-09-18-A` | 2026-09-18 | 微信开发者工具，调试基础库 3.17.2 | Navigation `switchTab`、Page Lifecycle 回归 | Console 记录 tab target `SHOWN`、`switch tab: PASS`、tab target `HIDDEN`，以及非 tab route `REFUSED HostFailure`；三项页面栈操作也完成回归 | Console 证据没有直接显示 tab 高亮视觉状态；目标 route 与 lifecycle 顺序已证明切换完成。 |
| `DEVICE-2026-09-18-A` | 2026-09-18 | 真机：OnePlus PLQ110、Android 36、微信 8.0.76 | Navigation、`switchTab`、Page Lifecycle 回归 | Console 记录三项页面栈操作全部 PASS、tab target `SHOWN`、`switch tab: PASS wx.switchTab /pages/tabtarget/index`、tab target `HIDDEN`，以及非 tab route `REFUSED HostFailure` | Runtime 报告基础库 3.17.2，调试面板显示 3.17.3 `[1641]`；两条无关的 `[wxapplib]` 隐私/广告错误不是 SDK 导航失败。 |
| `WDT-2026-09-15-C` | 2026-09-15 | WeChat Developer Tools，调试基础库 3.17.2 | Runtime Detection、Storage、Authentication Bootstrap、HTTP Request、Navigation | Console 记录 `runtime detection: PASS baseLibrary=3.17.2, platform=devtools, runtime-detection=Supported, storage=Supported, ungated=Unsupported`，以及 Storage、Authentication Bootstrap、HTTP Request 三项 PASS | `VersionDependent` 在此无法构造：开发者工具最低可选调试基础库为 2.21.4，高于 2.20.1 边界。 |
| `WDT-2026-09-15-D` | 2026-09-15 | WeChat Developer Tools Stable 2.01.2510290，调试基础库 3.17.2 | Permission | Console 记录 `permission query: PASS permission=microphone, state=Denied`、`permission settings: PASS permission=microphone, state=Granted`、`permission query: PASS permission=microphone, state=Granted`；卡片显示 `Granted` 与 `Denied` | 该账号无法复现 `NotRequested`。卡片初始为示例的 `UNKNOWN` 占位值，页面加载期间不出现任何弹窗。 |
| `DEVICE-2026-09-15-B` | 2026-09-15 | RealDevice：OnePlus PLQ110、Android 36、微信 8.0.76 | Permission、Runtime Detection、Storage、Authentication Bootstrap、HTTP Request | Console 记录 `permission query: PASS … state=Granted`、`permission request: PASS … state=Granted`、`permission settings: PASS … state=Denied`、`permission request: DENIED permission=microphone, state=Denied`、`permission settings: PASS … state=Granted`、`permission query: PASS … state=Granted`，以及 Runtime Detection、Storage、Authentication Bootstrap 与 HTTP Request 的结果。完整迁移：`Granted` → 设置 → `Denied` → 请求 → `DENIED` → 设置 → `Granted` → 查询 → `Granted`，拒绝后未出现第二次弹窗 | SDK 运行时 API 报告基础库 3.17.2，而开发者工具调试面板显示设备基础库 3.17.3 `[1641]`；记录为观测差异，不判定为 SDK 失败。该账号无法复现 `NotRequested`。Console 另有来自微信运行时的 `[wxapplib]` 广告优化错误。 |
| `DEVICE-2026-09-15-A` | 2026-09-15 | RealDevice：OnePlus PLQ110、Android 36、微信 8.0.76 | Runtime Detection、Storage、Authentication Bootstrap、HTTP Request、Navigation | Console 记录 `runtime detection: PASS baseLibrary=3.17.2, platform=android, runtime-detection=Supported, storage=Supported, ungated=Unsupported`，以及 Storage、Authentication Bootstrap、HTTP Request 与三项导航操作 PASS | SDK 运行时 API 报告基础库 3.17.2，而开发者工具调试面板显示设备基础库 3.17.3 `[1641]`。记录为真机自身基础库与调试运行时版本之间的观测差异，不判定为 SDK 失败。Console 另有来自微信运行时的 `[wxapplib]` 广告优化错误，与本 SDK 无关。 |
| `WDT-2026-09-15-E` | 2026-09-15 | WeChat Developer Tools，调试基础库 3.17.2 | File System Read、Write、Access、Remove | Console 记录写入 PASS、读回 `matched=true`、删除前 `exists=true`、删除 PASS、删除后 `exists=false` | 用户执行顺序为写入、读取、检查、删除、再检查，仍覆盖完整状态闭环；合法域名检查关闭的提示与文件系统无关。 |
| `WDT-2026-09-15-F` | 2026-09-15 | WeChat Developer Tools Stable 2.01.2510290，调试基础库 3.17.2 | Location capability、permission、privacy precondition | Console 记录 `wechat.location=Supported`、`state=NotRequested`、显式请求后的 `state=Granted` 与定位结果形状 PASS；页面仅在点击后请求，不显示或记录坐标 | 当前 AppID 报告隐私要求 `NOT_REQUIRED`，因此本次不声称验证了隐私弹窗；该条件仍由 BOB-60 单独跟踪。 |
| `DEVICE-2026-09-15-D` | 2026-09-15 | RealDevice：Android，基础库运行时报告 3.17.2 | Location permission refusal、guard 与恢复 | Console 记录 `state=Denied`、`location: DENIED permissionState=Denied`，随后恢复权限并再次记录 `location: PASS coordinatesValid=true, accuracyValid=true` | 拒绝时 adapter 在调用 `getLocation` 前阻断；成功日志不含经纬度。当前完成范围仅为 `getLocation`，不包含 `chooseLocation`、`openLocation` 或位置选择取消分类。 |
| `DEVICE-2026-09-15-C` | 2026-09-15 | RealDevice：Android，运行时基础库 3.17.2 | File System Read、Write、Access、Remove | Console 记录 `filesystem write: PASS`、`filesystem read: PASS matched=true`、`filesystem access: PASS exists=true`、`filesystem remove: PASS`、`filesystem access: PASS exists=false` | 设备具体型号和微信版本未在本次文本证据中重复提供；Console 的 `[wxapplib]` privacy/ad errors 来自微信运行环境，与文件系统链路无关。 |
| `DEVICE-2026-09-16-A` | 2026-09-16 | RealDevice：OnePlus PLQ110、Android 36、微信 8.0.76、运行时基础库 3.17.2 | Subscription capability 与未配置请求保护 | 截图与 Console 记录 `subscription capability: PASS wechat.request-subscribe-message=Supported` 和 `subscription request: NOT CONFIGURED templateCount=0`；未暴露模板 ID | 此路径有意不调用 `wx.requestSubscribeMessage`，因此不会出现订阅同意弹窗。同意、拒绝、关闭、多模板关联与送达因当前 AppID 没有已配置的有效测试模板而记为 NOT RUN。可见的 `[wxapplib]` background-fetch 与广告优化错误是与该能力无关的运行时噪声。 |

这些记录是既有事实的索引，不补造缺失字段。下一次复测必须使用完整模板，不能仅引用本表。

## 8. 回归策略

- 影响单一 capability 的变更：复跑该 capability 的 Unit、Contract、Node 和其最低宿主等级。
- 影响 export、wrapper、Kotlin/JS 或 distribution 的变更：复跑全部当前 `Stable` 和 `Partial` 的 DeveloperTools 清单。
- 影响 permission、privacy、lifecycle、listener 或硬件的变更：至少复跑相关 RealDevice 项。
- 影响 payment、login exchange、upload/download 测试服务的变更：复跑 BackendRequired 链路。
- 每个候选发布：执行完整矩阵；未执行项必须记录 `NOT RUN` 和原因，不能默认为 PASS。
