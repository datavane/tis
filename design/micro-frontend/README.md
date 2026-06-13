# TIS 微前端插件化架构方案

## 概述

本方案旨在解决TIS项目中`FormFieldType.MULTI_SELECTABLE`类型字段需要定制前端组件的OCP原则违反问题，通过微前端技术实现插件前端组件的动态加载。

## 问题背景

有一个问题一直困扰着我，例如：/Users/mozhenghua/j2ee_solution/project/plugins/tis-ontology-plugin/src/main/java/com/qlangtech/tis/plugin/ontology/impl/infer/InferOntologyFromLLMStep2Execute.java:L39                                
这里定义的inferInstances属性，相应的@FormField annotation中type                                                                                                                                                                     
为FormFieldType.MULTI_SELECTABLE，这意味inferInstances对于InferOntologyFromLLMStep2Execute这个实体是一对多的关系，在InferenceParse中又有多个属性，这样的属性和/Users                                                                  
/mozhenghua/j2ee_solution/project/tis-solr/tis-plugin/src/main/java/com/qlangtech/tis/plugin/annotation/FormFieldType.java                                                                                                          
中的其他枚举是不同的（INPUTTEXT，SELECTABLE，FILE等），他们只需要通过前端/Users/mozhenghua/j2ee_solution/project/tis-console/src/common/plugin/item-prop-val.component.ts 这一个<item-prop-val/>组件就能完成渲染。

    然而，inferInstances 这个属性，需要在/Users/mozhenghua/j2ee_solution/project/plugins/tis-ontology-plugin/src/main/resources/com/qlangtech/tis/plugin/ontology/impl/infer/InferOntologyFromLLMStep2Execute.json                      
    文件中，配置"elementCreator":                                                                                                                                                                                                       

"com.qlangtech.tis.plugin.ontology.impl.infer.InferenceParseCreatorFactory"，通过InferenceParseCreatorFactory类内部的逻辑来实现InferenceParse类在前端组件中的序列化与反序列逻辑。前端/Users/mozhenghua/j2ee_solution/project/tis-con  
sole/src/common/tis.plugin.ts:L1214中将获取到的序列化json内容封装到OntologyResInferenceResultTuplesProperty（/Users/mozhenghua/j2ee_solution/project/tis-console/src/base/common/ontology.common.ts:L186）对象中


    在前端页面中，为了渲染InferenceParse 需要专门为其定制编写 /Users/mozhenghua/j2ee_solution/project/tis-console/src/common/multi-selected/ontology.res.inference.component.ts 组件，在item-prop-val.component.ts:L330                 
    中将后端InferenceParse的渲染路由到 <ontology-res-inference-result/>组件上。                                                                                                                                                         
                                                                                                                                                                                                                                        
    这样日积月累，随着后端FormFieldType.MULTI_SELECTABLE 类型的属性越来越多，在前端中需要定制大量 <ontology-res-inference-result/> 这样的组件。/Users/mozhenghua/j2ee_solution/project/tis-console/src/common/multi-selected            
    这个目录下就都是为后端FormFieldType.MULTI_SELECTABLE 类型定制的前端组件脚本。                                                                                                                                                       
                                                                                                                                                                                                                                        
    本来我的设想是，开发TIS的功能组件，只需要在后端开发实现插件即可（目前定义非FormFieldType.MULTI_SELECTABLE确实已经不需要开发新增的前端脚本，前端可自动完成功能渲染），但是一旦有非FormFieldType.MULTI_SELECTABLE类型的属性后，就需要 

在前端中进行定制开发，这在软件开发中其实**违反了OCP原则**，当需求变化时，你应该通过添加新代码来改变系统行为，而不是修改已有的、经过测试的代码。所以我希望有一种机制，能够将/Users/mozhenghua/j2ee_solution/project/tis-console/src/c  
ommon/multi-selected/ontology.res.inference.component.ts 和与之相应的适配的脚本，都需要移植到后端插件工程中/Users/mozhenghua/j2ee_solution/project/plugins/tis-ontology-plugin，与InferenceParse.java                               
存放在同一个插件包中，ontology.res.inference.component.ts 可以独立编译，编译后的前端资源脚本存放在插件包中，当前端运行时需要运行InferenceParse的前端渲染逻辑时，从tis-ontology-plugin插件中将前端资源通过某种方式加载渲染并运行前端
逻辑。目前我使用的是angular框架来进行前端开发，我知道angular非常强大，肯定有相应的机制是实现这种“微前端式”的运行方式。

    请帮我评估一下需求，该如何实现比较好。                                                                                                                                                                                              


### 当前架构的痛点

