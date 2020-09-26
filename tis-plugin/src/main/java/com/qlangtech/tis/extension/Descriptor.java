/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.extension;

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.impl.XmlFile;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.ValidatorCommons;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.runtime.module.misc.IFieldErrorHandler;
import com.qlangtech.tis.util.AttrValMap;
import com.qlangtech.tis.util.ISelectOptionsGetter;
import com.qlangtech.tis.util.XStream2;
import org.apache.commons.beanutils.ConvertUtilsBean;
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
import static com.qlangtech.tis.runtime.module.misc.impl.DefaultFieldErrorHandler.popFieldStack;
import static com.qlangtech.tis.runtime.module.misc.impl.DefaultFieldErrorHandler.pushFieldStack;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public abstract class Descriptor<T extends Describable> implements Saveable, ISelectOptionsGetter {

    public static final String KEY_primaryVal = "_primaryVal";

    public static final String KEY_OPTIONS = "options";

    /**
     * The class being described by this descriptor.
     */
    public final transient Class<? extends T> clazz;

    private transient volatile Map<String, PropertyType> propertyTypes, globalPropertyTypes;

    private final transient Map<String, Method> validateMethodMap;

    /**
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
        this.validateMethodMap = this.createValidateMap();
    }

    private Map<String, Method> createValidateMap() {
        ImmutableMap.Builder<String, Method> mapBuilder = new ImmutableMap.Builder<>();
        // validate${fieldName}
        // System.out.println(this.getClass().getName());
        Method[] validateMethods = this.getClass().getMethods();
        Pattern validateMethodPattern = Pattern.compile("validate(.+?)");
        Matcher methodMatcher = null;
        String fieldName = null;
        for (Method validateMethod : validateMethods) {
            // System.out.println(validateMethod.getName());
            methodMatcher = validateMethodPattern.matcher(validateMethod.getName());
            if (methodMatcher.matches()) {
                fieldName = StringUtils.uncapitalize(methodMatcher.group(1));
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
        }
        return mapBuilder.build();
    }

    /**
     * Obtains the property type of the given field of {@link #clazz}
     */
    public PropertyType getPropertyType(String field) {
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

    public Map<String, /**
     * fieldname
     */
    PropertyType> getPropertyTypes() {
        if (propertyTypes == null)
            propertyTypes = buildPropertyTypes(clazz);
        return propertyTypes;
    }

    private Map<String, /**
     * fieldname
     */
    PropertyType> buildPropertyTypes(Class<?> clazz) {
        Map<String, PropertyType> r = new HashMap<String, PropertyType>();
        FormField formField = null;
        for (Field f : clazz.getDeclaredFields()) {
            if (!Modifier.isPublic(f.getModifiers())) {
                continue;
            }
            formField = f.getAnnotation(FormField.class);
            if (formField != null) {
                r.put(f.getName(), new PropertyType(f, formField));
            }
        }
        return r;
    }

    /**
     * 校验客户端提交的表单
     *
     * @param msgHandler
     * @param context
     * @param formData
     * @return
     */
    public final boolean validate(IFieldErrorHandler msgHandler, Context context, Map<String, /**
     * attr key
     */
    com.alibaba.fastjson.JSONObject> formData) {
        String impl = null;
        Descriptor descriptor;
        String attr;
        PropertyType attrDesc;
        JSONObject valJ;
        String attrVal;
        boolean valid = true;
        Map<String, PropertyType> /**
         * fieldname
         */
        propertyTypes = this.getPropertyTypes();
        for (Map.Entry<String, PropertyType> entry : propertyTypes.entrySet()) {
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
                AttrValMap attrValMap = AttrValMap.parseDescribableMap(msgHandler, descVal);
                pushFieldStack(context, attr, 0);
                try {
                    if (!attrValMap.validate(context)) {
                        valid = false;
                        continue;
                    }
                } finally {
                    popFieldStack(context);
                }
            } else {
                boolean containVal = valJ.containsKey(KEY_primaryVal);
                if (!containVal && attrDesc.isInputRequired()) {
                    addFieldRequiredError(msgHandler, context, attr);
                    valid = false;
                    continue;
                }
                if (containVal) {
                    attrVal = valJ.getString(KEY_primaryVal);
                    Validator[] validators = attrDesc.getValidator();
                    for (Validator v : validators) {
                        if (!v.validate(msgHandler, context, attr, attrVal)) {
                            valid = false;
                            continue;
                        }
                    }
                    // }
                    try {
                        Method validateMethod = this.validateMethodMap.get(attr);
                        if (validateMethod != null) {
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
        return valid;
    }

    private void addFieldRequiredError(IFieldErrorHandler msgHandler, Context context, String attrKey) {
        msgHandler.addFieldError(context, attrKey, ValidatorCommons.MSG_EMPTY_INPUT_ERROR);
    }

    // @Override
    public ParseDescribable<T> newInstance(Map<String, /**
     * attr key
     */
    com.alibaba.fastjson.JSONObject> formData) {
        try {
            T describable = clazz.newInstance();
            return parseDescribable(describable, formData);
        } catch (Exception e) {
            throw new RuntimeException("class:" + this.clazz.getName(), e);
        }
    }

    // protected boolean validateIdentity(IFieldErrorHandler msgHandler, Context context, String fieldName, String value) {
    // Matcher matcher = pattern_identity.matcher(value);
    // if (!matcher.matches()) {
    // msgHandler.addFieldError(context, fieldName, MSG_IDENTITY_ERROR);
    // return false;
    // }
    // return true;
    // }
    private ParseDescribable<T> parseDescribable(T describable, Map<String, /**
     * Attr Name
     */
    JSONObject> keyValMap) {
        ParseDescribable<T> result = new ParseDescribable<>(describable);
        String impl;
        Descriptor descriptor;
        String attr;
        PropertyType attrDesc;
        JSONObject valJ;
        String attrVal;
        Map<String, PropertyType> propertyTypes = this.getPropertyTypes();
        for (Map.Entry<String, PropertyType> entry : propertyTypes.entrySet()) {
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
                ParseDescribable vals = descriptor.newInstance(parseAttrValMap(descVal.get("vals")));
                attrDesc.setVal(describable, vals.instance);
            } else {
                boolean containVal = valJ.containsKey(KEY_primaryVal);
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
        return result;
    }

    public static class ParseDescribable<T extends Describable> {

        public final T instance;

        public final List<XStream2.PluginMeta> extraPluginMetas = Lists.newArrayList();

        public ParseDescribable(T instance) {
            this.instance = instance;
        }
    }

    public static Map<String, /**
     * attrName
     */
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

    private static final ConvertUtilsBean convertUtils = new ConvertUtilsBean();

    public static final class PropertyType {

        public final Class clazz;

        public final Type type;

        private volatile Class itemType;

        public final String displayName;

        private final FormField formField;

        private final Field f;

        private Boolean inputRequired;

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

        public String dftVal() {
            return formField.dftVal();
        }

        public int ordinal() {
            return formField.ordinal();
        }

        public int typeIdentity() {
            return formField.type().getIdentity();
        }

        // public FormField getFormField() {
        // return this.formField;
        // }
        public Validator[] getValidator() {
            return formField.validate();
        }

        public boolean isInputRequired() {
            if (inputRequired == null) {
                inputRequired = false;
                Validator[] validators = this.formField.validate();
                for (Validator v : validators) {
                    if (v == Validator.require) {
                        return inputRequired = true;
                    }
                }
            }
            return inputRequired;
        }

        PropertyType(Field f, FormField formField) {
            this(f, f.getType(), f.getGenericType(), f.toString(), formField);
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

        // 取得实例的值
        public Object getVal(Object instance) {
            try {
                return this.f.get(instance);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        public void setVal(Object instance, Object val) {
            Object prop = null;
            try {
                prop = convertUtils.convert(val, this.clazz);
                this.f.set(instance, prop);
            } catch (Throwable e) {
                throw new RuntimeException("\ntarget instance:" + instance.getClass() + "\nfield:" + this.f + (prop == null ? StringUtils.EMPTY : "\nprop class:" + prop.getClass()), e);
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
                throw new AssertionError(clazz + " is not an array/collection type in " + displayName + ". See https://wiki.jenkins-ci.org/display/JENKINS/My+class+is+missing+descriptor");
            }
            Descriptor d = TIS.get().getDescriptor(it);
            if (d == null)
                throw new AssertionError(it + " is missing its descriptor in " + displayName + ". See https://wiki.jenkins-ci.org/display/JENKINS/My+class+is+missing+descriptor");
            return d;
        }

        /**
         * Returns all the descriptors that produce types assignable to the property type.
         */
        public List<? extends Descriptor> getApplicableDescriptors() {
            return TIS.get().getDescriptorList(clazz);
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

    private final Map<String, Callable<List<? extends IdentityName>>> /**
     * fieldname
     */
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
            throw new IllegalStateException("fieldName:" + name + " is select options has not been register,class:" + this.getClass().getName() + ",has registed:" + selectOptsRegister.keySet().stream().collect(Collectors.joining(",")));
        }
        try {
            List<? extends IdentityName> opts = opsCallable.call();
            if (opts == null) {
                return Collections.emptyList();
            }
            return opts.stream().map((r) -> new SelectOption(r.getName(), r.getClass())).collect(Collectors.toList());
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
}
