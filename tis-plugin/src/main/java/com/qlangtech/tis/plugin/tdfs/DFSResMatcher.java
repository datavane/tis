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
import com.qlangtech.tis.extension.Describable;
import com.qlangtech.tis.plugin.annotation.FormField;
import com.qlangtech.tis.plugin.annotation.FormFieldType;
import com.qlangtech.tis.plugin.annotation.Validator;

import java.util.Set;

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
     * 查找 TDFSLinker.getRootPath() 下的所有匹配的资源文件
     *
     * @param dfsSession
     * @return
     * @see TDFSLinker
     */
    public final Set<ITDFSSession.Res> findAllRes(ITDFSSession dfsSession) {

        Set<ITDFSSession.Res> candidate = dfsSession.getListFiles(dfsSession.getRootPath(), 0, this.maxTraversalLevel);
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
}
