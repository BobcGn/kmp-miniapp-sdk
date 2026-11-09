---
id: goal-and-requirements
type: goal-and-requirements
status: draft
title: 项目目标与范围
tags: [product, scope]
---

# 项目目标与范围

[English](goal-and-requirements-en.md)

## 目标与使用者

首要价值是让 Kotlin Multiplatform 消费者把已有共享客户端逻辑接入 Mini App，同时由各宿主拥有最终渲染。主要使用者是维护这份共享 Kotlin 的客户端开发者；runtime 宿主能力是接入底座，直接从 JavaScript / TypeScript 消费 SDK 是补充方式，不是另一条并列的产品主线。

## 为什么需要这个项目

只提供 `wx` 的类型化包装，不能让已有 KMP 逻辑自动获得 Mini App compilation、runtime 依赖和宿主可加载产物。插件负责消除这部分构建接线；runtime 负责把逻辑所需的宿主能力接入公共契约；宿主只绑定状态与事件。这样共享行为不必因微信而复制，渲染也不必迁入 Kotlin。该主次由维护者确认；构建分工的既有依据是 [ADR 0010](docs/decisions/0010-miniapp-gradle-plugin-source-set-model-ch.md)。

## 当前能力

- Runtime 提供宿主无关契约、语义错误和异步适配，以及当前微信宿主实现；公共契约与平台专属 API 保持区分。
- 插件将 Mini App compilation、测试、runtime 依赖、宿主初始化和独立 distribution 接入消费者构建。模块内接入与根工程选择逻辑模块是不同配置入口，不是不同 runtime。
- 编译器生成 CommonJS 与 TypeScript 声明；消费者在宿主中调用导出入口，Kotlin 消费者使用对应 API 与源码文档。
- 仓库用隔离的自动测试、真实消费者夹具和宿主示例检查这些边界。实现存在、Node 接线通过、微信运行、真机和后端验收是不同结论。

精确实现状态及既有验收限制以 [项目事实](docs/PROJECT_FACTS-ch.md) 和 [微信能力矩阵](docs/platforms/wechat/WECHAT_CAPABILITIES-ch.md) 为索引；这里不把 `Partial`、`Experimental` 或未实现条目提升为完整能力。

## 持续成立的边界

后端拥有可信身份、订单和其他业务事实；SDK 拥有客户端行为与宿主能力；宿主拥有渲染。共享 presentation state 是已记录的责任归属，但当前没有 Presentation Core 实现，不从归属推断能力。

系统拓扑、依赖方向和具体约束由 [[architecture]] 统一说明；各模块规格说明其局部责任，不复制同一套系统规则。

## 非目标

- Renderer、Virtual DOM、布局引擎、Kotlin UI DSL 或 WXML/WXSS 的替代品。
- 在客户端签名、持有商户密钥，或以宿主回调确认身份、支付和订单事实。
- 完整镜像所有微信 API，或把微信专属语义伪装成通用宿主契约。
- 由消费者插件执行上传、部署，或管理 AppID、模板、支付与后台业务配置。
- 把两个实验性 KMP demo 当作通用项目模板或完整的平台支持承诺；它们的角色由 [[module-examples]] 拥有。

## 规格与证据

中文文件是规格树唯一节点，英文文件是同步译文；`draft` 表示仍待用户审阅，并不表示其中每项推断已获确认。规格描述当前责任与已明确决定，不是路线图或工作清单。

产品意图来自维护者确认；实现事实回到源码与可执行配置；历史取舍回到有效 ADR。缺少依据的设计动机必须就地标为「未确认」，不拿新建规格相互引用来证明推断。文档维护与验证的具体规则由 [[module-docs]] 拥有，既有运行结果仍保留原环境与时间。
