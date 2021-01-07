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

import com.opensymphony.xwork2.ActionProxy;
import com.qlangtech.tis.BasicActionTestCase;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-01-06 18:03
 */
public class TestSysInitializeAction extends BasicActionTestCase {

  public void testInitializeAppAndSchema() throws Exception {
    request.setParameter("emethod", "init");
    request.setParameter("action", "sys_initialize_action");

    ActionProxy proxy = getActionProxy("/runtime/index_query.ajax");
    assertNotNull(proxy);
    SysInitializeAction initAction = (SysInitializeAction) proxy.getAction();
    assertNotNull(initAction);
    initAction.initializeAppAndSchema();

  }

}
