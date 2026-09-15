# P1 WeChat Issues 落地顺序与 Claude 交接计划

更新日期：2026-09-15

## 1. 使用说明

本文档记录 Multica 项目「KMP小程序SDK」中 BOB-58 至 BOB-74 的落地顺序。以下 17 个执行条目与 Multica Issue 一一对应：每个 Issue 恰好对应一个独立执行条目，不合并、不拆分，也不把一个 Issue 的验收结果代替另一个 Issue。文末表格仅作为这 17 个执行条目的索引。

执行每个 Issue 前，应重新读取该 Issue 在 Multica 中的最新描述。若本文档与 Multica 存在差异，以 Multica 中的 Goal、Scope、Out of Scope、Acceptance Criteria、Dependencies、Verification 和 Documentation Impact 为准。

通用关闭规则：实现、单元测试、契约测试、适用的微信开发者工具或真机验证，以及相关中英文文档同步全部完成后，才能将对应 Issue 标记为 Done。Node 或 Kotlin 测试通过不能代替微信真实宿主验证。

## 2. 落地顺序

### 第 1 步：BOB-67 `[文档][P1][发布阻塞] 建立微信能力矩阵`

先建立中英文能力矩阵骨架，登记能力状态、API、权限、最低宿主版本、测试等级和备注。本步骤先建立结构，后续每个 Issue 完成时持续更新；只有所有条目与真实代码及验证证据一致后才能关闭 BOB-67。

### 第 2 步：BOB-71 `[测试][P1][发布阻塞] 建立微信真实宿主验证矩阵`

建立 Unit、Contract、DeveloperTools、RealDevice、BackendRequired 五级验证体系，并规定前置条件、执行步骤、预期结果、设备与基础库版本、证据和验证日期。先建立模板，后续随各能力持续补证，最后统一关闭。

### 第 3 步：BOB-70 `[微信][P1][发布阻塞] 添加运行时能力检测与版本门控`

实现 `wx.canIUse`、基础库版本读取和宿主能力状态。至少能够表达支持、不支持、版本依赖和权限依赖；不支持的 API 应映射为明确错误，而不是在运行时意外失败。该 Issue 是后续所有新增微信能力的首要技术依赖。

### 第 4 步：BOB-64 `[微信][P1][发布阻塞] 建立权限生命周期抽象`

接入 `getSetting`、`authorize`、`openSetting`，建立未请求、已授权、已拒绝三态模型。权限拒绝必须与用户取消、宿主失败区分，定位、相机、相册、麦克风和蓝牙等能力不得重复建立各自的权限体系。

### 第 5 步：BOB-60 `[微信][P1][发布阻塞] 接入微信隐私授权`

接入 `getPrivacySetting`，建立与普通权限相互独立的隐私授权状态和流程。完成后执行基础 Gate：确认 Runtime Detection、Permission、Privacy 职责分离，并可被后续设备能力统一复用。

### 第 6 步：BOB-58 `[微信][P1] 添加会话有效性检查能力`

在既有 `wx.login` bootstrap 上增加强类型 `wx.checkSession`。继续保持 `login code -> Backend -> verified identity` 的安全边界，不在客户端 SDK 中建立可信用户、服务端 session 或 token refresh 系统。

### 第 7 步：BOB-65 `[微信][P1] 添加剪贴板与震动能力`

先落地剪贴板读写以及短震动、长震动，验证标准能力扩展路径。剪贴板需要契约与开发者工具验证，震动必须补充真机证据。

### 第 8 步：BOB-72 `[微信][P1] 添加基础文件系统能力`

基于 `getFileSystemManager` 实现 read、write、access、remove 的最小契约，明确沙箱路径、编码和错误语义。该能力为后续下载结果和媒体临时文件处理提供基础，但不得扩张为完整 FileSystemManager 封装。

### 第 9 步：BOB-66 `[微信][P1] 添加定位能力`

至少实现 `getLocation`，并根据真实需求决定是否加入 choose/open location。必须完整验证 Runtime Detection、Permission、Privacy、Host Adapter、Error Mapping 和 RealDevice 链路。

### 第 10 步：BOB-61 `[微信][P1] 添加扫码能力`

实现强类型 `scanCode`，验证用户取消、权限与隐私前置条件、扫码结果校验和真机差异。相机原生组件及通用相机界面不属于该 Issue。

### 第 11 步：BOB-63 `[微信][P1] 添加媒体能力`

以 `chooseMedia` 为最小范围，处理媒体类型、临时路径、基础元数据、用户取消以及相册相关权限和隐私要求。不得进入 Camera、Video、播放器或编辑器等原生展现层。

### 第 12 步：BOB-73 `[微信][P1] 添加订阅消息能力`

实现微信专属 `requestSubscribeMessage`，保持用户主动触发语义，并区分同意、拒绝、取消和宿主失败。该能力通过微信平台专属入口暴露，不能抽象为通用推送通知。

