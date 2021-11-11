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

package com.qlangtech.tis.datax;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.datax.impl.DataxWriter;
import com.qlangtech.tis.plugin.ds.ISelectedTab;
import com.qlangtech.tis.solrdao.ISchema;
import com.qlangtech.tis.solrdao.SchemaMetaContent;
import com.qlangtech.tis.util.IPluginContext;
import org.apache.commons.lang.StringUtils;

import java.util.function.Consumer;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-06-13 18:00
 **/
public interface ISearchEngineTypeTransfer {

    public static ISearchEngineTypeTransfer load(IPluginContext context, String dataXName) {
        DataxWriter dataxWriter = DataxWriter.load(context, dataXName);
        if (!(dataxWriter instanceof ISearchEngineTypeTransfer)) {
            throw new IllegalStateException("dataxWriter must be type of " + ISearchEngineTypeTransfer.class.getSimpleName()
                    + " but now is " + dataxWriter.getClass().getName());
        }
        return (ISearchEngineTypeTransfer) dataxWriter;
    }

    static JSONObject getOriginExpertSchema(String schemaXmlContent) {
        return JSON.parseObject(StringUtils.defaultIfEmpty(schemaXmlContent, "{\"column\":[]}"));
    }


    //public VisualType mapSearchEngineType(ISelectedTab.DataXReaderColType type);

    /**
     * 初始化Schema内容
     *
     * @param tab
     * @return
     */
    public SchemaMetaContent initSchemaMetaContent(ISelectedTab tab);

    public ISchema projectionFromExpertModel(IDataxProcessor.TableAlias tableAlias, Consumer<byte[]> schemaContentConsumer);

    public ISchema projectionFromExpertModel(JSONObject body);

    public JSONObject mergeFromStupidModel(ISchema schema, JSONObject expertSchema);
}
