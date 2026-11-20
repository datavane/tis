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
package com.qlangtech.tis.util;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.impl.DefaultContext;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.datax.job.SSEEventWriter;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.plugin.ValidatorCommons;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import java.util.ArrayList;
import java.util.List;

import static com.qlangtech.tis.extension.Descriptor.KEY_primaryVal;
import static com.qlangtech.tis.util.AttrValMap.PLUGIN_EXTENSION_IMPL;
import static com.qlangtech.tis.util.AttrValMap.PLUGIN_EXTENSION_VALS;

import static org.junit.Assert.*;
import org.junit.Test;
/**
 * {@code RelevantFieldsContextAttrValMap#createRelevantFieldsContext(...)} 的契约测试。
 *
 * <p>这个方法做的是「把插件属性集裁剪成只含与本次级联相关的字段」，前后两段（validate /
 * createDescribable）用的是<b>同一个</b>按引用共享的 key 列表。这类失效全是静默的：
 * 白名单漏字段 → 该字段读成 null；列表被拷贝 → 追加的字段不可见；两者都不报错。
 *
 * <p>因此这里自带一个最小插件（必填 + 非必填 + 一条级联管道），把四条契约逐条钉住：
 * <ol>
 *   <li>未进白名单的<b>必填</b>字段不参与校验（否则级联子表单会被其它未填字段误报）；</li>
 *   <li>白名单内的必填字段缺值 → 校验不过，且错误定位到该字段；</li>
 *   <li>{@code validate()} 之后、{@code createDescribable()} 之前往 key 列表里追加的字段
 *       必须对实例化可见 —— 这是「不做防御性拷贝」那条契约的回归锚点；</li>
 *   <li>{@link RelevantFieldsContextAttrValMap.RelevantFieldKeys} 的两阶段收集与
 *       {@code createRelevantFieldsContext} 的引用契约配套。</li>
 * </ol>
 *
 * <p><b>运行要求</b>：需要 {@code --add-opens java.base/java.lang=ALL-UNNAMED}
 * （TIS 扩展扫描 {@code ClassLoaderReflectionToolkit}），与工程里其它扫 descriptor 的用例一致。
 */
public class TestRelevantFieldsContextAttrValMap {

    private static final String KEY_REQUIRED = "requiredField";
    private static final String KEY_OPTIONAL = "optionalField";
    private static final String KEY_CASCADE_FROM = "cascadeFrom";
    private static final String KEY_CASCADE_TO = "cascadeTo";

    /**
     * 未进白名单的必填字段不参与校验。
     *
     * <p>这正是本方法存在的理由：前端做级联取值时只提交表单子集，若拿整张表单的属性集去校验，
     * 所有尚未填写的必填字段都会误报。
     */
    @Test
    public void testFieldsOutsideWhitelistDoNotTakePartInValidation() {
        CapturingMsgHandler msgHandler = new CapturingMsgHandler();
        Context context = new DefaultContext();

        // 一个字段都不提交：requiredField 必填却没值，但它不在白名单里
        RelevantFieldsContextAttrValMap<RelevantFieldsProbe> ctx = createContext(msgHandler, context,
                postContent(new JSONObject()), validatePhaseKeys(KEY_OPTIONAL));

        Descriptor.PluginValidateResult result = ctx.validate();

        assertTrue("白名单外的必填字段不得参与校验，实际错误：" + msgHandler.fieldErrors,
                result.isValid());
        assertFalse("不应产生任何字段级错误", msgHandler.hasFieldError());
        assertNull("白名单外的字段也不应被收集进实例", ctx.createPluginInstance().requiredField);
    }

    /**
     * 白名单内的必填字段缺值 → 校验不过，且错误定位到该字段本身。
     */
    @Test
    public void testRequiredFieldInWhitelistIsValidated() {
        CapturingMsgHandler msgHandler = new CapturingMsgHandler();
        Context context = new DefaultContext();

        RelevantFieldsContextAttrValMap<RelevantFieldsProbe> ctx = createContext(msgHandler, context,
                postContent(new JSONObject()), validatePhaseKeys(KEY_REQUIRED));

        Descriptor.PluginValidateResult result = ctx.validate();

        assertFalse("白名单内的必填字段缺值必须校验不过", result.isValid());
        FieldErr err = msgHandler.firstFieldError(KEY_REQUIRED);
        assertNotNull("错误应定位到 " + KEY_REQUIRED + "，实际：" + msgHandler.fieldErrors, err);
        assertEquals("缺值提示应使用统一文案", ValidatorCommons.MSG_EMPTY_INPUT_ERROR, err.msg);
    }

