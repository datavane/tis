# JsonSchema 扁平化改造设计文档

## 1. 背景与问题

### 1.1 现状

TIS 通过 `JsonSchema.Builder` 将插件的 Descriptor 模型映射为 JSON Schema，用于指导 LLM 生成结构化 JSON 输出。当前生成的 schema 包含多层 TIS 内部约定的包装结构：

- **`_primaryVal` 包装**：每个简单字段都被包装为 `{ type: "object", properties: { _primaryVal: { ... } } }`
- **`descVal`/`impl`/`vals` 包装**：oneOf 字段的每个选项被包装为 `{ descVal: { impl: "...", vals: { ... } } }`

### 1.2 问题表现

以 MySQL 数据源的 `splitTableStrategy` 字段为例，当用户输入"host地址：192.168.28.200"时：

**LLM 实际生成**（错误）：
```json
"splitTableStrategy": { "id": "off" }
```

**期望生成**（正确）：
```json
"splitTableStrategy": {
  "descVal": {
    "impl": "com.qlangtech.tis.plugin.ds.NoneSplitTableStrategy",
    "vals": { "host": { "_primaryVal": "192.168.28.200" } }
  }
}
```

`timeZone` 同理，LLM 生成 `{ "_primaryVal": "Asia/Shanghai" }` 而非正确的 `{ "descVal": { ... } }` 结构。

### 1.3 根因分析

#### 原因一：`id` 字段误导 LLM

每个 oneOf 选项的 `id` 字段有 `const` 值（如 `"off"`），但不在 `required` 数组中。而 `descVal` 在 required 中。

代码位置：`JsonSchema.java` → `Builder.addOneOfProperty()` 方法

```java
// id 未加入 required（第4个参数 false）
schemaBuilder.addProperty(SCHEMA_PLUGIN_DESCRIPTOR_ID, FieldType.String,
    "代表本`oneOf`之一的标识符", false).setConst(jsonSchema.getSchemaName());
```

LLM 看到 `id: { const: "off" }` 后把它当作 discriminator shortcut，只输出 `{ "id": "off" }` 就结束了。

#### 原因二：嵌套层级过深

从 `splitTableStrategy` 到实际的 `host` 值，嵌套了 **5 层**：

```
splitTableStrategy → descVal → vals → host → _primaryVal
```

LLM 无法从 schema 推导出用户提供的 "host地址" 应放在这么深的位置。

#### 原因三：`impl` 字段是 Java 全限定类名

`descVal.impl` 的值是 `com.qlangtech.tis.plugin.ds.NoneSplitTableStrategy` 这样的 Java FQN。虽标记了 `const`，但 LLM 倾向于避免输出它不理解的长字符串。

### 1.4 代码路径追踪

Schema 生成链路：

```
DescriptorsJSONForAIPrompt.AISchemaDescriptorsMeta.addDesc()
  └─ JsonSchema.Builder.addObjectProperty("vals", inner -> {
       └─ [describable 字段] inner.addOneOfProperty(attrVal, descriptors)
       │    └─ addObjectProperty(KEY_DESC_VAL, ...)  ← 产生 descVal 包装
       │    └─ addProperty("id", ..., false)          ← id 非 required
       └─ [普通字段] inner.addObjectProperty(fieldKey, i -> {
            └─ i.addProperty("_primaryVal", ...)      ← 产生 _primaryVal 包装
     })
```

关键文件：
- `tis-plugin/src/main/java/com/qlangtech/tis/aiagent/llm/JsonSchema.java`
- `tis-plugin/src/main/java/com/qlangtech/tis/util/DescriptorsJSONForAIPrompt.java`

## 2. 改造目标

### 2.1 改造前后 Schema 对比

#### 普通属性（以 `port` 为例）

**改造前**（3 层嵌套）：
```json
"port": {
  "type": "object",
  "additionalProperties": false,
  "properties": {
    "_primaryVal": {
      "type": "integer",
      "default": 3306,
      "pattern": "-?[1-9]{1}[\\d]{0,}|0",
      "description": "\n `default`:3306"
    }
  },
  "required": ["_primaryVal"]
}
```

