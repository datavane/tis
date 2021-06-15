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

package com.qlangtech.tis.solrdao;

import com.qlangtech.tis.manage.common.TisUTF8;
import org.apache.commons.lang.StringUtils;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-06-15 09:53
 **/
public class SchemaMetaContent {
    public byte[] content;


    public ISchema parseResult;

    public ISchema getParseResult() {
        return this.parseResult;
    }

    protected void appendExtraProps(com.alibaba.fastjson.JSONObject schema) {
    }


    /**
     * 取得普通模式多字段
     *
     * @throws Exception
     */
    public com.alibaba.fastjson.JSONObject toJSON() {
        SchemaMetaContent result = this;
        // ISchema parseResult = result.parseResult;
        ISchema parseResult = result.parseResult;
        final com.alibaba.fastjson.JSONObject schema = new com.alibaba.fastjson.JSONObject();
        this.appendExtraProps(schema);

        // 设置原生schema的内容
        if (result.content != null) {
            schema.put("schemaXmlContent", new String(result.content, TisUTF8.get()));
        }
        String sharedKey = StringUtils.trimToEmpty(parseResult.getSharedKey());
        String pk = StringUtils.trimToEmpty(parseResult.getUniqueKey());
        schema.put("shareKey", sharedKey);
        schema.put("uniqueKey", pk);
        com.alibaba.fastjson.JSONArray fields = new com.alibaba.fastjson.JSONArray();
        com.alibaba.fastjson.JSONObject f = null;
        int id = 0;
        // String type = null;

        for (ISchemaField field : parseResult.getSchemaFields()) {

            // for (PSchemaField field : parseResult.dFields) {
            f = new com.alibaba.fastjson.JSONObject();
            // 用于标示field 頁面操作過程中不能變
            // 0 开始
            f.put("id", id++);
            // 用于表示UI上的行号
            // 1 开始
            f.put("index", id);
            // f.put("uniqueKey", id++);
            f.put("sharedKey", StringUtils.equals(field.getName(), sharedKey));
            f.put("uniqueKey", StringUtils.equals(field.getName(), pk));
            f.put("name", field.getName());
            // f.put("inputDisabled", field.inputDisabled);
            // f.put("rangequery", false);
            f.put("defaultVal", StringUtils.trimToNull(field.getDefaultValue()));
            //f.put("fieldtype", field.getTisFieldType());

            field.serialVisualType2Json(f);

//            if (field.getType() != null) {
//                serialVisualType2Json(f, field.getType());
//            } else {
//                throw new IllegalStateException("field:" + field.getName() + " 's fieldType is can not be null");
//            }
            f.put("docval", field.isDocValue());
            f.put("indexed", field.isIndexed());
            f.put("multiValue", field.isMultiValue());
            f.put("required", field.isRequired());
            f.put("stored", field.isStored());
            fields.add(f);
        }
        schema.put("fields", fields);


        schema.put("fieldtypes", parseResult.serialTypes());
        // this.setBizResult(context, schema);
        return schema;
    }
}
