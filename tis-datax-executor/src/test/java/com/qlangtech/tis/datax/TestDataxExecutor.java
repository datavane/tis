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

import com.qlangtech.tis.test.TISTestCase;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-04-20 14:12
 */
public class TestDataxExecutor extends TISTestCase {

    public void testDataxJobLaunch() throws Exception {
        String dataXName = "baisuitest";
        DataxExecutor executor = new DataxExecutor();
        Path path = Paths.get("/opt/data/tis/cfg_repo/tis_plugin_config/ap/baisuitest/dataxCfg/order_promotion_0.json");

        executor.startWork(dataXName, path.toString());
    }
}
