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

import com.alibaba.citrus.turbine.Context;
import com.alibaba.fastjson.JSONObject;
import com.qlangtech.tis.exec.IIndexMetaData;
import com.qlangtech.tis.manage.common.TisUTF8;
import com.qlangtech.tis.plugin.ds.ReflectSchemaFieldType;
import com.qlangtech.tis.runtime.module.misc.IMessageHandler;
import com.qlangtech.tis.runtime.module.misc.TokenizerType;
import com.qlangtech.tis.runtime.module.misc.VisualType;
import com.qlangtech.tis.solrdao.pojo.PSchemaField;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2021-01-27 13:08
 */
public class SchemaResult {

    private static final Logger logger = LoggerFactory.getLogger(SchemaResult.class);

    private boolean success = false;

    // 模板索引的id编号
    private int tplAppId;

    // protected final boolean xmlPost;

    public byte[] content;

    public boolean isSuccess() {
        return success;
    }

    protected SolrFieldsParser.ParseResult parseResult;

    public SolrFieldsParser.ParseResult getParseResult() {
        return this.parseResult;
    }

    public SchemaResult faild() {
        this.success = false;
        return this;
    }

    public static SchemaResult create(SolrFieldsParser.ParseResult parseResult, byte[] schemaContent) {
        SchemaResult schema = new SchemaResult();
        schema.parseResult = parseResult;
        schema.content = schemaContent;
        schema.success = true;
        return schema;
    }

//    public static SchemaResult parseSchemaResult(IMessageHandler module, Context context, byte[] schemaContent, boolean shallValidate
//            ,ISchemaFieldTypeContext schemaPlugin, SolrFieldsParser.ParseResultCallback... parseResultCallback) {
//        return parseSchemaResult(module, context, schemaContent, shallValidate, schemaPlugin, parseResultCallback);
//    }

