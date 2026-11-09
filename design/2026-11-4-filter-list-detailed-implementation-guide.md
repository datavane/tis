# Filter List Widget 详细实现指南（面向 DeepSeek 执行）

> **目标**：提供精确、无歧义的实现步骤，最小化自由发挥空间。
> **基线版本**：TIS 5.1.0（JDK 17）
> **实施优先级**：P0-2（Pills 布局）→ P0-3（用户增删）→ P0-4（默认值）→ P0-1（直方图）

---

## 零、前置约束与规范

### 0.1 代码规范（强制）

1. **JDK 17 语法规范**（见 `CLAUDE.md`）：
   - `instanceof` 必须使用模式匹配：`if (x instanceof Foo foo) { ... }`
   - 禁止 JDK 8 旧写法：`if (x instanceof Foo) { Foo foo = (Foo) x; ... }`
   - 多行字符串使用文本块 `"""`
   - Stream 转 List 用 `.toList()` 而非 `.collect(Collectors.toList())`

2. **插件属性设计原则**（见 `CLAUDE.md`）：
   - 禁止 JSON 字符串输入：结构化数据必须用 `@SubForm` 或 Describable 子类
   - 固定取值必须用 `@FormField(type = ENUM)` + Java 枚举
   - 枚举中文 label 走 `shortComment` 或 `.json` 的 `help`
   - 枚举颜色字段需在 `.json` 附带 `hex` 色值

3. **文件修改原则**：
   - 后端 Java 文件路径：`/Users/mozhenghua/j2ee_solution/project/plugins/tis-ontology-plugin/src/main/java/com/qlangtech/tis/plugin/ontology/workshop/widget/impl/`
   - 前端 TypeScript 文件路径：`/Users/mozhenghua/j2ee_solution/project/tis-console/src/workshop/components/widgets/filter-list/`
   - `.json` 资源文件路径：`/Users/mozhenghua/j2ee_solution/project/plugins/tis-ontology-plugin/src/main/resources/com/qlangtech/tis/plugin/ontology/workshop/widget/impl/`
   - 修改现有文件时，必须先 Read 全文，再用 Edit 工具精确替换

4. **命名约定**：
   - Java 枚举常量：大写下划线（`VERTICAL`, `PILLS`）
   - TypeScript 枚举/联合类型：与 Java 逐字对应（前端接收的是大写字符串）
   - 前端组件选择器：kebab-case（`filter-list.component.ts`）
   - CSS 类名：kebab-case（`.filter-pills-container`）

### 0.2 现有代码结构（基线）

**后端核心类**：
```
FilterListWidget.java                  // 主 Widget 类
  - objectSetVar: String               // 对象集变量（ordinal 20）
  - filters: List<FilterItemConfig>    // 过滤项列表（ordinal 21）
  - filterOutputVar: String            // 输出变量（ordinal 22）

FilterItemConfig.java                  // 过滤项配置（元素类）
  - property: String                   // 属性名（SELECTABLE）
  - component: FilterComponent         // 组件实例（多态字段）

FilterComponent.java                   // 抽象基类
  ├─ KeywordComponent                  // 关键词搜索
  ├─ SingleSelectComponent             // 单选下拉
  ├─ MultiSelectComponent              // 多选下拉
  └─ DatePickerComponent               // 日期选择
```

**前端核心文件**：
```
filter-list.component.ts               // 主组件（204行）
  - config: FilterListWidgetConfig
  - filters: FilterState[]             // 运行时数组
  - onFilterChange()                   // 写出到 filterOutputVar

filter-list.component.html             // 模板（当前只有垂直布局）
filter-list.component.scss             // 样式
widget-config.model.ts                 // 归一化器（normalizeFilterList）
```

---

## 一、P0-2：Pills 布局实现（工作量：1 天）

### 1.1 后端改动（3 个文件）

#### 文件 1：`FilterListWidget.java`（新增字段 + 枚举）

**位置**：第 93 行之后（`filterOutputVar` 字段之后）

**新增代码**：
```java
    /**
     * 布局模式：垂直列表 vs Pills（横向胶囊）。
     * 
     * <p>Pills 模式只显示有值的过滤项，空项不渲染，节省空间。
     * 点击 pill 弹出 popover 编辑。
     */
    @FormField(type = FormFieldType.ENUM, ordinal = 19, validate = {Validator.require})
    public LayoutMode layout;

    /**
     * 布局模式枚举
     */
    public enum LayoutMode {
        /** 垂直列表（默认） */
        VERTICAL("垂直布局"),
        /** 横向胶囊 */
        PILLS("Pills布局");

        private final String label;

        LayoutMode(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
```

**注意事项**：
- `ordinal = 19` 排在 `objectSetVar`(20) 之前，让布局选择在表单顶部
- 必须实现 `getLabel()` 方法，前端需要显示中文
- 枚举常量名（`VERTICAL`/`PILLS`）前端会直接接收，不要改

#### 文件 2：`FilterListWidget.json`（新增，配置枚举 label）

**路径**：`/Users/mozhenghua/j2ee_solution/project/plugins/tis-ontology-plugin/src/main/resources/com/qlangtech/tis/plugin/ontology/workshop/widget/impl/FilterListWidget.json`

**完整内容**：
```json
{
  "layout": {
    "help": "选择过滤器的显示布局方式",
    "dftVal": "VERTICAL",
    "enum": [
      {
        "label": "垂直列表",
        "val": "VERTICAL"
      },
      {
        "label": "Pills（横向胶囊）",
        "val": "PILLS"
      }
    ]
  },
  "objectSetVar": {
    "help": "选择要过滤的对象集变量，决定过滤项的属性候选"
  },
  "filters": {
    "help": "配置过滤项列表，每个过滤项对应一个可筛选属性"
  },
  "filterOutputVar": {
    "help": "选择输出变量（对象集过滤器类型），过滤条件将写入该变量供下游使用"
  }
}
```

**注意事项**：
- `dftVal: "VERTICAL"` 确保新建 widget 时默认垂直布局（向后兼容）
- `enum` 数组的 `val` 必须与 Java 枚举常量名完全一致（大写）
- 不要写 `hex` 字段（layout 不是颜色选择）

#### 文件 3：Java 编译验证

**命令**：
```bash
cd /Users/mozhenghua/j2ee_solution/project/plugins/tis-ontology-plugin
mvn clean compile -Dmaven.test.skip=true -o
```

**预期输出**：
```
[INFO] BUILD SUCCESS
```

