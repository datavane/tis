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

package com.qlangtech.tis.fullbuild.phasestatus;

import com.qlangtech.tis.assemble.FullbuildPhase;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.exec.ExecutePhaseRange;
import com.qlangtech.tis.fullbuild.phasestatus.impl.BasicPhaseStatus;
import com.qlangtech.tis.fullbuild.phasestatus.impl.DumpPhaseStatus;
import com.qlangtech.tis.fullbuild.phasestatus.impl.DumpPhaseStatus.TableDumpStatus;
import junit.framework.TestCase;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-04-16 09:58
 **/
public class TestPhaseStatusCollection extends TestCase {
    public void testISComplete() {
        Integer taskId = 999;

        PhaseStatusCollection statusCollection = new PhaseStatusCollection(taskId
                , new ExecutePhaseRange(FullbuildPhase.FullDump, FullbuildPhase.FullDump));

        MockFlush2Local flush2Local = new MockFlush2Local();

        DumpPhaseStatus dumpPhase = new DumpPhaseStatus(taskId, flush2Local);
        TableDumpStatus user = dumpPhase.getTable("user");
        user.setAllRows(999);
        user.setReadRows(998);
        user.setWaiting(false);
        user.setFaild(false);
        user.setComplete(false);
        statusCollection.setDumpPhase(dumpPhase);
        Assert.assertFalse("shall be doing", statusCollection.isComplete());

        for (int i = 0; i < 100; i++) {
            statusCollection.flushStatus2Local();
        }

        user.setReadRows(999);
        user.setComplete(true);
        statusCollection.isComplete();
        Assert.assertTrue("shall have done", statusCollection.isComplete());
        Assert.assertEquals(2, flush2Local.writeCount);


    }

    private static class MockFlush2Local implements IFlush2Local {
        private int writeCount;

        @Override
        public void write(BasicPhaseStatus status) throws Exception {
            this.writeCount++;
        }

        @Override
        public BasicPhaseStatus loadPhase() throws Exception {
            throw new UnsupportedOperationException();
        }
    }
}
