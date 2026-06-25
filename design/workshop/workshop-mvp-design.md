# TIS Workshop MVP 设计方案

## 目标

验证基于本体的低代码应用架构可行性，构建第一个应用原型：**数据质量巡检应用**

## 核心架构

### 1. Function 和 Action 实体定义

```java
// tis-plugin/src/main/java/com/qlangtech/tis/plugin/ontology/OntologyFunction.java
public abstract class OntologyFunction extends Ontology {
    
    public enum FunctionCategory {
        DATA_TRANSFORM,    // 数据转换
        BUSINESS_RULE,     // 业务规则
        DATA_QUALITY,      // 数据质量
        CALCULATION        // 计算派生
    }
    
    /**
     * Function 元信息
     */
    public static class FunctionMeta {
        private String name;
        private String description;
        private FunctionCategory category;
        private List<ParamDef> inputs;
        private ParamDef output;
    }
    
    /**
     * 执行 Function（由子类实现）
     */
    public abstract Object apply(Map<String, Object> inputs) throws FunctionException;
    
    /**
     * 验证输入参数
     */
    public ValidationResult validateInputs(Map<String, Object> inputs) {
        // 默认实现：检查必填参数
        return ValidationResult.success();
    }
}

// tis-plugin/src/main/java/com/qlangtech/tis/plugin/ontology/OntologyAction.java
public abstract class OntologyAction extends Ontology {
    
    public enum ActionCategory {
        DATA_INTEGRATION,   // 数据集成
        DATA_REPAIR,        // 数据修复
        SCHEMA_EVOLUTION,   // Schema 演进
        DATA_GOVERNANCE     // 数据治理
    }
    
    /**
     * Action 元信息
     */
    public static class ActionMeta {
        private String name;
        private String description;
        private ActionCategory category;
        private List<ParamDef> inputs;
        private String requiredPermission;
        private boolean needsConfirmation;
    }
    
    /**
     * 执行 Action（由子类实现）
     */
    public abstract ActionResult execute(ActionContext context) throws ActionException;
    
    /**
     * 前置检查（如权限、前置条件）
     */
    public ValidationResult precheck(ActionContext context) {
        // 默认实现：权限检查
        return checkPermission(context.getUser());
    }
    
    /**
     * 权限检查
     */
    protected ValidationResult checkPermission(User user) {
        // TODO: 集成 TIS 现有的权限系统
        return ValidationResult.success();
    }
}
```

### 2. Function 和 Action 注册机制

```java
// tis-plugin/src/main/java/com/qlangtech/tis/plugin/ontology/registry/FunctionRegistry.java
public class FunctionRegistry {
    
    private static final FunctionRegistry INSTANCE = new FunctionRegistry();
    private final Map<String, OntologyFunction> functions = new ConcurrentHashMap<>();
    
    public static FunctionRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * 注册 Function（启动时通过 SPI 自动扫描）
     */
    public void register(OntologyFunction function) {
        String functionName = function.identityValue();
        functions.put(functionName, function);
    }
    
    /**
     * 调用 Function
     */
    public Object invoke(String functionName, Map<String, Object> inputs) {
        OntologyFunction function = functions.get(functionName);
        if (function == null) {
            throw new FunctionNotFoundException("Function not found: " + functionName);
        }
        
        // 验证输入
        ValidationResult validation = function.validateInputs(inputs);
        if (!validation.isValid()) {
            throw new InvalidInputException(validation.getMessage());
        }
        
        // 执行 Function
        return function.apply(inputs);
    }
    
    /**
     * 列出所有 Function
     */
    public List<FunctionMeta> listFunctions(FunctionCategory category) {
        return functions.values().stream()
            .filter(f -> category == null || f.getCategory() == category)
            .map(OntologyFunction::getMeta)
            .collect(Collectors.toList());
    }
}

// tis-plugin/src/main/java/com/qlangtech/tis/plugin/ontology/registry/ActionRegistry.java
public class ActionRegistry {
    
    private static final ActionRegistry INSTANCE = new ActionRegistry();
    private final Map<String, OntologyAction> actions = new ConcurrentHashMap<>();
    
    public static ActionRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * 注册 Action（启动时通过 SPI 自动扫描）
     */
    public void register(OntologyAction action) {
        String actionName = action.identityValue();
        actions.put(actionName, action);
    }
    
    /**
     * 执行 Action（带事务、审计）
     */
    public ActionResult execute(String actionName, ActionContext context) {
        OntologyAction action = actions.get(actionName);
        if (action == null) {
            return ActionResult.failure("Action not found: " + actionName);
        }
        
        // 1. 前置检查（权限、参数、前置条件）
        ValidationResult precheck = action.precheck(context);
        if (!precheck.isValid()) {
            return ActionResult.failure(precheck.getMessage());
        }
        
        // 2. 执行 Action（带事务）
        ActionResult result;
        long startTime = System.currentTimeMillis();
        try {
            result = action.execute(context);
        } catch (Exception e) {
            result = ActionResult.failure("Action execution failed: " + e.getMessage());
        }
        long duration = System.currentTimeMillis() - startTime;
        
        // 3. 记录审计日志
        auditLog(context.getUser(), actionName, context.getParams(), result, duration);
        
        // 4. 触发后置事件（如通知、同步）
        if (result.isSuccess()) {
            eventBus.post(new ActionExecutedEvent(actionName, context, result));
        }
        
        return result;
    }
    
    private void auditLog(User user, String actionName, Map<String, Object> params, 
                          ActionResult result, long duration) {
        // TODO: 集成 TIS 的日志系统
    }
}
```

