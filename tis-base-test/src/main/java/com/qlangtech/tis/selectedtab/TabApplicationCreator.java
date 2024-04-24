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

package com.qlangtech.tis.selectedtab;

import com.google.common.collect.Lists;
import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.plugin.datax.SelectedTab;
import com.qlangtech.tis.plugin.ds.CMeta;
import com.qlangtech.tis.plugin.ds.DataXReaderColType;

import java.util.List;
import java.util.function.Consumer;

public class TabApplicationCreator {
    public static IDataxProcessor.TableMap getTabApplication(Consumer<List<CMeta>>... colsProcess) {
        String tableName = "application";
        SelectedTab tab = new SelectedTab();
        tab.name = tableName;

        List<CMeta> sourceCols = Lists.newArrayList();
        CMeta col = new CMeta();
        col.setPk(true);
        col.setName("user_id");
        col.setType(DataXReaderColType.Long.dataType);
        tab.primaryKeys = Lists.newArrayList(col.getName());
        sourceCols.add(col);

        col = new CMeta();
        col.setName("user_name");
        col.setType(DataXReaderColType.STRING.dataType);
        sourceCols.add(col);

        for (Consumer<List<CMeta>> p : colsProcess) {
            p.accept(sourceCols);
        }
        tab.cols.addAll(sourceCols);
        IDataxProcessor.TableMap tableMap = new IDataxProcessor.TableMap(tab);
        tableMap.setFrom(tableName);
        tableMap.setTo("application_alias");
        //tableMap.setSourceCols(sourceCols);
        return tableMap;
    }
}
