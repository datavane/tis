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
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.Descriptor.PostFormVals;
import com.qlangtech.tis.extension.IPropertyType;
import com.qlangtech.tis.extension.PluginFormProperties;
import com.qlangtech.tis.extension.SubFormFilter;
import com.qlangtech.tis.extension.impl.AdapterPluginFormProperties;
import com.qlangtech.tis.extension.impl.PropValRewrite;
import com.qlangtech.tis.extension.impl.PropertyType;
import com.qlangtech.tis.lang.TisException;
import com.qlangtech.tis.runtime.module.misc.FormVaildateType;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.util.impl.AttrVals;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.qlangtech.tis.extension.Descriptor.KEY_DESC_VAL;
import static com.qlangtech.tis.extension.Descriptor.KEY_primaryVal;

/**
 * 代表从前端页面中提交的表单plugin内容
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
@SuppressWarnings("all")
public class AttrValMap {

    public static final String PLUGIN_EXTENSION_IMPL = "impl";
    public static final String PLUGIN_EXTENSION_VALS = "vals";

    private static final ThreadLocal<Descriptor> currentRootPluginValidator = new ThreadLocal<>();

    public static void setCurrentRootPluginValidator(Descriptor descriptor) {
        currentRootPluginValidator.set(Objects.requireNonNull(descriptor, "descriptor can not be null"));
    }

    public static Descriptor getCurrentRootPluginValidator() {
        return Objects.requireNonNull(currentRootPluginValidator.get(), "currentRootPluginValidator must be present");
    }

    public static void removeCurrentRootPluginValidator() {
        currentRootPluginValidator.remove();
    }

    private final AttrVals attrValMap;

    public final Descriptor descriptor;

    //private IControlMsgHandler msgHandler;
    private final Optional<SubFormFilter> subFormFilter;
    private final PropValRewrite propValRewrite;

    public static List<AttrValMap> describableAttrValMapList(JSONArray itemsArray,
                                                             Optional<SubFormFilter> subFormFilter) {
        return describableAttrValMapList(itemsArray, subFormFilter, PropValRewrite.dftRewrite());
    }

    public static List<AttrValMap> describableAttrValMapList(JSONArray itemsArray,
                                                             Optional<SubFormFilter> subFormFilter,
                                                             PropValRewrite propValRewrite) {
        List<AttrValMap> describableAttrValMapList = Lists.newArrayList();
        AttrValMap describableAttrValMap = null;
        JSONObject itemObj = null;
        for (int i = 0; i < itemsArray.size(); i++) {
            itemObj = itemsArray.getJSONObject(i);
            describableAttrValMap = parseDescribableMap(subFormFilter, itemObj, propValRewrite);
            describableAttrValMapList.add(describableAttrValMap);
        }
        return describableAttrValMapList;
    }

    public static AttrValMap parseDescribableMap(Optional<SubFormFilter> subFormFilter,
                                                 com.alibaba.fastjson.JSONObject jsonObject) {
        return parseDescribableMap(subFormFilter, jsonObject, ((propType, val) -> val));
    }


    /**
     *
     * @param subFormFilter
     * @param jsonObject     确认需要 <Code>FlatJsonToTisConverter.#convert()转化过</Code>
     * @param propValRewrite
     * @return
     */
    public static AttrValMap parseDescribableMap(Optional<SubFormFilter> subFormFilter,
                                                 com.alibaba.fastjson.JSONObject jsonObject,
                                                 PropValRewrite propValRewrite) {
        String impl = Objects.requireNonNull(jsonObject, "jsonObject can not be null").getString(PLUGIN_EXTENSION_IMPL);
        Descriptor descriptor = TIS.get().getDescriptor(impl);
        if (descriptor == null) {
            throw new IllegalStateException("impl:" + impl + " can not find relevant ");
        }
        Object vals = jsonObject.get(PLUGIN_EXTENSION_VALS);
        AttrVals attrValMap = AttrVals.parseAttrValMap(vals);
        return new AttrValMap(attrValMap, subFormFilter, descriptor, propValRewrite);
    }

    private AttrValMap(AttrVals attrValMap, Optional<SubFormFilter> subFormFilter, Descriptor descriptor,
                       PropValRewrite propValRewrite) {
        this.attrValMap = attrValMap;
        this.descriptor = descriptor;
        //  this.msgHandler = msgHandler;
        this.subFormFilter = subFormFilter;
        this.propValRewrite = propValRewrite;
    }

    public boolean strictValidate(PartialSettedPluginContext msgHandler, Context ctx) {
        FormVaildateType verify = FormVaildateType.create(true);
        FormVaildateType validate = FormVaildateType.create(false);
        boolean validateFaild = false;
        try {
            if (!this.validate(msgHandler, ctx, verify, Optional.empty()).isValid() //
                    || !this.validate(msgHandler, ctx, validate, Optional.empty()).isValid()) {
                // error
                validateFaild = true;
            }
        } catch (Exception e) {
            validateFaild = true;
            TisException expt = null;
            if ((expt = ExceptionUtils.throwableOfType(e, TisException.class)) != null) {
                msgHandler.addErrorMessage(ctx, expt.getMessage());
            } else {
                throw new RuntimeException(e);
            }
        }
        return validateFaild;
    }

    /**
     * 取得主键键值
     *
     * @return
     */
    public final String getPrimaryFieldVal() {
        return String.valueOf(getPKVal());
    }

    public final boolean isPrimaryFieldEmpty() {

        Object val = getPKVal();
        return val == null || StringUtils.isEmpty(String.valueOf(val));
    }

    /**
     * 包含主键属性
     *
     * @return
     */
    public boolean containPKField() {
        return getIdField() != null;
    }

    private Object getPKVal() {

        PropertyType idField = getIdField();
        if (idField == null) {
            return null;
        }

        return this.getAttrVals().getPrimaryVal(idField.propertyName());
    }

    private PropertyType getIdField() {
        return
                Objects.requireNonNull(this.descriptor, "descriptor can not be null")
                        .getIdentityField(false);
    }

    /**
     * 用于在前端页面上渲染plugin实例的json
     *
     * @return
     */
    public JSONObject getPostJsonBody() {
        JSONObject body = new JSONObject();
        DescriptorsJSON.setDescInfo(this.descriptor, false, body);

        JSONObject vals = new JSONObject();
        this.attrValMap.vistAttrValMap((field, val) -> {
            convertFieldVal(vals, field, val);
        });
        body.put(PLUGIN_EXTENSION_VALS, vals);
        return body;
    }

    private void convertFieldVal(JSONObject vals, String field, JSON val) {
        if (val instanceof JSONObject) {
            JSONObject propVal = (JSONObject) val;
            if (propVal.containsKey(KEY_DESC_VAL)) {
                // 说明是describle类型的
                JSONObject pluginBody = propVal.getJSONObject(KEY_DESC_VAL);
                JSONObject pluginVals = new JSONObject();
                //"field:" + field + ",prop:" + PLUGIN_EXTENSION_VALS + " relevant val can not be null"
                JSONObject rawVals = (pluginBody.getJSONObject(PLUGIN_EXTENSION_VALS));
                if (rawVals != null) {
                    for (Map.Entry<String, Object> entry : rawVals.entrySet()) {
                        convertFieldVal(pluginVals, entry.getKey(), (JSON) entry.getValue());
                    }
                }
                pluginBody.put(PLUGIN_EXTENSION_VALS, pluginVals);
                vals.put(field, pluginBody);
            } else {
                vals.put(field, propVal.get(KEY_primaryVal));
            }
        } else {
            throw new IllegalStateException("illegal val type:" + val.getClass().getName());
        }
    }


    public Descriptor.PluginValidateResult validate(IControlMsgHandler msgHandler, Context context,
                                                    FormVaildateType verify, Optional<PostFormVals> parentFormVals) {
        return this.validate(msgHandler, context, Optional.empty(), verify, parentFormVals);
    }

    /**
     * 校验表单输入内容
     *
     * @param context
     * @param verify  是否进行业务逻辑校验
     * @return true：校验没有错误 false：校验有错误
     */
    public Descriptor.PluginValidateResult validate(IControlMsgHandler msgHandler, Context context,
                                                    Optional<PluginFormProperties> propertyTypes,
                                                    FormVaildateType verify, Optional<PostFormVals> parentFormVals) {
        return this.descriptor.verify(msgHandler, context, verify, attrValMap, propertyTypes, subFormFilter,
                this.propValRewrite, parentFormVals);
    }

    public Descriptor.PluginValidateResult validateWithScope(IControlMsgHandler msgHandler, Context context,
                                                             int pluginIndex, int itemIndex, FormVaildateType verify) {
        return validateWithScope(msgHandler, context, Optional.empty(), pluginIndex, itemIndex, verify);
    }

    /**
     * 带作用域的校验：设置当前 root plugin validator 以及本 item 在整体表单中的位置，
     * 使单条校验与批量表单提交的校验循环({@link com.qlangtech.tis.util.PluginItems#validate})
     * 保持一致的语义（错误信息能定位到具体 item）。
     * <p>
     * 调用结束后必然清理 ThreadLocal，不会把作用域泄漏给后续校验。
     *
     * @param pluginIndex 当前 plugin 在整体表单中的位置
     * @param itemIndex   当前 item 在 plugin 内的位置
     */
    public Descriptor.PluginValidateResult validateWithScope(IControlMsgHandler msgHandler, Context context,
                                                             Optional<PluginFormProperties> propertyTypes,
                                                             int pluginIndex, int itemIndex, FormVaildateType verify) {
        try {
            setCurrentRootPluginValidator(this.descriptor);
            Descriptor.PluginValidateResult.setValidateItemPos(context, pluginIndex, itemIndex);
            //
            Descriptor.PluginValidateResult validateResult =
                    this.validate(msgHandler, context, propertyTypes, verify, Optional.empty());
            if (validateResult.isValid()) {
                validateResult.setDescriptor(this.descriptor);
            }
            return validateResult;
        } finally {
            removeCurrentRootPluginValidator();
        }
    }

    public Descriptor.ParseDescribable createDescribable(IControlMsgHandler pluginContext, Context context) {
        return this.createDescribable(pluginContext, context, Optional.empty());
    }

    /**
     * 创建插件实例对象
     *
     * @return
     */
    public Descriptor.ParseDescribable createDescribable(IControlMsgHandler pluginContext, Context context,
                                                         Optional<PluginFormProperties> formProperties) {
        return this.descriptor.parseDescribable(pluginContext, context, this.attrValMap, (formProperties),
                this.subFormFilter, this.propValRewrite);
    }

    /**
     * 为「值变更管道({@link com.qlangtech.tis.extension.ValueChangePipe})级联取值」场景创建校验与实例化上下文。
     * <p>
     * 前端做级联取值时提交上来的是插件完整表单的<b>一个子集</b>（例如在 MULTI_DESCRIBLE_PLUGIN 表格中点击某列，
     * 只会带上与该列相关的若干属性）。若拿整张表单的属性集去校验，会因为「表单中其它尚未填写的必填属性」而误报错误。
     * 因此这里把插件的属性集裁剪成只含<b>与本次级联相关的字段</b>：相关字段由调用方通过
     * <code>relevantFieldKeysCreator</code> 依据 descriptor 自行计算，
     * 典型实现为 <code>descriptor.getValueChangeFromFieldKeys(toFieldKey)</code>，即全部指向 toFieldKey 的 from 字段。
     * <p>
     * <b>契约</b>：<code>relevantFieldKeysCreator</code> 返回的集合被<b>按引用</b>用作属性集的过滤依据
     * （见 {@link AdapterPluginFormProperties#getKVTuples()} 的懒求值实现），本方法<b>不做任何防御性拷贝</b>。
     * 所以调用方可以一直持有该集合，在 {@link RelevantFieldsContextAttrValMap#validate()} 之后、
     * {@link RelevantFieldsContextAttrValMap#createDescribable()} 之前继续往里追加字段——典型场景是级联的
     * 目标字段本身：校验时它还没有值不能参与校验，但创建实例时必须带上它，否则实例上取不到该属性值。
     * <p>
     * <b>注意</b>：若日后把这里改成先拷贝一份返回集合，调用方的上述追加会<b>静默失效</b>（级联实例取不到目标字段值且不报错）。
     *
     * @param paramGetter              用于收集错误信息与回写业务结果
     * @param context                  校验错误信息与业务结果的承载上下文
     * @param postContent              已由调用方从请求体中取出的插件表单内容（嵌套描述符需调用方自行定位到对应层级）
     * @param relevantFieldKeysCreator 依据 descriptor 计算「相关字段」的创建器，descriptor 由本次解析结果的 impl 决定；
     *                                 返回的集合需由调用方持有，以便在 validate() 之后再追加字段
     * @see #parseDescribableMap(Optional, JSONObject)
     */
    public static <T extends Describable> RelevantFieldsContextAttrValMap<T> createRelevantFieldsContext(
            IControlMsgHandler paramGetter, Context context, JSONObject postContent,
            Function<Descriptor, List<String>> relevantFieldKeysCreator) {
        final AttrValMap valMap = AttrValMap.parseDescribableMap(Optional.empty(), postContent);
        // 哪些字段算「相关」交由调用方决定，本方法只负责据此裁剪属性集
        List<String> relevantFieldKeys = relevantFieldKeysCreator.apply(valMap.descriptor);
        final Optional<PluginFormProperties> propertyTypes =
                Optional.of(new AdapterPluginFormProperties(valMap.descriptor.getPluginFormPropertyTypes()) {
                    @Override
                    public Set<Map.Entry<String, IPropertyType>> getKVTuples() {
                        // getKVTuples() 是懒求值的（每次调用才委托 target 做一次过滤），
                        // 所以调用方在 validate() 之后往 relevantFieldKeys 里追加的字段，对 createDescribable() 一样可见
                        return super.getKVTuples().stream() //
                                .filter((e) -> relevantFieldKeys.contains(e.getKey())).collect(Collectors.toSet());
                    }
                });

        return new RelevantFieldsContextAttrValMap<T>() {
            public Descriptor.PluginValidateResult validate() {
                // 走带作用域的校验，保证错误信息能定位到具体 item
                Descriptor.PluginValidateResult validate = valMap.validateWithScope(paramGetter, context,
                        propertyTypes, 0, 0,
                        FormVaildateType.VERIFY);
                return validate;
            }

            public Descriptor.ParseDescribable<T> createDescribable() {
                Descriptor.ParseDescribable<T> describable = valMap.createDescribable(paramGetter, context,
                        propertyTypes);
                return describable;
            }
        };
    }

    public interface RelevantFieldsContextAttrValMap<T extends Describable> {

        /**
         * 校验「相关字段」（以及事后追加的字段）的取值，错误信息能定位到具体 item
         *
         * @see #createRelevantFieldsContext(IControlMsgHandler, Context, JSONObject, Function)
         */
        public Descriptor.PluginValidateResult validate();

        /**
         * 创建插件实例，只读取「相关字段」（以及事后追加的字段）
         *
         * @see #createRelevantFieldsContext(IControlMsgHandler, Context, JSONObject, Function)
         */
        public Descriptor.ParseDescribable<T> createDescribable();

        public default T createPluginInstance() {
            return createDescribable().getInstance();
        }

    }

    public int size() {
        return this.attrValMap.size();
    }

    public AttrVals getAttrVals() {
        return this.attrValMap;
    }

    /**
     * @author: 百岁（baisui@qlangtech.com）
     * @create: 2022-08-12 21:54
     **/
    public interface IAttrVals {

        public static IAttrVals rootForm(Map<String, JSONObject> sform) {
            return new IAttrVals() {
                @Override
                public Map<String, JSONObject> asRootFormVals() {
                    return sform;
                }

                @Override
                public int size() {
                    return sform.size();
                }
            };
        }

        default Map<String, JSONObject> asRootFormVals() {
            throw new UnsupportedOperationException();
        }

        default Map<String, JSONArray> asSubFormDetails() {
            throw new UnsupportedOperationException();
        }

        int size();
    }
}
