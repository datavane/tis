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