**失败处理**：
- 如出现 `cannot find symbol: LayoutMode`，检查枚举是否定义在 `FilterListWidget` 内部
- 如出现 `ordinal 19 conflicts with ...`，检查是否有其他字段占用了 ordinal 19

---

### 1.2 前端改动（3 个文件）

#### 文件 1：`filter-list.component.ts`（修改，新增 Pills 逻辑）

**步骤 1.2.1**：在 `FilterListComponent` 类开头新增字段

**位置**：第 30 行左右（`config` 字段之后）

**新增代码**：
```typescript
  /** 是否为 Pills 布局模式 */
  get isPillsLayout(): boolean {
    return this.config?.layout === 'PILLS';
  }

  /** Pills 模式下，只显示有值的过滤项 */
  get visibleFilters(): FilterState[] {
    if (!this.isPillsLayout) {
      return this.filters;
    }
    return this.filters.filter(f => !this.isEmptyFilterValue(f.value));
  }
```

**步骤 1.2.2**：新增空值判断方法

**位置**：组件类的最后（`ngOnDestroy` 之前）

**新增代码**：
```typescript
  /**
   * 判断过滤值是否为空
   */
  private isEmptyFilterValue(value: any): boolean {
    if (value === undefined || value === null) {
      return true;
    }
    if (typeof value === 'string' && value.trim() === '') {
      return true;
    }
    if (Array.isArray(value) && value.length === 0) {
      return true;
    }
    return false;
  }

  /**
   * Pills 模式：格式化过滤值为显示文本
   */
  getFilterDisplayValue(filter: FilterState): string {
    if (this.isEmptyFilterValue(filter.value)) {
      return '未设置';
    }
    
    const componentType = filter.componentType;
    if (componentType === 'keyword') {
      return String(filter.value);
    } else if (componentType === 'single-select') {
      // 从 options 中找 label
      const option = filter.options?.find(opt => opt.val === filter.value);
      return option?.label || String(filter.value);
    } else if (componentType === 'multi-select') {
      if (Array.isArray(filter.value)) {
        return filter.value.map(v => {
          const option = filter.options?.find(opt => opt.val === v);
          return option?.label || v;
        }).join(', ');
      }
      return String(filter.value);
    } else if (componentType === 'date-picker') {
      // 日期格式化
      if (typeof filter.value === 'string') {
        return filter.value.substring(0, 10); // YYYY-MM-DD
      }
      return String(filter.value);
    }
    return String(filter.value);
  }
```

#### 文件 2：`filter-list.component.html`（重写，支持两种布局）

**完整替换内容**：
```html
<div class="filter-list-widget" [ngClass]="{'pills-layout': isPillsLayout, 'vertical-layout': !isPillsLayout}">
  
  <!-- 垂直布局（原有逻辑） -->
  <ng-container *ngIf="!isPillsLayout">
    <div *ngFor="let filter of filters" class="filter-item">
      <label class="filter-label">{{ filter.label }}</label>
      
      <!-- 关键词搜索 -->
      <input
        *ngIf="filter.componentType === 'keyword'"
        nz-input
        [(ngModel)]="filter.value"
        (ngModelChange)="onFilterChange()"
        placeholder="请输入关键词"
      />
      
      <!-- 单选下拉 -->
      <nz-select
        *ngIf="filter.componentType === 'single-select'"
        [(ngModel)]="filter.value"
        (ngModelChange)="onFilterChange()"
        nzAllowClear
        nzPlaceHolder="请选择"
      >
        <nz-option
          *ngFor="let option of filter.options"
          [nzValue]="option.val"
          [nzLabel]="option.label"
        ></nz-option>
      </nz-select>
      
      <!-- 多选下拉 -->
      <nz-select
        *ngIf="filter.componentType === 'multi-select'"
        [(ngModel)]="filter.value"
        (ngModelChange)="onFilterChange()"
        nzMode="multiple"
        nzAllowClear
        nzPlaceHolder="请选择"
      >
        <nz-option
          *ngFor="let option of filter.options"
          [nzValue]="option.val"
          [nzLabel]="option.label"
        ></nz-option>
      </nz-select>
      
      <!-- 日期选择 -->
      <nz-date-picker
        *ngIf="filter.componentType === 'date-picker'"
        [(ngModel)]="filter.value"
        (ngModelChange)="onFilterChange()"
        nzAllowClear
        nzPlaceHolder="请选择日期"
      ></nz-date-picker>
    </div>
  </ng-container>
  
  <!-- Pills 布局（新增） -->
  <ng-container *ngIf="isPillsLayout">
    <div class="filter-pills-container">
      <nz-tag
        *ngFor="let filter of visibleFilters"
        class="filter-pill"
        nzMode="default"
        [nz-popover]="popoverContent"
        nzPopoverTrigger="click"
        nzPopoverPlacement="bottomLeft"
        [nzPopoverTitle]="filter.label"
      >
        <span class="pill-label">{{ filter.label }}:</span>
        <span class="pill-value">{{ getFilterDisplayValue(filter) }}</span>
      </nz-tag>
      
      <!-- Popover 内容模板 -->
      <ng-template #popoverContent let-filter="filter">
        <div class="pill-editor" style="min-width: 200px;">
          <!-- 关键词搜索 -->
          <input
            *ngIf="filter.componentType === 'keyword'"
            nz-input
            [(ngModel)]="filter.value"
            (ngModelChange)="onFilterChange()"
            placeholder="请输入关键词"
          />
          
          <!-- 单选下拉 -->
          <nz-select
            *ngIf="filter.componentType === 'single-select'"
            [(ngModel)]="filter.value"
            (ngModelChange)="onFilterChange()"
            nzAllowClear
            nzPlaceHolder="请选择"
            style="width: 100%;"
          >
            <nz-option
              *ngFor="let option of filter.options"
              [nzValue]="option.val"
              [nzLabel]="option.label"
            ></nz-option>
          </nz-select>
          
          <!-- 多选下拉 -->
          <nz-select
            *ngIf="filter.componentType === 'multi-select'"
            [(ngModel)]="filter.value"
            (ngModelChange)="onFilterChange()"
            nzMode="multiple"
            nzAllowClear
            nzPlaceHolder="请选择"
            style="width: 100%;"
          >
            <nz-option
              *ngFor="let option of filter.options"
              [nzValue]="option.val"
              [nzLabel]="option.label"
            ></nz-option>
          </nz-select>
          
          <!-- 日期选择 -->
          <nz-date-picker
            *ngIf="filter.componentType === 'date-picker'"
            [(ngModel)]="filter.value"
            (ngModelChange)="onFilterChange()"
            nzAllowClear
            nzPlaceHolder="请选择日期"
            style="width: 100%;"
          ></nz-date-picker>
        </div>
      </ng-template>
    </div>
  </ng-container>
  
</div>
```

