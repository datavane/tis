/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qlangtech.tis.build.yarn;

import com.qlangtech.tis.build.NodeMaster;
import com.qlangtech.tis.fullbuild.indexbuild.TaskContext;
import com.qlangtech.tis.fullbuild.indexbuild.impl.JobConfParams;
import com.qlangtech.tis.manage.common.CenterResource;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.order.center.IParamContext;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import junit.framework.TestCase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-04-23 14:00
 */
public class TestTableDumpNodeMaster extends TestCase {


    static {
        CenterResource.setNotFetchFromCenterRepository();
//        Config.setDataDir(dataflowDir);
//        try {
//            FileUtils.forceMkdir(new File(dataflowDir));
//        } catch (IOException e) {
//            throw new RuntimeException(dataflowDir, e);
//        }
        HttpUtils.addMockGlobalParametersConfig();
    }

    public void testDump() throws Exception {
        EntityName dumpTable = EntityName.parse("order1.totalpayinfo");
        SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmss");
        Map<String, String> params = new HashMap<>();
        params.put(IParamContext.KEY_TASK_ID, "123");
        TaskContext taskContext = TaskContext.create(params);
        JobConfParams tabDumpParams = JobConfParams.createTabDumpParams(taskContext, dumpTable, Long.parseLong(f.format(new Date())), "yarn_tab_dump");
        NodeMaster.main(tabDumpParams.paramsArray());
    }
}
