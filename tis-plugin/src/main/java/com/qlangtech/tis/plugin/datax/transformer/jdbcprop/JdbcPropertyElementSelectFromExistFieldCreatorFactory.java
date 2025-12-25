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

package com.qlangtech.tis.plugin.datax.transformer.jdbcprop;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.qlangtech.tis.datax.IDataxReader;
import com.qlangtech.tis.plugin.datax.SelectedTab;
import com.qlangtech.tis.plugin.datax.ThreadCacheTableCols;
import com.qlangtech.tis.plugin.ds.CMeta;
import com.qlangtech.tis.plugin.ds.ContextParamConfig;
import org.apache.commons.collections.MapUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 从已有的数据表中选择一个已有的数据列
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-06-17 10:21
 **/
public class JdbcPropertyElementSelectFromExistFieldCreatorFactory extends JdbcPropertyElementCreatorFactory {
    @Override
    protected void setPropertyInCollectionFieldType(JSONObject biz) {
        super.setPropertyInCollectionFieldType(biz);
        biz.put("selectFromExistField", true);
    }

    @Override
    protected List<CMeta> getColsCandidate() {
        List<CMeta> colsCandidate = super.getColsCandidate();

        ThreadCacheTableCols threadCacheTabCols = SelectedTab.getContextTableColsStream();

        Map<String, ContextParamConfig> dbContextParams = threadCacheTabCols.getDBContextParams();
        if (MapUtils.isNotEmpty(dbContextParams)) {
            colsCandidate = Lists.newArrayList(colsCandidate);
            for (Map.Entry<String, ContextParamConfig> entry : dbContextParams.entrySet()) {
                colsCandidate.add(CMeta.create(Optional.empty(),entry.getKey(), entry.getValue().getDataType()));
            }
        }

        return colsCandidate;
    }
}
