/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qlangtech.tis.solrextend.cloud;

import com.qlangtech.tis.TIS;
import com.qlangtech.tis.extension.IdentityDescribale;
import com.qlangtech.tis.plugin.IRepositoryResource;
import com.qlangtech.tis.plugin.PluginStore;
import com.qlangtech.tis.plugin.solr.schema.CharFilterFactoryFactory;
import com.qlangtech.tis.plugin.solr.schema.FieldTypeFactory;
import com.qlangtech.tis.plugin.solr.schema.TokenizerFactoryFactory;
import com.qlangtech.tis.solrdao.SolrFieldsParser;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.util.CharFilterFactory;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.solr.common.cloud.SolrClassLoader;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.PluginInfo;
import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.pkg.PackageListeners;
import org.apache.solr.pkg.PackageLoader;
import org.apache.solr.schema.FieldType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * is replacement of PackageListeningClassLoader
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-01-26 10:31
 */
public class TISPluginClassLoader implements SolrClassLoader, PackageListeners.Listener {

    private static final Map<Class<?>, Class<? extends IdentityDescribale>> expectedTypes;

    static {
        expectedTypes = com.google.common.collect.ImmutableMap.of(
                FieldType.class, FieldTypeFactory.class //
                , CharFilterFactory.class, CharFilterFactoryFactory.class //
                , TokenizerFactory.class, TokenizerFactoryFactory.class
        );
    }

    /**
     * 与schema相关的插件资源
     *
     * @param collection
     * @return
     */
    public static List<IRepositoryResource> getSchemaRelevantResource(String collection) {
        if (StringUtils.isEmpty(collection)) {
            throw new IllegalArgumentException("param collection can not be null");
        }
        return expectedTypes.values().stream().map((clazz) -> TIS.getPluginStore(collection, clazz)).collect(Collectors.toList());
    }


    private final CoreContainer coreContainer;
    private final SolrResourceLoader coreResourceLoader;
    private final Runnable onReload;
    private final String collectionName;


    public TISPluginClassLoader(String collectionName, CoreContainer coreContainer,
                                SolrResourceLoader coreResourceLoader,
                                Runnable onReload) {
        this.collectionName = collectionName;
        this.coreContainer = coreContainer;
        this.coreResourceLoader = coreResourceLoader;
        this.onReload = () -> {
            onReload.run();
        };
    }

    @Override
    public <T> T newInstance(String cname, Class<T> expectedType, String... subpackages) {
        PluginInfo.ClassName cName = new PluginInfo.ClassName(cname);
        if (cName.pkg == null) {
            return coreResourceLoader.newInstance(cname, expectedType, subpackages);
        } else {
            if (!SolrFieldsParser.KEY_PLUGIN.equals(cName.pkg)) {
                throw new IllegalStateException("plugin name:" + cName.pkg + " must be '" + SolrFieldsParser.KEY_PLUGIN + "'");
            }

            Class<? extends IdentityDescribale> targetClass = expectedTypes.get(expectedType);
            if (targetClass != null) {
                return (T) findAndCreatePlugin(cName, targetClass);
            } else {
                throw new IllegalStateException("expectedType:" + expectedType + " is illegal");
            }
        }
    }


    @Override
    public <T> T newInstance(String cname, Class<T> expectedType, String[] subPackages, Class[] params, Object[] args) {

        PluginInfo.ClassName cName = new PluginInfo.ClassName(cname);
        if (cName.pkg == null) {
            return coreResourceLoader.newInstance(cname, expectedType, subPackages, params, args);
        } else {
            throw new UnsupportedOperationException("cname:" + cName.original + ",expectedType:" + expectedType);
        }
    }

    private <T> T findAndCreatePlugin(PluginInfo.ClassName cName, Class<? extends IdentityDescribale> expectedType) {

        PluginStore<? extends IdentityDescribale> fieldTypeStore = TIS.getPluginStore(collectionName, expectedType);
        for (IdentityDescribale ftFactory : fieldTypeStore.getPlugins()) {
            if (cName.className.equals(ftFactory.identityValue())) {
                return (T) ftFactory.createInstance();
            }
        }

        throw new IllegalStateException("can not find instance '" + cName.className + "',collection:" + collectionName
                + " type:" + expectedType + ",exist:" + fieldTypeStore.getPlugins().stream().map((i) -> (i).identityValue() + ":"
                + i.getDescriptor().getId()).collect(Collectors.joining(",")));
    }

    @Override
    public <T> Class<? extends T> findClass(String cname, Class<T> expectedType) {
        return null;
    }

    @Override
    public String packageName() {
        return null;
    }

    @Override
    public PluginInfo pluginInfo() {
        return null;
    }

    @Override
    public void changed(PackageLoader.Package pkg, Ctx ctx) {
        throw new UnsupportedOperationException("TIS is not support solr default package plugin implemention");
    }
}