1. **违反OCP原则**：每增加一个MULTI_SELECTABLE类型字段，需要修改核心前端代码
2. **组件耦合**：定制组件硬编码在`tis-console/src/common/multi-selected/`目录
3. **路由硬编码**：`item-prop-val.component.ts`中使用`*ngSwitchCase`硬编码路由规则
4. **无法动态扩展**：插件无法携带自己的前端组件

### 现状数据流

```
后端插件 (InferOntologyFromLLMStep2Execute.java)
  ↓ @FormField(type = MULTI_SELECTABLE)
JSON配置 (InferOntologyFromLLMStep2Execute.json)
  ↓ elementCreator + viewtype
前端序列化 (tis.plugin.ts)
  ↓ 创建TuplesProperty
组件路由 (item-prop-val.component.ts)
  ↓ *ngSwitchCase='ontologyResInference'
定制组件 (ontology.res.inference.component.ts)
  ↓ 渲染
```

## 解决方案：Angular Elements + 动态加载

### 核心思想

1. **插件前端组件化**：将定制组件打包为Web Components
2. **插件自包含**：前端组件随后端插件一起打包发布
3. **运行时动态加载**：主应用运行时加载插件提供的前端脚本
4. **标准化接口**：定义组件输入输出规范，解耦主应用与插件组件

### 技术选型

- **Angular Elements**：将Angular组件转为Web Components
- **Custom Elements API**：W3C标准，浏览器原生支持
- **动态Script加载**：运行时注入插件脚本
- **Maven前端插件**：集成前端构建到Maven生命周期

### 架构图

```
┌─────────────────────────────────────────────────────────────┐
│  TIS Console (主应用)                                        │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ item-prop-val.component.ts                          │   │
│  │   ↓                                                  │   │
│  │ DynamicWebComponentLoader                           │   │
│  │   1. 读取webComponent配置                           │   │
│  │   2. 加载插件脚本                                   │   │
│  │   3. 创建Custom Element                             │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            ↓ HTTP请求
┌─────────────────────────────────────────────────────────────┐
│  Plugin Asset Servlet                                        │
│  /plugin-assets/{pluginName}/**                             │
└─────────────────────────────────────────────────────────────┘
                            ↓ ClassLoader加载
┌─────────────────────────────────────────────────────────────┐
│  tis-ontology-plugin.tpi (插件包)                           │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ META-INF/webapp/plugin-assets/                       │  │
│  │   └── ontology-res-inference.bundle.js               │  │
│  │       (Web Component)                                │  │
│  └──────────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ InferOntologyFromLLMStep2Execute.json                │  │
│  │   "webComponent": {                                  │  │
│  │     "tagName": "ontology-res-inference-result",      │  │
│  │     "scriptUrl": "/plugin-assets/..."                │  │
│  │   }                                                  │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

## 文档结构

- [architecture.md](./architecture.md) - 详细架构设计
- [implementation-guide.md](./implementation-guide.md) - 分步实施指南
- [migration-plan.md](./migration-plan.md) - 现有组件迁移计划
- [examples/](./examples/) - 完整示例代码
- [poc/](./poc/) - POC原型实现

## 快速开始

### 阶段1：POC验证（1周）

选择一个简单组件（如`jdbc.type.component.ts`）进行原型验证：

```bash
# 1. 创建POC插件
cd /Users/mozhenghua/j2ee_solution/project/plugins
mkdir tis-poc-webcomponent-plugin

# 2. 按照 poc/README.md 搭建前端子项目

# 3. 构建并测试
mvn clean package
```

### 阶段2：基础设施（2周）

- 实现DynamicWebComponentLoader
- 实现PluginAssetServlet
- 建立前端构建模板

### 阶段3：迁移现有组件（4周）

逐步迁移`multi-selected/`目录下的11个组件。

## 预期收益

1. **开发效率提升**：新增插件无需修改核心代码
2. **代码质量提升**：符合OCP原则，降低耦合
3. **部署灵活性**：插件可独立发布和更新
4. **技术债降低**：清理`multi-selected/`目录的累积代码

## 风险评估

| 风险 | 等级 | 缓解措施 |
|------|------|----------|
| Web Components浏览器兼容性 | 低 | 已包含polyfill，IE11+支持 |
| 构建复杂度增加 | 中 | 提供Maven模板和文档 |
| 现有组件迁移工作量 | 中 | 分阶段迁移，新旧并存 |
| 运行时性能影响 | 低 | 脚本按需加载，有缓存 |

## 下一步

1. 评审本方案，确认技术选型
2. 实施POC（见`poc/`目录）
3. 如果POC成功，推进基础设施开发
4. 制定详细的迁移时间表

## 作者与版本

- **作者**：Claude Code & TIS Team
- **创建日期**：2026-06-11
- **版本**：v1.0