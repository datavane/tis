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
package com.qlangtech.tis.solr.common.cloud;

import java.io.IOException;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.cloud.DefaultConnectionStrategy;
import org.apache.solr.common.cloud.SolrZooKeeper;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
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
