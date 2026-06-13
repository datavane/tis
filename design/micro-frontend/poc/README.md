# POC实现：JDBC类型选择器Web Component

本目录包含一个完整的POC实现，用于验证微前端插件化架构的可行性。

## 目录结构

```
poc/
├── README.md                           # 本文件
├── tis-poc-webcomponent-plugin/        # 后端插件工程
│   ├── pom.xml
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/qlangtech/tis/plugin/poc/
│   │   │   │       ├── PocMultiSelectPlugin.java
│   │   │   │       └── JdbcTypeItem.java
│   │   │   └── resources/
│   │   │       └── com/qlangtech/tis/plugin/poc/
│   │   │           └── PocMultiSelectPlugin.json
│   │   └── test/
│   └── webapp/                         # 前端子项目
│       ├── package.json
│       ├── tsconfig.json
│       ├── webpack.config.js
│       └── src/
│           ├── shared/                 # 共享代码
│           │   └── types.ts
│           └── jdbc-type-selector/     # 组件代码
│               ├── jdbc-type-selector.component.ts
│               ├── jdbc-type-selector.element.ts
│               └── index.ts
└── frontend-integration/               # 主应用集成代码
    ├── dynamic-web-component-loader.service.ts
    ├── dynamic-web-component-host.component.ts
    └── web-component-config.ts
```

## 快速开始

### 前置条件

- JDK 8+
- Maven 3.6+
- Node.js 18+
- npm 9+

### 构建步骤

```bash
# 1. 进入POC目录
cd /Users/mozhenghua/j2ee_solution/project/tis-solr/design/micro-frontend/poc

# 2. 构建插件（会自动构建前端）
cd tis-poc-webcomponent-plugin
mvn clean package -Dmaven.test.skip=true

# 3. 检查输出
ls -lh target/tis-poc-webcomponent-plugin.tpi
ls -lh src/main/resources/META-INF/webapp/plugin-assets/

# 4. 部署到TIS
cp target/tis-poc-webcomponent-plugin.tpi ~/tis-plugins/
```

### 前端集成

```bash
# 1. 复制集成代码到主应用
cp frontend-integration/* \
   /Users/mozhenghua/j2ee_solution/project/tis-console/src/common/dynamic-loader/

# 2. 在app.module.ts中注册
# （参考implementation-guide.md的说明）

# 3. 修改item-prop-val.component.ts
# （添加dynamic-web-component-host的使用）
```

### 测试

1. 启动TIS服务
2. 创建一个使用PocMultiSelectPlugin的配置
3. 观察浏览器Console和Network面板
4. 验证组件是否正常加载和工作

## 验证清单

### 构建验证

- [ ] Maven构建成功
- [ ] 前端webpack构建成功
- [ ] .tpi包中包含bundle.js文件
- [ ] bundle.js大小合理（< 200KB）

```bash
# 检查tpi内容
unzip -l target/tis-poc-webcomponent-plugin.tpi | grep bundle.js
```

### 部署验证

- [ ] 插件成功加载到TIS
- [ ] PluginAssetServlet正确响应资源请求
- [ ] 浏览器能访问 /plugin-assets/tis-poc-webcomponent-plugin/jdbc-type-selector.bundle.js

```bash
# 测试资源访问
curl -I http://localhost:8080/plugin-assets/tis-poc-webcomponent-plugin/jdbc-type-selector.bundle.js
```

### 运行时验证

打开浏览器DevTools：

**Console验证：**
- [ ] 看到 `[POC] Web Component registered: tis-jdbc-type-selector`
- [ ] 无JavaScript错误
- [ ] customElements.get('tis-jdbc-type-selector') 返回构造函数

```javascript
// 在Console中执行
console.log(customElements.get('tis-jdbc-type-selector'));
```

**Network验证：**
- [ ] 看到对bundle.js的请求
- [ ] 状态码为200
- [ ] Content-Type为 application/javascript
- [ ] 有缓存头（Cache-Control, ETag）

