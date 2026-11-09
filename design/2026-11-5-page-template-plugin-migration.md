# 页面模板从枚举到插件类的改造方案

## 改造动机

原 `PageTemplate` 是固定的 8 个枚举值（BLANK / DETAILS / INBOX / TWO_COLUMN / GRID / OVERVIEW / SETTINGS / THREE_COLUMN），经实践发现：

1. **BLANK / DETAILS / INBOX / TWO_COLUMN 本质相同**：都是左右两栏，唯一区别是左栏宽度（30% / 260px / 340px / 50%）
2. **OVERVIEW / SETTINGS 也类似**：都是两栏 + 首项通栏，只是比例不同
3. **无法支持用户自定义宽度**：想要"左侧占 25%"只能加新枚举，扩展性差

改为插件类后，用户可以：
- 配置任意左栏宽度（百分比或像素）
- 配置顶栏高度（新增上下布局）
- 统一 4 个两栏变体为一个可配置的模板

---

## 后端改造

### 1. 新建抽象基类 `PageTemplate.java`

**路径**：`plugins/tis-ontology-plugin/src/main/java/.../model/page/PageTemplate.java`

```java
public abstract class PageTemplate implements Describable<PageTemplate>, Serializable {
    /**
     * 返回 CSS grid-template-columns 的值
     * 示例："30% minmax(0,1fr)" / "260px minmax(0,1fr)" / "repeat(auto-fit, minmax(280px,1fr))"
     */
    public abstract String getGridTemplateColumns();

    /**
     * 返回 CSS grid-template-rows 的值（上下布局专用）
     * 示例："120px minmax(0,1fr)" / "auto minmax(0,1fr)"
     */
    public String getGridTemplateRows() { return null; }

    /**
     * 是否让首个子 Section 通栏
     */
    public boolean isFirstChildSpanAll() { return false; }

    /**
     * 建页时铺设的初始 Section 数量
     */
    public int getInitialSectionCount() { return 2; }

    /**
     * 窄屏（≤768px）下是否回落单栏
     */
    public boolean collapseOnNarrowScreen() { return true; }
}
```

### 2. 创建具体实现类

#### (1) `TwoColumnPageTemplate.java` - 两栏布局

**合并了原 BLANK / DETAILS / INBOX / TWO_COLUMN**

```java
public class TwoColumnPageTemplate extends PageTemplate {
    public enum WidthUnit { PERCENT, PX }

    @FormField(ordinal = 0, type = FormFieldType.INT_NUMBER, validate = {Validator.require})
    public Integer leftColumnWidth = 30;

    @FormField(ordinal = 1, type = FormFieldType.ENUM, validate = {Validator.require})
    public WidthUnit leftColumnUnit = WidthUnit.PERCENT;

    @Override
    public String getGridTemplateColumns() {
        String leftWidth = leftColumnUnit == WidthUnit.PERCENT
                ? leftColumnWidth + "%"
                : leftColumnWidth + "px";
        return leftWidth + " minmax(0,1fr)";
    }
}
```

**配置示例**：
- 空白页（BLANK）：`leftColumnWidth=30, leftColumnUnit=PERCENT`
- 详情布局（DETAILS）：`leftColumnWidth=260, leftColumnUnit=PX`
- 收件箱（INBOX）：`leftColumnWidth=340, leftColumnUnit=PX`
- 等宽两栏（TWO_COLUMN）：`leftColumnWidth=50, leftColumnUnit=PERCENT`

#### (2) `HeaderContentPageTemplate.java` - 上下布局（新增）

```java
public class HeaderContentPageTemplate extends PageTemplate {
    public enum HeightUnit { PX, AUTO }

    @FormField(ordinal = 0, type = FormFieldType.INT_NUMBER, validate = {Validator.require})
    public Integer headerHeight = 80;

    @FormField(ordinal = 1, type = FormFieldType.ENUM, validate = {Validator.require})
    public HeightUnit headerHeightUnit = HeightUnit.PX;

    @Override
    public String getGridTemplateColumns() { return null; } // 单列

    @Override
    public String getGridTemplateRows() {
        String topHeight = headerHeightUnit == HeightUnit.AUTO
                ? "auto"
                : headerHeight + "px";
        return topHeight + " minmax(0,1fr)";
    }

    @Override
    public boolean collapseOnNarrowScreen() { return false; } // 上下布局窄屏仍合理
}
```

