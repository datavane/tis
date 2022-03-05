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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.IPropertyType;
import com.qlangtech.tis.extension.PluginFormProperties;
import com.qlangtech.tis.plugin.annotation.SubForm;
import com.qlangtech.tis.util.DescriptorsJSON;
import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-11 13:22
 */
public class SuFormProperties extends PluginFormProperties implements IPropertyType {
    public final Map<String, /*** fieldname */PropertyType> fieldsType;
    public final Field subFormField;
    private Class desClazz;
    public final SubForm subFormFieldsAnnotation;
    public final Class<?> parentClazz;
    private final PropertyType pkPropertyType;

    private DescriptorsJSON.IPropGetter subFormFieldsEnumGetter;

    public static SuFormProperties copy(Map<String, /*** fieldname */PropertyType> fieldsType, Class<?> descClazz, SuFormProperties old) {
        SuFormProperties result = new SuFormProperties(old.parentClazz, old.subFormField, old.subFormFieldsAnnotation, fieldsType);
        if (!old.subFormFieldsAnnotation.desClazz().isAssignableFrom(descClazz)) {
            throw new IllegalStateException("class:" + old.subFormFieldsAnnotation.desClazz() + " must be parent of " + descClazz);
        }
        return result.overWriteDesClazz(descClazz);
    }

    public SuFormProperties(Class<?> parentClazz, Field subFormField
            , SubForm subFormFieldsAnnotation, Map<String, PropertyType> fieldsType) {
        Objects.requireNonNull(fieldsType, "fieldsType can not be null");
        this.parentClazz = parentClazz;
        this.fieldsType = fieldsType;
        this.subFormField = subFormField;
        this.subFormFieldsAnnotation = subFormFieldsAnnotation;
        this.desClazz = subFormFieldsAnnotation.desClazz();
        Optional<Map.Entry<String, PropertyType>> idType = fieldsType.entrySet().stream().filter((ft) -> ft.getValue().isIdentity()).findFirst();
        if (!idType.isPresent()) {
            throw new IllegalArgumentException(subFormFieldsAnnotation.desClazz() + " has not define a identity prop");
        }
        this.pkPropertyType = idType.get().getValue();
    }

    public String getSubFormFieldName() {
        return this.subFormField.getName();
    }

    private String getIdListGetScript() {
        return this.subFormFieldsAnnotation.idListGetScript();
    }

