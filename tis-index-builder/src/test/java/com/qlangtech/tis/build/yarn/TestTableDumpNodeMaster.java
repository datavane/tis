/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
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