    /**
     * 白名单内的字段有值时正常通过，并且只读取白名单内的字段。
     */
    @Test
    public void testOnlyWhitelistedFieldsAreReadIntoInstance() {
        CapturingMsgHandler msgHandler = new CapturingMsgHandler();
        Context context = new DefaultContext();

        JSONObject vals = new JSONObject();
        vals.put(KEY_REQUIRED, primaryVal("required-value"));
        vals.put(KEY_OPTIONAL, primaryVal("optional-value"));

        RelevantFieldsContextAttrValMap<RelevantFieldsProbe> ctx = createContext(msgHandler, context,
                postContent(vals), validatePhaseKeys(KEY_REQUIRED));

        assertTrue("只校验白名单字段，有值则应通过：" + msgHandler.fieldErrors, ctx.validate().isValid());

        RelevantFieldsProbe instance = ctx.createPluginInstance();
        assertEquals("requiredField 在白名单内，必须被读进来", "required-value", instance.requiredField);
        assertNull("optionalField 不在白名单内，必须保持 null（漏进白名单就是这条断言失败）",
                instance.optionalField);
    }

    /**
     * 核心契约：key 列表<b>按引用</b>使用 —— {@code validate()} 之后追加的字段，{@code createDescribable()}
     * 必须同样可见。
     *
     * <p>走的是真实调用路径 {@link RelevantFieldsContextAttrValMap.RelevantFieldKeys#addToFieldNewInstanceRelevantKeys()}：
     * 级联的目标字段在校验阶段还不该参与（它此刻还没有值），只有实例化阶段才需要。若把
     * {@code createRelevantFieldsContext} 改成先拷贝一份 key 集合，追加会<b>静默失效</b> ——
     * 实例上取不到目标字段值，且不报错。
     */
    @Test
    public void testKeysAppendedAfterValidateAreVisibleAtCreateTime() {
        CapturingMsgHandler msgHandler = new CapturingMsgHandler();
        Context context = new DefaultContext();

        JSONObject vals = new JSONObject();
        vals.put(KEY_CASCADE_FROM, primaryVal("cascade-from-value"));
        // 目标字段本身是有值的：上游只提交「相关字段」，但值都在请求体里
        vals.put(KEY_CASCADE_TO, primaryVal("cascade-target-value"));

        // 默认两阶段：apply() 收级联主字段 cascadeFrom；addToFieldNewInstanceRelevantKeys() 再追加 cascadeTo
        RelevantFieldsContextAttrValMap.RelevantFieldKeys keysCreator
                = new RelevantFieldsContextAttrValMap.RelevantFieldKeys(KEY_CASCADE_TO);
        RelevantFieldsContextAttrValMap<RelevantFieldsProbe> ctx = createContext(msgHandler, context,
                postContent(vals), keysCreator);

        Descriptor.PluginValidateResult result = ctx.validate();
        assertTrue("级联主字段齐备时应通过校验：" + msgHandler.fieldErrors, result.isValid());
        assertEquals("校验阶段白名单里只有级联主字段",
                List.of(KEY_CASCADE_FROM + "(required)"), fieldsOf(keysCreator.relevantFieldKeys));

        // 校验通过之后追加目标字段 —— 这一步是调用方的动作，不是本方法做的
        keysCreator.addToFieldNewInstanceRelevantKeys();

        RelevantFieldsProbe instance = ctx.createPluginInstance();
        assertEquals("validate() 之后追加的字段必须对 createDescribable() 可见；"
                        + "若失败说明 createRelevantFieldsContext 把 key 集合拷贝了一份"
                        + "（防御性拷贝会让追加静默失效）",
                "cascade-target-value", instance.cascadeTo);
        assertEquals("级联主字段同样要读进来", "cascade-from-value", instance.cascadeFrom);
    }

