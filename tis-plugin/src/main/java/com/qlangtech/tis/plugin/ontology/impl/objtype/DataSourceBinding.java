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
import com.qlangtech.tis.manage.common.Option;
import com.qlangtech.tis.manage.common.OptionWithEndType;
import com.qlangtech.tis.plugin.IEndTypeGetter;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.ds.ColumnMetaData;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;
import com.qlangtech.tis.plugin.ds.TableNotFoundException;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import com.qlangtech.tis.util.IPluginContext;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/5/8
 */
public class DataSourceBinding extends ObjectTypeBinding {

    private static final String KEY_DISPLAY_NAME = "DataSource Bound";

    private static final String KEY_DB_NAME = "dbName";
    private static final String KEY_PHYSICAL_TABLE_NAME = "physicalTableName";
    @FormField(ordinal = 0, type = FormFieldType.ENUM, validate = {Validator.require})
    public String dbName;

    /**
     * 物理表名（可空）。空时回落到 ObjectType 的 name；
     * 用于「ObjectType 逻辑名 ≠ Doris 物理表名」的场景，例如本体侧用中文语义名而 Doris 侧用下划线英文表名。
     */
    @FormField(ordinal = 1, type = FormFieldType.ENUM, validate = {Validator.require, Validator.db_col_name})
    public String physicalTableName;

    public DataSourceFactory getDataSrouce() {
        return DataSourceFactory.load(this.dbName);
    }
    

    @Override
    public List<ColumnMetaData> resolveTabCols() {
        try {
            return getDataSrouce().getTableMetadata(false, IPluginContext.getThreadLocalInstance(),
                    EntityName.parse(this.physicalTableName));
        } catch (TableNotFoundException e) {
            throw new RuntimeException(e);
        }
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
            this.valueChangePipe(KEY_DB_NAME, KEY_PHYSICAL_TABLE_NAME).render((meta, params) -> {
                String dbName = params.getString(KEY_DB_NAME);
                DataSourceFactory dataSource = DataSourceFactory.load(dbName);
                List<String> tabs = dataSource.getTablesInDB().getTabs();
                return tabs.stream().map((tab) -> new OptionWithEndType(tab, tab, IEndTypeGetter.EndType.Table)).toList();
            });
        }

        @Override
        public String getDisplayName() {
            return KEY_DISPLAY_NAME;
        }
    }
}