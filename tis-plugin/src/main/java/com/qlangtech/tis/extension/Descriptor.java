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
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.datax.impl.DataxWriter;
import com.qlangtech.tis.extension.impl.*;
import com.qlangtech.tis.extension.util.GroovyShellEvaluate;
import com.qlangtech.tis.extension.util.PluginExtraProps;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.ValidatorCommons;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.SubForm;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.runtime.module.misc.IControlMsgHandler;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;
import com.qlangtech.tis.runtime.module.misc.IMessageHandler;
import com.qlangtech.tis.runtime.module.misc.impl.DefaultFieldErrorHandler;
import com.qlangtech.tis.util.AttrValMap;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.ISelectOptionsGetter;
import com.qlangtech.tis.util.XStream2;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.jvnet.tiger_types.Types;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.qlangtech.tis.runtime.module.misc.impl.DefaultFieldErrorHandler.*;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public abstract class Descriptor<T extends Describable> implements Saveable, ISelectOptionsGetter {

    public static final String KEY_ENUM_PROP = "enum";

    public static final String KEY_primaryVal = "_primaryVal";

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
            throw new AssertionError(getClass() + " doesn't have an outer class. Use the constructor that takes the Class object explicitly.");
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
                Method validateMethod = clazz.getDeclaredMethod(
                        "verify", IControlMsgHandler.class, Context.class, PostFormVals.class);
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
        return Collections.emptyMap();
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
                                throw new IllegalStateException("method:" + validateMethod.getName() + " return type shall be type of boolean");
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
        return new XmlFile(new File(TIS.pluginCfgRoot, getPluginFileName(pluginId)));
    }

    private PluginFormProperties getSubPluginFormPropertyTypes(String subFieldName) {
        IPropertyType propertyType = getPropertyTypes().get(subFieldName);
        if (propertyType == null) {
            throw new IllegalStateException(this.clazz.getName() + "'s prop subField:" + subFieldName + " relevant prop can not be null,exist prop keys:"
                    + getPropertyTypes().keySet().stream().collect(Collectors.joining(",")));
        }
        if (!(propertyType instanceof SuFormProperties)) {
            throw new IllegalStateException("subFieldName:" + subFieldName + " prop must be "
                    + SuFormProperties.class.getSimpleName() + "but now is :" + propertyType.getClass().getName());
        }
        return (SuFormProperties) propertyType;
    }

    public List<PluginFormProperties> getSubPluginFormPropertyTypes() {
        return getPropertyTypes().values().stream()
                .filter((pp) -> pp instanceof SuFormProperties)
                .map((pp) -> (SuFormProperties) pp).collect(Collectors.toList());
    }

    public Set<String> getPropertyFields() {
        return getPropertyTypes().keySet();
    }

    public PluginFormProperties getPluginFormPropertyTypes() {
        return getPluginFormPropertyTypes(Optional.empty());
    }

    public PluginFormProperties getPluginFormPropertyTypes(Optional<IPropertyType.SubFormFilter> subFormFilter) {
        IPropertyType.SubFormFilter filter = null;
        if (subFormFilter.isPresent()) {
            filter = subFormFilter.get();
            if (filter.match(this)) {
                SuFormProperties subPluginFormPropertyTypes = (SuFormProperties) getSubPluginFormPropertyTypes(filter.subFieldName);


                try {

                    // 类似Hudi的Writer需要覆盖Reader的subFieldName的在Reader的表设置表单中需要设置Hudi相关的属性
                    //   DataxWriter dataxWriter = DataxWriter.load(filter.uploadPluginMeta.getPluginContext(), dataXName);
                    Descriptor writerDescriptor
                            = IDataxProcessor.getWriterDescriptor(filter.uploadPluginMeta);// dataxWriter.getClass();
                    if (writerDescriptor instanceof DataxWriter.IRewriteSuFormProperties) {
                        return Objects.requireNonNull(((DataxWriter.IRewriteSuFormProperties) writerDescriptor)
                                        .overwriteSubPluginFormPropertyTypes(subPluginFormPropertyTypes)
                                , "result can not be null " + PluginFormProperties.class.getSimpleName());
                    }
//                    String overwriteSubField = IOUtils.loadResourceFromClasspath(
//                            writerClass, writerClass.getSimpleName() + "." + filter.subFieldName + ".json", false);
//                    if (overwriteSubField != null) {
//                        JSONObject subField = JSON.parseObject(overwriteSubField);
//                        Class<?> clazz = writerClass.getClassLoader().loadClass(subField.getString(SubForm.FIELD_DES_CLASS));
//                        return SuFormProperties.copy(filterFieldProp(buildPropertyTypes(this, clazz)), subPluginFormPropertyTypes);
//                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                if (filter.subformDetailView) {
                    final String subformDetailId = filter.subformDetailId;
                    return new AdapterPluginFormProperties(subPluginFormPropertyTypes) {
                        @Override
                        public JSON getInstancePropsJson(Object instance) {

                            Collection<IdentityName> subFormPropVal
                                    = subPluginFormPropertyTypes.getSubFormPropVal(instance);
                            for (IdentityName subProp : subFormPropVal) {
                                if (StringUtils.equals(subformDetailId, subProp.identityValue())) {
                                    return (new RootFormProperties(subPluginFormPropertyTypes.fieldsType)).getInstancePropsJson(subProp)
                                }
                            }

                            throw new IllegalStateException("subformDetailId:" + subformDetailId + " has not find subForm instance");
                        }
                    };

                } else {
                    return subPluginFormPropertyTypes;
                }
            }
        }

        return new RootFormProperties(filterFieldProp(getPropertyTypes()));
    }

    public static Map<String, /*** fieldname*/PropertyType> filterFieldProp(Map<String, /*** fieldname*/IPropertyType> props) {
        return props.entrySet().stream().filter((e) -> e.getValue() instanceof PropertyType)
                .collect(Collectors.toMap((e) -> e.getKey(), (e) -> (PropertyType) e.getValue()));
    }


    private Map<String, /*** fieldname*/IPropertyType> getPropertyTypes() {
        if (propertyTypes == null) {
            propertyTypes = buildPropertyTypes(Optional.of(this), clazz);

            List<PropertyType> identityFields
                    = propertyTypes.values().stream().filter((p) -> {
                return (p instanceof PropertyType) && ((PropertyType) p).isIdentity();
            }).map((p) -> (PropertyType) p).collect(Collectors.toList());
            if (IdentityName.class.isAssignableFrom(this.clazz)) {
                if (identityFields.size() != 1) {
                    throw new IllegalStateException("class:" + this.clazz + " is type of "
                            + IdentityName.class + " ,size:" + identityFields.size() + " must sign no more than one col:"
                            + identityFields.stream().map((c) -> c.displayName).collect(Collectors.joining(",")));
                }
                this.identityProp = identityFields.get(0);
            } else {
                if (identityFields.size() > 0) {
                    throw new IllegalStateException("class:" + this.clazz + " is not type of "
                            + IdentityName.class + " but more than one identity col:"
                            + identityFields.stream().map((c) -> c.displayName).collect(Collectors.joining(",")));
                }
            }


        }
        return propertyTypes;
    }

    /**
     * 可能plugin form 表单需要几个步骤才能 填充完一个plugin form 表单就需要单独取出部分表单属性去渲染前端页面
     *
     * @param clazz
     * @return
     */
    public static Map<String, /*** fieldname */IPropertyType> buildPropertyTypes(Optional<Descriptor> descriptor, Class<?> clazz) {
        try {
            Map<String, IPropertyType> r = new HashMap<>();

            Optional<PluginExtraProps> extraProps = PluginExtraProps.load(descriptor, clazz);

            // 支持使用继承的方式来实现复用，例如：DataXHiveWriter继承DataXHdfsWriter来实现
            PluginExtraProps.visitAncestorsClass(clazz, new PluginExtraProps.IClassVisitor<Void>() {
                @Override
                public Void process(Class<?> targetClass, Void v) {
                    FormField formField = null;
                    SubForm subFormFields = null;
                    PropertyType ptype = null;
                    PluginExtraProps.Props fieldExtraProps = null;
                    Class<?> subFromDescClass = null;
                    try {
                        for (Field f : targetClass.getDeclaredFields()) {
                            if (!Modifier.isPublic(f.getModifiers()) || Modifier.isStatic(f.getModifiers())) {
                                continue;
                            }

                            if ((subFormFields = f.getAnnotation(SubForm.class)) != null) {
                                subFromDescClass = subFormFields.desClazz();
                                if (subFromDescClass == null) {
                                    throw new IllegalStateException("field " + f.getName()
                                            + "'s SubForm annotation descClass can not be null");
                                }
                                r.put(f.getName()
                                        , new SuFormProperties(clazz, f, subFormFields, filterFieldProp(buildPropertyTypes(descriptor, subFromDescClass))));
                            }

                            formField = f.getAnnotation(FormField.class);
                            if (formField != null) {
                                ptype = new PropertyType(f, formField);
                                if (extraProps.isPresent()
                                        && (fieldExtraProps = extraProps.get().getProp(f.getName())) != null) {
                                    String dftVal = fieldExtraProps.getDftVal();
                                    String help = fieldExtraProps.getHelpContent();

                                    if (fieldExtraProps.getBoolean(PluginExtraProps.KEY_DISABLE)) {
                                        r.remove(f.getName());
                                        continue;
                                        //return null;
                                    }
                                    JSONObject props = fieldExtraProps.getProps();
                                    if (StringUtils.isNotEmpty(help) && StringUtils.startsWith(help, IMessageHandler.TSEARCH_PACKAGE)) {
                                        props.put(PluginExtraProps.Props.KEY_HELP, GroovyShellEvaluate.eval(help));
                                    }

                                    if (StringUtils.isNotEmpty(dftVal) && StringUtils.startsWith(dftVal, IMessageHandler.TSEARCH_PACKAGE)) {

//                                        UploadPluginMeta meta = UploadPluginMeta.parse(dftVal);
//                                        boolean unCache = meta.getBoolean(UploadPluginMeta.KEY_UNCACHE);
//
//                                        Callable<String> valGetter = () -> (String) GroovyShellEvaluate.scriptEval(dftVal);
                                        props.put(PluginExtraProps.KEY_DFTVAL_PROP, GroovyShellEvaluate.scriptEval(dftVal));
                                    }

                                    if (descriptor.isPresent()
                                            && ((formField.type() == FormFieldType.ENUM)
                                            || formField.type() == FormFieldType.MULTI_SELECTABLE)) {
                                        resolveEnumProp(descriptor.get(), fieldExtraProps);
                                    }
                                    ptype.setExtraProp(fieldExtraProps);
                                }
                                r.put(f.getName(), ptype);
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return null;
                }


            });

            return r;
        } catch (Exception e) {
            throw new RuntimeException("parse desc:" + clazz.getName(), e);
        }
    }


//    public static JSONArray resolveEnumProp(Descriptor descriptor, PropertyType propType) {
//        return resolveEnumProp(descriptor, propType.extraProp);
//    }

    private static JSONArray resolveEnumProp(Descriptor descriptor, PluginExtraProps.Props fieldExtraProps) {
        Object anEnum = fieldExtraProps.getProps().get(KEY_ENUM_PROP);
//                                if (anEnum == null) {
//                                    throw new IllegalStateException("fieldName:" + f.getName() + " relevant enum descriptor in json config can not be null");
//                                }
        JSONArray enums = new JSONArray();
        if (anEnum != null && anEnum instanceof String) {
            // 使用了如下这种配置方式，需要使用groovy进行解析
            // "enum": "com.qlangtech.tis.plugin.ds.ReflectSchemaFieldType.all()"
            // 需要转化成以下这种格式:
            //                                "enum": [
            //                                {
            //                                    "label": "是",
            //                                        "val": true
            //                                },
            //                                {
            //                                    "label": "否",
            //                                        "val": false
            //                                }
            // ]
            try {
                GroovyShellEvaluate.descriptorThreadLocal.set(descriptor);
                fieldExtraProps.getProps().put(KEY_ENUM_PROP, GroovyShellEvaluate.scriptEval((String) anEnum, (opts) -> {
                    return Option.toJson((List<Option>) opts);
                }));
            } finally {
                GroovyShellEvaluate.descriptorThreadLocal.remove();
            }
        }
        return enums;
    }

    /**
     * 校验客户端提交的表单
     *
     * @param msgHandler
     * @param context
     * @param verify     是否进行业务逻辑校验，例如数据库是否能正常连接成功
     * @param formData
     * @return
     */
    public final PluginValidateResult verify(IControlMsgHandler msgHandler
            , Context context //
            , boolean verify
            , Map<String, /*** attr key */JSONObject> formData, Optional<IPropertyType.SubFormFilter> subFormFilter) {
//        String impl = null;
//        Descriptor descriptor;
//        String attr;
//        PropertyType attrDesc;
//        JSONObject valJ;
//        String attrVal;


        final PluginFormProperties /** * fieldname */
                propertyTypes = this.getPluginFormPropertyTypes(subFormFilter);

        return propertyTypes.accept(new PluginFormProperties.IVisitor() {
            @Override
            public PluginValidateResult visit(RootFormProperties props) {

                PostFormVals postFormVals = new PostFormVals(formData);

                PluginValidateResult validateResult = new PluginValidateResult(postFormVals
                        , (Integer) context.get(DefaultFieldErrorHandler.KEY_VALIDATE_PLUGIN_INDEX)
                        , (Integer) context.get(DefaultFieldErrorHandler.KEY_VALIDATE_ITEM_INDEX));

                boolean valid = isValid(msgHandler, context, verify, subFormFilter, propertyTypes, postFormVals);

                if (valid && verify && !verify(msgHandler, context, postFormVals)) {
                    valid = false;
                }
                if (valid && !verify && !validateAll(msgHandler, context, postFormVals)) {
                    valid = false;
                }
                validateResult.valid = valid;
                return validateResult;
            }

            @Override
            public PluginValidateResult visit(SuFormProperties props) {
                PluginValidateResult validateResult = null;
                String subFormId = null;
                JSONObject subformData = null;
                Map<String, JSONObject> subform = null;
                PostFormVals postFormVals = null;


                if (verify) {
                    // 校验的时候子表单是{key1:val1,key2:val2} 的格式
                    PostFormVals formVals = new PostFormVals(formData);
                    boolean valid = isValid(msgHandler, context, verify, subFormFilter, propertyTypes, formVals);
                    if (!valid) {
                        validateResult = new PluginValidateResult(formVals
                                , (Integer) context.get(DefaultFieldErrorHandler.KEY_VALIDATE_PLUGIN_INDEX)
                                , (Integer) context.get(DefaultFieldErrorHandler.KEY_VALIDATE_ITEM_INDEX));
                        validateResult.valid = false;
                        return validateResult;
                    }
                } else {

                    if (props.subFormFieldsAnnotation.atLeastOne() && (formData.size() < 1)) {
                        // 是否至少要选一个以上的校验
                        msgHandler.addErrorMessage(context, "请至少选择一个");
                        validateResult = new PluginValidateResult(null, 0, 0);
                        validateResult.valid = false;
                        return validateResult;
                    }

                    if (Descriptor.this instanceof SubForm.ISubFormItemValidate) {
                        assert (subFormFilter.isPresent());
                        if (!((SubForm.ISubFormItemValidate) Descriptor.this)
                                .validateSubFormItems(msgHandler, context, props, subFormFilter.get(), formData)) {
                            validateResult = new PluginValidateResult(null, 0, 0);
                            validateResult.valid = false;
                            return validateResult;
                        }
                    }

                    // 提交表单的时候子表单是 {idfieldName1:{key1:val1,key2:val2},idfieldName2:{key1:val1,key2:val2}} 这样的格式
                    validateResult = props.visitAllSubDetailed(formData, new SuFormProperties.ISubDetailedProcess<PluginValidateResult>() {
                        @Override
                        public PluginValidateResult process(String subFormId, Map<String, JSONObject> sform) {
                            PostFormVals pfv = new PostFormVals(sform);
                            boolean valid = isValid(msgHandler, context, verify, subFormFilter, propertyTypes, pfv);
                            if (!valid) {
                                PluginValidateResult vResult = new PluginValidateResult(pfv
                                        , (Integer) context.get(DefaultFieldErrorHandler.KEY_VALIDATE_PLUGIN_INDEX)
                                        , (Integer) context.get(DefaultFieldErrorHandler.KEY_VALIDATE_ITEM_INDEX));
                                vResult.valid = false;
                                return vResult;
                            }
                            return (PluginValidateResult) null;
                        }
                    });
                    if (validateResult != null) {
                        return validateResult;
                    }
//                    for (Map.Entry<String, JSONObject> entry : formData.entrySet()) {
//                        subFormId = entry.getKey();
//                        subformData = entry.getValue();
//                        subform = Maps.newHashMap();
//                        for (String fieldName : subformData.keySet()) {
//                            subform.put(fieldName, subformData.getJSONObject(fieldName));
//                        }
//                        postFormVals = new PostFormVals(subform);
//                        boolean valid = isValid(msgHandler, context, bizValidate, subFormFilter, propertyTypes, postFormVals);
//                        if (!valid) {
//                            validateResult = new PluginValidateResult(postFormVals
//                                    , (Integer) context.get(DefaultFieldErrorHandler.KEY_VALIDATE_PLUGIN_INDEX)
//                                    , (Integer) context.get(DefaultFieldErrorHandler.KEY_VALIDATE_ITEM_INDEX));
//                            validateResult.valid = false;
//                            return validateResult;
//                        }
//                    }
                }


                validateResult = new PluginValidateResult(null, 0, 0);
                validateResult.valid = true;
                return validateResult;
            }
        });
//        if (valid && bizValidate && !this.validate(msgHandler, context, postFormVals)) {
//            valid = false;
//        }
//        validateResult.valid = valid;
//        return validateResult;
    }


    private boolean isValid(IControlMsgHandler msgHandler, Context context, boolean bizValidate
            , Optional<IPropertyType.SubFormFilter> subFormFilter, PluginFormProperties propertyTypes, PostFormVals postFormVals) {
        Objects.requireNonNull(postFormVals, "postFormVals can not be null");
        Map<String, JSONObject> formData = postFormVals.rawFormData;
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
                JSONObject descVal = valJ.getJSONObject("descVal");
                impl = descVal.getString("impl");
                if (StringUtils.isBlank(impl)) {
                    addFieldRequiredError(msgHandler, context, attr);
                    valid = false;
                    continue;
                }
                AttrValMap attrValMap = AttrValMap.parseDescribableMap(msgHandler, subFormFilter, descVal);
                pushFieldStack(context, attr, 0);
                try {
                    if (!attrValMap.validate(context, bizValidate).isValid()) {
                        valid = false;
                        continue;
                    }
                } finally {
                    popFieldStack(context);
                }
            } else {

                if (attrDesc.typeIdentity() == FormFieldType.MULTI_SELECTABLE.getIdentity()) {
                    List<FormFieldType.SelectedItem> selectedItems = getSelectedMultiItems(valJ);
                    // 多选类型的 multi select
//                    JSONObject eprops = valJ.getJSONObject("_eprops");
//                    Objects.requireNonNull(eprops, "property '_eprops' of attr:" + attr + " can not be null");
//                    // enums 格式例子：`com/qlangtech/tis/extension/form-prop-enum-example.json`
//                    JSONArray enums = eprops.getJSONArray("enum");
//                    JSONObject select = null;
//                    int selected = 0;
//                    List<FormFieldType.SelectedItem> selectedItems = Lists.newArrayList();
//                    FormFieldType.SelectedItem item = null;
//                    for (int i = 0; i < enums.size(); i++) {
//                        select = enums.getJSONObject(i);
//                        item = new FormFieldType.SelectedItem(select.getString("label"), select.getString("val")
//                                , select.containsKey(keyChecked) && select.getBoolean(keyChecked));
//                        if (item.isChecked()) {
//                            selected++;
//                        }
//                        selectedItems.add(item);
//                    }
                    if (selectedItems.size() < 1) {
                        // 没有选中
                        Validator[] validators = attrDesc.getValidator();
                        for (Validator v : validators) {
                            if (v == Validator.require) {
                                v.validate(msgHandler, context, attr, StringUtils.EMPTY);
                            }
                        }
                    } else if (this instanceof FormFieldType.IMultiSelectValidator) {
                        FormFieldType.IMultiSelectValidator multiSelectValidator = (FormFieldType.IMultiSelectValidator) this;
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
                        attrVal = valJ.getString(KEY_primaryVal);
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

    private List<FormFieldType.SelectedItem> getSelectedMultiItems(JSONObject valJ) {
        final String keyChecked = "checked";
        // 多选类型的 multi select
        JSONObject eprops = valJ.getJSONObject("_eprops");
        Objects.requireNonNull(eprops, "property '_eprops'   can not be null");
        // enums 格式例子：`com/qlangtech/tis/extension/form-prop-enum-example.json`
        JSONArray enums = eprops.getJSONArray(Descriptor.KEY_ENUM_PROP);
        if (enums == null) {
            enums = new JSONArray();
            //   throw new IllegalStateException("enums of prop can not be null");
        }
        JSONObject select = null;
        int selected = 0;
        List<FormFieldType.SelectedItem> selectedItems = Lists.newArrayList();
        FormFieldType.SelectedItem item = null;
        for (int i = 0; i < enums.size(); i++) {
            select = enums.getJSONObject(i);
            item = new FormFieldType.SelectedItem(select.getString(PluginExtraProps.KEY_LABEL), select.getString("val")
                    , select.containsKey(keyChecked) && select.getBoolean(keyChecked));
            if (item.isChecked()) {
                selected++;
            }
            selectedItems.add(item);
        }
        return selectedItems;
    }

    public static class PluginValidateResult {
        private final PostFormVals itemForm;
        private boolean valid;
        private Descriptor descriptor;

        // 标注当前 item表单在整个大表单中的位置
        private final Integer validatePluginIndex;
        private final Integer validatePluginItemIndex;

        public void setDescriptor(Descriptor descriptor) {
            this.descriptor = descriptor;
        }

        public static void setValidateItemPos(Context context, Integer pluginIndex, Integer itemIndex) {
            context.put(KEY_VALIDATE_PLUGIN_INDEX, (pluginIndex));
            context.put(KEY_VALIDATE_ITEM_INDEX, (itemIndex));
        }

        public void addIdentityFieldValueDuplicateError(IControlMsgHandler handler, Context context) {
            setValidateItemPos(context, validatePluginIndex, validatePluginItemIndex);
            handler.addFieldError(context, descriptor.getIdentityField().displayName, IdentityName.MSG_ERROR_NAME_DUPLICATE);
        }

        public String getIdentityFieldValue() {
            if (descriptor == null) {
                throw new IllegalStateException("descriptor can not be null");
            }
            return itemForm.getField(descriptor.getIdentityField().displayName);
        }

        public PostFormVals getItemForm() {
            return itemForm;
        }

        public boolean isValid() {
            return valid;
        }

        public PluginValidateResult(PostFormVals itemForm, Integer validatePluginIndex, Integer validatePluginItemIndex) {
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

    public ParseDescribable<T> newInstance(
            String appName, //
            FormData formData //
    ) {
        return newInstance(IPluginContext.namedContext(appName), formData.body, Optional.empty());
    }

    public static class FormData {
        final HashMap<String, JSONObject> body = Maps.newHashMap();

        public JSONObject addProp(String key, String val) {
            JSONObject o = new JSONObject();
            o.put(Descriptor.KEY_primaryVal, val);
            body.put(key, o);
            return o;
        }
    }

    // @Override
    public ParseDescribable<T> newInstance(
            IPluginContext pluginContext, //
            Map<String, /** * attr key */com.alibaba.fastjson.JSONObject> formData, //
            Optional<IPropertyType.SubFormFilter> subFormFilter) {
        try {
            //PluginFormProperties pfPropertyTypes = this.getPluginFormPropertyTypes(subFormFilter);
            T describable = clazz.newInstance();
//            T describable = pfPropertyTypes.accept(new PluginFormProperties.IVisitor() {
//                @Override
//                public <T> T visit(RootFormProperties props) {
//                    try {
//                        return (T) clazz.newInstance();
//                    } catch (Exception e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//
//                @Override
//                public <T> T visit(SuFormProperties props) {
//                    Assert.assertTrue(subFormFilter.isPresent());
//                    IPropertyType.SubFormFilter filter = subFormFilter.get();
//                    return filter.getOwnerPlugin(pluginContext);
//                }
//            });

            return parseDescribable(pluginContext, describable, formData, subFormFilter);
        } catch (Exception e) {
            throw new RuntimeException("class:" + this.clazz.getName(), e);
        }
    }


    private ParseDescribable<T> parseDescribable(
            IPluginContext pluginContext, T describable
            , Map<String, /*** Attr Name*/JSONObject> keyValMap
            , Optional<IPropertyType.SubFormFilter> subFormFilter) {
        ParseDescribable<T> result = new ParseDescribable<>(describable);

        PluginFormProperties propertyTypes = this.getPluginFormPropertyTypes(subFormFilter);

        propertyTypes.accept(new PluginFormProperties.IVisitor() {
            @Override
            public Void visit(RootFormProperties props) {
                buildPluginInstance(pluginContext, keyValMap, result, propertyTypes);
                return null;
            }

            @Override
            public Void visit(SuFormProperties props) {
                // 保存子form detail list
                List<Object> subDetailedList = Lists.newArrayList();
                props.visitAllSubDetailed(keyValMap, new SuFormProperties.ISubDetailedProcess<Void>() {
                    public Void process(String subFormId, Map<String, JSONObject> subform) {
                        ParseDescribable<Object> r = new ParseDescribable<>(props.newSubDetailed());
                        subDetailedList.add(buildPluginInstance(pluginContext, subform, r, propertyTypes));
                        return null;
                    }
                });

                try {
                    props.subFormField.set(result.instance, subDetailedList);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

                return null;
            }
        });


        return result;
    }

    private <TARGET> TARGET buildPluginInstance(IPluginContext pluginContext
            , Map<String, JSONObject> keyValMap, ParseDescribable<TARGET> result, PluginFormProperties propertyTypes) {
        TARGET describable = result.instance;
        String attr;
        PropertyType attrDesc;
        JSONObject valJ;
        String impl;
        Descriptor descriptor;
        String attrVal;
        for (Map.Entry<String, PropertyType> entry : propertyTypes.getKVTuples()) {
            attr = entry.getKey();
            attrDesc = entry.getValue();
            valJ = keyValMap.get(attr);
            if (valJ == null && attrDesc.isInputRequired()) {
                throw new IllegalStateException("prop:" + attr + " can not be empty");
            }
            if (valJ == null) {
                valJ = new JSONObject();
            }
            if (attrDesc.isDescribable()) {
                JSONObject descVal = valJ.getJSONObject("descVal");
                impl = descVal.getString("impl");
                descriptor = TIS.get().getDescriptor(impl);
                if (descriptor == null) {
                    throw new IllegalStateException("impl:" + impl + " relevant descripotor can not be null");
                }
                ParseDescribable vals = descriptor.newInstance(pluginContext, parseAttrValMap(descVal.get("vals")), Optional.empty());
                attrDesc.setVal(describable, vals.instance);
            } else {

                if (attrDesc.typeIdentity() == FormFieldType.MULTI_SELECTABLE.getIdentity()) {
                    List<FormFieldType.SelectedItem> selectedItems = getSelectedMultiItems(valJ);
                    List<String> multi = selectedItems.stream()
                            .filter((item) -> item.isChecked())
                            .map((item) -> (String) item.getValue())
                            .collect(Collectors.toList());

                    attrDesc.setVal(describable, multi);
                } else {

                    boolean containVal = valJ.containsKey(KEY_primaryVal) && StringUtils.isNotBlank(valJ.getString(KEY_primaryVal));
                    // describable
                    if (!containVal && attrDesc.isInputRequired()) {
                        throw new IllegalStateException("prop:" + attr + " can not be empty");
                    }
                    if (containVal) {
                        attrVal = valJ.getString(KEY_primaryVal);
                        attrDesc.setVal(describable, attrVal);
                        if (valJ.containsKey(KEY_OPTIONS)) {
                            JSONArray options = valJ.getJSONArray(KEY_OPTIONS);
                            JSONObject opt = null;
                            for (int i = 0; i < options.size(); i++) {
                                opt = options.getJSONObject(i);
                                try {
                                    // 将options中的选中的插件来源记录下来，后续在集群中各组件中传输插件可以用
                                    if (StringUtils.equals(attrVal, opt.getString("name"))) {
                                        Class<?> implClass = TIS.get().pluginManager.uberClassLoader.loadClass(opt.getString("impl"));
                                        PluginWrapper pluginWrapper = TIS.get().pluginManager.whichPlugin(implClass);
                                        XStream2.PluginMeta pluginMeta = pluginWrapper.getDesc();
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
                throw new IllegalStateException("property identityProp can not be null");
            }
        }
        return identityProp;
    }

    public static class ParseDescribable<T extends Object> {

        public final T instance;

        public final List<XStream2.PluginMeta> extraPluginMetas = Lists.newArrayList();

        public ParseDescribable(T instance) {
            this.instance = instance;
        }
    }

    public static Map<String, /*** attrName */
            JSONObject> parseAttrValMap(Object vals) {
        Map<String, com.alibaba.fastjson.JSONObject> attrValMap = Maps.newHashMap();
        if (vals == null) {
            return attrValMap;
        }
        // Object vals = jsonObject.get("vals");
        if (vals instanceof com.alibaba.fastjson.JSONObject) {
            ((JSONObject) vals).forEach((attrName, val) -> {
                attrValMap.put(attrName, (JSONObject) val);
            });
        }
        return attrValMap;
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
                    + this.getClass().getName() + ",has registed:" + selectOptsRegister.keySet().stream().collect(Collectors.joining(",")));
        }
        try {
            List<? extends IdentityName> opts = opsCallable.call();
            if (opts == null) {
                return Collections.emptyList();
            }
            return opts.stream().map((r) -> new SelectOption(r.identityValue(), r.getClass())).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("field name:" + name + ",class:" + this.getClass().getName(), e);
        }
    }

    public static class SelectOption {

        private final String name;

        private final Class<?> implClass;

        public SelectOption(String name, Class<?> implClass) {
            this.name = name;
            this.implClass = implClass;
        }

        public String getName() {
            return name;
        }

        public String getImpl() {
            return implClass.getName();
        }
    }

    public static class PostFormVals {
        public final Map<String, /*** attr key */com.alibaba.fastjson.JSONObject> rawFormData;

        public PostFormVals(Map<String, JSONObject> rawFormData) {
            this.rawFormData = rawFormData;
        }

        private Map<String, String> fieldVals = Maps.newHashMap();

        public String getField(String key) {
            return fieldVals.get(key);
        }
    }

    public PluginExtraProps fieldExtraDescs = new PluginExtraProps();

    protected void addFieldDescriptor(String fieldName, Object dftVal, String helperContent) {
        this.addFieldDescriptor(fieldName, dftVal, helperContent, Optional.empty());
    }

    protected void addFieldDescriptor(String fieldName, Object dftVal
            , String helperContent, Optional<List<Option>> enums) {
        JSONObject c = new JSONObject();
        c.put(PluginExtraProps.KEY_DFTVAL_PROP, dftVal);
        PluginExtraProps.Props props = new PluginExtraProps.Props(c);
        props.tagAsynHelp(new StringBuffer(helperContent));
        if (enums.isPresent()) {
            c.put(KEY_ENUM_PROP, Option.toJson(enums.get()));
        }
        this.fieldExtraDescs.put(fieldName, props);
    }


}
