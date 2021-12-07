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
package com.qlangtech.tis.solr.common.cloud;

import java.io.IOException;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.cloud.DefaultConnectionStrategy;
import org.apache.solr.common.cloud.SolrZooKeeper;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 加入zk服务器重试连接机制，尝试9000次连接
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ZkRepeatClientConnectionStrategy extends DefaultConnectionStrategy {

    private static final Logger log = LoggerFactory.getLogger(ZkRepeatClientConnectionStrategy.class);

    private static final int RETRY_TIMES = 9000;

    protected SolrZooKeeper createSolrZooKeeper(final String serverAddress, final int zkClientTimeout, final Watcher watcher) throws IOException {
        SolrZooKeeper result;
        int retryCount = 0;
        while (true) {
            try {
                result = new SolrZooKeeper(serverAddress, zkClientTimeout, watcher);
                log.info("connect to zk server " + serverAddress + " session id:" + result.getSessionId());
                return result;
            } catch (Exception e) {
            }
            if (retryCount++ > RETRY_TIMES) {
                // 尝试N次之后结束zk连接
                throw new SolrException(ErrorCode.INVALID_STATE, "connect to zk server:" + serverAddress + " " + retryCount + " times");
            }
            log.error("zk server has some error,this is " + retryCount + " times retry to connect:" + serverAddress);
            try {
                Thread.sleep(zkClientTimeout * 2);
            } catch (InterruptedException e) {
            }
        }
    }
}
