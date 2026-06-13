# 现有组件迁移计划

本文档提供将`tis-console/src/common/multi-selected/`目录下11个组件迁移到微前端架构的详细计划。

## 组件清单

根据复杂度和依赖关系，将组件分为3个优先级：

### P1 - 简单组件（2周）

| 组件 | 文件 | 复杂度 | 依赖 | 预计工时 |
|------|------|--------|------|----------|
| JDBC类型选择 | jdbc.type.component.ts | 低 | 少 | 2天 |
| JDBC类型属性 | jdbc.type.props.component.ts | 低 | 少 | 2天 |
| 单值多选 | multi.select.single.val.component.ts | 低 | 少 | 2天 |

### P2 - 中等复杂度组件（3周）

| 组件 | 文件 | 复杂度 | 依赖 | 预计工时 |
|------|------|--------|------|----------|
| 表连接匹配条件 | table.join.match.condition.component.ts | 中 | 中 | 3天 |
| 表连接过滤条件 | table.join.filter.condition.component.ts | 中 | 中 | 3天 |
| Schema编辑器 | schema.edit.component.ts | 中 | 中 | 4天 |
| 转换规则 | transformer.rules.component.ts | 中 | 多 | 4天 |

### P3 - 复杂组件（3周）

| 组件 | 文件 | 复杂度 | 依赖 | 预计工时 |
|------|------|--------|------|----------|
| 本体属性列表 | ontology.props.list.component.ts | 高 | 多 | 4天 |
| 本体属性角色类型链接 | ontology.prop.role.type.linker.component.ts | 高 | 多 | 4天 |
| 本体资源推理 | ontology.res.inference.component.ts | 高 | 多 | 5天 |

### 基础组件

| 组件 | 文件 | 说明 |
|------|------|------|
| 基础元组视图 | basic.tuples.view.component.ts | 抽象基类，需特殊处理 |

## 迁移流程

每个组件的迁移流程：

```
1. 分析组件
   ├─ 识别依赖（服务、模块、第三方库）
   ├─ 识别输入输出
   ├─ 识别后端交互API
   └─ 评估复杂度

2. 创建插件项目
   ├─ 从模板创建项目结构
   ├─ 配置pom.xml
   └─ 配置package.json

3. 迁移组件代码
   ├─ 复制组件文件
   ├─ 调整导入路径
   ├─ 处理依赖注入
   └─ 适配Web Component接口

4. 配置后端
   ├─ 编写插件Java类
   ├─ 配置JSON描述文件
   └─ 添加webComponent配置

5. 构建测试
   ├─ 本地构建
   ├─ 部署测试环境
   ├─ 功能测试
   └─ 回归测试

6. 清理
   ├─ 删除原文件
   ├─ 更新文档
   └─ Code Review
```

## 详细迁移步骤示例

以`jdbc.type.component.ts`为例：

### 步骤1: 分析组件

**原组件位置：**
```
/Users/mozhenghua/j2ee_solution/project/tis-console/src/common/multi-selected/jdbc.type.component.ts
```

**依赖分析：**
```typescript
// 导入分析
import { TISService } from '../tis.service';  // 核心服务
import { NzModalService } from 'ng-zorro-antd/modal';  // UI库
import { BasicTuplesViewComponent } from './basic.tuples.view.component';  // 基类

// 输入
@Input() tabletView: TuplesProperty
@Input() error: any

// 输出
@Output() tabletViewChange = new EventEmitter()

// 后端API
this.httpPost('/xxx/action', params)
```

**复杂度评估：**
- 代码行数: ~200行
- 依赖服务: 3个
- 后端API调用: 2个
- 评级: 低

### 步骤2: 创建插件项目

```bash
# 使用Maven Archetype创建
cd /Users/mozhenghua/j2ee_solution/project/plugins

mvn archetype:generate \
  -DarchetypeGroupId=com.qlangtech.tis \
  -DarchetypeArtifactId=tis-webcomponent-plugin-archetype \
  -DarchetypeVersion=1.0.0 \
  -DgroupId=com.qlangtech.tis.plugins \
  -DartifactId=tis-jdbc-type-plugin \
  -Dversion=1.0.0-SNAPSHOT \
  -DcomponentName=JdbcTypeSelector \
  -DtagName=tis-jdbc-type-selector
```