**关键点**：
- `[ngClass]` 根据 `isPillsLayout` 动态切换样式类
- Pills 模式迭代 `visibleFilters`（自动过滤空值）
- Popover 内的表单控件与垂直布局**完全相同**（复用逻辑）
- `nz-popover` 指令直接写在 `nz-tag` 上，点击 tag 触发

**已知问题**（需在 1.2.3 修复）：
- `ng-template` 内无法直接访问外层 `*ngFor` 的 `filter` 变量，需传递

#### 文件 3：`filter-list.component.scss`（新增 Pills 样式）

**追加内容**（不删除现有样式）：
```scss
.filter-list-widget {
  
  // 垂直布局样式（保持原有）
  &.vertical-layout {
    .filter-item {
      margin-bottom: 16px;
      
      .filter-label {
        display: block;
        margin-bottom: 8px;
        font-weight: 500;
        color: rgba(0, 0, 0, 0.85);
      }
    }
  }
  
  // Pills 布局样式（新增）
  &.pills-layout {
    .filter-pills-container {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      
      .filter-pill {
        cursor: pointer;
        padding: 4px 12px;
        font-size: 14px;
        border-radius: 16px;
        transition: all 0.3s;
        
        &:hover {
          background-color: #e6f7ff;
          border-color: #1890ff;
        }
        
        .pill-label {
          font-weight: 500;
          margin-right: 4px;
        }
        
        .pill-value {
          color: rgba(0, 0, 0, 0.65);
        }
      }
    }
    
    .pill-editor {
      padding: 8px 0;
    }
  }
}
```

---

### 1.3 验证步骤

#### 后端验证
```bash
cd /Users/mozhenghua/j2ee_solution/project/plugins/tis-ontology-plugin
mvn clean compile -Dmaven.test.skip=true -o
```

#### 前端验证
```bash
cd /Users/mozhenghua/j2ee_solution/project/tis-console
ng build --configuration development
```

**预期无报错**。

#### 功能验证清单
- [ ] 配置面板显示 "布局模式" 下拉框（垂直布局 / Pills布局）
- [ ] 默认选中 "垂直布局"
- [ ] 切换到 Pills 后，过滤项横向排列成 tag
- [ ] 空值的过滤项在 Pills 模式下不显示
- [ ] 点击 pill 弹出 popover，内含输入控件
- [ ] 在 popover 里修改值后，pill 上的显示值同步更新
- [ ] 切换回垂直布局，所有过滤项恢复显示

---

## 二、P0-3：用户动态增删过滤项（工作量：1.5 天）

### 2.1 后端改动（1 个文件）

#### 文件 1：`FilterListWidget.java`（新增字段）

**位置**：第 93 行之后（`layout` 字段之后）

**新增代码**：
```java
    /**
     * 是否允许最终用户动态增删过滤项。
     * 
     * <p>false（默认）：过滤项列表固定，只有模块构建者能在配置面板改；<br>
     * true：渲染 "+ Add filter" 按钮，最终用户可临时增删过滤项（不写回配置）。
     */
    @FormField(type = FormFieldType.INPUTTEXT, ordinal = 23, validate = {Validator.require})
    public Boolean allowUserModify;
```

**注意事项**：
- 类型是 `Boolean`（包装类型），允许 `null`（老数据兼容）
- `ordinal = 23` 排在所有字段最后
- 前端需处理 `null` → 视为 `false`

#### 更新 `FilterListWidget.json`

**追加字段**：
```json
{
  "allowUserModify": {
    "help": "允许最终用户在运行时临时增删过滤项（不会保存到配置）",
    "dftVal": "false"
  }
}
```

---

### 2.2 前端改动（2 个文件）

#### 文件 1：`filter-list.component.ts`（新增增删逻辑）

**步骤 2.2.1**：新增字段和 getter

**位置**：`visibleFilters` getter 之后

**新增代码**：
```typescript
  /** 是否允许用户增删过滤项 */
  get allowUserModify(): boolean {
    return this.config?.allowUserModify === true;
  }

  /** 可添加的属性候选（全部属性 - 已有过滤项） */
  availableProperties: Array<{label: string, val: string}> = [];
```

**步骤 2.2.2**：在 `ngOnInit` 加载候选属性

**位置**：`ngOnInit` 方法最后

**新增代码**：
```typescript
    // 如果允许用户增删，加载全部属性候选
    if (this.allowUserModify) {
      this.loadAvailableProperties();
    }
```

**步骤 2.2.3**：新增方法：加载候选属性

**位置**：组件类最后

**新增代码**：
```typescript
  /**
   * 加载可添加的属性候选列表
   */
  private async loadAvailableProperties(): Promise<void> {
    if (!this.config.objectSetVar) {
      this.availableProperties = [];
      return;
    }

    try {
      // 调用后端获取对象集的全部属性
      const result = await this.workshopModuleApiService.getObjectProperties(
        this.config.objectSetVar
      );
      
      // 过滤掉已配置的属性
      const existingProps = new Set(this.filters.map(f => f.property));
      this.availableProperties = result.filter(prop => !existingProps.has(prop.val));
    } catch (error) {
      console.error('Failed to load available properties:', error);
      this.availableProperties = [];
    }
  }

  /**
   * 用户点击 "+ Add filter"
   */
  onAddFilter(propertyId: string): void {
    const property = this.availableProperties.find(p => p.val === propertyId);
    if (!property) {
      return;
    }

    // 创建新的过滤项（默认关键词组件）
    const newFilter: FilterState = {
      property: property.val,
      label: property.label,
      componentType: 'keyword',
      value: undefined,
      options: []
    };

    this.filters.push(newFilter);
    
    // 从候选列表移除
    this.availableProperties = this.availableProperties.filter(p => p.val !== propertyId);
    
    // 不触发 onFilterChange()，因为新项的 value 是 undefined（空值不写出）
  }

  /**
   * 用户点击 "×" 删除过滤项
   */
  onRemoveFilter(index: number): void {
    const removed = this.filters.splice(index, 1)[0];
    
    // 加回候选列表
    this.availableProperties.push({
      label: removed.label,
      val: removed.property
    });
    
    // 触发变更（已有值的项被删除，需要更新输出）
    this.onFilterChange();
  }
```

