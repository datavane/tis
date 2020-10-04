package com.qlangtech.tis.collectinfo;

import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.manage.common.HttpUtils;
import junit.framework.TestCase;
import org.apache.solr.common.cloud.Replica;
import org.apache.solr.common.cloud.Slice;
import org.apache.solr.common.cloud.SolrZkClient;
import org.easymock.EasyMock;

import java.util.Collection;
import java.util.Collections;

/**
 * @author: baisui 百岁
 * @create: 2020-10-04 15:18
 **/
public class TestCoreStatisticsReport extends TestCase {

    public void testAddClusterCoreInfo() {
        // int order, String testStr, String classpath, Class<?> clazz
        HttpUtils.addMockApply(0 //
                , CoreStatisticsReport.GET_METRIX_PATH
                , "search4totalpay_query_update_metrix.xml"
                , TestCoreStatisticsReport.class);

        //String collectionName = "search4totalpay";
        //Slice slice, SolrZkClient zookeeper

        Slice slice = EasyMock.createMock("slice", Slice.class);
        EasyMock.expect(slice.getName()).andReturn("shard1");
        Replica replica = EasyMock.createMock("replica", Replica.class);

        EasyMock.expect(replica.getStr(Slice.LEADER)).andReturn("true");
        EasyMock.expect(replica.getCoreUrl()).andReturn("http://192.168.28.200:8080/solr/search4totalpay_shard1_replica_n1/");
        EasyMock.expect(replica.getNodeName()).andReturn("http://192.168.28.200:8080/solr");
        Collection<Replica> replicas = Collections.singleton(replica);
        EasyMock.expect(slice.getReplicas()).andReturn(replicas).times(2);
        SolrZkClient zookeeper = EasyMock.createMock("solrZkClient", SolrZkClient.class);


        EasyMock.replay(slice, zookeeper, replica);
        CoreStatisticsReport statisticsReport = new CoreStatisticsReport(Config.S4TOTALPAY, zookeeper);

        assertTrue(statisticsReport.addClusterCoreInfo(slice));

        assertEquals(1683, statisticsReport.requestCount.getCount());
        assertEquals(9527, statisticsReport.updateCount.getCount());
        assertEquals(23360, statisticsReport.numDocs.get());


        assertEquals(9999, statisticsReport.updateErrorCount.getCount());
        assertEquals(22222, statisticsReport.requestErrorCount.getCount());

        EasyMock.verify(slice, zookeeper, replica);
    }

}
