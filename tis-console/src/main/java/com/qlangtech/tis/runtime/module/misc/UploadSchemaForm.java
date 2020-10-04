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
package com.qlangtech.tis.runtime.module.misc;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import com.qlangtech.tis.solrdao.ISchema;
import com.qlangtech.tis.solrdao.ISchemaField;

/**
 * 面向前端使用對象模型
 *
 * @author 百岁（baisui@qlangtech.com）
 * @date 2017年5月8日
 */
public class UploadSchemaForm implements ISchema {

    private final List<SchemaField> fields = new ArrayList<SchemaField>();

    // private final Set<String> fieldKeys = new HashSet<String>();
    public List<ISchemaField> getSchemaFields() {
        List<ISchemaField> result = new ArrayList<>();
        for (SchemaField f : fields) {
            result.add(f);
        }
        return result;
    }

    public List<SchemaField> getFields() {
        return this.fields;
    }

    public boolean containsField(String name) {
        for (SchemaField f : this.fields) {
            if (StringUtils.equals(f.getName(), name)) {
                return true;
            }
        }
        return false;
    }

    public int getFieldCount() {
        return this.fields.size();
    }

    private String memo;

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getUniqueKey() {
        for (SchemaField f : this.fields) {
            if (f.isUniqueKey()) {
                return f.getName();
            }
        }
        return StringUtils.EMPTY;
    }

    public String getShareKey() {
        for (SchemaField f : this.fields) {
            if (f.isSharedKey()) {
                return f.getName();
            }
        }
        return StringUtils.EMPTY;
    }

    @Override
    public String getSharedKey() {
        return this.getShareKey();
    }
}
