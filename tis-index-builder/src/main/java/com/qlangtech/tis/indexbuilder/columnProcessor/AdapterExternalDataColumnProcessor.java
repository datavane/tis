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
