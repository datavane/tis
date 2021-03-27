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
package org.apache.solr.handler.admin;

import com.qlangtech.tis.cloud.ICoreAdminAction;
import com.qlangtech.tis.test.TISTestCase;
import org.apache.solr.common.params.CommonAdminParams;
import org.apache.solr.common.params.CoreAdminParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.easymock.EasyMock;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TODO:需要继续完善
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-03-27 12:53
 */
public class TestTisCoreAdminHandler extends TISTestCase {

    public void testhandleSwapindexfileAction() throws Exception {

//        /admin/cores?action=CREATEALIAS&execaction="
//                + ICoreAdminAction.ACTION_SWAP_INDEX_FILE + "&core=" + replica.getStr(CORE_NAME_PROP)
//                + "&property.hdfs_timestamp=" + timestamp + "&property.hdfs_user=admin&" + CommonAdminParams.ASYNC + "=" + requestId);
        String coreName = "search4totalpay_shard1_replica_n1";
        final String taskid = "task_1";
        CoreContainer coreContainer = this.mock("coreContainer", CoreContainer.class);
        EasyMock.expect(coreContainer.getAllCoreNames()).andReturn(Collections.singleton(coreName)).anyTimes();
        AtomicBoolean execHandleSwapindexfileActionComplete = new AtomicBoolean();
        CountDownLatch countDown = new CountDownLatch(1);
        TisCoreAdminHandler coreAdminHandler = new TisCoreAdminHandler(coreContainer) {
            @Override
            protected void handleSwapindexfileAction(TaskObject taskObject, SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
                try {
                    super.handleSwapindexfileAction(taskObject, req, rsp);
                    execHandleSwapindexfileActionComplete.set(true);
                } catch (Throwable e) {
                    e.printStackTrace();
                    throw e;
                } finally {
                    countDown.countDown();
                }
            }
        };

        SolrQueryRequest req = mock("solrQueryRequest", SolrQueryRequest.class);
        ModifiableSolrParams solrParams = new ModifiableSolrParams();
        solrParams.set(ICoreAdminAction.EXEC_ACTION, ICoreAdminAction.ACTION_SWAP_INDEX_FILE);
        solrParams.set(CommonAdminParams.ASYNC, taskid);
        solrParams.set(CoreAdminParams.ACTION, "CREATEALIAS");
        solrParams.set(CoreAdminParams.CORE, coreName);
        EasyMock.expect(req.getParams()).andReturn(solrParams).anyTimes();
        SolrQueryResponse rsp = mock("solrQueryResponse", SolrQueryResponse.class);
        rsp.setHttpCaching(false);
        replay();
        coreAdminHandler.handleRequestBody(req, rsp);
        if (!countDown.await(10, TimeUnit.SECONDS)) {
            fail("wait expire");
        }
        verifyAll();
        assertTrue("execHandleSwapindexfileActionComplete", execHandleSwapindexfileActionComplete.get());
    }
}
