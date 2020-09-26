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
package com.qlangtech.tis.realtime.test.client;

import com.qlangtech.tis.cloud.CloudServerException;
import com.qlangtech.tis.cloud.ICloudInputDocument;
import com.qlangtech.tis.cloud.ITISCoordinator;
import com.qlangtech.tis.cloud.ITisCloudClient;
import com.google.common.collect.Maps;
import org.apache.solr.common.SolrDocument;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class MockTisCloudClient implements ITisCloudClient {

    public static Map<String, ExpectStatus> /*pk*/
    expectStatus = Maps.newHashMap();

    public static void expectGetDocById(String pk, SolrDocument doc) {
        expectStatus.put(pk, new ExpectStatus(doc));
    }

    public static boolean validateExpect() {
        for (Map.Entry<String, ExpectStatus> entry : expectStatus.entrySet()) {
            if (entry.getValue().count < 1) {
                throw new IllegalStateException("key:" + entry.getKey() + " relevant doc have not be use");
            }
        }
        return true;
    }

    @Override
    public void add(String collection, ICloudInputDocument doc, long timeVersion) throws CloudServerException {
    }

    @Override
    public SolrDocument getDocById(String collection, String pk, String shareId) throws CloudServerException {
        ExpectStatus expectStatus = MockTisCloudClient.expectStatus.get(pk);
        if (expectStatus == null) {
            throw new IllegalStateException("pk:" + pk + " have not be defined");
        }
        expectStatus.count++;
        return expectStatus.doc;
    }

    @Override
    public ITISCoordinator getCoordinator() {
        return new MockCoordinator();
    }

    private static class ExpectStatus {

        private int count;

        private final SolrDocument doc;

        public ExpectStatus(SolrDocument doc) {
            this.doc = doc;
        }
    }
}
