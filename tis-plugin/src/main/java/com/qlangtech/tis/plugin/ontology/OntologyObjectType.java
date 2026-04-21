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

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.Lists;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.extension.impl.XmlFile;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.KeyedPluginStore;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.util.HeteroEnum;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.Selectable;
import com.qlangtech.tis.util.UploadPluginMeta;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.List;
import java.util.Objects;

import static com.qlangtech.tis.extension.Descriptor.getPluginFileName;
import static com.qlangtech.tis.plugin.ontology.OntologyDomain.NAME_ONTOLOGY_DOMAIN;

/**
 * 创建本体Object type，对应一个表
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/4/16
 */
public class OntologyObjectType implements Describable<OntologyObjectType>, IdentityName {

    public static final String KEY_DATASOURCE_NAME = "ds";
    // public static final String KEY_OBJECT_TYPE = "objectType";
    public static final String KEY_OBJECT_TYPE = "object-type";

    @FormField(identity = true, ordinal = 0, validate = {Validator.require, Validator.identity})
    public String name;

    @FormField(ordinal = 1, validate = {Validator.require, Validator.identity})
    public String alias;

    @FormField(ordinal = 2, validate = {Validator.require, Validator.none_blank})
    public String description;

    private transient String dataSourceName;
    private List<OntologyProperty> cols;

    public static IPluginStore<OntologyObjectType> getPluginStore(String ontologyName, String dataSourceName,
                                                                  String tableName) {
        return ONTOLOGY_OBJECT_TYPE
                .getPluginStore(null, UploadPluginMeta.create(ONTOLOGY_OBJECT_TYPE)
                        .putExtraParams(NAME_ONTOLOGY_DOMAIN, ontologyName) //
                        .putExtraParams(KEY_DATASOURCE_NAME, dataSourceName) //
                        .putExtraParams(KEY_OBJECT_TYPE, tableName));
    }

    @JSONField(serialize = false)
    @Override
    public Descriptor<OntologyObjectType> getDescriptor() {
        return Describable.super.getDescriptor();
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
        for (String ds : Objects.requireNonNull(objectTypeDir.list(), "subDir of objectTypeDir can not be null")) {
            File dsDir = new File(objectTypeDir, ds);
            for (File ot : FileUtils.listFiles(dsDir, new String[]{XmlFile.KEY_XML_EXTENSION}, false)) {
                IPluginStore<OntologyObjectType> ps =
                        getPluginStore(ontologyName, ds,
                                StringUtils.removeEnd(ot.getName(), XmlFile.KEY_XML_DOT_EXTENSION));

                objectTypes.add(Objects.requireNonNull(ps.getPlugin(), "ot:" + ot.getAbsolutePath()).setDataSourceName(ds));
            }
        }
        return objectTypes;
    }

    public static OntologyObjectType loadDetail(String ontologyName, String dataSourceName, String objType) {
        return getPluginStore(ontologyName, dataSourceName, objType).getPlugin();
    }


    @TISExtension
    public static final HeteroEnum<OntologyObjectType> ONTOLOGY_OBJECT_TYPE = new HeteroEnum<>(//
            OntologyObjectType.class, //
            KEY_OBJECT_TYPE, // },
            "本体对象类型", Selectable.Single, false) {
        @SuppressWarnings("all")
        @Override
        public IPluginStore<OntologyObjectType> getPluginStore(IPluginContext pluginContext,
                                                               UploadPluginMeta pluginMeta) {
            String ontology = pluginMeta.getExtraParam(NAME_ONTOLOGY_DOMAIN);
            if (StringUtils.isEmpty(ontology)) {
                throw new IllegalStateException("param ontology can not be empty");
            }
            String dataSource = pluginMeta.getExtraParam(KEY_DATASOURCE_NAME);
            String tableName = pluginMeta.getExtraParam(KEY_OBJECT_TYPE);
            if (StringUtils.isEmpty(dataSource)) {
                throw new IllegalStateException("param dataSource can not be empty");
            }
            if (StringUtils.isEmpty(tableName)) {
                throw new IllegalStateException("param tableName can not be empty");
            }

            KeyedPluginStore.Key key = getStoreKey(ontology, dataSource, tableName);
            return TIS.getPluginStore(key);
        }
    };

    @SuppressWarnings("all")
    private static KeyedPluginStore.Key getStoreKey(String ontologyName, String dataSourceName, String tableName) {

        KeyedPluginStore.Key key = new KeyedPluginStore.Key(ONTOLOGY_OBJECT_TYPE.getIdentity(), ontologyName
                , ONTOLOGY_OBJECT_TYPE.extensionPoint) {
            @Override
            public File getStoreFile() {
                File objectTypeDir = OntologyDomain.getObjectTypeDir(ontologyName);
                return new File(objectTypeDir, dataSourceName + File.separator + getPluginFileName(getFileName()));
            }

            public String getSerializeFileRelativePath() {
                return this.getSubDirPath() + File.separator + getFileName();
            }

            @Override
            protected String getFileName() {
                return tableName;
            }

            @Override
            public int hashCode() {
                return Objects.hash(keyVal.getKeyVal(), ontologyName, dataSourceName, tableName);
            }
        };
        return key;
    }


    public OntologyObjectType setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
        return this;
    }

    public String getDataSourceName() {
        return this.dataSourceName;
    }

    public List<OntologyProperty> getCols() {
        return cols;
    }

    public void setCols(List<OntologyProperty> cols) {
        this.cols = cols;
    }

    @Override
    public String identityValue() {
        return this.name;
    }

}
