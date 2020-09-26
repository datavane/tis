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
package com.qlangtech.tis.realtime.rpc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
// import org.apache.hadoop.net.NetUtils;
import com.qlangtech.tis.realtime.transfer.TableSingleDataIndexStatus;
import com.qlangtech.tis.realtime.yarn.rpc.IncrStatusUmbilicalProtocol;
import com.qlangtech.tis.realtime.yarn.rpc.LaunchReportInfo;
import com.qlangtech.tis.realtime.yarn.rpc.MasterJob;
import com.qlangtech.tis.realtime.yarn.rpc.TopicInfo;
import com.qlangtech.tis.realtime.yarn.rpc.UpdateCounterMap;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2016年4月7日
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
        // return incrStatusUmbilicalProtocol;
        return null;
    }
}
