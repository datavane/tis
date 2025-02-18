/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qlangtech.tis.datax.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.plugin.ds.CMeta;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-06-16 16:41
 **/
public class ESTableAlias extends IDataxProcessor.TableMap {
    public static final String KEY_COLUMN = "column";
    public static final String MAX_READER_TABLE_SELECT_COUNT = "maxReaderTableCount";

    // json 格式
    private final String schemaContent;

    public ESTableAlias(String schemaContent) {
        super(parseSourceCols(schemaContent));
        this.schemaContent = schemaContent;
    }

    @Override
    protected List<CMeta> rewriteCols(final List<CMeta> cmetas) {
        return cmetas;
    }

    @Override
    public List<String> getPrimaryKeys() {
        return getSourceCols().stream()
                .filter((col) -> col.isPk())
                .map((col) -> col.getName())
                .collect(Collectors.toList());
    }

    // private List<CMeta> colsMeta;

    // @Override
    private static List<CMeta> parseSourceCols(String schemaContent) {

        // if (colsMeta == null) {
        List<CMeta> colsMeta = Lists.newArrayList();
        CMeta colMeta = null;
        JSONArray cols = getSchemaCols(schemaContent);
        JSONObject col = null;
        for (int i = 0; i < cols.size(); i++) {
            col = cols.getJSONObject(i);
            colMeta = new CMeta();
            colMeta.setName(col.getString("name"));
            colMeta.setPk(col.getBoolean("pk"));
            colsMeta.add(colMeta);
        }
        // }

        return colsMeta;
    }

    private static JSONObject getSchema(String schemaContent) {
        if (StringUtils.isEmpty(schemaContent)) {
            throw new IllegalStateException("schemaContent can not be empty");
        }
        return JSON.parseObject(schemaContent);
    }

    public static JSONArray getSchemaCols(String schemaContent) {
        JSONObject schema = getSchema(schemaContent);
        JSONArray cols = schema.getJSONArray(KEY_COLUMN);
        return cols;
    }

    public byte[] getSchemaByteContent() {
        return schemaContent.getBytes(TisUTF8.get());
    }

    public String getSchemaContent() {
        return this.schemaContent;
    }

//    public void setSchemaContent(String schemaContent) {
//        this.schemaContent = schemaContent;
//    }
}
