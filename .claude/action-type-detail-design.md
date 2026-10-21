# Action Type 详情页设计方案

## 一、需求背景

根据 Palantir Action Types 文档和用户需求，需要将 Action 创建流程分为两个阶段：

### 第一阶段：创建向导（Creation Wizard）
- **ActionMetadata**：元信息（名称、描述等）
- **ActionRuleSetter**：规则设置（定义修改哪些属性）

### 第二阶段：详细配置（Post-Creation Configuration）
- **Parameters**：参数配置（从 ActionRuleSetter 的规则自动生成，只能编辑不能新增）
- **Submission Criteria**：提交标准（条件校验）
- **Rules**：规则进一步调整
- **Test Run**：测试运行
- **Overview**：概览信息

创建向导完成后，跳转到详情页进行第二阶段的配置。

---

## 二、后端改动

### 2.1 修改 DefaultOntologyAction.java

**文件**: `plugins/tis-ontology-plugin/src/main/java/.../impl/action/DefaultOntologyAction.java`

**改动**: L177-184，只保留两个步骤

```java
@Override
public List<OneStepOfMultiSteps.BasicDesc> getStepDescriptionList() {
    return List.of(
            new ActionMetadata.Desc(),
            new ActionRuleSetter.Desc()
    );
}
```

**理由**: 
- 第一阶段只需完成基本的元信息和规则骨架
- Parameters 和 SubmissionCriteria 移到第二阶段在详情页编辑

### 2.2 ActionParameters 初始化逻辑改造

**需求**: ActionParameters 应该从 ActionRuleSetter 中提取规则并自动生成参数列表

**实现要点**:
1. 在 ActionParameters 的 Descriptor 中重写相关方法
2. 从父级 ActionRuleSetter 读取已配置的规则
3. 为每个涉及的对象和属性自动生成 Parameter：
   - 对象本身的 parameter（如 `Ticket`）
   - 要修改属性的 parameter（如 `Priority`）

**参考 Palantir 文档**:
> "The `Ticket` and `Priority` parameters have already been created based on the **Rule**."
> (来源: getting-started.md:42)

---

## 三、前端改动

### 3.1 新增 Action Types 到 Ontology Detail 页面

**文件**: `tis-console/src/base/ontology.detail.component.ts`

**改动**: 在左侧菜单添加 "Action Types" 项

```typescript
type MenuKey = 'Object Types' | 'Link Types' | 'Value Types' | 'Shared Properties' | 'Glossary' | 'Action Types';
```

在模板中添加：
```html
<li nz-menu-item [nzSelected]="selectedMenu === 'Action Types'" (click)="selectMenu('Action Types')">
  <span nz-icon nzType="thunderbolt" nzTheme="outline"></span>
  <span>Action Types</span>
  <nz-badge class="ontology-resources-count" [nzCount]="ontologyDetail.actionTypes?.length || 0" nzStandalone></nz-badge>
</li>
```

### 3.2 创建新组件：ontology.action-type.detail.component.ts

**路径**: `tis-console/src/base/ontology.action-type.detail.component.ts`

**参考模板**: `ontology.object-type.detail.component.ts`

### 3.3 页面布局设计（参考 Palantir）

根据 Palantir 文档和截图，Action Type 详情页采用 **顶部 Profile Card + Tab 分页** 结构：

#### 顶部区域：Profile Card
```html
<nz-card class="profile-card" nzTitle="Profile">
  <nz-descriptions nzBordered [nzColumn]="2">
    <nz-descriptions-item nzTitle="Action Name">{{ actionDetail.name }}</nz-descriptions-item>
    <nz-descriptions-item nzTitle="Description">{{ actionDetail.description }}</nz-descriptions-item>
    <nz-descriptions-item nzTitle="Object Type">{{ actionDetail.objectType }}</nz-descriptions-item>
    <nz-descriptions-item nzTitle="Created Time">{{ actionDetail.createTime | date:'yyyy/MM/dd HH:mm:ss' }}</nz-descriptions-item>
  </nz-descriptions>
</nz-card>
```

#### Tab 结构（5个主要Tab）

**1. Overview Tab**
- 显示 Action 的详细描述
- 关联的 Object Type 信息
- 创建和更新时间

**2. Rules Tab**
- 显示已配置的规则列表（表格形式）
- 列：Rule Type / Target / Properties / Operations
- 支持添加新规则按钮（右上角）
- 每行可编辑/删除

**3. Parameters Tab** ⭐核心
- 顶部提示：`Parameters are automatically generated from Rules. You can only edit their constraints and settings.`
- 表格列：Parameter Name / Display Name / Data Type / Constraints / Operations
- **没有"添加"按钮**（由规则驱动）
- 点击参数名可编辑其约束、默认值等配置

**4. Security & Submission Criteria Tab**
- Submission Criteria 卡片：
  - 条件列表表格
  - 添加条件按钮
- Permissions 卡片（下方）

**5. Test Run Tab**（可选，MVP可延后）
- 参数输入表单
- 运行测试按钮
- 显示预期变更结果