**依赖说明**：
- `workshopModuleApiService.getObjectProperties()` 方法需要新增（见 2.2.4）
- `FilterState` 接口需确保有 `property` 字段（已有）

**步骤 2.2.4**：在 `WorkshopModuleApiService` 新增方法

**文件路径**：`/Users/mozhenghua/j2ee_solution/project/tis-console/src/workshop/services/api/workshop-module-api.service.ts`

**新增方法**：
```typescript
  /**
   * 获取对象集的全部属性列表（供 Filter List 动态增删使用）
   */
  async getObjectProperties(objectSetVarName: string): Promise<Array<{label: string, val: string}>> {
    const params = {
      action: 'plugin_action',
      emethod: 'get_object_properties',
      objectSetVar: objectSetVarName
    };
    
    const response = await lastValueFrom(
      this.http.get<{success: boolean, bizresult: Array<{label: string, val: string}>}>(
        '/workshop/ontology',
        { params }
      )
    );
    
    if (!response.success) {
      throw new Error('Failed to load object properties');
    }
    
    return response.bizresult || [];
  }
```

**后端对应端点**（已存在，无需改动）：
- `WidgetOptionHelper.getObjectPropertyOptions()` 已实现
- 前端参数 `emethod: 'get_object_properties'` 会路由到该方法

#### 文件 2：`filter-list.component.html`（新增增删按钮）

**修改 1：垂直布局增加删除按钮**

**位置**：垂直布局的 `.filter-item` 内部

**修改后**：
```html
    <div *ngFor="let filter of filters; let i = index" class="filter-item">
      <div class="filter-header">
        <label class="filter-label">{{ filter.label }}</label>
        <button
          *ngIf="allowUserModify"
          nz-button
          nzType="text"
          nzSize="small"
          nzDanger
          (click)="onRemoveFilter(i)"
          class="remove-btn"
        >
          <i nz-icon nzType="close"></i>
        </button>
      </div>
      
      <!-- 输入控件（保持不变）-->
      ...
    </div>
```

**修改 2：垂直布局末尾增加 Add 按钮**

**位置**：垂直布局 `</ng-container>` 之前

**新增代码**：
```html
    <!-- Add filter 按钮（垂直布局） -->
    <div *ngIf="allowUserModify && availableProperties.length > 0" class="add-filter-section">
      <nz-select
        nzPlaceHolder="+ Add filter"
        nzAllowClear
        (ngModelChange)="onAddFilter($event)"
        [ngModel]="null"
        style="width: 200px;"
      >
        <nz-option
          *ngFor="let prop of availableProperties"
          [nzValue]="prop.val"
          [nzLabel]="prop.label"
        ></nz-option>
      </nz-select>
    </div>
```

**修改 3：Pills 布局增加删除和 Add 按钮**

**位置**：Pills 容器内

**修改后**：
```html
    <div class="filter-pills-container">
      <nz-tag
        *ngFor="let filter of visibleFilters; let i = index"
        class="filter-pill"
        [nz-popover]="popoverContent"
        nzPopoverTrigger="click"
        nzPopoverPlacement="bottomLeft"
        [nzPopoverTitle]="filter.label"
        [nzCloseable]="allowUserModify"
        (nzOnClose)="onRemoveFilter(i)"
      >
        <span class="pill-label">{{ filter.label }}:</span>
        <span class="pill-value">{{ getFilterDisplayValue(filter) }}</span>
      </nz-tag>
      
      <!-- Add filter 按钮（Pills 布局） -->
      <nz-select
        *ngIf="allowUserModify && availableProperties.length > 0"
        nzPlaceHolder="+ Add filter"
        nzAllowClear
        (ngModelChange)="onAddFilter($event)"
        [ngModel]="null"
        class="add-filter-pill"
        nzSize="small"
      >
        <nz-option
          *ngFor="let prop of availableProperties"
          [nzValue]="prop.val"
          [nzLabel]="prop.label"
        ></nz-option>
      </nz-select>
      
      <!-- Popover 模板（保持不变）-->
      ...
    </div>
```

**关键点**：
- `[nzCloseable]="allowUserModify"` 让 tag 可关闭
- `(nzOnClose)` 事件绑定删除方法
- `[ngModel]="null"` 确保下拉框每次都是空状态（选中后立即触发 `ngModelChange` 并重置）

#### 更新样式（`filter-list.component.scss`）

**追加内容**：
```scss
  &.vertical-layout {
    .filter-item {
      .filter-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;
        
        .remove-btn {
          padding: 0;
          height: 22px;
          width: 22px;
        }
      }
    }
    
    .add-filter-section {
      margin-top: 16px;
    }
  }
  
  &.pills-layout {
    .add-filter-pill {
      width: 120px;
    }
  }
```

---

### 2.3 验证步骤

#### 功能验证清单
- [ ] 配置面板显示 "允许用户增删" 开关（默认关闭）
- [ ] 开关关闭时，前端无 "×" 按钮，无 "+ Add filter" 按钮
- [ ] 开关打开时，垂直布局每行右上角有 "×"
- [ ] 开关打开时，Pills 布局每个 tag 有关闭图标
- [ ] 点击 "×" 或关闭 tag，对应过滤项消失
- [ ] 删除后的属性重新出现在 "+ Add filter" 候选列表
- [ ] 选择候选属性后，新过滤项出现（默认关键词组件，值为空）
- [ ] 刷新页面，用户增删的项消失（恢复配置的列表）

---

## 三、P0-4：默认过滤值回填（工作量：0.5 天）

### 3.1 前端改动（1 个文件）

#### 文件 1：`filter-list.component.ts`（修改 `ngOnInit`）

**步骤 3.1.1**：在 `ngOnInit` 最后回填默认值

**位置**：`loadAvailableProperties()` 调用之后

**新增代码**：
```typescript
    // 回填默认过滤值（从 filterOutputVar 读取）
    await this.loadDefaultFilterValues();
```

**步骤 3.1.2**：新增方法：加载默认值

**位置**：组件类最后

**新增代码**：
```typescript
  /**
   * 从 filterOutputVar 读取默认过滤值并回填
   */
  private async loadDefaultFilterValues(): Promise<void> {
    if (!this.config.filterOutputVar) {
      return;
    }

    try {
      const defaultValue = await this.variableService.getVariableValue(
        this.config.filterOutputVar
      );
      
      if (!defaultValue || !Array.isArray(defaultValue.filters)) {
        return;
      }

      // 逐项回填
      this.filters.forEach(filter => {
        const match = defaultValue.filters.find(
          (df: any) => df.property === filter.property
        );
        if (match && match.value !== undefined) {
          filter.value = match.value;
        }
      });
      
    } catch (error) {
      console.error('Failed to load default filter values:', error);
    }
  }
```

