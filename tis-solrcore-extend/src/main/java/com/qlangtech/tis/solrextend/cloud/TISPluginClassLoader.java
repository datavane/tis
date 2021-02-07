/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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

//            if (expectedType == FieldType.class) {
//                return (T) findAndCreatePlugin(cName, FieldTypeFactory.class);
//            } else if (expectedType == CharFilterFactory.class) {
//                return (T) findAndCreatePlugin(cName, CharFilterFactoryFactory.class);
//            } else if (expectedType == TokenFilterFactory.class) {
//                return (T) findAndCreatePlugin(cName, TokenFilterFactoryFactory.class);
//            } else if (expectedType == TokenizerFactory.class) {
//                return (T) findAndCreatePlugin(cName, TokenizerFactoryFactory.class);
//            } else {
//                throw new IllegalStateException("expectedType:" + expectedType + " is illegal");
//            }
//            PackageLoader.Package.Version version = findPkgVersion(cName);
//            return applyResourceLoaderAware(version, version.getLoader().newInstance(cName.className, expectedType, subpackages));
            //   return null;
        }
    }


    @Override
    public <T> T newInstance(String cname, Class<T> expectedType, String[] subPackages, Class[] params, Object[] args) {

        //  throw new UnsupportedOperationException();

        PluginInfo.ClassName cName = new PluginInfo.ClassName(cname);
        if (cName.pkg == null) {
            return coreResourceLoader.newInstance(cname, expectedType, subPackages, params, args);
        } else {
//            PackageLoader.Package.Version version = findPkgVePackageLoader.Package.Version version = findPkgVersion(cName);
////            return applyResourceLoaderAware(version, version.getLoader().newInstance(cName.className, expectedType, subPackages, params, args));rsion(cName);
//            return applyResourceLoaderAware(version, version.getLoader().newInstance(cName.className, expectedType, subPackages, params, args));
            throw new UnsupportedOperationException("cname:" + cName.original + ",expectedType:" + expectedType);
        }
//
//        return null;
    }

    private <T> T findAndCreatePlugin(PluginInfo.ClassName cName, Class<? extends IdentityDescribale> expectedType) {

        PluginStore<? extends IdentityDescribale> fieldTypeStore = TIS.getPluginStore(collectionName, expectedType);
        for (IdentityDescribale ftFactory : fieldTypeStore.getPlugins()) {
            if (cName.className.equals(ftFactory.identityValue())) {
                return (T) ftFactory.createInstance();
            }
        }

        throw new IllegalStateException("can not find instance " + cName.className + ",collection:" + collectionName + " type:" + expectedType);
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