### 3. MVP 应用：数据质量巡检

构建一个简单但完整的应用来验证架构。

#### 3.1 Function 实现

```java
// plugins/tis-ontology-plugin/.../function/CompletenessFunction.java
@TISPlugin(name = "calculateCompleteness")
public class CompletenessFunction extends OntologyFunction {
    
    @Override
    public FunctionCategory getCategory() {
        return FunctionCategory.DATA_QUALITY;
    }
    
    @Override
    public Object apply(Map<String, Object> inputs) {
        String objectTypeName = (String) inputs.get("objectType");
        String tableName = (String) inputs.get("tableName");
        
        // 1. 加载本体定义
        OntologyObjectType objectType = OntologyObjectType.load(objectTypeName);
        
        // 2. 查询数据
        DataSource ds = DataSourceFactory.load(tableName);
        List<Map<String, Object>> rows = ds.queryAll();
        
        // 3. 计算完整性
        int totalWeight = 0;
        int actualWeight = 0;
        
        for (OntologyProperty prop : objectType.getCols()) {
            int weight = prop.isPk() ? 2 : 1;
            totalWeight += weight;
            
            long nonNullCount = rows.stream()
                .filter(row -> row.get(prop.getName()) != null)
                .count();
            
            if (nonNullCount == rows.size()) {
                actualWeight += weight;
            }
        }
        
        return (actualWeight * 100.0) / totalWeight;
    }
}

// plugins/tis-ontology-plugin/.../function/ConstraintViolationFunction.java
@TISPlugin(name = "detectConstraintViolations")
public class ConstraintViolationFunction extends OntologyFunction {
    
    @Override
    public Object apply(Map<String, Object> inputs) {
        String objectTypeName = (String) inputs.get("objectType");
        
        OntologyObjectType objectType = OntologyObjectType.load(objectTypeName);
        DataSource ds = DataSourceFactory.load(objectTypeName);
        
        List<ViolationRecord> violations = new ArrayList<>();
        
        for (OntologyProperty prop : objectType.getCols()) {
            // 检查 Constraint 违规
            if (prop.getTypeRef() instanceof SharedPropertyTypeRef) {
                SharedPropertyTypeRef ref = (SharedPropertyTypeRef) prop.getTypeRef();
                OntologySharedProperty sharedProp = ref.getSharedProperty();
                // 检查共享属性的约束
            }
            
            // 检查 nullable 约束
            if (!prop.isNullable()) {
                long nullCount = ds.query()
                    .where(prop.getName() + " IS NULL")
                    .count();
                if (nullCount > 0) {
                    violations.add(new ViolationRecord(
                        objectTypeName,
                        prop.getName(),
                        "NOT_NULL",
                        nullCount
                    ));
                }
            }
        }
        
        return violations;
    }
}
```

#### 3.2 Action 实现

