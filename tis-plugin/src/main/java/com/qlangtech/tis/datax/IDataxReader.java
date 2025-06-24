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

import com.qlangtech.tis.extension.impl.SuFormProperties;
import com.qlangtech.tis.plugin.IRepositoryResourceScannable;
import com.qlangtech.tis.plugin.datax.SelectedTab;
import com.qlangtech.tis.plugin.datax.ThreadCacheTableCols;
import com.qlangtech.tis.plugin.ds.CMeta;
import com.qlangtech.tis.plugin.ds.DataSourceMeta;
import com.qlangtech.tis.plugin.ds.IReaderSource;
import com.qlangtech.tis.plugin.ds.ISelectedTab;
import com.qlangtech.tis.plugin.ds.TableNotFoundException;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.qlangtech.tis.plugin.datax.SelectedTab.KEY_TABLE_COLS;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-07 14:36
 */
public interface IDataxReader extends DataSourceMeta, IDataXPluginMeta
        , IStreamTableMeataCreator.ISourceStreamMetaCreator, IRepositoryResourceScannable, IReaderSource {

    default SourceColMetaGetter createSourceColMetaGetter() {
        return new SourceColMetaGetter(this, true);
    }

    public boolean isSupportBatch();

    /**
     * 是否支持导入多个子表，当reader如果只支持单个表，那writer如果是MysqlWriter就可以指定表名称和列名
     *
     * @return
     */
    default boolean hasMulitTable() {
        return getSelectedTabs().size() > 0;
    }

    <T extends ISelectedTab> List<T> getSelectedTabs();

    @Override
    default ISelectedTab getSelectedTab(String tableName) {
        if (StringUtils.isEmpty(tableName)) {
            throw new IllegalArgumentException("param tableName can not be null");
        }
        for (ISelectedTab tab : getSelectedTabs()) {
            if (tableName.equals(tab.getName())) {
                return tab;
            }
        }
        throw new IllegalStateException("tableName:" + tableName + " relevant tab can not be null");
    }

    /**
     * 取得子任务
     *
     * @return
     */
    default IGroupChildTaskIterator getSubTasks() {
        return getSubTasks((tab) -> true);
    }

    IGroupChildTaskIterator getSubTasks(Predicate<ISelectedTab> filter);

    /**
     * 取得配置模版
     *
     * @return
     */
    String getTemplate();


    public default ThreadCacheTableCols getContextTableColsStream(SuFormProperties.SuFormGetterContext context) {
        return getContextTableColsStream(context, (targetTab) -> {
            //plugin.getSelectedTab()
            // 从临时文件中将已经选中的列取出来
            SelectedTab selectedTab = SelectedTab.loadFromTmp(
                    Objects.requireNonNull(context.store, "store can not be null"), targetTab.getFullName());
            List<SelectedTab> filledSelectedTab = this.fillSelectedTabMeta(Collections.singletonList(selectedTab));
            for (SelectedTab tab : filledSelectedTab) {
                for (CMeta cmeta : tab.getCols()) {
                    if (cmeta.getType() == null) {
                        throw new IllegalStateException("table:" + context.getSubFormIdentityField()
                                + ",col:" + cmeta.getName() + " relevant type can not be null");
                    }
                }
                return tab.getCols();
            }
            throw new IllegalStateException("can not arrive here");
        });
    }


    public default ThreadCacheTableCols getContextTableColsStream(SuFormProperties.SuFormGetterContext context, Function<EntityName, List<CMeta>> selectedCols) {
        // SuFormProperties.SuFormGetterContext context = SuFormProperties.subFormGetterProcessThreadLocal.get();
        if (context == null || context.plugin == null) {
//            List<ColumnMetaData> empt = Collections.emptyList();
//            return new ThreadCacheTableCols(null, null, (target) -> Collections.emptyList(), empt);// empt.stream();
            return ThreadCacheTableCols.createEmptyTableCols();
        }
        IDataxReader plugin = this; //Objects.requireNonNull(context.plugin, "context.plugin can not be null");
//        if (!(plugin instanceof DataSourceMeta)) {
//            throw new IllegalStateException("plugin must be type of " + DataSourceMeta.class.getName() + ", now type "
//                    + "of " + plugin.getClass().getName());
//        }
        DataSourceMeta dsMeta = plugin;
        ThreadCacheTableCols cols = context.getContextAttr(KEY_TABLE_COLS, (key) -> {
            try {
                final EntityName targetTable = EntityName.parse(context.getSubFormIdentityField());
                return new ThreadCacheTableCols(plugin, targetTable, selectedCols
                        , dsMeta.getTableMetadata(false //
                        , Objects.requireNonNull(context.param, "param can not be null").getPluginContext()
                        , targetTable));
            } catch (TableNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
        return cols;// func.apply(cols);
    }
}