    /**
     * 解析提交的schemaxml 内容
     *
     * @param context
     * @return
     * @throws Exception
     */
    public static SchemaResult parseSchemaResult(IMessageHandler module, Context context, byte[] schemaContent, boolean shallValidate
            , ISchemaFieldTypeContext schemaPlugin, SolrFieldsParser.ParseResultCallback... parseResultCallback) {
        if (schemaContent == null) {
            throw new IllegalStateException("schemaContent can not be null");
        }
        if (schemaPlugin == null) {
            throw new IllegalArgumentException("param schemaPlugin can not be null");
        }
        SolrFieldsParser.ParseResult parseResult;
        try {
            IIndexMetaData meta = SolrFieldsParser.parse(() -> schemaContent, schemaPlugin, shallValidate);
            parseResult = meta.getSchemaParseResult();
            for (SolrFieldsParser.ParseResultCallback process : parseResultCallback) {
                process.process(Collections.emptyList(), parseResult);
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            parseResult = new SolrFieldsParser.ParseResult(shallValidate);
            parseResult.errlist.add(e.getMessage());
        }
        if (!parseResult.isValid() || parseResult.errlist.size() > 0) {
            for (String err : parseResult.errlist) {
                module.addErrorMessage(context, err);
            }
            return create(null, schemaContent).faild();
        }
        return create(parseResult, schemaContent);
//        // new String(, getEncode());
//        result.content = schemaContent;
//        result.success = true;
//        result.parseResult = parseResult;
//        return result;
    }

    /**
     * 将xml中的solr type转换成小白模式下的可视化field type
     *
     * @param f
     * @param solrType
     * @param
     * @throws JSONException
     */
    private static void serialVisualType2Json(com.alibaba.fastjson.JSONObject f, SolrFieldsParser.SolrType solrType) throws JSONException {
        String type = solrType.getSType().getName();
        TokenizerType tokenizerType = TokenizerType.parse(type);
        if (tokenizerType == null) {
            // 非分词字段

            if (solrType.tokenizerable) {
//                f.put("split", true);
//                f.put(ISchemaField.KEY_FIELD_TYPE, ReflectSchemaFieldType.STRING.literia);
//                f.put("tokenizerType", type);
                setStringType(f, type);
            } else {
                f.put("split", false);
                VisualType vtype = TokenizerType.visualTypeMap.get(type);
                if (vtype != null) {
                    f.put(ISchemaField.KEY_FIELD_TYPE, vtype.getType());
                    return;
                }
                f.put(ISchemaField.KEY_FIELD_TYPE, type);
            }
        } else {
            // 分词字段
            setStringType(f, tokenizerType.getKey());
            //f.put("range", false);
        }
    }

    private static void setStringType(JSONObject f, String tokenizerType) {
        f.put("split", true);
        f.put(ISchemaField.KEY_FIELD_TYPE, ReflectSchemaFieldType.STRING.literia);
        f.put("tokenizerType", tokenizerType);
    }

    /**
     * @param schema
     * @param types
     * @throws JSONException
     */
    @SuppressWarnings("all")
    private void serialTypes(final com.alibaba.fastjson.JSONObject schema) throws JSONException {

        com.alibaba.fastjson.JSONArray types = new com.alibaba.fastjson.JSONArray();
        com.alibaba.fastjson.JSONObject f = null;
        com.alibaba.fastjson.JSONArray tokens = null;
        com.alibaba.fastjson.JSONObject tt = null;
        SolrFieldsParser.SolrType solrType = null;
        // Set<String> typesSet = new HashSet<String>();
        for (Map.Entry<String, VisualType> t : TokenizerType.visualTypeMap.entrySet()) {
            f = new com.alibaba.fastjson.JSONObject();
            f.put("name", t.getKey());
            f.put("split", t.getValue().isSplit());
            tokens = new com.alibaba.fastjson.JSONArray();
            if (t.getValue().isSplit()) {
                // 默认类型
                for (TokenizerType tokenType : t.getValue().getTokenerTypes()) {
                    tt = new com.alibaba.fastjson.JSONObject();
                    tt.put("key", tokenType.getKey());
                    tt.put("value", tokenType.getDesc());
                    tokens.add(tt);
                }
                // 外加类型
                for (Map.Entry<String, SolrFieldsParser.SolrType> entry : this.parseResult.types.entrySet()) {
                    solrType = entry.getValue();
                    if (solrType.tokenizerable) {
                        tt = new com.alibaba.fastjson.JSONObject();
                        tt.put("key", entry.getKey());
                        tt.put("value", entry.getKey());
                        tokens.add(tt);
                    }
                }
                f.put("tokensType", tokens);
            }
            types.add(f);
        }

        for (Map.Entry<String, SolrFieldsParser.SolrType> entry : this.parseResult.types.entrySet()) {
            solrType = entry.getValue();
            if (!solrType.tokenizerable && !TokenizerType.isContain(entry.getKey())) {
                f = new com.alibaba.fastjson.JSONObject();
                f.put("name", entry.getKey());
                types.add(f);
            }
        }
        schema.put("fieldtypes", types);
    }

    /**
     * 取得普通模式多字段
     *
     * @throws Exception
     */
    public com.alibaba.fastjson.JSONObject toJSON() {
        SchemaResult result = this;
        SolrFieldsParser.ParseResult parseResult = result.parseResult;
        final com.alibaba.fastjson.JSONObject schema = new com.alibaba.fastjson.JSONObject();
        if (result.getTplAppId() > 0) {
            schema.put("tplAppId", result.getTplAppId());
        }
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
        for (PSchemaField field : parseResult.dFields) {
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
            if (field.getType() != null) {
//                type = field.getType().getSType().getName();
//                serialVisualType2Json(f, type);
                // type = field.getType().getSType().getName();
                serialVisualType2Json(f, field.getType());
            } else {
                throw new IllegalStateException("field:" + field.getName() + " 's fieldType is can not be null");
            }
            f.put("docval", field.isDocValue());
            f.put("indexed", field.isIndexed());
            f.put("multiValue", field.isMltiValued());
            f.put("required", field.isRequired());
            f.put("stored", field.isStored());
            fields.add(f);
        }
        schema.put("fields", fields);
        serialTypes(schema);
        // this.setBizResult(context, schema);
        return schema;
    }

    public int getTplAppId() {
        return tplAppId;
    }

    public void setTplAppId(int tplAppId) {
        this.tplAppId = tplAppId;
    }

    public SchemaResult() {
        super();
        //this.xmlPost = xmlPost;
    }
}
