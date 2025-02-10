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

package com.qlangtech.tis.plugin.datax.common;

import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.datax.SourceColMetaGetter;
import com.qlangtech.tis.datax.impl.DataxWriter;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.plugin.IEndTypeGetter.EndType;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.datax.CreateTableSqlBuilder;
import com.qlangtech.tis.plugin.datax.CreateTableSqlBuilder.ColWrapper;
import com.qlangtech.tis.plugin.datax.common.impl.NoneCreateTable;
import com.qlangtech.tis.plugin.datax.transformer.RecordTransformerRules;
import org.apache.commons.collections.CollectionUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 自动创建目标表
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-12-14 11:29
 **/
//@TISExtensible
public abstract class AutoCreateTable<COL_WRAPPER extends ColWrapper> implements Describable<AutoCreateTable<COL_WRAPPER>>, Serializable {

    @FormField(ordinal = 100, advance = true, type = FormFieldType.INPUTTEXT, validate = {Validator.identity})
    public String aliasPrefix;

    /**
     * 取得映射表的前缀
     *
     * @return
     */
    public Optional<String> getMapperTabPrefix() {
        return Optional.ofNullable(aliasPrefix);
    }

    public abstract AutoCreateTableColCommentSwitch getAddComment();

    public static List<BasicDescriptor> descFilter(List<BasicDescriptor> descs, String endType) {
        return descFilter(descs, endType, null);
    }

    /**
     * 过滤得到目标实例
     *
     * @param descs
     * @param endType
     * @return
     */
    public static List<BasicDescriptor> descFilter(List<BasicDescriptor> descs, String endType, String compatibleModeEndType) {
        if (CollectionUtils.isEmpty(descs)) {
            return Collections.emptyList();
        }
        EndType targetEndType = EndType.parse(endType);
        final Optional<EndType> compatibleModeEndTypeFilter = Optional.ofNullable(compatibleModeEndType).map((e) -> EndType.parse(compatibleModeEndType));

        return descs.stream().filter((desc) -> {
            boolean dsEndTypeMatch = (desc.getEndType() == null || targetEndType == desc.getEndType());
            if (dsEndTypeMatch && compatibleModeEndTypeFilter.isPresent()) {
                return desc.getCompatibleModeEndType()
                        .map((e) -> e == compatibleModeEndTypeFilter.get())
                        .orElse(false);
//                if (desc.getCompatibleModeEndType().isEmpty()) {
//                    return false;
//                }
//                return compatibleModeEndTypeFilter.get() == desc.getCompatibleModeEndType().get();
            }
            return dsEndTypeMatch;
        }).collect(Collectors.toList());
    }

    public static AutoCreateTable dft() {
        return new NoneCreateTable();
    }

    public abstract CreateTableSqlBuilder<COL_WRAPPER> createSQLDDLBuilder(
            DataxWriter rdbmsWriter, SourceColMetaGetter sourceColMetaGetter
            , IDataxProcessor.TableMap tableMapper, Optional<RecordTransformerRules> transformers);

    /**
     * 是否开启了
     *
     * @return
     */
    public abstract boolean enabled();

    public boolean enabledColumnComment() {
        return false;
    }


    @Override
    public Descriptor<AutoCreateTable<COL_WRAPPER>> getDescriptor() {
        Descriptor<AutoCreateTable<COL_WRAPPER>> desc = Describable.super.getDescriptor();

        if (!BasicDescriptor.class.isAssignableFrom(desc.getClass())) {
            throw new IllegalStateException("desc class:" + desc.getClass().getName()
                    + " must be child of " + BasicDescriptor.class.getName());
        }

        return desc;
    }

    public static abstract class BasicDescriptor extends Descriptor<AutoCreateTable> {

        public abstract EndType getEndType();

        /**
         * 类似取得KingBase支持的SQL关系数据库类型
         *
         * @return
         */
        public Optional<EndType> getCompatibleModeEndType() {
            return Optional.empty();
        }
    }
}
