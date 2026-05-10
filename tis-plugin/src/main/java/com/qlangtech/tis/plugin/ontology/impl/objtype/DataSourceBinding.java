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

package com.qlangtech.tis.plugin.ontology.impl.objtype;

import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/5/8
 */
public class DataSourceBinding extends ObjectTypeBinding {

    private static final String KEY_DISPLAY_NAME = "DataSource Bound";

    @FormField(ordinal = 0, type = FormFieldType.ENUM, validate = {Validator.require})
    public String dbName;

    /**
     * 物理表名（可空）。空时回落到 ObjectType 的 name；
     * 用于「ObjectType 逻辑名 ≠ Doris 物理表名」的场景，例如本体侧用中文语义名而 Doris 侧用下划线英文表名。
     */
    @FormField(ordinal = 1, type = FormFieldType.INPUTTEXT, validate = {Validator.db_col_name})
    public String physicalTableName;

    public DataSourceFactory getDataSrouce() {
        return DataSourceFactory.load(this.dbName);
    }

    public String resolvePhysicalTableName(String objectTypeName) {
        return StringUtils.isBlank(this.physicalTableName) ? objectTypeName : this.physicalTableName;
    }

    @Override
    public ObjectTypeBindingInfo getObjectTypeBindingInfo() {
        final String dsName = getDataSrouce().name;
        if (getDataSrouce().getDescriptor() instanceof IEndTypeGetter endTypeGetter) {
            return new ObjectTypeBindingInfo(dsName, endTypeGetter.getEndType());
        }
        return new ObjectTypeBindingInfo(dsName, IEndTypeGetter.EndType.UnKnowStoreType);
    }

    @TISExtension
    public static class DftDesc extends Descriptor<ObjectTypeBinding> {
        public DftDesc() {
            super();
        }

        @Override
        public String getDisplayName() {
            return KEY_DISPLAY_NAME;
        }
    }
}