**使用场景**：
- 固定顶栏：页面标题 + 全局操作按钮
- 筛选 + 列表：顶部筛选器占固定高度，下方列表撑满

#### (3) `GridPageTemplate.java` - 网格布局

```java
public class GridPageTemplate extends PageTemplate {
    @FormField(ordinal = 0, type = FormFieldType.INT_NUMBER, validate = {Validator.require})
    public Integer minColumnWidth = 280;

    @FormField(ordinal = 1, type = FormFieldType.ENUM, validate = {Validator.require})
    public Boolean firstChildSpanAll = true;

    @Override
    public String getGridTemplateColumns() {
        return "repeat(auto-fit, minmax(" + minColumnWidth + "px, 1fr))";
    }

    @Override
    public boolean isFirstChildSpanAll() { return firstChildSpanAll; }

    @Override
    public int getInitialSectionCount() { return 5; } // 筛选器 + 4 个网格块

    @Override
    public boolean collapseOnNarrowScreen() { return false; } // 网格本身响应式
}
```

#### (4) `ThreeColumnPageTemplate.java` - 三栏布局

```java
public class ThreeColumnPageTemplate extends PageTemplate {
    @Override
    public String getGridTemplateColumns() {
        return "repeat(3, minmax(0, 1fr))";
    }

    @Override
    public int getInitialSectionCount() { return 3; }
}
```

### 3. 迁移工具 `PageTemplateMigration.java`

处理旧枚举值到新插件实例的转换：

```java
public class PageTemplateMigration {
    public static PageTemplate migrateFromLegacyEnum(String legacyEnumName) {
        switch (legacyEnumName.toLowerCase()) {
            case "blank":
                return createBlankTemplate(); // leftColumnWidth=30, unit=PERCENT
            case "details":
                return createDetailsTemplate(); // leftColumnWidth=260, unit=PX
            case "inbox":
                return createInboxTemplate(); // leftColumnWidth=340, unit=PX
            case "two_column":
                return createTwoColumnTemplate(); // leftColumnWidth=50, unit=PERCENT
            case "grid":
                return createGridTemplate(); // minColumnWidth=280, firstChildSpanAll=true
            case "overview":
                return createOverviewTemplate(); // leftColumnWidth=50, unit=PERCENT（退化，丢失首项通栏）
            case "settings":
                return createSettingsTemplate(); // leftColumnWidth=33, unit=PERCENT（退化，丢失首项通栏）
            case "three_column":
                return createThreeColumnTemplate();
            default:
                return null;
        }
    }
}
```

### 4. 修改 `WorkshopPage.java`

```java
/**
 * 页面布局模板：从枚举改为插件类，支持可配置的栏宽/栏高
 */
@FormField(ordinal = 3, type = FormFieldType.SELECTABLE, validate = {Validator.require})
public PageTemplate template; // 不再提供默认值
```

### 5. 读盘时的兜底转换（需在相关加载代码中添加）

在 `WorkshopPageStore` 或 `WorkshopModule.load` 中：

```java
// 伪代码示例
Object templateField = readFromDisk();
if (templateField instanceof String) {
    page.template = PageTemplateMigration.migrateFromLegacyEnum((String) templateField);
} else {
    page.template = deserializeAsPlugin(templateField);
}
```

---

## 前端改造

### 1. 更新模型 `page.model.ts`

#### (1) 定义插件类型

```typescript
/**
 * 页面模板插件实例
 */
export interface PageTemplatePlugin {
  impl: PageTemplateImpl;
  vals: AttrValMap;
}

export const PAGE_TEMPLATE_IMPLS = [
  'two-column-page-template',
  'header-content-page-template',
  'grid-page-template',
  'three-column-page-template',
] as const;

export type PageTemplateImpl = typeof PAGE_TEMPLATE_IMPLS[number];
```

#### (2) 旧枚举迁移函数

