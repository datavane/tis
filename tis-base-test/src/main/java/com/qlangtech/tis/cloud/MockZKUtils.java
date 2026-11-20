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
package com.qlangtech.tis.cloud;

import com.google.common.collect.Lists;
import com.qlangtech.tis.common.utils.Assert;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.solrj.util.ZkUtils;
import com.qlangtech.tis.test.EasyMockUtil;
import org.apache.commons.io.IOUtils;
import org.mockito.Mockito;

import java.io.InputStream;
import java.util.List;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-03-03 15:34
 */
public class MockZKUtils {

    public static ITISCoordinator createZkMock() throws Exception {
        ITISCoordinator zkCoordinator = EasyMockUtil.mock("zkCoordinator", ITISCoordinator.class);
        Mockito.when(zkCoordinator.shallConnect2RemoteIncrStatusServer()).thenReturn(false);
        Mockito.when(zkCoordinator.unwrap()).thenReturn(zkCoordinator);
        createAssembleLogCollectPathMock(zkCoordinator);

        try (InputStream input = MockZKUtils.class.getResourceAsStream("overseer_elect_leader.json")) {
            Assert.assertNotNull(input);
            Mockito.when(zkCoordinator.getData(ZkUtils.ZK_PATH_OVERSEER_ELECT_LEADER, true))
                    .thenReturn(IOUtils.toByteArray(input));
        }

        return zkCoordinator;
    }

    private static void createAssembleLogCollectPathMock(ITISCoordinator zkCoordinator) {
        createAssembleLogCollectPathMock(zkCoordinator, 1);
    }

    private static void createAssembleLogCollectPathMock(ITISCoordinator zkCoordinator, int times) {
        String childPath = "nodes0000000361";
        String childPathContent = "192.168.28.200:38293";
        List<String> incrStatecollectList = Lists.newArrayList(childPath);
        Mockito.when(zkCoordinator.getChildren(ZkUtils.ZK_ASSEMBLE_LOG_COLLECT_PATH, true)).thenReturn(incrStatecollectList);
        Mockito.when(zkCoordinator.getData(ZkUtils.ZK_ASSEMBLE_LOG_COLLECT_PATH + "/" + childPath, true))
                .thenReturn(childPathContent.getBytes(TisUTF8.get()));
    }

}
