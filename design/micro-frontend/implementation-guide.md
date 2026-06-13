# 微前端插件化架构实施指南

本文档提供分步实施指南，从POC到全面上线。

## 实施路线图

```
阶段1: POC验证 (1周)
    ↓
阶段2: 基础设施 (2周)
    ↓
阶段3: 插件开发模板 (1周)
    ↓
阶段4: 试点迁移 (2周)
    ↓
阶段5: 全面迁移 (4周)
    ↓
阶段6: 清理与优化 (1周)
```

---

## 阶段1: POC验证 (1周)

目标：验证技术可行性，选择最简单的组件做原型。

### 1.1 选择POC组件

建议选择 `jdbc.type.component.ts`，原因：
- 功能相对简单
- 依赖少
- 现有实现稳定

### 1.2 创建POC插件项目

```bash
cd /Users/mozhenghua/j2ee_solution/project/plugins
mkdir tis-poc-webcomponent-plugin
cd tis-poc-webcomponent-plugin
```

**项目结构：**
```
tis-poc-webcomponent-plugin/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/qlangtech/tis/plugin/poc/
│   │   │       └── PocMultiSelectPlugin.java
│   │   └── resources/
│   │       ├── META-INF/
│   │       │   └── webapp/plugin-assets/
│   │       │       └── (编译后的js文件)
│   │       └── com/qlangtech/tis/plugin/poc/
│   │           └── PocMultiSelectPlugin.json
│   └── test/
└── webapp/                      # 前端子项目
    ├── package.json
    ├── tsconfig.json
    ├── webpack.config.js
    └── src/
        └── jdbc-type/
            ├── jdbc-type.component.ts
            ├── jdbc-type.element.ts
            └── index.ts
```

### 1.3 编写后端插件类

**PocMultiSelectPlugin.java:**
```java
package com.qlangtech.tis.plugin.poc;

import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;

import java.util.List;

public class PocMultiSelectPlugin extends Descriptor<PocMultiSelectPlugin> {

    @FormField(
        ordinal = 1, 
        type = FormFieldType.MULTI_SELECTABLE, 
        validate = {Validator.require}
    )
    public List<String> selectedTypes;

    @TISExtension
    public static class DefaultDescriptor extends Descriptor<PocMultiSelectPlugin> {
        @Override
        public String getDisplayName() {
            return "POC多选组件测试";
        }
    }
}
```

**PocMultiSelectPlugin.json:**
```json
{
  "selectedTypes": {
    "label": "选择类型",
    "help": "这是一个使用Web Component的测试组件",
    "enum": "java.util.Arrays.asList(\"Type1\",\"Type2\",\"Type3\")",
    "viewtype": "tuplelist",
    "webComponent": {
      "tagName": "tis-jdbc-type-selector",
      "scriptUrl": "/plugin-assets/tis-poc-webcomponent-plugin/jdbc-type-selector.bundle.js",
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

### 1.4 编写前端组件

**webapp/src/jdbc-type/jdbc-type.component.ts:**
```typescript
import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnInit,
  ChangeDetectorRef
} from '@angular/core';
import { TuplesProperty } from '../../../tis-console/src/common/plugin/type.utils';

@Component({
  selector: 'app-jdbc-type-internal',
  template: `
    <div class="jdbc-type-selector">
      <h4>JDBC类型选择器 (Web Component POC)</h4>
      <nz-checkbox-group [(ngModel)]="selectedItems"
                         (ngModelChange)="onSelectionChange()">
      </nz-checkbox-group>
      <div class="selection-info">
        已选择: {{ selectedItems?.length || 0 }} 项
      </div>
    </div>
  `,
  styles: [`
    .jdbc-type-selector {
      padding: 16px;
      border: 1px solid #d9d9d9;
      border-radius: 4px;
    }
    .selection-info {
      margin-top: 12px;
      color: #666;
      font-size: 12px;
    }
  `]
})
export class JdbcTypeComponent implements OnInit {
  
  @Input() tabletView: TuplesProperty;
  @Input() error: any;
  @Output() tabletViewChange = new EventEmitter<TuplesProperty>();
  
  selectedItems: any[] = [];
  
  constructor(private cdr: ChangeDetectorRef) {}
  
  ngOnInit() {
    // 初始化数据
    if (this.tabletView) {
      // 解析tabletView数据
      this.selectedItems = this.parseTabletView(this.tabletView);
    }
  }
  
  onSelectionChange() {
    // 触发变更事件
    this.tabletViewChange.emit(this.buildTabletView());
    this.cdr.detectChanges();
  }
  