    /**
     * 反证：字段只存在于请求体、但从未进过 key 列表时，实例上必须是 null。
     *
     * <p>与上一个用例配对使用 —— 否则「追加后可见」也可能是因为白名单根本没生效（全量字段都被读取），
     * 那样上一条断言会假通过。
     */
    @Test
    public void testFieldNeverAddedToKeysStaysNull() {
        CapturingMsgHandler msgHandler = new CapturingMsgHandler();
        Context context = new DefaultContext();

        JSONObject vals = new JSONObject();
        vals.put(KEY_CASCADE_FROM, primaryVal("cascade-from-value"));
        vals.put(KEY_CASCADE_TO, primaryVal("cascade-target-value"));

        // 只跑第一阶段：绝不调用 addToFieldNewInstanceRelevantKeys()
        RelevantFieldsContextAttrValMap<RelevantFieldsProbe> ctx = createContext(msgHandler, context,
                postContent(vals), validatePhaseKeys(KEY_CASCADE_FROM));

        assertTrue("当前白名单内字段齐备，应通过校验", ctx.validate().isValid());

        RelevantFieldsProbe instance = ctx.createPluginInstance();
        assertNull("请求体里有值但没进 key 列表，实例上必须为 null（说明白名单确实在裁剪）",
                instance.cascadeTo);
        assertEquals("进了白名单的主字段照常读入", "cascade-from-value", instance.cascadeFrom);
    }

    // ===================================================================
    //  实例化阶段放宽必填：RelevantFieldKey.fieldValRequired=false 的落地
    // ===================================================================

    /**
     * 第二阶段追加的字段会以 {@code fieldValRequired=false} 进入白名单。若该字段本身声明了
     * {@code Validator.require}，{@code getKVTuples()} 会把它的 {@code isInputRequired()} 用
     * {@code AdapterPropertyType} 掰成 false —— 因为这一路的语义是「用于回显已选项，
     * 有值就带上，没值也不该报错」。
     *
     * <p>本用例是那个包装分支的<b>正向触发</b>：{@code requiredField} 是必填字段，
     * 但在第二阶段才进白名单且请求体里没有值，实例化必须成功且该字段为 null，
     * 而不是抛 {@code prop:requiredField can not be empty}。
     */
    @Test
    public void testInstancePhaseKeyRelaxesRequiredFieldWhenValueAbsent() {
        CapturingMsgHandler msgHandler = new CapturingMsgHandler();
        Context context = new DefaultContext();

        // 刻意不提交 requiredField：它要在「实例化阶段」以非必填身份进白名单
        JSONObject vals = new JSONObject();
        vals.put(KEY_CASCADE_FROM, primaryVal("cascade-from-value"));

        RelevantFieldsContextAttrValMap.RelevantFieldKeys keysCreator = instancePhaseAdds(KEY_REQUIRED);
        RelevantFieldsContextAttrValMap<RelevantFieldsProbe> ctx = createContext(msgHandler, context,
                postContent(vals), keysCreator);

        assertTrue("校验阶段白名单里没有 requiredField，不应报缺值：" + msgHandler.fieldErrors,
                ctx.validate().isValid());

        keysCreator.addToFieldNewInstanceRelevantKeys();
        assertEquals("第二阶段追加的 key 必须标为 optional（正是它触发 isInputRequired 的放宽）",
                List.of(KEY_CASCADE_FROM + "(required)", KEY_REQUIRED + "(optional)"),
                fieldsOf(keysCreator.relevantFieldKeys));

        // 放宽分支若失效，这里会抛 IllegalStateException("prop:requiredField can not be empty,...")，
        // 直接冒泡成 error 看不出是哪条契约破了，所以显式接住并给出意图
        RelevantFieldsProbe instance;
        try {
            instance = ctx.createPluginInstance();
        } catch (IllegalStateException e) {
            fail("必填字段在实例化阶段被放宽，缺值时不应抛异常，实际：" + e.getMessage());
            return;
        }
        assertNull("必填字段在实例化阶段被放宽，缺值时应为 null", instance.requiredField);
        assertEquals("其余字段照常读入", "cascade-from-value", instance.cascadeFrom);
    }

