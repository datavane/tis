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

import com.google.common.collect.Lists;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.datax.IDataxReader;
import com.qlangtech.tis.datax.IGroupChildTaskIterator;
import com.qlangtech.tis.datax.IStreamTableMeta;
import com.qlangtech.tis.datax.TableAlias;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.impl.SuFormProperties;
import com.qlangtech.tis.plugin.ds.*;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import com.qlangtech.tis.util.IPluginContext;
import com.qlangtech.tis.util.UploadPluginMeta;
import junit.framework.TestCase;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2023-03-24 15:06
 **/
public class TestSelectedTab extends TestCase {

    static final String tabName = "order";

    public void testGetContextTableCols() {
        DataSourceMetaPlugin sourceMetaPlugin = new DataSourceMetaPlugin();

        UploadPluginMeta pluginMeta = UploadPluginMeta.parse("test");

        SuFormProperties.setSuFormGetterContext(sourceMetaPlugin, pluginMeta, tabName);

        List<CMeta> selectableCols = SelectedTab.getColsCandidate();
        Assert.assertEquals(2, selectableCols.size());

        List<CMeta> selectedCols = SelectedTab.getSelectedCols();
        Assert.assertEquals(1, selectedCols.size());
    }


    public static class DataSourceMetaPlugin
            implements Describable<DataSourceMetaPlugin>, IDataxReader {

        static List<ColumnMetaData> cols = Lists.newArrayList();
        static final ColumnMetaData orderId;

        static {
            orderId = new ColumnMetaData(0, "order_id", DataType.createVarChar(10), true);
            cols.add(orderId);

            ColumnMetaData col = new ColumnMetaData(1, "name", DataType.createVarChar(10), false);
            cols.add(col);
        }

        @Override
        public boolean isSupportBatch() {
            return true;
        }

        @Override
        public List<ColumnMetaData> getTableMetadata(boolean inSink, IPluginContext pluginContext, EntityName table) throws TableNotFoundException {
            Assert.assertEquals(tabName, table.getTableName());
            //  List<ColumnMetaData> cols = Lists.newArrayList();
            //  int index, String key, DataType type, boolean pk
            return cols;
        }


        @Override
        public List<SelectedTab> getSelectedTabs() {

            SelectedTab tab = new SelectedTab();
            tab.name = tabName;
            tab.cols = Lists.newArrayList(ColumnMetaData.convert(orderId));
            return Collections.singletonList(tab);
        }

        @Override
        public IGroupChildTaskIterator getSubTasks(Predicate<ISelectedTab> filter) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getTemplate() {
            throw new UnsupportedOperationException();
        }

        @Override
        public IStreamTableMeta getStreamTableMeta(TableAlias tableAlias) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void startScanDependency() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void refresh() {
            throw new UnsupportedOperationException();
        }
    }
}