**生成的项目结构：**
```
tis-jdbc-type-plugin/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/qlangtech/tis/plugins/jdbc/
│   │   │       └── JdbcTypeSelectorPlugin.java
│   │   └── resources/
│   │       └── com/qlangtech/tis/plugins/jdbc/
│   │           └── JdbcTypeSelectorPlugin.json
│   └── test/
└── webapp/
    ├── package.json
    ├── tsconfig.json
    ├── webpack.config.js
    └── src/
        └── jdbc-type-selector/
            ├── jdbc-type-selector.component.ts
            ├── jdbc-type-selector.element.ts
            └── index.ts
```

### 步骤3: 迁移组件代码

**3.1 复制并重构组件**

```bash
# 复制原组件
cp /Users/mozhenghua/j2ee_solution/project/tis-console/src/common/multi-selected/jdbc.type.component.ts \
   tis-jdbc-type-plugin/webapp/src/jdbc-type-selector/jdbc-type-selector.component.ts
```

**3.2 调整导入路径**

原代码：
```typescript
import { TISService } from '../tis.service';
import { BasicTuplesViewComponent } from './basic.tuples.view.component';
```

迁移后：
```typescript
// 需要将依赖的服务和基类一起迁移或重新实现
import { TISService } from '../shared/tis.service';
import { BasicTuplesViewComponent } from '../shared/basic.tuples.view.component';
```

**3.3 处理共享依赖**

创建共享模块：
```
webapp/src/shared/
├── tis.service.ts         // 从主应用复制核心服务
├── basic.tuples.view.component.ts  // 基类
└── types.ts               // 共享类型定义
```

**3.4 适配Web Component接口**

```typescript
// 原组件（Angular Component）
@Component({
  selector: 'jdbc-type-selector',
  template: `...`
})
export class JdbcTypeComponent extends BasicTuplesViewComponent {
  @Input() tabletView: TuplesProperty;
  @Output() tabletViewChange = new EventEmitter();
}

// 适配为Web Component
@Component({
  selector: 'app-jdbc-type-internal',  // 内部选择器
  template: `...`,
  encapsulation: ViewEncapsulation.ShadowDom  // Shadow DOM隔离
})
export class JdbcTypeComponent extends BasicTuplesViewComponent {
  // 输入属性需要手动处理（Web Component传入）
  private _tabletView: TuplesProperty;
  
  set tabletView(value: TuplesProperty) {
    this._tabletView = value;
    this.onTabletViewChange();
  }
  
  get tabletView(): TuplesProperty {
    return this._tabletView;
  }
  
  // 输出事件改为原生CustomEvent
  emitChange(data: TuplesProperty) {
    const event = new CustomEvent('tabletViewChange', {
      detail: data,
      bubbles: true,
      composed: true  // 穿透Shadow DOM
    });
    this.elementRef.nativeElement.dispatchEvent(event);
  }
}
```

**3.5 创建Web Component包装器**

```typescript
// webapp/src/jdbc-type-selector/jdbc-type-selector.element.ts

import { createCustomElement } from '@angular/elements';
import { createApplication } from '@angular/platform-browser';
import { JdbcTypeComponent } from './jdbc-type-selector.component';
import { provideHttpClient } from '@angular/common/http';
import { importProvidersFrom } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NzModalModule } from 'ng-zorro-antd/modal';
import { NzTableModule } from 'ng-zorro-antd/table';

(async () => {
  try {
    const app = await createApplication({
      providers: [
        provideHttpClient(),
        importProvidersFrom(
          FormsModule,
          NzModalModule,
          NzTableModule
        )
      ]
    });
    
    const element = createCustomElement(JdbcTypeComponent, {
      injector: app.injector
    });
    
    customElements.define('tis-jdbc-type-selector', element);
    
    console.log('[TIS Plugin] JDBC Type Selector registered');
  } catch (error) {
    console.error('[TIS Plugin] Failed to register component:', error);
  }
})();
```

### 步骤4: 配置后端

