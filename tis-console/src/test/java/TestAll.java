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

import com.qlangtech.tis.config.module.action.TestCollectionAction;
import com.qlangtech.tis.coredefine.module.action.TestCoreAction;
import com.qlangtech.tis.coredefine.module.action.TestPluginAction;
import com.qlangtech.tis.coredefine.module.action.TestTISK8sDelegate;
import com.qlangtech.tis.manage.servlet.TestIncrTagHeatBeatMonitor;
import com.qlangtech.tis.runtime.module.action.TestSchemaAction;
import com.qlangtech.tis.runtime.module.action.TestSysInitializeAction;
import com.qlangtech.tis.solrdao.TestSchemaResult;
import com.qlangtech.tis.util.TestPluginItems;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class TestAll extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(TestPluginItems.class);
    suite.addTestSuite(TestIncrTagHeatBeatMonitor.class);
   // suite.addTestSuite(TestTISK8sDelegate.class);
    suite.addTestSuite(TestCoreAction.class);

    suite.addTestSuite(TestSysInitializeAction.class);
    suite.addTestSuite(TestSchemaResult.class);
    suite.addTestSuite(TestSchemaAction.class);
    //suite.addTestSuite(TestCollectionAction.class);
    suite.addTestSuite(TestPluginAction.class);

    return suite;
  }
}