**依赖说明**：
- `variableService.getVariableValue()` 方法已存在（用于读取变量当前值）
- 默认值格式：`{filters: [{property, operator, value}]}`（与 `onFilterChange()` 写出格式一致）

---

### 3.2 后端改动（无）

**说明**：
- `ObjectSetFilterVariable` 已支持 `defaultValue` 字段（变量定义时可配置）
- Filter List 只需消费该默认值，无需后端改动

---

### 3.3 验证步骤

#### 功能验证清单
- [ ] 在变量定义面板，为 `filterOutputVar` 设置默认值（如 `{filters: [{property: "status", operator: "equals", value: "Active"}]}`）
- [ ] 打开 module，Filter List 自动回填 "status" 过滤项的值为 "Active"
- [ ] 其他未设默认值的过滤项保持空
- [ ] 用户修改后，新值覆盖默认值（写回 `filterOutputVar`）
- [ ] 刷新页面，默认值重新生效

---

## 四、P0-1：直方图组件（工作量：2 天）

### 4.1 后端改动（4 个文件）

#### 文件 1：`HistogramComponent.java`（新建）

**路径**：`/Users/mozhenghua/j2ee_solution/project/plugins/tis-ontology-plugin/src/main/java/com/qlangtech/tis/plugin/ontology/workshop/widget/component/HistogramComponent.java`

**完整代码**：
```java
package com.qlangtech.tis.plugin.ontology.workshop.widget.component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;

import java.util.List;

/**
 * 直方图组件：数值属性分布可视化
 * 
 * <p>服务端聚合查询，返回 [{min, max, count}] 数组，前端用 ECharts 柱状图渲染。
 * 用户点击柱子筛选区间（between 操作符）。
 * 
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/11/4
 */
public class HistogramComponent extends FilterComponent {

    /**
     * 区间数量（桶数），默认 10
     */
    @FormField(type = FormFieldType.INT_NUMBER, ordinal = 10, validate = {Validator.require})
    public Integer bucketCount;

    /**
     * 最小值（可选，不填则自动推导）
     */
    @FormField(type = FormFieldType.INT_NUMBER, ordinal = 11)
    public Double minValue;

    /**
     * 最大值（可选，不填则自动推导）
     */
    @FormField(type = FormFieldType.INT_NUMBER, ordinal = 12)
    public Double maxValue;

    @Override
    protected JSONObject createPayloadProps() throws Exception {
        JSONObject payload = new JSONObject();
        
        // TODO: 实际实现需从 objectSetVar 查询数值属性的分布
        // 当前返回模拟数据
        JSONArray buckets = new JSONArray();
        int count = bucketCount != null ? bucketCount : 10;
        double min = minValue != null ? minValue : 0.0;
        double max = maxValue != null ? maxValue : 100.0;
        double step = (max - min) / count;
        
        for (int i = 0; i < count; i++) {
            JSONObject bucket = new JSONObject();
            bucket.put("min", min + i * step);
            bucket.put("max", min + (i + 1) * step);
            bucket.put("count", (int)(Math.random() * 100)); // 模拟计数
            buckets.add(bucket);
        }
        
        payload.put("buckets", buckets);
        return payload;
    }

    @TISExtension
    public static class DescriptorImpl extends BaseComponentDescriptor {
        
        public DescriptorImpl() {
            super();
        }

        @Override
        public String getDisplayName() {
            return "直方图（Histogram）";
        }
    }
}
```

**关键点**：
- `bucketCount` 默认 10，前端需在 `.json` 设置 `dftVal: "10"`
- `createPayloadProps()` 返回 `{buckets: [{min, max, count}]}`
- TODO 标记真实实现位置（需查询数据库聚合）

#### 文件 2：`HistogramComponent.json`（新建）

**路径**：`/Users/mozhenghua/j2ee_solution/project/plugins/tis-ontology-plugin/src/main/resources/com/qlangtech/tis/plugin/ontology/workshop/widget/component/HistogramComponent.json`

**完整内容**：
```json
{
  "bucketCount": {
    "help": "直方图的区间数量（桶数），建议 5-20 之间",
    "dftVal": "10"
  },
  "minValue": {
    "help": "最小值（可选，不填则根据数据自动推导）"
  },
  "maxValue": {
    "help": "最大值（可选，不填则根据数据自动推导）"
  }
}
```

#### 文件 3：`FilterItemConfig.java`（修改 descriptor，新增组件注册）

**位置**：`DescriptorImpl` 构造函数内

**新增代码**：
```java
            // 注册直方图组件（只对数值属性开放）
            this.addSelectableInstance(KEY_COMPONENT, HistogramComponent.class);
```

**关键点**：
- 与现有 4 个组件（Keyword/SingleSelect/MultiSelect/DatePicker）并列注册
- 前端候选项会自动多出 "直方图（Histogram）"

#### 文件 4：编译验证

```bash
cd /Users/mozhenghua/j2ee_solution/project/plugins/tis-ontology-plugin
mvn clean compile -Dmaven.test.skip=true -o
```

---

### 4.2 前端改动（3 个文件）

#### 文件 1：安装 ECharts 依赖

```bash
cd /Users/mozhenghua/j2ee_solution/project/tis-console
npm install echarts@5.4.0 --save
npm install ngx-echarts@16.0.0 --save
```

#### 文件 2：`app.module.ts`（注册 NgxEchartsModule）

**位置**：imports 数组

**新增代码**：
```typescript
import { NgxEchartsModule } from 'ngx-echarts';

@NgModule({
  imports: [
    // ... 现有 imports
    NgxEchartsModule.forRoot({
      echarts: () => import('echarts')
    })
  ]
})
```

#### 文件 3：`histogram-filter.component.ts`（新建）

**路径**：`/Users/mozhenghua/j2ee_solution/project/tis-console/src/workshop/components/widgets/filter-list/histogram-filter.component.ts`

