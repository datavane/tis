# 微前端插件化架构详细设计

## 1. 总体架构

### 1.1 分层架构

```
┌─────────────────────────────────────────────────────────────────┐
│  表现层 (Presentation Layer)                                     │
│  ┌─────────────────┐  ┌──────────────────┐  ┌────────────────┐ │
│  │ 主应用组件       │  │ 插件Web Component │  │ 插件Web Component│ │
│  │ item-prop-val   │  │ ontology-res-*   │  │ jdbc-type-*     │ │
│  └─────────────────┘  └──────────────────┘  └────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│  加载层 (Loading Layer)                                          │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ DynamicWebComponentLoader                                │  │
│  │  - Script缓存管理                                         │  │
│  │  - Custom Element注册等待                                │  │
│  │  - 组件生命周期管理                                       │  │
│  │  - 属性和事件绑定                                         │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│  资源层 (Resource Layer)                                         │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ PluginAssetServlet                                       │  │
│  │  - 插件资源路由 (/plugin-assets/{plugin}/**)             │  │
│  │  - ClassLoader资源加载                                   │  │
│  │  - 缓存控制 (ETag, Last-Modified)                        │  │
│  │  - MIME类型处理                                          │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│  插件层 (Plugin Layer)                                           │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ Plugin Package (.tpi)                                    │  │
│  │  META-INF/                                               │  │
│  │    └── webapp/plugin-assets/                             │  │
│  │        └── *.bundle.js                                   │  │
│  │  com/qlangtech/tis/plugin/                               │  │
│  │    ├── *.class                                           │  │
│  │    └── *.json (webComponent配置)                         │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

## 2. 核心组件设计

### 2.1 WebComponentConfig (配置协议)

定义插件前端组件的元数据。

```typescript
/**
 * Web Component配置接口
 */
interface WebComponentConfig {
  /** Custom Element标签名 (必须包含连字符) */
  tagName: string;
  
  /** 脚本资源URL */
  scriptUrl: string;
  
  /** 组件版本号 (用于缓存失效) */
  version: string;
  
  /** 可选：依赖的共享库 */
  dependencies?: {
    name: string;
    version: string;
    url?: string;
  }[];
  
  /** 可选：CSS样式URL */
  styleUrls?: string[];
  
  /** 可选：组件输入属性定义 */
  inputs?: {
    name: string;
    type: 'string' | 'object' | 'array' | 'boolean' | 'number';
    required: boolean;
  }[];
  
  /** 可选：组件输出事件定义 */
  outputs?: {
    name: string;
    type: string;
  }[];
}
```

**JSON配置示例：**
```json
{
  "inferInstances": {
    "label": "推理结果",
    "elementCreator": "com.qlangtech.tis.plugin.ontology.impl.infer.InferenceParseCreatorFactory",
    "enum": "...",
    "viewtype": "tuplelist",
    "webComponent": {
      "tagName": "ontology-res-inference-result",
      "scriptUrl": "/plugin-assets/tis-ontology-plugin/ontology-res-inference.bundle.js",
      "version": "1.0.0",
      "inputs": [
        {"name": "tabletView", "type": "object", "required": true},
        {"name": "error", "type": "object", "required": false}
      ],
      "outputs": [
        {"name": "tabletViewChange", "type": "object"}
      ]
    }
  }
}
```

### 2.2 DynamicWebComponentLoader (动态加载器)

前端动态加载和管理Web Components的核心服务。

#### 职责
1. **脚本加载**：按需加载插件脚本，避免重复加载
2. **注册等待**：等待Custom Element注册完成
3. **实例管理**：创建和销毁组件实例
4. **属性绑定**：双向数据绑定支持
5. **错误处理**：加载失败的降级和重试

#### 接口设计

```typescript
@Injectable({ providedIn: 'root' })
export class DynamicWebComponentLoader {
  
  /**
   * 脚本加载缓存
   * Key: scriptUrl, Value: Promise<void>
   */
  private scriptCache = new Map<string, Promise<void>>();
  
  /**
   * 组件注册状态
   * Key: tagName, Value: boolean
   */
  private componentRegistry = new Map<string, boolean>();
  
