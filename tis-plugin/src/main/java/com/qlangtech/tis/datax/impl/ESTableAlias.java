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
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.datax.SelectedTab;
import com.qlangtech.tis.plugin.datax.transformer.OutputParameter;
import com.qlangtech.tis.plugin.datax.transformer.RecordTransformerRules;
import com.qlangtech.tis.plugin.ds.CMeta;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-06-16 16:41
 **/
public class ESTableAlias extends SelectedTab {
    public static final String KEY_COLUMN = "column";
    public static final String MAX_READER_TABLE_SELECT_COUNT = "maxReaderTableCount";

    // json 格式
    @FormField(ordinal = 9999, advance = true, type = FormFieldType.TEXTAREA, validate = {})
    public String schemaContent;
    // private final List<String> pks;

    public static ESTableAlias create(Optional<RecordTransformerRules> transformerRules, String schemaContent) {
        ESTableAlias esTableAlias = new ESTableAlias();
        esTableAlias.schemaContent = schemaContent;
        esTableAlias.cols = ESTableAlias.parseSourceCols(schemaContent);
        transformerRules.ifPresent((rule) -> {
            // 需要将虚拟列过滤掉
            Set<String> virtualCols =
                    rule.relevantTypedOutterColKeys().stream().filter(OutputParameter::isVirtual).map(OutputParameter::getName).collect(Collectors.toSet());
            esTableAlias.cols =
                    esTableAlias.cols.stream().filter((col) -> !virtualCols.contains(col.getName())).collect(Collectors.toList());
        });

        esTableAlias.primaryKeys =
                esTableAlias.cols.stream().filter(CMeta::isPk).map(CMeta::getName).collect(Collectors.toList());
        return esTableAlias;
    }

    //    public ESTableAlias() {
    //
    //    }

    //    public ESTableAlias(String schemaContent) {
    //        super("esTabName");
    //        this.cols = parseSourceCols(schemaContent);
    //        this.primaryKeys = this.cols.stream().filter(CMeta::isPk).map(CMeta::getName).collect(Collectors.toList
    //        ());
    //        this.schemaContent = schemaContent;
    //    }

    public static final DefaultDescriptor desc = new DefaultDescriptor();

    @Override
    public final Descriptor<SelectedTab> getDescriptor() {
        return desc;
    }

    //  @Override
    protected List<CMeta> rewriteCols(final List<CMeta> cmetas) {
        return cmetas;
    }

    // private List<CMeta> colsMeta;

    // @Override
    public static List<CMeta> parseSourceCols(String schemaContent) {

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


    @TISExtension
    public static class DefaultDescriptor extends SelectedTab.DefaultDescriptor {
        public DefaultDescriptor() {
            super();
        }
    }
}
