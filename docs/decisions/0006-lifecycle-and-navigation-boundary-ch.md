# ADR 0006：生命周期与导航边界

[English](0006-lifecycle-and-navigation-boundary-en.md)

- 状态：Accepted
- 日期：2026-09-14

## 背景

SDK 需要微信宿主运行时生命周期集成与导航。这带来了 ADR 0003 与 ADR 0004 未涵盖的问题：

- 哪些生命周期概念真正跨小程序宿主共享，哪些只是套了中性名字的微信语义？
- 导航是公共 capability，还是宿主专属桥接？
- ADR 0004 记录了「在真实 Host integration 提出需求前不泛化 promise 与 event adapter」。生命周期状态就是该需求。
- 微信只把生命周期投递给消费者自己注册的 `App(...)` 与 `Page(...)`，因此 SDK 无法自行观测。

## 决策

- **应用级生命周期是公共 capability。** `MiniAppLifecycle` 建模应用是否呈现在用户面前，取值为 `MiniAppLifecycleState.FOREGROUND` 或 `BACKGROUND`，由 `CapabilityKey("lifecycle")` 标识。微信与支付宝通过 `onShow`/`onHide` 上报，WebView 宿主通过可见性上报。这是每个小程序宿主都能诚实表达的唯一生命周期概念。
- **状态变化以 `Flow` 发布。** 契约暴露当前 `state` 与 `stateChanges` 流。这正是 ADR 0004 等待的真实宿主集成。它只泛化状态观测，不引入 promise adapter，也不引入通用 event adapter。
- **Page 生命周期不是公共 capability。** 页面、页面 route 与页面栈都是 DSL 小程序概念，WebView 宿主一个都没有。因此 `WechatPageLifecycle` 位于微信 runtime，仅通过逃生口可达。它跟踪 runtime 报告为「已显示」的页面；它不是导航栈，SDK 也不对 page 钩子采取任何动作。
- **导航不是公共 capability。** `navigateTo`、`redirectTo`、`navigateBack` 作用于页面栈，而只有 DSL runtime 才有页面栈。因此 `WechatNavigation` 位于微信 adapter，仅通过 `WechatPlatformApi` 可达。
- **生命周期是转发而非拦截。** 微信只向消费者拥有的注册投递生命周期，因此 SDK 提供由消费者转发钩子的入口，而不是假装能直接观测 runtime。
- **`onLoad` 不是独立入口。** 页面在被显示时才成为用户所在的页面，而 `onShow` 会上报同一个 route，因此单独的 `onLoad` 入口只会增加没有可观测效果的公共 API。`onLaunch` 仍然保留，因为它对「只转发这一个钩子」的消费者是合法的独立入口；微信总会在其后立即调用 `onShow`，因此两者上报的都是前台状态。
- **导航失败属于 host failure。** 页面栈已满、route 未知、以及在首页执行返回，微信都通过 failure callback 上报，统一映射为 `MiniAppException.HostFailure`，不会静默成功。
- 不引入 router 框架、导航栈框架、UI 组件生命周期抽象、Compose Navigation 或渲染器。

## 后果

- 共享 Kotlin 代码无需导入微信声明即可响应前后台切换。
- Consumer 能区分「SDK 建模的应用级状态」与「宿主定义的页面级状态」。
- 生命周期只有在消费者转发钩子后才生效。完全不转发的消费者只会观测到初始的后台状态，这是真实结果而非静默失败。
- 页面 route 跟踪反映 runtime 报告为已显示的页面。从更深页面返回是正确的，因为被卸载的页面会先清除自己的 route，随后被显示的页面再上报自己的 route。
- 未来 WebView 宿主可以用可见性事件实现 `MiniAppLifecycle`，且没有义务去发明页面或页面栈。