```java
// plugins/tis-ontology-plugin/.../action/DataQualityReportAction.java
@TISPlugin(name = "generateDataQualityReport")
public class DataQualityReportAction extends OntologyAction {
    
    @Override
    public ActionCategory getCategory() {
        return ActionCategory.DATA_GOVERNANCE;
    }
    
    @Override
    public ActionResult execute(ActionContext context) {
        String domain = context.getParam("domain");
        
        // 1. 加载该 domain 下的所有 ObjectType
        List<OntologyObjectType> objectTypes = OntologyObjectType.load(domain);
        
        // 2. 对每个 ObjectType 调用质量检测 Function
        List<QualityScore> scores = new ArrayList<>();
        
        for (OntologyObjectType ot : objectTypes) {
            // 调用完整性 Function
            Double completeness = (Double) FunctionRegistry.getInstance().invoke(
                "calculateCompleteness",
                Map.of("objectType", ot.getName())
            );
            
            // 调用约束违规检测 Function
            List<ViolationRecord> violations = (List<ViolationRecord>) 
                FunctionRegistry.getInstance().invoke(
                    "detectConstraintViolations",
                    Map.of("objectType", ot.getName())
                );
            
            scores.add(new QualityScore(
                ot.getName(),
                completeness,
                violations.size()
            ));
        }
        
        // 3. 生成报告（HTML/PDF）
        String reportPath = generateReport(domain, scores);
        
        // 4. 返回结果
        return ActionResult.success()
            .withMessage("数据质量报告已生成")
            .withData("reportPath", reportPath)
            .withData("scores", scores);
    }
}

// plugins/tis-ontology-plugin/.../action/FixViolationsAction.java
@TISPlugin(name = "fixConstraintViolations")
public class FixViolationsAction extends OntologyAction {
    
    @FormField(ordinal = 1, type = FormFieldType.SELECTABLE)
    public String repairStrategy;  // 修复策略：DEFAULT_VALUE / DELETE / SKIP
    
    @Override
    public ActionResult execute(ActionContext context) {
        String objectTypeName = context.getParam("objectType");
        String propertyName = context.getParam("property");
        
        // 1. 检测违规数据
        List<ViolationRecord> violations = (List<ViolationRecord>) 
            FunctionRegistry.getInstance().invoke(
                "detectConstraintViolations",
                Map.of("objectType", objectTypeName)
            );
        
        // 2. 根据策略修复
        int fixedCount = 0;
        DataSource ds = DataSourceFactory.load(objectTypeName);
        
        for (ViolationRecord v : violations) {
            if (!v.getPropertyName().equals(propertyName)) {
                continue;
            }
            
            switch (repairStrategy) {
                case "DEFAULT_VALUE":
                    Object defaultValue = getDefaultValue(v);
                    ds.update(v.getRecordId(), v.getPropertyName(), defaultValue);
                    fixedCount++;
                    break;
                case "DELETE":
                    ds.delete(v.getRecordId());
                    fixedCount++;
                    break;
                case "SKIP":
                default:
                    break;
            }
        }
        
        return ActionResult.success()
            .withMessage(String.format("已修复 %d 条违规数据", fixedCount));
    }
}
```

#### 3.3 应用定义（XML 配置）

```xml
<!-- tis-plugin/.../ontology/applications/data-quality-inspector.xml -->
<application>
    <name>data-quality-inspector</name>
    <displayName>数据质量巡检</displayName>
    <description>基于本体定义自动检测数据质量问题</description>
    
    <pages>
        <!-- 概览页：显示所有 ObjectType 的质量得分 -->
        <page name="overview" type="TABLE_VIEW">
            <objectType>meta</objectType>
            <columns>
                <column name="objectTypeName" label="对象类型"/>
                <column name="completeness" label="完整性" function="calculateCompleteness"/>
                <column name="violationCount" label="违规数量" function="detectConstraintViolations"/>
            </columns>
            <actions>
                <action name="generateDataQualityReport" label="生成报告" type="primary"/>
            </actions>
        </page>
        
        <!-- 详情页：显示单个 ObjectType 的违规明细 -->
        <page name="detail" type="TABLE_VIEW">
            <columns>
                <column name="recordId" label="记录 ID"/>
                <column name="propertyName" label="违规字段"/>
                <column name="violationType" label="违规类型"/>
                <column name="currentValue" label="当前值"/>
            </columns>
            <actions>
                <action name="fixConstraintViolations" label="修复违规" type="danger"/>
            </actions>
        </page>
    </pages>
</application>
```

