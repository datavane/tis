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
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.datax.ISelectedTab;

import java.util.Collections;
import java.util.List;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-06-16 16:41
 **/
public class ESTableAlias extends IDataxProcessor.TableMap {

    public static final String MAX_READER_TABLE_SELECT_COUNT = "maxReaderTableCount";

    // json 格式
    private String schemaContent;

    @Override
    public List<ISelectedTab.ColMeta> getSourceCols() {
        return Collections.emptyList();
    }

    public JSONObject getSchema() {
        return JSON.parseObject(schemaContent);
    }

    public void setSchemaContent(String schemaContent) {
        this.schemaContent = schemaContent;
    }
}
