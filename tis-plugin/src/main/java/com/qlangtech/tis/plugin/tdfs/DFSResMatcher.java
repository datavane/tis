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

package com.qlangtech.tis.plugin.tdfs;

import com.google.common.collect.Sets;
import com.qlangtech.tis.datax.IDataxProcessor;
import com.qlangtech.tis.datax.IGroupChildTaskIterator;
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;
import com.qlangtech.tis.plugin.ds.CMeta;
import com.qlangtech.tis.plugin.ds.ColumnMetaData;
import com.qlangtech.tis.plugin.ds.ISelectedTab;
import com.qlangtech.tis.plugin.ds.TableNotFoundException;
import com.qlangtech.tis.sql.parser.tuple.creator.EntityName;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * dfs 资源名称 查找
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2023-08-09 10:19
 **/
public abstract class DFSResMatcher implements Describable<DFSResMatcher> {

    @FormField(ordinal = 14, type = FormFieldType.INT_NUMBER, validate = {Validator.require})
    public Integer maxTraversalLevel;

    /**
     * 在某些情况下DataXReader/Writer会在运行期由于内部属性变化，改变对应是否支持rdbms的属性，而不再由descriptpr通过静态的方式决定
     *
     * @return
     */
    public abstract boolean isRDBMSSupport();

    /**
     * @param hdfsSession
     * @param paths
     * @param processor
     * @return
     */

    /**
     *
     * @param hdfsSession
     * @param entityName  使用hive或meta OSS，需要在datax config 配置文件中 添加该参数表明 hive的表名
     * @param path dataX 配置文件中的path属性
     * @param processor
     * @return
     */
    public abstract SourceColsMeta getSourceColsMeta(ITDFSSession hdfsSession, Optional<String> entityName, String path, IDataxProcessor processor);

    public abstract List<ISelectedTab> getSelectedTabs(IDFSReader dfsReader);

    public abstract List<ColumnMetaData> getTableMetadata(IDFSReader dfsReader, EntityName table) throws TableNotFoundException;

    public abstract boolean hasMulitTable(IDFSReader dfsReader);

    public abstract IGroupChildTaskIterator getSubTasks(Predicate<ISelectedTab> filter, IDFSReader dfsReader);

    public final Set<ITDFSSession.Res> findAllRes(ITDFSSession dfsSession) {
        return findAllRes(dfsSession, Collections.singletonList(dfsSession.getRootPath()));
    }

    /**
     * 查找 TDFSLinker.getRootPath() 下的所有匹配的资源文件
     *
     * @param dfsSession
     * @return
     * @see TDFSLinker
     */
    public final Set<ITDFSSession.Res> findAllRes(ITDFSSession dfsSession, List<String> targetPath) {
        Set<ITDFSSession.Res> candidate = dfsSession.getAllFiles(targetPath, 0, this.maxTraversalLevel);
        // Set<ITDFSSession.Res> candidate = dfsSession.getListFiles(dfsSession.getRootPath(), 0, this.maxTraversalLevel);
        Set<ITDFSSession.Res> result = Sets.newHashSet();
        for (ITDFSSession.Res path : candidate) {
            if (this.isMatch(path)) {
                result.add(path);
            }
        }
        return result;
    }

    /**
     * 资源名称是否匹配
     *
     * @param testRes
     * @return
     */
    public abstract boolean isMatch(ITDFSSession.Res testRes);


    public static class SourceColsMeta {
        private final List<CMeta> colsMeta;
        private final Predicate<String> colSelectedPredicate;

        public SourceColsMeta(List<CMeta> colsMeta) {
            this(colsMeta, (col) -> true);
        }

        public SourceColsMeta(List<CMeta> colsMeta, Predicate<String> colSelectedPredicate) {
            this.colsMeta = colsMeta;
            this.colSelectedPredicate = colSelectedPredicate;
        }

        public boolean colSelected(String col) {
            return colSelectedPredicate.test(col);
        }

        public List<CMeta> getColsMeta() {
            return this.colsMeta;
        }
    }
}
