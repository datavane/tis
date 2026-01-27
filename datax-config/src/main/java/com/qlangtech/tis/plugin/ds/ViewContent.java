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
    Unknow("unknow");
    private String token;

    private ViewContent(String token) {
        this.token = token;
    }

    public String getToken() {
        return this.token;
    }
}
