/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qlangtech.tis.plugin.ontology;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.impl.DefaultContext;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.aiagent.core.IAgentContext;
import com.qlangtech.tis.aiagent.llm.ITISJsonSchema;
import com.qlangtech.tis.aiagent.llm.LLMProvider;
import com.qlangtech.tis.aiagent.llm.TISJsonSchema;
import com.qlangtech.tis.aiagent.llm.UserPrompt;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.extension.util.impl.DefaultGroovyShellFactory;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.ontology.impl.OntologyPluginMeta;
import com.qlangtech.tis.trigger.util.JsonUtil;
import com.qlangtech.tis.util.DescriptorsJSONForAIPrompt;
import com.qlangtech.tis.util.DescriptorsMeta;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.PartialSettedPluginContext;
import com.qlangtech.tis.util.UploadPluginMeta;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.qlangtech.tis.manage.common.UserProfile.KEY_FIELD_LLM_NAME;
import static com.qlangtech.tis.plugin.ontology.OntologyDomain.NAME_ONTOLOGY_DOMAIN;

/**
 * 利用 LLM 从已有 ObjectType 的表结构中推断 Link Type、Shared Property、Value Type
 * <p>
 * 用户在本体域管理界面触发此操作后，系统收集当前 domain 下所有 ObjectType 的 schema，
 * 组装 prompt 提交给 LLM，LLM 返回结构化 JSON 建议列表，前端展示供用户确认。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/5/20
 */
public class InferOntologyFromLLM extends OntologyDomainManipulate {

    //    @FormField(ordinal = 0, type = FormFieldType.SELECTABLE, validate = {Validator.require, Validator.identity})
    //    public String ontologyDomain;

    /**
     * 大模型接口
     */
    @FormField(type = FormFieldType.SELECTABLE, ordinal = 1, validate = {Validator.identity})
    public String llm;

    public LLMProvider getLlmProvider() {
        return LLMProvider.load(Objects.requireNonNull(IPluginContext.getThreadLocalInstance()), llm);
    }

    @Override
    public void manipuldateProcess(IPluginContext pluginContext, UploadPluginMeta pluginMeta,
                                   Optional<Context> context) {

        OntologyPluginMeta ometa = OntologyPluginMeta.createPluginMeta(pluginMeta);

        if (StringUtils.isEmpty(ometa.getDomain())) {
            throw new IllegalArgumentException("property ontologyDomain can not be null");
        }

        List<OntologyObjectType> objectTypes = OntologyObjectType.loadAll(ometa.getDomain());
        if (objectTypes.isEmpty()) {
            throw new IllegalStateException("domain '" + ometa.getDomain()
                    + "' has no ObjectType, please export tables first");
        }

        JSONObject tablesPayload = buildTablesPayload(objectTypes);
        String systemPrompt = this.buildSystemPrompt();
        String userPrompt = tablesPayload.toJSONString();

        // UserProfile userProfile = UserProfile.load(pluginContext, true);
        LLMProvider llmProvider = this.getLlmProvider();

        /**
         * 大模型推断
         */
        LLMProvider.LLMResponse response = llmProvider.chatJson(
                IAgentContext.createNull(),
                new UserPrompt("Infer ontology relations", userPrompt),
                Collections.singletonList(systemPrompt),
                buildOutputJsonSchema());

        if (!response.isSuccess() || response.getJsonContent() == null) {
            throw new IllegalStateException("LLM inference failed: "
                    + (response.getErrorMessage() != null ? response.getErrorMessage() : "no response"));
        }

        JSONObject jsonContent = response.getJsonContent();
        // 把 vals 内的 primitive 字段（如 MetadataOfValueType.type:int）包装成 {_primaryVal: val}，
        // 使其能被 AttrVals#parseAttrValMap 反序列化。详见 normalizeValsForReparse 注释。
        normalizeValsForReparse(jsonContent);

        pluginContext.setBizResult(context.orElseThrow(), jsonContent);
    }

