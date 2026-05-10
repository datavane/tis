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
import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Lists;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.MultiStepsSupportHost;
import com.qlangtech.tis.extension.MultiStepsSupportHostDescriptor;
import com.qlangtech.tis.extension.OneStepOfMultiSteps;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.extension.impl.XmlFile;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.ontology.impl.OntologyPluginMeta;
import com.qlangtech.tis.plugin.ontology.impl.objtype.ObjectTypeProfile;
import com.qlangtech.tis.plugin.ontology.impl.objtype.ObjectTypeProperties;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.UploadPluginMeta;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 创建本体Object type，对应一个表
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/4/16
 */
public class OntologyObjectType extends Ontology implements IdentityName, MultiStepsSupportHost,
        IPluginStore.ManipuldateProcessor, IPluginStore.BeforePluginSaved {

    public static final String KEY_DATASOURCE_NAME = "ds";
    // public static final String KEY_OBJECT_TYPE = "objectType";
    public static final String KEY_OBJECT_TYPE = "object-type";
    // private transient String dataSourceName;

    @FormField(identity = true, ordinal = 0, validate = {Validator.require, Validator.identity})
    public String useless;

    public static IPluginStore<OntologyObjectType> getPluginStore(String ontologyName, //String dataSourceName,
                                                                  String tableName) {
        OntologyPluginMeta pluginMeta = OntologyPluginMeta.create(OntologyEnum.ObjectType, ontologyName);
        pluginMeta.getDelegate() //
                //.putExtraParams(KEY_DATASOURCE_NAME, dataSourceName) //
                .putExtraParams(KEY_OBJECT_TYPE, tableName);
        IPluginStore<Ontology> store = OntologyEnum.ObjectType.getPluginStore(pluginMeta);
        return store.unsaveCast();
    }

    private OneStepOfMultiSteps[] _stepsPlugin;

    @Override
    public void setSteps(OneStepOfMultiSteps[] stepsPlugin) {
        this._stepsPlugin = Objects.requireNonNull(stepsPlugin, "stepsPlugin can not be null");
    }

    public ObjectTypeProfile getProfile() {
        return (ObjectTypeProfile) getMultiStepsSavedItems()[0];
    }

    private ObjectTypeProperties getPropsStep() {
        return (ObjectTypeProperties) getMultiStepsSavedItems()[1];
    }

    @Override
    public OneStepOfMultiSteps[] getMultiStepsSavedItems() {
        if (_stepsPlugin.length != 2) {
            throw new IllegalStateException("lenght of _stepsPlugin must be 2");
        }
        return _stepsPlugin;
    }

    @Override
    public void beforeSaved(IPluginContext pluginContext, Optional<Context> context) {

    }

    @Override
    public void manipuldateProcess(IPluginContext pluginContext, UploadPluginMeta pluginMeta,
                                   Optional<Context> context) {

    }

    @JSONField(serialize = false)
    @Override
    public Descriptor<Ontology> getDescriptor() {
        return super.getDescriptor();
    }

    /**
     * 加载某一个本体域中的Object Type
     *
     * @param ontologyName
     * @return
     */
    public static List<OntologyObjectType> load(String ontologyName) {
        List<OntologyObjectType> objectTypes = Lists.newArrayList();
        File objectTypeDir = OntologyDomain.getObjectTypeDir(ontologyName);
        if (!objectTypeDir.exists()) {
            return Collections.emptyList();
        }
        //        for (String ds : Objects.requireNonNull(objectTypeDir.list(), "subDir of objectTypeDir can not be
        //        null")) {
        //            File dsDir = new File(objectTypeDir, ds);
        for (File ot : FileUtils.listFiles(objectTypeDir, new String[]{XmlFile.KEY_XML_EXTENSION}, false)) {
            IPluginStore<OntologyObjectType> ps =
                    getPluginStore(ontologyName, //ds,
                            StringUtils.removeEnd(ot.getName(), XmlFile.KEY_XML_DOT_EXTENSION));

            objectTypes.add(Objects.requireNonNull(ps.getPlugin(), "ot:" + ot.getAbsolutePath()));
        }
        //}
        return objectTypes;
    }

    public static OntologyObjectType loadDetail(String ontologyName, String objType) {
        return getPluginStore(ontologyName, objType).getPlugin();
    }


    //    public OntologyObjectType setDataSourceName(String dataSourceName) {
    //        this.dataSourceName = dataSourceName;
    //        return this;
    //    }

    //    public String getDataSourceName() {
    //        return this.getProfile().
    //    }


    @Override
    public String identityValue() {
        return this.getProfile().name;
    }

    public final List<OntologyProperty> getCols() {
        return this.getPropsStep().getCols();
    }

    public String getName() {
        return this.getProfile().name;
    }

    @TISExtension
    public static class DefaultDesc extends BasicDesc implements MultiStepsSupportHostDescriptor<OntologyObjectType> {
        public DefaultDesc() {
            super();
        }

        @Override
        public EndType getEndType() {
            return EndType.OntologyObjectType;
        }

        @Override
        protected OntologyEnum getOntologyType() {
            return OntologyEnum.ObjectType;
        }


        @Override
        public String getDisplayName() {
            return "Object Type";
        }

        @Override
        public Class<OntologyObjectType> getHostClass() {
            return OntologyObjectType.class;
        }

        @Override
        public List<OneStepOfMultiSteps.BasicDesc> getStepDescriptionList() {
            return List.of(new ObjectTypeProfile.DftDesc(), new ObjectTypeProperties.DftDesc());
        }

        @Override
        public void appendExternalProps(JSONObject multiStepsCfg) {

        }
    }

}
