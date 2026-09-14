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

TypeScript index 页面导入规范化后的 CommonJS SDK，调用 `sdkVersion()`，将返回值输出到 console 并展示在页面上。

## 本地验证

```shell
npm install
npm run smoke
npm run typecheck
```

## 微信开发者工具

将本目录作为小程序项目导入并编译。index 页面必须显示 `0.1.0-SNAPSHOT`，console 必须包含：

```text
[kmp-miniapp-sdk] sdkVersion: 0.1.0-SNAPSHOT
```

Node 与 TypeScript 检查不能替代真实宿主验证。

`miniprogram/libs/` 下的文件是 consumer-facing distribution 副本。本阶段仍手工刷新编译器产物，构建自动化属于后续任务。
