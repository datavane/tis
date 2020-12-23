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
package com.qlangtech.tis.realtime.s4employee;

import com.qlangtech.tis.TIS;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.realtime.TisIncrLauncher;
import junit.framework.TestCase;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020-12-23 15:52
 */
public class TestS4employeeResourceDownload extends TestCase {
    String collectionName = "search4employees";
    long wfTimestamp = 20201223151616l;
    static{
        Config.setDataDir("/tmp/tis");
    }

    public void testResourceDownload() throws Exception {

        System.out.println(TIS.permitInitialize);

        final TisIncrLauncher incrLauncher = new TisIncrLauncher(collectionName, wfTimestamp);
        incrLauncher.downloadDependencyJarsAndPlugins();
    }
}