**改造后**（直接描述）：
```json
"port": {
  "type": "integer",
  "default": 3306,
  "pattern": "-?[1-9]{1}[\\d]{0,}|0",
  "description": "\n `default`:3306"
}
```

#### oneOf 属性（以 `splitTableStrategy` 的 "off" 选项为例）

**改造前**（5 层嵌套）：
```json
"splitTableStrategy": {
  "oneOf": [{
    "type": "object",
    "properties": {
      "descVal": {
        "type": "object",
        "properties": {
          "impl": { "const": "com.qlangtech.tis.plugin.ds.NoneSplitTableStrategy", "type": "string" },
          "vals": {
            "type": "object",
            "properties": {
              "host": {
                "type": "object",
                "properties": {
                  "_primaryVal": { "type": "string", "description": "服务器节点连接地址..." }
                },
                "required": ["_primaryVal"]
              }
            },
            "required": ["host"]
          }
        },
        "required": ["impl", "vals"]
      },
      "id": { "const": "off", "type": "string", "description": "代表本`oneOf`之一的标识符" }
    },
    "required": ["descVal"],
    "additionalProperties": false
  }]
}
```

**改造后**（2 层嵌套）：
```json
"splitTableStrategy": {
  "oneOf": [{
    "type": "object",
    "properties": {
      "id": { "const": "off", "type": "string", "description": "代表本`oneOf`之一的标识符" },
      "host": { "type": "string", "description": "服务器节点连接地址..." }
    },
    "required": ["id", "host"],
    "additionalProperties": false
  }]
}
```

### 2.2 LLM 期望输出格式（扁平格式）

```json
{
  "impl": "com.qlangtech.tis.plugin.ds.mysql.MySQLV5DataSourceFactory",
  "vals": {
    "port": 3306,
    "dbName": "shop",
    "userName": "root",
    "password": "123456",
    "encode": "utf8",
    "useCompression": true,
    "extraParams": "",
    "name": "shop_ds",
    "splitTableStrategy": { "id": "off", "host": "192.168.28.200" },
    "timeZone": { "id": "default", "timeZone": "Asia/Shanghai" }
  }
}
```

### 2.3 TIS 后端期望的格式（深层格式，还原后）

```json
{
  "impl": "com.qlangtech.tis.plugin.ds.mysql.MySQLV5DataSourceFactory",
  "vals": {
    "port": { "_primaryVal": 3306 },
    "dbName": { "_primaryVal": "shop" },
    "userName": { "_primaryVal": "root" },
    "password": { "_primaryVal": "123456" },
    "encode": { "_primaryVal": "utf8" },
    "useCompression": { "_primaryVal": true },
    "extraParams": { "_primaryVal": "" },
    "name": { "_primaryVal": "shop_ds" },
    "splitTableStrategy": {
      "descVal": {
        "impl": "com.qlangtech.tis.plugin.ds.NoneSplitTableStrategy",
        "vals": {
          "host": { "_primaryVal": "192.168.28.200" }
        }
      }
    },
    "timeZone": {
      "descVal": {
        "impl": "com.qlangtech.tis.plugin.timezone.DefaultTISTimeZone",
        "vals": {
          "timeZone": { "_primaryVal": "Asia/Shanghai" }
        }
      }
    }
  }
}
```

## 3. 改造方案

### 3.1 第一部分：扁平化 Schema 生成（源头改造）

#### 3.1.1 修改文件：`tis-plugin/src/main/java/com/qlangtech/tis/util/DescriptorsJSONForAIPrompt.java`

**修改 `AISchemaDescriptorsMeta.addDesc()` 方法 (L127-197)**

当前逻辑（L138）对非 describable 字段通过 `addObjectProperty` 做了 `_primaryVal` 包装：

```java
// 当前代码
inner.addObjectProperty(attrVal.fieldKey, (i) -> {
    // ...
    JsonSchema.AddedProperty addedProperty =
        i.addProperty(KEY_primaryVal, schemaFieldType, helpContent.toString(), pt.isInputRequired());
    // 设置 default/enum/pattern ...
});
```