    private JSONObject buildTablesPayload(List<OntologyObjectType> objectTypes) {
        JSONObject payload = new JSONObject();
        JSONArray tables = new JSONArray();
        for (OntologyObjectType ot : objectTypes) {
            JSONObject tableObj = new JSONObject();
            tableObj.put("name", ot.getName());
            JSONArray columns = new JSONArray();
            for (OntologyProperty col : ot.getCols()) {
                JSONObject colObj = new JSONObject();
                colObj.put("name", col.getName());
                colObj.put("type", col.parseOntologyType().name());
                colObj.put("pk", col.isPk());
                colObj.put("nullable", col.isNullable());
                if (StringUtils.isNotEmpty(col.getDescription())) {
                    colObj.put("comment", col.getDescription());
                }
                columns.add(colObj);
            }
            tableObj.put("columns", columns);
            tables.add(tableObj);
        }
        payload.put("tables", tables);
        return payload;
    }

    //    private String buildSystemPrompt() {
    //        return """
    //                你是一个数据建模专家，擅长分析数据库表结构并推断表之间的语义关系。
    //
    //                根据用户提供的表结构列表（JSON格式），请分析并推断以下本体对象：
    //
    //                ## 1. Link Type（关联关系）
    //                表之间的关联关系，有三种类型：
    //                - ObjectTypeForeignKeys (token=1): 通过外键关联，用于一对一或一对多关系。
    //                  判断依据：某表的列名为 xxx_id 且另一张表名为 xxx 且有 id 主键。
    //                - JoinTableDataset (token=2): 通过中间表关联，用于多对多关系。
    //                  判断依据：某表只有两个外键列组成联合主键，分别指向两张实体表。
    //                - BackingObjectType (token=3): 通过中间对象类型关联，用于带属性的多对多关系。
    //                  判断依据：某表有两个外键列但还有其他业务属性列。
    //
    //                ## 2. Shared Property（共享属性）
    //                多个表中出现的相同语义的属性，适合抽取为共享属性复用。
    //                判断依据：多张表中出现相同名称且相同类型的列（如 create_time, update_time, status, currency_code 等）。
    //                至少在2张表中出现才考虑抽取。
    //
    //                ## 3. Value Type（值类型 + 约束）
    //                列值有明确约束的属性，适合定义为值类型。
    //                判断依据：
    //                - 列注释中包含枚举值列表（如 "PENDING/PAID/SHIPPED"）→ Enum 约束
    //                - 列类型暗示范围约束（如 VARCHAR(3) 可能是国家代码）→ Range 约束
    //
    //                ## 4. Glossary（业务术语 / 同义词词典）
    //                业务术语字典，用于 ChatBI 自然语言到 SQL 的桥接，把用户口语化的业务名词映射到本体对象。
    //                有三种 target 类型：
    //                - GlossaryTargetOT: 业务实体名 → 某个 ObjectType。
    //                  判断依据：表名对应业务实体（如 customer/orders/products）。
    //                  示例：term="客户"，synonyms=["用户","User","buyer","购买方"]，target.targetType="GlossaryTargetOT"，target
    //                  .objectType="customer"
    //                - GlossaryTargetProperty: 业务字段名 → 某个 ObjectType 的某列。
    //                  判断依据：业务上有明确语义的列（如 amount/status/created_at）。
    //                  示例：term="订单金额"，synonyms=["金额","总额","订单总额"]，target.targetType="GlossaryTargetProperty"，target
    //                  .objectType="orders"，target.propertyName="amount"
    //                - GlossaryTargetMetricExpr: 业务指标 → 自定义 SQL 表达式。
    //                  判断依据：常见业务指标（如总销售额、活跃用户数、客单价）可由聚合 SQL 表达。
    //                  示例：term="总销售额"，synonyms=["销售总额","GMV"]，target.targetType="GlossaryTargetMetricExpr"，target
    //                  .sql="SUM(orders.amount)"
    //
    //                同义词请尽量覆盖：中文同义词、英文同义词、口语化表达、行业术语。
    //                优先从列注释/表名中提取业务名词，避免编造。
    //
    //                ## 输出要求
    //                请严格按照 response_format 中定义的 JSON Schema 格式输出。
    //                对于每个推断结果，请给出 confidence 字段（high/medium/low）表示置信度。
    //                - high: 有明确证据（如显式外键命名、注释中的枚举值）
    //                - medium: 基于命名约定推断（如 xxx_id 引用 xxx 表）
    //                - low: 基于经验猜测
    //                """;
    //    }

