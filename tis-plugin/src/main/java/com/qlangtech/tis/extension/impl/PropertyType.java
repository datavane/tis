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
package com.qlangtech.tis.extension.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.aiagent.llm.TISJsonSchema;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.DescriptorUseableShortComment;
import com.qlangtech.tis.extension.ElementPluginDesc;
import com.qlangtech.tis.extension.IPropertyType;
import com.qlangtech.tis.extension.util.GroovyShellEvaluate;
import com.qlangtech.tis.extension.util.GroovyShellUtil;
import com.qlangtech.tis.extension.util.MultiItemsViewType;
import com.qlangtech.tis.extension.util.OverwriteProps;
import com.qlangtech.tis.extension.util.PluginExtraProps;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.manage.common.OptionWithEndType;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.SubForm;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.ds.CMeta;
import com.qlangtech.tis.plugin.ds.DataTypeMeta;
import com.qlangtech.tis.plugin.ds.ElementCreatorFactory;
import com.qlangtech.tis.runtime.module.misc.IMessageHandler;
import com.qlangtech.tis.trigger.util.JsonUtil;
import com.qlangtech.tis.trigger.util.UnCacheString;
import com.qlangtech.tis.util.AttrValMap;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.lang.StringUtils;
import org.jvnet.tiger_types.Types;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.qlangtech.tis.extension.Descriptor.KEY_DESC_VAL;
import static com.qlangtech.tis.extension.util.PluginExtraProps.Props.KEY_VIEW_TYPE;
import static com.qlangtech.tis.manage.common.Option.KEY_LABEL;
import static com.qlangtech.tis.manage.common.Option.KEY_VALUE;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-11 12:07
 */
@SuppressWarnings("all")
public class PropertyType implements IPropertyType {
    private static final ConvertUtilsBean convertUtils = new ConvertUtilsBean();

    static {
        convertUtils.register(new Converter() {
            @Override
            public <T> T convert(Class<T> type, Object value) {
                if (value instanceof UnCacheString) {
                    return (T) ((UnCacheString) value).getValue();
                } else if (value instanceof JSONArray) {
                    JSONArray array = (JSONArray) value;
                    List<String> convert = array.toJavaList(String.class);
                    return (T) convert;
                } else {
                    return (T) value;
                }
            }
        }, List.class);
    }

    private static final JSONArray bolOps;

    static {
        bolOps = new JSONArray();
        OverwriteProps.ENUM_BOOLEAN.forEach((option) -> {
            JSONObject b = new JSONObject();
            b.put(KEY_LABEL, option.getName());
            b.put(KEY_VALUE, option.getValue());
            bolOps.add(b);
        });
    }


    private final Class ownerClazz;
    // private final Optional<Descriptor.ElementPluginDesc> parentPluginDesc;
    public final Class fieldClazz;

    public final Optional<Class<? extends Describable>> fieldListElementClazz;

    public final Type type;

    private volatile Class itemType;

    public final String displayName;

    public final FormField formField;

    public final Field f;

    @Override
    public String propertyName() {
        return this.f.getName();
    }

    public TISJsonSchema.FieldType schemaFieldType() {
        FormFieldType fieldType = this.formField.type();
        if ((this.fieldClazz == boolean.class || this.fieldClazz == Boolean.class)) {
            return TISJsonSchema.FieldType.Boolean;
        } else if (this.fieldClazz == int.class || this.fieldClazz == Integer.class) {
            return TISJsonSchema.FieldType.Integer;
        } else if (Collection.class.isAssignableFrom(this.fieldClazz)) {
            EnumFieldMode enumFieldMode = null;
            if ((enumFieldMode = this.getEnumFieldMode()) != null && enumFieldMode != EnumFieldMode.MULTIPLE) {
                throw new IllegalStateException("property:" + this.propertyName() + ",plugin:" + this.ownerClazz.getName() //
                        + ",EnumFieldMode:" + this.getEnumFieldMode() + " must be:" + EnumFieldMode.MULTIPLE);
            }
            return TISJsonSchema.FieldType.Array;
        }
        return fieldType.schemaFieldType;
    }

