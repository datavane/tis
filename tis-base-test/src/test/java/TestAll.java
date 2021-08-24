/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 *   This program is free software: you can use, redistribute, and/or modify
 *   it under the terms of the GNU Affero General Public License, version 3
 *   or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

import com.qlangtech.tis.manage.common.TestSnapshotDomainUtils;
import com.qlangtech.tis.plugin.TestPluginUtils;
import com.qlangtech.tis.realtime.s4totalpay.TestS4Totalpay;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-08-20 16:05
 **/
public class TestAll extends TestCase {

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(TestPluginUtils.class);
        suite.addTestSuite(TestSnapshotDomainUtils.class);
        suite.addTestSuite(TestS4Totalpay.class);
        return suite;
    }
}