**改为**直接将属性添加到 inner builder（不经过 `addObjectProperty` 和 `_primaryVal` 包装）：

```java
// 改造后代码
JsonSchema.AddedProperty addedProperty =
    inner.addProperty(attrVal.fieldKey, schemaFieldType, helpContent.toString(), pt.isInputRequired());
// 设置 default/enum/pattern 直接在 addedProperty 上操作
if (dft != null) {
    addedProperty.setDefault(dft);
}
if (CollectionUtils.isNotEmpty(enumPropOptions)) {
    addedProperty.setValEnums(...);
}
// pattern 设置同理
```

#### 3.1.2 修改文件：`tis-plugin/src/main/java/com/qlangtech/tis/aiagent/llm/JsonSchema.java`

**修改 `Builder.addOneOfProperty()` 方法**

当前逻辑对每个 oneOf 选项通过 `descVal` 包装：

```java
// 当前代码
for (Map.Entry<String, JsonSchema> entry : schemaDescriptorsMeta.descSchemaRegister.entrySet()) {
    Builder schemaBuilder = createInnerSchemaBuilder(propertyKey);
    JsonSchema jsonSchema = entry.getValue();
    // descVal 包装
    schemaBuilder.addObjectProperty(KEY_DESC_VAL, (descBuilder) -> {
        descBuilder.setSchema(jsonSchema);
    });
    // id 非 required
    schemaBuilder.addProperty(SCHEMA_PLUGIN_DESCRIPTOR_ID, FieldType.String,
        "代表本`oneOf`之一的标识符", false).setConst(jsonSchema.getSchemaName());
    oneOf.add(schemaBuilder.build().schema());
}
```

**改为**：去掉 `descVal` 包装，将子 schema 的属性直接提升到 oneOf 选项顶层。

```java
// 改造后代码
for (Map.Entry<String, JsonSchema> entry : schemaDescriptorsMeta.descSchemaRegister.entrySet()) {
    Builder schemaBuilder = createInnerSchemaBuilder(propertyKey);
    JsonSchema jsonSchema = entry.getValue();

    // 1. id 设为 required
    schemaBuilder.addProperty(SCHEMA_PLUGIN_DESCRIPTOR_ID, FieldType.String,
        "代表本`oneOf`之一的标识符", true).setConst(jsonSchema.getSchemaName());

    // 2. 直接将子 schema 的属性添加到当前 builder（不经过 descVal 包装）
    //    需要新增方法 setFlatSchema() 或修改 setSchema()
    schemaBuilder.setFlatSchema(jsonSchema);

    oneOf.add(schemaBuilder.build().schema());
}
```

**新增方法 `setFlatSchema()`**：

```java
/**
 * 将子 schema 的属性（已扁平化的 vals 内属性）直接添加到当前 builder。
 * 不经过 descVal/impl/vals 包装层。
 */
private void setFlatSchema(JsonSchema schema) {
    JSONObject innerSchema = schema.schema();
    // 从子 schema 中提取 vals 内的 properties
    JSONObject valsProps = innerSchema.getJSONObject("properties")
        .getJSONObject("vals")
        .getJSONObject("properties");
    JSONArray valsRequired = innerSchema.getJSONObject("properties")
        .getJSONObject("vals")
        .getJSONArray("required");

    // 将 vals 内的每个属性直接添加到当前 builder
    for (String fieldName : valsProps.keySet()) {
        JSONObject fieldSchema = valsProps.getJSONObject(fieldName);
        this.properties.put(fieldName, fieldSchema);
    }
    // 将 vals 的 required 合并到当前 builder
    if (valsRequired != null) {
        this.requiredFields.addAll(Sets.newHashSet(valsRequired.toArray(String[]::new)));
    }
    // fieldsDesc 同步处理
    for (Option fieldDesc : schema.getFieldsDesc()) {
        this.fieldsDesc.add(fieldDesc);
    }
}
```

