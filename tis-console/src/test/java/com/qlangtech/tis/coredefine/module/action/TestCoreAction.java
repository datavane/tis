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
package com.qlangtech.tis.coredefine.module.action;

import com.qlangtech.tis.manage.common.Config;
import junit.framework.TestCase;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-09-03 16:01
 */
public class TestCoreAction extends TestCase {

    public void testGetAllRowsCount() throws Exception {
        String coreURL = "http://192.168.28.200:8080/solr/search4totalpay_shard1_replica_n1/";
        assertTrue(CoreAction.getAllRowsCount(Config.S4TOTALPAY, coreURL) > 0);
    }
}