  private parseTabletView(view: TuplesProperty): any[] {
    // 根据实际TuplesProperty结构解析
    return [];
  }
  
  private buildTabletView(): TuplesProperty {
    // 构建TuplesProperty对象
    return null as any;
  }
}
```

**webapp/src/jdbc-type/jdbc-type.element.ts:**
```typescript
import { createCustomElement } from '@angular/elements';
import { createApplication } from '@angular/platform-browser';
import { JdbcTypeComponent } from './jdbc-type.component';
import { provideHttpClient } from '@angular/common/http';
import { importProvidersFrom } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NzCheckboxModule } from 'ng-zorro-antd/checkbox';

// 异步注册Web Component
(async () => {
  try {
    const app = await createApplication({
      providers: [
        provideHttpClient(),
        importProvidersFrom(FormsModule, NzCheckboxModule)
      ]
    });
    
    const element = createCustomElement(JdbcTypeComponent, {
      injector: app.injector
    });
    
    customElements.define('tis-jdbc-type-selector', element);
    
    console.log('[POC] Web Component registered: tis-jdbc-type-selector');
  } catch (error) {
    console.error('[POC] Failed to register web component:', error);
  }
})();
```

**webapp/src/jdbc-type/index.ts:**
```typescript
// 入口文件，导入element定义
import './jdbc-type.element';
```

### 1.5 配置前端构建

**webapp/package.json:**
```json
{
  "name": "@tis/poc-webcomponent",
  "version": "1.0.0",
  "scripts": {
    "build": "webpack --mode production",
    "dev": "webpack --mode development --watch"
  },
  "dependencies": {
    "@angular/core": "^15.2.0",
    "@angular/common": "^15.2.0",
    "@angular/elements": "^15.2.0",
    "@angular/platform-browser": "^15.2.0",
    "@angular/forms": "^15.2.0",
    "ng-zorro-antd": "^15.1.0",
    "rxjs": "^7.8.0",
    "zone.js": "^0.12.0"
  },
  "devDependencies": {
    "@angular-devkit/build-angular": "^15.2.0",
    "@types/node": "^18.0.0",
    "typescript": "~4.9.0",
    "webpack": "^5.75.0",
    "webpack-cli": "^5.0.0",
    "ts-loader": "^9.4.0",
    "@ngtools/webpack": "^15.2.0"
  }
}
```

**webapp/tsconfig.json:**
```json
{
  "compilerOptions": {
    "target": "ES2020",
    "module": "ES2020",
    "moduleResolution": "node",
    "lib": ["ES2020", "dom"],
    "outDir": "../src/main/resources/META-INF/webapp/plugin-assets",
    "sourceMap": true,
    "declaration": false,
    "experimentalDecorators": true,
    "emitDecoratorMetadata": true,
    "skipLibCheck": true,
    "strict": false
  },
  "include": ["src/**/*"],
  "exclude": ["node_modules"]
}
```

**webapp/webpack.config.js:**
```javascript
const path = require('path');
const AngularCompilerPlugin = require('@ngtools/webpack').AngularWebpackPlugin;

