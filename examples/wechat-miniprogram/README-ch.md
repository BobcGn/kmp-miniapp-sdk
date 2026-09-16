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

将本目录作为小程序项目导入并编译。剪贴板与震动卡片需要真机才能验证震动，剪贴板检查需要剪贴板访问权限。index 页面必须显示 `0.1.0-SNAPSHOT`、`FOREGROUND` 生命周期状态与页面 route、基础库版本，以及 Runtime detection、Storage、Client login code、Network 四项 `PASS`。Console 必须包含：

```text
[kmp-miniapp-sdk] sdkVersion: 0.1.0-SNAPSHOT
[kmp-miniapp-sdk] runtime detection: PASS baseLibrary=…, platform=…, runtime-detection=…, storage=Supported, ungated=Unsupported
[kmp-miniapp-sdk] storage: PASS first=first, overwritten=second, missing=null
[kmp-miniapp-sdk] auth bootstrap: PASS codeReceived=true, length=<正整数>
[kmp-miniapp-sdk] network: PASS status=200, bytes=<正整数>
[kmp-miniapp-sdk] session check: PASS check #N, state=Valid
[kmp-miniapp-sdk] clipboard write: PASS
[kmp-miniapp-sdk] clipboard read: PASS matched=true
[kmp-miniapp-sdk] haptics short: PASS
[kmp-miniapp-sdk] haptics long: PASS
[kmp-miniapp-sdk] filesystem write: PASS
[kmp-miniapp-sdk] filesystem access: PASS exists=true
[kmp-miniapp-sdk] filesystem read: PASS matched=true
[kmp-miniapp-sdk] filesystem remove: PASS
[kmp-miniapp-sdk] filesystem access: PASS exists=false
[kmp-miniapp-sdk] location capability: PASS wechat.location=Supported
[kmp-miniapp-sdk] location permission query: PASS state=Granted
[kmp-miniapp-sdk] location privacy query: PASS requirement=NOT_REQUIRED
[kmp-miniapp-sdk] location: PASS coordinatesValid=true, accuracyValid=true
[kmp-miniapp-sdk] scanner capability: PASS wechat.scan-code=Supported
[kmp-miniapp-sdk] scan: PASS resultPresent=true, typeRecognized=true
[kmp-miniapp-sdk] scan: INTERRUPTED cause=indeterminate
[kmp-miniapp-sdk] media capability: PASS wechat.choose-media=Supported
[kmp-miniapp-sdk] media choose: PASS count=1, typesValid=true, metadataValid=true
[kmp-miniapp-sdk] media choose: INTERRUPTED cause=indeterminate
[kmp-miniapp-sdk] subscription capability: PASS wechat.request-subscribe-message=Supported
[kmp-miniapp-sdk] subscription request: NOT CONFIGURED templateCount=0
[kmp-miniapp-sdk] subscription request: PASS accepted=1, otherStatuses=0, signals=accept
[kmp-miniapp-sdk] subscription request: FAIL reason=HostFailure, signal=fail-cancel
[kmp-miniapp-sdk] network capabilities: PASS query=Supported, listener=Supported, upload=Supported, download=Supported
[kmp-miniapp-sdk] network type: PASS connected=true, type=WIFI
[kmp-miniapp-sdk] network observation: PASS events=2, last=CELLULAR_4G
[kmp-miniapp-sdk] upload check: NOT CONFIGURED
[kmp-miniapp-sdk] download check: NOT CONFIGURED
```

Network Extensions 卡片覆盖五个宿主 API，逐项门控：网络类型查询、网络状态监听、上传与下载。`Check network capabilities` 报告每一项的判定；`Get current network type` 执行一次不注册任何东西的查询；`Start`/`Stop network status observation` 注册一个宿主 listener 并将其移除，停止那行会报告收到了多少次变更以及最后的连接类型。上传与下载检查在本地配置受控 HTTPS endpoint 之前什么都不做——卡片报告 `NOT CONFIGURED` 且不调用宿主——其结果只报告状态、字节数以及宿主是否报告过进度，从不报告 URL、header、文件路径或响应正文。`Cancel upload` 与 `Cancel download` 中止进行中的传输，并报告是否真的触发了宿主 abort。