> **注意**：由于 `addDesc()` 中 `_primaryVal` 包装也已去除（3.1.1），子 schema 的 vals 内属性本身已是扁平的，可直接提升。

#### 3.1.3 关键变化总结

| 变更点 | 改造前 | 改造后 |
|-------|--------|--------|
| 普通字段包装 | `{ fieldKey: { _primaryVal: { ... } } }` | `{ fieldKey: { type, description, ... } }` |
| oneOf 选项包装 | `{ descVal: { impl, vals: {...} }, id }` | `{ id, field1, field2, ... }` |
| `id` 字段 required | `false` | `true` |
| `impl`/`vals`/`descVal` 层 | 存在 | 移除 |

### 3.2 第二部分：反向还原工具

#### 3.2.1 新增文件：`tis-plugin/src/main/java/com/qlangtech/tis/aiagent/llm/FlatJsonToTisConverter.java`

将 LLM 生成的扁平 JSON 还原为 TIS 后端 `AttrValMap.parseDescribableMap()` 期望的深层格式。

**转换规则**：

1. 根级 `impl` 字段保持不变
2. 遍历 `vals` 中的每个字段，通过 `Descriptor.getPropertyTypes()` 获取字段的 `PropertyType`
3. **非 describable 字段**（`!pt.isDescribable()`）：
   - 将 `"port": 3306` → `"port": { "_primaryVal": 3306 }`
4. **describable 字段**（`pt.isDescribable()`，即 oneOf）：
   - 从扁平值中取 `id` 字段值（如 `"off"`）
   - 通过 `pt.getApplicableDescriptors()` 遍历可选 Descriptor，匹配 `descriptor.getDisplayName() == id`
   - 得到对应的 `impl`（`descriptor.getId()`，即 Java 全限定类名）
   - 剩余字段（除 `id` 外）递归转换为 `vals`（同样添加 `_primaryVal` 包装）
   - 组装为 `{ "descVal": { "impl": "...", "vals": { ... } } }`

**核心方法签名**：

```java
public class FlatJsonToTisConverter {

    /**
     * 将 LLM 生成的扁平 JSON 还原为 TIS 后端期望的深层格式
     *
     * @param flatJson  扁平 JSON，结构为 { "impl": "...", "vals": { fieldKey: value, ... } }
     * @return TIS 格式 JSON，结构为 { "impl": "...", "vals": { fieldKey: { "_primaryVal": value }, ... } }
     */
    public static JSONObject convert(JSONObject flatJson) {
        String impl = flatJson.getString("impl");
        Descriptor descriptor = TIS.get().getDescriptor(impl);
        JSONObject result = new JSONObject();
        result.put("impl", impl);
        result.put("vals", convertVals(flatJson.getJSONObject("vals"), descriptor));
        return result;
    }

    /**
     * 递归转换 vals 内的属性
     */
    private static JSONObject convertVals(JSONObject flatVals, Descriptor descriptor) {
        JSONObject result = new JSONObject();
        Map<String, IPropertyType> propertyTypes = descriptor.getPropertyTypes();

        for (Map.Entry<String, Object> entry : flatVals.entrySet()) {
            String fieldName = entry.getKey();
            Object value = entry.getValue();
            PropertyType pt = (PropertyType) propertyTypes.get(fieldName);

            if (pt != null && pt.isDescribable()) {
                // oneOf 字段：需要还原 descVal 包装
                JSONObject flatOneOf = (JSONObject) value;
                result.put(fieldName, convertDescribableField(flatOneOf, pt));
            } else {
                // 普通字段：添加 _primaryVal 包装
                JSONObject wrapped = new JSONObject();
                wrapped.put("_primaryVal", value);
                result.put(fieldName, wrapped);
            }
        }
        return result;
    }

    /**
     * 还原 describable（oneOf）字段
     * 输入：{ "id": "off", "host": "192.168.28.200" }
     * 输出：{ "descVal": { "impl": "...NoneSplitTableStrategy", "vals": { "host": { "_primaryVal": "..." } } } }
     */
    private static JSONObject convertDescribableField(JSONObject flatOneOf, PropertyType pt) {
        String id = flatOneOf.getString("id");

        // 通过 id（displayName）找到对应的 Descriptor
        Descriptor matchedDesc = null;
        for (Descriptor desc : pt.getApplicableDescriptors()) {
            if (desc.getDisplayName().equals(id)) {
                matchedDesc = desc;
                break;
            }
        }

        JSONObject descVal = new JSONObject();
        descVal.put("impl", matchedDesc.getId());

        // 除 id 外的字段递归转换为 vals
        JSONObject innerVals = new JSONObject();
        for (Map.Entry<String, Object> entry : flatOneOf.entrySet()) {
            if ("id".equals(entry.getKey())) continue;
            // 递归处理：子 Descriptor 中如果还有 describable 字段
            Map<String, IPropertyType> innerPts = matchedDesc.getPropertyTypes();
            PropertyType innerPt = (PropertyType) innerPts.get(entry.getKey());
            if (innerPt != null && innerPt.isDescribable()) {
                innerVals.put(entry.getKey(), convertDescribableField((JSONObject) entry.getValue(), innerPt));
            } else {
                JSONObject wrapped = new JSONObject();
                wrapped.put("_primaryVal", entry.getValue());
                innerVals.put(entry.getKey(), wrapped);
            }
        }
        descVal.put("vals", innerVals);

        JSONObject result = new JSONObject();
        result.put("descVal", descVal);
        return result;
    }
}
```