#### 3.4 前端集成

```typescript
// tis-console/src/base/application-runtime.component.ts
@Component({
  template: `
    <div class="application-runtime">
      <h2>{{ appMeta.displayName }}</h2>
      
      <!-- 动态渲染页面 -->
      <nz-tabset [(nzSelectedIndex)]="activePageIndex">
        <nz-tab *ngFor="let page of appMeta.pages" [nzTitle]="page.name">
          <app-page-renderer [page]="page" [objectType]="page.objectType">
          </app-page-renderer>
        </nz-tab>
      </nz-tabset>
    </div>
  `
})
export class ApplicationRuntimeComponent {
  appMeta: ApplicationMeta;
  activePageIndex = 0;
  
  ngOnInit() {
    // 加载应用定义
    this.httpClient.get('/ontology/application/data-quality-inspector')
      .subscribe(data => {
        this.appMeta = data;
      });
  }
}

// tis-console/src/base/page-renderer.component.ts
@Component({
  selector: 'app-page-renderer',
  template: `
    <div class="page-renderer">
      <!-- 表格视图 -->
      <nz-table *ngIf="page.type === 'TABLE_VIEW'" 
                [nzData]="tableData"
                [nzLoading]="loading">
        <thead>
          <tr>
            <th *ngFor="let col of page.columns">{{ col.label }}</th>
            <th *ngIf="page.actions?.length > 0">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let row of tableData">
            <td *ngFor="let col of page.columns">
              <!-- 如果有 Function，调用 Function 计算值 -->
              <span *ngIf="col.function">
                {{ invokeFunction(col.function, row) }}
              </span>
              <span *ngIf="!col.function">
                {{ row[col.name] }}
              </span>
            </td>
            <td *ngIf="page.actions?.length > 0">
              <button *ngFor="let action of page.actions"
                      (click)="executeAction(action.name, row)">
                {{ action.label }}
              </button>
            </td>
          </tr>
        </tbody>
      </nz-table>
    </div>
  `
})
export class PageRendererComponent {
  @Input() page: PageMeta;
  @Input() objectType: string;
  
  tableData: any[] = [];
  loading = false;
  
  ngOnInit() {
    this.loadData();
  }
  
  loadData() {
    this.loading = true;
    // 加载数据（根据 page 配置）
    this.httpClient.post('/ontology/application/query', {
      objectType: this.objectType,
      columns: this.page.columns.map(c => c.name)
    }).subscribe(data => {
      this.tableData = data;
      this.loading = false;
    });
  }
  
  invokeFunction(functionName: string, row: any) {
    // 调用后端 Function API
    // 可以考虑批量调用 + 前端缓存优化性能
    return this.functionService.invoke(functionName, {
      objectType: this.objectType,
      recordId: row.id
    });
  }
  
  executeAction(actionName: string, row: any) {
    // 弹出 Action 参数配置对话框
    const dialogRef = this.modal.create({
      nzTitle: '执行操作',
      nzContent: ActionConfigDialogComponent,
      nzComponentParams: {
        actionName: actionName,
        context: { row: row }
      }
    });
    
    dialogRef.afterClose.subscribe(params => {
      if (params) {
        // 执行 Action
        this.actionService.execute(actionName, params).subscribe(result => {
          if (result.success) {
            this.message.success(result.message);
            this.loadData(); // 刷新数据
          } else {
            this.message.error(result.message);
          }
        });
      }
    });
  }
}
```

## 验收标准

MVP 完成后应该能够：

1. ✅ 通过配置文件定义一个应用（无需写代码）
2. ✅ 前端自动渲染应用界面（表格、表单、按钮）
3. ✅ Function 被正确调用并显示计算结果
4. ✅ Action 被正确执行并有审计日志
5. ✅ 用户可以通过 UI 执行数据质量巡检并修复问题

## 下一步

MVP 验证成功后，进入阶段 2：
- 实现可视化的应用设计器（拖拽式 UI）
- 支持更多页面类型（表单、Dashboard、工作流）
- 完善权限和审计体系
- 构建应用市场和模板库