    /**
     * 同上，但请求体里<b>有值</b>：放宽只影响「缺值是否报错」，不影响取值本身。
     *
     * <p>这条与上一条配对，确保 {@code AdapterPropertyType} 包装后字段仍能被正常赋值
     * （包装只覆写 {@code isInputRequired()}，其余全部委托给原 PropertyType）。
     */
    @Test
    public void testInstancePhaseKeyStillReadsValueWhenPresent() {
        CapturingMsgHandler msgHandler = new CapturingMsgHandler();
        Context context = new DefaultContext();

        JSONObject vals = new JSONObject();
        vals.put(KEY_CASCADE_FROM, primaryVal("cascade-from-value"));
        vals.put(KEY_REQUIRED, primaryVal("required-value"));

        RelevantFieldsContextAttrValMap.RelevantFieldKeys keysCreator = instancePhaseAdds(KEY_REQUIRED);
        RelevantFieldsContextAttrValMap<RelevantFieldsProbe> ctx = createContext(msgHandler, context,
                postContent(vals), keysCreator);

        assertTrue(ctx.validate().isValid());
        keysCreator.addToFieldNewInstanceRelevantKeys();

        RelevantFieldsProbe instance = ctx.createPluginInstance();
        assertEquals("放宽必填只影响是否报错，有值时仍然要读进来", "required-value", instance.requiredField);
    }

    /**
     * 对照：同一个必填字段走<b>第一阶段</b>（{@code fieldValRequired=true}）时不被放宽，
     * 缺值必须照常报错。
     *
     * <p>没有这条，上面两条可能因为「包装根本没生效也没关系」而假通过 ——
     * 它证明放宽与否确实取决于 {@code fieldValRequired}，而不是字段本身的注解。
     */
    @Test
    public void testValidatePhaseKeyKeepsRequiredFieldStrict() {
        CapturingMsgHandler msgHandler = new CapturingMsgHandler();
        Context context = new DefaultContext();

        RelevantFieldsContextAttrValMap<RelevantFieldsProbe> ctx = createContext(msgHandler, context,
                postContent(new JSONObject()), validatePhaseKeys(KEY_REQUIRED));

        assertFalse("第一阶段收集的必填字段不得被放宽", ctx.validate().isValid());
        assertNotNull("应照常报缺值错误", msgHandler.firstFieldError(KEY_REQUIRED));
    }

    // ===================================================================
    //  契约来源：RelevantFieldKeys
    // ===================================================================

    /**
     * {@link RelevantFieldsContextAttrValMap.RelevantFieldKeys#addToFieldNewInstanceRelevantKeys()} 与
     * {@code createRelevantFieldsContext} 的引用契约是配套的：两阶段用的是同一个 list 实例，
     * 才能让「校验后再追加」这条路走通。
     *
     * <p>这里同时覆盖 {@code toFieldValiateRelevantKeysCreator} 的默认实现
     * （{@code descriptor.getValueChangeFromFieldKeys(toPropertyKey)}）——
     * 级联主字段由 descriptor 上登记的 {@code valueChangePipe} 决定。
     */
    @Test
    public void testRelevantFieldKeysTwoStageCollectsIntoSharedList() {
        Descriptor<RelevantFieldsProbe> desc = new RelevantFieldsProbe.DefaultDescriptor();

        // 默认实现：从 descriptor 的 valueChangePipe 反查指向 targetField 的 from 字段
        RelevantFieldsContextAttrValMap.RelevantFieldKeys cascadeKeys
                = new RelevantFieldsContextAttrValMap.RelevantFieldKeys(KEY_CASCADE_TO);
        List<String> validatePhaseKeys = cascadeKeys.apply(desc);
        assertEquals("apply() 的返回值是校验阶段的字段名快照", List.of(KEY_CASCADE_FROM), validatePhaseKeys);

        // 累计列表里记的是 RelevantFieldKey（字段名 + 是否必须传值）
        assertEquals("校验阶段收集的主字段必须标为 required",
                List.of(KEY_CASCADE_FROM + "(required)"), fieldsOf(cascadeKeys.relevantFieldKeys));

        cascadeKeys.addToFieldNewInstanceRelevantKeys();
        assertEquals("第二阶段应在同一个 list 上追加目标字段本身（顺序：主字段 → 目标字段）",
                List.of(KEY_CASCADE_FROM + "(required)", KEY_CASCADE_TO + "(optional)"),
                fieldsOf(cascadeKeys.relevantFieldKeys));
    }

