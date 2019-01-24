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
package com.qlangtech.tis.manage.spring;

import java.util.regex.Matcher;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.qlangtech.tis.TisZkClient;
import com.qlangtech.tis.common.utils.TSearcherConfigFetcher;
import com.qlangtech.tis.pubhook.common.RunEnvironment;

/* *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class ZooKeeperGetter extends EnvironmentBindService<TisZkClient> {

    private static final Log log = LogFactory.getLog(ZooKeeperGetter.class);

    private void validateMultiServerIsReachable(final String zkAddress) {
        Matcher matcher = ZK_ADDRESS.matcher(zkAddress);
        while (matcher.find()) {
            validateServerIsReachable(matcher.group(1));
        }
    }

    @Override
    protected TisZkClient createSerivce(final RunEnvironment runtime) {
        // SolrZkClient zookeeper = null;
        final String zkAddress = TSearcherConfigFetcher.get().getZkAddress();
        validateMultiServerIsReachable(zkAddress);
        try {
            log.debug("runtime:" + runtime + ", address:" + zkAddress + " rmi server connection has been established");
            // try {
            final TisZkClient target = new TisZkClient(zkAddress, 30000);
            return target;
        } catch (Exception e) {
            // }
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
