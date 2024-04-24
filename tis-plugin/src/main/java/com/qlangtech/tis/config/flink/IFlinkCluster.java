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

package com.qlangtech.tis.config.flink;

import com.google.common.collect.Sets;
import com.qlangtech.tis.plugin.IdentityName;

import java.util.Collections;
import java.util.Set;

/**
 * FlinkCluster Configuration
 *
 * @author: 百岁（baisui@qlangtech.com）
 * @create: 2021-10-23 12:21
 **/
public interface IFlinkCluster extends IdentityName, IFlinkClusterConfig {
    String PLUGIN_DEPENDENCY_FLINK_DEPENDENCY = "tis-flink-dependency";
    String PLUGIN_SKIP_FLINK_EXTENDS = "tis-flink-extends-plugin";
    String PLUGIN_TIS_DATAX_LOCAL_EXECOTOR = "tis-datax-local-executor";
    String SKIP_CLASSLOADER_FACTORY_CREATION = "skip_classloader_factory_creation";
    String KEY_DISPLAY_NAME = "Flink-Cluster";
    Set<String> SKIP_PLUGIN_NAMES = Collections.unmodifiableSet(
            Sets.newHashSet(IFlinkCluster.PLUGIN_DEPENDENCY_FLINK_DEPENDENCY//
                    , IFlinkCluster.PLUGIN_SKIP_FLINK_EXTENDS //
                    , PLUGIN_TIS_DATAX_LOCAL_EXECOTOR));
}
