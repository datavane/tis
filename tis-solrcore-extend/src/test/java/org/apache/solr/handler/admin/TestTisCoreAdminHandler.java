/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.apache.solr.handler.admin;

import com.qlangtech.tis.cloud.ICoreAdminAction;
import com.qlangtech.tis.test.TISTestCase;
import org.apache.solr.common.params.CommonAdminParams;
import org.apache.solr.common.params.CoreAdminParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrCore;
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


        String coreName = "search4totalpay_shard1_replica_n1";
        final String taskid = "task_1";
        CoreContainer coreContainer = this.mock("coreContainer", CoreContainer.class);
        EasyMock.expect(coreContainer.getAllCoreNames()).andReturn(Collections.singleton(coreName)).anyTimes();

        SolrCore solrCore = mock("solrCore", SolrCore.class);

        EasyMock.expect(coreContainer.getCore(coreName)).andReturn(solrCore).anyTimes();
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
