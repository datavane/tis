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
package com.qlangtech.tis.fullbuild.indexbuild;

import com.qlangtech.tis.plugin.ds.ColMeta;
import com.qlangtech.tis.plugin.ds.DataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public interface IDumpTable {
    static List<String> createPreservedPsCols() {
        List<String> pt = new ArrayList<>();
        pt.add(IDumpTable.PARTITION_PT);
        pt.add(IDumpTable.PARTITION_PMOD);
        return Collections.unmodifiableList(pt);
    }
    String PARTITION_KEY_TYPE = "string";
    static List<ColMeta> appendPreservedPsCols(List<ColMeta> outputCols) {
        ArrayList<ColMeta> result = new ArrayList<>(outputCols);
        for (String ptKey : createPreservedPsCols()) {
            result.add(new ColMeta(ptKey, DataType.createVarChar(32), false));
        }
        return result;
    }


    public List<String> preservedPsCols = createPreservedPsCols();


    String DEFAULT_DATABASE_NAME = "tis";

    String PARTITION_PT = "pt";

    String PARTITION_PMOD = "pmod";



    public String getDbName();

    public String getTableName();

    public String getFullName();
}
