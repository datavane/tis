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

package com.qlangtech.tis.plugin.ds;

/**
 * <a href="https://www.processon.com/diagraming/69689f89faec1a656012e77d">...</a>
 */
public enum ViewContent {
    TransformerRules("transformerRules"),
    MongoCols("mongoCols"),
    JdbcTypeProps("jdbcTypeProps"),
    /**
     * 两个表执行join操作
     */
    TableJoinMatchCondition("tableJoinMatchCondition"),
    /**
     * 表JOIN时的过滤条件
     */
    TableJoinFilterCondition("tableJoinFilterCondition"),
    /**
     * 多个可选的单个值
     * see：//BasicMultiSelectSingleValElementCreatorFactory
     */
    MultiSelectSingleVal("multiSelectSingleVal", false),
    /**
     * 本体属性Measure连接器
     */
    OntologyPropRoleTypeLinker("ontologyPropRoleTypeLinker", false),

    /**
     * 本体object_type 属性(s)列表
     */
    OntologyProps("ontologyProps", false),
    /**
     * 本体资源推理结果列表
     */
    OntologyResInference("ontologyResInference", false),

    /**
     * Workshop 过滤列表（FilterListWidget）中的过滤项行。
     *
     * <p>每行是「控件形态 + 显示名 + 目标属性 + 算子」，没有列类型元数据的概念，
     * 故 {@code colTypeMetasAware = false}（同 {@link #OntologyProps} 一族）。
     *
     * @see com.qlangtech.tis.plugin.ds.ElementCreatorFactory
     */
    WorkshopFilterItems("workshopFilterItems", false),

    /**
     * Workshop 变量的「接口输入映射」行（{@code MappingInterfaceConfig.inputs}）。
     *
     * <p>每行是「外部接口参数名 + 模块内变量名」，同样没有列类型元数据的概念，
     * 故 {@code colTypeMetasAware = false}（同 {@link #WorkshopFilterItems} 一族）。
     *
     * @see com.qlangtech.tis.plugin.ds.ElementCreatorFactory
     */
    InterfaceInputRows("interfaceInputRows", false),

    /**
     * Workshop 变量的「路由参数映射」行（{@code PageRoutingConfig.params}）。
     *
     * <p>每行是「URL 参数名 + 模块内变量名」，同样没有列类型元数据的概念。
     *
     * @see com.qlangtech.tis.plugin.ds.ElementCreatorFactory
     */
    RoutingParamRows("routingParamRows", false),

    Unknow("unknow", false);

    private String token;
    private boolean colTypeMetasAware;

    private ViewContent(String token) {
        this(token, true);
    }

    /**
     *
     * @param token
     * @param colTypeMetasAware 是否需要包含 colType的信息
     * @see DataTypeMeta#createViewBiz(DataTypeMeta.IMultiItemsView, Object)
     */
    private ViewContent(String token, boolean colTypeMetasAware) {
        this.token = token;
        this.colTypeMetasAware = colTypeMetasAware;
    }

    public boolean isColTypeMetasAware() {
        return this.colTypeMetasAware;
    }

    public String getToken() {
        return this.token;
    }
}
