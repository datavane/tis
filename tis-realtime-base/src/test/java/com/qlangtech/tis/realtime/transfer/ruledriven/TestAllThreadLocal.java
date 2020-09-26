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
package com.qlangtech.tis.realtime.transfer.ruledriven;

import com.qlangtech.tis.realtime.transfer.IPk;
import com.qlangtech.tis.realtime.transfer.impl.DefaultPk;
import junit.framework.TestCase;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TestAllThreadLocal extends TestCase {

    public void testGet() {
        AllThreadLocal.DefaultThreadLocal<String> tl = AllThreadLocal.addThreadLocalVal();
        String cacheVal = "hello1";
        tl.set(cacheVal);
        assertEquals(cacheVal, tl.get());
        AllThreadLocal.cleanAllThreadLocalVal();
        assertNull(tl.get());
    }

    public void testPKGet() {
        String pkVal = "123";
        DefaultPk dftPk = new DefaultPk(pkVal);
        AllThreadLocal.pkThreadLocal.set(dftPk);
        IPk storedPk = AllThreadLocal.getPkThreadLocal();
        assertNotNull(storedPk);
        assertNotNull(pkVal, storedPk.getValue());
    }
}