#### 代码框架示例

```typescript
<nz-tabset nzType="card" [(nzSelectedIndex)]="selectedTabIndex">
  <nz-tab nzTitle="Overview">
    <nz-card nzTitle="Basic Information">
      <nz-descriptions nzBordered [nzColumn]="2">
        <nz-descriptions-item nzTitle="Name">{{ actionDetail.name }}</nz-descriptions-item>
        <nz-descriptions-item nzTitle="Description">{{ actionDetail.description }}</nz-descriptions-item>
      </nz-descriptions>
    </nz-card>
  </nz-tab>

  <nz-tab nzTitle="Rules">
    <nz-card [nzExtra]="rulesExtraTemplate">
      <tis-page [rows]="actionDetail.rules" [spinning]="formDisabled">
        <tis-col title="Rule Type" width="20" field="ruleType"></tis-col>
        <tis-col title="Target" width="30" field="target"></tis-col>
        <tis-col title="Properties" width="30">
          <ng-template let-rule='r'>
            <nz-tag *ngFor="let prop of rule.properties">{{ prop }}</nz-tag>
          </ng-template>
        </tis-col>
        <tis-col title="Operations" width="20">
          <ng-template let-rule='r'>
            <button nz-button nzSize="small" nzType="link" (click)="editRule(rule)">Edit</button>
            <button nz-button nzSize="small" nzType="link" nzDanger (click)="deleteRule(rule)">Delete</button>
          </ng-template>
        </tis-col>
      </tis-page>
    </nz-card>
    <ng-template #rulesExtraTemplate>
      <button nz-button nzSize="small" (click)="addNewRule()">
        <span nz-icon nzType="plus"></span> Add new rule
      </button>
    </ng-template>
  </nz-tab>

  <nz-tab nzTitle="Parameters">
    <nz-alert 
      nzType="info" 
      nzMessage="Parameters are automatically generated from Rules. You can only edit their constraints and settings."
      nzShowIcon
      [nzCloseable]="false"
      style="margin-bottom: 16px;">
    </nz-alert>
    <tis-page [rows]="actionDetail.parameters" [spinning]="formDisabled">
      <tis-col title="Parameter Name" width="20">
        <ng-template let-param='r'>
          <button nz-button nzType="link" nzSize="small" (click)="editParameter(param)">
            {{ param.parameterId }}
          </button>
        </ng-template>
      </tis-col>
      <tis-col title="Display Name" width="20" field="displayName"></tis-col>
      <tis-col title="Data Type" width="20" field="dataType"></tis-col>
      <tis-col title="Constraints" width="20">
        <ng-template let-param='r'>
          <nz-tag *ngIf="param.required">Required</nz-tag>
          <nz-tag *ngIf="param.hidden">Hidden</nz-tag>
        </ng-template>
      </tis-col>
      <tis-col title="Operations" width="20">
        <ng-template let-param='r'>
          <button nz-button nzSize="small" nzType="link" (click)="editParameter(param)">Edit</button>
        </ng-template>
      </tis-col>
    </tis-page>
  </nz-tab>

  <nz-tab nzTitle="Security & Submission Criteria">
    <nz-card nzTitle="Submission Criteria" [nzExtra]="criteriaExtraTemplate">
      <tis-page [rows]="actionDetail.submissionCriteria" [spinning]="formDisabled">
        <tis-col title="Condition Type" width="20" field="conditionType"></tis-col>
        <tis-col title="Expression" width="40" field="expression"></tis-col>
        <tis-col title="Failure Message" width="20" field="failureMessage"></tis-col>
        <tis-col title="Operations" width="20">
          <ng-template let-criterion='r'>
            <button nz-button nzSize="small" nzType="link" (click)="editCriterion(criterion)">Edit</button>
            <button nz-button nzSize="small" nzType="link" nzDanger (click)="deleteCriterion(criterion)">Delete</button>
          </ng-template>
        </tis-col>
      </tis-page>
    </nz-card>
    <ng-template #criteriaExtraTemplate>
      <button nz-button nzSize="small" (click)="addCriterion()">
        <span nz-icon nzType="plus"></span> Add condition
      </button>
    </ng-template>
  </nz-tab>

  <nz-tab nzTitle="Test Run">
    <nz-card nzTitle="Test Action Execution">
      <nz-alert 
        nzType="warning" 
        nzMessage="Test runs validate submission criteria, rules, and proposed changes without affecting real data."
        nzShowIcon
        style="margin-bottom: 16px;">
      </nz-alert>
      <!-- 测试表单 -->
    </nz-card>
  </nz-tab>
</nz-tabset>
```

### 3.4 路由配置

**文件**: `tis-console/src/base/base.manage-routing.module.ts`

添加路由：
```typescript
{
  path: 'ontology/:targetName/action-type/:actionName',
  component: OntologyActionTypeDetailComponent
}
```

### 3.5 数据模型定义

**文件**: `tis-console/src/base/common/ontology.common.ts`

