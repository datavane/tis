# Examples - 示例代码集合

本目录包含各种示例代码，帮助开发者理解和实施微前端插件化架构。

## 目录结构

```
examples/
├── README.md                          # 本文件
├── backend/                           # 后端示例
│   ├── plugin-asset-servlet/          # 资源服务Servlet完整实现
│   ├── plugin-json-config/            # 插件JSON配置示例
│   └── plugin-class-examples/         # 各种插件类示例
├── frontend/                          # 前端示例
│   ├── dynamic-loader/                # 动态加载器完整实现
│   ├── web-component-examples/        # 各种Web Component示例
│   └── integration-examples/          # 主应用集成示例
└── build/                             # 构建配置示例
    ├── maven-frontend-plugin/         # Maven前端插件配置
    ├── webpack-configs/               # 各种Webpack配置
    └── maven-archetype/               # Maven项目模板

```

## 使用说明

### Backend示例

#### 1. PluginAssetServlet
参考：`backend/plugin-asset-servlet/PluginAssetServlet.java`

完整的插件资源服务实现，包括：
- 资源路由和加载
- 缓存控制（ETag, Cache-Control）
- 安全检查（路径遍历防护）
- MIME类型处理

**使用方式：**
```java
// 复制到你的项目
cp backend/plugin-asset-servlet/PluginAssetServlet.java \
   /path/to/tis-console/src/main/java/com/qlangtech/tis/runtime/module/action/
```

#### 2. 插件JSON配置
参考：`backend/plugin-json-config/*.json`

包含各种场景的配置示例：
- 简单多选组件
- 复杂嵌套组件
- 带依赖的组件
- 多版本组件

#### 3. 插件类示例
参考：`backend/plugin-class-examples/*.java`

- `SimpleMultiSelectPlugin.java` - 最简单的多选插件
- `ComplexPlugin.java` - 复杂插件（多个MULTI_SELECTABLE字段）
- `NestedPlugin.java` - 嵌套结构插件

### Frontend示例

#### 1. 动态加载器
参考：`frontend/dynamic-loader/`

完整的加载器实现：
- `dynamic-web-component-loader.service.ts` - 核心服务
- `dynamic-web-component-host.component.ts` - 宿主组件
- `web-component-config.ts` - 配置接口

**使用方式：**
```bash
# 复制到主应用
cp -r frontend/dynamic-loader/* \
   /path/to/tis-console/src/common/dynamic-loader/
```

#### 2. Web Component示例
参考：`frontend/web-component-examples/`

- `simple-selector/` - 简单选择器组件
- `table-editor/` - 表格编辑器组件
- `tree-selector/` - 树形选择器组件
- `with-external-deps/` - 使用外部依赖的组件

#### 3. 集成示例
参考：`frontend/integration-examples/`

- 如何在item-prop-val.component.ts中集成
- 如何在app.module.ts中注册
- 如何处理旧组件兼容

### Build示例

#### 1. Maven配置
参考：`build/maven-frontend-plugin/`

各种Maven配置场景：
- 基础配置
- 多模块项目配置
- 条件构建配置
- CI/CD集成配置

#### 2. Webpack配置
参考：`build/webpack-configs/`

- `basic.config.js` - 基础配置
- `with-externals.config.js` - 外部化依赖配置
- `multi-component.config.js` - 多组件构建
- `production.config.js` - 生产环境优化配置

#### 3. Maven Archetype
参考：`build/maven-archetype/`

可复用的项目模板，用于快速创建新插件：

```bash
# 使用archetype创建新插件
mvn archetype:generate \
  -DarchetypeGroupId=com.qlangtech.tis \
  -DarchetypeArtifactId=tis-webcomponent-plugin-archetype \
  -DgroupId=com.qlangtech.tis.plugins \
  -DartifactId=my-new-plugin
```

## 快速查找

### 我想要...

**创建一个新的Web Component插件**
→ 参考 `poc/tis-poc-webcomponent-plugin/` 完整示例

**实现资源服务**
→ 参考 `backend/plugin-asset-servlet/PluginAssetServlet.java`

**实现动态加载器**
→ 参考 `frontend/dynamic-loader/`

**配置Maven构建**
→ 参考 `build/maven-frontend-plugin/pom.xml`

**配置Webpack**
→ 参考 `build/webpack-configs/basic.config.js`

**创建项目模板**
→ 参考 `build/maven-archetype/`

**迁移现有组件**
→ 参考 `../migration-plan.md` 和 `frontend/web-component-examples/`

## 示例代码说明

所有示例代码都是：
- ✅ **可运行的** - 经过验证，可以直接使用
- ✅ **有注释的** - 包含详细的说明注释
- ✅ **最佳实践** - 遵循推荐的编码规范
- ✅ **可扩展的** - 容易修改和扩展

## 注意事项

1. **路径调整**：复制代码时注意调整import路径
2. **版本兼容**：检查Angular、Webpack等版本是否匹配
3. **依赖安装**：确保安装了所需的npm依赖
4. **测试验证**：复制后务必进行测试

## 获取帮助

如果示例代码不够清楚：
1. 查看 `../implementation-guide.md` 了解详细实施步骤
2. 查看 `../architecture.md` 了解架构设计
3. 查看 `../poc/README.md` 运行完整POC
4. 联系架构团队获取支持

## 贡献示例

欢迎贡献新的示例代码！

添加新示例的步骤：
1. 在相应目录下创建子目录
2. 添加代码文件和README
3. 在本文件中更新目录和说明
4. 提交PR

---

**注意**：完整的POC实现在 `../poc/` 目录下，如果你想看一个端到端的完整示例，从那里开始。
