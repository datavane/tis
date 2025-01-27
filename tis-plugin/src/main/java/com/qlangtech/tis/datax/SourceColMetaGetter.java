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

package com.qlangtech.tis.datax;

import com.google.common.collect.Maps;
import com.qlangtech.tis.datax.IDataxProcessor.TableMap;
import com.qlangtech.tis.plugin.ds.ColumnMetaData;
import com.qlangtech.tis.plugin.ds.TableNotFoundException;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;

import java.util.Map;
import java.util.Objects;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-12-14 15:10
 **/
public class SourceColMetaGetter {
    private final IDataxReader dataXReader;

    private final Map<TableMap, Map<String, ColumnMetaData>> tab2ColsMapper = Maps.newHashMap();

    public static SourceColMetaGetter getNone() {
        SourceColMetaGetter colMetaGetter = new SourceColMetaGetter(null, false) {
            @Override
            public ColumnMetaData getColMeta(TableMap tableMapper, String colName) {
                return null;
            }
        };
        return colMetaGetter;
    }

    public SourceColMetaGetter(IDataxReader dataXReader, boolean validateNull) {
        if (validateNull) {
            Objects.requireNonNull(dataXReader, "dataXReader");
        }
        this.dataXReader = dataXReader;
    }

    protected Map<String, ColumnMetaData> getColMetaDataMap(IDataxReader dataXReader, TableMap tableMapper) {
        try {
            return ColumnMetaData.toMap(dataXReader.getTableMetadata(false, tableMapper));
        } catch (TableNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param tableMapper
     * @param colName
     * @return
     */
    public ColumnMetaData getColMeta(TableMap tableMapper, String colName) {
        Map<String, ColumnMetaData> colsMeta = tab2ColsMapper.get(tableMapper);
        if (colsMeta == null) {
            colsMeta = getColMetaDataMap(dataXReader, tableMapper);
            tab2ColsMapper.put(tableMapper, colsMeta);
        }
        return colsMeta.get(colName);
    }
}