### 第 13 步：BOB-68 `[微信][P1] 补齐网络上传、下载与网络状态能力`

按 `getNetworkType`、`uploadFile`、`downloadFile`、`onNetworkStatusChange`、`offNetworkStatusChange` 的内部顺序推进。重点验证可取消 Task 与 `abort()` 的关系、on/off 监听成对清理、上传下载的真机与测试后端条件，以及下载结果与 BOB-72 文件系统边界。WebSocket 只在能力矩阵中记录状态，不在本 Issue 实现。

### 第 14 步：BOB-59 `[微信][P1][发布阻塞] 实现微信标准支付能力`

先定义强类型 PaymentRequest 和 PaymentResult，再接入 `wx.requestPayment`，最后使用合法商户和后端环境完成真机验证。支付签名只能由 Backend 生成；用户取消必须独立表达；客户端支付成功不能作为最终订单事实，最终状态必须由 Backend 校验。

### 第 15 步：BOB-74 `[微信][P1] 定义虚拟支付能力边界`

在标准支付模型稳定后，独立定义 `requestVirtualPayment` 的 capability、请求与结果语义、平台限制和后端责任。P1 不要求生产实现，但必须在能力矩阵中给出明确状态，不得与标准支付合并为充满可空字段的 DTO。

### 第 16 步：BOB-69 `[微信][P1][实验性] 验证 BLE 事件驱动能力模型`

只进行最小实验：打开适配器、开始发现、设备发现事件、停止发现、连接和断开。重点验证 Flow、取消、连接生命周期、背压、重复事件，以及 `onXXX/offXXX` 清理。完成后仍标记为 Experimental，不得宣传为完整或稳定 BLE SDK。

### 第 17 步：BOB-62 `[性能][P1] 建立 Kotlin/JS 包体积基线`

在能力实现基本稳定后，记录 SDK 自身、Kotlin 标准库、Coroutines、AtomicFU 和包装层的原始及 gzip 尺寸，提供可重复命令、首次加载观察方法和回归告警阈值。单次机器测量不能被描述为绝对性能保证。

## 3. 阶段 Gate

### Gate A：基础前置条件

BOB-70、BOB-64、BOB-60 完成后，确认运行时检测、权限和隐私是三个清晰边界，设备能力能够直接复用，错误分类没有退化为通用 HostFailure。

### Gate B：代表性能力

BOB-58、BOB-65、BOB-72、BOB-66、BOB-61、BOB-63、BOB-73 完成后，确认新增能力均遵循相同的 interop、adapter、async、error、export、testing 和 documentation 路径。

### Gate C：复杂异步与安全

BOB-68、BOB-59、BOB-74、BOB-69 完成后，确认 abort、listener cleanup、用户取消、权限拒绝、支付后端责任和事件资源生命周期模型成立。

### Gate D：收尾

BOB-62 完成后，回到 BOB-67 和 BOB-71 补齐全部状态与证据。17 个 Issue 逐项满足自身 Acceptance Criteria 后，才进入项目现有的 P1 Release Gate 审查。

## 4. 一一对应核对表

| 顺序 | Multica Issue | 中文标题 | 计划阶段 |
| --- | --- | --- | --- |
| 1 | BOB-67 | 建立微信能力矩阵 | 验收框架 |
| 2 | BOB-71 | 建立微信真实宿主验证矩阵 | 验收框架 |
| 3 | BOB-70 | 添加运行时能力检测与版本门控 | 公共前置条件 |
| 4 | BOB-64 | 建立权限生命周期抽象 | 公共前置条件 |
| 5 | BOB-60 | 接入微信隐私授权 | 公共前置条件 |
| 6 | BOB-58 | 添加会话有效性检查能力 | 基础能力 |
| 7 | BOB-65 | 添加剪贴板与震动能力 | 基础能力 |
| 8 | BOB-72 | 添加基础文件系统能力 | 基础能力 |
| 9 | BOB-66 | 添加定位能力 | 设备能力 |
| 10 | BOB-61 | 添加扫码能力 | 设备能力 |
| 11 | BOB-63 | 添加媒体能力 | 设备能力 |
| 12 | BOB-73 | 添加订阅消息能力 | 微信专属能力 |
| 13 | BOB-68 | 补齐网络上传、下载与网络状态能力 | 复杂异步 |
| 14 | BOB-59 | 实现微信标准支付能力 | 支付安全 |
| 15 | BOB-74 | 定义虚拟支付能力边界 | 支付设计 |
| 16 | BOB-69 | 验证 BLE 事件驱动能力模型 | 实验性架构验证 |
| 17 | BOB-62 | 建立 Kotlin/JS 包体积基线 | 发布收尾 |

核对结果：BOB-58 至 BOB-74 共 17 个 Multica Issue 均存在一个且仅一个对应执行条目。
