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
package com.qlangtech.tis.solrextend.fieldtype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.index.IndexableField;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.schema.StrField;
import org.json.JSONObject;
import org.json.JSONTokener;
import com.google.common.collect.Sets;

/**
 * 之前JSONField的实现不太雅观，重新又实现了一个
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2019年1月17日
 */
public class JSONField2 extends StrField {

    private String propPrefix;

    private Set<String> filterKeys;

    // 是否要存储所有的字段，即使設置了filterKeys
    protected Set<String> storeIncludesSet;

    protected IndexSchema schema;

    protected void init(IndexSchema schema, Map<String, String> args) {
        super.init(schema, args);
        this.propPrefix = (args.remove("prefix"));
        // 是否要存储所有的json属性字段，即使設置了filterKeys
        String storeIncludes = args.remove("storeIncludes");
        if (StringUtils.isNotBlank(storeIncludes)) {
            this.storeIncludesSet = Sets.newHashSet(StringUtils.split(storeIncludes, ","));
        }
        final String fk = args.remove("filterKey");
        if (StringUtils.isNotBlank(fk)) {
            this.filterKeys = Sets.newHashSet(StringUtils.split(fk, ","));
        }
        this.schema = schema;
    }

    @Override
    public List<IndexableField> createFields(SchemaField sf, Object value) {
        List<IndexableField> result = new ArrayList<>();
        String textValue = String.valueOf(value);
        if (value == null || !StringUtils.startsWith(textValue, "{")) {
            return Collections.emptyList();
        }
        JSONTokener tokener = new JSONTokener(textValue);
        JSONObject json = new JSONObject(tokener);
        if (isPropPrefixNotEmpty()) {
            String val = null;
            for (String key : json.keySet()) {
                val = String.valueOf(json.get(key));
                if ("null".equalsIgnoreCase(val) || (filterKeys != null && !this.filterKeys.contains(key))) {
                    continue;
                }
                result.add(schema.getFieldOrNull(getPropPrefix() + key).createField(val));
                addExtraField(result, key, val);
            }
            this.addExtraField(result, json);
        }
        if (sf.stored()) {
            if (storeIncludesSet != null && !storeIncludesSet.isEmpty()) {
                json = new JSONObject(json, this.storeIncludesSet.toArray(new String[] {}));
            } else if (filterKeys != null && !this.filterKeys.isEmpty()) {
                json = new JSONObject(json, this.filterKeys.toArray(new String[] {}));
            }
            result.add(this.createField(sf, json.toString()));
        }
        return result;
    }

    protected boolean isPropPrefixNotEmpty() {
        return StringUtils.isNotEmpty(this.getPropPrefix());
    }

    protected void addExtraField(List<IndexableField> result, String key, String val) {
    }

    protected void addExtraField(List<IndexableField> result, JSONObject extension) {
    }

    @Override
    public boolean isPolyField() {
        return true;
    }

    protected String getPropPrefix() {
        return propPrefix;
    }
}
