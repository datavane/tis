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


import com.google.common.collect.Maps;
import com.qlangtech.tis.datax.IDataxProcessor.TableMap;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

/**
 * 数据源meta信息获取
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-07 15:51
 */
public interface DataSourceMeta extends Describable.IRefreshable, IDBReservedKeys {

    /**
     * 可以在Transformer中使用的数据上下文绑定的参数，例如有这样的需求，用户数据源表是分库的分表的，设计表的主键为自增，导入到目标端doris中会合并成一个表，为了保证多个表的主键没有重复冲突,需要将将主键变换成‘dbName’+‘_’+ pk 的方式免除主键冲突 <br/>
     *
     * @return Map<String, Object> key: 添加的可以使用key名称 $dbName， $tableName， $userName 等。
     */
    default Map<String, ContextParamConfig> getDBContextParams() {
        return Maps.newHashMap();
    }

    /**
     * Get all the tables in dataBase
     *
     * @return
     */
    default TableInDB getTablesInDB() {
        throw new UnsupportedOperationException();
    }

    default List<ColumnMetaData> getTableMetadata(boolean inSink, TableMap tableMapper) throws TableNotFoundException {
        return getTableMetadata(inSink, EntityName.parse(tableMapper.getFrom()));
    }

    /**
     * Get table column metaData list
     *
     * @param inSink 是否在处理sink数据源 https://github.com/qlangtech/tis/issues/192,在处理oracle的Date类型时：inSink：true 则要定义成sql.date false: 保持datetime类型
     * @param table
     * @return
     * @throws TableNotFoundException
     */
    default List<ColumnMetaData> getTableMetadata(boolean inSink, EntityName table) throws TableNotFoundException {
        throw new UnsupportedOperationException("invoke from:" + this.getClass().getName() + ",inSink:" + inSink);
    }


    /**
     * Get table column metaData list
     *
     * @param inSink 是否在执行sink流程 https://github.com/qlangtech/tis/issues/192
     * @param table
     * @return
     */
    default List<ColumnMetaData> getTableMetadata(JDBCConnection conn, boolean inSink, EntityName table) throws TableNotFoundException {
        throw new UnsupportedOperationException();
    }

//    public interface JDBCConnectionFactory {
//
//        public JDBCConnection createConnection(String jdbcUrl, boolean verify) throws SQLException;
//    }


    public interface ResultProcess {

        /**
         * @param result
         * @return false: 中断执行
         * @throws Exception
         */
        public boolean callback(ResultSet result) throws Exception;
    }
}