    private String buildSystemPrompt() {
        return """
                你是一个数据建模专家，擅长分析数据库表结构并推断表之间的语义关系。
                
                根据用户提供的表结构列表（JSON格式），请分析并推断以下本体对象：
                
                ## 1. Glossary（业务术语 / 同义词词典）
                业务术语字典，用于 ChatBI 自然语言到 SQL 的桥接，把用户口语化的业务名词映射到本体对象。
                有三种 target 类型：
                - GlossaryTargetOT: 业务实体名 → 某个 ObjectType。
                  判断依据：表名对应业务实体（如 customer/orders/products）。
                  示例：term="客户"，synonyms=["用户","User","buyer","购买方"]，target.targetType="GlossaryTargetOT"，target.objectType="customer"
                - GlossaryTargetProperty: 业务字段名 → 某个 ObjectType 的某列。
                  判断依据：业务上有明确语义的列（如 amount/status/created_at）。
                  示例：term="订单金额"，synonyms=["金额","总额","订单总额"]，target.targetType="GlossaryTargetProperty"，target.objectType="orders"，target.propertyName="amount"
                - GlossaryTargetMetricExpr: 业务指标 → 自定义 SQL 表达式。
                  判断依据：常见业务指标（如总销售额、活跃用户数、客单价）可由聚合 SQL 表达。
                  示例：term="总销售额"，synonyms=["销售总额","GMV"]，target.targetType="GlossaryTargetMetricExpr"，target.sql="SUM(orders.amount)"
                
                同义词请尽量覆盖：中文同义词、英文同义词、口语化表达、行业术语。
                优先从列注释/表名中提取业务名词，避免编造。
                
                ## 输出要求
                请严格按照 response_format 中定义的 JSON Schema 格式输出。
                对于每个推断结果，请给出 confidence 字段（high/medium/low）表示置信度。
                - high: 有明确证据（如显式外键命名、注释中的枚举值）
                - medium: 基于命名约定推断（如 xxx_id 引用 xxx 表）
                - low: 基于经验猜测
                """;
    }

    private TISJsonSchema buildOutputJsonSchema() {
        TISJsonSchema.Builder builder = TISJsonSchema.Builder.create("ontology_inference_result", Optional.empty());

        // linkTypes array
        builder.addProperty("linkTypes", TISJsonSchema.FieldType.Array, "推断出的关联关系列表")
                .setItems(buildLinkTypeItemSchema());
        //
        //        // sharedProperties array
        //        builder.addProperty("sharedProperties", TISJsonSchema.FieldType.Array, "推断出的共享属性列表")
        //                .setItems(buildSharedPropertyItemSchema());
        //
        // valueTypes array
        builder.addProperty("valueTypes", TISJsonSchema.FieldType.Array, "推断出的值类型列表")
                .setItems(buildValueTypeItemSchema());

        // glossaries array
        //        builder.addProperty("glossaries", TISJsonSchema.FieldType.Array, "推断出的业务术语列表")
        //                .setItems(buildGlossaryItemSchema());

        return builder.build();
        //        TISJsonSchema schema = ;
        //        StringBuilder buffer = new StringBuilder();
        //        schema.appendFieldDescToPrompt(buffer);
        //        System.out.println(buffer);
        //        return schema;
    }


