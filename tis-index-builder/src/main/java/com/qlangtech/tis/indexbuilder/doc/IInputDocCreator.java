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

import com.qlangtech.tis.indexbuilder.source.SourceReader;
import org.apache.solr.common.SolrInputDocument;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public interface IInputDocCreator {

    /**
     * 数据流中每次构建生成一个InputDocument对象实体
     *
     * @param recordReader
     * @return
     * @throws Exception
     */
    public SolrInputDocument createSolrInputDocument(SourceReader recordReader) throws Exception;
}
