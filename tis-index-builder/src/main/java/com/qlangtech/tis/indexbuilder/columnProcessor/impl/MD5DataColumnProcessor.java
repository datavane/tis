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
package com.qlangtech.tis.indexbuilder.columnProcessor.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrInputDocument;
import com.google.common.collect.Lists;
import com.qlangtech.tis.indexbuilder.columnProcessor.AdapterExternalDataColumnProcessor;
import com.qlangtech.tis.solrdao.SolrFieldsParser.ParseResult;
import com.qlangtech.tis.solrdao.extend.ProcessorSchemaField;
import com.qlangtech.tis.solrdao.pojo.PSchemaField;

/*
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
