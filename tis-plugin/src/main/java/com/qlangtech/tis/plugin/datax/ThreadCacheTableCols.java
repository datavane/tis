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

package com.qlangtech.tis.plugin.datax;

import com.qlangtech.tis.datax.IDataxReader;
import com.qlangtech.tis.plugin.ds.CMeta;
import com.qlangtech.tis.plugin.ds.ColumnMetaData;
import com.qlangtech.tis.plugin.ds.ContextParamConfig;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-06-25 10:08
 * @see com.qlangtech.tis.plugin.datax.SelectedTab
 **/
public class ThreadCacheTableCols {
    private final Function<EntityName, List<CMeta>> selectedColsSupplier;
    private List<CMeta> selectedCols;
    private final List<ColumnMetaData> selectableCols;
    private IDataxReader plugin;
    private final EntityName targetTable;
    // private final Function<List<ColumnMetaData>, Stream<ColumnMetaData>> selectableColsStreamFunc;

    public static ThreadCacheTableCols createEmptyTableCols() {
        List<ColumnMetaData> empt = Collections.emptyList();

        return new ThreadCacheTableCols(null, null, (target) -> Collections.emptyList(), empt) {
            @Override
            public List<CMeta> getSelectedCols() {
                return Collections.emptyList();
            }

            @Override
            public Map<String, ContextParamConfig> getDBContextParams() {
                return ContextParamConfig.defaultContextParams();
            }

            @Override
            public Stream<ColumnMetaData> getStreamedSelectableCols(Function<List<ColumnMetaData>,
                    Stream<ColumnMetaData>> selectableColsStreamFunc) {
                return Stream.empty();
            }
        };//
    }

    /**
     * @param selectedCols   已经选中的列
     * @param selectableCols
     */
    public ThreadCacheTableCols(IDataxReader plugin, EntityName targetTable,
                                Function<EntityName, List<CMeta>> selectedCols, List<ColumnMetaData> selectableCols) {
        this.selectedColsSupplier = selectedCols;
        this.selectableCols = selectableCols;
        this.plugin = plugin;
        this.targetTable = targetTable;
    }

    public Map<String, ContextParamConfig> getDBContextParams() {
        return plugin.getDBContextParams();
    }

    public List<CMeta> getSelectedCols() {
        if (selectedCols == null) {
            selectedCols = selectedColsSupplier.apply(Objects.requireNonNull(targetTable,
                    "targetTable can not be " + "null"));
        }
        return selectedCols;
    }

    public Stream<ColumnMetaData> getStreamedSelectableCols(Function<List<ColumnMetaData>, Stream<ColumnMetaData>> selectableColsStreamFunc) {
        return selectableColsStreamFunc.apply(this.selectableCols);
    }
}
