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
package com.qlangtech.tis.indexbuilder.columnProcessor.impl;

import com.google.common.collect.Lists;
import com.qlangtech.tis.indexbuilder.columnProcessor.AdapterExternalDataColumnProcessor;
import com.qlangtech.tis.solrdao.impl.ParseResult;
import com.qlangtech.tis.solrdao.extend.ProcessorSchemaField;
import com.qlangtech.tis.solrdao.pojo.PSchemaField;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrInputDocument;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 将原始数据中的几列通过md5生成一个新列
 * @time 2017年8月9日下午7:24:32
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class MD5DataColumnProcessor extends AdapterExternalDataColumnProcessor {

    public static final String NAME = "md5";

    private final List<String> md5Cols;

    private final String dest;

    public MD5DataColumnProcessor(ProcessorSchemaField processorMap, ParseResult schemaParseResult) {
        this.md5Cols = Lists.newArrayList(StringUtils.split(processorMap.getParam("from"), ","));
        Set<String> fields = new HashSet<String>();
        for (PSchemaField f : schemaParseResult.dFields) {
            fields.add(f.getName());
        }
        for (String col : this.md5Cols) {
            if (!fields.contains(col)) {
                throw new IllegalStateException("col:" + col + " is not defined in the schema");
            }
        }
        this.dest = processorMap.getParam("dest");
        if (this.md5Cols.size() < 1) {
            throw new IllegalArgumentException("please set param 'from'");
        }
        if (StringUtils.isBlank(this.dest)) {
            throw new IllegalArgumentException("please set param 'dest'");
        }
    }

    @Override
    public void process(SolrInputDocument doc, Map<String, String> entry) {
        StringBuffer buffer = new StringBuffer();
        for (String column : md5Cols) {
            buffer.append(entry.get(column));
        }
        doc.setField(dest, DigestUtils.md5Hex(buffer.toString()));
    }

    @Override
    public String getName() {
        return NAME;
    }
}
