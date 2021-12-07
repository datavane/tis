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
package com.qlangtech.tis.indexbuilder.columnProcessor;

import org.apache.solr.common.SolrInputDocument;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public abstract class AdapterExternalDataColumnProcessor extends ExternalDataColumnProcessor {

    @Override
    public void process(SolrInputDocument doc, Entry<String, String> entry) {
        throw new UnsupportedOperationException();
    }

    public void process(SolrInputDocument doc, Map<String, String> entry) {
        // 必须被覆写了才能用
        throw new UnsupportedOperationException();
    }
}