    /**
     * 值最终要显示在前端页面上给用户查看
     *
     * @param val
     * @return
     */
    public Object serialize2FrontendOutput(Object val) {
        if (val == null) {
            return null;
        }
        try {
            return this.formField.type().valProcessor.serialize2Output(this, val);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Boolean inputRequired;

    private MultiItemsViewType multiItemsViewType;

    public PluginExtraProps.Props extraProp;

    public PropertyType(Class ownerClazz, Field f, FormField formField) {
        this(ownerClazz, f, f.getType(), f.getGenericType(), f.getName(), formField);
    }

    PropertyType(Class ownerClazz, Field f, Class fieldClazz, Type type, String displayName, FormField formField) {
        this.ownerClazz = Objects.requireNonNull(ownerClazz, "ownerClass can not be null");
        this.f = f;
        this.fieldClazz = fieldClazz;
        this.type = type;
        this.displayName = displayName;
        if (formField == null) {
            throw new IllegalStateException("param formField can not be null");
        }
        this.formField = formField;
        // 当field为List<? extend Describle> 且 FormField也设置了desClazz属性（正常情况下desClazz要与List<?> 中的element类型要一致），则为
        this.fieldListElementClazz = this.createFieldListElementClazz();
    }

    private Optional<Class<? extends Describable>> createFieldListElementClazz() {
        final Class<? extends Describable> desClazz = this.formField.desClazz();
        if (desClazz == Describable.class) {
            // 未设置 desClazz，元素类型以声明的泛型为准，无需额外记录
            return Optional.empty();
        }

        // 复用 getItemType()：已由 tiger-types 从泛型签名里解析出 List<ElementType> 的 ElementType，
        // 对 raw List 及声明在泛型父类中的字段都能正确降级，不必自己 cast ParameterizedType
        final Class listElementClazz = this.getItemType();
        if (listElementClazz == null || listElementClazz != (desClazz)) {
            throw new IllegalStateException("desClazz:" + desClazz.getName() + " must be class of field:"
                    + this.f.getName() + ", but now is:" + listElementClazz);
        }

        // IdentityName, IPluginStore.ManipuldateProcessor
        if (!IPluginStore.MultiDescribleElement.class.isAssignableFrom(desClazz)) {
            throw new IllegalStateException("since this.formField contain desClazz:" + desClazz.getName() + " "
                    + "then fieldClazz must be type of " + IPluginStore.MultiDescribleElement.class.getName());
        }

        //        if (!IPluginStore.ManipuldateProcessor.class.isAssignableFrom(desClazz)) {
        //            throw new IllegalStateException("since this.formField contain desClazz:" + desClazz.getName() +
        //            " "
        //                    + "then fieldClazz must be type of " + IPluginStore.ManipuldateProcessor.class.getName());
        //        }

        if (!List.class.isAssignableFrom(fieldClazz)) {
            throw new IllegalStateException("since this.formField contain desClazz:" + desClazz.getName() + " "
                    + "then fieldClazz must be type of " + List.class.getName());
        }

        if (this.formField.type() != FormFieldType.MULTI_DESCRIBLE_PLUGIN) {
            throw new IllegalStateException("annotation FormField type:" + FormFieldType.MULTI_DESCRIBLE_PLUGIN + " "
                    + "must be class of field:"
                    + this.f.getName() + ", but now is:" + this.formField.type());
        }


        return Optional.of(desClazz);
    }

    @Override
    public List<Option> getEnumPropOptions() {
        return this.getEnumPropOptions(true);
    }

    public List<Option> getEnumPropOptions(boolean validateNull) {
        List<Option> opts = Lists.newArrayList();

        if (FormFieldType.ENUM != this.formField.type() || this.getExtraProps() == null) {
            return opts;
        }
        Object enumPp = Objects.requireNonNull(this.getExtraProps(),
                "extraProps can not be null, for property:"
                        + this.f.getName() + ",owner:" + this.ownerClazz.getName()).get(Descriptor.KEY_ENUM_PROP);
        if (enumPp == null) {
            if (validateNull) {
                throw new IllegalStateException("field:" + this.f.getName() + " enum property can not be empty");
            } else {
                return Collections.emptyList();
            }
        }
        JSONArray enums = null;
        if (enumPp instanceof JSONArray) {
            enums = (JSONArray) enumPp;
        } else if (enumPp instanceof UnCacheString) {
            enums = ((UnCacheString<JSONArray>) enumPp).getValue();
        } else {
            throw new IllegalStateException("unsupport type:" + enumPp.getClass().getName());
        }
        if (enums != null) {
            for (int i = 0; i < enums.size(); i++) {
                JSONObject opt = enums.getJSONObject(i);
                Option o = OptionWithEndType.deserialize(opt);
                if (o == null) {
                    o = new Option(opt.getString(KEY_LABEL), opt.get(Option.KEY_VALUE));
                }
                opts.add(o);
            }
        }

        return opts;
    }

    /**
     * 设置默认值
     *
     * @param dftVal
     * @param props
     */
    public static void setDefaultVal(Object dftVal, JSONObject props) {
        if (dftVal != null) {
            final Class dftValClazz = dftVal.getClass();

            if (!(dftValClazz == String.class || dftValClazz == UnCacheString.class || Number.class.isAssignableFrom(dftValClazz) || dftValClazz == Boolean.class || dftValClazz.isEnum())) {
                throw new IllegalStateException("default value must be type of String or primitive,but now is type:" + dftVal.getClass());
            }
            props.put(PluginExtraProps.KEY_DFTVAL_PROP, dftVal);
        }
    }

    public static void setLabel(String label, JSONObject props) {
        if (StringUtils.isEmpty(label)) {
            throw new IllegalArgumentException("param label can not be null");
        }
        Objects.requireNonNull(props, "props can not be null").put(KEY_LABEL, label);
    }

    public static void setDisabled(JSONObject props) {
        Objects.requireNonNull(props, "props can not be null").put(PluginExtraProps.KEY_DISABLE, true);
    }

    public static void setReadOnly(JSONObject props) {
        Objects.requireNonNull(props, "props can not be null").put(PluginExtraProps.KEY_READONLY, true);
    }

    @Override
    public boolean isCollectionType() {
        //   PropertyType pt = (PropertyType) propertyType;
        return List.class.isAssignableFrom(this.fieldClazz);
    }

    /**
     * 过滤掉非 {@link PropertyType} 类型的属性（例如 @SubForm 字段对应的 SuFormProperties），
     * <p>
     * 返回值类型为 {@link IPropertyType}，因为部分消费方（例如 SuFormProperties）只能接收 PropertyType，
     * 需要用到窄类型视图的消费方请使用 {@link #toPropertyTypes(Map)}
     */
    public static Map<String, /*** fieldname*/IPropertyType> filterFieldProp(Map<String,
            /*** fieldname*/IPropertyType> props) {
        return props.entrySet().stream().filter((e) -> {
                    IPropertyType pt = e.getValue();
                    return pt instanceof PropertyType;
                }) //
                .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue()));
    }

