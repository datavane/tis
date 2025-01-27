/**
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.qlangtech.tis.plugin.datax.common.impl;

import com.qlangtech.tis.datax.IDataxProcessor.TableMap;
import com.qlangtech.tis.datax.SourceColMetaGetter;
import com.qlangtech.tis.datax.impl.DataxWriter;
import com.qlangtech.tis.extension.TISExtension;
import com.qlangtech.tis.plugin.IEndTypeGetter.EndType;
import com.qlangtech.tis.plugin.datax.CreateTableSqlBuilder;
import com.qlangtech.tis.plugin.datax.CreateTableSqlBuilder.ColWrapper;
import com.qlangtech.tis.plugin.datax.common.AutoCreateTable;
import com.qlangtech.tis.plugin.datax.common.AutoCreateTableColCommentSwitch;
import com.qlangtech.tis.plugin.datax.transformer.RecordTransformerRules;

import java.util.Optional;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-12-14 11:31
 **/
public class NoneCreateTable extends AutoCreateTable<ColWrapper> {
    @Override
    public boolean enabled() {
        return false;
    }

    @Override
    public AutoCreateTableColCommentSwitch getAddComment() {
        return new AutoCreateTableColCommentSwitchOFF();
    }

    @Override
    public CreateTableSqlBuilder<ColWrapper> createSQLDDLBuilder(
            DataxWriter rdbmsWriter, SourceColMetaGetter sourceColMetaGetter
            , TableMap tableMapper, Optional<RecordTransformerRules> transformers) {
        throw new UnsupportedOperationException();
    }

//    @Override
//    public void addStandardColComment(
//            SourceColMetaGetter sourceColMetaGetter, TableMap tableMapper, ColWrapper colWrapper, BlockScriptBuffer ddlScript) {
//
//    }

    @TISExtension
    public static final class DftDesc extends BasicDescriptor {
        public DftDesc() {
            super();
        }

        @Override
        public String getDisplayName() {
            return SWITCH_OFF;
        }

        @Override
        public EndType getEndType() {
            return null;
        }
    }
}
