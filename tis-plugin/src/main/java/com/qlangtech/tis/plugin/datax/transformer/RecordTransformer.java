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

package com.qlangtech.tis.plugin.datax.transformer;

import com.qlangtech.tis.plugin.ds.TypeBase;

/**
 * 定义一条 记录处理规则
 */
public class RecordTransformer extends TypeBase {


    public final String getName() {
        return target;
    }

    /**
     * 自定义规则
     */
    private UDFDefinition udf;
    private String target;

    public UDFDefinition getUdf() {
        return udf;
    }

    public void setUdf(UDFDefinition udf) {
        this.udf = udf;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}
