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

示例包含三个页面。index 页面导入规范化后的 CommonJS SDK，调用 `sdkVersion()`、验证 Storage、请求短期微信 login code、通过 SDK HTTP transport 发起一次 HTTPS 请求，并显示 App 生命周期状态与当前页面 route。第二、第三页用于验证页面栈桥接：第二页由 `wx.navigateTo` 打开并用 `wx.redirectTo` 替换为第三页，第三页用 `wx.navigateBack` 返回。示例不会记录或渲染 login code 本身。

`app.ts` 转发微信的 App 钩子，因为微信只把生命周期投递给消费者自己注册的入口。每个页面转发自己的 Page 钩子，并传入自己的 `this.route`。

## 本地验证

```shell
npm install
npm run smoke
npm run typecheck
```

## 微信开发者工具

将本目录作为小程序项目导入并编译。index 页面必须显示 `0.1.0-SNAPSHOT`、`FOREGROUND` 生命周期状态与页面 route，以及 Storage、Client login code、Network 三项 `PASS`。Console 必须包含：

```text
[kmp-miniapp-sdk] sdkVersion: 0.1.0-SNAPSHOT
[kmp-miniapp-sdk] storage: PASS first=first, overwritten=second, missing=null
[kmp-miniapp-sdk] auth bootstrap: PASS codeReceived=true, length=<正整数>
[kmp-miniapp-sdk] network: PASS status=200, bytes=<正整数>
```

Network 检查会向 `https://example.com/` 发起 `GET`。微信要求该 host 已列入 request domain 白名单，或编译时关闭域名校验。验证其他 endpoint 时请修改 `networkUrl` 常量。

导航需要交互，无法只靠一次页面加载验证。依次点击第二、第三页会为 `wx.navigateTo`、`wx.redirectTo`、`wx.navigateBack` 分别打印 `[kmp-miniapp-sdk] navigation: PASS <action>`；点击顺序见检查清单。

Node 与 TypeScript 检查不能替代真实宿主验证。[../../docs/TESTING-ch.md](../../docs/TESTING-ch.md) 是权威检查清单与两层测试模型的来源。

Login code 不是已认证用户或 session。Production consumer 必须将其发送到可信后端完成交换，且不得将其作为身份进行记录或持久化。

`miniprogram/libs/` 下的文件是 consumer-facing distribution 副本。在仓库根目录执行以下命令，可刷新由编译器管理的 JavaScript、TypeScript declaration、runtime 与 source-map 文件：

```shell
./gradlew buildMiniAppSdk
```

该任务会保留手工维护的 `kmp-miniapp-sdk.js` 与 `kmp-miniapp-sdk.d.ts` normalization 文件。外部 source maps 会复制供本地调试使用，并被 Git 忽略。