  /**
   * 加载并渲染Web Component
   * 
   * @param config - Web Component配置
   * @param container - 容器元素
   * @param props - 组件属性
   * @returns 组件实例
   */
  async loadAndRender(
    config: WebComponentConfig,
    container: HTMLElement,
    props: Record<string, any>
  ): Promise<HTMLElement> {
    
    // 1. 加载脚本
    await this.loadScript(config);
    
    // 2. 等待注册
    await this.waitForRegistration(config.tagName);
    
    // 3. 创建实例
    const element = document.createElement(config.tagName);
    
    // 4. 设置属性
    this.setProperties(element, props);
    
    // 5. 插入DOM
    container.appendChild(element);
    
    return element;
  }
  
  /**
   * 加载脚本（带缓存）
   */
  private async loadScript(config: WebComponentConfig): Promise<void> {
    const cacheKey = `${config.scriptUrl}?v=${config.version}`;
    
    if (!this.scriptCache.has(cacheKey)) {
      const promise = this.injectScript(cacheKey);
      this.scriptCache.set(cacheKey, promise);
    }
    
    return this.scriptCache.get(cacheKey)!;
  }
  
  /**
   * 注入脚本标签
   */
  private injectScript(url: string): Promise<void> {
    return new Promise((resolve, reject) => {
      const script = document.createElement('script');
      script.src = url;
      script.async = true;
      script.onload = () => resolve();
      script.onerror = () => reject(new Error(`Failed to load ${url}`));
      document.head.appendChild(script);
    });
  }
  
  /**
   * 等待Custom Element注册
   */
  private async waitForRegistration(tagName: string): Promise<void> {
    if (this.componentRegistry.has(tagName)) {
      return;
    }
    
    await customElements.whenDefined(tagName);
    this.componentRegistry.set(tagName, true);
  }
  
  /**
   * 设置组件属性
   */
  private setProperties(element: any, props: Record<string, any>): void {
    for (const [key, value] of Object.entries(props)) {
      element[key] = value;
    }
  }
  
  /**
   * 预加载脚本（优化性能）
   */
  preload(configs: WebComponentConfig[]): void {
    configs.forEach(config => {
      this.loadScript(config).catch(err => {
        console.warn(`Preload failed for ${config.tagName}:`, err);
      });
    });
  }
}
```

### 2.3 DynamicWebComponentHost (宿主组件)

封装动态加载逻辑的Angular组件。

```typescript
@Component({
  selector: 'dynamic-web-component-host',
  template: `
    <div #container class="web-component-container">
      <nz-spin *ngIf="loading" [nzTip]="'加载组件中...'"></nz-spin>
      <nz-alert *ngIf="error" 
                nzType="error" 
                [nzMessage]="'组件加载失败'"
                [nzDescription]="error.message">
      </nz-alert>
    </div>
  `,
  styles: [`
    .web-component-container {
      position: relative;
      min-height: 100px;
    }
  `]
})
export class DynamicWebComponentHostComponent implements OnInit, OnChanges, OnDestroy {
  
  @Input() config: WebComponentConfig;
  @Input() tabletView: TuplesProperty;
  @Input() error: any;
  @Output() tabletViewChange = new EventEmitter<TuplesProperty>();
  
  @ViewChild('container', { static: true }) 
  container: ElementRef<HTMLElement>;
  
  loading = false;
  componentError: Error | null = null;
  private componentInstance: HTMLElement | null = null;
  private eventListeners: Array<() => void> = [];
  
  constructor(private loader: DynamicWebComponentLoader) {}
  
  async ngOnInit() {
    await this.loadComponent();
  }
  
  ngOnChanges(changes: SimpleChanges) {
    if (this.componentInstance) {
      // 更新组件属性
      if (changes['tabletView']) {
        (this.componentInstance as any).tabletView = this.tabletView;
      }
      if (changes['error']) {
        (this.componentInstance as any).error = this.error;
      }
    }
  }
  
  ngOnDestroy() {
    // 清理事件监听器
    this.eventListeners.forEach(unsubscribe => unsubscribe());
    
    // 移除组件实例
    if (this.componentInstance) {
      this.componentInstance.remove();
    }
  }
  
