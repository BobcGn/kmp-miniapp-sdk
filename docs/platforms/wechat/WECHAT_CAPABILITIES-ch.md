# 微信能力矩阵

[English](WECHAT_CAPABILITIES-en.md)

本文档是微信宿主能力状态的事实索引，不是路线图。源码和可执行 Gradle 配置优先于本文档。只有具备实现、自动测试和规定层级的宿主验证证据时，能力才能标记为 `Stable`。

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

`Minimum Host Version` 当前统一写为“待 BOB-70”。仓库尚未实现 `wx.canIUse` 或基础库版本门控，不能根据开发机表现猜测最低版本。

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

标为 `Planned` 的条目在上述 production source 和 export surface 中没有实现；对应 Tracking 只表示缺口已进入 Multica，不表示能力存在。

## 核心与微信生态能力

| Capability | Status | API | Permission / Preconditions | Minimum Host Version | Test Level | Evidence / Notes | Tracking |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Consumer Bridge | `Stable` | Kotlin/JS CommonJS artifact | 无 | 不适用 | Unit, Node, DeveloperTools | `buildMiniAppSdk`、`.d.ts`、smoke 与微信示例已验证。 | BOB-45 Done |
| Runtime Detection | `Planned` | `wx.canIUse`, runtime info | 无 | 待 BOB-70 | 尚无 | 当前只有静态 `Supported` / `Unsupported`。 | BOB-70 |
| Permission | `Planned` | `getSetting`, `authorize`, `openSetting` | 用户操作和具体 scope | 待 BOB-70 | 尚无 | 尚无 NotRequested / Granted / Denied 生命周期。 | BOB-64 |
| Privacy | `Planned` | `getPrivacySetting` | 隐私协议与用户授权 | 待 BOB-70 | 尚无 | Privacy 必须与 Permission 分离。 | BOB-60 |
| Storage | `Stable` | `getStorage`, `setStorage`, `removeStorage` | 无 | 待 BOB-70 | Unit, Contract, Node, DeveloperTools | 支持字符串读取、写入、覆盖、删除、missing key 和幂等删除。 | BOB-50 Done |
| Storage Clear | `Unsupported` | `clearStorage` | 无 | 待 BOB-70 | 尚无 | 公共 contract 有意不清空消费者全部数据。 | 矩阵记录 |
| HTTP Request | `Stable` | `request` | 合法 HTTPS request domain | 待 BOB-70 | Unit, Contract, Node, DeveloperTools | 支持 method、URL、headers、文本 body、status、timeout、failure 和取消时 abort。 | BOB-48 Done |
| Upload | `Planned` | `uploadFile` | 合法 upload domain、可读文件 | 待 BOB-70 | 尚无 | 尚无 UploadTask、进度或 abort 适配。 | BOB-68 |
| Download | `Planned` | `downloadFile` | 合法 download domain、文件沙箱 | 待 BOB-70 | 尚无 | 尚无 DownloadTask、进度或文件结果适配。 | BOB-68 |
| Network Status | `Planned` | `getNetworkType`, `on/offNetworkStatusChange` | 无 | 待 BOB-70 | 尚无 | 必须验证 on/off 成对清理。 | BOB-68 |
| WebSocket | `Planned` | WebSocket APIs | 合法 socket domain | 待 BOB-70 | 尚无 | 不属于 BOB-68 的生产实现范围。 | 矩阵记录 |
| Authentication Bootstrap | `Partial` | `login` | Backend 负责 code exchange | 待 BOB-70 | Unit, Node, DeveloperTools | `wechatLogin()` 返回短期 code，不是身份、SDK session 或 access token。 | BOB-47 Done |
| Check Session | `Planned` | `checkSession` | 仍需 Backend 验证身份 | 待 BOB-70 | 尚无 | 尚不能查询微信登录态有效性。 | BOB-58 |
| Standard Payment | `Planned` | `requestPayment` | 合法商户、Backend 下单与签名、真机 | 待 BOB-70 | 尚无 | 客户端成功不能作为最终订单事实。 | BOB-59 |
| Virtual Payment | `Planned` | `requestVirtualPayment` | 平台资格与 Backend | 待 BOB-70 | 尚无 | 必须独立于 Standard Payment。 | BOB-74 |
| Subscription Message | `Planned` | `requestSubscribeMessage` | 用户主动触发、模板与 Backend | 待 BOB-70 | 尚无 | 微信专属，不是通用 push notification。 | BOB-73 |
| Navigation | `Partial` | `navigateTo`, `redirectTo`, `navigateBack` | 有效页面 route | 待 BOB-70 | Unit, Node, DeveloperTools | 三项操作已验证；`switchTab` 尚未实现。 | BOB-49 |
| App Lifecycle | `Partial` | consumer-forwarded launch/show/hide | 消费者转发宿主钩子 | 待 BOB-70 | Unit, Contract, Node, DeveloperTools | 前台已验证；后台迁移缺 RealDevice 证据。 | BOB-49 |
| Page Lifecycle | `Partial` | consumer-forwarded show/hide/unload | 消费者转发宿主钩子 | 待 BOB-70 | Unit, Node, DeveloperTools | 微信 escape hatch；Page load 未作为公共 capability，见 ADR-0006。 | BOB-49 |
| Platform Escape Hatch | `Stable` | `WechatPlatformApi` | 仅微信宿主 | 不适用 | Unit, Node | 微信 Auth、Navigation、Page Lifecycle 未伪装为通用能力。 | BOB-46 Done |
| Toast | `Partial` | `showToast` | 无 | 待 BOB-70 | Unit | 只有 typed interop；没有 production adapter 或 export。 | 矩阵记录 |

## 设备与系统能力

| Capability | Status | API | Permission / Preconditions | Minimum Host Version | Test Level | Evidence / Notes | Tracking |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Location | `Planned` | `getLocation`;按需评估 choose/open location | Location permission + Privacy | 待 BOB-70 | 尚无 | 尚无 interop、adapter、export 或真机证据。 | BOB-66 |
| Scanner | `Planned` | `scanCode` | Camera/Privacy | 待 BOB-70 | 尚无 | 取消必须独立于权限拒绝和宿主失败。 | BOB-61 |
| Clipboard | `Planned` | clipboard get/set | 按宿主规则 | 待 BOB-70 | 尚无 | 尚未实现。 | BOB-65 |
| Haptics | `Planned` | short/long vibration | 真机硬件 | 待 BOB-70 | 尚无 | 必须补 RealDevice 证据。 | BOB-65 |
| Media | `Planned` | `chooseMedia` | Album/Camera permission + Privacy | 待 BOB-70 | 尚无 | 不包含 Camera 或 Video 原生组件。 | BOB-63 |
| File System | `Planned` | `getFileSystemManager` read/write/access/remove | 小程序文件沙箱 | 待 BOB-70 | 尚无 | P1 只要求基础文件能力。 | BOB-72 |
| Bluetooth / BLE | `Planned` | adapter/discovery/event/connect APIs | Bluetooth permission + Privacy + RealDevice | 待 BOB-70 | 尚无 | 计划用 PoC 验证 Flow、取消和 listener cleanup；尚无实现，不能标为 Experimental。 | BOB-69 |
| Sensors | `Planned` | accelerometer, gyroscope, compass, beacon 等 | 依具体 API 和真机 | 待 BOB-70 | 尚无 | P1 不批量实现，只记录状态。 | 矩阵记录 |

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
4. BOB-70 完成前，不填写推测性的最低基础库版本。
5. Native UI 保持 `P3-Presentation`，不得借矩阵启动 Renderer、Compose、Virtual DOM 或 WXML 工作。
