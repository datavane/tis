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

package com.qlangtech.tis.plugin.datax;

import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.plugin.datax.transformer.RecordTransformerRules;
import com.qlangtech.tis.plugin.ds.CMeta;
import com.qlangtech.tis.plugin.ds.DataSourceMeta;
import com.qlangtech.tis.plugin.ds.IColMetaGetter;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2023-05-07 12:35
 **/
public abstract class AbstractCreateTableSqlBuilder {


    protected final String targetTableName;
    protected final List<IColMetaGetter> cols;
    protected final List<String> pks;
    public int maxColNameLength;
    protected final DataSourceMeta dsMeta;
    private final Optional<RecordTransformerRules> transformers;

    public AbstractCreateTableSqlBuilder(
            IDataxProcessor.TableMap tableMapper, DataSourceMeta dsMeta, Optional<RecordTransformerRules> transformers) {
        this.targetTableName = tableMapper.getTo();
        this.transformers = Objects.requireNonNull(transformers, "param transformers can not be null");

        this.pks = tableMapper.getSourceTab().getPrimaryKeys();

        List<IColMetaGetter> sourceCols = tableMapper.getSourceCols().stream().map((c) -> c).collect(Collectors.toList());

        if (transformers.isPresent()) {
            RecordTransformerRules transformerRules = transformers.get();
            sourceCols = transformerRules.overwriteCols(sourceCols);
        }

        this.cols = Collections.unmodifiableList(sourceCols);

        maxColNameLength = 0;
        for (IColMetaGetter col : this.getCols()) {
            int m = StringUtils.length(col.getName());
            if (m > maxColNameLength) {
                maxColNameLength = m;
            }
        }
        maxColNameLength += 4;
        this.dsMeta = dsMeta;
//        if (supportColEscapeChar()) {
//            Optional<String> escape = dsMeta.getEscapeChar();
//            if (!escape.isPresent()) {
//                throw new IllegalArgumentException("must contain escapeChar for DB entity");
//            }
//            this.escapeChar = escape.get();
//        } else {
//            this.escapeChar = StringUtils.EMPTY;
//        }
    }

    public abstract CreateTableSqlBuilder.CreateDDL build();


    protected CreateTableSqlBuilder.CreateTableName getCreateTableName() {
        return new CreateTableSqlBuilder.CreateTableName(targetTableName, this);
    }

    protected String wrapWithEscape(String val) {
        return dsMeta.getEscapedEntity(val);
    }

//    protected boolean supportColEscapeChar() {
//        return true;
//    }

    /**
     * @return
     * @see CMeta default implement class
     */
    public List<IColMetaGetter> getCols() {
        return this.cols;
    }
//    {
//        return this.tableMapper.getSourceCols();
//    }

    protected abstract CreateTableSqlBuilder.ColWrapper createColWrapper(IColMetaGetter c);//{


    public static class CreateDDL {
        private final StringBuffer script;
        private final AbstractCreateTableSqlBuilder builder;

        public CreateDDL(StringBuffer script, AbstractCreateTableSqlBuilder builder) {
            this.script = script;
            this.builder = builder;
        }

        public StringBuffer getDDLScript() {
            return this.script;
        }

        public String getSelectAllScript() {
            return "SELECT " + builder.getCols().stream().map((c) -> builder.wrapWithEscape(c.getName())).collect(Collectors.joining(",")) + " FROM " + (builder.getCreateTableName().getEntityName());
        }
    }


}
