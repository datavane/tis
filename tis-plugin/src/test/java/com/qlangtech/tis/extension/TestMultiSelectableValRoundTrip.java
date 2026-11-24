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
package com.qlangtech.tis.extension;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.citrus.turbine.impl.DefaultContext;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.datax.job.SSEEventWriter;
import com.qlangtech.tis.extension.impl.PropertyType;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.ds.CMeta;
import com.qlangtech.tis.plugin.ds.ElementCreatorFactory;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.util.RelevantFieldsContextAttrValMap;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.qlangtech.tis.util.AttrValMap.PLUGIN_EXTENSION_IMPL;
import static com.qlangtech.tis.util.AttrValMap.PLUGIN_EXTENSION_VALS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * {@code MULTI_SELECTABLE}（IdList 视图）字段在<b>实例化回填</b>时的取值身份契约测试。
 *
 * <p>执行点是 {@code Descriptor#buildPluginInstance} 的 MULTI_SELECTABLE 分支：当
 * {@code SelectedItem} 上没有携带 cmeta（IdList 视图必然如此，元组视图才由
 * {@code ElementCreatorFactory} 物化出带类型的元素）时，兜底 new 出的 {@link CMeta}
 * 的 {@code name} 必须取 {@code item.identityValue()}（线格式 {@code val}，即选项身份），
 * 而<b>不是</b> {@code item.getName()}（{@code label}，给人看的显示文案）。
 *
 * <p>为什么必须钉住：一旦改回 label，失效是<b>静默</b>的 ——
 * <ol>
 *   <li>GET 回显时 {@code ViewFormatType.IdList} 把存储值当 {@code val} 和选项比对勾选态，
 *       存了 label 的项永远对不上，保存后重开表单勾选全丢；</li>
 *   <li>{@link com.qlangtech.tis.plugin.MultiDescribleElementSetSelector} 的 {@code target}
 *       在 {@code generate_target_instance} 阶段拿存储值和候选
 *       {@code config.identityValue()} 做 {@code contains} 过滤 —— 候选项一旦 label≠val
 *       （例如多对象类型 Action 里带命名空间前缀的 {@code <objectType>_<prop>} 参数名），
 *       整批候选被静默过滤成空，用户勾选后一行都加不进来。</li>
 * </ol>
 * 历史上 IdList 选项几乎都是 {@code new Option(val)} 单参构造（label==val）所以两种写法碰巧
 * 等价，回归极难靠肉眼发现 —— 本测试构造 label≠val 的候选项把差异显式化。
 *
 * <p>顺带钉住两条配套约束：
 * <ol>
 *   <li>兜底元素必须是 {@link CMeta} 具体类（不能换成匿名 {@code IMultiElement}）：
 *       字段声明类型是 {@code List<IdentityName>}，GET 序列化还有 {@code (List<CMeta>)} 强转；</li>
 *   <li>未勾选的候选项不得混进存储值。</li>
 * </ol>
 *
 * <p><b>运行要求</b>：需要 {@code --add-opens java.base/java.lang=ALL-UNNAMED}
 * （{@code TIS.get()} 扩展扫描 {@code ClassLoaderReflectionToolkit}），
 * 与 {@code TestRelevantFieldsContextAttrValMap} 一致。
 */
public class TestMultiSelectableValRoundTrip {

    private static final String KEY_TARGET = "targetCols";

    /**
     * 回归锚点：label≠val 的候选项，落盘的必须是 val。
     *
     * <p>若哪天有人把兜底分支改回 {@code c.setName(item.getName())}，本用例会拿到
     * 「显示列甲」而不是 {@code col_a} —— 直接红在这里，别处不会先炸。
     */
    @Test
    public void testIdListFallbackStoresValNotLabel() {
        CapturingMsgHandler msgHandler = new CapturingMsgHandler();
        Context context = new DefaultContext();

        JSONObject vals = new JSONObject();
        vals.put(KEY_TARGET, multiSelectVal(
                option("显示列甲", "col_a", true),   // label≠val：回归锚点
                option("col_b", "col_b", true),      // label==val：历史形态（单参 Option 构造）必须继续兼容
                option("未选列", "col_c", false)     // 未勾选：不得混进存储值
        ));

        RelevantFieldsContextAttrValMap<MultiSelectRoundTripProbe> ctx =
                RelevantFieldsContextAttrValMap.createRelevantFieldsContext(msgHandler, context,
                        postContent(vals), validatePhaseKeys(KEY_TARGET));

        assertTrue("勾选项齐备应通过校验：" + msgHandler.fieldErrors, ctx.validate().isValid());

        MultiSelectRoundTripProbe instance = ctx.createPluginInstance();
        List<IdentityName> selected = instance.targetCols;
        assertNotNull("勾选了 2 项，字段必须被赋值", selected);
        assertEquals("只落勾选项，未勾选的 col_c 不得混入", 2, selected.size());

        IdentityName first = selected.get(0);
        assertTrue("兜底元素必须是 CMeta 具体类（匿名 IMultiElement 过不了 GET 侧 (List<CMeta>) 强转）",
                first instanceof CMeta);
        assertEquals("存储值必须是线格式 val（选项身份），绝不能是 label（显示文案）；"
                        + "本断言红了说明有人把兜底分支改回 item.getName()",
                "col_a", ((CMeta) first).getName());
        assertEquals("CMeta.identityValue 委托 getName —— selector target 过滤消费的正是它，"
                        + "两者必须指向同一个 val",
                "col_a", first.identityValue());
        assertEquals("label==val 的历史形态不受影响", "col_b", ((CMeta) selected.get(1)).getName());
    }

    /**
     * GET 回显全链路：存储值经 {@code ViewFormatType.IdList} 序列化回前端时，
     * 交出的必须是 val 列表（前端拿它和选项 val 比对勾选态）。
     *
     * <p>这条同时是匿名 {@code IMultiElement} 变体的反证：泛型擦除让 {@code f.set}
     * 不报错，CCE 会推迟到本断言触发的 {@code (List<CMeta>)} 强转 + {@code CMeta::getName}
     * 上 —— 没有它，匿名类变体会藏到生产环境的下一次序列化才炸。
     */
    @Test
    public void testGetRoundTripEmitsStoredVals() {
        CapturingMsgHandler msgHandler = new CapturingMsgHandler();
        Context context = new DefaultContext();

        JSONObject vals = new JSONObject();
        vals.put(KEY_TARGET, multiSelectVal(
                option("显示列甲", "col_a", true),
                option("col_b", "col_b", true)
        ));

        RelevantFieldsContextAttrValMap<MultiSelectRoundTripProbe> ctx =
                RelevantFieldsContextAttrValMap.createRelevantFieldsContext(msgHandler, context,
                        postContent(vals), validatePhaseKeys(KEY_TARGET));
        assertTrue(ctx.validate().isValid());
        MultiSelectRoundTripProbe instance = ctx.createPluginInstance();

        PropertyType targetColPt = null;
        for (var entry : new MultiSelectRoundTripProbe.DefaultDescriptor()
                .getPluginFormPropertyTypes().getKVTuples()) {
            if (KEY_TARGET.equals(entry.getKey())) {
                targetColPt = (PropertyType) entry.getValue();
            }
        }
        assertNotNull("探针上必须存在 " + KEY_TARGET + " 属性", targetColPt);

        try {
            Object output = targetColPt.getFrontendOutput(instance);
            assertEquals("GET 回显必须交出 val 列表（前端按 val 匹配勾选态，交 label 则保存后勾选全丢）",
                    List.of("col_a", "col_b"), output);
        } catch (ClassCastException e) {
            fail("GET 序列化发生 CCE，说明兜底元素不是 CMeta 具体类（如被改成匿名 IMultiElement），实际：" + e);
        }
    }

    // ===================================================================
    //  探针插件与 helpers（结构与 TestRelevantFieldsContextAttrValMap 同风格）
    // ===================================================================

    private static RelevantFieldsContextAttrValMap.RelevantFieldKeys validatePhaseKeys(String... keys) {
        List<String> fields = List.of(keys);
        return new RelevantFieldsContextAttrValMap.RelevantFieldKeys(KEY_TARGET) {
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
     * 前端投递的线格式：{@code {impl, vals:{k:{_eprops:{enum:[{label,val,checked}]}}}}}。
     */
    private static JSONObject postContent(JSONObject vals) {
        JSONObject post = new JSONObject();
        post.put(PLUGIN_EXTENSION_IMPL, MultiSelectRoundTripProbe.class.getName());
        post.put(PLUGIN_EXTENSION_VALS, vals);
        return post;
    }

    /**
     * IdList 视图 MULTI_SELECTABLE 字段的字段值：前端把 GET 下发的 {@code _eprops.enum}
     * 原样回送，只改写每项的 {@code checked}。
     */
    private static JSONObject multiSelectVal(JSONObject... options) {
        JSONArray enums = new JSONArray();
        for (JSONObject opt : options) {
            enums.add(opt);
        }
        JSONObject enumProp = new JSONObject();
        enumProp.put(Descriptor.KEY_ENUM_PROP, enums);
        JSONObject val = new JSONObject();
        val.put(ElementCreatorFactory.PROPERTY_EXTERNAL_PROPERTIES, enumProp);
        return val;
    }

    private static JSONObject option(String label, String val, boolean checked) {
        JSONObject o = new JSONObject();
        o.put(Option.KEY_LABEL, label);
        o.put(Option.KEY_VALUE, val);
        o.put(Option.keyChecked, checked);
        return o;
    }

    private record FieldErr(String field, String msg) {
    }

    /**
     * 只记字段级错误的最小 handler（不继承 DefaultFieldErrorHandler：其 addFieldError 是 final）。
     */
    private static class CapturingMsgHandler implements IControlMsgHandler {
        final List<FieldErr> fieldErrors = new ArrayList<>();

        @Override
        public void addFieldError(Context context, String fieldName, String msg, Object... params) {
            this.fieldErrors.add(new FieldErr(fieldName, msg));
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
     * 测试用插件：一个 IdList 视图的 {@code MULTI_SELECTABLE} 字段，字段声明类型与
     * {@code MultiDescribleElementSetSelector#target} 等真实消费者一致（{@code List<IdentityName>}）。
     *
     * <p>descriptor 写成静态内嵌类，供扩展扫描按 impl 反查（同 {@code TestRelevantFieldsContextAttrValMap}）。
     * 字段的 {@code viewtype=idlist} 等额外属性声明在同包资源 {@code MultiSelectRoundTripProbe.json} 中。
     */
    public static class MultiSelectRoundTripProbe implements Describable<MultiSelectRoundTripProbe> {

        @FormField(ordinal = 0, type = FormFieldType.MULTI_SELECTABLE, validate = {Validator.require})
        public List<IdentityName> targetCols;

        @TISExtension
        public static class DefaultDescriptor extends Descriptor<MultiSelectRoundTripProbe> {
        }
    }
}
