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

package com.alibaba.datax.core.job;

import com.qlangtech.tis.plugin.ds.IColMetaGetter;
import com.qlangtech.tis.plugin.ds.RunningContext;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-06-15 12:34
 * // @see com.qlangtech.tis.plugin.datax.transformer.RecordTransformerRules
 **/
public interface ITransformerBuildInfo {
    /**
     * 是否有当前上下文绑定参数
     *
     * @return
     */
    boolean containContextParams();

    /**
     * 取得上下文
     *
     * @param runningContext
     * @return
     */
    Map<String, Object> contextParamVals(RunningContext runningContext);

    /**
     * 取得上下文参数取得器，用于在增量实时通道中提取增量
     *
     * @param <CONTEXT>
     * @return
     */
    <CONTEXT extends RunningContext> Map<String, Function<CONTEXT, Object>> contextParamValsGetter();

    /**
     * 取得source端对应字段的meta（未经transformer处理，通过T之后字段类型可能已经变化），该方法在增量处理中使用
     * @return
     */
    public List<IColMetaGetter> originColsWithContextParams();

    /**
     * 取得执行当前上线文绑定的参数，例如，当前数据库的名称等
     *
     * @return
     */
    // List<ContextParamConfig> getContextParms();

    // List<String> relevantOutterColKeys();
    public <T extends IColMetaGetter> List<IColMetaGetter> overwriteColsWithContextParams(List<T> sourceCols);
}
