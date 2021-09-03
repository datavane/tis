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

package com.qlangtech.tis.full.dump;

import com.qlangtech.tis.exec.impl.DefaultChainContext;
import com.qlangtech.tis.fullbuild.phasestatus.PhaseStatusCollection;
import com.qlangtech.tis.fullbuild.phasestatus.impl.DumpPhaseStatus;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.order.center.IParamContext;
import com.qlangtech.tis.order.center.IndexSwapTaskflowLauncher;
import com.qlangtech.tis.test.TISEasyMock;
import junit.framework.TestCase;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-09-02 20:33
 **/
public class TestDefaultChainContext extends TestCase implements TISEasyMock {

    // private static final String dataXname = "dataXName";

    private static final String dataXname = "mysql_elastic";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        Config.setDataDir("./src/test/resources/com/qlangtech/tis/full/dump");
        HttpUtils.mockConnMaker = new HttpUtils.DefaultMockConnectionMaker();
        // HttpUtils.addMockApply(-1,)
        HttpUtils.addMockApply(0, "do_get_latest_success_workflow", "getLatestWFSuccessTaskId_false.json", TestDefaultChainContext.class);
        HttpUtils.addMockApply(1, "do_get_latest_success_workflow", "getLatestWFSuccessTaskId_success.json", TestDefaultChainContext.class);
        String s = IndexSwapTaskflowLauncher.KEY_INDEX_SWAP_TASK_FLOW_LAUNCHER;
    }

    public void testLoadPhaseStatusFromLatest() {
        IParamContext paramContext = this.mock("paramContext", IParamContext.class);
        DefaultChainContext chainContext = new DefaultChainContext(paramContext);

        PhaseStatusCollection statusCollection = chainContext.loadPhaseStatusFromLatest(dataXname);
        assertNull(statusCollection);

// ./src/test/resources/com/qlangtech/tis/full/dump/cfg_repo/df-logs/66/dump
        statusCollection = chainContext.loadPhaseStatusFromLatest(dataXname);
        assertNotNull(statusCollection);
        DumpPhaseStatus dumpPhase = statusCollection.getDumpPhase();
        assertNotNull(dumpPhase);
        assertEquals(62, dumpPhase.getTaskId());
        String dataXFileName = "instancedetail_0.json";
        DumpPhaseStatus.TableDumpStatus dataXExecStatus = dumpPhase.getTable(dataXFileName);
        assertNotNull(dataXFileName + " relevant dataX instance can be null", dataXExecStatus);
        assertEquals(524525, dataXExecStatus.getReadRows());
        assertEquals(1000001, dataXExecStatus.getAllRows());
    }
}