**完整代码**：
```typescript
import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { EChartsOption } from 'echarts';

export interface HistogramBucket {
  min: number;
  max: number;
  count: number;
}

@Component({
  selector: 'app-histogram-filter',
  template: `
    <div echarts [options]="chartOptions" (chartClick)="onChartClick($event)" class="histogram-chart"></div>
  `,
  styles: [`
    .histogram-chart {
      width: 100%;
      height: 200px;
    }
  `]
})
export class HistogramFilterComponent implements OnInit {
  
  @Input() buckets: HistogramBucket[] = [];
  @Input() label: string = '';
  @Output() valueChange = new EventEmitter<{min: number, max: number}>();

  chartOptions: EChartsOption = {};

  ngOnInit(): void {
    this.updateChart();
  }

  ngOnChanges(): void {
    this.updateChart();
  }

  private updateChart(): void {
    if (!this.buckets || this.buckets.length === 0) {
      return;
    }

    this.chartOptions = {
      title: {
        text: this.label,
        left: 'center',
        textStyle: {
          fontSize: 14
        }
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        },
        formatter: (params: any) => {
          const data = params[0];
          const bucket = this.buckets[data.dataIndex];
          return `${bucket.min} ~ ${bucket.max}<br/>数量: ${bucket.count}`;
        }
      },
      xAxis: {
        type: 'category',
        data: this.buckets.map(b => `${b.min.toFixed(1)}`),
        axisLabel: {
          rotate: 45,
          fontSize: 10
        }
      },
      yAxis: {
        type: 'value',
        name: '数量'
      },
      series: [
        {
          type: 'bar',
          data: this.buckets.map(b => b.count),
          itemStyle: {
            color: '#1890ff'
          },
          emphasis: {
            itemStyle: {
              color: '#40a9ff'
            }
          }
        }
      ]
    };
  }

  onChartClick(event: any): void {
    const index = event.dataIndex;
    if (index !== undefined && this.buckets[index]) {
      const bucket = this.buckets[index];
      this.valueChange.emit({ min: bucket.min, max: bucket.max });
    }
  }
}
```

#### 文件 4：`filter-list.component.ts`（新增直方图渲染逻辑）

**步骤 4.2.1**：在 `FilterState` 接口新增字段

**位置**：接口定义处

**新增字段**：
```typescript
interface FilterState {
  // ... 现有字段
  buckets?: HistogramBucket[];  // 直方图桶数据
}
```

**步骤 4.2.2**：修改 `onFilterChange` 处理直方图值

**位置**：`onFilterChange` 方法内

**修改后**：
```typescript
  onFilterChange(): void {
    const filterConditions = this.filters
      .filter(f => !this.isEmptyFilterValue(f.value))
      .map(f => {
        if (f.componentType === 'histogram') {
          // 直方图返回 {min, max}，转成 between 操作符
          return {
            property: f.property,
            operator: 'between',
            value: [f.value.min, f.value.max]
          };
        }
        return {
          property: f.property,
          operator: this.getOperatorByComponent(f.componentType),
          value: f.value
        };
      });

    this.variableService.setVariableValue(
      this.config.filterOutputVar,
      { filters: filterConditions }
    );
  }

  private getOperatorByComponent(type: string): string {
    switch (type) {
      case 'keyword': return 'contains';
      case 'single-select': return 'equals';
      case 'multi-select': return 'in';
      case 'date-picker': return 'equals';
      case 'histogram': return 'between';
      default: return 'equals';
    }
  }
```

#### 文件 5：`filter-list.component.html`（新增直方图渲染）

**位置**：垂直布局和 Pills 布局的输入控件部分

**新增代码**（垂直布局）：
```html
      <!-- 直方图 -->
      <app-histogram-filter
        *ngIf="filter.componentType === 'histogram'"
        [buckets]="filter.buckets || []"
        [label]="filter.label"
        (valueChange)="onHistogramSelect(filter, $event)"
      ></app-histogram-filter>
```

**新增方法**（`filter-list.component.ts`）：
```typescript
  onHistogramSelect(filter: FilterState, value: {min: number, max: number}): void {
    filter.value = value;
    this.onFilterChange();
  }
```

---

### 4.3 验证步骤

#### 功能验证清单
- [ ] 配置面板，过滤项组件下拉多出 "直方图（Histogram）"
- [ ] 选择直方图后，显示 "区间数量" 输入框（默认 10）
- [ ] 前端渲染柱状图，横轴显示区间，纵轴显示计数
- [ ] 点击柱子，过滤值变为 `{min, max}`
- [ ] 输出变量收到 `{property, operator: 'between', value: [min, max]}`
- [ ] 切换到 Pills 布局，直方图仍正常显示（pill 上显示 "10~20"）

---

## 五、通用验证流程

### 5.1 后端编译验证

```bash
cd /Users/mozhenghua/j2ee_solution/project/plugins/tis-ontology-plugin
mvn clean compile -Dmaven.test.skip=true -o
```

**预期输出**：
```
[INFO] BUILD SUCCESS
```

### 5.2 前端编译验证

```bash
cd /Users/mozhenghua/j2ee_solution/project/tis-console
ng build --configuration development
```

**预期输出**：
```
✔ Browser application bundle generation complete.
```

**失败处理**：
- 模板错误：检查 `.html` 的 `*ngIf` / `*ngFor` 语法
- 类型错误：检查 `.ts` 的接口定义是否匹配
- 导入错误：检查 `import` 路径是否正确

### 5.3 运行时验证

```bash
cd /Users/mozhenghua/j2ee_solution/project/tis-solr
./tis-web-start/bin/start.sh
```

**访问**：`http://localhost:8080/workshop`

**验证步骤**：
1. 新建 Workshop Module
2. 新建 Filter List Widget
3. 配置 `objectSetVar`（选择一个对象集变量）
4. 配置 `layout`（切换 Pills / Vertical）
5. 配置 `allowUserModify`（开关）
6. 添加过滤项，选择组件类型
7. 保存后刷新，检查默认值是否回填
8. 运行时增删过滤项，检查候选列表是否更新

---

## 六、错误排查手册

### 6.1 后端常见错误

| 错误信息 | 原因 | 解决方案 |
|---|---|---|
| `cannot find symbol: LayoutMode` | 枚举未定义在正确位置 | 确保枚举是 `FilterListWidget` 的内部类 |
| `ordinal X conflicts with ...` | ordinal 重复 | 检查所有 `@FormField` 的 ordinal 不重复 |
| `UnknownFieldException: layout` | `.json` 缺少字段定义 | 在 `.json` 添加对应字段配置 |
| `validate() failed: objectSetVar is required` | 必填字段为 null | 检查前端是否传递了该字段 |

### 6.2 前端常见错误

