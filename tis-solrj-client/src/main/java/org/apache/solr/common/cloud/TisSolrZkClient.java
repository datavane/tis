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
package org.apache.solr.common.cloud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class TisSolrZkClient extends SolrZkClient {

    private static final Logger log = LoggerFactory.getLogger(TisSolrZkClient.class);

    public TisSolrZkClient(String zkServerAddress, int zkClientTimeout, int clientConnectTimeout, ZkClientConnectionStrategy strat, final OnReconnect onReconnect) {
        super(zkServerAddress, zkClientTimeout, clientConnectTimeout, strat, onReconnect);
    }
}
