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
package com.qlangtech.tis.indexbuilder.doc;

import org.apache.solr.common.SolrInputDocument;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/09/25
 */
public class SolrDocPack {

    public static final int BUFFER_PACK_SIZE = 100;

    private final SolrInputDocument[] docs = new SolrInputDocument[BUFFER_PACK_SIZE];

    int index = -1;

    public SolrInputDocument getDoc(int index) {
        return this.docs[index];
    }

    public boolean isNotEmpty() {
        return this.index >= 0;
    }

    public int getCurrentIndex() {
        return this.index;
    }

    /**
     * @param doc
     * @return full 放满了
     */
    public boolean add(SolrInputDocument doc) {
        this.docs[++index] = doc;
        return (index + 1) >= BUFFER_PACK_SIZE;
    }
}