**4.1 编写插件类**

```java
// src/main/java/com/qlangtech/tis/plugins/jdbc/JdbcTypeSelectorPlugin.java

package com.qlangtech.tis.plugins.jdbc;

import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;

import java.util.List;

public class JdbcTypeSelectorPlugin extends DataSourceFactory {

    @FormField(
        ordinal = 1,
        type = FormFieldType.MULTI_SELECTABLE,
        validate = {Validator.require}
    )
    public List<JdbcTypeItem> jdbcTypes;

    @TISExtension
    public static class DefaultDescriptor extends Descriptor<JdbcTypeSelectorPlugin> {
        @Override
        public String getDisplayName() {
            return "JDBC类型选择器";
        }
    }
    
    public static class JdbcTypeItem {
        public String typeName;
        public String typeCode;
        public boolean selected;
    }
}
```

**4.2 配置JSON描述**

```json
// src/main/resources/com/qlangtech/tis/plugins/jdbc/JdbcTypeSelectorPlugin.json

{
  "jdbcTypes": {
    "label": "JDBC类型",
    "help": "选择支持的JDBC类型",
    "elementCreator": "com.qlangtech.tis.plugins.jdbc.JdbcTypeItemCreatorFactory",
    "enum": "com.qlangtech.tis.plugins.jdbc.JdbcTypeSelectorPlugin.getAvailableTypes()",
    "viewtype": "tuplelist",
    "webComponent": {
      "tagName": "tis-jdbc-type-selector",
      "scriptUrl": "/plugin-assets/tis-jdbc-type-plugin/jdbc-type-selector.bundle.js",
      "version": "1.0.0",
      "inputs": [
        {
          "name": "tabletView",
          "type": "object",
          "required": true
        },
        {
          "name": "error",
          "type": "object",
          "required": false
        }
      ],
      "outputs": [
        {
          "name": "tabletViewChange",
          "type": "object"
        }
      ]
    }
  }
}
```

### 步骤5: 构建测试

**5.1 本地构建**

```bash
cd tis-jdbc-type-plugin

# 安装前端依赖
cd webapp
npm install

# 手动测试webpack构建
npm run build

# 检查输出
ls -lh ../src/main/resources/META-INF/webapp/plugin-assets/

# Maven完整构建
cd ..
mvn clean package -Dmaven.test.skip=true
```

**5.2 部署测试**

```bash
# 复制到插件目录
cp target/tis-jdbc-type-plugin.tpi ~/tis-plugins/

# 重启TIS
~/tis/bin/restart.sh

# 观察日志
tail -f ~/tis/logs/tis.log
```

**5.3 功能测试清单**

- [ ] 组件正常加载显示
- [ ] 初始数据正确渲染
- [ ] 用户交互（勾选/取消）正常
- [ ] 数据变更正确回传
- [ ] 表单验证正常
- [ ] 错误处理正常
- [ ] 样式正常（无冲突）
- [ ] 在不同浏览器测试（Chrome, Firefox, Safari）

**5.4 回归测试**

- [ ] 原有使用此字段的所有插件功能正常
- [ ] 数据持久化正常
- [ ] 与其他字段联动正常

### 步骤6: 清理

**6.1 删除原文件**

```bash
# 备份原文件
mkdir -p ~/tis-migration-backup/jdbc-type
cp /Users/mozhenghua/j2ee_solution/project/tis-console/src/common/multi-selected/jdbc.type.component.ts \
   ~/tis-migration-backup/jdbc-type/

# 删除原文件
git rm /Users/mozhenghua/j2ee_solution/project/tis-console/src/common/multi-selected/jdbc.type.component.ts
```

**6.2 更新item-prop-val.component.ts**

移除对应的hard-coded路由：
```typescript
// 删除这部分
<ng-container *ngSwitchCase="'jdbcType'">
  <jdbc-type-selector [tabletView]="..." />
</ng-container>

// 统一由dynamic-web-component-host处理
```

**6.3 更新模块声明**

从`app.module.ts`中移除：
```typescript
import { JdbcTypeComponent } from './common/multi-selected/jdbc.type.component';

declarations: [
  // JdbcTypeComponent,  // 删除
]
```

