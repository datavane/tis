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
package com.qlangtech.tis.manage.spring;

import java.util.regex.Matcher;

import com.qlangtech.tis.cloud.ITISCoordinator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.qlangtech.tis.TisZkClient;
import com.qlangtech.tis.manage.common.Config;
import com.qlangtech.tis.pubhook.common.RunEnvironment;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2012-8-15
 */
public class ZooKeeperGetter extends EnvironmentBindService<ITISCoordinator> {

    private static final Log log = LogFactory.getLog(ZooKeeperGetter.class);

    private void validateMultiServerIsReachable(final String zkAddress) {
        Matcher matcher = ZK_ADDRESS.matcher(zkAddress);
        while (matcher.find()) {
            validateServerIsReachable(matcher.group(1));
        }
    }

    @Override
    protected ITISCoordinator createSerivce(final RunEnvironment runtime) {
        // SolrZkClient zookeeper = null;
        final String zkAddress = Config.getZKHost();
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

//    public ITISCoordinator getOnlineTerminatorZooKeeper() {
//        // createSerivce(RunEnvironment.ONLINE);
//        return this.getInstance(RunEnvironment.ONLINE);
//    }
}
