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
package com.qlangtech.tis.datax;

import junit.framework.TestCase;

import java.io.File;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-20 14:12
 */
public class TestDataxExecutor extends TestCase {
    public void testDataxJobLaunch() throws Exception {
        DataxExecutor dataxExecutor = new DataxExecutor();
        File jobPath = new File("");
        dataxExecutor.startWork("baisuitest", jobPath.toString());
    }
}
