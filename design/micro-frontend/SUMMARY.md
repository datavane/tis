# TIS微前端插件化架构方案 - 完整实施文档

本目录包含了TIS微前端插件化架构的完整设计和实施方案。

## 📁 文档结构

```
micro-frontend/
├── README.md                          # 总览（本文件）
├── architecture.md                    # 详细架构设计
├── implementation-guide.md            # 分步实施指南
├── migration-plan.md                  # 现有组件迁移计划
├── examples/                          # 示例代码
│   ├── backend/                       # 后端示例
│   ├── frontend/                      # 前端示例
│   └── build/                         # 构建配置示例
└── poc/                               # POC原型实现
    ├── README.md                      # POC说明
    ├── tis-poc-webcomponent-plugin/   # 完整POC插件
    └── frontend-integration/          # 主应用集成代码
```

## 🎯 方案目标

解决TIS项目中`FormFieldType.MULTI_SELECTABLE`类型字段需要定制前端组件的架构问题：

### 当前问题
- ❌ 违反OCP原则：每增加一个组件需要修改核心代码
- ❌ 组件耦合：定制组件硬编码在`multi-selected/`目录
- ❌ 无法动态扩展：插件无法携带自己的前端组件

### 解决方案
- ✅ 插件前端组件化：将定制组件打包为Web Components
- ✅ 插件自包含：前端组件随后端插件一起发布
- ✅ 运行时动态加载：主应用运行时加载插件脚本
- ✅ 符合OCP原则：新增插件无需修改核心代码

## 🚀 快速开始

### 1. 了解方案

阅读顺序：
1. **README.md**（本文件）- 了解方案概述
2. **architecture.md** - 理解技术架构
3. **implementation-guide.md** - 学习实施步骤
4. **poc/README.md** - 查看POC实现

### 2. 运行POC

```bash
# 进入POC目录
cd poc/tis-poc-webcomponent-plugin

# 构建插件
mvn clean package -Dmaven.test.skip=true

# 查看输出
ls -lh target/*.tpi
ls -lh src/main/resources/META-INF/webapp/plugin-assets/

# 部署测试
cp target/tis-poc-webcomponent-plugin.tpi ~/tis-plugins/
```

### 3. 评估方案

- 检查构建产物
- 测试运行效果
- 评估性能指标
- 收集团队反馈

### 4. 推进实施

如果POC验证成功，按照`implementation-guide.md`推进：
- 阶段1: POC验证（1周）
- 阶段2: 基础设施（2周）
- 阶段3: 插件开发模板（1周）
- 阶段4: 试点迁移（2周）
- 阶段5: 全面迁移（4周）
- 阶段6: 清理与优化（1周）

## 📊 技术方案

### 核心技术栈

| 技术 | 用途 | 版本 |
|------|------|------|
| Angular Elements | 将Angular组件转为Web Components | 15+ |
| Custom Elements API | W3C标准，浏览器原生支持 | - |
| Webpack 5 | 前端构建工具 | 5.75+ |
| Maven Frontend Plugin | 集成前端构建到Maven | 1.12+ |

### 架构分层

```
┌─────────────────────────────────────┐
│  表现层: Angular Components         │
│  + Web Components (插件提供)        │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│  加载层: DynamicWebComponentLoader  │
│  + Script缓存 + 组件生命周期管理    │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│  资源层: PluginAssetServlet         │
│  + 插件资源路由 + 缓存控制          │
└─────────────────────────────────────┘
              ↓
┌─────────────────────────────────────┐
│  插件层: .tpi包                     │
│  + Java类 + 前端bundle.js           │
└─────────────────────────────────────┘
```

## 📋 迁移计划

需要迁移的组件（共11个）：

### P1 - 简单组件（2周）
- jdbc.type.component.ts
- jdbc.type.props.component.ts  
- multi.select.single.val.component.ts

### P2 - 中等复杂度（3周）
- table.join.match.condition.component.ts
- table.join.filter.condition.component.ts
- schema.edit.component.ts
- transformer.rules.component.ts