module.exports = {
  entry: './src/jdbc-type/index.ts',
  
  output: {
    filename: 'jdbc-type-selector.bundle.js',
    path: path.resolve(__dirname, '../src/main/resources/META-INF/webapp/plugin-assets'),
    clean: true
  },
  
  resolve: {
    extensions: ['.ts', '.js']
  },
  
  module: {
    rules: [
      {
        test: /\.ts$/,
        use: '@ngtools/webpack'
      }
    ]
  },
  
  plugins: [
    new AngularCompilerPlugin({
      tsConfigPath: './tsconfig.json',
      sourceMap: true
    })
  ],
  
  // 外部化依赖（假设主应用已加载）
  externals: {
    '@angular/core': 'ng.core',
    '@angular/common': 'ng.common',
    '@angular/platform-browser': 'ng.platformBrowser',
    '@angular/forms': 'ng.forms',
    'rxjs': 'rxjs',
    'zone.js': 'Zone'
  },
  
  optimization: {
    minimize: true
  }
};
```

### 1.6 配置Maven构建

**pom.xml:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>com.qlangtech.tis.plugins</groupId>
        <artifactId>tis-plugin-parent</artifactId>
        <version>${revision}</version>
        <relativePath>../pom.xml</relativePath>
    </parent>
    
    <artifactId>tis-poc-webcomponent-plugin</artifactId>
    <packaging>tpi</packaging>
    
    <name>TIS POC Web Component Plugin</name>
    
    <build>
        <plugins>
            <!-- 前端构建插件 -->
            <plugin>
                <groupId>com.github.eirslett</groupId>
                <artifactId>frontend-maven-plugin</artifactId>
                <version>1.12.1</version>
                <configuration>
                    <workingDirectory>webapp</workingDirectory>
                    <installDirectory>target/node</installDirectory>
                </configuration>
                <executions>
                    <!-- 安装Node.js和npm -->
                    <execution>
                        <id>install node and npm</id>
                        <goals>
                            <goal>install-node-and-npm</goal>
                        </goals>
                        <configuration>
                            <nodeVersion>v18.16.0</nodeVersion>
                            <npmVersion>9.5.1</npmVersion>
                        </configuration>
                    </execution>
                    
                    <!-- npm install -->
                    <execution>
                        <id>npm install</id>
                        <goals>
                            <goal>npm</goal>
                        </goals>
                        <configuration>
                            <arguments>install</arguments>
                        </configuration>
                    </execution>
                    
                    <!-- webpack build -->
                    <execution>
                        <id>webpack build</id>
                        <goals>
                            <goal>npm</goal>
                        </goals>
                        <phase>generate-resources</phase>
                        <configuration>
                            <arguments>run build</arguments>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
            
            <!-- TPI打包插件 -->
            <plugin>
                <groupId>com.qlangtech.tis</groupId>
                <artifactId>tis-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

### 1.7 实现前端动态加载器（主应用侧）

**在tis-console中创建：**

**src/common/dynamic-loader/web-component-config.ts:**
```typescript
export interface WebComponentConfig {
  tagName: string;
  scriptUrl: string;
  version: string;
  dependencies?: {
    name: string;
    version: string;
    url?: string;
  }[];
  styleUrls?: string[];
  inputs?: {
    name: string;
    type: string;
    required: boolean;
  }[];
  outputs?: {
    name: string;
    type: string;
  }[];
}
```

**src/common/dynamic-loader/dynamic-web-component-loader.service.ts:**
```typescript
import { Injectable } from '@angular/core';
import { WebComponentConfig } from './web-component-config';

@Injectable({ providedIn: 'root' })
export class DynamicWebComponentLoader {
  
  private scriptCache = new Map<string, Promise<void>>();
  private componentRegistry = new Map<string, boolean>();
  
  async loadAndRender(
    config: WebComponentConfig,
    container: HTMLElement,
    props: Record<string, any>
  ): Promise<HTMLElement> {
    
    console.log('[DynamicLoader] Loading component:', config.tagName);
    
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
    
    console.log('[DynamicLoader] Component rendered:', config.tagName);
    
    return element;
  }
  
  private async loadScript(config: WebComponentConfig): Promise<void> {
    const cacheKey = `${config.scriptUrl}?v=${config.version}`;
    
    if (!this.scriptCache.has(cacheKey)) {
      console.log('[DynamicLoader] Loading script:', cacheKey);
      const promise = this.injectScript(cacheKey);
      this.scriptCache.set(cacheKey, promise);
    }
    
    return this.scriptCache.get(cacheKey)!;
  }
  
  private injectScript(url: string): Promise<void> {
    return new Promise((resolve, reject) => {
      const script = document.createElement('script');
      script.src = url;
      script.async = true;
      script.crossOrigin = 'anonymous';
      
      script.onload = () => {
        console.log('[DynamicLoader] Script loaded:', url);
        resolve();
      };
      
      script.onerror = (error) => {
        console.error('[DynamicLoader] Script load failed:', url, error);
        reject(new Error(`Failed to load ${url}`));
      };
      
      document.head.appendChild(script);
    });
  }
  
  private async waitForRegistration(tagName: string): Promise<void> {
    if (this.componentRegistry.has(tagName)) {
      return;
    }
    
    console.log('[DynamicLoader] Waiting for registration:', tagName);
    
    await customElements.whenDefined(tagName);
    this.componentRegistry.set(tagName, true);
    
    console.log('[DynamicLoader] Component registered:', tagName);
  }
  
  private setProperties(element: any, props: Record<string, any>): void {
    for (const [key, value] of Object.entries(props)) {
      element[key] = value;
    }
  }
  