    public static void main(String[] args) {
        DefaultGroovyShellFactory.setInConsoleModule();
        InferOntologyFromLLM infer = new InferOntologyFromLLM();
        infer.llm = "qwen1";
        String ontologyName = "falcon_14";
        PartialSettedPluginContext pluginContext = IPluginContext.namedContext(ontologyName);
        // pluginContext.setLoginUser()
        pluginContext.setLoginUser(() -> "admin");
        DefaultContext context = new DefaultContext();
        pluginContext.setContext(context);


        UploadPluginMeta pluginMeta = OntologyPluginMeta.createPluginMeta(UploadPluginMeta.create(Ontology.ONTOLOGY))
                .getDelegate().putExtraParams(NAME_ONTOLOGY_DOMAIN, ontologyName);

        //  context.put(UploadPluginMeta.KEY_PLUGIN_META, pluginMeta);
        UploadPluginMeta.putPluginMeta(context, pluginMeta);

        // infer.manipuldateProcess(pluginContext, pluginMeta, Optional.of(context));

        //  TISJsonSchema schema = infer.buildOutputJsonSchema();

        // System.out.println(JsonUtil.toString(schema.root()));

        DescriptorsJSONForAIPrompt descriptorsJSON =
                new DescriptorsJSONForAIPrompt<>(Collections.singletonList(new OntologyLinker.DefaultDesc
                        ()), true);

        DescriptorsMeta descMeta
                = descriptorsJSON.getDescriptorsJSON();

        for (Map.Entry<String /* concrete plugin implement class */, ITISJsonSchema> entry :
                descMeta.getPluginJsonSchema().entrySet()) {

            System.out.println(JsonUtil.toString(entry.getValue().root()));
        }
    }

    /**
     *
     * @return
     * @see OntologyLinker
     */
    private ITISJsonSchema buildLinkTypeItemSchema() {

        DescriptorsJSONForAIPrompt descriptorsJSON =
                new DescriptorsJSONForAIPrompt<>(Collections.singletonList(new OntologyLinker.DefaultDesc()),
                        true);
        DescriptorsMeta meta = descriptorsJSON.getDescriptorsJSON();
        ITISJsonSchema schema = meta.getFirstPluginJsonSchema();//.getPluginJsonSchema().values().iterator().next();
        System.out.println(JsonUtil.toString(schema.root()));
        return schema;
        //        TISJsonSchema.Builder b = TISJsonSchema.Builder.create("linkTypeItem", Optional.of("linkTypes"));
        //        b.addProperty("sourceObjectType", TISJsonSchema.FieldType.String, "源对象类型（表名）");
        //        b.addProperty("sourceField", TISJsonSchema.FieldType.String, "源表关联字段");
        //        b.addProperty("targetObjectType", TISJsonSchema.FieldType.String, "目标对象类型（表名）");
        //        b.addProperty("targetField", TISJsonSchema.FieldType.String, "目标表关联字段");
        //        b.addProperty("relationshipType", TISJsonSchema.FieldType.String, "关系类型")
        //                .setValEnums("ObjectTypeForeignKeys", "JoinTableDataset", "BackingObjectType");
        //        b.addProperty("confidence", TISJsonSchema.FieldType.String, "置信度")
        //                .setValEnums("high", "medium", "low");
        //        b.addProperty("reason", TISJsonSchema.FieldType.String, "推断理由");
        //        return b.build();
    }

    /**
     *
     * @return
     * @see OntologySharedProperty
     */
    private ITISJsonSchema buildSharedPropertyItemSchema() {

        DescriptorsJSONForAIPrompt descriptorsJSON =
                new DescriptorsJSONForAIPrompt<>(Collections.singletonList(new OntologySharedProperty.DefaultDesc()),
                        true);
        DescriptorsMeta meta = descriptorsJSON.getDescriptorsJSON();
        return meta.getPluginJsonSchema().values().iterator().next();


        //        TISJsonSchema.Builder b = TISJsonSchema.Builder.create("sharedPropertyItem", Optional.of
        //        ("sharedProperties"));
        //        b.addProperty("name", TISJsonSchema.FieldType.String, "共享属性名称");
        //        b.addProperty("description", TISJsonSchema.FieldType.String, "属性描述");
        //        b.addProperty("ontologyType", TISJsonSchema.FieldType.String, "属性类型（对应 OntologyType 枚举名）");
        //        b.addProperty("usedByTables", TISJsonSchema.FieldType.Array, "使用该属性的表名列表")
        //                .setItems(TISJsonSchema.FieldType.String);
        //        b.addProperty("confidence", TISJsonSchema.FieldType.String, "置信度")
        //                .setValEnums("high", "medium", "low");
        //        return b.build();
    }