| 错误信息 | 原因 | 解决方案 |
|---|---|---|
| `Cannot read property 'layout' of undefined` | config 未初始化 | 在 `ngOnInit` 检查 `this.config` 是否有值 |
| `NG0303: Can't bind to 'ngModel'` | FormsModule 未导入 | 在模块的 imports 添加 `FormsModule` |
| `NullInjectorError: No provider for WorkshopModuleApiService` | 服务未注入 | 在组件构造函数注入服务 |
| `Property 'isPillsLayout' does not exist` | getter 拼写错误 | 检查 getter 名称是否与模板一致 |

### 6.3 集成测试失败

| 现象 | 原因 | 解决方案 |
|---|---|---|
| 配置面板不显示 layout 字段 | 后端 `.json` 未生效 | 检查文件路径、Maven 编译是否包含资源文件 |
| Pills 布局切换后仍是垂直 | 前端 `isPillsLayout` 判断错误 | 检查 `config.layout === 'PILLS'`（大写） |
| 候选项为空 | `getObjectProperties` 调用失败 | 打开浏览器 Network 面板，检查 API 响应 |
| 默认值未回填 | `filterOutputVar` 无初始值 | 在变量定义面板设置 defaultValue |

---

## 七、后续扩展方向（P1/P2）

### 7.1 分布图（P1-6）

- 新建 `DistributionChartComponent`（饼图）
- ECharts 配置：`type: 'pie'`
- 点击扇区写 `{operator: 'equals', value: clickedValue}`

### 7.2 时间线（P1-7）

- 新建 `TimelineComponent`（折线图 + dataZoom）
- 后端聚合按天/周/月
- 拖选区间写 `{operator: 'between', value: [start, end]}`

### 7.3 关联对象过滤（P0-5）

**改动量最大，需单独设计文档**，涉及：
- `FilterItemConfig` 多态化（DirectProperty / LinkedProperty）
- 前端级联选择器（link → linkedProperty）
- 后端 JOIN 查询抽象层
- Neo4j 关系遍历支持

---

## 八、交付清单

### 8.1 P0-2（Pills 布局）

- [x] `FilterListWidget.java`：新增 `layout` 字段 + `LayoutMode` 枚举
- [x] `FilterListWidget.json`：配置 `layout` 枚举选项
- [x] `filter-list.component.ts`：新增 `isPillsLayout` / `visibleFilters` getter
- [x] `filter-list.component.html`：Pills 布局模板（nz-tag + popover）
- [x] `filter-list.component.scss`：Pills 样式

### 8.2 P0-3（用户增删）

- [x] `FilterListWidget.java`：新增 `allowUserModify` 字段
- [x] `FilterListWidget.json`：配置 `allowUserModify` help
- [x] `filter-list.component.ts`：新增 `onAddFilter` / `onRemoveFilter` 方法
- [x] `filter-list.component.html`：新增 "+ Add filter" 按钮 + 删除按钮
- [x] `WorkshopModuleApiService`：新增 `getObjectProperties` 方法

### 8.3 P0-4（默认值）

- [x] `filter-list.component.ts`：新增 `loadDefaultFilterValues` 方法

### 8.4 P0-1（直方图）

- [x] `HistogramComponent.java`：新建组件类
- [x] `HistogramComponent.json`：配置字段 help
- [x] `FilterItemConfig.java`：注册 HistogramComponent
- [x] `histogram-filter.component.ts`：新建前端组件
- [x] `filter-list.component.ts`：集成直方图渲染
- [x] `package.json`：新增 echarts + ngx-echarts 依赖

---

## 九、时间估算

| 任务 | 工作量 | 累计工时 |
|---|---|---|
| P0-2 Pills 布局 | 1 天 | 1 天 |
| P0-3 用户增删 | 1.5 天 | 2.5 天 |
| P0-4 默认值 | 0.5 天 | 3 天 |
| P0-1 直方图 | 2 天 | 5 天 |
| 集成测试与修复 | 1 天 | 6 天 |

**总计**：6 个工作日（约 1.2 周）

---

## 十、DeepSeek 执行建议

### 10.1 执行顺序（严格按序）

1. **P0-2 Pills 布局**（独立功能，最小改动）
   - 先改后端（Java + JSON）
   - 再改前端（TS + HTML + SCSS）
   - 编译验证 → 功能验证

2. **P0-3 用户增删**（依赖 P0-2 的 Pills UI）
   - 后端新增字段
   - 前端新增增删逻辑
   - 集成 API 服务

3. **P0-4 默认值**（最简单，只改前端）
   - 单方法新增，无依赖冲突

4. **P0-1 直方图**（独立组件，改动最大）
   - 后端新建组件类
   - 前端新建子组件
   - 集成 ECharts

### 10.2 每步检查点

**后端改动后**：
```bash
mvn clean compile -Dmaven.test.skip=true -o
# 必须 BUILD SUCCESS
```

**前端改动后**：
```bash
ng build --configuration development
# 必须无错误
```

**功能验证**：
- 打开浏览器开发者工具（F12）
- Network 面板检查 API 请求
- Console 面板检查 JS 错误
- 逐项勾选验收清单

### 10.3 遇到错误时

1. **立即停止**，不要继续改其他文件
2. 复制完整错误信息（堆栈跟踪）
3. 对照「错误排查手册」定位原因
4. 修复后重新编译验证
5. 确认无误后继续下一步

---

**文档结束**

---

## 十一、实施记录（实际落地，2026-11-04）

> 本节是在**按本指南实施完成之后**追加的。指南正文里存在若干与仓库实况不符的描述，
> 实施时一律以仓库为准。下面是真实落地清单与逐条勘误 —— 后人在读正文前请先读这一节。

### 11.1 真实落地清单