  preload(configs: WebComponentConfig[]): void {
    configs.forEach(config => {
      this.loadScript(config).catch(err => {
        console.warn(`[DynamicLoader] Preload failed for ${config.tagName}:`, err);
      });
    });
  }
}
```

**src/common/dynamic-loader/dynamic-web-component-host.component.ts:**
```typescript
import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnInit,
  OnChanges,
  OnDestroy,
  ViewChild,
  ElementRef,
  SimpleChanges,
  ChangeDetectorRef
} from '@angular/core';
import { DynamicWebComponentLoader } from './dynamic-web-component-loader.service';
import { WebComponentConfig } from './web-component-config';
import { TuplesProperty } from '../plugin/type.utils';

@Component({
  selector: 'dynamic-web-component-host',
  template: `
    <div #container class="web-component-container">
      <nz-spin *ngIf="loading" [nzTip]="'加载组件中...'"></nz-spin>
      <nz-alert *ngIf="error" 
                nzType="error" 
                nzMessage="组件加载失败"
                [nzDescription]="error.message"
                nzShowIcon>
        <button nz-button nzType="link" (click)="retry()">重试</button>
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
  
  constructor(
    private loader: DynamicWebComponentLoader,
    private cdr: ChangeDetectorRef
  ) {}
  
  async ngOnInit() {
    await this.loadComponent();
  }
  
  ngOnChanges(changes: SimpleChanges) {
    if (this.componentInstance) {
      if (changes['tabletView']) {
        (this.componentInstance as any).tabletView = this.tabletView;
      }
      if (changes['error']) {
        (this.componentInstance as any).error = this.error;
      }
    }
  }
  
  ngOnDestroy() {
    this.cleanup();
  }
  
  async retry() {
    this.cleanup();
    await this.loadComponent();
  }
  
  private async loadComponent() {
    if (!this.config) {
      console.warn('[DynamicHost] No config provided');
      return;
    }
    
    this.loading = true;
    this.componentError = null;
    this.cdr.detectChanges();
    
    try {
      this.componentInstance = await this.loader.loadAndRender(
        this.config,
        this.container.nativeElement,
        {
          tabletView: this.tabletView,
          error: this.error
        }
      );
      
      this.bindOutputs();
      
    } catch (error) {
      this.componentError = error as Error;
      console.error('[DynamicHost] Failed to load component:', error);
    } finally {
      this.loading = false;
      this.cdr.detectChanges();
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
      
      this.eventListeners.push(() => {
        this.componentInstance!.removeEventListener(output.name, handler);
      });
    });
  }
  
  private cleanup() {
    this.eventListeners.forEach(unsubscribe => unsubscribe());
    this.eventListeners = [];
    
    if (this.componentInstance) {
      this.componentInstance.remove();
      this.componentInstance = null;
    }
  }
}
```

### 1.8 修改item-prop-val.component.ts

在MULTI_SELECTABLE的switch case中添加：

```typescript
// 在item-prop-val.component.ts中找到MULTI_SELECTABLE的处理逻辑

<ng-container *ngSwitchCase="8"> <!-- MULTI_SELECTABLE -->
  
  <!-- 检查是否有webComponent配置 -->
  <ng-container *ngIf="_pp._eprops['webComponent']; else legacyMultiSelect">
    <dynamic-web-component-host
      [config]="_pp._eprops['webComponent']"
      [attr.data-testid]="_pp.key"
      [(tabletView)]="_pp.mcolsEnums"
      [error]="_pp.error">
    </dynamic-web-component-host>
  </ng-container>
  
  <!-- 原有的多选逻辑（向后兼容） -->
  <ng-template #legacyMultiSelect>
    <!-- 原来的 ontology-res-inference-result 等组件路由逻辑 -->
    <ng-container [ngSwitch]="_pp.mcolsEnums?.viewType()">
      <ng-container *ngSwitchCase="'ontologyResInference'">
        <ontology-res-inference-result ...>
        </ontology-res-inference-result>
      </ng-container>
      <!-- ... 其他现有组件 ... -->
    </ng-container>
  </ng-template>
  
</ng-container>
```

### 1.9 测试POC

```bash
# 1. 构建POC插件
cd /Users/mozhenghua/j2ee_solution/project/plugins/tis-poc-webcomponent-plugin
mvn clean package -Dmaven.test.skip=true

# 2. 复制到TIS插件目录
cp target/tis-poc-webcomponent-plugin.tpi ~/tis-plugins/

# 3. 重启TIS
cd /Users/mozhenghua/j2ee_solution/project/tis-solr
./restart.sh

# 4. 访问测试页面，观察：
#    - 浏览器Network中是否成功加载bundle.js
#    - Console中是否有Web Component注册日志
#    - 组件是否正常渲染和交互
```

**验证清单：**
- [ ] 脚本成功加载
- [ ] Custom Element成功注册
- [ ] 组件正常渲染
- [ ] 数据双向绑定正常
- [ ] 事件传递正常
- [ ] 错误处理正常
- [ ] 样式隔离正常

---

## 阶段2: 基础设施 (2周)

POC成功后，完善基础设施。

### 2.1 实现PluginAssetServlet

参考`architecture.md`中的完整实现，放置在：
```
/Users/mozhenghua/j2ee_solution/project/tis-solr/tis-console/src/main/java/com/qlangtech/tis/runtime/module/action/PluginAssetServlet.java
```

### 2.2 配置Web.xml

在`tis-web-start/src/main/webapp/WEB-INF/web.xml`中添加Servlet映射。

### 2.3 扩展Descriptor支持

在`Descriptor.java`中添加webComponent配置读取：

```java
public Optional<WebComponentConfig> getWebComponentConfig(String fieldName) {
    JSONObject fieldMeta = getPropertyType(fieldName).getMetadata();
    if (fieldMeta.containsKey("webComponent")) {
        return Optional.of(
            JSONObject.parseObject(
                fieldMeta.getString("webComponent"), 
                WebComponentConfig.class
            )
        );
    }
    return Optional.empty();
}
```

### 2.4 前端Module注册

在`app.module.ts`中注册新模块：

```typescript
import { DynamicWebComponentHostComponent } from './common/dynamic-loader/dynamic-web-component-host.component';

@NgModule({
  declarations: [
    // ...
    DynamicWebComponentHostComponent
  ],
  // ...
})
export class AppModule {}
```

---

## 阶段3: 插件开发模板 (1周)

创建可复用的开发模板。

### 3.1 创建Maven Archetype

```bash
cd /Users/mozhenghua/j2ee_solution/project
mkdir tis-webcomponent-plugin-archetype
```

参考`examples/build/maven-archetype/`目录结构。

### 3.2 编写开发文档

创建`PLUGIN_DEVELOPER_GUIDE.md`，指导插件开发者如何：
1. 创建插件项目
2. 编写Web Component
3. 配置构建
4. 调试和测试

---

## 阶段4: 试点迁移 (2周)

选择2-3个现有组件进行迁移，验证迁移流程。

**建议迁移顺序：**
1. `jdbc.type.component.ts` - 最简单
2. `schema.edit.component.ts` - 中等复杂度
3. `ontology.res.inference.component.ts` - 最复杂

---

## 阶段5: 全面迁移 (4周)

迁移`multi-selected/`目录下的所有11个组件。

参考`migration-plan.md`获取详细计划。

---

## 阶段6: 清理与优化 (1周)

### 6.1 清理遗留代码

删除已迁移组件的原始文件。

### 6.2 性能优化

- 启用脚本预加载
- 优化bundle大小
- 配置CDN

### 6.3 文档完善

- 更新开发文档
- 编写最佳实践
- 制作视频教程

---

## 故障排查

### 问题1: 脚本404

**症状：** 浏览器Network显示404

**排查：**
1. 检查`scriptUrl`配置是否正确
2. 检查PluginAssetServlet是否正确映射
3. 检查插件包中是否包含js文件
4. 检查ClassLoader是否能加载资源

```bash
# 解压tpi查看内容
unzip -l target/xxx.tpi | grep bundle.js
```

### 问题2: Custom Element未注册

**症状：** Console显示"Component not registered"

**排查：**
1. 检查脚本是否成功执行（Console有无报错）
2. 检查`customElements.define()`是否被调用
3. 检查tagName是否正确（必须包含连字符）

```javascript
// 在Console中检查
console.log(customElements.get('tis-jdbc-type-selector'));
```

### 问题3: 属性不更新

**症状：** 修改输入属性，组件不响应

**排查：**
1. 检查Web Component是否实现了`attributeChangedCallback`
2. 检查属性名是否匹配
3. 使用Chrome DevTools检查元素属性

### 问题4: 样式冲突

**症状：** 主应用样式影响Web Component

**解决：**
使用Shadow DOM：

```typescript
@Component({
  selector: 'app-jdbc-type-internal',
  encapsulation: ViewEncapsulation.ShadowDom,
  // ...
})
```

---

## 下一步

完成实施后，参考：
- [migration-plan.md](./migration-plan.md) - 迁移计划
- [examples/](./examples/) - 示例代码
- [poc/](./poc/) - POC完整实现

有问题请查看各文档或咨询架构团队。
