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
| Navigation | Unit + Node + DeveloperTools | 示例包含 index、second、third 页面 | index navigateTo second；second redirectTo third；third navigateBack | 第二页被第三页替换；返回后显示 index；Console 顺序与文档一致 | `WDT-2026-09-15-A` | 每次 navigation interop、adapter 或页面配置变更 |
| Platform Escape Hatch | Unit + Node + capability consumer 的宿主等级 | 使用 `WechatPlatformApi` | 运行相关测试，并由具体微信专属能力执行宿主验证 | 微信 API 可达且未被描述为通用 capability | 由 Auth、Page Lifecycle、Navigation 间接覆盖 | 每次 platform API surface 变更 |

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
| BOB-58 | Check Session | Unit + Node + DeveloperTools；RealDevice/OpenAPI 抽查；不得声称后端身份已验证。 |
| BOB-65 | Clipboard | Unit + Contract + DeveloperTools；真机抽查读写。 |
| BOB-65 | Haptics | Unit + RealDevice；短震动和长震动分别验证。 |
| BOB-72 | File System | Unit + Contract + DeveloperTools；真机抽查沙箱路径、编码和删除。 |
| BOB-66 | Location | Unit + Contract + RealDevice；覆盖权限、隐私、成功与拒绝。 |
| BOB-61 | Scanner | Unit + Contract + RealDevice；覆盖二维码/条码、取消和权限/隐私。 |
| BOB-63 | Media | Unit + Contract + RealDevice；覆盖选择、取消、临时文件及权限/隐私。 |
| BOB-73 | Subscription Message | Unit + Contract + RealDevice；用户主动触发；端到端发送时 BackendRequired。 |
| BOB-68 | Upload / Download | Unit + Contract + RealDevice + BackendRequired；覆盖进度、成功、失败、abort 和文件结果。 |
| BOB-68 | Network Status | Unit + Contract + RealDevice；网络切换；取消订阅后不得继续收到事件。 |
| BOB-74 | Virtual Payment Boundary | 架构与文档审查；如实现则 RealDevice + BackendRequired。 |
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

这些记录是既有事实的索引，不补造缺失字段。下一次复测必须使用完整模板，不能仅引用本表。

## 8. 回归策略

- 影响单一 capability 的变更：复跑该 capability 的 Unit、Contract、Node 和其最低宿主等级。
- 影响 export、wrapper、Kotlin/JS 或 distribution 的变更：复跑全部当前 `Stable` 和 `Partial` 的 DeveloperTools 清单。
- 影响 permission、privacy、lifecycle、listener 或硬件的变更：至少复跑相关 RealDevice 项。
- 影响 payment、login exchange、upload/download 测试服务的变更：复跑 BackendRequired 链路。
- 每个候选发布：执行完整矩阵；未执行项必须记录 `NOT RUN` 和原因，不能默认为 PASS。