### 3.3 第三部分：接入反向还原

#### 3.3.1 文件：`tis-console/src/main/java/com/qlangtech/tis/aiagent/execute/impl/BasicStepExecutor.java`

在 `createPluginInstance()` 方法（L151-154），LLM 输出经过 `extractUserInput2Json()` 后、传入 `parseDescribableMap()` 之前，调用反向还原：

```java
// 当前代码
JSONObject pluginPostBody = extractUserInput2Json(context, userInput, endType,
    Objects.requireNonNull(entry.getValue()), llmProvider);
AttrValMap attrValMap = parseDescribableMap(Optional.empty(), pluginPostBody);
```

```java
// 改造后代码
JSONObject pluginPostBody = extractUserInput2Json(context, userInput, endType,
    Objects.requireNonNull(entry.getValue()), llmProvider);
// 扁平 → TIS 深层格式
pluginPostBody = FlatJsonToTisConverter.convert(pluginPostBody);
AttrValMap attrValMap = parseDescribableMap(Optional.empty(), pluginPostBody);
```

#### 3.3.2 文件：`tis-console/src/main/java/com/qlangtech/tis/mcp/WeatherHttpMcpServer.java`

在 `createDataSourceCreateToolSpec()` 的 `callHandler` 中，MCP tool 收到的 `request.arguments()` 需要反向还原后再提交给 TIS 后端：

```java
return McpServerFeatures.SyncToolSpecification.builder().tool(tool).callHandler((exchange, request) -> {
    JSONObject flatArgs = new JSONObject(request.arguments());
    // 反向还原
    JSONObject tisFormat = FlatJsonToTisConverter.convert(flatArgs);
    // 提交给 TIS 后端处理...
}).build();
```

## 4. 关键文件清单

| 文件路径 | 改动类型 | 说明 |
|---------|---------|------|
| `tis-plugin/.../aiagent/llm/JsonSchema.java` | **修改** | 修改 `addOneOfProperty`，去掉 `descVal` 包装；新增 `setFlatSchema` 方法；`id` 改为 required |
| `tis-plugin/.../util/DescriptorsJSONForAIPrompt.java` | **修改** | 修改 `addDesc` 中普通属性生成，去掉 `_primaryVal` 包装 |
| `tis-plugin/.../aiagent/llm/FlatJsonToTisConverter.java` | **新增** | 反向还原工具类 |
| `tis-console/.../execute/impl/BasicStepExecutor.java` | **修改** | 在 `createPluginInstance` 中插入反向还原调用 |
| `tis-console/.../mcp/WeatherHttpMcpServer.java` | **修改** | 在 callHandler 中调用反向还原 |

