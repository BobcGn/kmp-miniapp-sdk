# 微信小程序集成宿主示例

[English](README-en.md)

本目录是 SDK Consumer Bridge 的最小微信小程序集成宿主。

集成闭环为：

```text
Gradle SDK build
      ↓
JavaScript artifact
      ↓
miniprogram/libs/
      ↓
TypeScript require()
      ↓
WeChat Developer Tools
```

示例包含三个页面。index 页面导入规范化后的 CommonJS SDK，调用 `sdkVersion()`、验证 Storage、请求短期微信 login code、通过 SDK HTTP transport 发起一次 HTTPS 请求，并显示 App 生命周期状态、当前页面 route，以及 runtime 关于自身的报告与各纳入门控能力的支持状态。第二、第三页用于验证页面栈桥接：第二页由 `wx.navigateTo` 打开并用 `wx.redirectTo` 替换为第三页，第三页用 `wx.navigateBack` 返回。示例不会记录或渲染 login code 本身。

`app.ts` 转发微信的 App 钩子，因为微信只把生命周期投递给消费者自己注册的入口。每个页面转发自己的 Page 钩子，并传入自己的 `this.route`。

## 本地验证

```shell
npm install
npm run smoke
npm run typecheck
```

## 微信开发者工具

将本目录作为小程序项目导入并编译。index 页面必须显示 `0.1.0-SNAPSHOT`、`FOREGROUND` 生命周期状态与页面 route、基础库版本，以及 Runtime detection、Storage、Client login code、Network 四项 `PASS`。Console 必须包含：

```text
[kmp-miniapp-sdk] sdkVersion: 0.1.0-SNAPSHOT
[kmp-miniapp-sdk] runtime detection: PASS baseLibrary=…, platform=…, runtime-detection=…, storage=Supported, ungated=Unsupported
[kmp-miniapp-sdk] storage: PASS first=first, overwritten=second, missing=null
[kmp-miniapp-sdk] auth bootstrap: PASS codeReceived=true, length=<正整数>
[kmp-miniapp-sdk] network: PASS status=200, bytes=<正整数>
[kmp-miniapp-sdk] session check: PASS check #N, state=Valid
```

Network 检查会向 `https://example.com/` 发起 `GET`。微信要求该 host 已列入 request domain 白名单，或编译时关闭域名校验。验证其他 endpoint 时请修改 `networkUrl` 常量。

导航需要交互，无法只靠一次页面加载验证。依次点击第二、第三页会为 `wx.navigateTo`、`wx.redirectTo`、`wx.navigateBack` 分别打印 `[kmp-miniapp-sdk] navigation: PASS <action>`；点击顺序见检查清单。

WeChat Session Check 卡片向微信询问其自身的客户端登录态是否仍然可用。它在页面加载时执行一次，且刻意位于 login bootstrap 之前：获取 code 会刷新客户端登录态并掩盖已过期的会话，因此启动顺序是先检查、后获取。有效结果不是已认证用户或后端 session，失效结果本身也不会获取 code。

Privacy Authorization 卡片查询宿主对其自身隐私协议的要求，并且只在按钮触发时请求用户同意。查询在页面加载时执行，因为它没有副作用；请求则绝不如此。微信要求小程序先在 MP 后台隐私指引中声明收集类型才会弹窗，而读到 `NOT_REQUIRED` 并不证明用户已同意。详见 [../../docs/DEVELOPMENT-ch.md](../../docs/DEVELOPMENT-ch.md) 的隐私配置一节。

Permission lifecycle 卡片用于查询、请求权限以及打开设置；页面加载期间不会执行其中任何一步，每一步都由点击触发。权限状态每次都从宿主读取而不是记忆，拒绝会报告为 denied 而不是宿主失败。完整点击顺序见检查清单。该流程已在微信开发者工具（基础库 3.17.2）与 Android 真机上执行；所用账号已对所映射权限持有决定，因此无法产出 `NotRequested`。

Runtime detection 卡片的期望状态来自宿主，因此把调试基础库降到 2.20.1 以下即可让 `runtime-detection` 报告 `VersionDependent`，无需改动任何代码，而 `storage` 仍报告 `Supported`。未登记能力始终显示 `ungated=Unsupported`，因此当前版本与降级版本两次运行可人工覆盖全部三态。卡片同时显示基础库版本与 platform，这两项是验证记录所需的字段。

Node 与 TypeScript 检查不能替代真实宿主验证。[../../docs/TESTING-ch.md](../../docs/TESTING-ch.md) 是权威检查清单与两层测试模型的来源。

Login code 不是已认证用户或 session。Production consumer 必须将其发送到可信后端完成交换，且不得将其作为身份进行记录或持久化。

`miniprogram/libs/` 下的文件是 consumer-facing distribution 副本。在仓库根目录执行以下命令，可刷新由编译器管理的 JavaScript、TypeScript declaration、runtime 与 source-map 文件：

```shell
./gradlew buildMiniAppSdk
```

该任务会保留手工维护的 `kmp-miniapp-sdk.js` 与 `kmp-miniapp-sdk.d.ts` normalization 文件。外部 source maps 会复制供本地调试使用，并被 Git 忽略。