```typescript
export type LegacyPageTemplateEnum = 'blank' | 'details' | 'grid' | 'inbox' | 'overview' | 'settings' | 'two_column' | 'three_column';

function migrateFromLegacyEnum(legacyEnum: LegacyPageTemplateEnum): PageTemplatePlugin {
  switch (legacyEnum) {
    case 'blank':
      return { impl: 'two-column-page-template', vals: { leftColumnWidth: 30, leftColumnUnit: 'PERCENT' } };
    case 'details':
      return { impl: 'two-column-page-template', vals: { leftColumnWidth: 260, leftColumnUnit: 'PX' } };
    // ... 其他情况
  }
}

export function resolvePageTemplate(raw: any): PageTemplatePlugin | null {
  if (!raw) return null;
  if (typeof raw === 'object' && raw.impl) return raw;
  if (typeof raw === 'string') return migrateFromLegacyEnum(raw as LegacyPageTemplateEnum);
  // 兜底
  return { impl: 'two-column-page-template', vals: { leftColumnWidth: 30, leftColumnUnit: 'PERCENT' } };
}
```

#### (3) CSS 样式提取函数

```typescript
export function extractPageLayoutStyle(template: PageTemplatePlugin | null): {
  gridTemplateColumns?: string;
  gridTemplateRows?: string;
} {
  if (!template) {
    return { gridTemplateColumns: '30% minmax(0,1fr)' }; // 兜底
  }

  const { impl, vals } = template;

  switch (impl) {
    case 'two-column-page-template': {
      const width = vals.leftColumnWidth ?? 30;
      const unit = vals.leftColumnUnit ?? 'PERCENT';
      const leftWidth = unit === 'PERCENT' ? `${width}%` : `${width}px`;
      return { gridTemplateColumns: `${leftWidth} minmax(0,1fr)` };
    }

    case 'header-content-page-template': {
      const height = vals.headerHeight ?? 80;
      const unit = vals.headerHeightUnit ?? 'PX';
      const topHeight = unit === 'AUTO' ? 'auto' : `${height}px`;
      return { gridTemplateRows: `${topHeight} minmax(0,1fr)` };
    }

    case 'grid-page-template': {
      const minWidth = vals.minColumnWidth ?? 280;
      return { gridTemplateColumns: `repeat(auto-fit, minmax(${minWidth}px, 1fr))` };
    }

    case 'three-column-page-template':
      return { gridTemplateColumns: 'repeat(3, minmax(0, 1fr))' };
  }
}
```

### 2. 修改 `workshop-page.component.ts`

#### (1) 替换 `pageLayoutClass()` 为 `pageLayoutStyle()`

```typescript
// 旧代码
pageLayoutClass(page: WorkshopPage): string {
  return `page-template-${page.template || 'blank'}`;
}

// 新代码
pageLayoutStyle(page: WorkshopPage): any {
  const template = resolvePageTemplate(page.template);
  const style = extractPageLayoutStyle(template);
  
  return {
    'grid-template-columns': style.gridTemplateColumns || null,
    'grid-template-rows': style.gridTemplateRows || null,
  };
}
```

#### (2) 更新模板类型引用

```typescript
// 旧代码
readonly templateOptions: PageTemplate[] = [...PAGE_TEMPLATES];
onTemplatePick(pageId: string, template: PageTemplate): void { ... }

// 新代码
readonly templateOptions: PageTemplatePlugin[] = [
  { impl: 'two-column-page-template', vals: { leftColumnWidth: 30, leftColumnUnit: 'PERCENT' } },
  { impl: 'two-column-page-template', vals: { leftColumnWidth: 260, leftColumnUnit: 'PX' } },
  // ... 其他预设
];
onTemplatePick(pageId: string, template: PageTemplatePlugin): void { ... }
```

### 3. 修改模板 `workshop-page.component.html`

```html
<!-- 旧代码 -->
<div class="page-sections" [ngClass]="pageLayoutClass(page)">

<!-- 新代码 -->
<div class="page-sections" [ngStyle]="pageLayoutStyle(page)">
```

### 4. 删除或重构 CSS 类 `workshop-page.component.less`

```less
// 旧代码（可以删除）
.page-template-blank { grid-template-columns: 30% minmax(0,1fr); }
.page-template-details { grid-template-columns: 260px minmax(0,1fr); }
// ...

// 新代码（只保留基础 grid 容器）
.page-sections {
  display: grid;
  gap: 16px;
  // grid-template-columns / grid-template-rows 由 [ngStyle] 动态注入
}
```

### 5. 修改 `ModuleStateService.seedSectionsByTemplate()`

