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
package org.apache.solr.common.cloud;

import java.lang.reflect.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TisSolrZkClient extends SolrZkClient {

    private static final Logger log = LoggerFactory.getLogger(TisSolrZkClient.class);

    private static final Field isClosedField;

    static {
        try {
            isClosedField = SolrZkClient.class.getDeclaredField("isClosed");
            isClosedField.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public TisSolrZkClient(String zkServerAddress, int zkClientTimeout, int clientConnectTimeout, ZkClientConnectionStrategy strat, final OnReconnect onReconnect) {
        super(zkServerAddress, zkClientTimeout, clientConnectTimeout, strat, onReconnect);
    }

    @Override
    void updateKeeper(SolrZooKeeper keeper) throws InterruptedException {
        if (this.isClosed()) {
            throw new RuntimeException("solr zk has been closed");
        }
    // try {
    // if (this.isClosed()) {
    // if (this.getSolrZooKeeper() != null) {
    // log.info("old client has been closed,sessionid:"
    // + this.getSolrZooKeeper().getSessionId()
    // + ",will soon open");
    // }
    // 
    // isClosedField.set(this, false);
    // }
    // } catch (Exception e) {
    // throw new RuntimeException(e);
    // }
    // 
    // super.updateKeeper(keeper);
    }
}
