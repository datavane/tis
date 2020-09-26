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
package com.qlangtech.tis.full.dump;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;
// import org.apache.hadoop.ipc.RPC;
import com.qlangtech.tis.realtime.yarn.rpc.IncrStatusUmbilicalProtocol;
import com.qlangtech.tis.realtime.yarn.rpc.UpdateCounterMap;
import junit.framework.TestCase;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年6月6日
 */
public class TestClientIncrStatusUmbilicalProtocol extends TestCase {

    private IncrStatusUmbilicalProtocol incrStatusUmbilicalProtocol;

    public void testClient() throws Exception {
        final AtomicInteger i = new AtomicInteger();
        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                while (true) {
                    if (incrStatusUmbilicalProtocol != null) {
                        UpdateCounterMap m = new UpdateCounterMap();
                        incrStatusUmbilicalProtocol.reportStatus(m);
                        System.out.println("send times:" + i.incrementAndGet() + ",instacne:");
                    }
                    try {
                        Thread.sleep(400);
                    } catch (InterruptedException e) {
                    }
                }
            }
        };
        Thread t = new Thread(runnable);
        t.setDaemon(false);
        t.start();
        startClient(0);
    }

    protected void startClient(int incrPort) throws Exception {
    // final int port = TestServerIncrStatusUmbilicalProtocol.INIT_PORT + incrPort;
    // InetSocketAddress address = new InetSocketAddress("127.0.0.1", port);
    // this.incrStatusUmbilicalProtocol = (IncrStatusUmbilicalProtocol) RPC.getProxy(IncrStatusUmbilicalProtocol.class, IncrStatusUmbilicalProtocol.versionID, address, new Configuration());
    // System.out.println("connect server port:" + port);
    // Thread.sleep(60 * 1000);
    // startClient(++incrPort);
    }
}