## 5. 数据流图

```
┌──────────────────────────────────────────────────────────────────┐
│                        Schema 生成链路                            │
│                                                                  │
│  Descriptor/PropertyType                                         │
│       │                                                          │
│       ▼                                                          │
│  DescriptorsJSONForAIPrompt.addDesc()                            │
│       │  ← 改造：去掉 _primaryVal 包装                            │
│       ▼                                                          │
│  JsonSchema.Builder.addOneOfProperty()                           │
│       │  ← 改造：去掉 descVal 包装，id 加入 required              │
│       ▼                                                          │
│  扁平化 JsonSchema                                               │
│       │                                                          │
│       ├─── MCP Tool inputSchema ──→ MCP Client ──→ LLM          │
│       │                                                          │
│       └─── AI Agent chatJson ──→ LLM Provider ──→ LLM           │
└──────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────┐
│                        输出处理链路                               │
│                                                                  │
│  LLM 输出（扁平 JSON）                                           │
│       │                                                          │
│       ▼                                                          │
│  FlatJsonToTisConverter.convert()  ← 新增                        │
│       │  包装 _primaryVal                                        │
│       │  还原 descVal/impl/vals                                  │
│       ▼                                                          │
│  TIS 深层格式 JSON                                               │
│       │                                                          │
│       ▼                                                          │
│  AttrValMap.parseDescribableMap()  ← 不改动                      │
│       │                                                          │
│       ▼                                                          │
│  Descriptor.validate() → 创建插件实例                             │
└──────────────────────────────────────────────────────────────────┘
```

## 6. 验证方式

### 6.1 单元测试

1. 运行现有 schema 生成测试：
   ```bash
   mvn test -pl tis-plugin -Dtest=TestDescriptorsJSONForAIPromote -Dmaven.test.skip=false -o
   ```

2. 为 `FlatJsonToTisConverter` 编写单元测试，覆盖：
   - 普通字段还原（`port: 3306` → `{ _primaryVal: 3306 }`）
   - oneOf 字段还原（`{ id: "off", host: "..." }` → `{ descVal: { ... } }`）
   - 嵌套 oneOf 字段还原（oneOf 内部还有 oneOf 的场景）
   - 边界情况：可选字段缺失、空值处理

### 6.2 集成验证

1. 启动 TIS 控制台
2. 通过 MCP client 调用 `tools/list`，验证返回的 `create_datasource` tool 的 `inputSchema` 为扁平格式
3. 通过 agent 发送测试输入：
   > "我提供关于mysql的相关配置 数据库名：shop，用户名：root，密码：123456，端口：3306，时区：上海，host地址：192.168.28.200"
4. 验证 LLM 生成正确的扁平 JSON
5. 验证 `FlatJsonToTisConverter` 将扁平 JSON 正确还原为 TIS 格式
6. 验证 TIS 后端能正确创建 MySQL 数据源实例

### 6.3 回归验证

确保现有的 TIS AI Agent（`TISPlanAndExecuteAgent`）流程在改造后仍正常工作：
- `BasicStepExecutor.createPluginInstance()` 能正确处理扁平 schema
- `validateAttrValMap()` 在还原后能正常校验

## 7. 风险点与注意事项

1. **影响范围**：改造影响所有使用 `DescriptorsJSONForAIPrompt` 生成 schema 的流程（MCP 和 AI Agent）
2. **向后兼容**：如果有已保存的 schema 或缓存，需要清理
3. **递归 oneOf**：oneOf 选项内部可能还有 oneOf（如 `splitTableStrategy` 的 "on" 选项内部还有多个子字段），`setFlatSchema` 和 `FlatJsonToTisConverter` 都需要正确递归处理
4. **`fieldsDesc` 同步**：`JsonSchema` 的 `fieldsDesc` 用于生成提示词（`appendFieldDescToPrompt`），扁平化后需要确保 fieldsDesc 的路径和描述仍然正确
