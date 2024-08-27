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
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.datax.impl.DataxWriter;
import com.qlangtech.tis.extension.impl.AdapterPluginFormProperties;
import com.qlangtech.tis.extension.impl.BaseSubFormProperties;
import com.qlangtech.tis.extension.impl.EnumFieldMode;
import com.qlangtech.tis.extension.impl.PropertyType;
import com.qlangtech.tis.extension.impl.PropValRewrite;
import com.qlangtech.tis.extension.impl.RootFormProperties;
import com.qlangtech.tis.extension.impl.SuFormProperties;
import com.qlangtech.tis.extension.impl.XmlFile;
import com.qlangtech.tis.extension.util.PluginExtraProps;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.plugin.IDataXEndTypeGetter;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.IEndTypeGetter.EndType;
import com.qlangtech.tis.plugin.IEndTypeGetter.IEndType;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.ValidatorCommons;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.SubForm;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.ds.CMeta;
import com.qlangtech.tis.plugin.ds.IMultiElement;
import com.qlangtech.tis.plugin.ds.ISelectedTab;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;
import com.qlangtech.tis.runtime.module.misc.impl.DefaultFieldErrorHandler;
import com.qlangtech.tis.trigger.util.JsonUtil;
import com.qlangtech.tis.util.AttrValMap;
import com.qlangtech.tis.util.DescribableJSON;
import com.qlangtech.tis.util.DescriptorsJSON;
import com.qlangtech.tis.util.ISelectOptionsGetter;
import com.qlangtech.tis.util.PluginMeta;
import com.qlangtech.tis.util.impl.AttrVals;
import com.qlangtech.tis.web.start.TisAppLaunch;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jvnet.tiger_types.Types;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
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
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.qlangtech.tis.runtime.module.misc.impl.DefaultFieldErrorHandler.KEY_VALIDATE_ITEM_INDEX;
import static com.qlangtech.tis.runtime.module.misc.impl.DefaultFieldErrorHandler.KEY_VALIDATE_PLUGIN_INDEX;
import static com.qlangtech.tis.runtime.module.misc.impl.DefaultFieldErrorHandler.popFieldStack;
import static com.qlangtech.tis.runtime.module.misc.impl.DefaultFieldErrorHandler.pushFieldStack;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public abstract class Descriptor<T extends Describable> implements Saveable, ISelectOptionsGetter {

    public static final String SWITCH_OFF = "off";
    public static final String SWITCH_ON = "on";

    public static final String KEY_ENUM_PROP = "enum";

    public static final String KEY_primaryVal = "_primaryVal";
    public static final String KEY_DESC_VAL = "descVal";

    public static final String KEY_OPTIONS = "options";

    private static final String KEY_VALIDATE_METHOD_PREFIX = "validate";
    private static final Pattern validateMethodPattern = Pattern.compile(KEY_VALIDATE_METHOD_PREFIX + "(.+?)");

    /**
     * The class being described by this descriptor.
     */
    public final transient Class<? extends T> clazz;
    public transient boolean overWriteValidateMethod;

    private transient volatile Map<String, IPropertyType> propertyTypes, globalPropertyTypes;
    /**
     * Identity prop of one plugin the plugin must implement the IdentityName interface
     */
    private transient volatile PropertyType identityProp = null;

    private final transient Map<String, Method> validateMethodMap;

    /**
     * this.identityProp
     *
     * @param clazz Pass in {@link #self()} to have the descriptor describe itself,
     *              (this hack is needed since derived types can't call "getClass()" to refer to itself.
     */
    protected Descriptor(Class<? extends T> clazz) {
        if (clazz == self())
            clazz = (Class) getClass();
        this.clazz = clazz;
        this.validateMethodMap = this.createValidateMap();

        // doing this turns out to be very error prone,
        // as field initializers in derived types will override values.
        // load();
    }


    public void cleanPropertyTypes() {
        this.propertyTypes = null;
    }

    /**
     * Infers the type of the corresponding {@link Describable} from the outer class.
     * This version works when you follow the common convention, where a descriptor
     * is written as the static nested class of the describable class.
     *
     * @since 1.278
     */
    protected Descriptor() {
        this.clazz = (Class<T>) getClass().getEnclosingClass();
        if (clazz == null)
            throw new AssertionError(getClass() + " doesn't have an outer class. Use the constructor that takes the " + "Class object explicitly.");
        // detect an type error
        Type bt = Types.getBaseClass(getClass(), Descriptor.class);
        if (bt instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) bt;
            // this 't' is the closest approximation of T of Descriptor<T>.
            Class t = Types.erasure(pt.getActualTypeArguments()[0]);
            if (!t.isAssignableFrom(clazz))
                throw new AssertionError("Outer class " + clazz + " of " + getClass() + " is not assignable to " + t + ". Perhaps wrong outer class?");
        }
        // this prevents a bug like http://www.nabble.com/Creating-a-new-parameter-Type-%3A-Masked-Parameter-td24786554.html
        try {
            Method getd = clazz.getMethod("getDescriptor");
            if (!getd.getReturnType().isAssignableFrom(getClass())) {
                throw new AssertionError(getClass() + " must be assignable to " + getd.getReturnType());
            }
        } catch (NoSuchMethodException e) {
            throw new AssertionError(getClass() + " is missing getDescriptor method.");
        }
        // this.getClass()
        this.initializeValidateMethod();

        this.validateMethodMap = this.createValidateMap();
    }

    private void initializeValidateMethod() {
        List<Class> allSuperclasses = Lists.newArrayList(this.getClass());
        allSuperclasses.addAll(ClassUtils.getAllSuperclasses(this.getClass()));
        for (Class clazz : allSuperclasses) {
            if (clazz == Descriptor.class) {
                break;
            }
            try {
                Method validateMethod = clazz.getDeclaredMethod("verify", IControlMsgHandler.class, Context.class,
                        PostFormVals.class);
                this.overWriteValidateMethod = true;//(validateMethod.getDeclaringClass() != Descriptor.class);
                break;
            } catch (NoSuchMethodException e) {
                //throw new AssertionError(this.getClass() + " is missing validate method.");
            }
        }
    }

    /**
     * Get extract props for client UI initialize
     *
     * @return
     */
    public Map<String, Object> getExtractProps() {
        Map<String, Object> props = new HashMap<>();
        Map<String, Boolean> notebook = new HashMap<>();
        notebook.put("ability", (this instanceof INotebookable));
        notebook.put("activate", TisAppLaunch.get().isZeppelinActivate());
        props.put("notebook", notebook);

        if (this instanceof IEndTypeGetter) {
            appendProps(((IEndTypeGetter) this).getEndType(), (props));
        }
        if (this instanceof IDescribableManipulate) {
            IDescribableManipulate descManipuldate = ((IDescribableManipulate) this);
            Map<String, Object> manipulate = new HashMap<>();
            manipulate.put("extendPoint"
                    , Objects.requireNonNull(descManipuldate.getManipulateExtendPoint()).getName());

            Optional<IPluginStore> manipulateStore = descManipuldate.getManipulateStore();
            manipulateStore.ifPresent((man) -> {
                List<Describable> plugins = man.getPlugins();
                IdentityName id = null;
                Descriptor desc = null;
                JSONArray storeManipuldate = new JSONArray();
                Map<String, Object> eprops = null;
                try {
                    for (Describable plugin : plugins) {
                        if (!(plugin instanceof IdentityName)) {
                            throw new IllegalStateException("plugin must be a IdentityName:" + ToStringBuilder.reflectionToString(plugin));
                        }
                        desc = plugin.getDescriptor();
                        eprops = Maps.newHashMap();
                        id = (IdentityName) plugin;
                        eprops.put(IdentityName.PLUGIN_IDENTITY_NAME, id.identityValue());
                        eprops.put("descMeta", DescriptorsJSON.createPluginFormPropertyTypes(desc, Optional.empty()).getLeft());

//                        if (desc instanceof IEndTypeGetter) {
//                            appendProps(((IEndTypeGetter) desc).getEndType(), (eprops));
//                        }
//                        DescriptorsJSON.setDescInfo(desc,eprops);
                        storeManipuldate.add(eprops);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                manipulate.put("stored", storeManipuldate);
            });
            props.put("manipulate", manipulate);
        }

        return props;
    }

    private Map<String, Object> appendProps(IEndType endType, Map<String, Object> eprops) {
        eprops.put(EndType.KEY_END_TYPE, endType.getVal());
        eprops.put(EndType.KEY_SUPPORT_ICON, endType.getIcon() != null);
        return eprops;
    }

    private Map<String, Method> createValidateMap() {
        ImmutableMap.Builder<String, Method> mapBuilder = new ImmutableMap.Builder<>();
        // validate${fieldName}
        // System.out.println(this.getClass().getName());
        Method[] validateMethods = this.getClass().getMethods();

        Matcher methodMatcher = null;
        String fieldName = null;
        for (Method validateMethod : validateMethods) {
            // System.out.println(validateMethod.getName());
            methodMatcher = validateMethodPattern.matcher(validateMethod.getName());
            if (methodMatcher.matches()) {
                fieldName = StringUtils.uncapitalize(methodMatcher.group(1));
                //KEY_VALIDATE_METHOD_PREFIX
                if (StringUtils.isNotBlank(fieldName)) {
                    // 针对某一个字段进行校验
                    Parameter[] parameters = validateMethod.getParameters();
                    if (parameters.length == 4) {
                        if (// key
                                parameters[0].getType() == IFieldErrorHandler.class && parameters[1].getType() == Context.class && // value
                                        parameters[2].getType() == String.class && parameters[3].getType() == String.class) {
                            if (validateMethod.getReturnType() != Boolean.TYPE) {
                                throw new IllegalStateException("method:" + validateMethod.getName() + " return type "
                                        + "shall be type of boolean");
                            }
                            mapBuilder.put(fieldName, validateMethod);
                            // validateMethodMap.put(fieldName, validateMethod);
                        }
                    }
                }

                //                else {
                //                    // 针对全部属性联合进行校验，例如：在上提交数据库配置表单，服务端需要连接一下数据库进行测试就需要拿到所有表单信息之后惊醒一次校验
                //                    // 这个校验一般是放在字段校验之后进行的
                //
                //                   // mapBuilder.put(KEY_VALIDATE_METHOD_PREFIX, validateMethod);
                //                }
            }
        }
        return mapBuilder.build();
    }


    /**
     * Obtains the property type of the given field of {@link #clazz}
     */
    public IPropertyType getPropertyType(String field) {
        return getPropertyTypes().get(field);
    }

    /**
     * Saves the configuration info to the disk.
     */
    public synchronized void save() {
        try {
            getConfigFile().write(this, Collections.emptySet());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads the data from the disk into this object.
     *
     * <p>
     * The constructor of the derived class must call this method.
     * (If we do that in the base class, the derived class won't
     * get a chance to set default values.)
     */
    public synchronized void load() {
        XmlFile file = getConfigFile();
        if (!file.exists())
            return;
        try {
            file.unmarshal(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected XmlFile getConfigFile() {
        // return new XmlFile(new File(TIS.get().getRootDir(), getId() + ".xml"));
        return getConfigFile(this.getId());
    }


    public static String getPluginFileName(String pluginId) {
        return pluginId + ".xml";
    }

    public static XmlFile getConfigFile(String pluginId) {
        return getConfigFile(pluginId, false);
    }

    public static XmlFile getConfigFile(String pluginId, boolean metaCfgDir) {
        String pluginFileName = getPluginFileName(pluginId);
        return new XmlFile(new File(metaCfgDir ? Config.getMetaCfgDir() : TIS.pluginCfgRoot, pluginFileName),
                pluginFileName);
    }

    public PluginFormProperties getSubPluginFormPropertyTypes(String subFieldName) {
        IPropertyType propertyType = getPropertyTypes().get(subFieldName);
        if (propertyType == null) {
            throw new IllegalStateException(this.clazz.getName() + "'s prop subField:" + subFieldName + " relevant " + "prop can not be null,exist prop keys:" + getPropertyTypes().keySet().stream().collect(Collectors.joining(",")));
        }
        if (!(propertyType instanceof SuFormProperties)) {
            throw new IllegalStateException("subFieldName:" + subFieldName + " prop must be " + SuFormProperties.class.getSimpleName() + "but now is :" + propertyType.getClass().getName());
        }
        return (SuFormProperties) propertyType;
    }

    public List<PluginFormProperties> getSubPluginFormPropertyTypes() {
        return getPropertyTypes().values().stream().filter((pp) -> pp instanceof SuFormProperties).map((pp) -> (SuFormProperties) pp).collect(Collectors.toList());
    }

    public Set<String> getPropertyFields() {
        return getPropertyTypes().keySet();
    }

    public PluginFormProperties getPluginFormPropertyTypes() {
        return getPluginFormPropertyTypes(Optional.empty());
    }

    public PluginFormProperties getPluginFormPropertyTypes(Optional<SubFormFilter> subFormFilter) {

        SubFormFilter filter = null;
        SuFormProperties subPluginFormPropertyTypes;
        if (subFormFilter.isPresent()) {
            filter = subFormFilter.get();

            if (!filter.match(this)) {
                /**
                 *保存子表单聚合内容
                 * 提交表单的时候子表单是 {idfieldName1:{key1:val1,key2:val2},idfieldName2:{key1:val1,key2:val2}} 这样的格式
                 */
                Descriptor parentDesc = filter.getTargetDescriptor();

                SuFormProperties subProps =
                        (SuFormProperties) parentDesc.getSubPluginFormPropertyTypes(filter.subFieldName);

                //  return subProps;

                Objects.requireNonNull(subProps, "prop:" + filter.subFieldName + " relevant subProps can not be null ");

                subPluginFormPropertyTypes = SuFormProperties.copy(
                        PropertyType.filterFieldProp(this.getPropertyTypes(ElementPluginDesc.create(this)))
                        , this.clazz
                        , this
                        , subProps);

                return subPluginFormPropertyTypes.overWriteInstClazz(this.clazz);
            } else {
                subPluginFormPropertyTypes = (SuFormProperties) getSubPluginFormPropertyTypes(filter.subFieldName);

                try {
                    // 类似Hudi的Writer需要覆盖Reader的subFieldName的在Reader的表设置表单中需要设置Hudi相关的属性

                    Descriptor writerDescriptor = IDataxProcessor.getWriterDescriptor(filter.uploadPluginMeta);//
                    // dataxWriter.getClass();
                    if (writerDescriptor != null && writerDescriptor instanceof DataxWriter.IRewriteSuFormProperties) {
                        subPluginFormPropertyTypes =
                                Objects.requireNonNull(((DataxWriter.IRewriteSuFormProperties) writerDescriptor) //
                                                .overwriteSubPluginFormPropertyTypes(subPluginFormPropertyTypes) //
                                        , "result can not be null " + PluginFormProperties.class.getSimpleName());
                    }

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                if (filter.subformDetailView) {
                    final String subformDetailId = filter.subformDetailId;
                    final SuFormProperties _subPluginFormPropertyTypes = subPluginFormPropertyTypes;
                    return new AdapterPluginFormProperties(subPluginFormPropertyTypes) {


                        @Override
                        public JSON getInstancePropsJson(Object instance) {

                            Collection<IdentityName> subFormPropVal =
                                    _subPluginFormPropertyTypes.getSubFormPropVal(instance);
                            for (IdentityName subProp : subFormPropVal) {
                                if (StringUtils.equals(subformDetailId, subProp.identityValue())) {
                                    return _subPluginFormPropertyTypes.convertRootFormProps().getInstancePropsJson(subProp);
//                                    return (new RootFormProperties(
//                                            _subPluginFormPropertyTypes.subFormFieldsDescriptor, _subPluginFormPropertyTypes.fieldsType)).getInstancePropsJson(subProp);
                                }
                            }

                            ISelectedTab subDetailed = _subPluginFormPropertyTypes.newSubDetailed();
                            _subPluginFormPropertyTypes.pkPropertyType.setVal(subDetailed, subformDetailId);
                            return _subPluginFormPropertyTypes.convertRootFormProps().getInstancePropsJson(subDetailed);
                            // return (new RootFormProperties(_subPluginFormPropertyTypes.subFormFieldsDescriptor, _subPluginFormPropertyTypes.fieldsType)).getInstancePropsJson(subDetailed);
                            // throw new IllegalStateException("subformDetailId:" + subformDetailId + " has not find
                            // subForm instance");
                        }
                    };

                } else {
                    return subPluginFormPropertyTypes;
                }
            }
        }

        return new RootFormProperties(this, PropertyType.filterFieldProp(getPropertyTypes()));
    }

    public Map<String, /*** fieldname*/IPropertyType> getPropertyTypes() {
        return getPropertyTypes(ElementPluginDesc.create(this));
    }

    private Map<String, /*** fieldname*/IPropertyType> getPropertyTypes(Optional<ElementPluginDesc> descriptor) {
        if (propertyTypes == null) {
            propertyTypes = Collections.unmodifiableMap(PropertyType.buildPropertyTypes(descriptor, clazz));

            List<PropertyType> identityFields = propertyTypes.values().stream().filter((p) -> {
                return (p instanceof PropertyType) && ((PropertyType) p).isIdentity();
            }).map((p) -> (PropertyType) p).collect(Collectors.toList());
            if (IdentityName.class.isAssignableFrom(this.clazz)) {
                if (identityFields.size() != 1) {
                    throw new IllegalStateException("class:" + this.clazz + " is type of " + IdentityName.class //
                            + " ,size:" + identityFields.size() + " must sign no more than one col:" //
                            + identityFields.stream().map((c) -> c.displayName).collect(Collectors.joining(",")));
                }
                this.identityProp = identityFields.get(0);
            } else {
                if (identityFields.size() > 0) {
                    throw new IllegalStateException("class:" + this.clazz + " is not type of " + IdentityName.class //
                            + " but more than one identity col:" + identityFields.stream().map((c) -> c.displayName).collect(Collectors.joining(",")));
                }
            }


        }
        return propertyTypes;
    }


    //    public final PluginValidateResult verify(IControlMsgHandler msgHandler, Context context //
//            , boolean verify //
//            , AttrVals formData, Optional<IPropertyType.SubFormFilter> subFormFilter) {
//        final PluginFormProperties /** * fieldname */
//                propertyTypes = this.getPluginFormPropertyTypes(subFormFilter);
//        return verify(msgHandler, context, verify, formData, propertyTypes, subFormFilter);
//    }

    /**
     * 校验客户端提交的表单
     *
     * @param msgHandler
     * @param context
     * @param verify     是否进行业务逻辑校验，例如数据库是否能正常连接成功
     * @param formData
     * @return
     */
    public final PluginValidateResult verify(IControlMsgHandler msgHandler, Context context //
            , boolean verify //
            , AttrVals formData, Optional<PluginFormProperties> pTypes, Optional<SubFormFilter> subFormFilter, PropValRewrite propValRewrite) {

        if (context == null) {
            throw new IllegalArgumentException("param contenxt can not be null");
        }
        if (msgHandler == null) {
            throw new IllegalArgumentException("param msgHandler can not be null");
        }
        if (msgHandler == null) {
            throw new IllegalArgumentException("formData msgHandler can not be null");
        }
        try {
            // IRepositoryTargetFile.TARGET_FILE_CONTEXT.set(targetFile);
//            final PluginFormProperties /** * fieldname */
//                    propertyTypes = this.getPluginFormPropertyTypes(subFormFilter);

            PluginFormProperties propertyTypes = getPropertyTypes(pTypes, subFormFilter);

            return propertyTypes.accept(new PluginFormProperties.IVisitor() {
                @Override
                public PluginValidateResult visit(RootFormProperties props) {

                    PostFormVals postFormVals = new PostFormVals(Descriptor.this, props, subFormFilter, msgHandler, context, formData);

                    PluginValidateResult validateResult = new PluginValidateResult(postFormVals,
                            (Integer) context.get(DefaultFieldErrorHandler.KEY_VALIDATE_PLUGIN_INDEX),
                            (Integer) context.get(DefaultFieldErrorHandler.KEY_VALIDATE_ITEM_INDEX));

                    boolean valid = validatePostFormVals(postFormVals, Optional.empty());
                    validateResult.valid = valid;
                    validateResult.setDescriptor(Descriptor.this);
                    return validateResult;
                }

                private boolean validatePostFormVals(PostFormVals postFormVals,
                                                     Optional<SubFormFilter> subFormFilter) {
                    boolean valid = isValid(msgHandler, context, verify, subFormFilter, propertyTypes, postFormVals, propValRewrite);

                    if (valid && verify) {
                        if (!verify(msgHandler, context, postFormVals) //
                                || !validateSubformByParent(subFormFilter, postFormVals)) {
                            valid = false;
                        }
                    }
                    if (valid && !verify) {
                        if (!validateAll(msgHandler, context, postFormVals)//
                                || !validateSubformByParent(subFormFilter, postFormVals)) {
                            valid = false;
                        }
                    }
                    return valid;
                }

                private boolean validateSubformByParent(Optional<SubFormFilter> subFormFilter,
                                                        PostFormVals postFormVals) {
                    if (subFormFilter.isPresent()) {
                        Descriptor parentDesc = subFormFilter.get().getTargetDescriptor();
                        if (parentDesc instanceof SubForm.ISubFormItemValidate) {
                            return ((SubForm.ISubFormItemValidate) parentDesc).validateSubForm( //
                                    msgHandler, context, postFormVals.newInstance());
                        }
                    }

                    return true;
                }

                @Override
                public PluginValidateResult visit(BaseSubFormProperties props) {
                    PluginValidateResult validateResult = null;
                    if (!subFormFilter.isPresent()) {
                        throw new IllegalStateException("subFormFilter must be present");
                    }
                    SubFormFilter filter = subFormFilter.get();
                    if (filter.subformDetailView) {

                        // 校验的时候子表单是{key1:val1,key2:val2} 的格式
                        PostFormVals formVals
                                = new PostFormVals(Descriptor.this, props, subFormFilter, msgHandler, context, formData);
                        // boolean valid = isValid(msgHandler, context, verify, subFormFilter, propertyTypes, formVals);
                        boolean valid = validatePostFormVals(formVals, subFormFilter);
                        if (!valid) {
                            validateResult = new PluginValidateResult(formVals,
                                    (Integer) context.get(DefaultFieldErrorHandler.KEY_VALIDATE_PLUGIN_INDEX),
                                    (Integer) context.get(DefaultFieldErrorHandler.KEY_VALIDATE_ITEM_INDEX));
                            validateResult.valid = false;
                            return validateResult;
                        }
                    } else {

                        if (props.atLeastOne() && (formData.size() < 1)) {
                            // 是否至少要选一个以上的校验
                            msgHandler.addErrorMessage(context, "请至少选择一个");
                            validateResult = new PluginValidateResult(null, 0, 0);
                            validateResult.valid = false;
                            return validateResult;
                        }

                        if (Descriptor.this instanceof SubForm.ISubFormItemValidate) {
                            assert (subFormFilter.isPresent());
                            if (!((SubForm.ISubFormItemValidate) Descriptor.this).validateSubFormItems(msgHandler,
                                    context, props, subFormFilter.get(), formData)) {
                                validateResult = new PluginValidateResult(null, 0, 0);
                                validateResult.valid = false;
                                return validateResult;
                            }
                        }

                        // 提交表单的时候子表单是 {idfieldName1:{key1:val1,key2:val2},idfieldName2:{key1:val1,key2:val2}} 这样的格式
                        SubDetailedPluginValidateResult subDetailedValidateResult = new SubDetailedPluginValidateResult();
                        props.visitAllSubDetailed(context, formData,
                                new SuFormProperties.ISubDetailedProcess<PluginValidateResult>() {
                                    @Override
                                    public PluginValidateResult process(String subFormId, AttrValMap sform) {
                                        PluginValidateResult vResult = sform.validate(msgHandler, context, Optional.of(props.convertRootFormProps()), verify);
                                        // if (!vResult.isValid()) {

                                        subDetailedValidateResult.addSubDetailVaildateResult(subFormId, vResult);

//                                            return vResult;
//                                        }
                                        return (PluginValidateResult) null;
                                    }
                                });

                        if (!subDetailedValidateResult.isValid()) {
                            return subDetailedValidateResult;
                        }

//                        if (validateResult != null) {
//                            return validateResult;
//                        }
                    }


                    validateResult = new PluginValidateResult(null, 0, 0);
                    validateResult.valid = true;
                    return validateResult;
                }
            });
        } finally {
            // IRepositoryTargetFile.TARGET_FILE_CONTEXT.remove();
        }
    }

    private static class SubDetailedPluginValidateResult extends PluginValidateResult {
        Map<String, PluginValidateResult> detailedValidateResult = Maps.newHashMap();

        public SubDetailedPluginValidateResult() {
            super(null, 0, 0);
        }

        public void addSubDetailVaildateResult(String subFormId, PluginValidateResult vResult) {
            this.detailedValidateResult.put(subFormId, vResult);
        }

        @Override
        public boolean isValid() {
            if (this.valid != null) {
                return this.valid;
            }
            for (Map.Entry<String, PluginValidateResult> entry : detailedValidateResult.entrySet()) {
                if (!entry.getValue().isValid()) {
                    return this.valid = false;
                }
            }
            return this.valid = true;
        }
    }


    private boolean isValid(IControlMsgHandler msgHandler, Context context, boolean bizValidate,
                            Optional<SubFormFilter> subFormFilter, PluginFormProperties propertyTypes,
                            PostFormVals postFormVals, PropValRewrite propValRewrite) {

        Objects.requireNonNull(postFormVals, "postFormVals can not be null");
        Map<String, JSONObject> formData = postFormVals.rawFormData.asRootFormVals();
        boolean valid = true;
        String attr;
        PropertyType attrDesc;
        JSONObject valJ;
        String impl;
        String attrVal;
        for (Map.Entry<String, PropertyType> entry : propertyTypes.getKVTuples()) {
            attr = entry.getKey();
            attrDesc = entry.getValue();
            valJ = formData.get(attr);
            if (valJ == null && attrDesc.isInputRequired()) {
                addFieldRequiredError(msgHandler, context, attr);
                valid = false;
                continue;
            }
            if (valJ == null) {
                valJ = new JSONObject();
            }
            if (attrDesc.isDescribable()) {
                JSONObject descVal = valJ.getJSONObject(KEY_DESC_VAL);
                impl = Objects.requireNonNull(descVal, "prop:" + KEY_DESC_VAL + " json:" + JsonUtil.toString(valJ))
                        .getString(AttrValMap.PLUGIN_EXTENSION_IMPL);
                if (StringUtils.isBlank(impl)) {
                    addFieldRequiredError(msgHandler, context, attr);
                    valid = false;
                    continue;
                }
                AttrValMap attrValMap = AttrValMap.parseDescribableMap(Optional.empty(), descVal);
                pushFieldStack(context, attr, 0);
                try {
                    if (!attrValMap.validate(msgHandler, context, bizValidate).isValid()) {
                        valid = false;
                        continue;
                    }
                } finally {
                    popFieldStack(context);
                }
            } else {

                if (attrDesc.typeIdentity() == FormFieldType.MULTI_SELECTABLE.getIdentity()) {
                    List<FormFieldType.SelectedItem> selectedItems = getSelectedMultiItems(msgHandler, context,
                            attrDesc, valJ);
                    if (context.hasErrors()) {
                        return false;
                    }
                    if (selectedItems.size() < 1) {
                        // 没有选中
                        Validator[] validators = attrDesc.getValidator();
                        for (Validator v : validators) {
                            if (v == Validator.require) {
                                v.validate(msgHandler, context, attr, StringUtils.EMPTY);
                            }
                        }
                    } else if (this instanceof FormFieldType.IMultiSelectValidator) {
                        FormFieldType.IMultiSelectValidator multiSelectValidator =
                                (FormFieldType.IMultiSelectValidator) this;
                        if (!multiSelectValidator.validate(msgHandler, subFormFilter, context, attr, selectedItems)) {
                            valid = false;
                            break;
                        }
                    }
                } else {
                    // single value
                    boolean containVal = valJ.containsKey(KEY_primaryVal);

                    if (!containVal && attrDesc.isInputRequired()) {
                        addFieldRequiredError(msgHandler, context, attr);
                        valid = false;
                        continue;
                    }

                    if (containVal) {
                        EnumFieldMode m = null;
                        // 如果是多选列组件
                        if ((m = attrDesc.getEnumFieldMode()) != null && m == EnumFieldMode.MULTIPLE) {
                            JSONArray multiSelected = valJ.getJSONArray(KEY_primaryVal);
                            if (multiSelected.size() < 1) {
                                addFieldRequiredError(msgHandler, context, attr);
                                valid = false;
                                continue;
                            }
                        }
                    }

                    if (containVal) {
                        attrVal = (String) propValRewrite.rewrite(attrDesc, valJ.getString(KEY_primaryVal));
                        postFormVals.fieldVals.put(attr, attrVal);
                        Validator[] validators = attrDesc.getValidator();
                        for (Validator v : validators) {
                            if (!v.validate(msgHandler, context, attr, attrVal)) {
                                valid = false;
                                break;
                            }
                        }
                        try {
                            Method validateMethod = this.validateMethodMap.get(attr);
                            if (validateMethod != null && StringUtils.isNotEmpty(attrVal)) {
                                if (!(boolean) validateMethod.invoke(this, msgHandler, context, attr, attrVal)) {
                                    valid = false;
                                }
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }// end for
        return valid;
    }

    private List<FormFieldType.SelectedItem> getSelectedMultiItems(
            IControlMsgHandler msgHandler, Context context,
            PropertyType attrDesc, JSONObject valJ) {

        // 多选类型的 multi select
        JSONObject eprops = valJ.getJSONObject("_eprops");
        Objects.requireNonNull(eprops, "property '_eprops'   can not be null");
        // enums 格式例子：`com/qlangtech/tis/extension/form-prop-enum-example.json`
        return attrDesc.multiSelectablePropProcess((viewType) -> {
            return viewType.getPostSelectedItems(attrDesc, msgHandler, context, eprops);
        });
        //return attrDesc.getMultiItemsViewType().getPostSelectedItems(attrDesc, msgHandler, context, eprops);

//        return propertyTypes.accept(new PluginFormProperties.IVisitor() {
//            @Override
//            public List<FormFieldType.SelectedItem> visit(RootFormProperties props) {
//                throw new UnsupportedOperationException("RootFormProperties");
//            }
//
//            @Override
//            public List<FormFieldType.SelectedItem> visit(BaseSubFormProperties props) {
//
//            }
//        });


    }

    public static class PluginValidateResult {
        private final PostFormVals itemForm;
        public Boolean valid;
        private Descriptor descriptor;

        // 标注当前 item表单在整个大表单中的位置
        private final Integer validatePluginIndex;
        private final Integer validatePluginItemIndex;

        public PluginValidateResult(PostFormVals itemForm, Integer validatePluginIndex,
                                    Integer validatePluginItemIndex) {
            this.itemForm = itemForm;
            if (validatePluginIndex == null) {
                throw new IllegalArgumentException("param validatePluginIndex can not be null");
            }
            if (validatePluginItemIndex == null) {
                throw new IllegalArgumentException("param validatePluginItemIndex can not be null");
            }
            this.validatePluginIndex = validatePluginIndex;
            this.validatePluginItemIndex = validatePluginItemIndex;
        }

        public void setDescriptor(Descriptor descriptor) {
            this.descriptor = descriptor;
        }

        public static void setValidateItemPos(Context context, Integer pluginIndex, Integer itemIndex) {
            context.put(KEY_VALIDATE_PLUGIN_INDEX, (pluginIndex));
            context.put(KEY_VALIDATE_ITEM_INDEX, (itemIndex));
        }

        public void addIdentityFieldValueDuplicateError(IControlMsgHandler handler, Context context) {
            setValidateItemPos(context, validatePluginIndex, validatePluginItemIndex);
            handler.addFieldError(context, descriptor.getIdentityField().displayName,
                    IdentityName.MSG_ERROR_NAME_DUPLICATE);
        }

        public String getIdentityFieldValue() {
            if (descriptor == null) {
                throw new IllegalStateException("descriptor can not be null");
            }
            return itemForm.getField(descriptor.getIdentityField().displayName);
        }

        public PostFormVals getItemForm() {
            return this.itemForm;
        }

        public <T extends Describable> T newInstance() {
            if (this.descriptor == null) {
                throw new IllegalStateException("descriptor can not be null");
            }
            Describable describable = this.itemForm.newInstance();
            return (T) describable;
        }

        public boolean isValid() {
            return Boolean.TRUE.equals(valid);
        }


    }

    /**
     * 校验整体表单,表单提交不进行校验
     *
     * @param msgHandler
     * @param context
     * @param postFormVals
     * @return true 代表没有错误
     */
    protected boolean verify(IControlMsgHandler msgHandler, Context context, PostFormVals postFormVals) {
        return true;
    }

    /**
     * 校验整体表单,需要进行校验
     *
     * @param msgHandler
     * @param context
     * @param postFormVals
     * @return true 代表没有错误
     */
    protected boolean validateAll(IControlMsgHandler msgHandler, Context context, PostFormVals postFormVals) {
        return true;
    }

    private void addFieldRequiredError(IFieldErrorHandler msgHandler, Context context, String attrKey) {
        msgHandler.addFieldError(context, attrKey, ValidatorCommons.MSG_EMPTY_INPUT_ERROR);
    }


    public ParseDescribable<Describable> newInstance(IControlMsgHandler pluginContext, Context context, //
                                                     FormData formData //
    ) {
        return newInstance(pluginContext, context, formData, Optional.empty(), (propType, val) -> val);
    }


    public ParseDescribable<Describable> newInstance(String appName, //
                                                     FormData formData //
    ) {
        return newInstance(IControlMsgHandler.namedContext(appName), new DefaultContext(), formData, Optional.empty(), (propType, val) -> val);
    }

    public static class FormData extends AttrVals {
        // final HashMap<String, JSONObject> body = Maps.newHashMap();

        public FormData() {
            super(Maps.newHashMap());
        }

        public JSONObject addProp(String key, String val) {
            JSONObject o = new JSONObject();
            o.put(Descriptor.KEY_primaryVal, val);
            this.attrValMap.put(key, o);
            return o;
        }

        public JSONObject addSubForm(String key, String formImpl, FormData form) {
            JSONObject o = new JSONObject();
            JSONObject vals = new JSONObject();
            if (StringUtils.isEmpty(formImpl)) {
                throw new IllegalArgumentException("parm formImpl can not empty");
            }
            vals.put(AttrValMap.PLUGIN_EXTENSION_VALS, form.asRootFormVals());
            vals.put(AttrValMap.PLUGIN_EXTENSION_IMPL, formImpl);

            o.put(KEY_DESC_VAL, vals);
            // attrValMap.put(key, new JSONArray(Collections.singletonList(o)));
            attrValMap.put(key, o);
            return o;
        }
    }

    public ParseDescribable<Describable> newInstance(IControlMsgHandler pluginContext, Context context, //
                                                     AttrValMap.IAttrVals formData, //
                                                     Optional<SubFormFilter> subFormFilter, PropValRewrite propValRewrite) {
        try {
            return parseDescribable(pluginContext, context, formData, Optional.empty(), subFormFilter, propValRewrite);
        } catch (Exception e) {
            throw new RuntimeException("class:" + this.clazz.getName(), e);
        }
    }

//    private ParseDescribable<Describable> parseDescribable(IPluginContext pluginContext
//            , AttrValMap.IAttrVals keyValMap, Optional<IPropertyType.SubFormFilter> subFormFilter) {
//        return parseDescribable(pluginContext, keyValMap, , subFormFilter);
//    }

    public ParseDescribable<Describable> parseDescribable(IControlMsgHandler pluginContext
            , Context context, AttrValMap.IAttrVals keyValMap
            , Optional<PluginFormProperties> pTypes, Optional<SubFormFilter> subFormFilter, PropValRewrite propValRewrite) {

        PluginFormProperties propertyTypes = getPropertyTypes(pTypes, subFormFilter);

        //   PluginFormProperties propertyTypes = this.getPluginFormPropertyTypes(subFormFilter);

        return propertyTypes.accept(new PluginFormProperties.IVisitor() {
            @Override
            public ParseDescribable<Describable> visit(RootFormProperties props) {
                return createPluginInstance();
            }

            private ParseDescribable<Describable> createPluginInstance() {
                try {
                    ParseDescribable<Describable> result = new ParseDescribable<>(clazz.newInstance());
                    Descriptor.this.buildPluginInstance(pluginContext, context, keyValMap.asRootFormVals(), result,
                            propertyTypes, propValRewrite);
                    return result;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            private Describable setParentPluginClass(ParseDescribable<Describable> r) {
                Describable plugin = r.getInstance();
//                if (plugin instanceof IParentHostPluginAware) {
//                    ((IParentHostPluginAware) plugin).setParentHostPluginClass(props.getParentPluginDesc().clazz);
//                }
                return plugin;
            }

            @Override
            public ParseDescribable<Describable> visit(BaseSubFormProperties props) {

                if (!subFormFilter.isPresent()) {
                    throw new IllegalStateException("subFormFilter must be present");
                }
                SubFormFilter filter = subFormFilter.get();
                if (filter.subformDetailView) {
                    return new ParseDescribable<>(setParentPluginClass(createPluginInstance()));
                } else {
                    try {
                        // 子表单聚合提交，多个selected tab 一起提交进行保存
                        //Descriptor targetDescriptor = filter.getTargetDescriptor();


                        // 保存子form detail list
                        List<Describable> subDetailedList = Lists.newArrayList();
                        //AtomicReference<SelectedTabExtend> batchSourceExtendRef = new AtomicReference<>();
                        props.visitAllSubDetailed(null, keyValMap, new SuFormProperties.ISubDetailedProcess<Void>() {
                            public Void process(String subFormId, AttrValMap attrVals) {

                                ParseDescribable<Describable> r = attrVals.createDescribable(pluginContext, context, Optional.of(props.convertRootFormProps()));

                                Describable plugin = setParentPluginClass(r);
//                                if (plugin instanceof IParentHostPluginAware) {
//                                    ((IParentHostPluginAware) plugin).setParentHostPluginClass(props.getParentPluginDesc().clazz);
//                                }
                                // BATCH_SOURCE 类型的 tabExtend 需要将他set到 Selected中去
                                //                                SelectedTabExtend sourceBatchExtend = null;
                                //                                if (plugin instanceof SelectedTabExtend //
                                //                                        && (sourceBatchExtend = (SelectedTabExtend)
                                //                                        plugin).getExtendType() ==
                                //                                        SelectedTabExtend.ExtendType.BATCH_SOURCE) {
                                //                                    batchSourceExtendRef.set(sourceBatchExtend);
                                //                                } else {
                                subDetailedList.add(plugin);
                                //}

                                return null;
                            }
                        });
                        //                        SelectedTabExtend tabExtend = batchSourceExtendRef.get();
                        //                        if (tabExtend != null) {
                        //                            subDetailedList.stream().filter((tab) -> tab instanceof
                        //                            SelectedTab) //
                        //                                    .map((tab) -> (SelectedTab) tab)//
                        //                                    .forEach((tab) -> {
                        //                                        tab.setSourceProps(tabExtend);
                        //                                    });
                        //                        }


                        // props.subFormField.set(result.instance, subDetailedList);
                        return new ParseDescribable<>(subDetailedList);

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    private PluginFormProperties getPropertyTypes(Optional<PluginFormProperties> pTypes, Optional<SubFormFilter> subFormFilter) {
        return pTypes.map((pp) -> {
            return (Descriptor.this == pp.getDescriptor()) ? pp : null;
        }).orElseGet(() -> {
            return this.getPluginFormPropertyTypes(subFormFilter);
        });
    }

    private <TARGET extends Describable<TARGET>> TARGET buildPluginInstance(IControlMsgHandler pluginContext, Context context //
            , Map<String, JSONObject> keyValMap //
            , ParseDescribable<TARGET> result, PluginFormProperties propertyTypes, PropValRewrite propValRewrite) {
        TARGET describable = result.getInstance();
        String attr;
        PropertyType attrDesc;
        JSONObject valJ;
        String impl;
        Descriptor descriptor;
        Object attrVal;
        for (Map.Entry<String, PropertyType> entry : propertyTypes.getKVTuples()) {
            attr = entry.getKey();
            attrDesc = entry.getValue();
            valJ = keyValMap.get(attr);
            // attrDesc.getExtraProps(PluginExtraProps.KEY_DISABLE);
            if (valJ == null && attrDesc.isInputRequired()) {
                throw new IllegalStateException("prop:" + attr + " can not be empty,desc :" + this.clazz.getSimpleName());
            }
            if (valJ == null) {
                valJ = new JSONObject();
            }
            if (attrDesc.isDescribable()) {
                JSONObject descVal = Objects.requireNonNull(valJ.getJSONObject(KEY_DESC_VAL),
                        "key:" + KEY_DESC_VAL + " relevant instant can not be null");
                impl = descVal.getString(AttrValMap.PLUGIN_EXTENSION_IMPL);
                descriptor = TIS.get().getDescriptor(impl);
                if (descriptor == null) {
                    throw new IllegalStateException("impl:" + impl + " relevant descripotor can not be null");
                }
                ParseDescribable vals = descriptor.newInstance(pluginContext, context,
                        parseAttrValMap(descVal.get(AttrValMap.PLUGIN_EXTENSION_VALS)), Optional.empty(), propValRewrite);
                attrDesc.setVal(describable, propValRewrite.rewrite(attrDesc, vals.getInstance()));
            } else {

                if (attrDesc.typeIdentity() == FormFieldType.MULTI_SELECTABLE.getIdentity()) {
                    List<FormFieldType.SelectedItem> selectedItems = getSelectedMultiItems(pluginContext, context, attrDesc, valJ);
                    List<IMultiElement> multi = selectedItems.stream().filter((item) -> item.isChecked()).map((item) -> {
                        if (item.getCmeta() != null) {
                            return item.getCmeta();
                        } else {
                            CMeta c = new CMeta();
                            c.setName(item.getName());
                            return c;
                        }
                    }).collect(Collectors.toList());
                    //attrDesc.is

                    if (attrDesc.isCollectionType()) {
                        attrDesc.setVal(describable, propValRewrite.rewrite(attrDesc, multi));
                    } else {
                        for (IMultiElement type : multi) {
                            attrDesc.setVal(describable, propValRewrite.rewrite(attrDesc, type));
                        }
                    }

                } else {

                    boolean containVal =
                            valJ.containsKey(KEY_primaryVal) && StringUtils.isNotBlank(valJ.getString(KEY_primaryVal));
                    // describable
                    if (!containVal && attrDesc.isInputRequired()) {
                        throw new IllegalStateException("prop:" + attr + " can not be empty ,descriptor:" + this.clazz.getSimpleName());
                    }
                    if (containVal) {
                        attrVal = valJ.get(KEY_primaryVal);
                        attrDesc.setVal(describable, propValRewrite.rewrite(attrDesc, attrVal));
                        if (valJ.containsKey(KEY_OPTIONS)) {
                            JSONArray options = valJ.getJSONArray(KEY_OPTIONS);
                            JSONObject opt = null;
                            for (int i = 0; i < options.size(); i++) {
                                opt = options.getJSONObject(i);
                                try {
                                    // 将options中的选中的插件来源记录下来，后续在集群中各组件中传输插件可以用
                                    if (StringUtils.equals((String) attrVal, opt.getString("name"))) {
                                        Class<?> implClass =
                                                TIS.get().pluginManager.uberClassLoader.loadClass(opt.getString("impl"
                                                ));
                                        PluginWrapper pluginWrapper = TIS.get().pluginManager.whichPlugin(implClass);
                                        PluginMeta pluginMeta = Objects.requireNonNull(pluginWrapper,
                                                "implClass:" + implClass + " relevant pluginWrapper can not be null").getDesc();
                                        result.extraPluginMetas.add(pluginMeta);
                                        break;
                                    }
                                } catch (ClassNotFoundException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }
                }
            }
        }
        return describable;
    }

    public String getIdentityValue(T tDescribable) {
        return (String) getIdentityField().getVal(tDescribable);
    }

    public PropertyType getIdentityField() {
        if (identityProp == null) {
            getPropertyTypes();
            if (identityProp == null) {
                throw new IllegalStateException("property identityProp can not be null,desc:" + this.getClass());
            }
        }
        return identityProp;
    }

    public static class ParseDescribable<T extends Object> {

        private final List<T> instance;
        public final boolean subFormFields;

        public final List<PluginMeta> extraPluginMetas = Lists.newArrayList();

        public ParseDescribable(T instance) {
            this(Collections.singletonList(instance), false);
        }

        public List<T> getSubFormInstances() {
            return this.instance;
        }

        public <TT> TT getInstance() {
            if (subFormFields) {
                throw new IllegalStateException("has multi instance");
            }
            Optional<T> first = this.instance.stream().findFirst();
            return first.isPresent() ? (TT) first.get() : null;
        }

        public ParseDescribable(List<T> instance) {
            this(instance, true);
        }

        private ParseDescribable(List<T> instance, boolean subFormFields) {
            this.instance = instance;
            this.subFormFields = subFormFields;
        }
    }

    public static AttrVals parseAttrValMap(Object vals) {
        Map<String, JSON> attrValMap = Maps.newHashMap();
        if (vals == null) {
            return new AttrVals(attrValMap);
        }
        // Object vals = jsonObject.get("vals");
        if (vals instanceof Map) {
            ((Map<String, Object>) vals).forEach((attrName, val) -> {
                try {
                    attrValMap.put(attrName, (JSON) val);
                } catch (Exception e) {
                    // 在multiSelectItem的场景下，可能存在提交的itemProperty没有使用‘ItemPropVal’包装的情况
                    if (val instanceof String) {
                        JSONObject o = new JSONObject();
                        o.put(Descriptor.KEY_primaryVal, val);
                        attrValMap.put(attrName, o);
                    } else {
                        throw new RuntimeException("attrName:" + attrName + ",valType:" + val.getClass().getSimpleName(), e);
                    }
                }
            });
        }
        return new AttrVals(attrValMap);
    }


    public final boolean isSubTypeOf(Class type) {
        return type.isAssignableFrom(clazz);
    }

    public String getDisplayName() {
        return clazz.getSimpleName();
    }

    public String getId() {
        return clazz.getName();
    }

    /**
     * Unlike {@link #clazz}, return the parameter type 'T', which determines
     * the {@link DescriptorExtensionList} that this goes to.
     *
     * <p>
     * In those situations where subtypes cannot provide the type parameter,
     * this method can be overridden to provide it.
     */
    public Class<T> getT() {
        Type subTyping = Types.getBaseClass(getClass(), Descriptor.class);
        if (!(subTyping instanceof ParameterizedType)) {
            throw new IllegalStateException(getClass() + " doesn't extend Descriptor with a type parameter.");
        }
        return Types.erasure(Types.getTypeArgument(subTyping, 0));
    }

    /**
     * Special type indicating that {@link Descriptor} describes itself.
     *
     * @see Descriptor#Descriptor(Class)
     */
    public static final class Self {
    }

    protected static Class self() {
        return Self.class;
    }

    private final Map<String, Callable<List<? extends IdentityName>>> /*** fieldname*/
            selectOptsRegister = Maps.newHashMap();

    /**
     * 如果插件中有selectable的控件，则在descriptor中需要注册selectable控件中的内容
     *
     * @param fieldName
     * @param getter
     */
    protected final void registerSelectOptions(String fieldName, Callable<List<? extends IdentityName>> getter) {
        selectOptsRegister.put(fieldName, getter);
    }

    @Override
    public final List<SelectOption> getSelectOptions(String name) {
        Callable<List<? extends IdentityName>> opsCallable = selectOptsRegister.get(name);
        if (opsCallable == null) {
            throw new IllegalStateException("fieldName:" + name + " is select options has not been register,class:"
                    + this.getClass().getName()
                    + ",has registed:" + selectOptsRegister.keySet().stream().collect(Collectors.joining(",")));
        }
        try {
            List<? extends IdentityName> opts = opsCallable.call();
            if (opts == null) {
                return Collections.emptyList();
            }
            return opts.stream().map((r) -> {
//                Descriptor desc = null;
//                EndType endType = null;
//                if ((desc = ((Describable) r).getDescriptor()) instanceof IEndTypeGetter) {
//                    endType = ((IEndTypeGetter) desc).getEndType();
//                }
                return new SelectOption(r.identityValue(), r.getDescribleClass(), null);
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("field name:" + name + ",class:" + this.getClass().getName(), e);
        }
    }

    public static class SelectOption {

        private final String name;

        private final Class<?> implClass;
        private final IDataXEndTypeGetter.EndType endType;

        public SelectOption(String name, Class<?> implClass, IDataXEndTypeGetter.EndType endType) {
            this.name = name;
            this.implClass = implClass;
            this.endType = endType;
        }

        public String getEndType() {
            if (this.endType != null) {
                return this.endType.getVal();
            }
            return null;
        }

        public String getName() {
            return name;
        }

        public String getImpl() {
            return implClass.getName();
        }
    }

    public static class PostFormVals {
        // public final Map<String, /*** attr key */com.alibaba.fastjson.JSONObject> rawFormData;
        public final AttrValMap.IAttrVals rawFormData;
        private final Descriptor desc;
        private final PluginFormProperties formProperties;
        private final Optional<SubFormFilter> subFormFilter;

        private final IControlMsgHandler msgHandler;
        private final Context context;

        private Describable instance;

        public <T extends Describable> T newInstance() {
            if (instance == null) {

                ParseDescribable<Describable> plugin = desc.parseDescribable(
                        msgHandler, this.context, this.rawFormData, Optional.of(this.formProperties), subFormFilter, ((propType, val) -> val));

//                ParseDescribable<Describable> plugin = desc.newInstance((IPluginContext) msgHandler, this.rawFormData
//                        , Optional.empty());
                instance = plugin.getInstance();
            }
            return (T) instance;
        }

        public PostFormVals(Descriptor desc //
                , IControlMsgHandler msgHandler, Context context, AttrValMap.IAttrVals rawFormData) {
            this(desc, desc.getPluginFormPropertyTypes(Optional.empty())
                    , Optional.empty(), msgHandler, context, rawFormData);
        }

        public PostFormVals(Descriptor desc, PluginFormProperties formProperties //
                , Optional<SubFormFilter> subFormFilter //
                , IControlMsgHandler msgHandler, Context context, AttrValMap.IAttrVals rawFormData) {
            this.rawFormData = rawFormData;
            this.desc = desc;
            this.formProperties = formProperties;
            this.subFormFilter = subFormFilter;
            this.msgHandler = msgHandler;
            this.context = context;
        }

        private Map<String, String> fieldVals = Maps.newHashMap();

        public String getField(String key) {
            return fieldVals.get(key);
        }
    }

    public PluginExtraProps fieldExtraDescs = new PluginExtraProps();

    public void addFieldDescriptor(String fieldName, Object dftVal, String helperContent) {
        this.addFieldDescriptor(fieldName, dftVal, helperContent, Optional.empty());
    }

    public PluginExtraProps.Props addFieldDescriptor(String fieldName, Object dftVal, String helperContent,
                                                     Optional<List<Option>> enums) {
        JSONObject c = new JSONObject();
        c.put(PluginExtraProps.KEY_DFTVAL_PROP, dftVal);
        PluginExtraProps.Props props = new PluginExtraProps.Props(c);
        if (StringUtils.isNotEmpty(helperContent)) {
            props.tagAsynHelp(new StringBuffer(helperContent));
        }
        if (enums.isPresent()) {
            c.put(KEY_ENUM_PROP, Option.toJson(enums.get()));
        }
        this.fieldExtraDescs.put(fieldName, props);
        return props;
    }


}