观察那一半需要真机：模拟器没有可切换的连接。传输那一半属 `BackendRequired`：它需要一个列入 request domain 的受控 HTTPS 服务，因此 `https://example.com/` 不能替代。详见 [../../docs/DEVELOPMENT-ch.md](../../docs/DEVELOPMENT-ch.md) 的网络扩展一节。

Network 检查会向 `https://example.com/` 发起 `GET`。微信要求该 host 已列入 request domain 白名单，或编译时关闭域名校验。验证其他 endpoint 时请修改 `networkUrl` 常量。

导航需要交互，无法只靠一次页面加载验证。依次点击第二、第三页会为 `wx.navigateTo`、`wx.redirectTo`、`wx.navigateBack` 分别打印 `[kmp-miniapp-sdk] navigation: PASS <action>`；点击顺序见检查清单。

File System 卡片会向小程序沙箱中的固定文件名写入固定的非敏感字符串，读回、检查存在、删除，并再次检查已删除。页面加载期间不会触碰文件系统。仅支持沙箱内的 UTF-8 文本，且沙箱根本身从不显示或记录。删除不存在的文件会失败，遵循微信契约，因此删除与最后一步检查应按顺序各执行一次。

Clipboard and Haptics 卡片会写入一段固定的非敏感测试文本、读回剪贴板进行比对，并触发短震动与长震动。页面加载期间不会触碰剪贴板或震动器，每个动作都由点击触发。读取只与页面自己写入的字符串比较，页面从不显示或记录剪贴板里可能存在的其他内容。震动日志行只表示微信接受了该调用，不代表震动被感知——那只能在真机上确认。`getClipboardData` 不属于 `app.json.requiredPrivateInfos` 允许的字段，示例不声明它。

WeChat Session Check 卡片向微信询问其自身的客户端登录态是否仍然可用。它在页面加载时执行一次，且刻意位于 login bootstrap 之前：获取 code 会刷新客户端登录态并掩盖已过期的会话，因此启动顺序是先检查、后获取。有效结果不是已认证用户或后端 session，失效结果本身也不会获取 code。

Privacy Authorization 卡片查询宿主对其自身隐私协议的要求，并且只在按钮触发时请求用户同意。查询在页面加载时执行，因为它没有副作用；请求则绝不如此。微信要求小程序先在 MP 后台隐私指引中声明收集类型才会弹窗，而读到 `NOT_REQUIRED` 并不证明用户已同意。详见 [../../docs/DEVELOPMENT-ch.md](../../docs/DEVELOPMENT-ch.md) 的隐私配置一节。

Permission lifecycle 卡片用于查询、请求权限以及打开设置；页面加载期间不会执行其中任何一步，每一步都由点击触发。权限状态每次都从宿主读取而不是记忆，拒绝会报告为 denied 而不是宿主失败。完整点击顺序见检查清单。该流程已在微信开发者工具（基础库 3.17.2）与 Android 真机上执行；所用账号已对所映射权限持有决定，因此无法产出 `NotRequested`。

Runtime detection 卡片的期望状态来自宿主而不是固定表格。未登记能力始终显示 `ungated=Unsupported`，已登记能力则显示其宿主的实际判定。`VersionDependent` 无法在此构造：开发者工具可选的最低调试基础库为 2.21.4，高于该能力记录的 2.20.1 边界，因此该状态改由自动化测试覆盖。卡片同时显示基础库版本与 platform，这两项是验证记录所需的字段。