    /**
     * {@code RelevantFieldKeys} 允许子类改写两阶段的收集范围；改写后同样必须落在同一个 list 上。
     */
    @Test
    public void testRelevantFieldKeysSubclassOverrides() {
        RelevantFieldsContextAttrValMap.RelevantFieldKeys custom
                = new RelevantFieldsContextAttrValMap.RelevantFieldKeys(KEY_CASCADE_TO) {
            @Override
            protected List<String> toFieldValiateRelevantKeysCreator(Descriptor descriptor) {
                return List.of(KEY_REQUIRED, KEY_OPTIONAL);
            }

            @Override
            protected List<String> toFieldNewInstanceRelevantKeysCreator() {
                return List.of(KEY_CASCADE_TO, KEY_CASCADE_FROM);
            }
        };

        List<String> collected = custom.apply(new RelevantFieldsProbe.DefaultDescriptor());
        assertEquals("apply() 返回子类自定义的校验阶段字段名", List.of(KEY_REQUIRED, KEY_OPTIONAL), collected);
        assertEquals("校验阶段收集的字段一律标 required",
                List.of(KEY_REQUIRED + "(required)", KEY_OPTIONAL + "(required)"),
                fieldsOf(custom.relevantFieldKeys));

        custom.addToFieldNewInstanceRelevantKeys();
        assertEquals("实例化阶段追加的字段一律标 optional",
                List.of(KEY_REQUIRED + "(required)", KEY_OPTIONAL + "(required)",
                        KEY_CASCADE_TO + "(optional)", KEY_CASCADE_FROM + "(optional)"),
                fieldsOf(custom.relevantFieldKeys));
    }

    /**
     * {@code RelevantFieldKeys} 第二阶段是累积而不是覆盖：重复调用会把内容追加两次。
     *
     * <p>钉住这一现状是为了防止「顺手加个去重/重置」而改变调用方语义 ——
     * 白名单按字段名过滤，重复项没有副作用；但一旦误改成「重置」，
     * 第一阶段收集的主字段会被丢掉，级联校验就会失效。
     */
    @Test
    public void testRelevantFieldKeysAddIsCumulative() {
        RelevantFieldsContextAttrValMap.RelevantFieldKeys keys
                = new RelevantFieldsContextAttrValMap.RelevantFieldKeys(KEY_CASCADE_TO);
        keys.apply(new RelevantFieldsProbe.DefaultDescriptor());
        keys.addToFieldNewInstanceRelevantKeys();
        keys.addToFieldNewInstanceRelevantKeys();

        assertEquals("第二阶段是追加语义（不做去重/重置），主字段必须仍在列表里",
                List.of(KEY_CASCADE_FROM + "(required)",
                        KEY_CASCADE_TO + "(optional)", KEY_CASCADE_TO + "(optional)"),
                fieldsOf(keys.relevantFieldKeys));
    }

    // ===================================================================
    //  helpers
    // ===================================================================

    private static <T extends Describable> RelevantFieldsContextAttrValMap<T> createContext(
            IControlMsgHandler msgHandler, Context context, JSONObject postContent,
            RelevantFieldsContextAttrValMap.RelevantFieldKeys keysCreator) {
        return RelevantFieldsContextAttrValMap.createRelevantFieldsContext(msgHandler, context, postContent,
                keysCreator);
    }

    /**
     * 把累积的 key 列表摊成 {@code 字段名(required|optional)}，让两阶段语义在断言里直接可读。
     */
    private static List<String> fieldsOf(List<RelevantFieldsContextAttrValMap.RelevantFieldKey> keys) {
        return keys.stream()
                .map((k) -> k.getFieldKey() + (k.isFieldValRequired() ? "(required)" : "(optional)"))
                .toList();
    }

    /**
     * 只声明「校验阶段」白名单的创建器：覆写第一阶段收集器直接给出指定字段，
     * 第二阶段返回空 —— 供那些只想钉住「白名单裁剪」、不关心级联收集的用例使用。
     */
    private static RelevantFieldsContextAttrValMap.RelevantFieldKeys validatePhaseKeys(String... keys) {
        List<String> fields = List.of(keys);
        return new RelevantFieldsContextAttrValMap.RelevantFieldKeys(KEY_CASCADE_TO) {
            @Override
            protected List<String> toFieldValiateRelevantKeysCreator(Descriptor descriptor) {
                return fields;
            }

            @Override
            protected List<String> toFieldNewInstanceRelevantKeysCreator() {
                return List.of();
            }
        };
    }