**6.4 更新文档**

- 在插件README中添加迁移说明
- 更新开发者文档
- 记录已知问题

**6.5 提交代码**

```bash
git add .
git commit -m "feat: migrate jdbc.type.component to web component plugin

- 创建tis-jdbc-type-plugin插件
- 迁移jdbc.type.component.ts为Web Component
- 删除原multi-selected目录下的硬编码组件
- 更新item-prop-val路由逻辑

BREAKING CHANGE: jdbc.type.component不再在主应用中，需要安装tis-jdbc-type-plugin插件

Ref: #ISSUE-NUMBER"
```

## 迁移时间表

### 第1-2周: P1组件

- Week 1
  - Day 1-2: jdbc.type.component.ts
  - Day 3-4: jdbc.type.props.component.ts
  - Day 5: 测试和修复
- Week 2
  - Day 1-2: multi.select.single.val.component.ts
  - Day 3-5: 测试和文档

### 第3-5周: P2组件

- Week 3
  - Day 1-3: table.join.match.condition.component.ts
  - Day 4-5: 测试
- Week 4
  - Day 1-3: table.join.filter.condition.component.ts
  - Day 4-5: 测试
- Week 5
  - Day 1-4: schema.edit.component.ts + transformer.rules.component.ts
  - Day 5: 集成测试

### 第6-8周: P3组件

- Week 6
  - Day 1-4: ontology.props.list.component.ts
  - Day 5: 测试
- Week 7
  - Day 1-4: ontology.prop.role.type.linker.component.ts
  - Day 5: 测试
- Week 8
  - Day 1-5: ontology.res.inference.component.ts（最复杂）

### 第9周: 收尾

- 全面回归测试
- 性能优化
- 文档完善
- 培训与交接

## 风险管理

### 高风险项

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 共享依赖处理 | 高 | 提前抽取shared模块 |
| 基类BasicTuplesView | 高 | 先迁移基类，或在插件中复制 |
| 样式冲突 | 中 | 使用Shadow DOM隔离 |
| 性能下降 | 中 | 脚本预加载、代码分割 |
| 浏览器兼容性 | 中 | 引入polyfill，充分测试 |

### 回退策略

如果迁移遇到重大问题：

1. **Phase 1回退**（迁移前3个组件时发现问题）
   - 删除已迁移的插件
   - 恢复原文件
   - 重新评估方案

2. **Phase 2回退**（迁移一半发现问题）
   - 已迁移的保留（已充分测试）
   - 暂停未迁移的
   - 新老并存，逐步切换

3. **降级兼容**
   - 保留原组件作为fallback
   - 根据配置开关选择新旧实现

## 质量标准

每个迁移的组件必须满足：

### 功能性
- [ ] 所有原功能正常工作
- [ ] 数据双向绑定正确
- [ ] 表单验证正常
- [ ] 错误处理完善

### 性能
- [ ] 首次加载时间 < 500ms
- [ ] Bundle大小 < 200KB（gzip后）
- [ ] 无内存泄漏

### 兼容性
- [ ] Chrome 90+
- [ ] Firefox 88+
- [ ] Safari 14+
- [ ] Edge 90+

### 代码质量
- [ ] TypeScript编译无错误
- [ ] ESLint无警告
- [ ] 代码覆盖率 > 80%
- [ ] 通过Code Review

### 文档
- [ ] README完整
- [ ] API文档清晰
- [ ] 示例代码可运行

## 资源需求

### 人力
- 前端开发: 2人
- 后端开发: 1人（辅助）
- 测试: 1人
- 项目协调: 1人

### 工具
- Node.js 18+
- npm/yarn
- Webpack 5
- Chrome DevTools

### 环境
- 开发环境: 每人一套
- 测试环境: 1套
- 预发布环境: 1套

## 总结

通过系统化的迁移计划，我们可以：
1. 分阶段、低风险地完成迁移
2. 确保每个组件质量达标
3. 积累经验，优化后续流程
4. 最终实现完全的插件化架构

迁移完成后，新增功能组件只需开发插件，无需修改核心代码，真正符合OCP原则。
