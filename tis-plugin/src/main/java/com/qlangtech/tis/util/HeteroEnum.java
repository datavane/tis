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
package com.qlangtech.tis.util;

import com.qlangtech.tis.IPluginEnum;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.async.message.client.consumer.impl.MQListenerFactory;
import com.qlangtech.tis.config.ParamsConfig;
import com.qlangtech.tis.coredefine.module.action.TargetResName;
import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.datax.impl.DataxProcessor;
import com.qlangtech.tis.datax.impl.DataxReader;
import com.qlangtech.tis.datax.impl.DataxWriter;
import com.qlangtech.tis.datax.job.DataXJobWorker;
import com.qlangtech.tis.datax.job.DataXJobWorker.K8SWorkerCptType;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.ExtensionList;
import com.qlangtech.tis.extension.NoStorePlaceholderPlugin;
import com.qlangtech.tis.extension.PluginFormProperties;
import com.qlangtech.tis.extension.SubFormFilter;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.extension.impl.BaseSubFormProperties;
import com.qlangtech.tis.extension.impl.SuFormProperties;
import com.qlangtech.tis.manage.IAppSource;
import com.qlangtech.tis.offline.FileSystemFactory;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.IdentityName;
import com.qlangtech.tis.plugin.KeyedPluginStore;
import com.qlangtech.tis.plugin.KeyedPluginStore.Key;
import com.qlangtech.tis.datax.StoreResourceType;
import com.qlangtech.tis.plugin.credentials.ParamsConfigPluginStore;
import com.qlangtech.tis.plugin.datax.SelectedTab;
import com.qlangtech.tis.plugin.datax.SelectedTabExtend;
import com.qlangtech.tis.plugin.datax.transformer.RecordTransformerRules;
import com.qlangtech.tis.plugin.datax.transformer.TargetColumn;
import com.qlangtech.tis.plugin.datax.transformer.UDFDefinition;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;
import com.qlangtech.tis.plugin.ds.PostedDSProp;
import com.qlangtech.tis.plugin.incr.IncrStreamFactory;
import com.qlangtech.tis.plugin.k8s.K8sImage;
import com.qlangtech.tis.plugin.k8s.K8sImage.ImageCategory;
import com.qlangtech.tis.plugin.utils.UploadCustomizedTPI;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 表明一种插件的类型
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class HeteroEnum<T extends Describable<T>> implements IPluginEnum<T> {

    @TISExtension
    public static final HeteroEnum<FileSystemFactory> FS = new HeteroEnum<FileSystemFactory>(//
            FileSystemFactory.class, //
            "fs", "存储");

    @TISExtension
    public static final HeteroEnum<UploadCustomizedTPI> uploadCustomizedTPI //
            = new HeteroEnum<UploadCustomizedTPI>(//
            UploadCustomizedTPI.class, //
            "uploadCustomizedTPI", "upload customized TPI", Selectable.Multi, true) {
        @Override
        public IPluginStore getPluginStore(IPluginContext pluginContext, UploadPluginMeta pluginMeta) {
            return IPluginStore.noSaveStore();
        }
    };

    @TISExtension
    public static final HeteroEnum<RecordTransformerRules> TRANSFORMER_RULES //
            = new HeteroEnum<RecordTransformerRules>(//
            RecordTransformerRules.class, //
            "transformer", "Transformer", Selectable.Multi, true) {
        @Override
        public IPluginStore getPluginStore(IPluginContext pluginContext, UploadPluginMeta pluginMeta) {
            final String tableName = pluginMeta.getExtraParam(SuFormProperties.SuFormGetterContext.FIELD_SUBFORM_ID);
            if (StringUtils.isEmpty(tableName)) {
                throw new IllegalStateException("extra param " + SuFormProperties.SuFormGetterContext.FIELD_SUBFORM_ID + " can not be empty");
            }
            return HeteroEnum.createDataXReaderAndWriterRelevant(pluginContext, pluginMeta, new DBOrAppRelevantCreator<IPluginStore>() {
                @Override
                public IPluginStore dbRelevant(IPluginContext pluginContext, String saveDbName) {
                    Key key = TransformerRuleKey.createStoreKey(pluginContext, StoreResourceType.DataBase, saveDbName, tableName);
                    return TIS.getPluginStore(key);
                }

                @Override
                public IPluginStore appRelevant(IPluginContext pluginContext, DataXName dataxName) {
                    final String appName = dataxName.getPipelineName();
                    Key key = TransformerRuleKey.createStoreKey(pluginContext, pluginMeta.getProcessModel().resType, appName, tableName);
                    return TIS.getPluginStore(key);
                }
            });


        }


    };


    @TISExtension
    public static final HeteroEnum<NoStorePlaceholderPlugin> noStore //
            = new HeteroEnum<NoStorePlaceholderPlugin>(//
            NoStorePlaceholderPlugin.class, //
            "noStore", "noStore", Selectable.Multi, false) {
        @Override
        public IPluginStore getPluginStore(IPluginContext pluginContext, UploadPluginMeta pluginMeta) {
            return IPluginStore.noSaveStore();
        }
    };


    @TISExtension
    public static final HeteroEnum<UDFDefinition> TRANSFORMER_UDF //
            = new HeteroEnum<UDFDefinition>(//
            UDFDefinition.class, //
            "transformerUDF", "Transformer UDF", Selectable.Multi, true) {
        @Override
        public IPluginStore getPluginStore(IPluginContext pluginContext, UploadPluginMeta pluginMeta) {
            return IPluginStore.noSaveStore();
        }
    };

    @TISExtension
    public static final HeteroEnum<TargetColumn> TARGET_COLUMN //
            = new HeteroEnum<TargetColumn>(//
            TargetColumn.class, //
            "target-column", "Target Column", Selectable.Multi, true) {
        @Override
        public IPluginStore getPluginStore(IPluginContext pluginContext, UploadPluginMeta pluginMeta) {
            return IPluginStore.noSaveStore();
        }
    };


    // ////////////////////////////////////////////////////////
    @TISExtension
    public static final HeteroEnum<MQListenerFactory> MQ = new HeteroEnum<MQListenerFactory>(//
            MQListenerFactory.class, //
            "mq", "Source Factory", Selectable.Multi, true) {
        @Override
        public IPluginStore getPluginStore(IPluginContext pluginContext, UploadPluginMeta pluginMeta) {
            return super.getPluginStore(pluginContext, pluginMeta);
        }
    };


    // ////////////////////////////////////////////////////////
    @TISExtension
    public static final HeteroEnum<ParamsConfig> PARAMS_CONFIG = new HeteroEnum<ParamsConfig>(//
            ParamsConfig.class, //
            "params-cfg", // },//
            "基础配置", Selectable.Multi, false) {
        @Override
        public IPluginStore getPluginStore(IPluginContext pluginContext, UploadPluginMeta pluginMeta) {
            return new ParamsConfigPluginStore(pluginMeta);
        }

//        @Override
//        public <T extends Describable<T>> List<Descriptor<T>> descriptors() {
//            return super.descriptors();
//        }
    };
    // ////////////////////////////////////////////////////////
    private static final String KEY_K8S_IMAGES = "k8s-images";

    private static class DockerImageHeteroEnum extends HeteroEnum<K8sImage> {
        private ImageCategory imageCategory;

        private static String parseCaption(String token) {
            String[] tokens = StringUtils.split(token, "-");
            return Arrays.stream(tokens).map((t) -> StringUtils.capitalize(t)).collect(Collectors.joining("-"));
        }

        public DockerImageHeteroEnum(ImageCategory imageCategory) {
            super(K8sImage.class, imageCategory.token, parseCaption(imageCategory.token));
            this.imageCategory = imageCategory;
        }

        @Override
        public List<Descriptor<K8sImage>> descriptors() {
            List<Descriptor<K8sImage>> descs = getPluginStore(null, null).allDescriptor();
            return descs.stream().filter((desc) -> imageCategory.token.equals(desc.getDisplayName())).collect(Collectors.toList());
        }

        @Override
        public IPluginStore getPluginStore(IPluginContext pluginContext, UploadPluginMeta pluginMeta) {
            // UploadPluginMeta.TargetDesc targetDesc = pluginMeta.getTargetDesc();
            // ImageCategory imageCategory = K8sImage.ImageCategory.parse(targetDesc.matchTargetPluginDescName);
            KeyedPluginStore.Key key = new KeyedPluginStore.Key(KEY_K8S_IMAGES, imageCategory.token, this.extensionPoint);
            return TIS.getPluginStore(key);
        }
    }

    @TISExtension
    public static final HeteroEnum<K8sImage> K8S_DEFAULT_IMAGES = new DockerImageHeteroEnum(ImageCategory.DEFAULT_DESC_NAME);
    @TISExtension
    public static final HeteroEnum<K8sImage> K8S_POWERJOB_IMAGES = new DockerImageHeteroEnum(ImageCategory.DEFAULT_POWERJOB_DESC_NAME);
    @TISExtension
    public static final HeteroEnum<K8sImage> K8S_FLINK_IMAGES = new DockerImageHeteroEnum(ImageCategory.DEFAULT_FLINK_DESC_NAME);
    // ////////////////////////////////////////////////////////
    @TISExtension
    public static final HeteroEnum<DataXJobWorker> appJobWorkerTplReWriter = new HeteroEnum<DataXJobWorker>(//
            DataXJobWorker.class, //
            DataXJobWorker.K8SWorkerCptType.JobTplAppOverwrite.token
            , DataXJobWorker.K8SWorkerCptType.JobTplAppOverwrite.name(), Selectable.Single, true) {
        @Override
        public IPluginStore getPluginStore(IPluginContext pluginContext, UploadPluginMeta pluginMeta) {
            return super.getPluginStore(pluginContext, pluginMeta);
        }

        @Override
        public List<DataXJobWorker> getPlugins(IPluginContext pluginContext, UploadPluginMeta pluginMeta) {
            return super.getPlugins(pluginContext, pluginMeta);
        }
    };
    @TISExtension
    public static final HeteroEnum<DataXJobWorker> DATAX_WORKER = new HeteroEnum<DataXJobWorker>(//
            DataXJobWorker.class, //
            "datax-worker", // },//
            "DataX Worker", Selectable.Single, true) {
        @Override
        public IPluginStore getPluginStore(IPluginContext pluginContext, UploadPluginMeta pluginMeta) {

            if (!pluginContext.isCollectionAware()) {
                throw new IllegalStateException("must be collection aware");
            }
            DataXName dataXName = pluginMeta.getDataXName();
            DataXJobWorker.K8SWorkerCptType powerjobCptType = DataXJobWorker.K8SWorkerCptType.parse(dataXName.getPipelineName());
            DataXName dataX = pluginContext.getCollectionName();
            if (dataX.getType() != StoreResourceType.DataApp) {
                throw new IllegalStateException(" dataX.getType must be " + StoreResourceType.DataApp);
            }
            return DataXJobWorker.getJobWorkerStore(new TargetResName(dataX.getPipelineName()), Optional.of(powerjobCptType));

            //return super.getPluginStore(pluginContext, pluginMeta);
        }
    };

    public static <T extends DataXJobWorker> T getFlinkK8SSessionCluster(String clusterId) {
        return (T) K8S_SESSION_WORKER.getPluginStore(null
                , UploadPluginMeta.parse(K8SWorkerCptType.FlinkCluster.token + ":"
                        + UploadPluginMeta.KEY_REQUIRE + "," + StoreResourceType.DATAX_NAME + "_" + clusterId)).getPlugin();
    }

    @TISExtension
    public static final HeteroEnum<DataXJobWorker> K8S_SESSION_WORKER = new HeteroEnum<DataXJobWorker>(//
            DataXJobWorker.class, //
            K8SWorkerCptType.FlinkCluster.token, // },//
            K8SWorkerCptType.FlinkCluster.name(), Selectable.Single, true) {
        @Override
        public IPluginStore getPluginStore(IPluginContext pluginContext, UploadPluginMeta pluginMeta) {

//            if (!pluginContext.isCollectionAware()) {
//                throw new IllegalStateException("must be collection aware");
//            }
            return DataXJobWorker.getJobWorkerStore(
                    new TargetResName(K8SWorkerCptType.FlinkCluster.token + "/" + pluginMeta.getDataXName())
                    , Optional.of(K8SWorkerCptType.FlinkCluster));
        }
    };

    @TISExtension
    public static final HeteroEnum<DataXJobWorker> Flink_Kubernetes_Application_Cfg = new HeteroEnum<DataXJobWorker>(//
            DataXJobWorker.class, //
            K8SWorkerCptType.FlinkKubernetesApplicationCfg.token, // },//
            K8SWorkerCptType.FlinkKubernetesApplicationCfg.name(), Selectable.Multi, true) {
        @Override
        public boolean isIdentityUnique() {
            return true;
        }

        @Override
        public IPluginStore getPluginStore(IPluginContext pluginContext, UploadPluginMeta pluginMeta) {
            return DataXJobWorker.getFlinkKubernetesApplicationCfgStore();
            //return super.getPluginStore(pluginContext, pluginMeta);
        }
    };
    // ////////////////////////////////////////////////////////

    @TISExtension
    public static final HeteroEnum<IncrStreamFactory> INCR_STREAM_CONFIG = new HeteroEnum<>(//
            IncrStreamFactory.class, //
            "incr-config", // },
            "增量引擎配置", Selectable.Single, true);

    @TISExtension
    public static final HeteroEnum<DataSourceFactory> DATASOURCE = new HeteroEnum<DataSourceFactory>(//
            DataSourceFactory.class, //
            "datasource", //
            "数据源", //
            Selectable.Single, true) {
        @Override
        public IPluginStore getPluginStore(IPluginContext pluginContext, UploadPluginMeta pluginMeta) {
            //return super.getPluginStore(pluginContext, pluginMeta);

            if (!pluginContext.isDataSourceAware()) {
                throw new IllegalArgumentException("pluginContext must be dataSourceAware");
            }

            PostedDSProp dsProp = PostedDSProp.parse(pluginMeta);
            if (!dsProp.getDbname().isPresent()) {
                return null;
            }
            return TIS.getDataSourceFactoryPluginStore(dsProp);
        }
    };
    //    @TISExtension
    //    public static final HeteroEnum<FieldTypeFactory> SOLR_FIELD_TYPE = new HeteroEnum<FieldTypeFactory>(//
    //            FieldTypeFactory.class, //
    //            "field-type", //
    //            "字段类型", //
    //            Selectable.Multi);
    //    //  @TISExtension
    //    public static final HeteroEnum<QueryParserFactory> SOLR_QP = new HeteroEnum<QueryParserFactory>(//
    //            QueryParserFactory.class, //
    //            "qp", //
    //            "QueryParser", //
    //            Selectable.Multi);
    //    //@TISExtension
    //    public static final HeteroEnum<SearchComponentFactory> SOLR_SEARCH_COMPONENT = new
    //    HeteroEnum<SearchComponentFactory>(//
    //            SearchComponentFactory.class, //
    //            "searchComponent", //
    //            "SearchComponent", //
    //            Selectable.Multi);
    //    //@TISExtension
    //    public static final HeteroEnum<TISTransformerFactory> SOLR_TRANSFORMER = new
    //    HeteroEnum<TISTransformerFactory>(//
    //            TISTransformerFactory.class, //
    //            "transformer", //
    //            "Transformer", //
    //            Selectable.Multi);
    @TISExtension
    public static final HeteroEnum<DataxReader> DATAX_READER = new HeteroEnum<DataxReader>(//
            DataxReader.class, //
            "dataxReader", //
            "DataX Reader", //
            Selectable.Multi, true) {
        @Override
        public IPluginStore getPluginStore(IPluginContext pluginContext, UploadPluginMeta pluginMeta) {
            //   return super.getPluginStore(pluginContext, pluginMeta);
            return getDataXReaderAndWriterStore(pluginContext, true, pluginMeta, Optional.empty());
        }
    };
    @TISExtension
    public static final HeteroEnum<DataxWriter> DATAX_WRITER = new HeteroEnum<DataxWriter>(//
            DataxWriter.class, //
            "dataxWriter", //
            "DataX Writer", //
            Selectable.Multi, true) {
        @Override
        public IPluginStore getPluginStore(IPluginContext pluginContext, UploadPluginMeta pluginMeta) {
            return getDataXReaderAndWriterStore(pluginContext, false, pluginMeta);
        }
    };

    @TISExtension
    public static final HeteroEnum<IAppSource> APP_SOURCE = new HeteroEnum<IAppSource>(//
            IAppSource.class, //
            "appSource", //
            "App Source", //
            Selectable.Multi, true) {
        @Override
        public IPluginStore getPluginStore(IPluginContext pluginContext, UploadPluginMeta pluginMeta) {
            final DataXName dataxName = pluginMeta.getDataXName();// (pluginMeta.getExtraParam(DataxUtils.DATAX_NAME));
//            if (dataxName.assetCheckDataAppType() != pluginMeta.getProcessModel().resType) {
//
//            }
            return com.qlangtech.tis.manage.IAppSource.getPluginStore(pluginContext,
                    pluginMeta.getProcessModel().resType, dataxName.getPipelineName());
        }
    };

//    @TISExtension
//    public static final HeteroEnum<JobTrigger> JOB_TRIGGER = new HeteroEnum<JobTrigger>(//
//            JobTrigger.class, //
//            "jobTrigger", //
//            "Job Trigger", //
//            Selectable.Single, true) {
//        @Override
//        public IPluginStore getPluginStore(IPluginContext pluginContext, UploadPluginMeta pluginMeta) {
//            return IPluginStore.noSaveStore();
//        }
//    };

    public final String caption;

    public final String identity;

    public final Class<? extends Describable> extensionPoint;

    // public final IDescriptorsGetter descriptorsGetter;
    // private final IItemGetter itemGetter;
    public final Selectable selectable;
    private final boolean appNameAware;

    public HeteroEnum(Class<T> extensionPoint, String identity, String caption, Selectable selectable) {
        this(extensionPoint, identity, caption, selectable, false);
    }

    // for Test stub
    public static Function<String, MQListenerFactory> incrSourceListenerFactoryStub;

    public static MQListenerFactory getIncrSourceListenerFactory(DataXName dataXName) {
        dataXName.assetCheckDataAppType();
        if (incrSourceListenerFactoryStub != null) {
            return incrSourceListenerFactoryStub.apply(dataXName.getPipelineName());
        }

        IPluginContext pluginContext = IPluginContext.namedContext(dataXName.getPipelineName());
        List<MQListenerFactory> mqFactories = MQ.getPlugins(pluginContext, null);
        MQListenerFactory mqFactory = null;
        for (MQListenerFactory factory : mqFactories) {
            mqFactory = factory;
        }
        Objects.requireNonNull(mqFactory,
                "mqFactory can not be null, dataXName:" + dataXName + " mqFactories size:" + mqFactories.size());
        return mqFactory;
    }

    public static IncrStreamFactory getIncrStreamFactory(String dataxName) {
        IPluginContext pluginContext = IPluginContext.namedContext(dataxName);
        List<IncrStreamFactory> streamFactories = HeteroEnum.INCR_STREAM_CONFIG.getPlugins(pluginContext, null);
        for (IncrStreamFactory factory : streamFactories) {
            return factory;
        }
        throw new IllegalStateException("stream app:" + dataxName + " incrController can not not be null");
    }

    public static IPluginStore<?> getDataXReaderAndWriterStore(IPluginContext pluginContext, boolean getReader,
                                                               UploadPluginMeta pluginMeta) {
        return getDataXReaderAndWriterStore(pluginContext, getReader, pluginMeta, Optional.empty());
    }

    public static IPluginStore<?> getDataXReaderAndWriterStore(IPluginContext pluginContext, boolean getReader,
                                                               UploadPluginMeta pluginMeta,
                                                               Optional<SubFormFilter> subFormFilter) {
        IPluginStore<?> store = null;

        if (subFormFilter.isPresent()) {
            SubFormFilter filter = subFormFilter.get();
            Descriptor targetDescriptor = filter.getTargetDescriptor();

            final Class<Describable> clazz = targetDescriptor.getT();

            PluginFormProperties pluginProps = targetDescriptor.getPluginFormPropertyTypes(subFormFilter);

            store = pluginProps.accept(new PluginFormProperties.IVisitor() {
                @Override
                public IPluginStore<?> visit(BaseSubFormProperties props) {
                    // 为了在更新插件时候不把plugin上的@SubForm标记的属性覆盖掉，需要先将老的plugin上的值覆盖到新http post过来的反序列化之后的plugin上
                    //   Class<Describable> clazz = (Class<Describable>) heteroEnum.getExtensionPoint();
                    // DataxReader.SubFieldFormAppKey<Describable> key =
                    return HeteroEnum.createDataXReaderAndWriterRelevant(pluginContext, pluginMeta,
                            new DBOrAppRelevantCreator<IPluginStore>() {
                                @Override
                                public IPluginStore dbRelevant(IPluginContext pluginContext, String saveDbName) {
                                    DataxReader.SubFieldFormAppKey key = new DataxReader.SubFieldFormAppKey<>(pluginContext,
                                            true, saveDbName, props, clazz);
                                    return KeyedPluginStore.getPluginStore(key);
                                }

                                @Override
                                public IPluginStore appRelevant(IPluginContext pluginContext, DataXName dataxName) {

                                    DataxReader.SubFieldFormAppKey key = new DataxReader.SubFieldFormAppKey<>(pluginContext,
                                            false, dataxName.getPipelineName(), props, clazz);
                                    KeyedPluginStore<SelectedTab> subFormStore = KeyedPluginStore.getPluginStore(key);

                                    return SelectedTabExtend.wrapSubFormStore(pluginContext, dataxName, subFormStore);
                                }
                            });
                }
            });
        } else {
            store = getDataXReaderAndWriterRelevantPluginStore(pluginContext, getReader, pluginMeta);
        }
        return store;
    }

    public static IPluginStore<?> getDataXReaderAndWriterRelevantPluginStore(
            IPluginContext pluginContext, boolean getReader, UploadPluginMeta pluginMeta) {
        IPluginStore<?> store;
        store = createDataXReaderAndWriterRelevant(pluginContext, pluginMeta,
                new DBOrAppRelevantCreator<IPluginStore<?>>() {
                    @Override
                    public IPluginStore<?> dbRelevant(IPluginContext pluginContext, String saveDbName) {
                        if (!getReader) {
                            throw new IllegalStateException("getReader must be true");
                        }

                        IPluginContext maskExecIdContext = null; //IPluginContext.namedContext(pluginContext.getCollectionName());
                        return DataxReader.getPluginStore(maskExecIdContext, true, saveDbName);
                    }

                    @Override
                    public IPluginStore<?> appRelevant(IPluginContext pluginContext, DataXName dataxName) {
//                        KeyedPluginStore<?> keyStore = getReader ?
//                                :
                        if (getReader) {
                            if (dataxName.isDataAppType()) {
                                return DataxReader.getPluginStore(pluginContext, dataxName.getPipelineName());
                            } else if (dataxName.isDataFlowType()) {
                                IDataxProcessor dataflowProcessor = DataxProcessor.load(pluginContext, dataxName);
                                return null;
                            } else {
                                throw new IllegalStateException("illegal resource type:" + dataxName);
                            }
                        } else {
                            return DataxWriter.getPluginStore(pluginContext, dataxName.getType(), dataxName.getPipelineName());
                        }


//                        KeyedPluginStore<?> keyStore =
//                               // DataxReader.getPluginStore(pluginContext, dataxName) :
//                                DataxWriter.getPluginStore(pluginContext, dataxName.getType(), dataxName.getPipelineName());

                        // return keyStore;
                    }
                });
        return store;
    }


    public static <T> T createDataXReaderAndWriterRelevant(IPluginContext pluginContext, UploadPluginMeta pluginMeta,
                                                           DBOrAppRelevantCreator<T> creator) {
        final DataXName dataxName = pluginMeta.getDataXName(true);

        //if (StringUtils.isEmpty(dataxName)) {
        if (dataxName.getType() == StoreResourceType.DataBase) {
            String saveDbName = dataxName.getPipelineName();// pluginMeta.getExtraParam(DataxUtils.DATAX_DB_NAME);
            if (StringUtils.isNotBlank(saveDbName)) {
                return creator.dbRelevant(pluginContext, saveDbName);
            } else {
                throw new IllegalArgumentException("plugin extra param " + StoreResourceType.DATAX_NAME + " can not be null");
            }
        } else {
            return creator.appRelevant(pluginContext, dataxName);
        }
    }

    public interface DBOrAppRelevantCreator<T> {
        public T dbRelevant(IPluginContext pluginContext, String saveDbName);

        public T appRelevant(IPluginContext pluginContext, DataXName dataxName);

    }

    @Override
    public boolean isAppNameAware() {
        return this.appNameAware;
    }

    public HeteroEnum(Class<T> extensionPoint, String identity, String caption, Selectable selectable,
                      boolean appNameAware) {
        this.extensionPoint = extensionPoint;
        this.caption = caption;
        this.identity = identity;
        this.selectable = selectable;
        this.appNameAware = appNameAware;
    }

    /**
     * 判断实例是否是应该名称唯一的
     *
     * @return
     */
    public boolean isIdentityUnique() {
        return IdentityName.class.isAssignableFrom(this.extensionPoint);
    }


    HeteroEnum(Class<T> extensionPoint, String identity, String caption) {
        this(extensionPoint, identity, caption, Selectable.Multi);
    }

    public <T> T getPlugin() {
        if (this.selectable != Selectable.Single) {
            throw new IllegalStateException(this.extensionPoint + " selectable is:" + this.selectable);
        }
        IPluginStore store = TIS.getPluginStore(this.extensionPoint);
        return (T) store.getPlugin();
    }

    public Pair<List<T>, IPluginStore> getPluginsAndStore(IPluginContext pluginContext, UploadPluginMeta pluginMeta) {
        IPluginStore store = getPluginStore(pluginContext, pluginMeta);
        if (store == null) {
            return Pair.of(Collections.emptyList(), null);
        }
        List<T> plugins = store.getPlugins();
        UploadPluginMeta.TargetDesc targetDesc = null;
        if (pluginMeta != null && (targetDesc = pluginMeta.getTargetDesc()).shallMatchTargetDesc()) {
            final UploadPluginMeta.TargetDesc finalDesc = targetDesc;
            return Pair.of(plugins.stream().filter((p) -> finalDesc.isNameMatch(p.getDescriptor().getDisplayName())).collect(Collectors.toList()), store);
        }

        return Pair.of(plugins, store);
    }

    /**
     * ref: PluginItems.save()
     *
     * @param pluginContext
     * @param pluginMeta
     * @param
     * @return
     */
    public List<T> getPlugins(IPluginContext pluginContext, UploadPluginMeta pluginMeta) {
        return getPluginsAndStore(pluginContext, pluginMeta).getLeft();
//        IPluginStore store = getPluginStore(pluginContext, pluginMeta);
//        if (store == null) {
//            return Collections.emptyList();
//        }
//        List<T> plugins = store.getPlugins();
//        UploadPluginMeta.TargetDesc targetDesc = null;
//        if (pluginMeta != null && (targetDesc = pluginMeta.getTargetDesc()).shallMatchTargetDesc()) {
//            final UploadPluginMeta.TargetDesc finalDesc = targetDesc;
//            return plugins.stream().filter((p) -> finalDesc.isNameMatch(p.getDescriptor().getDisplayName())).collect(Collectors.toList());
//        }
//
//        return plugins;
    }

    @Override
    public IPluginStore getPluginStore(IPluginContext pluginContext, UploadPluginMeta pluginMeta) {
        IPluginStore store = null;
        if (this.isAppNameAware()) {
            if (!pluginContext.isCollectionAware()) {
                throw new IllegalStateException(this.getExtensionPoint().getName() + " must be collection aware");
            }
            DataXName dataXName = pluginContext.getCollectionName();
            store = TIS.getPluginStore(dataXName.getType().getType(), dataXName.getPipelineName(), this.extensionPoint);
        } else {
            store = TIS.getPluginStore(this.extensionPoint);
        }
        //}
        Objects.requireNonNull(store, "plugin store can not be null");
        return store;
    }


    public <T extends Describable<T>> List<Descriptor<T>> descriptors() {
        IPluginStore pluginStore = TIS.getPluginStore(this.extensionPoint);
        return pluginStore.allDescriptor();
    }

    public static <T extends Describable<T>> IPluginEnum<T> of(String identity) {

        ExtensionList<IPluginEnum> pluginEnums = TIS.get().getExtensionList(IPluginEnum.class);

        for (IPluginEnum he : pluginEnums) {
            if (StringUtils.equals(he.getIdentity(), identity)) {
                return he;
            }
        }
        throw new IllegalStateException("identity:" + identity + " is illegal,exist:"
                + pluginEnums.stream().map((h) -> "'" + h.getIdentity() + "'").collect(Collectors.joining(",")));
    }


    @Override
    public Class getExtensionPoint() {
        return this.extensionPoint;
    }

    @Override
    public String getIdentity() {
        return this.identity;
    }

    @Override
    public String getCaption() {
        return this.caption;
    }

    @Override
    public Selectable getSelectable() {
        return this.selectable;
    }
}