    /**
     * 利用 {@link DescriptorsJSONForAIPrompt} 自动生成 {@link OntologyValueType} 的 schema。
     * 由于 {@link OntologyValueType} 实现 {@link com.qlangtech.tis.extension.MultiStepsSupportHost}，
     * 自动生成的 schema 形如 <code>{ impl, vals:{ multiStepsSavedItems:[{impl,vals},{impl,vals}] } }</code>，
     * 与 {@link com.qlangtech.tis.extension.OneStepOfMultiSteps#parseStepsPlugin} 期望的反序列化格式天然对齐。
     * 外层再平铺 sourceColumn / confidence / reason 三个推断元数据字段。
     *
     * @return
     * @see OntologyValueType
     */
    private ITISJsonSchema buildValueTypeItemSchema() {

        DescriptorsJSONForAIPrompt descriptorsJSON =
                new DescriptorsJSONForAIPrompt<>(Collections.singletonList(new OntologyValueType.DefaultDesc()), true
                        , (b, desc) -> {
                    // 推断元数据
                    b.addProperty("sourceColumn", TISJsonSchema.FieldType.String, "来源列（表名.列名）");
                    b.addProperty("confidence", TISJsonSchema.FieldType.String, "置信度")
                            .setValEnums("high", "medium", "low");
                    b.addProperty("reason", TISJsonSchema.FieldType.String, "推断理由");
                }, (attr, addedProp) -> false);

        DescriptorsMeta meta
                = descriptorsJSON.getDescriptorsJSON();

        // host schema 形如 { impl, vals:{ multiStepsSavedItems:[...] } }
        ITISJsonSchema hostSchema = meta.getFirstPluginJsonSchema();
        return hostSchema;

        //        TISJsonSchema.Builder b = TISJsonSchema.Builder.create("valueTypeItem", Optional.of("valueTypes"));
        //
        //        // 把 host schema 的 properties (impl, vals) 平铺到外层
        //        JSONObject hostProps = hostSchema.schema().getJSONObject(TISJsonSchema.SCHEMA_PROPERTIES);
        //        for (String key : hostProps.keySet()) {
        //            b.addRawProperty(key, hostProps.getJSONObject(key), true);
        //        }
        //
        //
        //        return b.build();
    }

