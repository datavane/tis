/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.qlangtech.tis.datax.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.plugin.ds.ISelectedTab;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.plugin.ds.ColumnMetaData;

import java.util.List;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-06-16 16:41
 **/
public class ESTableAlias extends IDataxProcessor.TableMap {
    public static final String KEY_COLUMN = "column";
    public static final String MAX_READER_TABLE_SELECT_COUNT = "maxReaderTableCount";

    // json 格式
    private String schemaContent;

    @Override
    public List<ISelectedTab.ColMeta> getSourceCols() {
        List<ISelectedTab.ColMeta> colsMeta = Lists.newArrayList();
        ISelectedTab.ColMeta colMeta = null;
        JSONArray cols = getSchemaCols();
        JSONObject col = null;
        for (int i = 0; i < cols.size(); i++) {
            col = cols.getJSONObject(i);
            colMeta = new ISelectedTab.ColMeta() {

                @Override
                public ColumnMetaData.DataType getType() {
                    //return super.getType();
                    throw new UnsupportedOperationException();
                }
            };
            colMeta.setName(col.getString("name"));
            colMeta.setPk(col.getBoolean("pk"));
            colsMeta.add(colMeta);
        }
        return colsMeta;
    }

    public JSONObject getSchema() {
        return JSON.parseObject(schemaContent);
    }

    public JSONArray getSchemaCols() {
        JSONObject schema = this.getSchema();
        JSONArray cols = schema.getJSONArray(KEY_COLUMN);
        return cols;
    }

    public byte[] getSchemaByteContent() {
        return schemaContent.getBytes(TisUTF8.get());
    }

    public String getSchemaContent(){
        return this.schemaContent;
    }

    public void setSchemaContent(String schemaContent) {
        this.schemaContent = schemaContent;
    }
}