| 优先级 | 文件 | 内容 |
|---|---|---|
| P0-2 | `plugins/tis-ontology-plugin/.../widget/impl/FilterListWidget.java` | `LayoutMode {vertical, pills}` 枚举 + `layout` 字段（ordinal 19） |
| P0-2 | `plugins/tis-ontology-plugin/.../widget/impl/FilterListWidget.json` | `layout.dftVal = "vertical"`（**真枚举常量名**，不是大写） |
| P0-2 | `tis-console/src/workshop/models/widgets/filter-list.config.ts` | `FilterListLayout` 类型 + `toTokenOf` 归一 + `FilterListWidget.layout` |
| P0-2 | `tis-console/src/workshop/components/widgets/filter-list/filter-list.component.ts` | `isPillsLayout` / `isEditing` / `onPillClick` / `filterDisplayValue` + `nz-tag` 胶囊 + **内联编辑区** |
| P0-3 | `FilterListWidget.java` | `allowUserModify`（ordinal 23）+ `HTTP_METHOD_GET_PROPERTY_OPTIONS` + `httpProcess` 三分派 |
| P0-3 | `filter-list.component.ts` | `loadAvailableProperties` / `onAddFilterByProperty` / `onRemoveFilter` + 工具栏 `nz-select` |
| P0-4 | `filter-list.component.ts` | `applyCachedFilterValues`（同步首帧）+ `loadDefaultFilterValues`（异步兜底）+ `applyFilterValues` |
| P0-1 | `.../widget/component/HistogramComponent.java` + `.json` | 新控件形态，字段 `bucketCount`（1–100，默认 10） |
| P0-1 | `.../widget/impl/filterlist/HistogramDataProcessor.java` | 两趟聚合分桶（MIN/MAX 定宽 → `FLOOR(col/width)` 分组计数） |
| P0-1 | `.../impl/objtype/DataSourceBinding.java` | 允许**无分组键**的标量聚合（第一趟 MIN/MAX 需要它） |
| P0-1 | `.../impl/objtype/aggregation/MySqlAggregationSqlBuilder.java` | `GroupKey.numeric` → `FLOOR(col / width)` |
| P0-1 | `filter-list.component.ts` + `widget-config.model.ts` | `FilterComponentKind` 加 `'histogram'`、`canvas baseChart` 渲染、点击 → 一对谓词 |

### 11.2 指南正文勘误（按影响排序）

1. **没有 `filter-list.component.html` / `.scss`** —— 该组件是单文件内联模板 + 内联 `styles`，
   所有模板改动都落在 `.ts` 里。
2. **图表库不是 ECharts** —— 仓库用 `chart.js 3.9.1` + `ng2-charts 6.0.0`，
   `provideCharts(withDefaultRegisterables())` 已在 `workshop.module.ts` 里注册。
   **不要**新增 `echarts` / `ngx-echarts` 依赖（交付清单 8.4 最后一条是错的）。
3. **`VariableService` 没有 `getVariableValue()` / `setVariableValue()`** ——
   真实 API 是 `getValue(nameOrId)`（未注册的变量**同步 throw**）、`getCachedValue(nameOrId)`。
4. **`WorkshopModuleApiService` 没有 `getObjectProperties()`** —— 属性候选走
   `descriptor.applyHttpRequest(..., 'get_property_options', postBody, {objectSetVar})`，
   端点名是 `get_property_options`（不是 `get_object_properties`），也没有 `/workshop/ontology` 这个 URL。
5. **`ObjectSetFilterVariable` 没有任何字段** —— 不存在 `defaultValue`。默认值来自变量**定义**
   （`StaticConfig.value`，TEXTAREA 字符串），在运行时通过 `getValue()` 读取。
6. **`nz-tag` 没有 `[nzCloseable]` 输入** —— 关闭是 `nzMode="closeable"` + `(nzOnClose)`；
   本次实现用 `<i nz-icon nzType="close" (click)="...; $event.stopPropagation()">` 自绘，
   避免整枚胶囊的点击（展开编辑）与关闭按钮打架。
7. **`nz-popover` 的内容不携带行上下文**（经 `*nzStringTemplateOutlet` 渲染，没有 context），
   故 Pills 用的是**内联编辑区**，不是 popover。理由写在模板注释里。
8. **`OntologyFilterOperator` 没有 `between`** —— 区间只能是两条「与」关系的谓词
   （`>= lower`，非末桶再加 `< upper`）。
9. **不存在 `FilterState` / `componentType` / `histogram-filter.component.ts`** ——
   归一后的行类型是 `FilterRow`（`WorkshopFilterItem` + `value` + `histogram`），
   控件形态键名是 kebab 的 `component`，直方图直接并进 `filter-list.component.ts` 的 `ngSwitch`。
10. **`LayoutMode` 不能用 `.json` 的 `enum` 数组 + `getLabel()`** ——
    枚举走 `DescriptorUseableShortComment#shortComment()` 派生 help，`.json` 里只写
    `dftVal`（值 = **常量名** `"vertical"`）。
11. **`allowUserModify` 不是 `INPUTTEXT`/`"false"` 字符串** —— 是 `Boolean` + `FormFieldType.ENUM`，
    `.json` 里 `"dftVal": false`（真布尔）。
12. **不能覆写 `createPayloadProps()`**（`final`）—— 直方图不做 `AppendPayloadAttribute`，
    而是走独立的 `get_histogram` 端点。
13. **`FilterItemConfig.json` 在 `widget/` 下**（不是 `widget/impl/`）。
14. **不需要「注册 HistogramComponent」** —— `FilterComponent` 是多态 `Describable`，
    `@TISExtension` 的 `DefaultDescriptor` 会被 sezpoj 自动发现，选择器自动多出一项。
15. **`minValue`/`maxValue` 不是 `INT_NUMBER` 的 `Double`** —— `HistogramComponent` 只有
    `bucketCount` 一个字段；**区间边界由数据决定**，不让构建者手填。
16. **`ngOnInit` 里不能 `await`** —— P0-4 拆成「`ngOnInit` 同步读缓存」+「`initialize()` 异步兜底」
    两步；`getValue()` 的**同步 throw** 必须单独 try/catch 包住调用本身。
17. **Pills 不能过滤掉空值的行**（指南的 `visibleFilters`）—— 过滤掉之后用户就没有入口去
    设置一个还没设过的条件。实现里空值是灰字「未设置」。
18. **`BaseComponentDescriptor` 不存在** —— 抽象基类是 `FilterComponent.BasicDescriptor`。

### 11.3 关键实现约束（不在指南里，但改这块代码必须知道）

- `initialize()` **不得抛出**；三个 `loadXxx` 的契约都是「永不抛出、失败保留上一帧」。
- Chart.js 的 `data` / `options` **必须按行新建**（内部元数据会挂回对象上），
  共享常量会让多张图互相串，也会让 `ngOnChanges` 的引用判等失效。
- `filter-list` 是 `WidgetRenderCacheService` 的使用者之一：快照载荷是
  `{options, histograms, availableProperties}` **一条**，因为缓存服务每个 widget 只有一条条目。
- 运行时增删（P0-3）改完 `availableProperties` 后要**立刻 `writeRenderSnapshot()`**，
  否则下一次击键重建会从旧快照里把刚删掉的候选项恢复回来（表现为「删不掉」）。
