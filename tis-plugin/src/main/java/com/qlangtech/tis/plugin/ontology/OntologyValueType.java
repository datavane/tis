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
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.MultiStepsSupportHost;
import com.qlangtech.tis.extension.MultiStepsSupportHostDescriptor;
import com.qlangtech.tis.extension.OneStepOfMultiSteps;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.extension.util.GroovyShellUtil;
import com.qlangtech.tis.manage.common.OptionWithEndType;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.ontology.impl.OntologyPluginMeta;
import com.qlangtech.tis.plugin.ontology.impl.valuetype.ConstraintsOfValueType;
import com.qlangtech.tis.plugin.ontology.impl.valuetype.MetadataOfValueType;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.UploadPluginMeta;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 本体值类型
 * <a href="https://www.palantir.com/docs/foundry/object-link-types/create-value-type/">...</a>
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/4/14
 * @see ConstraintsOfValueType
 * @see MetadataOfValueType
 */
public class OntologyValueType extends Ontology implements IdentityName, MultiStepsSupportHost,
        IPluginStore.ManipuldateProcessor, IPluginStore.BeforePluginSaved {

    /**
     * 值类型
     */
    public static final String KEY_VALUE_TYPE = "ontology-value-type";
    public static final String KEY_START_PERSISTENCE = "startPersistence";

    /**
     * Caution: 这个字段目前没有用，由于 实现了IdentityName接口
     */
    @FormField(identity = true, ordinal = 0, validate = {Validator.require, Validator.identity})
    public String useless;

    private OneStepOfMultiSteps[] stepsPlugin;

    public String getDescription() {
        return getMeta().description;
    }

    private MetadataOfValueType getMeta() {
        return (MetadataOfValueType) Objects.requireNonNull( //
                stepsPlugin[OneStepOfMultiSteps.Step.Step1.getStepIndex()], "step1 can can not be null");
    }

    /**
     * 加载某一个本体域中的Object Type
     *
     * @param ontologyName
     * @return
     */
    public static List<OntologyValueType> load(String ontologyName) {
        if (StringUtils.isEmpty(ontologyName)) {
            throw new IllegalArgumentException("param ontologyName can not be empty");
        }
        //        List<OntologyValueType> objectTypes = Lists.newArrayList();
        //        File objectTypeDir = OntologyDomain.getValueTypeDir(ontologyName);
        //        String valTypeName = null;
        //        for (String ds : Objects.requireNonNull(objectTypeDir.list(), "subDir of objectTypeDir can not be
        //        null")) {
        //            File dsDir = new File(objectTypeDir, ds);
        //            for (File ot : FileUtils.listFiles(dsDir, new String[]{XmlFile.KEY_XML_EXTENSION}, false)) {
        //                valTypeName = StringUtils.removeEnd(ot.getName(), XmlFile.KEY_XML_DOT_EXTENSION);
        //                UploadPluginMeta pluginMeta = UploadPluginMeta.create(ONTOLOGY) //
        //                        .putExtraParams(KEY_START_PERSISTENCE, Boolean.TRUE.toString())
        //                        .putExtraParams(NAME_ONTOLOGY_DOMAIN, ontologyName)
        //                        .putExtraParams(IdentityName.PLUGIN_IDENTITY_NAME, valTypeName);
        //                Ontology.OntologyEnum ontologyEnum = OntologyEnum.ValueType;
        //                IPluginStore<OntologyValueType> ps
        //                        = ontologyEnum.getStoreKey(OntologyPluginMeta.createPluginMeta(pluginMeta))
        //                        .unsaveCast();
        //                objectTypes.add(Objects.requireNonNull(ps.getPlugin(), "ot:" + ot.getAbsolutePath()));
        //            }
        //        }
        //        ;
        return OntologyEnum.ValueType.loadAll(OntologyPluginMeta.create(OntologyEnum.ValueType, ontologyName));
        //  return objectTypes;
    }

    /**
     * 通过 OntologyProperty 的type 获取 valueType的下拉可选项目
     *
     * @return
     */
    public static List<OptionWithEndType> availableValTypes() {
        Map<Class<? extends Descriptor>, Describable> classDescribableMap =
                Objects.requireNonNull(GroovyShellUtil.pluginThreadLocal.get(), "classDescribableMap can not be null");
        for (Map.Entry<Class<? extends Descriptor>, Describable> entry : classDescribableMap.entrySet()) {
            if (!(entry.getValue() instanceof OntologyProperty ontologyProp)) {
                throw new IllegalStateException("entry.getValue() must be type of "
                        + OntologyProperty.class.getName() + " but now is " + entry.getValue().getClass().getName());
            }
            OntologyType selectedType = ontologyProp.parseOntologyType();
            IPluginContext pluginContext = IPluginContext.getThreadLocalInstance();
            OntologyPluginMeta meta = OntologyPluginMeta.createPluginMeta(pluginContext.getContext());
            return getMatchedValTypeOptions(meta, selectedType);
        }
        throw new IllegalStateException("classDescribableMap.entrySet() can not be empty");
    }


    public static List<OptionWithEndType> getMatchedValTypeOptions(OntologyPluginMeta meta,
                                                                   OntologyType selectedType) {
        return OntologyValueType.load(meta.getDomain()).stream()
                .filter((valType) -> {
                    return Objects.requireNonNull(selectedType, "endType can not be null")
                            == valType.getMeta().ontologyType();
                }).map((valType) -> new OptionWithEndType(valType.identityValue(), valType.identityValue(),
                        selectedType.getEndType()))
                .collect(Collectors.toList());
    }

    //    /**
    //     *
    //     * @return
    //     */
    //    public static OntologyValueType parse() {
    //
    //        return null;
    //    }

    @Override
    public String identityValue() {
        //  return this.name;
        if (stepsPlugin[0] instanceof MetadataOfValueType meta) {
            return meta.name;
        }
        return null;
    }


    @Override
    public void beforeSaved(IPluginContext pluginContext, Optional<Context> context) {
        for (OneStepOfMultiSteps step : getMultiStepsSavedItems()) {
            if (step instanceof MetadataOfValueType) {
                //  this.name = ((MetadataOfValueType) step).name;
                return;
            }
        }
        throw new IllegalStateException("illegal name have not been set");
    }

    @Override
    public void setSteps(OneStepOfMultiSteps[] stepsPlugin) {
        this.stepsPlugin = Objects.requireNonNull(stepsPlugin, "stepsPlugin can not be null");
        final int FIXED_VALUE_TYPE_STEPS_LENGTH = 2;
        if (stepsPlugin.length != FIXED_VALUE_TYPE_STEPS_LENGTH) {
            throw new IllegalStateException("stepsPlugin.length must be equal to" + FIXED_VALUE_TYPE_STEPS_LENGTH);
        }
    }

    @Override
    public OneStepOfMultiSteps[] getMultiStepsSavedItems() {
        return stepsPlugin;
    }

    @Override
    public void manipuldateProcess(IPluginContext pluginContext, UploadPluginMeta pluginMeta,
                                   Optional<Context> context) {
        // 进行持久化
        IPluginStore<OntologyValueType> valTypeStore =
                OntologyEnum.ValueType.getPluginStore(OntologyPluginMeta.createPluginMeta(pluginMeta.putExtraParams(KEY_START_PERSISTENCE,
                                Boolean.TRUE.toString())
                        .putExtraParams(IdentityName.PLUGIN_IDENTITY_NAME,
                                this.getMeta().name))).unsaveCast();

        //        = ONTOLOGY_VALUE_TYPE.getPluginStore(pluginContext,
        //                pluginMeta.putExtraParams(KEY_START_PERSISTENCE,
        //                                Boolean.TRUE.toString())
        //                        .putExtraParams(IdentityName.PLUGIN_IDENTITY_NAME,
        //                                this.getMeta().name));

        valTypeStore.setPlugins(pluginContext, context,
                Collections.singletonList(new Descriptor.ParseDescribable<>(this)));
    }


    @TISExtension
    public static class DefaultDesc extends Ontology.BasicDesc implements MultiStepsSupportHostDescriptor<OntologyValueType> {
        public DefaultDesc() {
            super();
        }

        @Override
        protected OntologyEnum getOntologyType() {
            return OntologyEnum.ValueType;
        }

        @Override
        public String getDisplayName() {
            return "Value Type";
        }

        @Override
        public Class<OntologyValueType> getHostClass() {
            return OntologyValueType.class;
        }

        @Override
        public EndType getEndType() {
            return EndType.OntologyValueType;
        }

        @Override
        public List<OneStepOfMultiSteps.BasicDesc> getStepDescriptionList() {
            return List.of(new MetadataOfValueType.Desc(), new ConstraintsOfValueType.Desc());
        }

        @Override
        public void appendExternalProps(JSONObject multiStepsCfg) {

        }
    }
}
