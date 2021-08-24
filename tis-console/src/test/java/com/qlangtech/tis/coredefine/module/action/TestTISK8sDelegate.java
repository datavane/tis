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
package com.qlangtech.tis.coredefine.module.action;

import com.qlangtech.tis.manage.common.CenterResource;
import com.qlangtech.tis.manage.common.HttpUtils;
import com.qlangtech.tis.plugin.PluginStubUtils;
import com.qlangtech.tis.trigger.jst.ILogListener;
import junit.framework.TestCase;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @create: 2020-09-01 15:06
 */
public class TestTISK8sDelegate extends TestCase {

  private static final String totalpay = "search4totalpay";

  @Override
  public void setUp() throws Exception {
    super.setUp();
//    PluginStubUtils.setDataDir("/tmp/tis");
    CenterResource.setNotFetchFromCenterRepository();
//    HttpUtils.mockConnMaker = new HttpUtils.DefaultMockConnectionMaker();
//    PluginStubUtils.stubPluginConfig();
  }

//  /**
//   * 前提要保证pod实例启动
//   */
//  public void testMessageListener() throws Exception {
//    TISK8sDelegate k8SDelegate = TISK8sDelegate.getK8SDelegate(totalpay);
//    System.out.println("get k8SDelegate instance");
//    AtomicInteger msgReceiveCount = new AtomicInteger();
//    RcDeployment rc = k8SDelegate.getRcConfig(true);
//    assertTrue("pod size must big than 1", rc.getPods().size() > 0);
//    RcDeployment.PodStatus podStatus = rc.getPods().get(0);
//    k8SDelegate.listPodsAndWatchLog(podStatus.getName(), new ILogListener() {
//
//      @Override
//      public void sendMsg2Client(Object biz) throws IOException {
//        System.out.println("flushCount:" + msgReceiveCount.incrementAndGet());
//      }
//
//      @Override
//      public void read(Object event) {
//      }
//
//      @Override
//      public boolean isClosed() {
//        return false;
//      }
//    });
//    System.out.println("start listPodsAndWatchLog");
//    Thread.sleep(60000);
//    assertTrue(msgReceiveCount.get() > 0);
//  }
}
