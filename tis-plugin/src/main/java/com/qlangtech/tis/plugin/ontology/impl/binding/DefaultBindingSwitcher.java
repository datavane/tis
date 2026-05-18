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
package com.qlangtech.tis.plugin.ontology.impl.binding;

import com.google.common.collect.Lists;
import com.qlangtech.tis.extension.Descriptor;
import com.qlangtech.tis.plugin.IPluginStore;
import com.qlangtech.tis.plugin.ds.ColumnMetaData;
import com.qlangtech.tis.plugin.ds.DataSourceFactory;
import com.qlangtech.tis.plugin.ds.TableInDB;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;
import com.qlangtech.tis.plugin.ontology.OntologyObjectType;
import com.qlangtech.tis.plugin.ontology.OntologyProperty;
import com.qlangtech.tis.plugin.ontology.impl.OntologyPluginMeta;
import com.qlangtech.tis.plugin.ontology.impl.objtype.DataSourceBinding;
import com.qlangtech.tis.plugin.ontology.impl.objtype.ObjectTypeBinding;
import com.qlangtech.tis.plugin.ontology.impl.objtype.ObjectTypeProfile;
import com.qlangtech.tis.util.IPluginContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@link OntologyBindingSwitcher} 的默认实现。
 *
 * @author 百岁 (baisui@qlangtech.com)
 * @date 2026/5/9
 */
public class DefaultBindingSwitcher implements OntologyBindingSwitcher {

    /**
     * 列差异阻塞阈值：差异比例 >= 该值时直接阻止切换。
     */
    private static final double COLUMN_DIFF_BLOCK_RATIO = 0.1d;

    @Override
    public BindingSwitchReport validate(IPluginContext pluginContext, OntologyObjectType ot,
                                        DataSourceFactory tagetDS) {


        // 物理表名（DataSourceBinding.physicalTableName 留空则回落为 OT name）
        String physicalTable = ot.getName();
        ObjectTypeBinding binding = ot.getProfile().binding;
        if (binding instanceof DataSourceBinding dsb) {
            physicalTable = dsb.resolvePhysicalTableName(ot.getName());
        }

        List<ColumnMetaData> dsCols;
        try {
            dsCols = tagetDS.getTableMetadata(false, pluginContext,
                    EntityName.parse(physicalTable));
        } catch (Exception e) {
            return BindingSwitchReport.blocked("failed to read columns of table '" + physicalTable + "': " + e.getMessage());
        }

        Set<String> otColNames = ot.getCols().stream()
                .map(OntologyProperty::getName).map(String::toLowerCase).collect(Collectors.toCollection(HashSet::new));
        Set<String> dsColNames = dsCols.stream()
                .map(ColumnMetaData::getName).map(String::toLowerCase).collect(Collectors.toCollection(HashSet::new));

        List<String> missing = Lists.newArrayList(otColNames);
        missing.removeAll(dsColNames);
        Collections.sort(missing);

        List<String> extra = Lists.newArrayList(dsColNames);
        extra.removeAll(otColNames);
        Collections.sort(extra);

        int otSize = Math.max(otColNames.size(), 1);
        double diffRatio = (double) missing.size() / (double) otSize;
        if (diffRatio >= COLUMN_DIFF_BLOCK_RATIO) {
            return new BindingSwitchReport(false, missing, extra, List.of(),
                    "column diff " + String.format("%.1f%%", diffRatio * 100) + " >= threshold");
        }

        return new BindingSwitchReport(true, missing, extra, List.of(), null);
    }

    @Override
    public void switchBinding(OntologyObjectType ot, DataSourceFactory tagetDS, IPluginContext ctx) {
        BindingSwitchReport report = validate(ctx, ot, tagetDS);
        if (!report.ok()) {
            throw new IllegalStateException("binding switch blocked: " + report.error());
        }
        ObjectTypeProfile profile = ot.getProfile();
        DataSourceBinding newBinding = new DataSourceBinding();
        newBinding.dbName = tagetDS.name;
        if (profile.binding instanceof DataSourceBinding old) {
            newBinding.physicalTableName = old.physicalTableName;
        }
        profile.binding = newBinding;

        IPluginStore<OntologyObjectType> store = OntologyObjectType.getPluginStore(
                Objects.requireNonNull(OntologyPluginMeta.createPluginMeta(ctx.getContext()).getDomain(),
                        "domain can not be null"),
                ot.getName());
        store.setPlugins(ctx, Optional.empty(),
                Collections.singletonList(new Descriptor.ParseDescribable<>(ot)));
    }
}