    /**
     * 窄类型视图：将过滤后的属性 map 视图强转为 PropertyType 的 map（零拷贝，只能当只读使用）
     * <p>
     * <strong>入参必须是 {@link #filterFieldProp(Map)} 的过滤结果</strong>，否则运行期访问元素时会抛 ClassCastException
     */
    @SuppressWarnings("unchecked")
    public static Map<String, /*** fieldname*/PropertyType> toPropertyTypes(Map<String, ? extends IPropertyType> props) {
        return (Map<String, PropertyType>) (Map) props;
    }

    /**
     * 放宽类型视图：将 PropertyType 的 map 视图放宽为 IPropertyType 的 map（零拷贝，只能当只读使用）
     */
    @SuppressWarnings("unchecked")
    public static Map<String, /*** fieldname*/IPropertyType> toIPropertyTypes(Map<String, ? extends IPropertyType> props) {
        return (Map<String, IPropertyType>) (Map) props;
    }

    /**
     * 可能plugin form 表单需要几个步骤才能 填充完一个plugin form 表单就需要单独取出部分表单属性去渲染前端页面
     *
     * @param clazz
     * @return
     */
    @SuppressWarnings("all")
    public static Map<String, /*** fieldname */IPropertyType> buildPropertyTypes(Optional<ElementPluginDesc> descriptor, final Class<? extends Describable> clazz) {
        try {
            Map<String, IPropertyType> propMapper = new HashMap<>();

            Optional<PluginExtraProps> extraProps = PluginExtraProps.load(descriptor, clazz);

            // 支持使用继承的方式来实现复用，例如：DataXHiveWriter继承DataXHdfsWriter来实现
            PluginExtraProps.visitAncestorsClass(clazz, new PluginExtraProps.IClassVisitor<Void>() {
                @Override
                public Void process(Class<?> targetClass, Void v, boolean finalChild) {
                    FormField formField = null;
                    SubForm subFormFields = null;
                    //   ptype = null;

                    Class<? extends Describable> subFromDescClass = null;
                    Field targetField = null;
                    try {
                        for (Field f : targetClass.getDeclaredFields()) {
                            targetField = f;
                            if (!Modifier.isPublic(f.getModifiers()) || Modifier.isStatic(f.getModifiers())) {
                                continue;
                            }

                            if ((subFormFields = f.getAnnotation(SubForm.class)) != null) {
                                subFromDescClass = subFormFields.desClazz();
                                if (subFromDescClass == null) {
                                    throw new IllegalStateException("field " + f.getName() + "'s SubForm annotation " + "descClass can not be null");
                                }

                                final Descriptor subFormDesc =
                                        Objects.requireNonNull(TIS.get().getDescriptor(subFromDescClass),
                                                "subFromDescClass:" + subFromDescClass + " relevant descriptor can " + "not be null");

                                propMapper.put(f.getName(), new SuFormProperties(clazz, f, subFormFields, subFormDesc
                                        ,
                                        toPropertyTypes(filterFieldProp(buildPropertyTypes(ElementPluginDesc.create(subFormDesc),
                                                subFromDescClass)))));
                            } else if ((formField = f.getAnnotation(FormField.class)) != null) {

                                PluginExtraProps.Props fieldExtraProps = null;
                                final PropertyType ptype = new PropertyType(clazz, f, formField);
                                if (extraProps.isPresent() && (fieldExtraProps =
                                        extraProps.get().getProp(f.getName())) != null) {

                                    ptype.setExtraProp(fieldExtraProps);
                                    String placeholder = fieldExtraProps.getPlaceholder();
                                    Object dftVal = fieldExtraProps.getDftVal();
                                    String help = fieldExtraProps.getHelpContent();
                                    JSONObject props = fieldExtraProps.getProps();

                                    if (fieldExtraProps.getBoolean(PluginExtraProps.KEY_DISABLE)) {
                                        propMapper.remove(f.getName());
                                        continue;
                                    }

                                    if (StringUtils.isNotEmpty(help) && StringUtils.startsWith(help,
                                            IMessageHandler.TSEARCH_PACKAGE)) {
                                        //props.put(PluginExtraProps.Props.KEY_HELP, GroovyShellEvaluate.eval(help));
                                        props.put(Option.KEY_HELP, GroovyShellEvaluate.scriptEval(help));
                                    }

                                    if (dftVal != null && StringUtils.startsWith(String.valueOf(dftVal),
                                            IMessageHandler.TSEARCH_PACKAGE) && !(dftVal instanceof UnCacheString)) {
                                        final PropertyType pt = ptype;

                                        Function<Object, Object> process = pt.getEnumFieldMode() != null ?
                                                pt.getEnumFieldMode().createDefaultValProcess(targetClass, f) :
                                                Function.identity();

                                        setDefaultVal(GroovyShellEvaluate.scriptEval(String.valueOf(dftVal), process)
                                                , props);
                                    }

                                    if (placeholder != null && StringUtils.startsWith(placeholder,
                                            IMessageHandler.TSEARCH_PACKAGE)) {
                                        props.put(PluginExtraProps.KEY_PLACEHOLDER_PROP,
                                                GroovyShellEvaluate.scriptEval(placeholder));
                                    }

                                    if (descriptor.isPresent() //
                                            && (formField.type() == FormFieldType.ENUM)) {

                                        resolveEnumProp(f, descriptor.get().getElementDesc(), fieldExtraProps,
                                                (opts) -> {
                                                    return Option.toJson((List<Option>) opts);
                                                });
                                    }

                                    if (descriptor.isPresent() //
                                            && (formField.type() == FormFieldType.MULTI_SELECTABLE)) {

                                        final PluginExtraProps.Props feProps = fieldExtraProps;

                                        ElementPluginDesc paretPluginRef = descriptor.get();
                                        resolveEnumProp(f, paretPluginRef.getElementDesc(), feProps, (cols) -> {
                                            final List<CMeta> mcols = (List<CMeta>) cols;

                                            return ptype.multiSelectablePropProcess((viewType) -> {
                                                // cols有两种显示模式
                                                MultiItemsViewType multiItemsViewType = viewType;
                                                switch (multiItemsViewType.viewType) {
                                                    case IdList:
                                                        return Option.toJson(mcols);
                                                    case TupleList:
                                                        return DataTypeMeta.createViewBiz(multiItemsViewType, mcols);
                                                    default:
                                                        throw new IllegalStateException("unhandle view type:" + multiItemsViewType);
                                                }

                                            }, true);
                                        });
                                    }
                                }
                                propMapper.put(f.getName(), ptype);
                            } else {

                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("field:" + targetField.getName() + " of targetClass:" + targetClass.getName(), e);
                    }
                    return null;
                }


            });

            return propMapper;
        } catch (Exception e) {
            throw new RuntimeException("parse desc:" + clazz.getName(), e);
        }
    }

    private static JSONArray resolveEnumProp(Field field, Descriptor descriptor,
                                             PluginExtraProps.Props fieldExtraProps, Function<Object, Object> process) {
        JSONObject props = fieldExtraProps.getProps();
        props.get(KEY_VIEW_TYPE);
        Object anEnum = props.get(Descriptor.KEY_ENUM_PROP);
        JSONArray enums = new JSONArray();
        if (anEnum != null && anEnum instanceof String) {
            try {
                GroovyShellUtil.descriptorThreadLocal.set(descriptor);
                props.put(Descriptor.KEY_ENUM_PROP, GroovyShellEvaluate.scriptEval((String) anEnum, process));
            } finally {
                GroovyShellUtil.descriptorThreadLocal.remove();
            }
        } else if (anEnum == null && (field.getType() == boolean.class || field.getType() == Boolean.class)) {

            props.put(Descriptor.KEY_ENUM_PROP, bolOps);

        } else if (anEnum == null && (field.getType().isEnum())) {
            props.put(Descriptor.KEY_ENUM_PROP, Option.toJson(createEnumOptions(field.getType())));
        }
        return enums;
    }

    /**
     * 将Java枚举类型的所有常量反射成前端可用的选项列表
     * <p>
     * val 取枚举常量名，保证前端提交的值能够被反序列化回对应的枚举实例；
     * label 优先取枚举常量上的 label 字段，次之取 {@link DescriptorUseableShortComment#shortComment()}，
     * 都没有定义则直接使用常量名；
     * 枚举实现了 {@link DescriptorUseableShortComment} 的，需要将shortComment传递给前端作为选项的说明信息，
     * 所以使用{@link OptionWithEndType}承载
     *
     * @param enumClazz 枚举类型
     * @see Option#toJson(List)
     */
    private static List<Option> createEnumOptions(Class<?> enumClazz) {
        Object[] enumConstants = enumClazz.getEnumConstants();
        if (enumConstants == null) {
            throw new IllegalStateException("clazz:" + enumClazz.getName() + " is not a enum type");
        }
        List<Option> enumOpts = Lists.newArrayList();
        for (Object enumConstant : enumConstants) {
            Enum<?> e = (Enum<?>) enumConstant;
            if (e instanceof DescriptorUseableShortComment comment) {
                // Optional<IEndTypeGetter.EndType> endOpt = Optional.empty();

                enumOpts.add(new OptionWithEndType(resolveEnumLabel(e), e.name(), IEndTypeGetter.EndType.Blank) {
                    @Override
                    public String endType() {
                        if (e instanceof IEndTypeGetter endTypeGetter) {
                            return endTypeGetter.getEndType().getVal();
                        } else {
                            return null;
                        }
                    }
                }.setDescription(comment.shortComment()));
            } else {
                enumOpts.add(new Option(resolveEnumLabel(e), e.name()));
            }
        }
        return enumOpts;
    }

    /**
     * 取枚举常量显示用的label，约定枚举中声明 public final String label 字段（或者是shortComment()）作为显示文本
     */
    private static String resolveEnumLabel(Enum<?> e) {
        //        try {
        //            Field labelField = e.getDeclaringClass().getField("label");
        //            if (labelField.getType() == String.class) {
        //                String label = (String) labelField.get(e);
        //                if (StringUtils.isNotEmpty(label)) {
        //                    return label;
        //                }
        //            }
        //        } catch (NoSuchFieldException ex) {
        //            // 枚举中没有定义label字段，忽略
        //        } catch (IllegalAccessException ex) {
        //            throw new RuntimeException("enum:" + e.getDeclaringClass().getName(), ex);
        //        }
        //        if (e instanceof DescriptorUseableShortComment) {
        //            return ((DescriptorUseableShortComment) e).shortComment();
        //        }
        return e.name();
    }

    /**
     * @param useCache   是否使用缓存的
     * @param descriptor
     * @return
     */
    public static Map<String, /*** fieldname*/IPropertyType> filterFieldProp(boolean useCache, Descriptor descriptor) {
        return filterFieldProp(descriptor.getPropertyTypes(useCache));
    }

    @JSONField(serialize = false)
    public MultiItemsViewType getMultiItemsViewType() {
        if (this.multiItemsViewType == null) {

            this.multiItemsViewType = MultiItemsViewType.createMultiItemsViewType(this);
        }
        return this.multiItemsViewType;
    }

    public void setMultiItemsViewType(MultiItemsViewType multiItemsViewType) {
        this.multiItemsViewType = multiItemsViewType;
    }

    public void setMultiItemsViewType(PropertyType oldPt) {
        this.multiItemsViewType = oldPt.getMultiItemsViewType();
    }


    /**
     * 是否是主键
     *
     * @return
     */
    @Override
    public boolean isIdentity() {
        return this.formField.identity();
    }

    @Override
    @JSONField(serialize = false)
    public JSONObject getExtraProps() {
        if (this.extraProp == null) {
            return null;
        }
        return this.extraProp.getProps();
    }

    @JSONField(serialize = false)
    public Optional<PluginExtraProps.FieldRefCreateor> getRefCreator() {
        if (this.extraProp == null) {
            return Optional.empty();
        }
        return extraProp.getRefCreator();
    }

    @JSONField(serialize = false)
    public EnumFieldMode getEnumFieldMode() {
        if (this.formField.type() != FormFieldType.ENUM && this.formField.type() != FormFieldType.SELECTABLE) {
            return null;
        }
        return EnumFieldMode.parseEnumFieldMode(this.extraProp);
    }


    public void setExtraProp(PluginExtraProps.Props extraProp) {
        this.extraProp = extraProp;
    }

    @Override
    public Object dftVal() {
        if (this.extraProp == null) {
            return null;
        }
        return this.extraProp.getDftVal();
    }

    @Override
    public int ordinal() {
        return formField.ordinal();
    }

    @Override
    public boolean advance() {
        return (this.extraProp != null && this.extraProp.isAdvance()) || formField.advance();
    }

    @Override
    public int typeIdentity() {
        return formField.type().getIdentity();
    }

    @Override
    public void appendExternalProp(JSONObject attrVal) {
        formField.type().appendExternalProps.accept(attrVal);
    }

    private Validator[] validators;

    @JSONField(serialize = false)
    public Validator[] getValidator() {

        if (this.validators == null) {
            Set<Validator> result = Sets.newHashSet();

            Map<Validator, PluginExtraProps.Props.ValidatorCfg> validators = (extraProp == null ?
                    Collections.emptyList() : (this.extraProp.getExtraValidators())).stream() //
                    .collect(Collectors.toMap((v) -> ((PluginExtraProps.Props.ValidatorCfg) v).validator //
                            , (v) -> (PluginExtraProps.Props.ValidatorCfg) v));

            PluginExtraProps.Props.ValidatorCfg validatorCfg = null;
            for (Validator v : formField.validate()) {
                if ((validatorCfg = validators.get(v)) != null) {
                    if (!validatorCfg.disable) {
                        result.add(v);
                    }
                } else {
                    result.add(v);
                }
            }
            for (PluginExtraProps.Props.ValidatorCfg cfg : validators.values()) {
                if (!cfg.disable) {
                    result.add(cfg.validator);
                }
            }
            this.validators = result.toArray(new Validator[result.size()]);
        }

        return this.validators;
    }

    @Override
    public boolean isInputRequired() {
        if (inputRequired == null) {
            inputRequired = false;
            for (Validator v : this.getValidator()) {
                if (v == Validator.require) {
                    return inputRequired = true;
                }
            }
        }
        return inputRequired;
    }


    // PropertyType(Method getter) {
    // this(getter.getReturnType(), getter.getGenericReturnType(), getter.toString());
    // }
    @JSONField(serialize = false)
    public Enum[] getEnumConstants() {
        return (Enum[]) fieldClazz.getEnumConstants();
    }

    /**
     * If the property is a collection/array type, what is an item type?
     */
    @JSONField(serialize = false)
    public Class getItemType() {
        if (itemType == null)
            itemType = computeItemType();
        return itemType;
    }

    /**
     *
     * @param instance
     * @return
     */
    @Override
    public Object getFrontendOutput(Object instance) {
        return this.getVal(true, instance);
    }

    /**
     * 取得实例的值
     *
     * @param instance
     * @return
     */
    public Object getVal(boolean serialize2Frontend, Object instance) {

        try {
            Object val = this.f.get(instance);
            if (this.formField.type() == FormFieldType.MULTI_SELECTABLE) {
                return this.getMultiItemsViewType().serialize2Frontend(this.isCollectionType() ? val :
                        Collections.singletonList(val));
            }
            return serialize2Frontend ? serialize2FrontendOutput(val) : val;
            //  return this.formField.type().valProcessor.serialize2Output(this, val);
        } catch (Exception e) {
            throw new RuntimeException("property:" + this.f.getName(), e);
        }
    }

    public <T> T multiSelectablePropProcess(Function<MultiItemsViewType, T> consumer) {
        return multiSelectablePropProcess(consumer, false);
    }

    /**
     * 当类型为multiSelectable 属性的设置
     *
     * @param consumer
     */
    public <T> T multiSelectablePropProcess(Function<MultiItemsViewType, T> consumer, boolean validate) {
        if (this.formField.type() == FormFieldType.MULTI_SELECTABLE) {
            return consumer.apply(this.getMultiItemsViewType());
        }
        if (validate) {
            throw new IllegalStateException(" illegal form type:" + this.formField.type());
        }
        return null;
    }

    @Override
    public void setVal(Object instance, Object val) {

        PropVal fieldVal = new PropVal(val, this.fieldClazz, this);
        try {
            this.f.set(instance, this.formField.type().valProcessor.processInput(instance, fieldVal));
        } catch (Throwable e) {
            throw new RuntimeException("\ntarget instance:" + instance.getClass() + "\nfield:" + this.f.getName() + (
                    "\nprop class:" + val.getClass()), e);
        }
    }

    public static class PropVal {
        private final Object val;
        private final Class targetClazz;
        public final PropertyType propertyType;

        public PropVal(Object val, Class targetClazz, PropertyType propertyType) {
            this.val = val;
            this.targetClazz = targetClazz;
            this.propertyType = propertyType;


        }

        public <T> T convertedVal() {
            return (T) convertUtils.convert(val, this.targetClazz);
        }

        public Object rawVal() {
            return this.val;
        }

        public Class getTargetClazz() {
            return this.targetClazz;
        }
    }

    private Class computeItemType() {
        if (fieldClazz.isArray()) {
            return fieldClazz.getComponentType();
        }
        if (Collection.class.isAssignableFrom(fieldClazz)) {
            Type col = Types.getBaseClass(type, Collection.class);
            if (col instanceof ParameterizedType) {
                return Types.erasure(Types.getTypeArgument(col, 0));
            } else {
                return Object.class;
            }
        }
        return null;
    }

    /**
     * Returns {@link Descriptor} whose 'clazz' is the same as {@link #getItemType() the item type}.
     */
    @JSONField(serialize = false)
    public Descriptor getItemTypeDescriptor() {
        return TIS.get().getDescriptor(getItemType());
    }

    @Override
    public boolean isDescribable() {
        return Describable.class.isAssignableFrom(fieldClazz);
    }

    @Override
    public Class getFieldClazz() {
        return this.fieldClazz;
    }

    public static String getPluginImpl(JSONObject valJ) {
        JSONObject descVal = valJ.getJSONObject(KEY_DESC_VAL);
        final String impl = Objects.requireNonNull(descVal,
                        "prop:" + KEY_DESC_VAL + " json:" + JsonUtil.toString(valJ)) //
                .getString(AttrValMap.PLUGIN_EXTENSION_IMPL);
        return impl;
    }

    @JSONField(serialize = false)
    public Descriptor getItemTypeDescriptorOrDie() {
        Class it = getItemType();
        if (it == null) {
            throw new AssertionError(fieldClazz + " is not an array/collection type in " + displayName + ". See " +
                    "https" + "://wiki.jenkins-ci.org/display/JENKINS/My+class+is+missing+descriptor");
        }
        Descriptor d = TIS.get().getDescriptor(it);
        if (d == null)
            throw new AssertionError(it + " is missing its descriptor in " + displayName + ". See https://wiki" +
                    ".jenkins-ci.org/display/JENKINS/My+class+is+missing+descriptor");
        return d;
    }

    private Function<List<? extends Descriptor>, List<? extends Descriptor>> subDescFilter;

    @JSONField(serialize = false)
    public List<? extends Descriptor> getApplicableDescriptors() {
        return this.applicableDescriptors(true);
    }

    /**
     * Returns all the descriptors that produce types assignable to the property type.
     */
    //@JSONField(serialize = false)
    public List<? extends Descriptor> applicableDescriptors(boolean filterByPropertyScript) {

        JSONObject eprops = null;
        if (filterByPropertyScript && subDescFilter == null && (eprops = this.getExtraProps()) != null) {
            String subDescEnumFilter = eprops.getString(PluginExtraProps.KEY_ENUM_FILTER);
            if (StringUtils.isNotEmpty(subDescEnumFilter)) {
                final Class fieldClazz = this.ownerClazz;
                String className = fieldClazz.getSimpleName() + "_" + this.f.getName() + "_SubFilter";
                String pkg = fieldClazz.getPackage().getName();
                String script = "	package " + pkg + " ;\n"  //
                        + "import java.util.function.Function;\n" //
                        + "import java.util.List;\n" //
                        + "import " + com.qlangtech.tis.extension.Descriptor.class.getName() //
                        + ";\n" //
                        + "class " + className + " implements Function<List<? extends Descriptor>,List<? extends " //
                        + "Descriptor>> { \n" //
                        + "	@Override \n" //
                        + "	public List<? extends Descriptor> apply" //
                        + "(List<?" + " extends Descriptor> desc) {"  //
                        + subDescEnumFilter + "	}" + "}";

                subDescFilter = GroovyShellEvaluate.createParamizerScript(fieldClazz, className, script);
            }
        }

        if (subDescFilter == null) {
            subDescFilter = (descs) -> descs;
        }

        try {

            return subDescFilter.apply(TIS.get().getDescriptorList(extendpointClass()));
        } catch (Exception e) {
            throw new RuntimeException("formField:" + this.f, e);
        }
    }

    public Class extendpointClass() {
        return this.fieldListElementClazz.orElse(this.fieldClazz);
    }

    /**
     * Returns all the descriptors that produce types assignable to the item type for a collection property.
     */
    @JSONField(serialize = false)
    public List<? extends Descriptor> getApplicableItemDescriptors() {
        Class itemType = getItemType();
        if (itemType == null) {
            return null;
        }
        return TIS.get().getDescriptorList(itemType);
    }

    @JSONField(serialize = false)
    public ElementCreatorFactory getCMetaCreator() {
        return Objects.requireNonNull(this.multiItemsViewType, "multiItemsViewType can not be null").tupleFactory;
    }
}
