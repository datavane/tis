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
import com.google.common.collect.Sets;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.ElementPluginDesc;
import com.qlangtech.tis.extension.IPropertyType;
import com.qlangtech.tis.extension.util.GroovyShellEvaluate;
import com.qlangtech.tis.extension.util.GroovyShellUtil;
import com.qlangtech.tis.extension.util.MultiItemsViewType;
import com.qlangtech.tis.extension.util.PluginExtraProps;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.SubForm;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.ds.CMeta;
import com.qlangtech.tis.plugin.ds.DataTypeMeta;
import com.qlangtech.tis.plugin.ds.ElementCreatorFactory;
import com.qlangtech.tis.runtime.module.misc.IMessageHandler;
import com.qlangtech.tis.trigger.util.UnCacheString;
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

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-11 12:07
 */
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

    // private final Optional<Descriptor.ElementPluginDesc> parentPluginDesc;
    public final Class clazz;

    public final Type type;

    private volatile Class itemType;

    public final String displayName;

    public final FormField formField;

    public final Field f;


    private Boolean inputRequired;

    private MultiItemsViewType multiItemsViewType;

    public PluginExtraProps.Props extraProp;

    public PropertyType(Field f, FormField formField) {
        this(f, f.getType(), f.getGenericType(), f.getName(), formField);
    }

    PropertyType(Field f, Class clazz, Type type, String displayName, FormField formField) {
        this.f = f;
        this.clazz = clazz;
        this.type = type;
        this.displayName = displayName;
        if (formField == null) {
            throw new IllegalStateException("param formField can not be null");
        }
        this.formField = formField;
    }

    @Override
    public boolean isCollectionType() {
     //   PropertyType pt = (PropertyType) propertyType;
        return List.class.isAssignableFrom(this.clazz);
    }

    public static Map<String, /*** fieldname*/PropertyType> filterFieldProp(Map<String,
            /*** fieldname*/IPropertyType> props) {
        return props.entrySet().stream().filter((e) -> e.getValue() instanceof PropertyType) //
                .collect(Collectors.toMap((e) -> e.getKey(), (e) -> (PropertyType) e.getValue()));
    }

    /**
     * 可能plugin form 表单需要几个步骤才能 填充完一个plugin form 表单就需要单独取出部分表单属性去渲染前端页面
     *
     * @param clazz
     * @return
     */
    public static Map<String, /*** fieldname */IPropertyType> buildPropertyTypes( //
                                                                                  Optional<ElementPluginDesc> descriptor,
                                                                                  Class<? extends Describable> clazz) {
        try {
            Map<String, IPropertyType> propMapper = new HashMap<>();

            Optional<PluginExtraProps> extraProps = PluginExtraProps.load(descriptor, clazz);

            // 支持使用继承的方式来实现复用，例如：DataXHiveWriter继承DataXHdfsWriter来实现
            PluginExtraProps.visitAncestorsClass(clazz, new PluginExtraProps.IClassVisitor<Void>() {
                @Override
                public Void process(Class<?> targetClass, Void v) {
                    FormField formField = null;
                    SubForm subFormFields = null;
                    //   ptype = null;

                    Class<? extends Describable> subFromDescClass = null;
                    try {
                        for (Field f : targetClass.getDeclaredFields()) {
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
                                                "subFromDescClass:" + subFromDescClass + " relevant descriptor can not be null");

                                propMapper.put(f.getName(), new SuFormProperties(clazz, f, subFormFields, subFormDesc,
                                        filterFieldProp(buildPropertyTypes(ElementPluginDesc.create(subFormDesc), subFromDescClass))));
                            } else if ((formField = f.getAnnotation(FormField.class)) != null) {

                                PluginExtraProps.Props fieldExtraProps = null;
                                final PropertyType ptype = new PropertyType(f, formField);
                                if (extraProps.isPresent() && (fieldExtraProps = extraProps.get().getProp(f.getName())) != null) {

                                    ptype.setExtraProp(fieldExtraProps);
                                    String placeholder = fieldExtraProps.getPlaceholder();
                                    Object dftVal = fieldExtraProps.getDftVal();
                                    String help = fieldExtraProps.getHelpContent();
                                    JSONObject props = fieldExtraProps.getProps();
                                    // Object disabled = props.get(PluginExtraProps.KEY_DISABLE);

//                                    if (disabled != null) {
//                                        if (StringUtils.startsWith(String.valueOf(disabled), IMessageHandler.TSEARCH_PACKAGE)) {
//                                            props.put(PluginExtraProps.KEY_DISABLE, GroovyShellEvaluate.scriptEval(String.valueOf(disabled)));
//                                        }
//                                    }
//                                    disabled = props.get(PluginExtraProps.KEY_DISABLE);
//                                    if (disabled != null) {
//                                        if ((disabled instanceof JsonUtil.UnCacheString && ((JsonUtil.UnCacheString<Boolean>) disabled).getValue())
//                                                || (disabled instanceof Boolean && (Boolean) disabled)
//                                        ) {
//                                            propMapper.remove(f.getName());
//                                            continue;
//                                        }
//                                    }

                                    if (fieldExtraProps.getBoolean(PluginExtraProps.KEY_DISABLE)) {
                                        propMapper.remove(f.getName());
                                        continue;
                                        //return null;
                                    }

                                    if (StringUtils.isNotEmpty(help) && StringUtils.startsWith(help,
                                            IMessageHandler.TSEARCH_PACKAGE)) {
                                        props.put(PluginExtraProps.Props.KEY_HELP, GroovyShellEvaluate.eval(help));
                                    }

                                    if (dftVal != null && StringUtils.startsWith(String.valueOf(dftVal),
                                            IMessageHandler.TSEARCH_PACKAGE)) {
                                        final PropertyType pt = ptype;

                                        Function<Object, Object> process = pt.getEnumFieldMode() != null ?
                                                pt.getEnumFieldMode().createDefaultValProcess(targetClass, f) :
                                                Function.identity();

                                        props.put(PluginExtraProps.KEY_DFTVAL_PROP,
                                                GroovyShellEvaluate.scriptEval(String.valueOf(dftVal), process));
                                    }

                                    if (placeholder != null && StringUtils.startsWith(placeholder,
                                            IMessageHandler.TSEARCH_PACKAGE)) {
                                        props.put(PluginExtraProps.KEY_PLACEHOLDER_PROP,
                                                GroovyShellEvaluate.scriptEval(placeholder));
                                    }

                                    if (descriptor.isPresent() //
                                            && (formField.type() == FormFieldType.ENUM)) {
                                        resolveEnumProp(descriptor.get().getElementDesc(), fieldExtraProps, (opts) -> {
                                            return Option.toJson((List<Option>) opts);
                                        });
                                    }

                                    if (descriptor.isPresent() //
                                            && (formField.type() == FormFieldType.MULTI_SELECTABLE)) {

                                        final PluginExtraProps.Props feProps = fieldExtraProps;

                                        ElementPluginDesc paretPluginRef = descriptor.get();
                                        resolveEnumProp(paretPluginRef.getElementDesc(), feProps, (cols) -> {
                                            final List<CMeta> mcols = (List<CMeta>) cols;
//                                            if (CollectionUtils.isEmpty(mcols)) {
//                                                throw new IllegalStateException("mcols can not be empty");
//                                            }
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
                                // throw new IllegalStateException("field:" + f.getName() + " is illegal");
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return null;
                }


            });

            return propMapper;
        } catch (Exception e) {
            throw new RuntimeException("parse desc:" + clazz.getName(), e);
        }
    }

    private static JSONArray resolveEnumProp(Descriptor descriptor, PluginExtraProps.Props fieldExtraProps,
                                             Function<Object, Object> process) {
        Object anEnum = fieldExtraProps.getProps().get(Descriptor.KEY_ENUM_PROP);
        JSONArray enums = new JSONArray();
        if (anEnum != null && anEnum instanceof String) {
            try {
                GroovyShellUtil.descriptorThreadLocal.set(descriptor);
                fieldExtraProps.getProps().put(Descriptor.KEY_ENUM_PROP, GroovyShellEvaluate.scriptEval((String) anEnum, process));
            } finally {
                GroovyShellUtil.descriptorThreadLocal.remove();
            }
        }
        return enums;
    }

    public static Map<String, /*** fieldname*/PropertyType> filterFieldProp(Descriptor descriptor) {
        return filterFieldProp(descriptor.getPropertyTypes());
    }


    private MultiItemsViewType getMultiItemsViewType() {
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
    public boolean isIdentity() {
        return this.formField.identity();
    }

    public JSONObject getExtraProps() {
        if (this.extraProp == null) {
            return null;
        }
        return this.extraProp.getProps();
    }

    @JSONField(serialize = false)
    public EnumFieldMode getEnumFieldMode() {
        if (this.formField.type() != FormFieldType.ENUM) {
            return null;
        }
        return EnumFieldMode.parse(this.extraProp != null ? getExtraProps().getString("enumMode") : null);
    }


    public void setExtraProp(PluginExtraProps.Props extraProp) {
        this.extraProp = extraProp;
    }

    public Object dftVal() {
        if (this.extraProp == null) {
            return null;
        }
        return this.extraProp.getDftVal();
    }

    public int ordinal() {
        return formField.ordinal();
    }

    public boolean advance() {
        return (this.extraProp != null && this.extraProp.isAdvance()) || formField.advance();
    }

    public int typeIdentity() {
        return formField.type().getIdentity();
    }

    private Validator[] validators;

    public Validator[] getValidator() {

        if (this.validators == null) {
            Set<Validator> result = Sets.newHashSet();

            Map<Validator, PluginExtraProps.Props.ValidatorCfg> validators
                    = (extraProp == null ? Collections.emptyList() : (this.extraProp.getExtraValidators()))
                    .stream().collect(Collectors.toMap((v) -> ((PluginExtraProps.Props.ValidatorCfg) v).validator, (v) -> (PluginExtraProps.Props.ValidatorCfg) v));

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
    public Enum[] getEnumConstants() {
        return (Enum[]) clazz.getEnumConstants();
    }

    /**
     * If the property is a collection/array type, what is an item type?
     */
    public Class getItemType() {
        if (itemType == null)
            itemType = computeItemType();
        return itemType;
    }

    /**
     * 取得实例的值
     *
     * @param instance
     * @return
     */
    public Object getVal(Object instance) {

        try {
            Object val = this.f.get(instance);
            if (this.formField.type() == FormFieldType.MULTI_SELECTABLE) {

                return this.getMultiItemsViewType().serialize2Frontend(val);
                //  return this.extraProp.multiItemsViewType(this.getParentHostClass()).serialize2Frontend(val);
            }

            return this.formField.type().valProcessor.serialize2Output(this, val);
        } catch (Exception e) {
            throw new RuntimeException(e);
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

    public void setVal(Object instance, Object val) {
        PropVal fieldVal = new PropVal(val, this.clazz, this.extraProp);
        try {
            this.f.set(instance, this.formField.type().valProcessor.processInput(instance, fieldVal));
        } catch (Throwable e) {
            throw new RuntimeException("\ntarget instance:" + instance.getClass()
                    + "\nfield:" + this.f.getName() + ("\nprop class:" + val.getClass()), e);
        }
    }

    public static class PropVal {
        private final Object val;
        private final Class targetClazz;
        public final PluginExtraProps.Props extraProp;

        public PropVal(Object val, Class targetClazz, PluginExtraProps.Props extraProp) {
            this.val = val;
            this.targetClazz = targetClazz;
            this.extraProp = extraProp;
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
        if (clazz.isArray()) {
            return clazz.getComponentType();
        }
        if (Collection.class.isAssignableFrom(clazz)) {
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
    public Descriptor getItemTypeDescriptor() {
        return TIS.get().getDescriptor(getItemType());
    }

    public boolean isDescribable() {
        // }
        return Describable.class.isAssignableFrom(clazz);
    }

    public Descriptor getItemTypeDescriptorOrDie() {
        Class it = getItemType();
        if (it == null) {
            throw new AssertionError(clazz + " is not an array/collection type in " + displayName + ". See " + "https"
                    + "://wiki.jenkins-ci.org/display/JENKINS/My+class+is+missing+descriptor");
        }
        Descriptor d = TIS.get().getDescriptor(it);
        if (d == null)
            throw new AssertionError(it + " is missing its descriptor in " + displayName + ". See https://wiki" +
                    ".jenkins-ci.org/display/JENKINS/My+class+is+missing+descriptor");
        return d;
    }

    private Function<List<? extends Descriptor>, List<? extends Descriptor>> subDescFilter;

    /**
     * Returns all the descriptors that produce types assignable to the property type.
     */
    public List<? extends Descriptor> getApplicableDescriptors() {

        JSONObject eprops = null;
        if (subDescFilter == null && (eprops = this.getExtraProps()) != null) {
            String subDescEnumFilter = eprops.getString(PluginExtraProps.KEY_ENUM_FILTER);
            if (StringUtils.isNotEmpty(subDescEnumFilter)) {
                String className = this.clazz.getSimpleName() + "_" + this.f.getName() + "_SubFilter";
                String pkg = this.clazz.getPackage().getName();
                String script = "	package " + pkg + " ;\n"  //
                        + "import java.util.function.Function;\n" //
                        + "import java.util.List;\n" //
                        + "import " + com.qlangtech.tis.extension.Descriptor.class.getName() + ";\n"
                        // + "import com.qlangtech.plugins.incr.flink.chunjun.sink.SinkTabPropsExtends;\n"
                        + "class " + className + " implements Function<List<? extends Descriptor>,List<? extends " //
                        + "Descriptor>> { \n" //
                        + "	@Override \n" //
                        + "	public List<? extends Descriptor> apply" //
                        + "(List<?" + " extends Descriptor> desc) {"  //
                        + subDescEnumFilter + "	}" + "}";

                subDescFilter = GroovyShellEvaluate.createParamizerScript(this.clazz, className, script);
            }
        }

        if (subDescFilter == null) {
            subDescFilter = (descs) -> descs;
        }

        return subDescFilter.apply(TIS.get().getDescriptorList(clazz));
    }

    /**
     * Returns all the descriptors that produce types assignable to the item type for a collection property.
     */
    public List<? extends Descriptor> getApplicableItemDescriptors() {
        Class itemType = getItemType();
        if (itemType == null) {
            return null;
        }
        return TIS.get().getDescriptorList(itemType);
    }

    public ElementCreatorFactory getCMetaCreator() {
        return this.multiItemsViewType.tupleFactory;
    }
}