### P3 - 复杂组件（3周）
- ontology.props.list.component.ts
- ontology.prop.role.type.linker.component.ts
- ontology.res.inference.component.ts

详细计划见`migration-plan.md`。

## 💡 关键设计点

### 1. WebComponentConfig协议

定义插件前端组件的元数据：

```json
{
  "webComponent": {
    "tagName": "tis-jdbc-type-selector",
    "scriptUrl": "/plugin-assets/tis-poc-webcomponent-plugin/jdbc-type-selector.bundle.js",
    "version": "1.0.0",
    "inputs": [...],
    "outputs": [...]
  }
}
```

### 2. 动态加载机制

```typescript
// 1. 加载脚本（带缓存）
await loader.loadScript(config);

// 2. 等待Custom Element注册
await customElements.whenDefined(tagName);

// 3. 创建实例并绑定属性
const element = document.createElement(tagName);
element.tabletView = data;

// 4. 插入DOM
container.appendChild(element);
```

### 3. 插件资源服务

```
GET /plugin-assets/{pluginName}/{resourcePath}
    ↓
PluginAssetServlet
    ↓
从插件ClassLoader加载资源
    ↓
返回脚本内容（带缓存头）
```

## 📈 预期收益

### 开发效率
- 新增插件无需修改核心代码
- 插件可独立开发和测试
- 减少团队协作成本

### 代码质量
- 符合OCP原则
- 降低耦合度
- 提高可维护性

### 部署灵活性
- 插件可独立发布
- 支持热更新（未来）
- 版本管理更清晰

## ⚠️ 风险评估

| 风险 | 等级 | 缓解措施 |
|------|------|----------|
| 浏览器兼容性 | 低 | 引入polyfill |
| 构建复杂度 | 中 | 提供模板和文档 |
| 迁移工作量 | 中 | 分阶段实施 |
| 运行时性能 | 低 | 脚本缓存+预加载 |

## 📚 参考文档

### 外部资源
- [Angular Elements官方文档](https://angular.io/guide/elements)
- [Web Components标准](https://www.webcomponents.org/)
- [Custom Elements API](https://developer.mozilla.org/en-US/docs/Web/Web_Components/Using_custom_elements)
- [Webpack Module Federation](https://webpack.js.org/concepts/module-federation/)

### 内部文档
- [TIS插件开发指南](https://tis.pub/docs/develop/)
- [前端架构文档](../../tis-console/README.md)

## 🤝 团队协作

### 角色分工
- **架构师**: 方案设计和技术选型
- **前端开发**: 实现动态加载器和组件迁移
- **后端开发**: 实现PluginAssetServlet
- **测试工程师**: 功能测试和性能测试
- **项目经理**: 进度跟踪和资源协调

### 沟通渠道
- 周例会: 进度同步
- 技术评审: 关键节点决策
- 文档更新: 持续完善

## ✅ 验收标准

### 功能性
- [ ] POC成功运行
- [ ] 所有11个组件成功迁移
- [ ] 功能完整，无regression
- [ ] 浏览器兼容性测试通过

### 性能
- [ ] 脚本加载时间 < 500ms
- [ ] 组件渲染时间 < 100ms
- [ ] Bundle大小合理（< 200KB/组件）
- [ ] 无内存泄漏

### 代码质量
- [ ] TypeScript编译无错误
- [ ] 代码覆盖率 > 80%
- [ ] 通过Code Review
- [ ] 文档完整

## 📞 联系方式

如有问题或建议，请联系：

- **技术负责人**: architecture@tis.com
- **前端团队**: frontend@tis.com  
- **后端团队**: backend@tis.com
- **项目管理**: pm@tis.com

---

## 📝 更新日志

| 日期 | 版本 | 更新内容 | 作者 |
|------|------|----------|------|
| 2026-06-11 | v1.0 | 初始版本，完整方案设计 | Claude Code & TIS Team |

---

**开始实施前，请确保所有关键干系人已阅读并批准本方案。**