  private async loadComponent() {
    this.loading = true;
    this.componentError = null;
    
    try {
      this.componentInstance = await this.loader.loadAndRender(
        this.config,
        this.container.nativeElement,
        {
          tabletView: this.tabletView,
          error: this.error
        }
      );
      
      // 绑定输出事件
      this.bindOutputs();
      
    } catch (error) {
      this.componentError = error as Error;
      console.error('Failed to load web component:', error);
    } finally {
      this.loading = false;
    }
  }
  
  private bindOutputs() {
    if (!this.componentInstance || !this.config.outputs) {
      return;
    }
    
    this.config.outputs.forEach(output => {
      const handler = (event: CustomEvent) => {
        if (output.name === 'tabletViewChange') {
          this.tabletViewChange.emit(event.detail);
        }
      };
      
      this.componentInstance!.addEventListener(output.name, handler);
      
      // 保存清理函数
      this.eventListeners.push(() => {
        this.componentInstance!.removeEventListener(output.name, handler);
      });
    });
  }
}
```

### 2.4 PluginAssetServlet (后端资源服务)

提供插件静态资源的HTTP访问。

```java
package com.qlangtech.tis.runtime.module.action;

import com.qlangtech.tis.extension.PluginManager;
import com.qlangtech.tis.plugin.ComponentMeta;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 插件前端资源Servlet
 * 
 * URL格式: /plugin-assets/{pluginName}/{resourcePath}
 * 示例: /plugin-assets/tis-ontology-plugin/ontology-res-inference.bundle.js
 */
public class PluginAssetServlet extends HttpServlet {
    
    private static final String RESOURCE_BASE_PATH = "META-INF/webapp/plugin-assets/";
    
    // 资源ETag缓存
    private final ConcurrentMap<String, String> etagCache = new ConcurrentHashMap<>();
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        String pathInfo = req.getPathInfo();
        if (StringUtils.isEmpty(pathInfo)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing resource path");
            return;
        }
        
        // 解析路径: /pluginName/resourcePath
        String[] parts = pathInfo.substring(1).split("/", 2);
        if (parts.length < 2) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid resource path");
            return;
        }
        
        String pluginName = parts[0];
        String resourcePath = parts[1];
        
        // 安全检查：防止目录遍历攻击
        if (resourcePath.contains("..") || resourcePath.startsWith("/")) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid resource path");
            return;
        }
        
        serveResource(pluginName, resourcePath, req, resp);
    }
    
    private void serveResource(String pluginName, String resourcePath,
                               HttpServletRequest req, HttpServletResponse resp) 
            throws IOException {
        
        String fullResourcePath = RESOURCE_BASE_PATH + resourcePath;
        
        // 获取插件ClassLoader
        ClassLoader pluginClassLoader = getPluginClassLoader(pluginName);
        if (pluginClassLoader == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, 
                    "Plugin not found: " + pluginName);
            return;
        }
        
        // 加载资源
        InputStream resourceStream = pluginClassLoader.getResourceAsStream(fullResourcePath);
        if (resourceStream == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, 
                    "Resource not found: " + resourcePath);
            return;
        }
        
        try {
            // 设置内容类型
            String contentType = getContentType(resourcePath);
            resp.setContentType(contentType);
            
            // 设置缓存头
            setCacheHeaders(resp, pluginName, resourcePath, req);
            
            // 输出资源
            IOUtils.copy(resourceStream, resp.getOutputStream());
            
        } finally {
            IOUtils.closeQuietly(resourceStream);
        }
    }
    
    private ClassLoader getPluginClassLoader(String pluginName) {
        PluginManager pm = PluginManager.getInstance();
        ComponentMeta plugin = pm.getPlugin(pluginName);
        if (plugin == null) {
            return null;
        }
        return plugin.getClassLoader();
    }
    
    private String getContentType(String resourcePath) {
        String contentType = URLConnection.guessContentTypeFromName(resourcePath);
        if (contentType != null) {
            return contentType;
        }
        
        // 手动处理常见类型
        if (resourcePath.endsWith(".js")) {
            return "application/javascript; charset=utf-8";
        } else if (resourcePath.endsWith(".css")) {
            return "text/css; charset=utf-8";
        } else if (resourcePath.endsWith(".json")) {
            return "application/json; charset=utf-8";
        } else if (resourcePath.endsWith(".map")) {
            return "application/json; charset=utf-8";
        }
        
        return "application/octet-stream";
    }
    
    private void setCacheHeaders(HttpServletResponse resp, String pluginName, 
                                 String resourcePath, HttpServletRequest req) {
        
        String resourceKey = pluginName + "/" + resourcePath;
        
        // 生成ETag (使用插件名+资源路径+插件版本)
        String etag = generateETag(pluginName, resourcePath);
        etagCache.putIfAbsent(resourceKey, etag);
        
        resp.setHeader("ETag", etag);
        resp.setHeader("Cache-Control", "public, max-age=31536000"); // 1年
        
        // 检查If-None-Match
        String ifNoneMatch = req.getHeader("If-None-Match");
        if (etag.equals(ifNoneMatch)) {
            resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        }
    }
    
    private String generateETag(String pluginName, String resourcePath) {
        PluginManager pm = PluginManager.getInstance();
        ComponentMeta plugin = pm.getPlugin(pluginName);
        String version = plugin != null ? plugin.getVersion() : "unknown";
        
        return "\"" + pluginName + "-" + version + "-" + 
               Integer.toHexString((pluginName + resourcePath).hashCode()) + "\"";
    }
}
```

**Web.xml配置：**
```xml
<servlet>
    <servlet-name>PluginAssetServlet</servlet-name>
    <servlet-class>com.qlangtech.tis.runtime.module.action.PluginAssetServlet</servlet-class>