```typescript
// 旧代码
seedSectionsByTemplate(pageId: string, template: PageTemplate): WorkshopSection[] {
  switch (template) {
    case 'blank':
    case 'two_column':
      return [createSection('分区 1'), createSection('分区 2')];
    case 'grid':
      return [createSection('筛选条'), ...Array(4).fill(null).map((_, i) => createSection(`区块 ${i+1}`))];
    // ...
  }
}

// 新代码
seedSectionsByTemplate(pageId: string, template: PageTemplatePlugin): WorkshopSection[] {
  const count = getInitialSectionCount(template);
  return Array(count).fill(null).map((_, i) => createSection(`分区 ${i+1}`));
}
```

---

## 迁移兼容性

### 后端兼容

1. **读盘**：遇到旧枚举字符串，通过 `PageTemplateMigration.migrateFromLegacyEnum` 转成插件实例
2. **写盘**：统一按插件 JSON 格式写入（`{impl, vals}`）
3. **已有页面**：首次读取时自动转换，保存后变成新格式

### 前端兼容

1. **module GET 响应**：调用 `resolvePageTemplate` 归一化
2. **本地状态**：统一按 `PageTemplatePlugin` 类型存储
3. **模板选择器**：预设几个常用配置供快速选择

### 退化处理

**OVERVIEW / SETTINGS 的首项通栏特性暂时丢失**，因为 `TwoColumnPageTemplate` 没有对应字段。迁移时退化成：
- OVERVIEW → 等宽两栏（50% / 50%）
- SETTINGS → 1:2 两栏（33% / 67%）

若要完整保留，需给 `TwoColumnPageTemplate` 加 `boolean firstChildSpanAll` 字段。

---

## 实施步骤

### 阶段 1：后端准备（不影响线上）

1. 创建 `PageTemplate.java` 抽象基类
2. 创建 4 个派生类（TwoColumn / HeaderContent / Grid / ThreeColumn）
3. 创建 `PageTemplateMigration.java`
4. 修改 `WorkshopPage.java` 的字段类型
5. 在读盘逻辑中添加旧枚举转换

### 阶段 2：前端准备（不影响线上）

1. 更新 `page.model.ts`（定义新类型 + 迁移函数）
2. 修改 `workshop-page.component.ts`（改用动态样式）
3. 修改 `workshop-page.component.html`（改用 ngStyle）
4. 更新 `ModuleStateService.seedSectionsByTemplate`
5. 重构模板选择器组件（`page-template-picker.component.ts`）

### 阶段 3：联调测试

1. 创建新页面，选择各种模板，验证栅格正确
2. 打开已有页面，验证旧枚举自动转换
3. 修改模板配置（如改左栏宽度），验证实时生效
4. 保存后重新加载，验证配置持久化
5. 测试上下布局的新特性

### 阶段 4：上线

1. 后端先上线（旧前端仍可工作，只是看不到新配置字段）
2. 前端再上线（完整支持新特性）

---

## 未来扩展

### 1. 恢复 OVERVIEW / SETTINGS 的首项通栏

给 `TwoColumnPageTemplate` 加字段：

```java
@FormField(ordinal = 2, type = FormFieldType.ENUM)
public Boolean firstChildSpanAll = false;
```

前端 CSS：

```typescript
if (template.impl === 'two-column-page-template' && template.vals.firstChildSpanAll) {
  // 添加 :first-child { grid-column: 1 / -1 } 的类
}
```

### 2. 支持更多布局

- **四栏布局**：`FourColumnPageTemplate`
- **侧边栏 + 主区**：左/右固定侧边栏 + 中间两栏
- **自由网格**：用户自定义 grid-template-areas

### 3. 响应式断点配置

让用户配置"窄屏回落"的阈值（默认 768px）

### 4. 可视化配置器

拖拽分隔线调整栏宽，实时预览效果

---

## 总结

这次改造将固定的 8 个枚举值重构为可扩展的插件架构：

✅ **统一了 4 个两栏变体** → 一个可配置的 `TwoColumnPageTemplate`  
✅ **新增了上下布局** → `HeaderContentPageTemplate`  
✅ **支持用户自定义宽度** → 百分比或像素，任意值  
✅ **保留了网格和三栏** → `GridPageTemplate` / `ThreeColumnPageTemplate`  
✅ **完全兼容旧数据** → 旧枚举自动转成对应配置  

改造后，用户可以根据实际需求灵活配置页面布局，不再受固定枚举的限制。
