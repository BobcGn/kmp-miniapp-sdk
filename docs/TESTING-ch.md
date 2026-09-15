# 测试

[English](TESTING-en.md)

本文档描述两层测试体系、各层可使用的 fake，以及可复现的真实微信小程序运行检查清单。

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

7. 记录你观察到了哪些检查项、哪些没有观察到。只有在某项 capability 完成真实宿主运行后，才会在 [PROJECT_FACTS-ch.md](PROJECT_FACTS-ch.md) 中被记录为已通过宿主验证。

Storage 检查会写入专用测试 key、验证覆盖、删除该 key，并确认 missing key 读取为 `null`。Network 检查会向 `https://example.com/` 发起 `GET`，并报告 status code 与 body 长度；验证其他 host 时请修改示例中的 `networkUrl` 常量。

App 级生命周期的 `BACKGROUND` 状态无法从开发者工具模拟器触发。要观察它，需要在真机上让小程序进入后台；因此模拟器运行可以确认前台状态与页面 route，但无法确认后台迁移。

login code 是短期凭证，不会被记录或渲染。`wx.login` code 必须发送到可信的消费者后端与微信交换；取得 code 不代表用户已认证，也不能授权请求。

### 不在范围内

自动驱动微信开发者工具、完整端到端自动化，以及支付测试，均不属于本层。本仓库中没有任何自动化测试声称能够访问真实 `wx` runtime。
