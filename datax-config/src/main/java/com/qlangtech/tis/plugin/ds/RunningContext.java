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

import java.io.Serializable;
import java.util.Map;
import java.util.function.Function;

/**
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2024-08-02 12:46
 **/
public
interface RunningContext extends Serializable {

    public class RunningContextParamSetter implements Serializable {
        private final RunningContext runningContext;
        private final Map<String, Function<RunningContext, Object>> contextParamValsGetterMapper;

        public RunningContextParamSetter(RunningContext runningContext, Map<String, Function<RunningContext, Object>> contextParamValsGetterMapper) {
            this.runningContext = runningContext;
            this.contextParamValsGetterMapper = contextParamValsGetterMapper;
        }

        public void setContextParam(Map<String, Object> vals) {
            contextParamValsGetterMapper.forEach((contextParamName, getter) -> {
                vals.put(contextParamName, getter.apply(runningContext));
            });
        }
    }

    public String getDbName();

    public String getTable();
}
