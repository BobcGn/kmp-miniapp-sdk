# ADR 0007：能力支持状态与版本门控

[English](0007-capability-support-and-version-gating-en.md)

- 状态：Accepted
- 日期：2026-09-15

## 背景

ADR 0003 把能力支持限定为 `Supported` 与 `Unsupported`，并明确推迟了更丰富的状态：「Future capability-support states may express version or permission requirements when concrete use cases establish their required data. They are not added speculatively now.」

BOB-70 就是那个用例。在微信开发者工具里可用的能力，在更旧的基础库上可能并不存在；而在此之前 SDK 是按硬编码清单作答的，于是兼容性问题会表现为意料之外的宿主失败，而不是明确的「这里不可用」。微信提供 `wx.canIUse` 与基础库版本，因此这个问题可以由宿主自己来回答，而不是查表。

## 决策

- **`CapabilitySupport` 新增 `VersionDependent` 与 `PermissionDependent`。** `Supported` 与 `Unsupported` 含义不变。`VersionDependent(requiredVersion, currentVersion)` 表示宿主从更高的 runtime 版本起才提供该能力；`PermissionDependent(permission)` 表示该能力依赖某项权限。两者都意味着当前不可用。
- **宿主依据自身 runtime 作答，而不是依据清单。** `WechatHost` 委托给一个读取 `wx.canIUse` 与基础库版本的 gate，因此同一份 SDK 构建在两个宿主上可以给出不同答案。这正是能力模型的目的，而不是副作用。
- **版本比较以 `HostVersion` 落在 `commonMain`。** 点分数字版本的解析与排序属于平台无关逻辑，未来宿主可直接复用，而不必在自己的测试里重写。只识别数字段，其他形式一律视为不可确定，因为比较错误会静默误报支持状态。
- **只在已知之处记录版本数据。** 目录项可以不设最低版本，转而依靠 `wx.canIUse`——它回答的是当前真正运行的宿主，而不是从表格抄来的数字。微信官方文档确认 `wx.canIUse` 从 1.1.1 起支持，`wx.getAppBaseInfo` 与 `wx.getDeviceInfo` 从 2.20.1 起支持，`wx.getSystemInfoSync` 从 2.20.1 起停止维护。运行时检测能力记录的 2.20.1 指现代拆分 API 的边界；旧版回退路径仍用于读取版本并得出 `VersionDependent`。当前 Storage API 与 `wx.request` 的入口页没有标注 API 本身的引入版本，因此继续由 `wx.canIUse` 实时判定。
- **`PermissionDependent` 已建模，但微信侧尚未产出。** 微信目录表中没有任何条目声明权限，因为 NotRequested/Granted/Denied 三态模型由权限生命周期能力拥有。该状态的存在是为了让那个模型将来有处可报；在此之前它通过 FakeHost 契约检查被覆盖。
- **`requireSupported` 是产出 `UnsupportedCapability` 的唯一路径。** 不能缺少该能力的调用方使用它，从而拿到 SDK 错误，而不是自造一个失败。除 `Supported` 外的每个状态都会失败。该异常仍只携带 key；需要原因时读取支持状态。
- **runtime 标量读取被推迟并分别缓存。** 导出 facade 在模块加载期间就构造宿主，而 Node 进程没有 `wx`，因此构造期间不做任何探测；版本与平台分别在首次读取时保留。`wx.canIUse` 是实时能力查询，不使用该缓存。
- 目录表仅限微信。不引入多宿主能力注册表。

## 后果

- 共享代码可以区分「这个宿主永远不会提供」与「这个宿主在这里无法提供」，这正是放弃与降级之间的差别。
- 能力答案变成 runtime 探测，不再对某次构建恒定。此前假定静态答案的测试与文档必须重述。
- 单个 sealed 状态只能表达一个阻塞原因，因此同时受版本与权限门控的能力无法一次报出两者。该限制在此记录，而不是提前解决。
- 完全无法探测的宿主按失败关闭处理：SDK 无法确认的能力一律报告为 `Unsupported`，而不是假定可用。
- 能力矩阵可以记录已确知版本边界之处，并说明其余情况在 runtime 解析，而不必让每一行都停留在 pending。
