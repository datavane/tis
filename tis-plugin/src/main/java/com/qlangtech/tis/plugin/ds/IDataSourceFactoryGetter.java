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

package com.qlangtech.tis.plugin.ds;

import com.alibaba.datax.common.util.Configuration;
import com.alibaba.datax.core.job.IJobContainerContext;
import com.qlangtech.tis.TIS;
import com.qlangtech.tis.datax.DataXName;
import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.datax.IDataxReader;
import com.qlangtech.tis.datax.StoreResourceType;
import com.qlangtech.tis.datax.impl.DataxProcessor;
import com.qlangtech.tis.datax.impl.DataxWriter;
import com.qlangtech.tis.offline.DataxUtils;

import java.util.Objects;
import java.util.function.Function;

/**
 * 适配 BasicDataXRdbmsReader 和 BasicDataXRdbmsWriter DataSource获取借口
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-09-06 12:14
 **/
public interface IDataSourceFactoryGetter {
    public static IDataSourceFactoryGetter getWriterDataSourceFactoryGetter(Configuration originalConfig, IJobContainerContext containerContext) {
        return getDataSourceFactoryGetter(originalConfig, containerContext, (res) -> {
            return DataxWriter.load(null, res.getType(), res.getPipelineName(), true);
        });
    }

    public static IDataSourceFactoryGetter getReaderDataSourceFactoryGetter(Configuration config,
                                                                            IJobContainerContext containerContext) {
        return getDataSourceFactoryGetter(config, containerContext, (res) -> {

            if (res.getType() != StoreResourceType.DataFlow) {
                IDataxProcessor processor = DataxProcessor.load(null, res.getType(), res.getPipelineName());
                IDataxReader reader = null;
                if ((reader = processor.getReader(null)) instanceof IDataSourceFactoryGetter) {
                    return reader;
                }
            }


            final DBIdentity dbFactoryId = DBIdentity.parseId(config.getString(DataxUtils.DATASOURCE_FACTORY_IDENTITY));
            return new IDataSourceFactoryGetter() {
                @Override
                public DataSourceFactory getDataSourceFactory() {
                    return TIS.getDataBasePlugin(new PostedDSProp(dbFactoryId));
                }

                @Override
                public Integer getRowFetchSize() {
                    return 2000;
                }
            };
        });
    }

    static IDataSourceFactoryGetter getDataSourceFactoryGetter(Configuration originalConfig,
                                                               IJobContainerContext containerContext,
                                                               Function<DataXName, Object> callable) {


        DataXName dataXName = containerContext.getTISDataXName(); // originalConfig.getString(DataxUtils.DATAX_NAME);
        StoreResourceType resType =
                StoreResourceType.parse(originalConfig.getString(StoreResourceType.KEY_STORE_RESOURCE_TYPE));
        if (dataXName == null) {
            throw new IllegalArgumentException("param dataXName:" + dataXName + "can not be null");
        }
        if (dataXName.getType() != resType) {
            throw new IllegalStateException("dataXName type:" + dataXName.getType() + " must be equal with resType:" + resType);
        }
        try {
            Object dataxPlugin = callable.apply(dataXName);
            Objects.requireNonNull(dataxPlugin, "dataXName:" + dataXName + " relevant instance can not be null");
            if (!(dataxPlugin instanceof IDataSourceFactoryGetter)) {
                throw new IllegalStateException("dataxWriter:" + dataxPlugin.getClass() + " mus be type of " + IDataSourceFactoryGetter.class);
            }
            return (IDataSourceFactoryGetter) dataxPlugin;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    DataSourceFactory getDataSourceFactory();

    default IDBReservedKeys getDBReservedKeys() {
        return this.getDataSourceFactory();
    }

    /**
     * 批量导出数据，单次导出记录条数供游标遍历
     *
     * @return
     */
    default Integer getRowFetchSize() {
        throw new UnsupportedOperationException();
    }

}
