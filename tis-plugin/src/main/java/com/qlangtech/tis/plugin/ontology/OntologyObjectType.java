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
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.annotation.JSONField;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.MultiStepsSupportHost;
import com.qlangtech.tis.extension.OneStepOfMultiSteps;
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.manage.common.OptionWithEndType;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;
import com.qlangtech.tis.plugin.ontology.impl.OntologyPluginMeta;
import com.qlangtech.tis.plugin.ontology.impl.objtype.ObjectTypeBinding;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.UploadPluginMeta;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 创建本体Object type，对应一个表
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/4/16
 * //@see ObjectTypeProfile
 * //@see ObjectTypeProperties
 * //@see ObjectTypePropertiesRelevant
 */
public abstract class OntologyObjectType extends Ontology implements MultiStepsSupportHost,
        IPluginStore.ManipuldateProcessor, IPluginStore.BeforePluginSaved {

    public static final String KEY_DATASOURCE_NAME = "ds";
    // public static final String KEY_OBJECT_TYPE = "objectType";
    public static final String KEY_OBJECT_TYPE = "object-type";
    // private transient String dataSourceName;

    @FormField(identity = true, ordinal = 0, validate = {Validator.require, Validator.identity})
    public String useless;

    public abstract ObjectTypeBinding.ObjectTypeBindingInfo getObjectTypeBindingInfo();

    public static IPluginStore<OntologyObjectType> getPluginStore(String ontologyName,
                                                                  String tableName) {
        OntologyPluginMeta pluginMeta = OntologyPluginMeta.create(OntologyEnum.ObjectType, ontologyName);
        pluginMeta.setPersistence().setPluginIdVal(tableName);//.getDelegate() //
        //.putExtraParams(KEY_OBJECT_TYPE, tableName);

        IPluginStore<Ontology> store = OntologyEnum.ObjectType.getPluginStore(pluginMeta);//.getPluginStore(pluginMeta);
        return store.unsaveCast();
    }

    private OneStepOfMultiSteps[] _stepsPlugin;

    @Override
    public void setSteps(OneStepOfMultiSteps[] stepsPlugin) {
        this._stepsPlugin = Objects.requireNonNull(stepsPlugin, "stepsPlugin can not be null");
    }

    public abstract ObjectTypeBinding getDataSourceBinding();

    public abstract Optional<OntologyProperty> getPk();

    /**
     * 是否禁用了主键？，可以明确设置不需要主键，例如关系表上只有两个外键存在
     *
     * @return
     */
    public abstract boolean hasDisablePK();


    @JSONField(serialize = false)
    @Override
    public OneStepOfMultiSteps[] getMultiStepsSavedItems() {
        final int assertLength = 3;
        if (_stepsPlugin.length != assertLength) {
            throw new IllegalStateException("lenght of _stepsPlugin must be " + assertLength);
        }
        return _stepsPlugin;
    }

    @Override
    public void manipuldateProcess(IPluginContext pluginContext, UploadPluginMeta pluginMeta,
                                   Optional<Context> context) {
        IPluginStore<OntologyObjectType> valTypeStore =
                OntologyEnum.ObjectType.getPluginStore(
                        OntologyPluginMeta.createPluginMeta(pluginMeta).setPersistence()).unsaveCast();
        valTypeStore.setPlugins(pluginContext, context,
                Collections.singletonList(new Descriptor.ParseDescribable<>(this)));
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
    public static List<OntologyObjectType> loadAll(String ontologyName) {

        return OntologyEnum.ObjectType.loadAll(OntologyPluginMeta.create(OntologyEnum.ObjectType, ontologyName));

        //        List<OntologyObjectType> objectTypes = Lists.newArrayList();
        //        File objectTypeDir = OntologyDomain.getObjectTypeDir(ontologyName);
        //        if (!objectTypeDir.exists()) {
        //            return Collections.emptyList();
        //        }
        //        //        for (String ds : Objects.requireNonNull(objectTypeDir.list(), "subDir of objectTypeDir
        //        can not be
        //        //        null")) {
        //        //            File dsDir = new File(objectTypeDir, ds);
        //        for (File ot : FileUtils.listFiles(objectTypeDir, new String[]{XmlFile.KEY_XML_EXTENSION}, false)) {
        //            IPluginStore<OntologyObjectType> ps =
        //                    getPluginStore(ontologyName, //ds,
        //                            StringUtils.removeEnd(ot.getName(), XmlFile.KEY_XML_DOT_EXTENSION));
        //
        //            objectTypes.add(Objects.requireNonNull(ps.getPlugin(), "ot:" + ot.getAbsolutePath()));
        //        }
        //        //}
        //        return objectTypes;
    }

    @Override
    public String identityValue() {
        return this.getName();
    }

    public abstract List<OntologyProperty> getCols();

    @JSONField(serialize = false)
    public abstract List<OptionWithEndType> getColOpts();


    @JSONField(serialize = false)
    public final JSONArray getColsSerialize2JsonArray() {
        return Option.toJson(this.getColOpts());
    }


    public abstract String getName();


    /**
     * 将目标属替换成制定的sharedProp
     *
     * @param targetProperty
     * @param sharedProp
     */
    public abstract void setSharedProperty(TargetProperty targetProperty, OntologySharedProperty sharedProp);

    public abstract void setValeType(TargetProperty targetProperty, OntologyValueType valueType);


    /**
     * 绑定新的数据源
     *
     * @param tagetDS
     */
    public abstract void setNewDataSourceBinding(DataSourceFactory tagetDS);


}