添加类型定义：
```typescript
export type OntologyAction = {
  name: string;
  description: string;
  objectType: string;
  domain: string;
  createTime: number;
}

export type OntologyActionDetail = {
  name: string;
  description: string;
  objectType: string;
  rules: Array<ActionRule>;
  parameters: Array<ActionParameter>;
  submissionCriteria: Array<SubmissionCriterion>;
}

export type ActionRule = {
  ruleType: string;
  target: string;
  properties: Array<string>;
}

export type ActionParameter = {
  parameterId: string;
  displayName: string;
  dataType: string;
  required: boolean;
  hidden: boolean;
}

export type SubmissionCriterion = {
  conditionType: string;
  expression: string;
  failureMessage: string;
}
```

---

## 四、实现步骤

### Phase 1: 后端改造
1. ✅ 修改 `DefaultOntologyAction.java` 只保留两个步骤
2. 🔲 改造 `ActionParameters` 的初始化逻辑，从 `ActionRuleSetter` 提取规则并生成参数
3. 🔲 添加后端 REST API 支持详情页 CRUD 操作

### Phase 2: 前端基础
4. 🔲 在 `ontology.common.ts` 中添加类型定义
5. 🔲 创建 `ontology.action-type.detail.component.ts` 组件
6. 🔲 配置路由

### Phase 3: 前端 UI 实现（MVP）
7. 🔲 实现 Profile Card + Tab 基础框架
8. 🔲 实现 Overview Tab
9. 🔲 实现 Rules Tab（列表 + 基础 CRUD）
10. 🔲 实现 Parameters Tab（只读列表 + 编辑约束）
11. 🔲 实现 Security & Submission Criteria Tab

### Phase 4: 集成与跳转
12. 🔲 在 `ontology.detail.component.ts` 中添加 Action Types 菜单项
13. 🔲 创建向导完成后自动跳转到详情页
14. 🔲 测试完整流程

---

## 五、关键设计决策

### 5.1 为什么 Parameters 不能手动添加？

**Palantir 设计哲学**:
- Parameters 由 Rules 驱动自动生成
- 保证参数与规则的一致性
- 用户只需配置规则，参数自动出现

**实现**:
- 前端 Parameters Tab 不显示"添加"按钮
- 后端 ActionParameters 从 ActionRuleSetter 读取并生成
- 前端只能编辑参数的约束、默认值、显示设置

### 5.2 Tab 顺序的考虑

按照用户配置的自然流程排序：
1. **Overview** - 了解基本信息
2. **Rules** - 配置核心逻辑（第一步）
3. **Parameters** - 调整参数细节（由 Rules 生成）
4. **Security & Submission Criteria** - 添加约束
5. **Test Run** - 测试验证

### 5.3 Profile Card 的作用

- 固定在顶部，不占用 Tab 空间
- 显示最关键的信息：名称、描述、关联对象类型、创建时间
- 提供快速预览，无需切换 Tab

---

## 六、参考资源

### Palantir 文档
- `getting-started.md` - 第42行明确说明："The `Ticket` and `Priority` parameters have already been created based on the **Rule**."
- `explore-action-types.md` - Rules 和 Parameters 的配置位置说明
- `parameter-overview.md` - Parameters 的作用和配置方式

### TIS 现有组件参考
- `ontology.object-type.detail.component.ts` - Profile Card + 多区块布局模式
- `ontology.detail.component.ts` - 侧边栏菜单 + 内容区域布局

### 截图资源（Palantir）
- `actions_wizard.png` - 创建向导界面
- `actions_form.png` - Parameters 界面展示
- `actions_submission_criteria.png` - Submission Criteria 界面
- `action-test-run-tab.png` - Test Run Tab 示例

---

## 七、MVP 范围

### 必须实现（MVP）
- ✅ 后端：两阶段步骤拆分
- ✅ 后端：Parameters 自动生成逻辑
- ✅ 前端：Profile Card + Tab 框架
- ✅ 前端：Overview Tab
- ✅ 前端：Rules Tab（基础 CRUD）
- ✅ 前端：Parameters Tab（只读列表 + 编辑约束）
- ✅ 前端：Submission Criteria Tab（基础 CRUD）
- ✅ 路由配置和跳转

### 可延后（Post-MVP）
- Test Run Tab 的完整实现
- Side Effects 配置（Notification / Webhook / Schedule）
- Permissions 详细配置
- Action Metrics / Log / Reverts
- 与 Workshop 的深度集成

---

## 八、预估工作量

- **后端改造**: 2-3 天
  - DefaultOntologyAction 修改: 0.5 天
  - ActionParameters 初始化逻辑: 1 天
  - REST API 实现: 1-1.5 天

- **前端实现**: 4-5 天
  - 组件基础框架: 0.5 天
  - Profile Card + Tab 结构: 0.5 天
  - Overview Tab: 0.5 天
  - Rules Tab: 1.5 天
  - Parameters Tab: 1 天
  - Security & Submission Criteria Tab: 1 天

- **集成与测试**: 1-2 天

**总计**: 7-10 天