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
import com.qlangtech.tis.order.center.IParamContext;
import com.qlangtech.tis.test.TISEasyMock;
import junit.framework.TestCase;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-09-02 20:33
 **/
public class TestDefaultChainContext extends TestCase implements TISEasyMock {

    private static final String dataXname = "dataXName";

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testLoadPhaseStatusFromLatest() {
        IParamContext paramContext = this.mock("paramContext", IParamContext.class);
        DefaultChainContext chainContext = new DefaultChainContext(paramContext);

        PhaseStatusCollection statusCollection = chainContext.loadPhaseStatusFromLatest(dataXname);
        assertNotNull(statusCollection);

        DumpPhaseStatus dumpPhase = statusCollection.getDumpPhase();
        assertNotNull(dumpPhase);
    }
}