    public <T> T newSubDetailed() {
        try {
            // Class<?> aClass = desClazz this.subFormFieldsAnnotation.desClazz();
            return (T) this.desClazz.newInstance();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public SuFormProperties overWriteDesClazz(Class desClazz) {
        this.desClazz = desClazz;
        return this;
    }

    private static final CustomerGroovyClassLoader loader = new CustomerGroovyClassLoader();

    private static final class CustomerGroovyClassLoader extends GroovyClassLoader {
        public CustomerGroovyClassLoader() {
            super(new ClassLoader(SuFormProperties.class.getClassLoader()) {
                      @Override
                      protected Class<?> findClass(String name) throws ClassNotFoundException {
                          // return super.findClass(name);
                          return TIS.get().getPluginManager().uberClassLoader.findClass(name);
                      }
                  }
            );
        }

        @SuppressWarnings("all")
        public void loadMyClass(String name, String script) throws Exception {
            CompilationUnit unit = new CompilationUnit();
            SourceUnit su = unit.addSource(name, script);
            ClassCollector collector = createCollector(unit, su);
            unit.setClassgenCallback(collector);
            unit.compile(Phases.CLASS_GENERATION);
            int classEntryCount = 0;
            for (Object o : collector.getLoadedClasses()) {
                setClassCacheEntry((Class<?>) o);
                // System.out.println(o);
                classEntryCount++;
            }
        }
    }


    public DescriptorsJSON.IPropGetter getSubFormIdListGetter() {
        try {
            if (subFormFieldsEnumGetter == null) {
                synchronized (this) {
                    if (subFormFieldsEnumGetter == null) {
                        String className = this.parentClazz.getSimpleName() + "_SubFormIdListGetter_" + subFormField.getName();
                        String pkg = this.parentClazz.getPackage().getName();
                        String script = "	package " + pkg + " ;"
                                + "import java.util.Map;"
                                + "import com.qlangtech.tis.coredefine.module.action.DataxAction; "
                                + "import com.qlangtech.tis.util.DescriptorsJSON.IPropGetter; "
                                + "import com.qlangtech.tis.extension.IPropertyType; "
                                + "class " + className + " implements IPropGetter {"
                                + "	@Override"
                                + "	public Object build(IPropertyType.SubFormFilter filter) {" + this.getIdListGetScript() + "	}" + "}";
                        //this.getIdListGetScript()
                        loader.loadMyClass(className, script);
                        Class<?> groovyClass = loader.loadClass(pkg + "." + className);
                        subFormFieldsEnumGetter = (DescriptorsJSON.IPropGetter) groovyClass.newInstance();
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return subFormFieldsEnumGetter;
    }

    public <T> T visitAllSubDetailed(Map<String, /*** attr key */JSONObject> formData, ISubDetailedProcess<T> subDetailedProcess) {
        String subFormId = null;
        JSONObject subformData = null;
        Map<String, JSONObject> subform = null;
        for (Map.Entry<String, JSONObject> entry : formData.entrySet()) {
            subFormId = entry.getKey();
            subformData = entry.getValue();
            subform = Maps.newHashMap();
            for (String fieldName : subformData.keySet()) {
                subform.put(fieldName, subformData.getJSONObject(fieldName));
            }
            T result = subDetailedProcess.process(subFormId, subform);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public interface ISubDetailedProcess<T> {
        T process(String subFormId, Map<String, JSONObject> subform);
    }


    @Override
    public Set<Map.Entry<String, PropertyType>> getKVTuples() {
        return fieldsType.entrySet();
    }

    @Override
    public JSON getInstancePropsJson(Object instance) {
        Class<?> fieldType = subFormField.getType();
        if (!Collection.class.isAssignableFrom(fieldType)) {
            // 现在表单只支持1对n 关系的子表单，因为1对1就没有必要有子表单了
            throw new UnsupportedOperationException("sub form field:" + subFormField.getName()
                    + " just support one2multi relationship,declarFieldClass:" + fieldType.getName());
        }

        // JSONObject vals = new JSONObject();

        try {
            Object o = subFormField.get(instance);

            return createSubFormVals(o);
//            Collection<?> itItems = null;
//            if (o != null) {
//                itItems = (Collection<?>) o;
//                for (Object i : itItems) {
//                    vals.put(String.valueOf(pkPropertyType.getVal(i)), (new RootFormProperties(fieldsType)).getInstancePropsJson(i));
//                }
//            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        // return vals;
    }

    public JSONObject createSubFormVals(Object subFormFieldInstance) {
        JSONObject vals = new JSONObject();

        Collection<?> itItems = null;
        if (subFormFieldInstance != null) {
            itItems = (Collection<?>) subFormFieldInstance;
            for (Object i : itItems) {
                vals.put(String.valueOf(pkPropertyType.getVal(i)), (new RootFormProperties(fieldsType)).getInstancePropsJson(i));
            }
        }

        return vals;
    }

    @Override
    public <T> T accept(IVisitor visitor) {
        return visitor.visit(this);
    }

    public static class SuFormPropertiesBehaviorMeta {
        String clickBtnLabel;
        Map<String, SuFormPropertyGetterMeta> onClickFillData;

        public String getClickBtnLabel() {
            return clickBtnLabel;
        }

        public void setClickBtnLabel(String clickBtnLabel) {
            this.clickBtnLabel = clickBtnLabel;
        }

        public Map<String, SuFormPropertyGetterMeta> getOnClickFillData() {
            return onClickFillData;
        }

        public void setOnClickFillData(Map<String, SuFormPropertyGetterMeta> onClickFillData) {
            this.onClickFillData = onClickFillData;
        }
    }

    public static class SuFormPropertyGetterMeta {
        String method;
        List<String> params;

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public List<String> getParams() {
            return params;
        }

        public void setParams(List<String> params) {
            this.params = params;
        }
    }
}
