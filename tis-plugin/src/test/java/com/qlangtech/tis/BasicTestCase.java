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
package com.qlangtech.tis;

import com.qlangtech.tis.manage.common.CenterResource;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.util.TestHeteroList;
import junit.framework.TestCase;

public class BasicTestCase extends TestCase {

    @Override
    protected void setUp() throws Exception {
        Config.setDataDir(Config.DEFAULT_DATA_DIR);
        TestHeteroList.setTISField();
        CenterResource.setNotFetchFromCenterRepository();
        HttpUtils.mockConnMaker = new HttpUtils.DefaultMockConnectionMaker();
        HttpUtils.addMockGlobalParametersConfig();
        TIS.clean();
    }
}