    /**
     * 第一阶段给 {@link #KEY_CASCADE_FROM}，第二阶段再追加指定字段 ——
     * 用于触发「实例化阶段追加的 key 标 {@code fieldValRequired=false}」那条分支。
     */
    private static RelevantFieldsContextAttrValMap.RelevantFieldKeys instancePhaseAdds(String... instanceKeys) {
        List<String> fields = List.of(instanceKeys);
        return new RelevantFieldsContextAttrValMap.RelevantFieldKeys(KEY_CASCADE_TO) {
            @Override
            protected List<String> toFieldValiateRelevantKeysCreator(Descriptor descriptor) {
                return List.of(KEY_CASCADE_FROM);
            }

            @Override
            protected List<String> toFieldNewInstanceRelevantKeysCreator() {
                return fields;
            }
        };
    }

    /**
     * 前端投递的线格式：{@code {impl, vals:{k:{_primaryVal:v}}}}。
     * <p>
     * {@code impl} 取插件类名而非 descriptor 类名：{@link Descriptor#getId()} 返回的是
     * descriptor 所描述的那个插件类。
     */
    private static JSONObject postContent(JSONObject vals) {
        JSONObject post = new JSONObject();
        post.put(PLUGIN_EXTENSION_IMPL, RelevantFieldsProbe.class.getName());
        post.put(PLUGIN_EXTENSION_VALS, vals);
        return post;
    }

    private static JSONObject primaryVal(Object value) {
        JSONObject item = new JSONObject();
        item.put(KEY_primaryVal, value);
        return item;
    }

    private record FieldErr(String field, String msg) {
    }

    /**
     * 只记字段级错误的最小 handler。
     *
     * <p>不继承 {@code DefaultFieldErrorHandler}：它的 {@code addFieldError} 是 {@code final}，
     * 无法在子类里截获；而本测试只需要「哪个字段、什么文案」这两项。
     */
    private static class CapturingMsgHandler implements IControlMsgHandler {
        final List<FieldErr> fieldErrors = new ArrayList<>();

        @Override
        public void addFieldError(Context context, String fieldName, String msg, Object... params) {
            this.fieldErrors.add(new FieldErr(fieldName, msg));
        }

        boolean hasFieldError() {
            return !fieldErrors.isEmpty();
        }

        /**
         * 按名字匹配而非整串相等：错误键可能是带下标的路径（如 {@code field[0].x}）。
         */
        FieldErr firstFieldError(String fieldName) {
            return fieldErrors.stream()
                    .filter((e) -> e.field.equals(fieldName) || e.field.startsWith(fieldName + "["))
                    .findFirst().orElse(null);
        }

        @Override
        public boolean validateBizLogic(BizLogic logicType, Context context, String fieldName, String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BasicPipelineValidator getPipelineValidator(BizLogic logicType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public SSEEventWriter getEventStreamWriter() {
            throw new UnsupportedOperationException();
        }

        @Override
        public DataXName getCollectionName() {
            return null;
        }

        @Override
        public void errorsPageShow(Context context) {
        }

        @Override
        public void addActionMessage(Context context, String msg) {
        }

        @Override
        public void setBizResult(Context context, Object result, boolean overwriteable) {
        }

        @Override
        public void addErrorMessage(Context context, String msg) {
        }
    }

    /**
     * 测试用插件：一个必填、一个非必填、一条级联管道。
     *
     * <p>descriptor 写成静态内嵌类，是为了让 {@link Descriptor} 能按
     * 「外层类」推导出 {@code clazz}（{@link Descriptor#getId()} 因此返回本类名），
     * 也正是 {@code AttrValMap.parseDescribableMap} 按 impl 反查 descriptor 的前提。
     */
    public static class RelevantFieldsProbe implements Describable<RelevantFieldsProbe> {
        @FormField(ordinal = 0, type = FormFieldType.INPUTTEXT, validate = {Validator.require})
        public String requiredField;

        @FormField(ordinal = 1, type = FormFieldType.INPUTTEXT)
        public String optionalField;

        @FormField(ordinal = 2, type = FormFieldType.INPUTTEXT)
        public String cascadeFrom;

        @FormField(ordinal = 3, type = FormFieldType.INPUTTEXT)
        public String cascadeTo;

        @TISExtension
        public static class DefaultDescriptor extends Descriptor<RelevantFieldsProbe> {
            public DefaultDescriptor() {
                // 级联主字段 cascadeFrom → 目标字段 cascadeTo
                this.valueChangePipe(KEY_CASCADE_FROM, KEY_CASCADE_TO);
            }
        }
    }
}
