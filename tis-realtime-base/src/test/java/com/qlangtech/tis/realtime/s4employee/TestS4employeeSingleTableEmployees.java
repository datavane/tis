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

import com.qlangtech.tis.realtime.TisIncrLauncher;
import com.qlangtech.tis.spring.LauncherResourceUtils;
import junit.framework.TestCase;

/**
 * 监听employee 通过TiDB TiCDC kafka这条通道的数据
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020-12-25 13:32
 */
public class TestS4employeeSingleTableEmployees extends TestCase {


    public void testReceiveMQ() throws Exception {

        LauncherResourceUtils.resourceFilter = (r) -> false;

        String[] args = new String[]{"search4employees", "20201225113828"};
        TisIncrLauncher.main(args);

        //java -Ddata.dir=/opt/data -Denv_props=true -Dlog.dir=/opt/logs -Druntime=daily -Djava.security.egd=file:/dev/./urandom -jar /opt/tis/tis-incr/tis-incr.jar search4employees 20201225113828
        synchronized (this) {
            this.wait();
        }
    }

}
