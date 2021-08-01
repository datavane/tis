/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 *
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.full.dump;

import com.qlangtech.tis.manage.common.DagTaskUtils;
import org.apache.commons.lang3.StringUtils;
import com.qlangtech.tis.assemble.FullbuildPhase;
import com.qlangtech.tis.assemble.TriggerType;
import com.qlangtech.tis.exec.ExecutePhaseRange;
import com.qlangtech.tis.manage.common.DagTaskUtils.NewTaskParam;
import junit.framework.TestCase;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年7月3日
 */
public class TestTask extends TestCase {

    public void testCreateTask() {
        NewTaskParam newTaskParam = new NewTaskParam();
        // newTaskParam.setBuildHistoryId(taskId);
        String indexname = "search4supplyUnionTabs";
        if (StringUtils.isNotBlank(indexname)) {
            newTaskParam.setAppname(indexname);
        }
        newTaskParam.setWorkflowid(15);
        ExecutePhaseRange execRange = new ExecutePhaseRange(FullbuildPhase.FullDump, FullbuildPhase.IndexBackFlow);
        // (FullbuildPhase.FullDump);
        newTaskParam.setExecuteRanage(execRange);
        // newTaskParam.setToPhase(FullbuildPhase.IndexBackFlow);
        newTaskParam.setTriggerType(TriggerType.MANUAL);
        Integer taskid = DagTaskUtils.createNewTask(newTaskParam);
        System.out.println(taskid);
    }
}