</servlet>

<servlet-mapping>
    <servlet-name>PluginAssetServlet</servlet-name>
    <url-pattern>/plugin-assets/*</url-pattern>
</servlet-mapping>
```

## 3. 数据流设计

### 3.1 组件加载流程

```
用户访问页面
    ↓
Angular路由激活
    ↓
item-prop-val.component渲染
    ↓
检测到MULTI_SELECTABLE字段
    ↓
创建<dynamic-web-component-host>
    ↓
DynamicWebComponentLoader.loadAndRender()
    ├─→ 检查脚本缓存
    │   └─→ 未缓存 → 注入<script>标签
    │       └─→ 浏览器请求 /plugin-assets/{plugin}/{file}.js
    │           └─→ PluginAssetServlet处理
    │               └─→ 从插件ClassLoader加载资源
    │                   └─→ 返回脚本内容
    ├─→ 等待customElements.whenDefined()
    │   └─→ 脚本执行，注册Custom Element
    └─→ document.createElement(tagName)
        └─→ 设置属性 (tabletView, error)
            └─→ 绑定事件 (tabletViewChange)
                └─→ 插入DOM
                    └─→ 组件渲染完成
```

### 3.2 属性变更流程

```
Angular组件检测到输入变更 (ngOnChanges)
    ↓
更新Web Component属性
    element.tabletView = newValue
    ↓
Web Component内部检测到属性变更 (attributeChangedCallback)
    ↓
触发Angular组件重渲染
    ↓
更新UI
```

### 3.3 事件传递流程

```
Web Component内部触发事件
    this.dispatchEvent(new CustomEvent('tabletViewChange', {detail: data}))
    ↓
DynamicWebComponentHost监听到事件
    element.addEventListener('tabletViewChange', handler)
    ↓
触发Angular输出绑定
    this.tabletViewChange.emit(event.detail)
    ↓
父组件接收事件
    (tabletViewChange)="onTabletViewChange($event)"
```

## 4. 安全性设计

### 4.1 资源访问控制

1. **路径校验**：禁止`..`和绝对路径，防止目录遍历
2. **插件隔离**：通过插件ClassLoader隔离资源访问
3. **白名单机制**：只允许访问`META-INF/webapp/plugin-assets/`目录

### 4.2 内容安全策略 (CSP)

```html
<meta http-equiv="Content-Security-Policy" 
      content="script-src 'self' 'unsafe-eval'; 
               style-src 'self' 'unsafe-inline';">
```

**注意**：`unsafe-eval`可能需要保留以支持动态脚本加载。

### 4.3 脚本完整性验证

可选：在`WebComponentConfig`中添加`integrity`字段：

```json
{
  "scriptUrl": "/plugin-assets/...",
  "integrity": "sha384-oqVuAfXRKap7fdgcCY5uykM6+R9GqQ8K/uxy9rx7HNQlGYl1kPzQho1wx4JwY8wC"
}
```

## 5. 性能优化

### 5.1 脚本加载优化

1. **按需加载**：只加载当前页面需要的组件
2. **预加载**：根据用户导航预测，提前加载可能用到的组件
3. **并行加载**：多个组件脚本并行加载
4. **缓存策略**：利用浏览器缓存和ETag

### 5.2 构建优化

1. **代码分割**：每个组件独立打包
2. **Tree Shaking**：移除未使用代码
3. **压缩**：启用UglifyJS/Terser压缩
4. **Externals**：共享Angular核心库，避免重复打包

### 5.3 运行时优化

1. **Change Detection优化**：Web Component使用OnPush策略
2. **懒初始化**：组件可见时才初始化
3. **虚拟滚动**：大列表场景使用虚拟滚动

## 6. 可扩展性

### 6.1 支持其他框架

架构设计基于Web Components标准，理论上可支持：
- React组件（通过react-to-webcomponent）
- Vue组件（通过@vue/web-component-wrapper）
- Svelte组件（原生支持编译为Web Component）

### 6.2 版本兼容

支持同一组件的多个版本共存：

```json
{
  "tagName": "ontology-res-inference-result-v2",
  "scriptUrl": "/plugin-assets/tis-ontology-plugin/v2/ontology-res-inference.bundle.js",
  "version": "2.0.0"
}
```

### 6.3 运行时热更新

插件更新时，可通过以下机制触发组件重载：
1. 版本号变更触发脚本重新加载
2. WebSocket通知前端刷新组件
3. 页面刷新（最简单方案）

## 7. 降级方案

### 7.1 浏览器不支持Custom Elements

引入polyfill：

```html
<script src="https://unpkg.com/@webcomponents/custom-elements@1.5.0/custom-elements.min.js"></script>
```

### 7.2 脚本加载失败

显示错误提示和重试按钮：

```typescript
if (this.componentError) {
  // 显示错误UI
  // 提供重试功能
}
```

### 7.3 回退到内置组件

如果Web Component加载失败，回退到原有的Angular组件：

```typescript
@Component({
  template: `
    <dynamic-web-component-host *ngIf="useWebComponent else fallback"
                                [config]="config">
    </dynamic-web-component-host>
    
    <ng-template #fallback>
      <ontology-res-inference-result [tabletView]="tabletView">
      </ontology-res-inference-result>
    </ng-template>
  `
})
```

## 8. 监控与调试

### 8.1 加载性能监控

```typescript
performance.mark('wc-load-start');
await loader.loadAndRender(config, container, props);
performance.mark('wc-load-end');
performance.measure('wc-load', 'wc-load-start', 'wc-load-end');
```

### 8.2 错误上报

```typescript
window.addEventListener('error', (event) => {
  if (event.filename.includes('/plugin-assets/')) {
    // 上报插件组件错误
    reportError({
      type: 'plugin-component-error',
      plugin: extractPluginName(event.filename),
      message: event.message
    });
  }
});
```

### 8.3 开发者工具支持

在开发模式下提供调试信息：

```typescript
if (!environment.production) {
  (window as any).__TIS_WEB_COMPONENTS__ = {
    registry: this.componentRegistry,
    scriptCache: this.scriptCache,
    reload: (tagName: string) => { /* ... */ }
  };
}
```

## 9. 总结

本架构设计基于Web Components标准，充分利用了Angular Elements和现代浏览器特性，实现了插件前端组件的真正解耦和动态加载。核心优势：

1. **标准化**：基于W3C标准，未来兼容性好
2. **松耦合**：主应用与插件组件零耦合
3. **性能优良**：按需加载，缓存友好
4. **可维护**：每个插件独立开发和部署
5. **可扩展**：支持多框架、多版本

下一步可参考[implementation-guide.md](./implementation-guide.md)进行实施。