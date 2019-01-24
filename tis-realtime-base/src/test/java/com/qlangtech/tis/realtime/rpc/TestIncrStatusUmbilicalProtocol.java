/* 
 * The MIT License
 *
 * Copyright (c) 2018-2022, qinglangtech Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.qlangtech.tis.realtime.rpc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;
import org.apache.hadoop.net.NetUtils;
import com.qlangtech.tis.realtime.transfer.TableSingleDataIndexStatus;
import com.qlangtech.tis.realtime.yarn.rpc.IncrStatusUmbilicalProtocol;
import com.qlangtech.tis.realtime.yarn.rpc.LaunchReportInfo;
import com.qlangtech.tis.realtime.yarn.rpc.MasterJob;
import com.qlangtech.tis.realtime.yarn.rpc.TopicInfo;
import com.qlangtech.tis.realtime.yarn.rpc.UpdateCounterMap;
import junit.framework.Assert;
import junit.framework.TestCase;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TestIncrStatusUmbilicalProtocol extends TestCase {

    public void testTopicsInfo() throws Exception {
        ConcurrentHashMap<String, String> /* indexName */
        map = new ConcurrentHashMap<>();
        map.put("hello", "dd");
        map.replace("hello", "baisui");
        System.out.println(map.get("hello"));
        IncrStatusUmbilicalProtocol client = createRPCClient();
        Map<String, TopicInfo> /* collection */
        collectionFocusTopicInfo = new HashMap<>();
        TopicInfo topic = new TopicInfo();
        topic.addTag("hello-topic", "hello-tagggKKKKKKKKKKK");
        collectionFocusTopicInfo.put("search4totalpay", topic);
        LaunchReportInfo launchReportInfo = new LaunchReportInfo(collectionFocusTopicInfo);
        client.nodeLaunchReport(launchReportInfo);
    }

    public void testSend() throws Exception {
        IncrStatusUmbilicalProtocol incrStatusUmbilicalProtocol = createRPCClient();
        UpdateCounterMap data = new UpdateCounterMap();
        // HashMap<String, Map<String, Integer>> map = new HashMap<>();
        Map<String, Integer> tableCounter = new HashMap<>();
        tableCounter.put("order", 1);
        tableCounter.put("menu", 99999);
        TableSingleDataIndexStatus tableUpdateCounter = new TableSingleDataIndexStatus();
        tableUpdateCounter.setBufferQueueRemainingCapacity(999);
        tableUpdateCounter.setBufferQueueUsedSize(888);
        tableUpdateCounter.setConsumeErrorCount(777);
        tableUpdateCounter.setIgnoreRowsCount(123);
        // tableUpdateCounter.put("order", new IncrCounter(1));
        // tableUpdateCounter.put("menu", new IncrCounter(99999));
        data.addTableCounter("search4xxxx", tableUpdateCounter);
        /*
		 * data.addTableCounter("search4totalpay", tableCounter);
		 * data.addTableCounter("search4menu", tableCounter);
		 * data.addTableCounter("search4kkk", tableCounter);
		 * data.addTableCounter("search4xxxx", tableCounter);
		 */
        long start = System.currentTimeMillis();
        // for (int i = 0; i < 10; i++) {
        MasterJob job = incrStatusUmbilicalProtocol.reportStatus(data);
        Assert.assertNotNull(job);
        System.out.println(job.getIndexName());
        System.out.println(job.getJobType());
        System.out.println(job.isStop());
        // }
        System.out.println("consume:" + (System.currentTimeMillis() - start));
    }

    private IncrStatusUmbilicalProtocol createRPCClient() throws IOException {
        final InetSocketAddress address = NetUtils.createSocketAddrForHost("10.1.21.48", 42419);
        IncrStatusUmbilicalProtocol incrStatusUmbilicalProtocol = (IncrStatusUmbilicalProtocol) RPC.getProxy(IncrStatusUmbilicalProtocol.class, IncrStatusUmbilicalProtocol.versionID, address, new Configuration());
        return incrStatusUmbilicalProtocol;
    }
}
