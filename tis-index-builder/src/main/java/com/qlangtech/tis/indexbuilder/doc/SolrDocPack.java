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