**Elements验证：**
- [ ] DOM中存在 `<tis-jdbc-type-selector>` 元素
- [ ] 元素有Shadow Root（如果使用了Shadow DOM）
- [ ] 属性正确设置

### 功能验证

- [ ] 组件正确渲染
- [ ] 初始数据显示正确
- [ ] 用户交互（点击、选择）响应正常
- [ ] 数据变更事件正确触发
- [ ] 父组件能接收到变更

### 性能验证

```javascript
// 测量加载时间
performance.getEntriesByName('jdbc-type-selector.bundle.js')[0].duration
```

- [ ] 脚本加载时间 < 500ms
- [ ] 组件渲染时间 < 100ms
- [ ] 无内存泄漏

## 常见问题

### 问题1：Maven构建失败

**错误信息：** `frontend-maven-plugin: npm install failed`

**解决方法：**
```bash
# 手动进入webapp目录构建
cd webapp
npm install
npm run build

# 检查是否有node_modules和dist输出
ls -la
```

### 问题2：脚本404

**错误信息：** `GET /plugin-assets/xxx 404`

**排查步骤：**
1. 检查tpi包内容
2. 检查PluginAssetServlet是否注册
3. 检查web.xml配置
4. 查看TIS日志

### 问题3：Custom Element未注册

**错误信息：** `Failed to construct 'HTMLElement'`

**可能原因：**
- tagName不包含连字符
- 重复注册
- Angular版本不兼容

**解决方法：**
```typescript
// 检查tagName格式
customElements.define('tis-jdbc-type-selector', element); // ✓ 正确
customElements.define('jdbcTypeSelector', element);        // ✗ 错误
```

### 问题4：样式丢失

**现象：** 组件渲染但样式不正确

**解决方法：**
```typescript
// 方法1: 内联样式
@Component({
  styles: [`
    .my-class { color: red; }
  `]
})

// 方法2: 使用Shadow DOM
@Component({
  encapsulation: ViewEncapsulation.ShadowDom
})
```

## 调试技巧

### 启用详细日志

在前端代码中：
```typescript
// webpack.config.js
mode: 'development',
devtool: 'source-map'
```

在后端代码中：
```xml
<!-- logback.xml -->
<logger name="com.qlangtech.tis.plugin.poc" level="DEBUG"/>
```

### 使用Chrome DevTools

1. **Elements面板**
   - 检查`<tis-jdbc-type-selector>`元素
   - 查看Shadow DOM内容
   - 检查属性值

2. **Console面板**
   - 查看注册日志
   - 执行调试命令
   - 查看错误堆栈

3. **Network面板**
   - 检查bundle.js请求
   - 查看响应头
   - 确认缓存状态

4. **Performance面板**
   - 记录加载过程
   - 分析性能瓶颈

### 断点调试

在组件代码中：
```typescript
export class JdbcTypeComponent implements OnInit {
  ngOnInit() {
    debugger; // 设置断点
    console.log('Component initialized', this.tabletView);
  }
}
```

## 下一步

POC验证成功后：

1. **评审结果**
   - 召开技术评审会议
   - 展示POC成果
   - 收集反馈

2. **完善方案**
   - 根据反馈优化设计
   - 补充遗漏的场景
   - 更新文档

3. **推进实施**
   - 实施基础设施（阶段2）
   - 创建开发模板（阶段3）
   - 开始迁移组件（阶段4-5）

## 参考资源

- [Angular Elements官方文档](https://angular.io/guide/elements)
- [Web Components标准](https://www.webcomponents.org/)
- [Custom Elements API](https://developer.mozilla.org/en-US/docs/Web/Web_Components/Using_custom_elements)
- [implementation-guide.md](../implementation-guide.md)
- [architecture.md](../architecture.md)

## 联系方式

如有问题，请联系：
- 架构团队：architecture@tis.com
- 前端团队：frontend@tis.com
- 后端团队：backend@tis.com