Location 卡片是唯一由能力自身强制前置条件的卡片。`Check location capability` 报告门控对 `wechat.location` 的判定；权限与隐私要求分别报告，因为 API 是否存在与用户是否放行是两个不同问题。页面加载期间不会读取位置。在隐私协议被接受前尝试读取会以 SDK 的隐私前置条件错误失败，且不触及宿主；在权限授予前尝试读取会被拒绝，而不是代替消费者弹窗——驱动该弹窗需要用户手势，因此它始终留在 `Request location permission` 按钮之后。页面只校验返回值的形状并打印 `coordinatesValid` 与 `accuracyValid`，从不显示或记录经纬度。`app.json` 的两处声明必须就位，且接口必须在 MP 后台开启，否则宿主会在用户看到任何东西之前就拒绝。开发者工具的结果由 IP 推导而非来自设备定位模块，且只支持 `gcj02`，因此可以在其中走通流程，但只有真机能证明真实定位。详见 [../../docs/DEVELOPMENT-ch.md](../../docs/DEVELOPMENT-ch.md) 的定位配置一节。

Scanner 卡片打开微信自身的扫码界面，只报告返回结果而从不显示扫码内容。`Check scanner capability` 报告门控；`Scan code` 允许相册入口，`Scan from camera only` 设置 `onlyFromCamera=true`。真机证明主动取消和系统相机权限阻止界面启动产生相同信号，因此二者都打印 `INTERRUPTED cause=indeterminate`，不声称是用户取消或权限拒绝；其他失败打印 `FAIL`。页面从不显示或记录解码内容、`rawData`、字符集或图片路径，也不为扫码建立未经证实的 SDK 权限前置条件。

Media 卡片打开微信自身的选择界面，只报告返回结果而从不显示所选内容。`Check media capability` 报告门控对 `wechat.choose-media` 的判定；四个选择按钮分别打开图片、视频、任一类型，以及仅相机。页面加载期间不会打开选择界面。Console 只打印文件数量、类别是否被识别及元数据是否可用；不会记录所选媒体、base64、文件名、完整临时路径或原始失败对象。开发者工具的精确关闭信号（`chooseMedia:cancel`）与 Android 的关闭信号（`chooseMedia:fail cancel`）都打印 `INTERRUPTED cause=indeterminate`；其他失败只打印安全的 SDK 错误名与诊断类别。媒体选择不请求任何 SDK 权限。宿主返回的路径是临时资源，如需在本次运行后继续使用，应复制到调用方拥有的存储中。

Subscription Message 卡片请微信把消息模板呈现给用户。`Check subscription capability` 报告门控对 `wechat.request-subscribe-message` 的判定；`Request subscription` **只从该按钮触发**，因为微信要求用户手势，并且只有在本地配置了测试模板时才会执行。模板 ID 属于本小程序的账号，因此仓库里不写入任何真实值：卡片中的列表在仓库中为空，页面在本地粘贴你自己的测试 ID 之前会报告 `NOT CONFIGURED` 且完全不调用宿主，页面也从不显示或记录 ID。Console 只打印计数与封闭的状态标签（`accept`、`reject`、`ban`、`filter` 或 `other`），从不打印原始答案。本 SDK 只识别 `accept`，因为可离线来源没有确立其他取值；其他非空白状态原样保留在 `hostStatus`。缺少、多出、空白或非文本答案均无效。在真机运行确立精确的关闭信号之前，失败保持为 `HostFailure`，页面只打印封闭的诊断标签，从不打印原始错误。同意订阅是订阅状态，绝不表示消息已发送或已送达。详见 [../../docs/DEVELOPMENT-ch.md](../../docs/DEVELOPMENT-ch.md) 的订阅消息一节。

Node 与 TypeScript 检查不能替代真实宿主验证。[../../docs/TESTING-ch.md](../../docs/TESTING-ch.md) 是权威检查清单与两层测试模型的来源。

Login code 不是已认证用户或 session。Production consumer 必须将其发送到可信后端完成交换，且不得将其作为身份进行记录或持久化。

`miniprogram/libs/` 下的文件是 consumer-facing distribution 副本。在仓库根目录执行以下命令，可刷新由编译器管理的 JavaScript、TypeScript declaration、runtime 与 source-map 文件：

```shell
./gradlew buildMiniAppSdk
```

该任务会保留手工维护的 `kmp-miniapp-sdk.js` 与 `kmp-miniapp-sdk.d.ts` normalization 文件。外部 source maps 会复制供本地调试使用，并被 Git 忽略。