    /**
     * @see OntologyGlossary
     */
    private ITISJsonSchema buildGlossaryItemSchema() {
        //        TISJsonSchema.Builder b = TISJsonSchema.Builder.create("glossaryItem", Optional.of("glossaries"));
        //        b.addProperty("term", TISJsonSchema.FieldType.String, "业务术语");
        //        b.addProperty("synonyms", TISJsonSchema.FieldType.Array, "同义词列表（中文/英文/口语化表达）")
        //                .setItems(TISJsonSchema.FieldType.String);
        //        b.addProperty("description", TISJsonSchema.FieldType.String, "术语描述");
        //        b.addObjectProperty("target", (target) -> {
        //            target.addProperty("targetType", TISJsonSchema.FieldType.String, "Target 类型")
        //                    .setValEnums("GlossaryTargetOT", "GlossaryTargetProperty", "GlossaryTargetMetricExpr");
        //            target.addProperty("objectType", TISJsonSchema.FieldType.String,
        //                    "ObjectType 名称（targetType=OT 或 Property 时填写，MetricExpr 时填空字符串）");
        //            target.addProperty("propertyName", TISJsonSchema.FieldType.String,
        //                    "列名（targetType=Property 时填写，其它情况填空字符串）");
        //            target.addProperty("sql", TISJsonSchema.FieldType.String,
        //                    "SQL 表达式（targetType=MetricExpr 时填写，其它情况填空字符串）");
        //        });
        //        b.addProperty("confidence", TISJsonSchema.FieldType.String, "置信度")
        //                .setValEnums("high", "medium", "low");
        //        return b.build();

        DescriptorsJSONForAIPrompt descriptorsJSON =
                new DescriptorsJSONForAIPrompt<>(Collections.singletonList(new OntologyGlossary.DefaultDesc()), true,
                        (builder, desc) -> {
                            builder.addProperty("confidence", TISJsonSchema.FieldType.String, "置信度")
                                    .setValEnums("high", "medium", "low");
                        }, (attr, addedProp) -> false);

        DescriptorsMeta meta = descriptorsJSON.getDescriptorsJSON();

        ITISJsonSchema schema = meta.getFirstPluginJsonSchema();
        StringBuilder prompt = new StringBuilder();
        schema.appendFieldDescToPrompt(prompt);
        System.out.println(prompt);
        return schema;

        //        for (Map.Entry<String /* concrete plugin implement class */, Pair<TISJsonSchema, Descriptor>> entry :
        //                descriptorsJSON1.descSchemaRegister.entrySet()) {
        //            return entry.getValue().getKey();
        //        }
        //
        //        throw new IllegalStateException("can not find glossary schema");
    }

    /**
     * 把 LLM 返回 JSON 中的 primitive 值（Number / Boolean 等）递归包装成
     * <code>{_primaryVal: val}</code>，使其与 {@link com.qlangtech.tis.util.impl.AttrVals#parseAttrValMap}
     * 期望的 TIS 表单格式一致。
     * <p>
     * 必要性：{@code AttrVals#parseAttrValMap} 仅对 {@link String} 值的 cast 失败做自动包装兜底，
     * 对 Integer / Boolean 等会直接抛 RuntimeException。{@link OntologyValueType} 的 step1
     * {@link com.qlangtech.tis.plugin.ontology.impl.valuetype.MetadataOfValueType#type} 是 int，
     * 必现该问题。
     * <p>
     * 适用范围：JSONObject 内部所有非 String / 非容器的 primitive 字段；JSONArray 元素递归处理。
     * String 留给 {@code parseAttrValMap} 的 catch 分支兜底，不动。
     */
    static void normalizeValsForReparse(Object node) {
        if (node instanceof JSONObject) {
            JSONObject obj = (JSONObject) node;
            for (String key : new HashSet<>(obj.keySet())) {
                Object v = obj.get(key);
                if (v instanceof JSONObject || v instanceof JSONArray) {
                    normalizeValsForReparse(v);
                } else if (v != null && !(v instanceof String)) {
                    JSONObject wrapped = new JSONObject();
                    wrapped.put(Descriptor.KEY_primaryVal, v);
                    obj.put(key, wrapped);
                }
                // String 由 AttrVals.parseAttrValMap 的 catch 分支自动包装
            }
        } else if (node instanceof JSONArray) {
            JSONArray arr = (JSONArray) node;
            for (int i = 0; i < arr.size(); i++) {
                normalizeValsForReparse(arr.get(i));
            }
        }
    }


    @TISExtension
    public static final class DftDesc extends OntologyDomainManipulate.BasicDesc implements IEndTypeGetter {
        public DftDesc() {
            super();
            //            List<Pair<OntologyDomain, IPluginStore<OntologyDomain>>> domainList = OntologyDomain
            //            .getDoaminList();
            //            List<OntologyDomain> domains = domainList.stream().map(Pair::getKey).toList();
            //            this.registerSelectOptions("ontologyDomain", () -> domains);
            this.registerSelectOptions(KEY_FIELD_LLM_NAME, LLMProvider::getExistProviders);
        }

        @Override
        public String getDisplayName() {
            return "Infer Ontology From LLM";
        }

        @Override
        public EndType getEndType() {
            return EndType.Ontology;
        }
    }
}
