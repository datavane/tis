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
package com.qlangtech.tis.runtime.module.action;

import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.pubhook.common.RunEnvironment;
import com.qlangtech.tis.test.TISEasyMock;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.easymock.EasyMock;

import java.io.File;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-01-06 18:03
 */
public class TestSysInitializeAction extends TestCase implements TISEasyMock {

//  public void testInitializeAppAndSchema() throws Exception {
//    request.setParameter("emethod", "init");
//    request.setParameter("action", "sys_initialize_action");
//
//    ActionProxy proxy = getActionProxy("/runtime/index_query.ajax");
//    assertNotNull(proxy);
//    SysInitializeAction initAction = (SysInitializeAction) proxy.getAction();
//    assertNotNull(initAction);
//    initAction.initializeAppAndSchema();
//
//  }

  /**
   * 测试系统数据库初始化
   */
  public void testSystemDBInitializWithDerby() throws Exception {

    FileUtils.deleteQuietly(SysInitializeAction.getSysInitializedTokenFile());

    String[] args = new String[]{"/opt/misc/tis-ansible/tis_console_derby.sql", Config.DB_TYPE_DERBY};
    Config config = this.mock("config", Config.class);
    EasyMock.expect(config.getRuntime()).andReturn(RunEnvironment.DAILY.getKeyName()).anyTimes();
    Config.TisDbConfig mockDBType = new Config.TisDbConfig();
    mockDBType.dbtype = Config.DB_TYPE_DERBY;
    mockDBType.dbname = "tis_console_db";
    File dbDir = new File(mockDBType.dbname);
    FileUtils.deleteQuietly(dbDir);
    EasyMock.expect(config.getDbConfig()).andReturn(mockDBType).anyTimes();
    Config.setConfig(config);

    replay();
    SysInitializeAction.main(args);

    File initialSuccessToken = SysInitializeAction.getSysInitializedTokenFile();
    assertTrue("initialSuccessToken fiel:" + initialSuccessToken.getAbsolutePath(), initialSuccessToken.exists());

    verifyAll();
  }

}
